package com.xianzhuan.rocketmq;

import com.xianzhuan.config.RocketMQConfig;
import com.xianzhuan.dto.FlashMessage;
import com.xianzhuan.exception.FlashOrderCreateFailException;
import com.xianzhuan.mapper.FlashDealMapper;
import com.xianzhuan.service.FlashOrderService;
import com.xianzhuan.service.FlashDealService;
import com.xianzhuan.utils.RocketMQConstant;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.xianzhuan.utils.RedisConstant.FLASH_ORDER_KEY;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Component
@Slf4j
@RocketMQMessageListener(
        topic = RocketMQConstant.FLASH_DEAL_TOPIC,
        selectorExpression = RocketMQConstant.FLASH_DEAL_TAG,
        consumerGroup = RocketMQConstant.CONSUMER_GROUP,
        messageModel = MessageModel.CLUSTERING,
        consumeThreadNumber = 8,
        consumeThreadMax = 16,
        maxReconsumeTimes = 3
)
public class FlashDealConsumer implements RocketMQListener<FlashMessage> {
    @Autowired
    private FlashOrderService flashOrderService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private FlashDealMapper mapper;

    @Override
    public void onMessage(FlashMessage flashMessage) {
        Long userId = flashMessage.getUserId();
        Long dealId = flashMessage.getDealId();
        String mqKey = FLASH_ORDER_KEY + userId + ":" + dealId;

        Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent(mqKey, "1", 15, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(ifAbsent)) {
            log.info("限时订单已存在:{}", flashMessage);
            return;
        }

        log.info("收到限时订单:{}", flashMessage);
        try {
            int rows = flashOrderService.createOrder(flashMessage);
            if (rows > 0) {
                int result = mapper.decreaseStock(dealId);
                if (result > 0) {
                    log.info("限时抢购成功：userId={}, dealId={}", userId, dealId);
                } else {
                    log.error("数据库库存扣减失败：userId={}, dealId={}", userId, dealId);
                    throw new FlashOrderCreateFailException("数据库库存扣减失败");
                }
            } else {
                // 重试机制
                log.error("创建订单失败：userId={}, dealId={}", userId, dealId);
                throw new FlashOrderCreateFailException("创建订单失败");
            }
        } catch (FlashOrderCreateFailException e) {
            log.warn("限时订单创建失败，将触发重试：{}", e.getMessage());
            throw new RuntimeException("消费失败，触发重试", e);
        } catch (Exception e) {
            log.error("消费过程异常，userId={}, dealId={}", userId, dealId, e);
            throw new RuntimeException("消费失败，触发重试", e);
        }
    }

    @PostConstruct
    public void init() {
        log.info("限时订单消费者启动...");
    }
}