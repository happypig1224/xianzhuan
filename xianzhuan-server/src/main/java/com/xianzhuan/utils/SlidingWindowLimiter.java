package com.xianzhuan.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 基于Redis滑动窗口算法的分布式限流器
 * 使用Redis Sorted Set + Lua脚本实现原子性滑动窗口限流
 * 每个请求以当前时间戳为score存入ZSet，统计窗口内请求数判断是否放行
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class SlidingWindowLimiter {

    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> script;

    /**
     * Lua脚本：原子性滑动窗口限流
     * KEYS[1] = 限流Key (ZSet)
     * ARGV[1] = 窗口大小(毫秒)
     * ARGV[2] = 窗口内最大请求数
     * ARGV[3] = 当前时间戳(毫秒)
     * 返回: 1=放行, 0=限流
     */
    private static final String LUA_SCRIPT =
            "local key = KEYS[1]\n" +
            "local window = tonumber(ARGV[1])\n" +
            "local limit = tonumber(ARGV[2])\n" +
            "local now = tonumber(ARGV[3])\n" +
            "local windowStart = now - window\n" +
            "redis.call('zremrangebyscore', key, '-inf', windowStart)\n" +
            "local count = redis.call('zcard', key)\n" +
            "if count < limit then\n" +
            "    redis.call('zadd', key, now, now .. ':' .. math.random(1, 1000000))\n" +
            "    redis.call('expire', key, math.ceil(window / 1000) + 1)\n" +
            "    return 1\n" +
            "end\n" +
            "return 0";
    public SlidingWindowLimiter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.script = new DefaultRedisScript<>();
        this.script.setScriptText(LUA_SCRIPT);
        this.script.setResultType(Long.class);
    }

    /**
     * 尝试获取许可
     *
     * @param key           限流标识
     * @param windowMs      窗口大小(毫秒)
     * @param maxRequests   窗口内最大请求数
     * @return true=放行, false=限流
     */
    public boolean tryAcquire(String key, long windowMs, int maxRequests) {
        long now = System.currentTimeMillis();
        Long result = stringRedisTemplate.execute(
                script,
                Collections.singletonList(key),
                String.valueOf(windowMs),
                String.valueOf(maxRequests),
                String.valueOf(now)
        );
        return result != null && result == 1;
    }
}