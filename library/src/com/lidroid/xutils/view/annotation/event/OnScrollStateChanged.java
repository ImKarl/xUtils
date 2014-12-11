package com.lidroid.xutils.view.annotation.event;

import android.widget.AbsListView;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 列表控件的滑动状态改变事件注解
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-9-12
 * Time: 下午11:25
 * </pre>
 * 
 * @author wyouflf
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@EventBase(
        listenerType = AbsListView.OnScrollListener.class,
        listenerSetter = "setOnScrollListener",
        methodName = "onScrollStateChanged")
public @interface OnScrollStateChanged {
    
    /**
     * 要绑定事件的控件ID
     * @return 控件ID
     */
    int[] value();

    /**
     * 所属父控件的ID
     * @return 控件ID
     */
    int[] parentId() default 0;
    
}
