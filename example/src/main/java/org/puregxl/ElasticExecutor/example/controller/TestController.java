package org.puregxl.ElasticExecutor.example.controller;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.puregxl.ElasticExecutor.core.executor.ElasticExecutor;
import org.puregxl.ElasticExecutor.core.executor.ElasticExecutorProperties;
import org.puregxl.ElasticExecutor.core.executor.ElasticExecutorRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

@RestController

public class TestController {

    @Resource
    private ElasticExecutor testExecutor;
    @Resource
    private ElasticExecutor testExecutor1;

    @GetMapping("/test")
    public Map<String, Object> test(@RequestParam(value = "count", defaultValue = "50") int count) {

        // --- 1. 模拟高负载逻辑 ---
        for (int i = 0; i < count; i++) {
            try {
                testExecutor.execute(() -> {
                    try {

                        // 这样才能让队列保持 "堆积状态"，等待报警检测定时任务轮询到。
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            } catch (RejectedExecutionException e) {
                // 忽略拒绝策略异常。
                // 我们的目的是填满队列，填满后抛出异常说明已经达到最大负载，目的已达到。
            }

            try {
                testExecutor1.execute(() -> {
                    try {

                        // 这样才能让队列保持 "堆积状态"，等待报警检测定时任务轮询到。
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            } catch (RejectedExecutionException e) {
                // 忽略拒绝策略异常。
                // 我们的目的是填满队列，填满后抛出异常说明已经达到最大负载，目的已达到。
            }
        }

        // --- 2. 收集当前状态（用于验证） ---
        Map<String, Object> results = new HashMap<>();


        results.put(testExecutor.getThreadPoolId(), getPoolStatus(testExecutor, ElasticExecutorRegister.get(testExecutor.getThreadPoolId()).getExecutorProperties()));
        results.put(testExecutor1.getThreadPoolId(), getPoolStatus(testExecutor1, ElasticExecutorRegister.get(testExecutor1.getThreadPoolId()).getExecutorProperties()));

        return results;
    }

    /**
     * 提取通用的状态获取逻辑
     */
    private Map<String, Object> getPoolStatus(ElasticExecutor executor, ElasticExecutorProperties executorProperties) {
        Map<String, Object> status = new HashMap<>();

        // 基础信息
        status.put("threadPoolId", executor.getThreadPoolId());
        status.put("corePoolSize", executor.getCorePoolSize());
        status.put("maximumPoolSize", executor.getMaximumPoolSize());
        status.put("activeCount", executor.getActiveCount());

        // 队列相关
        int currentSize = executor.getQueue().size();
        int remaining = executor.getQueue().remainingCapacity();
        int capacity = currentSize + remaining;

        status.put("queueSize", currentSize);
        status.put("queueCapacity", capacity);
        status.put("queueUsage", String.format("%.2f%%", (currentSize * 100.0 / capacity))); // 直观展示使用率

        // 其他配置
        status.put("handler", executor.getRejectedExecutionHandler().getClass().getSimpleName());
        status.put("keepAliveTime", executor.getKeepAliveTime(TimeUnit.SECONDS));
        status.put("allowCoreThreadTimeOut", executor.allowsCoreThreadTimeOut());
        status.put("workQueue", executor.getQueue().getClass().getSimpleName());
        status.put("queue-threshold",  executorProperties.getAlarm().getQueueThreshold());
        status.put("active-threshold",  executorProperties.getAlarm().getActiveThreshold());
        return status;
    }
}
