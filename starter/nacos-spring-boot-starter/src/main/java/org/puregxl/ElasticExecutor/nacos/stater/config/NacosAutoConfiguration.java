package org.puregxl.ElasticExecutor.nacos.stater.config;


import com.alibaba.cloud.nacos.NacosConfigManager;
import org.puregxl.ElasticExecutor.com.config.MarkerConfiguration;
import org.puregxl.ElasticExecutor.core.config.BootstrapConfigProperties;
import org.puregxl.ElasticExecutor.nacos.stater.nacos.NacosRefresherHandler;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;


@ConditionalOnBean(MarkerConfiguration.Mark.class)
public class NacosAutoConfiguration {

    @Bean
    public NacosRefresherHandler nacosRefresherHandler(NacosConfigManager nacosConfigManager, BootstrapConfigProperties properties){
        return new NacosRefresherHandler(nacosConfigManager.getConfigService(), properties);
    }
}

