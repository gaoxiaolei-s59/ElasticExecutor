package org.puregxl.ElasticExecutor.com;


import org.puregxl.ElasticExecutor.com.config.MarkerConfiguration;
import org.springframework.context.annotation.Import;
import java.lang.annotation.*;


@Import(MarkerConfiguration.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableElastic {

}
