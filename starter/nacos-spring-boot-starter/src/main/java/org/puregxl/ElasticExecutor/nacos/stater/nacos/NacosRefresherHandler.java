package org.puregxl.ElasticExecutor.nacos.stater.nacos;


import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.puregxl.ElasticExecutor.core.Enum.ConfigFileTypeEnum;
import org.puregxl.ElasticExecutor.core.Enum.RejectedPolicyTypeEnum;
import org.puregxl.ElasticExecutor.core.builder.ThreadFactoryBuilder;
import org.puregxl.ElasticExecutor.core.config.BootstrapConfigProperties;
import org.puregxl.ElasticExecutor.core.executor.ElasticExecutorHolder;
import org.puregxl.ElasticExecutor.core.executor.ElasticExecutorProperties;
import org.puregxl.ElasticExecutor.core.executor.ElasticExecutorRegister;
import org.puregxl.ElasticExecutor.core.extend.ResizableCapacityLinkedBlockingQueue;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.yaml.snakeyaml.Yaml;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

import static org.puregxl.ElasticExecutor.core.message.MessageFormat.CHANGE_DELIMITER;
import static org.puregxl.ElasticExecutor.core.message.MessageFormat.CHANGE_THREAD_POOL_TEXT;


@Slf4j
public class NacosRefresherHandler implements ApplicationRunner {
    private ConfigService configService;

    private final BootstrapConfigProperties properties;

    public NacosRefresherHandler(ConfigService configService, BootstrapConfigProperties properties) {
        this.properties = properties;
        this.configService = configService;
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        registerNacosListener();
    }

    //设置一个单线程池
    public void registerNacosListener() throws NacosException {
        BootstrapConfigProperties.NacosConfig nacosConfig = properties.getNacos();
        configService.addListener(
                nacosConfig.getDataId(),
                nacosConfig.getGroup(),
                new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return new ThreadPoolExecutor(
                                1,
                                1,
                                0L, // 核心数等于最大数时，keepAliveTime 通常无意义（除非设置 allowCoreThreadTimeOut）
                                TimeUnit.SECONDS,
                                new SynchronousQueue<Runnable>(),
                                ThreadFactoryBuilder.builder().pre("nacos-refresher-thread-").build(),
                                new ThreadPoolExecutor.CallerRunsPolicy() // 建议：如果线程忙，由调用者线程自己运行，防止报错
                        );
                    }

                    //此处为接受回调函数的地方 我们需要对比远程的数据和现在的数据有没有区别
                    @Override
                    public void receiveConfigInfo(String info) {
                        refreshThreadPool(info);
                    }
                }
        );


    }


    /**
     * 为什么要这么做？
     * <p>
     * Nacos 传给你的是 String。
     * <p>
     * Spring 的绑定器（Binder）不直接吃 String，它需要一个属性源（PropertySource），而属性源底层通常是 Map。
     * <
     * YAML 的处理：使用 SnakeYAML 库（Spring Boot 内置）。它会将 YAML 的树状结构（缩进）转换为 Java 的 Map<String, Object>（嵌套的 Map 和 List）。
     * <p>
     * Properties 的处理：使用 JDK 自带的 Properties 类加载。
     * <p>
     * 此时的状态：数据还在，但它们只是散乱的 Map（例如 key 是 "onethread.enable"，value 是 "true"），还没有变成你的 BootstrapConfigProperties 对象。
     *
     * @param info
     */
    private void refreshThreadPool(String info) {
        //利用Spring自带的工具 把info转换
        BootstrapConfigProperties refreshConfigProperties = bindProperties(info);
        //比对逻辑
        List<ElasticExecutorProperties> executors = refreshConfigProperties.getExecutors();

        if (executors.isEmpty()) {
            return;
        }
        for (ElasticExecutorProperties executorProperties : refreshConfigProperties.getExecutors()) {
            //获取当前配置
            ElasticExecutorHolder executorHolder = ElasticExecutorRegister.get(executorProperties.getThreadPoolId());
            ElasticExecutorProperties currentProperties = executorHolder.getExecutorProperties();

            //如果没有变化
            if (!hasChanged(executorProperties, currentProperties)) {
                continue;
            }
            //变更属性
            updateThreadPool(executorProperties);


        }
    }

    public void updateThreadPool(ElasticExecutorProperties remoteProperties) {
        String threadPoolId = remoteProperties.getThreadPoolId();
        ElasticExecutorHolder holder = ElasticExecutorRegister.get(threadPoolId);

        // 双重检查，防止 holder 为空
        if (holder == null) {
            log.error("未找到线程池 [{}]，无法更新", threadPoolId);
            return;
        }
        synchronized(threadPoolId.intern()) {
            ThreadPoolExecutor executor = holder.getExecutor();
            ElasticExecutorProperties originalProperties = holder.getExecutorProperties();

            // ==========================================================================
            // 1. 核心参数修改 (Core & Max)
            // ==========================================================================
            int currentCore = executor.getCorePoolSize();
            int currentMax = executor.getMaximumPoolSize();

            // 确定新值：如果 Nacos 没传 (null)，就沿用当前运行时的值，简化后续逻辑
            Integer newCore = remoteProperties.getCorePoolSize() != null ? remoteProperties.getCorePoolSize() : currentCore;
            Integer newMax = remoteProperties.getMaximumPoolSize() != null ? remoteProperties.getMaximumPoolSize() : currentMax;

            // 校验配置合法性
            if (newCore > newMax) {

                return; // 或者抛出异常
            }

            // 只有当数值真的变了才执行修改逻辑
            if (!Objects.equals(newCore, currentCore) || !Objects.equals(newMax, currentMax)) {
                // 修改原则：
                // 1. 如果是扩容 (targetMax > currentMax)，必须先设置 Max，再设置 Core
                // 2. 如果是缩容 (targetMax < currentMax)，必须先设置 Core，再设置 Max
                // 3. Max 不变的情况，顺序无所谓，走 else 逻辑即可

                if (newMax > currentMax) {
                    // 扩容顺序
                    executor.setMaximumPoolSize(newMax);
                    executor.setCorePoolSize(newCore);

                } else {
                    // 缩容顺序 (或 Max 不变)
                    executor.setCorePoolSize(newCore);
                    executor.setMaximumPoolSize(newMax);

                }
            }


            // 2. 允许核心线程超时
            if (remoteProperties.getAllowCoreThreadTimeOut() != null &&
                    !Objects.equals(remoteProperties.getAllowCoreThreadTimeOut(), executor.allowsCoreThreadTimeOut())) {
                executor.allowCoreThreadTimeOut(remoteProperties.getAllowCoreThreadTimeOut());

            }


            // 3. 拒绝策略
            if (remoteProperties.getRejectedHandler() != null &&
                    !Objects.equals(remoteProperties.getRejectedHandler(), originalProperties.getRejectedHandler())) {
                // 假设你有 RejectedPolicyTypeEnum.createPolicy 工厂方法
                RejectedExecutionHandler handler = RejectedPolicyTypeEnum.createPolicy(remoteProperties.getRejectedHandler());
                if (handler != null) {
                    executor.setRejectedExecutionHandler(handler);
                }
            }


            // 4. 空闲回收时间
            if (remoteProperties.getKeepAliveTime() != null &&
                    !Objects.equals(remoteProperties.getKeepAliveTime(), executor.getKeepAliveTime(TimeUnit.SECONDS))) {
                executor.setKeepAliveTime(remoteProperties.getKeepAliveTime(), TimeUnit.SECONDS);
            }

            // 5. 队列容量 (需确保队列支持动态调整)
            if (remoteProperties.getQueueCapacity() != null &&
                    !Objects.equals(remoteProperties.getQueueCapacity(), originalProperties.getQueueCapacity())) {
                BlockingQueue<Runnable> queue = executor.getQueue();
                // 这里的 ResizableCapacityLinkedBlockingQueue 是自定义的类
                if (queue instanceof ResizableCapacityLinkedBlockingQueue) {
                    ((ResizableCapacityLinkedBlockingQueue<?>) queue).setCapacity(remoteProperties.getQueueCapacity());
                } else {
                    log.warn("[{}] 当前队列类型 {} 不支持动态修改容量", threadPoolId, queue.getClass().getSimpleName());
                    remoteProperties.setQueueCapacity(originalProperties.getQueueCapacity());
                }
            }

            //打印变更日志
            log.info(CHANGE_THREAD_POOL_TEXT,
                    threadPoolId,
                    // 2. 下面这些是参数，日志框架会自动把它们填入 {} 中
                    String.format(CHANGE_DELIMITER, originalProperties.getCorePoolSize(), remoteProperties.getCorePoolSize()),
                    String.format(CHANGE_DELIMITER, originalProperties.getMaximumPoolSize(), remoteProperties.getMaximumPoolSize()),
                    String.format(CHANGE_DELIMITER, originalProperties.getQueueCapacity(), remoteProperties.getQueueCapacity()),
                    String.format(CHANGE_DELIMITER, originalProperties.getKeepAliveTime(), remoteProperties.getKeepAliveTime()),
                    String.format(CHANGE_DELIMITER, originalProperties.getRejectedHandler(), remoteProperties.getRejectedHandler()),
                    String.format(CHANGE_DELIMITER, originalProperties.getAllowCoreThreadTimeOut(), remoteProperties.getAllowCoreThreadTimeOut())
            );
        }

//        if (remoteProperties.getWorkQueue() != null &&
//                !Objects.equals(remoteProperties.getWorkQueue(), currentProperties.getWorkQueue())) {
//            executor.setKeepAliveTime(remoteProperties.getKeepAliveTime(), TimeUnit.SECONDS);
//            log.info("[{}] KeepAliveTime 变更: {}s", threadPoolId, remoteProperties.getKeepAliveTime());
//        }


        // 6. 关键步骤：更新 Holder 中的本地配置缓存
        holder.setExecutorProperties(remoteProperties);
    }

    /**
     * 是否有发生变更
     */
    public boolean hasChanged(ElasticExecutorProperties remoteProperties, ElasticExecutorProperties currentProperties) {
        String threadPoolId = remoteProperties.getThreadPoolId();
        ElasticExecutorHolder holder = ElasticExecutorRegister.get(threadPoolId);

        // 双重校验：Holder 不应为空
        if (holder == null) return false;

        ThreadPoolExecutor executor = holder.getExecutor();
        boolean isChanged = false;
        if (isDiff(remoteProperties.getWorkQueue(), currentProperties.getWorkQueue())) {
            throw new RuntimeException("禁止修改阻塞队列, 会导致任务丢失");
        }
        // --- 1. CorePoolSize (核心线程数) ---
        // 对比目标：Remote配置 vs 运行时 Executor 真实值
        if (isDiff(remoteProperties.getCorePoolSize(), executor.getCorePoolSize())) {
            isChanged = true;
        }

        // --- 2. MaximumPoolSize (最大线程数) ---
        // 对比目标：Remote配置 vs 运行时 Executor 真实值
        if (isDiff(remoteProperties.getMaximumPoolSize(), executor.getMaximumPoolSize())) {
            isChanged = true;
        }

        // --- 3. KeepAliveTime (空闲回收时间) ---
        // 对比目标：Remote配置 vs 运行时 Executor 真实值 (统一转换为秒)
        if (isDiff(remoteProperties.getKeepAliveTime(), executor.getKeepAliveTime(TimeUnit.SECONDS))) {
            isChanged = true;
        }

        // --- 4. 拒绝策略 (RejectedHandler) ---
        // 对比目标：Remote配置 vs 本地缓存配置 (CurrentProperties)
        // 原因：Executor 运行时的 Handler 是对象，很难直接转回字符串名称，所以对比配置类的 String 字段最准
        if (isDiff(remoteProperties.getRejectedHandler(), currentProperties.getRejectedHandler())) {
            isChanged = true;
        }

        // --- 5. 允许核心线程超时 (AllowCoreThreadTimeOut) ---
        // 对比目标：Remote配置 vs 运行时 Executor 真实值
        if (isDiff(remoteProperties.getAllowCoreThreadTimeOut(), executor.allowsCoreThreadTimeOut())) {
            isChanged = true;
        }

        // --- 6. 队列容量 (QueueCapacity) ---
        // 注意：标准 LinkedBlockingQueue 的 capacity 是 final 的，不能修改，也不能直接读取(除非用反射)。
        // 这里通常需要自定义 ResizableLinkedBlockingQueue。
        // 这里先对比配置值。
        if (isDiff(remoteProperties.getQueueCapacity(), currentProperties.getQueueCapacity())) {
            isChanged = true;
        }

        return isChanged;
    }

    /**
     * 单属性是否变更
     *
     * @return
     */
    private boolean isDiff(Object remoteValue, Object currentValue) {
        // 1. 如果 Nacos 里没配这个值 (null)，默认认为不修改，返回 false
        if (remoteValue == null) {
            return false;
        }
        // 2. 对比值是否相等
        // Objects.equals 处理了 null 安全和内容对比
        return !Objects.equals(remoteValue, currentValue);
    }

    private BootstrapConfigProperties bindProperties(String configContent) {
        if (configContent == null || configContent.trim().isEmpty()) {
            return null;
        }

        try {
            // 1. 获取配置类型
            ConfigFileTypeEnum type = this.properties.getConfigFileType();

            // 定义一个局部变量来存放解析后的扁平配置
            Properties flatProperties = new Properties();

            // 2. 解析配置内容为扁平的 Properties
            // 无论 YAML 还是 Properties，统一转为扁平的 Key-Value (如 a.b.c=value)
            if (ConfigFileTypeEnum.YAML.equals(type) || ConfigFileTypeEnum.YML.equals(type)) {
                YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
                factory.setResources(new ByteArrayResource(configContent.getBytes(StandardCharsets.UTF_8)));
                // getObject() 会自动把 YAML 拍平成 properties 格式
                flatProperties = factory.getObject();
            } else {
                flatProperties.load(new StringReader(configContent));
            }

            if (flatProperties == null || flatProperties.isEmpty()) {
                return null;
            }

            // 3. 判断前缀策略
            // 检查属性中是否包含以 "elastic-executor." 开头的 Key
            boolean hasPrefix = flatProperties.keySet().stream()
                    .map(Object::toString)
                    .anyMatch(k -> k.startsWith(BootstrapConfigProperties.PRE + "."));

            // 如果配置里写了 "elastic-executor.enable=true"，则绑定前缀为 "elastic-executor"
            // 如果配置里直接写 "enable=true"，则绑定前缀为 "" (空)
            String bindPrefix = hasPrefix ? BootstrapConfigProperties.PRE : "";

            // 4. 执行绑定
            ConfigurationPropertySource source = new MapConfigurationPropertySource(flatProperties);
            Binder binder = new Binder(source);

            return binder.bind(bindPrefix, Bindable.of(BootstrapConfigProperties.class))
                    .orElse(null);

        } catch (Exception e) {
            log.error("解析配置失败", e);
            return null;
        }
    }

}
