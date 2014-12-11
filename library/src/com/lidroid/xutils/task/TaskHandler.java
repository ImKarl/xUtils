package com.lidroid.xutils.task;

/**
 * 任务处理接口
 * 
 * <pre>
 * Author: wyouflf
 * Time: 2014/05/23
 * </pre>
 * 
 * @author wyouflf
 */
public interface TaskHandler {

    /**
     * 判断是否支持暂停任务
     * @return 是否支持暂停任务
     */
    boolean supportPause();

    /**
     * 判断是否支持恢复任务
     * @return 是否支持恢复任务
     */
    boolean supportResume();

    /**
     * 判断是否支持取消任务
     * @return 是否支持取消任务
     */
    boolean supportCancel();

    /**
     * 暂停任务
     */
    void pause();

    /**
     * 恢复任务
     */
    void resume();

    /**
     * 取消任务
     */
    void cancel();

    /**
     * 判断任务是否处于暂停状态
     * @return 暂停:true，否则false
     */
    boolean isPaused();

    /**
     * 判断任务是否处于取消状态
     * @return 已取消:true，否则false
     */
    boolean isCancelled();
    
}
