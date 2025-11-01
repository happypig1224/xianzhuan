package com.xianzhuan.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Redis 缓存工具类
 * 封装解决缓存穿透、击穿、雪崩的通用方法
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class RedisCacheUtils {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final Random RANDOM = new Random();
    private static final String CACHE_NULL_FLAG = "__NULL__";

    public RedisCacheUtils(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 解决缓存穿透：缓存空对象
     * DB返回null时写入空值占位符，设置较短过期时间，防止恶意请求穿透到DB
     *
     * @param key        缓存键
     * @param type       返回数据类型
     * @param dbFunction 数据库查询函数
     * @param expireTime 正常数据过期时间
     * @param timeUnit   时间单位
     */
    public <T> T queryWithPassThrough(String key, Class<T> type, Function<String, T> dbFunction, long expireTime, TimeUnit timeUnit) {
        String json = stringRedisTemplate.opsForValue().get(key);

        if (StringUtils.hasText(json)) {
            if (CACHE_NULL_FLAG.equals(json)) {
                return null;
            }
            try {
                return objectMapper.readValue(json, type);
            } catch (JsonProcessingException e) {
                log.error("缓存反序列化失败, key={}", key, e);
            }
        }

        T data = dbFunction.apply(key);

        if (data == null) {
            stringRedisTemplate.opsForValue().set(key, CACHE_NULL_FLAG, 5, TimeUnit.MINUTES);
            return null;
        }

        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(data), expireTime, timeUnit);
        } catch (JsonProcessingException e) {
            log.error("缓存序列化失败, key={}", key, e);
        }
        return data;
    }

    /**
     * 解决缓存击穿：互斥锁
     * 热点Key失效时，只允许一个线程重建缓存，其他线程等待重试
     *
     * @param key        缓存键
     * @param type       返回数据类型
     * @param dbFunction 数据库查询函数
     * @param expireTime 过期时间
     * @param timeUnit   时间单位
     */
    public <T> T queryWithMutex(String key, Class<T> type, Function<String, T> dbFunction, long expireTime, TimeUnit timeUnit) {
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.hasText(json)) {
            if (CACHE_NULL_FLAG.equals(json)) return null;
            try {
                return objectMapper.readValue(json, type);
            } catch (JsonProcessingException e) {
                log.error("缓存反序列化失败, key={}", key, e);
            }
        }

        String lockKey = "lock:" + key;
        boolean isLock = tryLock(lockKey);
        T data;

        try {
            if (isLock) {
                json = stringRedisTemplate.opsForValue().get(key);
                if (StringUtils.hasText(json)) {
                    if (CACHE_NULL_FLAG.equals(json)) return null;
                    try {
                        return objectMapper.readValue(json, type);
                    } catch (JsonProcessingException e) {
                        log.error("缓存反序列化失败, key={}", key, e);
                    }
                }

                data = dbFunction.apply(key);
                if (data == null) {
                    stringRedisTemplate.opsForValue().set(key, CACHE_NULL_FLAG, 5, TimeUnit.MINUTES);
                } else {
                    try {
                        stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(data), expireTime, timeUnit);
                    } catch (JsonProcessingException e) {
                        log.error("缓存序列化失败, key={}", key, e);
                    }
                }
            } else {
                Thread.sleep(50);
                return queryWithMutex(key, type, dbFunction, expireTime, timeUnit);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            if (isLock) {
                unlock(lockKey);
            }
        }
        return data;
    }

    /**
     * 解决缓存雪崩：随机过期时间
     * 在基础过期时间上增加随机偏移量，避免大量Key同时失效
     *
     * @param key         缓存键
     * @param value       缓存值
     * @param baseExpire  基础过期时间
     * @param randomBound 随机波动范围（如300表示±300秒）
     * @param timeUnit    时间单位
     */
    public void setWithRandomExpire(String key, Object value, long baseExpire, long randomBound, TimeUnit timeUnit) {
        try {
            long randomOffset = randomBound > 0 ? RANDOM.nextInt((int) (randomBound * 2)) - randomBound : 0;
            long finalExpire = baseExpire + randomOffset;
            if (finalExpire <= 0) finalExpire = baseExpire;

            String json = objectMapper.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, json, finalExpire, timeUnit);
        } catch (JsonProcessingException e) {
            log.error("缓存序列化失败, key={}", key, e);
        }
    }

    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}