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

import com.lidroid.xutils.http.client.util.URLEncodedUtils;
import com.lidroid.xutils.util.LogUtils;
import org.apache.http.NameValuePair;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 网络请求参数实体
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-7-26
 * Time: 下午4:21
 * </pre>
 * 
 * @author wyouflf
 */
public class BodyParamsEntity extends AbstractHttpEntity implements Cloneable {

    protected byte[] content;

    private boolean dirty = true;

    private String charset = HTTP.UTF_8;

    private List<NameValuePair> params;

    /**
     * 构造网络请求参数
     * 
     * <pre>
     * 默认：
     * 字符编码：UTF-8，
     * 内容类型：application/x-www-form-urlencoded
     * </pre>
     */
    public BodyParamsEntity() {
        this((String) null);
    }
    /**
     * 构造网络请求参数
     * 
     * <pre>
     * 默认：
     * 内容类型：application/x-www-form-urlencoded
     * </pre>
     * 
     * @param charset 字符编码（如果为空，使用默认编码：UTF-8）
     */
    public BodyParamsEntity(String charset) {
        super();
        if (charset != null) {
            this.charset = charset;
        }
        setContentType(URLEncodedUtils.CONTENT_TYPE);
        params = new ArrayList<NameValuePair>();
    }
    /**
     * 构造网络请求参数
     * 
     * <pre>
     * 默认：
     * 字符编码：UTF-8，
     * 内容类型：application/x-www-form-urlencoded
     * </pre>
     * 
     * @param params 网络请求的参数
     */
    public BodyParamsEntity(List<NameValuePair> params) {
        this(params, null);
    }
    /**
     * 构造网络请求参数
     * 
     * <pre>
     * 默认：
     * 内容类型：application/x-www-form-urlencoded
     * </pre>
     * 
     * @param params 网络请求的参数
     * @param charset 字符编码（如果为空，使用默认编码：UTF-8）
     */
    public BodyParamsEntity(List<NameValuePair> params, String charset) {
        super();
        if (charset != null) {
            this.charset = charset;
        }
        setContentType(URLEncodedUtils.CONTENT_TYPE);
        this.params = params;
        refreshContent();
    }

    
    
    /**
     * 添加参数
     * @param name 参数名
     * @param value 参数的值
     * @return 当前实例
     */
    public BodyParamsEntity addParameter(String name, String value) {
        this.params.add(new BasicNameValuePair(name, value));
        this.dirty = true;
        return this;
    }
    /**
     * 添加参数
     * @param params 参数集合{@link java.util.List}
     * @return 当前实例
     */
    public BodyParamsEntity addParams(List<NameValuePair> params) {
        this.params.addAll(params);
        this.dirty = true;
        return this;
    }

    private void refreshContent() {
        if (dirty) {
            try {
                this.content = URLEncodedUtils.format(params, charset).getBytes(charset);
            } catch (UnsupportedEncodingException e) {
                LogUtils.e(e.getMessage(), e);
            }
            dirty = false;
        }
    }

    /**
     * 判断该参数实体是否可重复
     * @return <code>true</code>（可重复）
     */
    public boolean isRepeatable() {
        return true;
    }

    /**
     * 获取参数内容的长度
     * @return 参数内容的长度（单位：byte）
     */
    public long getContentLength() {
        refreshContent();
        return this.content.length;
    }

    /**
     * 获取参数内容的数据流
     * @return IO数据流 {@link java.io.InputStream}
     * @throws IOException IO流操作异常{@link java.io.IOException}
     */
    public InputStream getContent() throws IOException {
        refreshContent();
        return new ByteArrayInputStream(this.content);
    }

    /**
     * 将参数内容写入到输出流
     * @param outStream IO输出流 {@link java.io.OutputStream}
     * @throws IOException IO流操作异常{@link java.io.IOException}
     */
    public void writeTo(final OutputStream outStream) throws IOException {
        if (outStream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        refreshContent();
        outStream.write(this.content);
        outStream.flush();
    }

    /**
     * 判断该参数实体是否为数据流
     * @return <code>false</code>（不是IO数据流）
     */
    public boolean isStreaming() {
        return false;
    }

    /**
     * 克隆该参数实体
     * @return 新的实例
     * @throws CloneNotSupportedException 不支持克隆操作{@link java.CloneNotSupportedException}
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
