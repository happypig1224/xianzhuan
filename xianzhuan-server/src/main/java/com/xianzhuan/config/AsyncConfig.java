package com.xianzhuan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步配置类
 */
@Configuration
@EnableAsync  // 开启异步支持
public class AsyncConfig {
    /**
     * 限时抢购专用异步线程池
     * Bean 名称：flashAsyncExecutor
     */
    @Bean("flashAsyncExecutor")
    public Executor flashAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：正常维持的线程数（调优：10->50）
        executor.setCorePoolSize(50);
        // 最大线程数：高峰期最多创建的线程数（调优：20->100）
        executor.setMaxPoolSize(100);
        // 队列容量：任务队列大小，超出后触发拒绝策略（调优：200->500）
        executor.setQueueCapacity(500);
        // 线程名称前缀，方便日志追踪
        executor.setThreadNamePrefix("flash-async-");
        // 线程空闲时间（秒）
        executor.setKeepAliveSeconds(60);
        // 拒绝策略：队列满且线程数达到最大值时的处理策略
        // CallerRunsPolicy：由调用线程（主线程）处理，不会丢失任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 优雅关闭：等待任务全部结束才关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 关闭时最大等待时间
        executor.setAwaitTerminationSeconds(60);
        return executor;
    }
}
