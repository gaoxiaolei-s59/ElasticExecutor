package org.puregxl.ElasticExecutor.nacos.stater.config;


import org.puregxl.ElasticExecutor.com.config.MarkerConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

@ConditionalOnBean(MarkerConfiguration.Mark.class)
public class NacosAutoConfiguration {



}
