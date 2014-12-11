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
 * 数据库表中主键ID的列注解
 * 
 * <pre>
 * 列名不要使用SQL关键字、SQL保留字符、特殊字符、空格；
 * 若实体类属性名不是id、_id，必须添加该注解，否则报错；
 * 默认主键ID为数值自动增长；
 * 若主键非数值型（int ,Integer,long, Long），请添加注解{@link com.lidroid.xutils.db.annotation.NoAutoIncrement}
 * </pre>
 * 
 * @see com.lidroid.xutils.db.annotation.Column
 * @see com.lidroid.xutils.db.annotation.NoAutoIncrement
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {
    
    /**
     * 数据库的列名
     * @return 主键ID的列名（不要使用SQL关键字、SQL保留字符、特殊字符、空格）
     */
    String column() default "";
    
}
