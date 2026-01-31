package org.puregxl.ElasticExecutor.com.stater.config;

import org.puregxl.ElasticExecutor.com.config.ElasticExecutorConfiguration;
import org.puregxl.ElasticExecutor.com.config.MarkerConfiguration;
import org.puregxl.ElasticExecutor.core.config.BootstrapConfigProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;



@Import(ElasticExecutorConfiguration.class)
@ConditionalOnBean(MarkerConfiguration.Mark.class)
@AutoConfigureAfter(ElasticExecutorConfiguration.class)
@ConditionalOnProperty(prefix = BootstrapConfigProperties.PRE, value = "enable", matchIfMissing = true, havingValue = "true")
public class ComAutoConfiguration {

    @Bean
    public BootstrapConfigProperties bootstrapConfigProperties(Environment environment) {
        BootstrapConfigProperties properties = Binder.get(environment)
                .bind(BootstrapConfigProperties.PRE, BootstrapConfigProperties.class) // 把指定环境内容绑定到绑定配置类上
                .orElseThrow(() -> new IllegalStateException("缺少必要的配置项: " + BootstrapConfigProperties.PRE)); // 增加异常提示
        BootstrapConfigProperties.setInstance(properties);
        return properties;
    }

}
