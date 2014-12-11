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
 * 数据库表的注解
 * 
 * <pre>
 * 表名不要使用SQL关键字、SQL保留字符、特殊字符、空格；
 * 如果没有该注解，则默认使用实体类完整类名为该表的表名（代码混淆可能导致表名不易分辨，建议添加该注解）
 * </pre>
 * 
 * @see com.lidroid.xutils.db.annotation.Id
 * @see com.lidroid.xutils.db.annotation.Column
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

    /**
     * 数据库的表名
     * @return 表名（不要使用SQL关键字、SQL保留字符、特殊字符、空格）
     */
    String name() default "";

    /**
     * 数据库表创建后执行的SQL语句
     * @return SQL语句
     */
    String execAfterTableCreated() default "";
    
}