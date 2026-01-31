package org.puregxl.ElasticExecutor.example.controller;

import jakarta.annotation.Resource;
import org.puregxl.ElasticExecutor.core.executor.ElasticExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class TestController {

    @Resource(name = "testExecutor")
    private ElasticExecutor testExecutor;

    @GetMapping("/test")
    public Map<String, Object> test() {
        testExecutor.execute(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Map<String, Object> status = new HashMap<>();
        status.put("threadPoolId", testExecutor.getThreadPoolId());
        status.put("corePoolSize", testExecutor.getCorePoolSize());
        status.put("maximumPoolSize", testExecutor.getMaximumPoolSize());
        status.put("activeCount", testExecutor.getActiveCount());
        status.put("queueSize", testExecutor.getQueue().size());
        status.put("handler", testExecutor.getRejectedExecutionHandler().getClass().getSimpleName());
        status.put("keepAliveTime", testExecutor.getKeepAliveTime(TimeUnit.SECONDS));
        status.put("allowCoreThreadTimeOut", testExecutor.allowsCoreThreadTimeOut());
        return status;
    }
}
