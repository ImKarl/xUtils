package com.lidroid.xutils.view.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 首选项控件绑定注解
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-11-16
 * Time: 上午9:56
 * </pre>
 * 
 * @author wyouflf
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreferenceInject {

    /**
     * 要绑定的首选项标签名
     * @return 首选项标签名
     */
    String value();
    
}
