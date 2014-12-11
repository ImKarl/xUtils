package com.lidroid.xutils.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库表的主表关联注解
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-9-10
 * Time: 下午6:44
 * </pre>
 * 
 * @author wyouflf
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Finder {

    /**
     * 主表的主键ID的列名（当前实体类对应的表）
     * @return 当前ID列名
     */
    String valueColumn();

    /**
     * 从表关联该主表的列名（从表生成的关联列名）
     * @return 从表关联该主表ID的列名（关联列的名称）
     */
    String targetColumn();
    
}
