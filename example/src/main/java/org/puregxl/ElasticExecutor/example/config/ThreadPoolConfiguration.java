package org.puregxl.ElasticExecutor.example.config;

import org.puregxl.ElasticExecutor.com.ElasticExecutorPool;
import org.puregxl.ElasticExecutor.core.executor.ElasticExecutor;
import org.puregxl.ElasticExecutor.core.extend.ResizableCapacityLinkedBlockingQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfiguration {

    @Bean
    @ElasticExecutorPool
    public ElasticExecutor testExecutor() {
        return new ElasticExecutor(
                2,
                4,
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(50),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy(),
                "test-executor"
        );
    }

    @Bean
    @ElasticExecutorPool
    public ElasticExecutor testExecutor1() {
        return new ElasticExecutor(
                2,
                4,
                30,
                TimeUnit.SECONDS,
                new ResizableCapacityLinkedBlockingQueue<>(50),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy(),
                "test-executor1"
        );
    }



}
