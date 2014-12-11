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

package com.lidroid.xutils.http.client.multipart.content;

/**
 * 网络请求的内容描述接口（定义通用属性获取方法）
 */
public interface ContentDescriptor {

    /**
     * 获取MIME类型
     *
     * @return The MIME type, which has been parsed from the
     *         content-type definition. Must not be null, but
     *         "text/plain", if no content-type was specified.
     * @see #getMediaType()
     * @see #getSubType()
     * @see com.lidroid.xutils.http.client.multipart.MIME
     */
    String getMimeType();

    /**
     * 获取默认的MIME类型（如：<code>TEXT</code>, <code>IMAGE</code>, <code>MULTIPART</code>）
     * 
     * <pre>
     * 原文：
     * Gets the defaulted MIME media type for this content.
     * For example <code>TEXT</code>, <code>IMAGE</code>, <code>MULTIPART</code>
     * </pre>
     *
     * @return the MIME media type when content-type specified,
     *         otherwise the correct default (<code>TEXT</code>)
     * @see #getMimeType()
     * @see com.lidroid.xutils.http.client.multipart.MIME
     */
    String getMediaType();

    /**
     * 获取默认的MIME类型
     * 
     * <pre>
     * 原文：
     * Gets the defaulted MIME sub type for this content.
     * </pre>
     *
     * @return the MIME media type when content-type is specified,
     *         otherwise the correct default (<code>PLAIN</code>)
     * @see #getMimeType()
     * @see com.lidroid.xutils.http.client.multipart.MIME
     */
    String getSubType();

    /**
     * 获取字符编码
     * 
     * <pre>
     * 原文：
     * The body descriptors character set, defaulted appropriately for the MIME type.
     * 
     * For <code>TEXT</code> types, this will be defaulted to <code>UTF-8</code>.
     * For other types, when the charset parameter is missing this property will be null.
     * </pre>
     *
     * @return Character set, which has been parsed from the
     *         content-type definition. Not null for <code>TEXT</code> types, when unset will
     *         be set to default <code>UTF-8</code>. For other types, when unset,
     *         null will be returned.
     */
    String getCharset();

    /**
     * 获取传输编码
     * 
     * <pre>
     * 原文：
     * Returns the body descriptors transfer encoding.
     * </pre>
     *
     * @return The transfer encoding. Must not be null, but "7bit",
     *         if no transfer-encoding was specified.
     */
    String getTransferEncoding();

    /**
     * 获取内容长度
     * 
     * <pre>
     * 原文：
     * Returns the body descriptors content-length.
     * </pre>
     *
     * @return Content length, if known, or -1, to indicate the absence of a
     *         content-length header.
     */
    long getContentLength();

}
