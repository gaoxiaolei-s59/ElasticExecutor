package org.puregxl.ElasticExecutor.core.inter;

import java.util.concurrent.RejectedExecutionHandler;

public interface CustomRejectedPolicy {

    /**
     * 策略名称 (e.g., "MyLogPolicy")
     */
    String getName();

    /**
     * 生成具体的拒绝策略实例
     */
    RejectedExecutionHandler generatePolicy();
}
