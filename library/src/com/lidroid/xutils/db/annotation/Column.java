/*
 * Copyright (c) 2013. wyouflf (wyouflf@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lidroid.xutils.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库表中列注解
 * 
 * <pre>
 * 列名不要使用SQL关键字、SQL保留字符、特殊字符、空格；
 * 不能用于主键ID的注解；
 * 如果没有该注解，则默认使用实体类属性名为该列列名（代码混淆可能导致列名不易分辨，建议添加该注解）
 * </pre>
 * 
 * @see com.lidroid.xutils.db.annotation.Id
 * @see com.lidroid.xutils.db.annotation.NotNull
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    
    /**
     * 数据库的列名
     * @return 列名（不要使用SQL关键字、SQL保留字符、特殊字符、空格）
     */
    String column() default "";

    /**
     * 列的数据默认值
     * @return 默认值
     */
    String defaultValue() default "";
}
