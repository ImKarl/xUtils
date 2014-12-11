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

import com.lidroid.xutils.http.callback.RequestCallBackHandler;
import com.lidroid.xutils.util.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

/**
 * 网络请求参数实体的数据流解压器的通用基类（实现自{@link org.apache.http.HttpEntity}）
 * 
 * <pre>
 * Common base class for decompressing {@link org.apache.http.HttpEntity} implementations.
 * </pre>
 *
 * @since 4.1
 */
abstract class DecompressingEntity extends HttpEntityWrapper implements UploadEntity {

    /**
     * {@link #getContent()} method must return the same {@link java.io.InputStream}
     * instance when DecompressingEntity is wrapping a streaming entity.
     */
    private InputStream content;

    /**
     * 构造数据流解压器（包装请求参数实体）
     * @param wrapped 非空的网络请求参数实体{@link org.apache.http.HttpEntity}
     */
    public DecompressingEntity(final HttpEntity wrapped) {
        super(wrapped);
        this.uncompressedLength = wrapped.getContentLength();
    }

    /**
     * 解压数据流
     * @param wrapped 需要解压的数据流{@link java.io.InputStream}
     * @return 解压后的IO数据流{@link java.io.InputStream}
     * @throws IOException
     */
    abstract InputStream decorate(final InputStream wrapped) throws IOException;

    /**
     * 获取解压后的数据流
     * @return 解压后的IO数据流{@link java.io.InputStream}
     * @throws IOException IO流操作异常
     */
    private InputStream getDecompressingStream() throws IOException {
        InputStream in = null;
        try {
            in = wrappedEntity.getContent();
            return decorate(in);
        } catch (IOException ex) {
            IOUtils.closeQuietly(in);
            throw ex;
        }
    }

    /**
     * 获取解压后的数据流
     * 
     * <pre>
     * 如果包装的参数实体是流，则缓存解压后的数据流
     * </pre>
     * 
     * @return 解压后的IO数据流{@link java.io.InputStream}
     * @throws IOException IO流操作异常
     */
    @Override
    public InputStream getContent() throws IOException {
        if (wrappedEntity.isStreaming()) {
            if (content == null) {
                content = getDecompressingStream();
            }
            return content;
        } else {
            return getDecompressingStream();
        }
    }

    private long uncompressedLength;

    /**
     * 压缩的内容长度是不可知的
     * @return <code>-1</code>
     */
    @Override
    public long getContentLength() {
        /* length of compressed content is not known */
        return -1;
    }

    private long uploadedSize = 0;

    /**
     * 将内容写入到输出流
     * @param outStream IO输出流 {@link java.io.OutputStream}
     * @throws IOException IO流操作异常{@link java.io.IOException}
     */
    @Override
    public void writeTo(OutputStream outStream) throws IOException {
        if (outStream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        InputStream inStream = null;
        try {
            inStream = getContent();

            byte[] tmp = new byte[4096];
            int len;
            while ((len = inStream.read(tmp)) != -1) {
                outStream.write(tmp, 0, len);
                uploadedSize += len;
                if (callBackHandler != null) {
                    if (!callBackHandler.updateProgress(uncompressedLength, uploadedSize, false)) {
                        throw new InterruptedIOException("cancel");
                    }
                }
            }
            outStream.flush();
            if (callBackHandler != null) {
                callBackHandler.updateProgress(uncompressedLength, uploadedSize, true);
            }
        } finally {
            IOUtils.closeQuietly(inStream);
        }
    }

    private RequestCallBackHandler callBackHandler = null;

    @Override
    public void setCallBackHandler(RequestCallBackHandler callBackHandler) {
        this.callBackHandler = callBackHandler;
    }
    
}
