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

package com.lidroid.xutils.http.client.multipart;

/**
 * 最小的MIME字段
 * @since 4.0
 */
class MinimalField {

    private final String name;
    private final String value;

    MinimalField(final String name, final String value) {
        super();
        this.name = name;
        this.value = value;
    }

    /**
     * 获取字段名
     * @return 字段名
     */
    public String getName() {
        return this.name;
    }

    /**
     * 获取字段的值
     * @return 字段的值
     */
    public String getBody() {
        return this.value;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(this.name);
        buffer.append(": ");
        buffer.append(this.value);
        return buffer.toString();
    }

}
