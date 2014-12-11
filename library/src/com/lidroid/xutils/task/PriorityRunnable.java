package com.lidroid.xutils.task;

/**
 * 可设置线程优先级的任务接口
 * 
 * Author: wyouflf
 * Date: 14-5-16
 * Time: 上午11:25
 */
public class PriorityRunnable extends PriorityObject<Runnable> implements Runnable {

    /**
     * 构造任务接口
     * @param priority 线程优先级{@link com.lidroid.xutils.task.Priority}
     * @param obj 需要执行的任务
     */
    public PriorityRunnable(Priority priority, Runnable obj) {
        super(priority, obj);
    }

    /**
     * 执行任务
     */
    @Override
    public void run() {
        this.obj.run();
    }
    
}
