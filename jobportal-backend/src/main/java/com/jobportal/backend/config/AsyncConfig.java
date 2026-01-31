package com.jobportal.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Configure async task executor for email sending and other async operations
     */
    @Bean("emailTaskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        log.info("Configuring async task executor for email operations");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);       // Minimum number of threads
        executor.setMaxPoolSize(20);       // Maximum number of threads
        executor.setQueueCapacity(500);    // Queue size before creating new threads
        executor.setThreadNamePrefix("EmailAsync-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        return executor;
    }

    /**
     * General async executor for other operations
     */
    @Bean("generalTaskExecutor")
    public Executor generalAsyncExecutor() {
        log.info("Configuring general async task executor");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("GeneralAsync-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        
        return executor;
    }

    /**
     * Handle async method execution exceptions
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {
            log.error("Async method execution failed: method={}, args={}, error={}", 
                    method.getName(), objects, throwable.getMessage(), throwable);
        };
    }
}
