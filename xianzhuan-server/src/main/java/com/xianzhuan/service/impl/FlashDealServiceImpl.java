package com.xianzhuan.service.impl;

import com.xianzhuan.context.BaseContext;
import com.xianzhuan.dto.FlashMessage;
import com.xianzhuan.entity.FlashOrder;
import com.xianzhuan.entity.FlashDeal;
import com.xianzhuan.mapper.FlashOrderMapper;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.FlashOrderService;
import com.xianzhuan.service.FlashDealService;

import com.xianzhuan.mapper.FlashDealMapper;
import com.xianzhuan.utils.MemoryCacheUtil;
import com.xianzhuan.utils.RedisCacheUtils;
import com.xianzhuan.utils.SlidingWindowLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.xianzhuan.utils.RedisConstant.*;
import static com.xianzhuan.utils.RocketMQConstant.FLASH_DEAL_TAG;
import static com.xianzhuan.utils.RocketMQConstant.FLASH_DEAL_TOPIC;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Service
public class FlashDealServiceImpl implements FlashDealService {

    @Autowired
    private FlashDealMapper mapper;
    @Autowired
    private FlashOrderMapper flashOrderMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private DefaultRedisScript<Long> redisScript;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private MemoryCacheUtil localCache;
    @Autowired
    private SlidingWindowLimiter slidingWindowLimiter;
    @Autowired

    private RedisCacheUtils redisCacheUtils;

    /**
     * 本地售罄标记：库存为 0 的优惠活动，后续请求直接快速失败
     */
    private final ConcurrentHashMap<Long, Boolean> soldOutMap = new ConcurrentHashMap<>();

    @Value("${xianzhuan.flash.sliding-window.window-ms:1000}")
    private long windowMs;
    @Value("${xianzhuan.flash.sliding-window.max-requests:100}")
    private int maxRequests;

    public FlashDealServiceImpl() {
        redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("flash.lua")));
        redisScript.setResultType(Long.class);
    }

    /**
     * 限时优惠发放
     *
     * @param flashDeal
     */
    public Result<String> publishDeal(FlashDeal flashDeal) {
        // 设置优惠活动信息
        flashDeal.setId(System.currentTimeMillis());
        flashDeal.setCreateTime(LocalDateTime.now());
        int result = mapper.publishDeal(flashDeal);
        if (result > 0) {
            // 计算缓存过期时间
            long beginTimeSeconds = flashDeal.getEndTime().atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
            long currentTimeSeconds = System.currentTimeMillis() / 1000;
            long expireMinutes = (beginTimeSeconds - currentTimeSeconds) / 60L;

            if (expireMinutes < 0) {
                expireMinutes = 30;
            }

            // 设置Redis库存缓存
            String dealKey = FLASH_DEAL_KEY + flashDeal.getId();
            Integer stock = flashDeal.getStock();
            log.info("库存{},key{}", stock, dealKey);
            stringRedisTemplate.opsForValue().set(dealKey, String.valueOf(stock), expireMinutes, TimeUnit.MINUTES);
            // 清除售罄标记，允许新一轮限时抢购
            soldOutMap.remove(flashDeal.getId());
            return Result.success("发放成功");
        } else {
            return Result.error("发放失败");
        }
    }

    @Override
    public Result<String> flashBuy(Long dealId) {
        if (soldOutMap.containsKey(dealId)) {
            return Result.error("库存不足");
        }
        String limitKey = FLASH_DEAL_LIMIT_KEY + dealId;
        if (!slidingWindowLimiter.tryAcquire(limitKey, windowMs, maxRequests)) {
            return Result.error("系统繁忙，请稍后重试");
        }
        Long userId = BaseContext.getCurrentId();
        final long finalUserId = userId;
        final long finalDealId = dealId;
        boolean needAsyncSendMq = false;
        String lockKey = FLASH_LOCK_KEY + dealId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean isLocked = lock.tryLock(10, -1, TimeUnit.MILLISECONDS);
            if (!isLocked) {
                return Result.error("系统繁忙，请稍后重试");
            }
            String stockKey = FLASH_DEAL_KEY + dealId;
            String userKey = FLASH_USER_KEY + userId + ":" + dealId;
            Long result = stringRedisTemplate.execute(redisScript, Arrays.asList(stockKey, userKey));
            if (result == 1) {
                needAsyncSendMq = true;
                return Result.success("抢购成功");
            } else if (result == -1) {
                soldOutMap.put(dealId, true);
                return Result.error("库存不足");
            } else if (result == -2) {
                return Result.error("您已领取过该优惠");
            } else {
                return Result.error("抢购失败");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Result.error("系统繁忙");
        } catch (Exception e) {
            log.error("抢购异常", e);
            return Result.error("系统繁忙");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
            if (needAsyncSendMq) {
                FlashMessage msg = FlashMessage.builder()
                        .userId(finalUserId).dealId(finalDealId).build();
                Message<FlashMessage> mqMsg =
                        MessageBuilder.withPayload(msg).build();
                String destination = FLASH_DEAL_TOPIC + ":" + FLASH_DEAL_TAG;
                rocketMQTemplate.asyncSend(destination, mqMsg, new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("MQ异步发送成功: userId={}, dealId={}", finalUserId, finalDealId);
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.error("MQ异步发送失败: userId={}, dealId={}", finalUserId, finalDealId, e);
                    }
                });
            }
        }
    }

    @Override
    public Result<List<FlashDeal>> queryDeal() {
        // 穿透防护(空值缓存) + 击穿防护(互斥锁)：秒杀列表是热点查询
        String cacheKey = FLASH_DEAL_KEY + "list";
        String json = redisCacheUtils.queryWithMutex(cacheKey, String.class, key -> {
            List<FlashDeal> list = mapper.queryDeal();
            if (list == null || list.isEmpty()) {
                return null;
            }
            return cn.hutool.json.JSONUtil.toJsonStr(list);
        }, 10, TimeUnit.MINUTES);

        if (json == null) {
            return Result.success(Collections.emptyList());
        }
        List<FlashDeal> flashDeal = cn.hutool.json.JSONUtil.toList(json, FlashDeal.class);
        return Result.success(flashDeal);
    }

    @Override
    public Result<List<FlashOrder>> myDealList() {
        Long userId = BaseContext.getCurrentId();
        List<FlashOrder> flashOrderList = flashOrderMapper.selectList(userId);
        return flashOrderList != null ? Result.success(flashOrderList) : Result.success(Collections.emptyList());
    }

    @Async("flashAsyncExecutor")
    public void createDealOrder(Long dealId, Long userId) {
        FlashOrder flashOrder = FlashOrder.builder()
                .userId(userId)
                .dealId(dealId)
                .orderTime(LocalDateTime.now())
                .status(1)
                .createTime(LocalDateTime.now())
                .build();
        flashOrderMapper.save(flashOrder);
        
        int result = mapper.decreaseStock(dealId);
        if (result == 0) {
            log.error("数据库库存扣减失败，user{},dealId{}", userId, dealId);
        } else {
            log.info("数据库库存扣减成功，user{},dealId{}", userId, dealId);
        }
    }
}