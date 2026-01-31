package org.puregxl.ElasticExecutor.core.Enum;


import lombok.Getter;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 拒绝策略枚举 简单工厂模式 方便扩展
 */
@Getter
public enum RejectedPolicyTypeEnum {

    /**
     * {@link ThreadPoolExecutor.CallerRunsPolicy}
     */
    CALLER_RUNS_POLICY("CallerRunsPolicy", new ThreadPoolExecutor.CallerRunsPolicy()),

    /**
     * {@link ThreadPoolExecutor.AbortPolicy}
     */
    ABORT_POLICY("AbortPolicy", new ThreadPoolExecutor.AbortPolicy()),

    /**
     * {@link ThreadPoolExecutor.DiscardPolicy}
     */
    DISCARD_POLICY("DiscardPolicy", new ThreadPoolExecutor.DiscardPolicy()),

    /**
     * {@link ThreadPoolExecutor.DiscardOldestPolicy}
     */
    DISCARD_OLDEST_POLICY("DiscardOldestPolicy", new ThreadPoolExecutor.DiscardOldestPolicy());

    private String name;

    private RejectedExecutionHandler rejectedHandler;

    RejectedPolicyTypeEnum(String rejectedPolicyName, RejectedExecutionHandler rejectedHandler) {
        this.name = rejectedPolicyName;
        this.rejectedHandler = rejectedHandler;
    }

    // ================== 静态缓存 (O(1) 查找) ==================
    private static final Map<String, RejectedPolicyTypeEnum> REJECTED_POLICY_TYPE_ENUM_MAP;

    //静态保证容器能正确的初始化
    static {
        RejectedPolicyTypeEnum[] values = RejectedPolicyTypeEnum.values();
        REJECTED_POLICY_TYPE_ENUM_MAP = new HashMap<>(values.length);
        for (RejectedPolicyTypeEnum value : values) {
            REJECTED_POLICY_TYPE_ENUM_MAP.put(value.name, value);
        }
    }

    // ================== 核心工厂方法 ==================

    /**
     * 根据名称创建拒绝策略
     *
     * @param name 策略名称
     * @return 拒绝策略实例
     */
    public static RejectedExecutionHandler createPolicy(String name) {

        RejectedPolicyTypeEnum rejectedPolicyTypeEnum = REJECTED_POLICY_TYPE_ENUM_MAP.get(name);
        // 3. 兜底策略：默认为 AbortPolicy
        if (rejectedPolicyTypeEnum != null) return rejectedPolicyTypeEnum.rejectedHandler;
        //兜底策略 返回默认的拒绝任务
        return new ThreadPoolExecutor.AbortPolicy();
    }

    /**
     * 根据名称获取枚举类型
     */
    public static RejectedPolicyTypeEnum getEnumByName(String name) {
        return Optional.ofNullable(REJECTED_POLICY_TYPE_ENUM_MAP.get(name))
                .orElse(ABORT_POLICY);
    }
}
