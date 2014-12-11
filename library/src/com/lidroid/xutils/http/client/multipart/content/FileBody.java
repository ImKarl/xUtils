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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import com.lidroid.xutils.http.client.multipart.MIME;
import com.lidroid.xutils.util.IOUtils;

/**
 * 网络请求的文件内容主体
 * @since 4.0
 */
public class FileBody extends AbstractContentBody {

    private final File file;
    private final String filename;
    private final String charset;

    /**
     * 构造网络请求的文件内容主体
     * @param file 文件{@link java.io.File}
     * @param filename 文件名
     * @param mimeType MIME类型
     * @param charset 字符编码
     * @since 4.1
     * @see com.lidroid.xutils.http.client.multipart.MIME
     * @see java.nio.charset.Charset
     */
    public FileBody(final File file,
                    final String filename,
                    final String mimeType,
                    final String charset) {
        super(mimeType);
        if (file == null) {
            throw new IllegalArgumentException("File may not be null");
        }
        this.file = file;
        if (filename != null) {
            this.filename = filename;
        } else {
            this.filename = file.getName();
        }
        this.charset = charset;
    }
    /**
     * 构造网络请求的文件内容主体
     * @param file 文件{@link java.io.File}
     * @param mimeType MIME类型
     * @param charset 字符编码
     * @since 4.1
     * @see com.lidroid.xutils.http.client.multipart.MIME
     * @see java.nio.charset.Charset
     */
    public FileBody(final File file,
                    final String mimeType,
                    final String charset) {
        this(file, null, mimeType, charset);
    }
    /**
     * 构造网络请求的文件内容主体
     * @param file 文件{@link java.io.File}
     * @param mimeType MIME类型
     * @see com.lidroid.xutils.http.client.multipart.MIME
     */
    public FileBody(final File file, final String mimeType) {
        this(file, null, mimeType, null);
    }
    /**
     * 构造网络请求的文件内容主体
     * @param file 文件{@link java.io.File}
     */
    public FileBody(final File file) {
        this(file, null, "application/octet-stream", null);
    }

    
    /**
     * 获取文件输入流
     * @return 文件输入流{@link java.io.InputStream}
     * @throws IOException 文件IO操作异常
     */
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(this.file);
    }

    /**
     * 将文件内容写入到输出流
     * 
     * <pre>
     * out为空时，会抛出异常{@link java.io.OutputStream}
     * </pre>
     * 
     * @param out IO输出流 {@link java.io.OutputStream}
     * @throws IOExceptionIO流操作异常{@link java.io.IOException}
     */
    public void writeTo(final OutputStream out) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(this.file));
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
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * 获取传输编码
     * @return "binary"
     */
    public String getTransferEncoding() {
        return MIME.ENC_BINARY;
    }

    public String getCharset() {
        return charset;
    }

    public long getContentLength() {
        return this.file.length();
    }

    public String getFilename() {
        return filename;
    }

    public File getFile() {
        return this.file;
    }

}
