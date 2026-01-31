package org.puregxl.ElasticExecutor.core.builder;

import org.puregxl.ElasticExecutor.core.executor.ElasticExecutor;

import java.util.concurrent.*;

/**
 * 线程池工厂类
 */
public class ElasticExecutorBuilder {

    private String threadPoolId;
    private int corePoolSize = 1;
    private int maximumPoolSize = 1;
    private long keepAliveTime = 60;
    private TimeUnit unit = TimeUnit.SECONDS;
    private BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(1024);
    private RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();
    private ThreadFactory threadFactory;

    // 私有构造，强制通过静态方法开始
    private ElasticExecutorBuilder(String threadPoolId) {
        this.threadPoolId = threadPoolId;
    }

    public static ElasticExecutorBuilder builder(String threadPoolId) {
        return new ElasticExecutorBuilder(threadPoolId);
    }

    public ElasticExecutorBuilder core(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        return this;
    }

    public ElasticExecutorBuilder max(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
        return this;
    }

    public ElasticExecutorBuilder queue(int capacity) {
        this.workQueue = new LinkedBlockingQueue<>(capacity);
        return this;
    }

    public ElasticExecutorBuilder rejectPolicy(RejectedExecutionHandler handler) {
        this.handler = handler;
        return this;
    }

    public ElasticExecutor build() {
        if (threadFactory == null) {
            // 简单的默认线程工厂
            this.threadFactory = r -> new Thread(r, threadPoolId + "-thread");
        }
        return new ElasticExecutor(
                corePoolSize, maximumPoolSize, keepAliveTime, unit,
                workQueue, threadFactory, handler, threadPoolId
        );
    }


}
