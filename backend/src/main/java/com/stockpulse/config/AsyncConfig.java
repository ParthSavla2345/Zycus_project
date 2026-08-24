package com.stockpulse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async thread pool configuration for @Async listeners.
 */
@Configuration
public class AsyncConfig {

    @Value("${async.core-pool-size:4}")
    private int corePoolSize;

    @Value("${async.max-pool-size:8}")
    private int maxPoolSize;

    @Value("${async.queue-capacity:100}")
    private int queueCapacity;

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("stockpulse-async-");
        executor.initialize();
        return executor;
    }
}
