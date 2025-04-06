package com.netdisk.cloudserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class ThreadPoolConfiguration {


    /**
     * 核心线程池大小
     */
    private static final int CORE_POOL_SIZE = 9;  // 默认核心线程数，适中即可

    /**
     * 最大可创建的线程数
     */
    private static final int MAX_POOL_SIZE = 50;  // 允许的最大线程数，防止过多线程导致资源浪费

    /**
     * 队列最大长度
     */
    private static final int QUEUE_CAPACITY = 500;  // 队列的最大容量，过多任务会进入拒绝策略

    /**
     * 线程池维护线程所允许的空闲时间
     */
    private static final long KEEP_ALIVE_TIME = 1000L;  // 空闲线程的存活时间

    /**
     * 线程池的拒绝策略
     * 选择：CallerRunsPolicy - 如果任务无法执行，将由提交任务的线程来执行该任务
     */
    private final ThreadPoolExecutor.CallerRunsPolicy REJECT_POLICY = new ThreadPoolExecutor.CallerRunsPolicy();  // 任务返回提交线程执行


    @Bean
    public ExecutorService executorService() {
        AtomicInteger c = new AtomicInteger(1);
        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        return new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.MILLISECONDS,
                queue,
                r -> new Thread(r, "cloud-drive-pool-" + c.getAndIncrement()),
                REJECT_POLICY
        );

    }
}
