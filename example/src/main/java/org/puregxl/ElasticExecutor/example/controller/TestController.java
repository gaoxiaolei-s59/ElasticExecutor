package org.puregxl.ElasticExecutor.example.controller;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.puregxl.ElasticExecutor.core.executor.ElasticExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController

public class TestController {

    @Resource
    private ElasticExecutor testExecutor;
    @Resource
    private ElasticExecutor testExecutor1;

    @GetMapping("/test")
    public Map<String, Object> test() {
        testExecutor.execute(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Map<String, Object> results = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        result.put("threadPoolId", testExecutor.getThreadPoolId());
        result.put("corePoolSize", testExecutor.getCorePoolSize());
        result.put("maximumPoolSize", testExecutor.getMaximumPoolSize());
        result.put("activeCount", testExecutor.getActiveCount());
        result.put("queueSize", testExecutor.getQueue().size() + testExecutor.getQueue().remainingCapacity());
        result.put("handler", testExecutor.getRejectedExecutionHandler().getClass().getSimpleName());
        result.put("keepAliveTime", testExecutor.getKeepAliveTime(TimeUnit.SECONDS));
        result.put("allowCoreThreadTimeOut", testExecutor.allowsCoreThreadTimeOut());
        result.put("workQueue", testExecutor.getQueue().getClass().getSimpleName());
        results.put(testExecutor.getThreadPoolId(), result);
        Map<String, Object> result1 = new HashMap<>();

        result1.put("threadPoolId", testExecutor1.getThreadPoolId());
        result1.put("corePoolSize", testExecutor1.getCorePoolSize());
        result1.put("maximumPoolSize", testExecutor1.getMaximumPoolSize());
        result1.put("activeCount", testExecutor1.getActiveCount());
        result1.put("queueSize", testExecutor1.getQueue().size() + testExecutor1.getQueue().remainingCapacity());
        result1.put("handler", testExecutor1.getRejectedExecutionHandler().getClass().getSimpleName());
        result1.put("keepAliveTime", testExecutor1.getKeepAliveTime(TimeUnit.SECONDS));
        result1.put("allowCoreThreadTimeOut", testExecutor1.allowsCoreThreadTimeOut());
        result1.put("workQueue", testExecutor1.getQueue().getClass().getSimpleName());
        results.put(testExecutor1.getThreadPoolId(), result1);
        return results;
    }
}
