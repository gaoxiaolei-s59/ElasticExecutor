package org.puregxl.ElasticExecutor.core.executor;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ElasticExecutorRegister {
    /**
     * 并发安全的哈希map
     */
    private static final ConcurrentHashMap<String, ElasticExecutorHolder> T_MAP = new ConcurrentHashMap<>();

    /**
     * 注册到我们的核心线程池参数
     */
    public static void put(String threadPoolId, ElasticExecutor executor, ElasticExecutorProperties properties){
        ElasticExecutorHolder holder = new ElasticExecutorHolder(threadPoolId, executor, properties);
        T_MAP.put(threadPoolId, holder);
    }

    /**
     *
     * @param threadPoolId
     */
    public static void remove(String threadPoolId){
        if (!T_MAP.containsKey(threadPoolId)) {
            throw new RuntimeException("错误的删除非动态线程池的参数");
        }
        T_MAP.remove(threadPoolId);
    }

    /**
     * 获取参数
     * @param threadPoolId 线程池唯一标识
     * @return
     */
    public static ElasticExecutorHolder get(String threadPoolId){
        if (!T_MAP.containsKey(threadPoolId)) {
            throw new RuntimeException("没有该实例");
        }
        return T_MAP.get(threadPoolId);
    }

    /**
     * 返回所有实例
     * @return
     */
    public static Collection<ElasticExecutorHolder> getAll(){
        return T_MAP.values();
    }


}
