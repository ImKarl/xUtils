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

package com.lidroid.xutils.view.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.lidroid.xutils.view.ResType;

/**
 * 资源文件{@link android.content.res.Resources}内容绑定注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResInject {
    
    /**
     * 要获取内容的资源文件ID
     * @return 资源文件ID（如：R.string.appname、R.color.blue等）
     */
    int id();

    /**
     * 资源类型
     * @return 资源类型（如：String、Color等）{@link com.lidroid.xutils.view.ResType}
     */
    ResType type();
    
}
