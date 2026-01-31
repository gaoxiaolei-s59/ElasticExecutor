package org.puregxl.ElasticExecutor.com.processor;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.puregxl.ElasticExecutor.com.ElasticExecutorPool;
import org.puregxl.ElasticExecutor.core.Enum.BlockingQueueTypeEnum;
import org.puregxl.ElasticExecutor.core.Enum.RejectedPolicyTypeEnum;
import org.puregxl.ElasticExecutor.core.config.BootstrapConfigProperties;
import org.puregxl.ElasticExecutor.core.executor.ElasticExecutor;
import org.puregxl.ElasticExecutor.core.executor.ElasticExecutorHolder;
import org.puregxl.ElasticExecutor.core.executor.ElasticExecutorProperties;
import org.puregxl.ElasticExecutor.core.executor.ElasticExecutorRegister;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;


import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 2026 - 1 - 31
 */
@Slf4j
@RequiredArgsConstructor
public class ElasticBeanPostProcessor implements BeanPostProcessor {

    //构造器自动注入 启动配置类
    private final BootstrapConfigProperties bootstrapConfigProperties;

    /**
     * 实现了Bean的后置处理器 扫描我们标记为动态线程池的Bean
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //判断类型是否为指定基类
        ElasticExecutorPool annotationOnBean;
        if (bean instanceof ElasticExecutor) {
            //查找指定注解
            annotationOnBean = ApplicationContextHolder.findAnnotationOnBean(beanName, ElasticExecutorPool.class);
            if (Objects.isNull(annotationOnBean)) {
                return bean; //没检查到指定注解 就直接返回
            }
            //如果检查到了指定注解
            ElasticExecutor executor = (ElasticExecutor) bean;
            // 1. 定义结果变量，初始为 null
            ElasticExecutorProperties executorProperties = null;

            // 2. 使用增强 for 循环遍历
            for (ElasticExecutorProperties each : bootstrapConfigProperties.getExecutors()) {
                // 3. 判断 ID 是否相等 (使用 Objects.equals 防止空指针)
                if (Objects.equals(executor.getThreadPoolId(), each.getThreadPoolId())) {
                    executorProperties = each;
                    break; // 4. 找到后立即跳出循环
                }
            }
            // 5. 如果循环结束后变量仍为 null，说明没找到，抛出异常
            if (executorProperties == null) {
                throw new RuntimeException("The thread pool id does not exist in the configuration.");
            }

            // 6.如果找到了 把指定id和配置文件注册到我们的自定义容器里面
            refreshThreadPool(executorProperties, executor);
            ElasticExecutorRegister.put(executor.getThreadPoolId(), executor, executorProperties);
        }
        return bean;
    }

    private void refreshThreadPool(ElasticExecutorProperties executorProperties, ElasticExecutor elasticExecutor) {
        //
        Integer remoteCorePoolSize = executorProperties.getCorePoolSize();
        Integer remoteMaximumPoolSize = executorProperties.getMaximumPoolSize();
        Assert.isTrue(remoteCorePoolSize <= remoteMaximumPoolSize, "remoteCorePoolSize must be smaller than remoteMaximumPoolSize.");

        //防止设置的时候报错 因为核心线程数如果大于最大线程数会抛异常
        int originalMaximumPoolSize = elasticExecutor.getMaximumPoolSize();
        if (remoteCorePoolSize > originalMaximumPoolSize) {
            elasticExecutor.setMaximumPoolSize(remoteMaximumPoolSize);
            elasticExecutor.setCorePoolSize(remoteCorePoolSize);
        } else {
            elasticExecutor.setCorePoolSize(remoteCorePoolSize);
            elasticExecutor.setMaximumPoolSize(remoteMaximumPoolSize);
        }

        //TODO 需要修改
        BlockingQueue workQueue = BlockingQueueTypeEnum.createBlockQueue(executorProperties.getWorkQueue(), executorProperties.getQueueCapacity());
        //反射赋值
        ReflectUtil.setFieldValue(elasticExecutor, "workQueue", workQueue);

        // 赋值动态线程池其他核心参数
        elasticExecutor.setKeepAliveTime(executorProperties.getKeepAliveTime(), TimeUnit.SECONDS);
        elasticExecutor.allowCoreThreadTimeOut(executorProperties.getAllowCoreThreadTimeOut());
        elasticExecutor.setRejectedExecutionHandler(RejectedPolicyTypeEnum.createPolicy(executorProperties.getRejectedHandler()));
    }
}
