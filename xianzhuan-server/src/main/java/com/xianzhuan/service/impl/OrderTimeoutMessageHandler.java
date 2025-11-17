package com.xianzhuan.service.impl;

import com.alibaba.fastjson.JSON;
import com.xianzhuan.entity.LocalMessage;
import com.xianzhuan.mapper.OrderMapper;
import com.xianzhuan.service.MessageHandlerService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Author 吴汇明
 * @School 绥化学院
 * @CreateTime
 */
@Service
@Slf4j
public class OrderTimeoutMessageHandler implements MessageHandlerService {
    @Autowired
    private OrderMapper orderMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    public void handleMessage(LocalMessage message) {
        Map content = JSON.parseObject(message.getContent(), Map.class);
        Long orderId = Long.valueOf(content.get("orderId").toString());
        // 检查Redis支付标记
        String key = "order_paid:" + orderId;
        if (stringRedisTemplate.hasKey(key)) {
            log.info("订单已支付，跳过超时处理，订单ID: {}", orderId);
            return;
        }
        // 乐观锁更新订单状态为已取消
        int rows = orderMapper.updateOptimize(orderId);
        if (rows > 0) {
            log.info("订单超时取消成功（本地消息表兜底），订单ID: {}", orderId);
        } else {
            log.warn("订单取消失败，可能已支付或状态已变更，订单ID: {}", orderId);
        }
    }

    public String getSupportedMessageType() {
        return LocalMessage.TYPE_ORDER_TIMEOUT;
    }
}