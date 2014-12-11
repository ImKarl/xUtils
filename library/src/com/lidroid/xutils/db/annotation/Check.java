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
 * 数据库表中列CHECK约束注解（限制值的范围）
 * 
 * <pre>
 * SQL CHECK列约束用于限制列中的值的范围
 * </pre>
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-8-20
 * Time: 上午9:44
 * </pre>
 * 
 * @author wyouflf
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Check {
    
    /**
     * 标识该列只允许特定的值
     * @return 该列允许的值
     */
    String value();
    
}
