package org.puregxl.ElasticExecutor.com.processor;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 静态的容器 在非 Spring 管理的类中获取 Bean
 */
public class ApplicationContextHolder implements ApplicationContextAware {

    //静态容器
    private static ApplicationContext APP_CONTEXT;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHolder.APP_CONTEXT = applicationContext;
    }

    /**
     * 根据类型获取Bean
     */
    public static <T> T getBean(Class<T> clazz){
        return APP_CONTEXT.getBean(clazz);
    }

    /**
     * 根据名称获取Bean
     * @param beanName
     * @return
     */
    public static Object getBean(String beanName){
        return APP_CONTEXT.getBean(beanName);
    }

    /**
     * 根据名称和类型获取 Bean
     * @param name Bean 的名称
     * @param clazz Bean 的类
     * @param <T> 泛型
     * @return Bean 实例
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return APP_CONTEXT.getBean(name, clazz);
    }

    /**
     * 获取指定类型的所有 Bean (返回 Map<BeanName, BeanInstance>)
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return APP_CONTEXT.getBeansOfType(clazz);
    }

    // ================== 2. 代码中核心需要的注解查找 ==================

    /**
     * 在指定的 Bean 上查找注解
     * 核心作用：Spring 的这个方法可以处理 AOP 代理对象，找到原始类上的注解
     * * @param beanName Bean 名称
     * @param annotationType 注解类型
     * @param <A> 注解泛型
     * @return 注解实例，如果没找到则返回 null
     */
    public static <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) {
        // 使用 Spring 提供的 findAnnotationOnBean，它能自动处理代理问题
        return APP_CONTEXT.findAnnotationOnBean(beanName, annotationType);
    }

    // ================== 3. 其他实用方法 ==================

    /**
     * 获取环境配置 (application.properties / yml)
     * 例如: ApplicationContextHolder.getProperty("server.port")
     */
    public static String getProperty(String key) {
        Environment environment = APP_CONTEXT.getEnvironment();
        return environment.getProperty(key);
    }

    /**
     * 获取环境配置，带默认值
     */
    public static String getProperty(String key, String defaultValue) {
        Environment environment = APP_CONTEXT.getEnvironment();
        return environment.getProperty(key, defaultValue);
    }

    /**
     * 发布事件
     * 用于观察者模式解耦
     * @param event 事件对象
     */
    public static void publishEvent(Object event) {
        APP_CONTEXT.publishEvent(event);
    }


}
