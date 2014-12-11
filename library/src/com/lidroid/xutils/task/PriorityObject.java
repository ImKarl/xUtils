package com.lidroid.xutils.task;

/**
 * 可设置线程优先级的任务对象
 * 
 * <pre>
 * Author: wyouflf
 * Date: 14-5-16
 * Time: 上午11:25
 * </pre>
 * 
 * @param <E> 对象类型
 * @author wyouflf
 */
public class PriorityObject<E> {

    /**
     * 线程优先级
     */
    public final Priority priority;
    /**
     * 任务对象
     */
    public final E obj;

    /**
     * 构造任务对象
     * @param priority 线程优先级{@link com.lidroid.xutils.task.Priority}
     * @param obj 任务对象
     */
    public PriorityObject(Priority priority, E obj) {
        this.priority = priority == null ? Priority.DEFAULT : priority;
        this.obj = obj;
    }
    
}
