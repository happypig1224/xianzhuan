package com.xianzhuan.service;

import com.xianzhuan.dto.*;
import com.xianzhuan.result.PageResult;
import com.xianzhuan.vo.OrderPaymentVO;
import com.xianzhuan.vo.OrderStatisticsVO;
import com.xianzhuan.vo.OrderSubmitVO;
import com.xianzhuan.vo.OrderVO;

public interface OrderService {
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);
    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    PageResult pageQueryUser(int page, int pageSize, Integer status);

    OrderVO getDetailByOrderId(Long id);


    void cancelOrder(Long id);

    void repurchase(Long id);

    PageResult OrdersSearch(OrdersPageQueryDTO pageQueryDTO);

    OrderStatisticsVO getOrderStatistics();

    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    void cancelOrderAdmin(OrdersCancelDTO ordersCancelDTO);

    void ship(Long id);

    void complete(Long id);

    void reminder(Long id);

    /**
     * 生成订单提交Token
     *
     * @return Token字符串
     * @author Wu, Hui Ming
     * @version 1.0
     * @School Suihua University
     */
    String generateToken();
}
