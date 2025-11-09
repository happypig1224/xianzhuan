package com.xianzhuan.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xianzhuan.constant.MessageConstant;
import com.xianzhuan.context.BaseContext;
import com.xianzhuan.dto.*;
import com.xianzhuan.entity.*;
import com.xianzhuan.exception.AddressBusinessException;
import com.xianzhuan.exception.OrderBusinessException;
import com.xianzhuan.exception.ShoppingCartBusinessException;
import com.xianzhuan.mapper.*;
import com.xianzhuan.result.PageResult;
import com.xianzhuan.service.LocalMessageService;
import com.xianzhuan.service.OrderService;
import com.xianzhuan.service.UserMessageService;
import com.xianzhuan.utils.MemoryCacheUtil;
import com.xianzhuan.utils.RedisConstant;
import com.xianzhuan.vo.OrderPaymentVO;
import com.xianzhuan.vo.OrderStatisticsVO;
import com.xianzhuan.vo.OrderSubmitVO;
import com.xianzhuan.vo.OrderVO;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.xianzhuan.entity.LocalMessage.TYPE_ORDER_TIMEOUT;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class OrderServiceImpl implements OrderService {
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    // 编程式事务
    private TransactionTemplate transactionTemplate;
    @Autowired
    private LocalMessageService localMessageService;
    @Autowired
    private UserMessageService userMessageService;
    @Override
    public String generateToken() {
        Long userId = BaseContext.getCurrentId();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String tokenKey = RedisConstant.ORDER_TOKEN_KEY + userId + ":" + uuid;
        try {
            stringRedisTemplate.opsForValue().set(tokenKey, "1", RedisConstant.ORDER_TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis不可用，Token存储降级，userId: {}", userId);
        }
        return uuid;
    }

    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //购物车为空,地址未选,超出配送范围抛异常
        Address address = addressMapper.getById(ordersSubmitDTO.getAddressId());
        if (address == null) {
            throw new AddressBusinessException(MessageConstant.ADDRESS_IS_NULL);
        }
        //获取购物车列表
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        // Token令牌验证（防重复提交）
        String tokenKey = RedisConstant.ORDER_TOKEN_KEY + userId + ":" + ordersSubmitDTO.getToken();
        try {
            Boolean deleted = stringRedisTemplate.delete(tokenKey);
            if (Boolean.FALSE.equals(deleted)) {
                throw new OrderBusinessException(MessageConstant.ORDER_REPEAT_SUBMIT);
            }
        } catch (OrderBusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Redis不可用，Token验证降级放行，依赖数据库唯一索引兜底，userId: {}", userId);
        }

        //1.更新订单表数据
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, order);
        order.setAddress(address.getDetail());
        order.setNumber(UUID.randomUUID().toString().replace("-", ""));
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPayStatus(Orders.UN_PAID);
        order.setUserName(address.getConsignee());
        order.setPhone(address.getPhone());
        order.setUserId(userId);
        order.setOrderTime(LocalDateTime.now());
        
        Boolean result = transactionTemplate.execute(transactionStatus -> {
            orderMapper.insert(order);
            //2.更新详细订单表的数据
            List<OrderDetail> orderDetailList = new ArrayList<>();
            shoppingCartList.forEach(cart -> {
                OrderDetail orderDetail = new OrderDetail();
                BeanUtils.copyProperties(cart, orderDetail);
                orderDetail.setOrderId(order.getId());
                orderDetailList.add(orderDetail);
            });
            orderDetailMapper.insertBatch(orderDetailList);
            return true;
        });
        if (Boolean.FALSE.equals(result)) {
            throw new OrderBusinessException(MessageConstant.ORDER_SUBMIT_FAILED);
        }
        //封装结果返回
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime()).build();
        // 3. 发送订单超时延迟消息
        //sendOrderTimeoutMessage(order.getId());
        // 采用RocketMQ 延迟消息
        // 3.发送20分钟的延迟消息到MQ
        OrderMessage message =OrderMessage.builder()
                .orderId(order.getId())
                .userId(userId)
                .build();
        Message<OrderMessage> orderMsg = MessageBuilder.withPayload(message).build();
        try {
            SendResult sendResult = rocketMQTemplate.syncSend("order-timeout-topic", orderMsg, 3000, 15);
            if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                log.warn("RocketMQ发送订单超时消息失败，降级到本地消息表，订单ID: {}, 状态: {}", order.getId(), sendResult.getSendStatus());
                sendOrderTimeoutLocalMessage(order.getId());
            }
        } catch (Exception e) {
            log.warn("RocketMQ发送订单超时消息异常，降级到本地消息表，订单ID: {}, 异常: {}", order.getId(), e.getMessage());
            sendOrderTimeoutLocalMessage(order.getId());
        }
        return orderSubmitVO;
    }




    /**
     * 降级方案：通过本地消息表发送订单超时延迟消息
     */
    private void sendOrderTimeoutLocalMessage(Long orderId) {
        Map<String, Object> content = new HashMap<>();
        content.put("orderId", orderId);
        content.put("timeoutMinutes", 15);
        localMessageService.sendDelayMessage(orderId, TYPE_ORDER_TIMEOUT, content, 15);
        log.info("已通过本地消息表发送订单超时延迟消息，订单ID: {}", orderId);
    }

    /**
     * 取消订单超时本地消息（支付成功时调用）
     */
    @Async
    public void cancelOrderTimeoutLocalMessage(Long orderId) {
        localMessageService.cancelMessage(orderId, TYPE_ORDER_TIMEOUT);
        log.info("已取消订单超时本地消息，订单ID: {}", orderId);
    }


    /**
     * 处理订单支付请求
     *
     * @param ordersPaymentDTO 支付请求DTO
     * @return 支付VO
     * @throws Exception 支付异常
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);
        // 开发环境模拟支付，跳过真实微信支付
        // 调用微信支付接口，生成预支付交易单
        // 模拟支付返回数据
        OrderPaymentVO vo = new OrderPaymentVO();
        vo.setNonceStr("mock_nonce_str");
        vo.setPaySign("mock_pay_sign");
        vo.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
        vo.setSignType("RSA");
        vo.setPackageStr("prepay_id=mock_prepay_id");

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber());

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_SHIPPED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
        Map map = new HashMap();
        map.put("type", 1);
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号:" + ordersDB.getNumber());

        //打上支付成功标记
        String key = "order_paid:" + ordersDB.getId();
        // 支付时间，金额，方式，流水号
        String value = vo.getTimeStamp() + ":" + ordersDB.getAmount() + ":" + ordersDB.getPayMethod() + ":" + user.getPhone();
        stringRedisTemplate.opsForValue().set(key, value, 30, TimeUnit.MINUTES);
        // 支付成功，取消本地消息表中的超时消息
        cancelOrderTimeoutLocalMessage(ordersDB.getId());
        return vo;
    }


    @Override
    public PageResult pageQueryUser(int pageNum, int pageSize, Integer status) {
        // 设置分页
        PageHelper.startPage(pageNum, pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);
        // 分页条件查询
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> list = new ArrayList();
        // 查询出订单明细，并封装入OrderVO进行响应
        if (page != null && page.getTotal() > 0) {
            for (Orders orders : page) {
                Long orderId = orders.getId();// 订单id
                // 查询订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);
                list.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), list);
    }

    @Override
    public OrderVO getDetailByOrderId(Long id) {
        Orders orders = orderMapper.getById(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;

    }

    @Override
    public void cancelOrder(Long id) {
        Orders order = orderMapper.getById(id);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (order.getStatus() >= Orders.TO_BE_SHIPPED) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        order.setStatus(Orders.CANCELLED);
        order.setCancelReason("用户取消订单");
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    @Override
    public void repurchase(Long id) {
        Long userId = BaseContext.getCurrentId();

        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 搜索订单
     *
     * @param pageQueryDTO
     * @return
     */
    public PageResult OrdersSearch(OrdersPageQueryDTO pageQueryDTO) {
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(pageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public OrderStatisticsVO getOrderStatistics() {
        List<Orders> ordersList = orderMapper.getAll();
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        Integer toBeShipped = 0;
        Integer shipped = 0;
        Integer toBeReceived = 0;
        for (Orders orders : ordersList) {
            if (orders.getStatus() == Orders.TO_BE_SHIPPED) {
                toBeShipped++;
            } else if (orders.getStatus() == Orders.SHIPPED) {
                shipped++;
            } else if (orders.getStatus() == Orders.TO_BE_RECEIVED) {
                toBeReceived++;
            }
        }
        orderStatisticsVO.setToBeConfirmed(toBeShipped);
        orderStatisticsVO.setWaitingShip(shipped);
        orderStatisticsVO.setShipped(toBeReceived);
        return orderStatisticsVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = orderMapper.getById(ordersConfirmDTO.getId());
        orders.setStatus(Orders.SHIPPED);
        orderMapper.update(orders);
        try {
            UserMessageDTO msgDTO = UserMessageDTO.builder()
                    .userId(orders.getUserId())
                    .title("订单已发货")
                    .content("您的订单 " + orders.getNumber() + " 已发货，请注意查收")
                    .type(1)
                    .build();
            userMessageService.send(msgDTO);
        } catch (Exception e) {
            log.warn("发送订单接单站内消息失败，订单ID: {}", orders.getId(), e);
        }
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());
        orders.setCancelReason(ordersRejectionDTO.getRejectionReason());
        orders.setStatus(Orders.CANCELLED);
        orderMapper.update(orders);
    }

    @Override
    public void cancelOrderAdmin(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = orderMapper.getById(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orderMapper.update(orders);
    }

    @Override
    public void ship(Long id) {
        Orders orders = orderMapper.getById(id);
        orders.setStatus(Orders.TO_BE_RECEIVED);
        orderMapper.update(orders);
        try {
            UserMessageDTO msgDTO = UserMessageDTO.builder()
                    .userId(orders.getUserId())
                    .title("订单已发货")
                    .content("您的订单 " + orders.getNumber() + " 已发货，请注意查收")
                    .type(1)
                    .build();
            userMessageService.send(msgDTO);
        } catch (Exception e) {
            log.warn("发送订单发货站内消息失败，订单ID: {}", orders.getId(), e);
        }
    }

    @Override
    public void complete(Long id) {
        Orders orders = orderMapper.getById(id);
        orders.setStatus(Orders.COMPLETED);
        orders.setShippingTime(LocalDateTime.now());
        orderMapper.update(orders);
        try {
            UserMessageDTO msgDTO = UserMessageDTO.builder()
                    .userId(orders.getUserId())
                    .title("订单已完成")
                    .content("您的订单 " + orders.getNumber() + " 已完成，感谢您的购买！")
                    .type(1)
                    .build();
            userMessageService.send(msgDTO);
        } catch (Exception e) {
            log.warn("发送订单完成站内消息失败，订单ID: {}", orders.getId(), e);
        }
    }

    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Map map = new HashMap();
        map.put("type", 2);
        map.put("orderId", orders.getId());
        map.put("content", "订单号:" + orders.getNumber());


    }


    /**
     * 生成模拟签名
     *
     * @param timeStamp
     * @param nonceStr
     * @param packageStr
     * @return
     */
    private String generateMockSign(String timeStamp, String nonceStr, String packageStr) {
        // 简单的模拟签名生成逻辑
        String signStr = timeStamp + nonceStr + packageStr;
        return Base64.getEncoder().encodeToString(
                (signStr + "mock_key").getBytes()
        );
    }

}