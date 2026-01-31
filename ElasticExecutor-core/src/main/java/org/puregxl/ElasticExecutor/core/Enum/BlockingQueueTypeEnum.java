package org.puregxl.ElasticExecutor.core.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.*;


@Getter
@AllArgsConstructor
public enum BlockingQueueTypeEnum {
    /**
     * ArrayBlockingQueue 游街队列
     */
    ARRAY_BLOCKING_QUEUE("ArrayBlockingQueue") {
        @Override
        public BlockingQueue<Runnable> create(Integer capacity) {
            return new ArrayBlockingQueue<>(capacity);
        }
    },
    /**
     * LinkedBlockingQueue: 基于链表的阻塞队列，容量可选 (不填则为 Integer.MAX_VALUE)
     */
    LINKED_BLOCKING_QUEUE("LinkedBlockingQueue") {
        public LinkedBlockingQueue<Runnable> create(Integer capacity) {
            return new LinkedBlockingQueue<>(capacity);
        }
    },

    /**
     * SynchronousQueue: 不存储元素的阻塞队列，每个插入操作必须等待另一个线程的移除操作
     * 容量参数对此队列无效
     */
    SYNCHRONOUS_QUEUE("SynchronousQueue") {
        @Override
        public BlockingQueue<Runnable> create(Integer capacity) {
            return new SynchronousQueue<>();
        }
    },
    /**
     * LinkedTransferQueue: 无界阻塞队列
     */
    LINKED_TRANSFER_QUEUE("LinkedTransferQueue") {
        @Override
        public BlockingQueue<Runnable> create(Integer capacity) {
            return new LinkedTransferQueue<>();
        }
    },

    /**
     * PriorityBlockingQueue: 支持优先级的无界阻塞队列
     */
    PRIORITY_BLOCKING_QUEUE("PriorityBlockingQueue") {
        @Override
        public BlockingQueue<Runnable> create(Integer capacity) {
            return new PriorityBlockingQueue<>(capacity);
        }
    },

    /**
     * LinkedBlockingDeque: 双端阻塞队列
     */
    LINKED_BLOCKING_DEQUE("LinkedBlockingDeque") {
        @Override
        public BlockingQueue<Runnable> create(Integer capacity) {
            return new LinkedBlockingDeque<>(capacity);
        }
    };

    /**
     * 队列名称
     */
    private final String BlockingQueueName;

    /**
     * 默认容量 处理错误数据
     */
    private static final int DEFAULT_CAPACITY = 1024;

    public abstract BlockingQueue<Runnable> create(Integer capacity);

    // ================== 静态缓存与工厂方法 ==================

    private static final Map<String, BlockingQueueTypeEnum> BLOCKING_QUEUE_TYPE_ENUM_MAP = new ConcurrentHashMap<>();

    static {
        for (BlockingQueueTypeEnum value : BlockingQueueTypeEnum.values()) {
            BLOCKING_QUEUE_TYPE_ENUM_MAP.put(value.getBlockingQueueName(), value);
        }
    }

    public BlockingQueue<Runnable> createBlockQueue(String name, Integer capacity) {
        int actualCapacity = capacity == null || capacity < 0 ? DEFAULT_CAPACITY : capacity;
        BlockingQueueTypeEnum blockingQueueTypeEnum = BLOCKING_QUEUE_TYPE_ENUM_MAP.get(name);
        //如果为空返回默认数据
        if (blockingQueueTypeEnum == null) {
            return ARRAY_BLOCKING_QUEUE.create(actualCapacity);
        }
        //返回
        return blockingQueueTypeEnum.create(actualCapacity);
    }

}
