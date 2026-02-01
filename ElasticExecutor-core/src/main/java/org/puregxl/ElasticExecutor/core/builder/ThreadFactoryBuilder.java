package org.puregxl.ElasticExecutor.core.builder;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


public class ThreadFactoryBuilder {
    private ThreadFactory defultFactory;
    private String pre;
    private Boolean domain;


    public static ThreadFactoryBuilder builder() {
        return new ThreadFactoryBuilder();
    }

    public ThreadFactoryBuilder pre(String pre) {
        this.pre = pre;
        return this;
    }


    public ThreadFactoryBuilder threadFactory(Boolean domain) {
        this.domain = domain;
        return this;
    }

    public ThreadFactoryBuilder setBackingFactory(ThreadFactory factory) {
        this.defultFactory = factory;
        return this;
    }

    public ThreadFactory build() {
        ThreadFactory factory = (this.defultFactory == null) ? Executors.defaultThreadFactory() : this.defultFactory;


        AtomicInteger num = new AtomicInteger(0);

        return runnable -> {
            Thread thread = factory.newThread(runnable);


            if (this.pre != null) {
                thread.setName(this.pre + "-" + num.getAndIncrement());
            }


            if (this.domain != null) {
                thread.setDaemon(this.domain);
            }


            return thread;
        };
    }
}
