package org.puregxl.ElasticExecutor.core.executor;

import lombok.Getter;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 继承了ThreadPoolExecutor 实现动态线程池
 */
public class ElasticExecutor extends ThreadPoolExecutor {
    /**
     * 线程池唯一标识
     */
    @Getter
    private final String threadPoolId;

    private AtomicInteger rejectCount;

    public ElasticExecutor  (int corePoolSize,
                                int maximumPoolSize,
                                long keepAliveTime,
                                TimeUnit unit,
                                BlockingQueue<Runnable> workQueue,
                                ThreadFactory threadFactory,
                                RejectedExecutionHandler handler,
                                String threadPoolId) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.threadPoolId = threadPoolId;
    }


}
