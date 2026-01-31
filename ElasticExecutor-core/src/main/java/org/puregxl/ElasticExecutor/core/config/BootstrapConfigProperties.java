package org.puregxl.ElasticExecutor.core.config;

import lombok.Data;
import org.puregxl.ElasticExecutor.core.Enum.ConfigFileTypeEnum;
import org.puregxl.ElasticExecutor.core.executor.ElasticExecutorProperties;
import java.util.List;


/**
 * elastic-executor:
 *   # 1. 对应 private Boolean enable = Boolean.TRUE;
 *   enable: true
 *
 *   # 2. 对应 private NacosConfig nacos;
 *   nacos:
 *     data-id: my-dynamic-thread-pool-config.yaml
 *     group: DEFAULT_GROUP
 *
 *   # 3. 对应 private MonitorConfig monitor = new MonitorConfig();
 *   monitor:
 *     enable: true
 *     # 对应 collectType
 *     collect-type: micrometer
 *     # 对应 collectInterval (注意 Java 是 Long 类型)
 *     collect-interval: 10
 *
 *   # 4. 对应 private List<ElasticExecutorProperties> executors;
 *   # 这是一个 List，在 YAML 中使用 "-" (破折号) 表示数组元素
 *   executors:
 *     - thread-pool-id: order-service-executor  # 假设 ElasticExecutorProperties 里有这个字段
 *       core-pool-size: 10
 *       maximum-pool-size: 20
 *       keep-alive-time: 60
 *       # 下面这些字段取决于 ElasticExecutorProperties 的具体定义
 *       queue-capacity: 1024
 *       blocking-queue: LinkedBlockingQueue
 *       rejected-handler: AbortPolicy
 *
 *     - thread-pool-id: log-service-executor
 *       core-pool-size: 5
 *       maximum-pool-size: 10
 *       keep-alive-time: 30
 *       queue-capacity: 500
 */
@Data
public class BootstrapConfigProperties {
    public static final String PRE = "ElasticExecutor";


    private Boolean enable = Boolean.TRUE;

    /**
     * Nacos 配置文件
     */
    private NacosConfig nacos;



    /**
     * 监控配置
     */
    private MonitorConfig monitor = new MonitorConfig();

    /**
     * 线程池配置集合
     */
    private List<ElasticExecutorProperties> executors;



    @Data
    public static class MonitorConfig {

        /**
         * 默认开启监控配置
         */
        private Boolean enable = Boolean.TRUE;

        /**
         * 监控类型
         */
        private String collectType = "micrometer";

        /**
         * 采集间隔，默认 10 秒
         */
        private Long collectInterval = 10L;
    }

    @Data
    public static class NacosConfig {

        private String dataId;

        private String group;
    }



    @Data
    public static class WebThreadPoolExecutorConfig {

        /**
         * 核心线程数
         */
        private Integer corePoolSize;

        /**
         * 最大线程数
         */
        private Integer maximumPoolSize;

        /**
         * 线程空闲存活时间（单位：秒）
         */
        private Long keepAliveTime;

    }


    private static BootstrapConfigProperties INSTANCE = new BootstrapConfigProperties();

    public static BootstrapConfigProperties getInstance() {
        return INSTANCE;
    }

    public static void setInstance(BootstrapConfigProperties properties) {
        INSTANCE = properties;
    }
}
