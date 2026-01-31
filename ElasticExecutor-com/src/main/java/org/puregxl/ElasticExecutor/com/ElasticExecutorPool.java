package org.puregxl.ElasticExecutor.com;

import java.lang.annotation.*;


/**
 * 标记为线程池基类
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ElasticExecutorPool {

}
