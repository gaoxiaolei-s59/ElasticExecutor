package org.puregxl.ElasticExecutor.com.config;

import org.puregxl.ElasticExecutor.com.processor.ApplicationContextHolder;
import org.puregxl.ElasticExecutor.com.processor.ElasticBeanPostProcessor;
import org.puregxl.ElasticExecutor.core.config.BootstrapConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class ElasticExecutorConfiguration {

    @Bean
    public ApplicationContextHolder applicationContextHolder() {
        return new ApplicationContextHolder();
    }

    @Bean
    @DependsOn("applicationContextHolder") //依赖于上述容器 才能实现Bean的后置处理器
    public ElasticBeanPostProcessor elasticBeanPostProcessor(BootstrapConfigProperties bootstrapConfigProperties){
        return new ElasticBeanPostProcessor(bootstrapConfigProperties);
    }

}
