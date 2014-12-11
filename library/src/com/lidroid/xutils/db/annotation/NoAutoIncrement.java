package com.lidroid.xutils.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库表中主键ID不自动增长（默认自增）
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-9-24
 * Time: 上午9:33
 * </pre>
 * 
 * @author wyouflf
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoAutoIncrement {
}
