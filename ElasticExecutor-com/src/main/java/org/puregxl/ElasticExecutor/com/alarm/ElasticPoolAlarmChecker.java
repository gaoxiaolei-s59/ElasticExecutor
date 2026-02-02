package org.puregxl.ElasticExecutor.com.alarm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.puregxl.ElasticExecutor.core.builder.ThreadFactoryBuilder;
import org.puregxl.ElasticExecutor.core.executor.ElasticExecutorHolder;
import org.puregxl.ElasticExecutor.core.executor.ElasticExecutorProperties;
import org.puregxl.ElasticExecutor.core.executor.ElasticExecutorRegister;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.Collection;
import java.util.concurrent.*;

import static org.puregxl.ElasticExecutor.core.message.MessageFormat.ALARM_LOG_TEXT;

@Slf4j
@RequiredArgsConstructor
public class ElasticPoolAlarmChecker implements ApplicationRunner {

    // 使用单线程即可，给线程起个好名字方便 debug
    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(
            1,
            ThreadFactoryBuilder.builder()
                    .pre("scheduler-alarm-checker") // 稍微缩短一下前缀
                    .build()
    );

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // ApplicationRunner 保证了在容器完全启动后才执行
        start();
    }

    public void start() {
        log.info("ElasticPoolAlarmChecker 报警检测任务已启动");
        scheduledExecutorService.scheduleWithFixedDelay(this::check, 0, 10, TimeUnit.SECONDS);
    }

    public void stop() {
        log.info("ElasticPoolAlarmChecker 正在停止...");
        if (!scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdown();
        }
    }

    public void check() {
        // 获取当前所有注册的动态线程池
        Collection<ElasticExecutorHolder> holders = ElasticExecutorRegister.getAll();
        for (ElasticExecutorHolder executorHolder : holders) {
            try {
                checkQueue(executorHolder);
                checkThreadActivity(executorHolder);
            } catch (Exception e) {
                log.error("检测线程池 [{}] 异常", executorHolder.getThreadPoolId(), e);
            }
        }
    }

    /**
     * 检查队列使用率
     */
    public void checkQueue(ElasticExecutorHolder holder) {
        ThreadPoolExecutor executor = holder.getExecutor();
        ElasticExecutorProperties properties = holder.getExecutorProperties();

        BlockingQueue<Runnable> queue = executor.getQueue();
        int size = queue.size();
        int capacity = size + queue.remainingCapacity();

        if (capacity == 0) {
            return;
        }


        int usage = (int) Math.round((size * 100.0) / capacity);
        int threshold = properties.getAlarmConfig().getQueueThreshold();

        if (usage > threshold) {
            // 修正点：将计算好的 usage 和 threshold 传进去
            sendAlarm(holder, "QUEUE_USAGE", usage, threshold);
        }
    }

    /**
     * 检查活跃度（活跃线程数 / 最大线程数）
     */
    public void checkThreadActivity(ElasticExecutorHolder holder) {
        ThreadPoolExecutor executor = holder.getExecutor();
        ElasticExecutorProperties properties = holder.getExecutorProperties();

        int activeCount = executor.getActiveCount();
        int maximumPoolSize = executor.getMaximumPoolSize();

        if (maximumPoolSize == 0) return;

        int usage = (int) Math.round((activeCount * 100.0) / maximumPoolSize);
        int threshold = properties.getAlarmConfig().getActiveThreshold();

        if (usage > threshold) {
            sendAlarm(holder, "ACTIVE_THREADS", usage, threshold);
        }
    }

    /**
     * 发送报警
     * 修正点：增加了 usage 和 threshold 参数，匹配 ALARM_LOG_TEXT 的占位符数量
     */
    public void sendAlarm(ElasticExecutorHolder holder, String alarmType, int usage, int threshold) {
        ThreadPoolExecutor executor = holder.getExecutor();

        int queueSize = executor.getQueue().size();
        int queueCapacity = queueSize + executor.getQueue().remainingCapacity();


        String alarmLogContent = String.format(ALARM_LOG_TEXT,
                holder.getThreadPoolId(),       // 1. Pool Name
                alarmType,                      // 2. Alarm Type
                usage,                          // 3. Current Usage
                threshold,                      // 4. Threshold
                executor.getCorePoolSize(),     // 5. Core
                executor.getMaximumPoolSize(),  // 6. Max
                executor.getActiveCount(),      // 7. Active
                queueSize,                      // 8. Queue Size
                queueCapacity                   // 9. Queue Capacity
        );

        // 打印报警日志
        System.out.println(alarmLogContent);

        // TODO: 可以在这里扩展 NotifyService.send(...)


    }
}



