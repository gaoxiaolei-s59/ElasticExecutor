package org.puregxl.ElasticExecutor.com.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.error.Mark;

@Configuration
public class MarkerConfiguration {

    @Bean
    public Mark mark(){
        return new Mark();
    }

    /**
     * 注解启动逻辑的实现
     */
    public class Mark{

    }
}
