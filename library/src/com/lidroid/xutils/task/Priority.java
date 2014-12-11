package com.lidroid.xutils.task;

/**
 * 线程优先级
 * 
 * <pre>
 * Author: wyouflf
 * Date: 14-5-16
 * Time: 上午11:25
 * </pre>
 * 
 * @author wyouflf
 */
public enum Priority {
    /** 前台-高 */
    UI_TOP,
    /** 前台-默认 */
    UI_NORMAL,
    /** 前台-低 */
    UI_LOW,
    /** 默认 */
    DEFAULT,
    /** 后台-高 */
    BG_TOP,
    /** 后台-默认 */
    BG_NORMAL,
    /** 后台-低 */
    BG_LOW;
}
