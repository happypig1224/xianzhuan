package com.xianzhuan.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author HappyPig
 * @version 1.0
 * @since 2026/3/12 19:37
 */
@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://106.55.22.153:6379")
                .setPassword("123456")
                .setDatabase(10)
                .setConnectionPoolSize(256)
                .setConnectionMinimumIdleSize(32)
                .setIdleConnectionTimeout(10000)
                .setConnectTimeout(1000)
                .setTimeout(1000)
                .setRetryAttempts(0)
                .setRetryInterval(200);
        return Redisson.create(config);
    }
}