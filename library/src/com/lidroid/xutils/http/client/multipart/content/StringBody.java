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
import org.apache.http.protocol.HTTP;

import java.io.*;
import java.nio.charset.Charset;

/**
 * 网络请求的字符内容主体
 * @since 4.0
 */
public class StringBody extends AbstractContentBody {

    private final byte[] content;
    private final Charset charset;

    /**
     * 创建字符内容主体实例
     * @param text 字符内容（不能为{@code null}）
     * @param mimeType MIME类型（不能为{@code null}）
     * @param charset 字符编码（不能为{@code null}，默认："UTF-8"）
     * @return 新的实例
     * @throws IllegalArgumentException 字符编码不支持
     * @since 4.1
     */
    public static StringBody create(
            final String text,
            final String mimeType,
            final Charset charset) throws IllegalArgumentException {
        try {
            return new StringBody(text, mimeType, charset);
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalArgumentException("Charset " + charset + " is not supported", ex);
        }
    }
    /**
     * 创建字符内容主体实例
     * @param text 字符内容（不能为{@code null}）
     * @param charset 字符编码（不能为{@code null}，默认："UTF-8"）
     * @return 新的实例
     * @throws IllegalArgumentException 字符编码不支持
     * @since 4.1
     */
    public static StringBody create(
            final String text, final Charset charset) throws IllegalArgumentException {
        return create(text, null, charset);
    }
    /**
     * 创建字符内容主体实例
     * @param text 字符内容（不能为{@code null}）
     * @return 新的实例
     * @throws IllegalArgumentException 字符编码不支持
     * @since 4.1
     */
    public static StringBody create(final String text) throws IllegalArgumentException {
        return create(text, null, null);
    }

    
    /**
     * 构造一个指定文本，MIME类型和字符编码的内容主体
     * @param text 字符内容（不能为{@code null}）
     * @param mimeType MIME类型（不能为{@code null}）
     * @param charset 字符编码（不能为{@code null}，默认："UTF-8"）
     * @throws UnsupportedEncodingException 字符编码不支持
     * @throws IllegalArgumentException 字符内容为空
     */
    public StringBody(
            final String text,
            final String mimeType,
            Charset charset) throws UnsupportedEncodingException {
        super(mimeType);
        if (text == null) {
            throw new IllegalArgumentException("Text may not be null");
        }
        if (charset == null) {
            charset = Charset.forName(HTTP.UTF_8);
        }
        this.content = text.getBytes(charset.name());
        this.charset = charset;
    }
    /**
     * 构造一个指定文本，MIME类型和字符编码的内容主体
     * 
     * <pre>
     * 默认 MIME类型："text/plain"
     * </pre>
     * 
     * @param text 字符内容（不能为{@code null}）
     * @param charset 字符编码（不能为{@code null}，默认："UTF-8"）
     * @throws UnsupportedEncodingException 字符编码不支持
     * @throws IllegalArgumentException 字符内容为空
     */
    public StringBody(final String text, final Charset charset) throws UnsupportedEncodingException {
        this(text, "text/plain", charset);
    }
    /**
     * 构造一个指定文本，MIME类型和字符编码的内容主体
     * 
     * <pre>
     * 默认 MIME类型："text/plain"，字符编码："UTF-8"
     * </pre>
     * 
     * @param text 字符内容（不能为{@code null}）
     * @throws UnsupportedEncodingException 字符编码不支持
     * @throws IllegalArgumentException 字符内容为空
     */
    public StringBody(final String text) throws UnsupportedEncodingException {
        this(text, "text/plain", null);
    }

    
    /**
     * 获取字符流
     * @return IO字符流{@link java.io.Reader}
     */
    public Reader getReader() {
        return new InputStreamReader(
                new ByteArrayInputStream(this.content),
                this.charset);
    }

    public void writeTo(final OutputStream out) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        InputStream in = new ByteArrayInputStream(this.content);
        byte[] tmp = new byte[4096];
        int l;
        while ((l = in.read(tmp)) != -1) {
            out.write(tmp, 0, l);
            callBackInfo.pos += l;
            if (!callBackInfo.doCallBack(false)) {
                throw new InterruptedIOException("cancel");
            }
        }
        out.flush();
    }

    /**
     * 获取传输编码
     * @return "8bit"
     */
    public String getTransferEncoding() {
        return MIME.ENC_8BIT;
    }

    public String getCharset() {
        return this.charset.name();
    }

    public long getContentLength() {
        return this.content.length;
    }

    public String getFilename() {
        return null;
    }

}
