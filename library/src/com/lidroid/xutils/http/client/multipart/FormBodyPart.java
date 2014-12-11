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

import com.lidroid.xutils.http.client.multipart.content.ContentBody;

/**
 * 内容主体组合
 * 
 * <pre>
 * 这个类会基于封装的内容主体，自动填充HTTP请求头信息
 * </pre>
 * 
 * <pre>
 * 原文：
 * FormBodyPart class represents a content body that can be used as a part of multipart encoded
 * entities. This class automatically populates the header with standard fields based on
 * the content description of the enclosed body.
 * </pre>
 *
 * @since 4.0
 */
public class FormBodyPart {

    private final String name;
    private final MinimalFieldHeader header;

    private final ContentBody body;

    /**
     * 构造内容主体组合
     * 
     * <pre>
     * name或body为空时，抛出异常{@link java.lang.IllegalArgumentException}
     * </pre>
     * 
     * @param name 参数名
     * @param body 内容主体{@link com.lidroid.xutils.http.client.multipart.content.ContentBody}
     */
    public FormBodyPart(final String name, final ContentBody body) {
        super();
        if (name == null) {
            throw new IllegalArgumentException("Name may not be null");
        }
        if (body == null) {
            throw new IllegalArgumentException("Body may not be null");
        }
        this.name = name;
        this.body = body;
        this.header = new MinimalFieldHeader();

        generateContentDisposition(body);
        generateContentType(body);
        generateTransferEncoding(body);
    }
    /**
     * 构造内容主体组合
     * 
     * <pre>
     * name或body为空时，抛出异常{@link java.lang.IllegalArgumentException}
     * </pre>
     * 
     * @param name 参数名
     * @param body 内容主体{@link com.lidroid.xutils.http.client.multipart.content.ContentBody}
     * @param contentDisposition Content-Disposition
     */
    public FormBodyPart(final String name, final ContentBody body, final String contentDisposition) {
        super();
        if (name == null) {
            throw new IllegalArgumentException("Name may not be null");
        }
        if (body == null) {
            throw new IllegalArgumentException("Body may not be null");
        }
        this.name = name;
        this.body = body;
        this.header = new MinimalFieldHeader();

        if (contentDisposition != null) {
            addField(MIME.CONTENT_DISPOSITION, contentDisposition);
        } else {
            generateContentDisposition(body);
        }
        generateContentType(body);
        generateTransferEncoding(body);
    }

    
    /**
     * 获取参数名
     * @return 参数名
     */
    public String getName() {
        return this.name;
    }

    /**
     * 获取内容主体
     * @return 内容主体{@link com.lidroid.xutils.http.client.multipart.content.ContentBody}
     */
    public ContentBody getBody() {
        return this.body;
    }

    /**
     * 获取HTTP请求头信息
     * @return HTTP请求头信息{@link com.lidroid.xutils.http.client.multipart.MinimalFieldHeader}
     */
    public MinimalFieldHeader getHeader() {
        return this.header;
    }

    /**
     * 添加HTPP请求头信息
     * 
     * <pre>
     * name为空时，抛出异常{@link java.lang.IllegalArgumentException}
     * </pre>
     * 
     * @param name 请求头参数名
     * @param value 请求头参数的值
     */
    public void addField(final String name, final String value) {
        if (name == null) {
            throw new IllegalArgumentException("Field name may not be null");
        }
        this.header.addField(new MinimalField(name, value));
    }

    protected void generateContentDisposition(final ContentBody body) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("form-data; name=\"");
        buffer.append(getName());
        buffer.append("\"");
        if (body.getFilename() != null) {
            buffer.append("; filename=\"");
            buffer.append(body.getFilename());
            buffer.append("\"");
        }
        addField(MIME.CONTENT_DISPOSITION, buffer.toString());
    }

    protected void generateContentType(final ContentBody body) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(body.getMimeType()); // MimeType cannot be null
        if (body.getCharset() != null) { // charset may legitimately be null
            buffer.append("; charset=");
            buffer.append(body.getCharset());
        }
        addField(MIME.CONTENT_TYPE, buffer.toString());
    }

    protected void generateTransferEncoding(final ContentBody body) {
        addField(MIME.CONTENT_TRANSFER_ENC, body.getTransferEncoding()); // TE cannot be null
    }

}
