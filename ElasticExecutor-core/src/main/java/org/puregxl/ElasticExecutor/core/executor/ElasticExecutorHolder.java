package org.puregxl.ElasticExecutor.core.executor;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.ThreadPoolExecutor;

@Data
@AllArgsConstructor
public class ElasticExecutorHolder {
    /**
     * 动态线程池唯一标识
     */
    private String threadPoolId;

    /**
     * 线程池实例
     */
    private ThreadPoolExecutor executor;

    /**
     * 线程池属性参数
     */
    private ElasticExecutorProperties executorProperties;
}
