package org.puregxl.ElasticExecutor.core.executor;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ElasticExecutorProperties {

    /**
     * 线程池唯一标识
     */
    private String threadPoolId;

    /**
     * 核心线程数
     */
    private Integer corePoolSize;

    /**
     * 最大线程数
     */
    private Integer maximumPoolSize;

    /**
     * 队列容量
     */
    private Integer queueCapacity;

    /**
     * 阻塞队列类型
     */
    private String workQueue;

    /**
     * 拒绝策略类型
     */
    private String rejectedHandler;

    /**
     * 线程空闲存活时间（单位：秒）
     */
    private Long keepAliveTime;

    /**
     * 是否允许核心线程超时
     */
    private Boolean allowCoreThreadTimeOut;


    /**
     * 报警阈值
     */
    private AlarmConfig alarm = new AlarmConfig();


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AlarmConfig{
        /**
         * 阈值为百分之90
         */
        private Integer QueueThreshold = 90;

        /**
         * 活跃线程阈值
         */
        private Integer activeThreshold = 80;
    }


}
