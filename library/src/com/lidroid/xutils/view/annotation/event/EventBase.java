package com.lidroid.xutils.view.annotation.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 事件注解属性（给注解类使用）
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-9-9
 * Time: 下午12:43
 * </pre>
 * 
 * @author wyouflf
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventBase {
    
    /**
     * 监听类型
     * @return {@link java.lang.Class}
     */
    Class<?> listenerType();

    /**
     * 设置监听器的方法
     * @return 方法名（如："setOnClickListener"）
     */
    String listenerSetter();

    /**
     * 监听器的实现方法
     * @return 方法名（如："onClick"）
     */
    String methodName();
    
}
