package com.lidroid.xutils.task;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 可调度优先级的线程池
 * 
 * <pre>
 * Author: wyouflf
 * Date: 14-5-16
 * Time: 上午11:25
 * </pre>
 * 
 * @author wyouflf
 */
public class PriorityExecutor implements Executor {

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 256;
    private static final int KEEP_ALIVE = 1;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "PriorityExecutor #" + mCount.getAndIncrement());
        }
    };

    private final BlockingQueue<Runnable> mPoolWorkQueue = new PriorityObjectBlockingQueue<Runnable>();
    private final ThreadPoolExecutor mThreadPoolExecutor;
    
    /**
     * 实例化可调度优先级的线程池（默认线程池大小：5）
     */
    public PriorityExecutor() {
        this(CORE_POOL_SIZE);
    }
    /**
     * 实例化可调度优先级的线程池
     * @param poolSize 线程池大小
     */
    public PriorityExecutor(int poolSize) {
        mThreadPoolExecutor = new ThreadPoolExecutor(
                poolSize,
                MAXIMUM_POOL_SIZE,
                KEEP_ALIVE,
                TimeUnit.SECONDS,
                mPoolWorkQueue,
                sThreadFactory);
    }

    /**
     * 获取线程池大小
     * @return 线程池大小
     */
    public int getPoolSize() {
        return mThreadPoolExecutor.getCorePoolSize();
    }

    /**
     * 设置线程池大小
     * @param poolSize 线程池大小
     */
    public void setPoolSize(int poolSize) {
        if (poolSize > 0) {
            mThreadPoolExecutor.setCorePoolSize(poolSize);
        }
    }

    /**
     * 判断是否任务正忙
     * @return 是否任务正忙
     */
    public boolean isBusy() {
        return mThreadPoolExecutor.getActiveCount() >= mThreadPoolExecutor.getCorePoolSize();
    }

    /**
     * 执行任务
     * @param r 需要执行的任务
     */
    @Override
    public void execute(final Runnable r) {
        mThreadPoolExecutor.execute(r);
    }
    
}
