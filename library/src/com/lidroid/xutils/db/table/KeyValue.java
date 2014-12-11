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

package com.lidroid.xutils.db.table;

/**
 * 键值对模型
 */
public class KeyValue {
    /**
     * 键
     */
    public final String key;
    /**
     * 值
     */
    public final Object value;

    /**
     * 构造键值对
     * @param key 键
     * @param value 值
     */
    public KeyValue(String key, Object value) {
        this.key = key;
        this.value = value;
    }
}
