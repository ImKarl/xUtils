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

package com.lidroid.xutils.http.client.entity;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * GZip参数实体的数据流解压器（实现自{@link org.apache.http.HttpEntity}）
 */
public class GZipDecompressingEntity extends DecompressingEntity {

    /**
     * 构造GZip数据流解压器（包装请求参数实体）
     * @param wrapped 非空的网络请求参数实体{@link org.apache.http.HttpEntity}
     */
    public GZipDecompressingEntity(final HttpEntity entity) {
        super(entity);
    }

    @Override
    InputStream decorate(final InputStream wrapped) throws IOException {
        return new GZIPInputStream(wrapped);
    }

    /**
     * 获取内容编码
     * 
     * <pre>
     * 原文：This HttpEntityWrapper has dealt with the Content-Encoding
     * </pre>
     * 
     * @return <code>null</code>
     */
    @Override
    public Header getContentEncoding() {
        /* This HttpEntityWrapper has dealt with the Content-Encoding. */
        return null;
    }
    
}
