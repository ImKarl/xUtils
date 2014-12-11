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

import com.lidroid.xutils.http.client.multipart.MIME;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 字节数组包裹的网络请求文件内容主体
 * 
 * <pre>
 * 原文：Body part that is built using a byte array containing a file.
 * </pre>
 *
 * @since 4.1
 */
public class ByteArrayBody extends AbstractContentBody {

    /**
     * The contents of the file contained in this part.
     */
    private final byte[] data;

    /**
     * The name of the file contained in this part.
     */
    private final String filename;

    /**
     * 构造字节数组包裹的文件内容主体
     * 
     * <pre>
     * data为空时，抛出异常{@link java.lang.IllegalArgumentException}
     * </pre>
     * 
     * @param data 包含的数据内容（原文：The contents of the file contained in this part.）
     * @param mimeType MIME类型（原文：The mime type of the file contained in this part.）
     * @param filename 文件名（原文：The name of the file contained in this part.）
     * @see com.lidroid.xutils.http.client.multipart.MIME
     */
    public ByteArrayBody(final byte[] data, final String mimeType, final String filename) {
        super(mimeType);
        if (data == null) {
            throw new IllegalArgumentException("byte[] may not be null");
        }
        this.data = data;
        this.filename = filename;
    }

    /**
     * 构造字节数组包裹的文件内容主体
     * 
     * <pre>
     * data为空时，抛出异常{@link java.lang.IllegalArgumentException}；
     * 默认：MIME类型（application/octet-stream）
     * </pre>
     * 
     * @param data 包含的数据内容（原文：The contents of the file contained in this part.）
     * @param filename 文件名（原文：The name of the file contained in this part.）
     * @see com.lidroid.xutils.http.client.multipart.MIME
     */
    public ByteArrayBody(final byte[] data, final String filename) {
        this(data, "application/octet-stream", filename);
    }

    public String getFilename() {
        return filename;
    }

    public void writeTo(final OutputStream out) throws IOException {
        out.write(data);
        callBackInfo.pos += data.length;
        callBackInfo.doCallBack(false);
    }

    /**
     * 获取字符编码
     * @return <code>null</code>
     */
    public String getCharset() {
        return null;
    }

    /**
     * 获取传输编码
     * @return "binary"
     */
    public String getTransferEncoding() {
        return MIME.ENC_BINARY;
    }

    public long getContentLength() {
        return data.length;
    }

}
