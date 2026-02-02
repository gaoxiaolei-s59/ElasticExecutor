package org.puregxl.ElasticExecutor.core.message;

/**
 * 文本变更模板
 */
public class MessageFormat {
    /**
     * 线程池参数变更日志 - 文本块格式
     * <p>
     * 使用 JDK 15+ 文本块，包含分割线和对齐，清晰展示变更
     */
    public static final String CHANGE_THREAD_POOL_TEXT = """
            
            ----------------------------------------------------------------------
            🔄 [ElasticExecutor] Thread Pool Configuration Changed
            ----------------------------------------------------------------------
             Pool Name                : {}
             Core Pool Size           : {}
             Maximum Pool Size        : {}
             Queue Capacity           : {}
             Keep Alive Time          : {}
             Rejected Handle          : {}
             Allow Core Thread Timeout: {}
            ----------------------------------------------------------------------
            """;

    /**
     * 变更分隔符
     */
    public static final String CHANGE_DELIMITER = "%s ➜ %s";


    public static final String ALARM_LOG_TEXT = """
       \s
        ----------------------------------------------------------------------
        🚨 [ElasticExecutor] Thread Pool Alarm Triggered
        ----------------------------------------------------------------------
         Pool Name            : %s
         Alarm Type           : %s
         Current Usage        : %s%%
         Alarm Threshold      : %s%%
        ----------------------------------------------------------------------
         Core Pool Size       : %d
         Maximum Pool Size    : %d
         Active Threads       : %d
         Queue Size           : %d / %d
        ----------------------------------------------------------------------
        """;
}
