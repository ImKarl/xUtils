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

package com.lidroid.xutils.http;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.util.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HTTP;

import java.io.*;
import java.util.Locale;

/**
 * 网络请求响应数据读取通道
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-7-31
 * Time: 下午3:27
 * </pre>
 * 
 * @author wyouflf
 */
public class ResponseStream extends InputStream {

    private HttpResponse baseResponse;
    private InputStream baseStream;

    private String charset;

    private String requestUrl;
    private String requestMethod;
    private long expiry;

    /**
     * 构造网络请求响应数据读取通道
     * @param baseResponse HTTP响应{@link org.apache.http.HttpResponse}
     * @param requestUrl URL地址
     * @param expiry 过期时间（时间戳）
     * @throws IOException IO异常{@link java.io.IOException}
     */
    public ResponseStream(HttpResponse baseResponse, String requestUrl, long expiry) throws IOException {
        this(baseResponse, HTTP.UTF_8, requestUrl, expiry);
    }
    /**
     * 构造网络请求响应数据读取通道
     * @param baseResponse HTTP响应{@link org.apache.http.HttpResponse}
     * @param charset 字符编码
     * @param requestUrl URL地址
     * @param expiry 过期时间（时间戳）
     * @throws IOException IO异常{@link java.io.IOException}
     */
    public ResponseStream(HttpResponse baseResponse, String charset, String requestUrl, long expiry) throws IOException {
        if (baseResponse == null) {
            throw new IllegalArgumentException("baseResponse may not be null");
        }

        this.baseResponse = baseResponse;
        this.baseStream = baseResponse.getEntity().getContent();
        this.charset = charset;
        this.requestUrl = requestUrl;
        this.expiry = expiry;
    }

    private String _directResult;

    /**
     * 构造网络请求响应数据读取通道
     * @param result 响应数据
     * @throws IOException IO异常{@link java.io.IOException}
     */
    public ResponseStream(String result) throws IOException {
        if (result == null) {
            throw new IllegalArgumentException("result may not be null");
        }

        _directResult = result;
    }

    /**
     * 获取URL地址
     * @return URL地址
     */
    public String getRequestUrl() {
        return requestUrl;
    }

    /**
     * 获取HTTP请求类型
     * @return HTTP请求类型
     */
    public String getRequestMethod() {
        return requestMethod;
    }

    /*package*/ void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * 网络请求响应数据读取流
     * @return 读取流{@link java.io.InputStream}
     */
    public InputStream getBaseStream() {
        return baseStream;
    }

    /**
     * 获取HTTP响应实体
     * @return HTTP响应{@link org.apache.http.HttpResponse}
     */
    public HttpResponse getBaseResponse() {
        return baseResponse;
    }

    /**
     * 获取响应的状态码
     * @return HTTP状态码（默认：200）
     */
    public int getStatusCode() {
        if (_directResult != null) return 200;
        return baseResponse.getStatusLine().getStatusCode();
    }

    /**
     * 获取语言信息
     * @return 语言信息{@link java.util.Locale}
     */
    public Locale getLocale() {
        if (_directResult != null) return Locale.getDefault();
        return baseResponse.getLocale();
    }

    /**
     * 获取状态行的原因短语
     * @return 原因短语
     */
    public String getReasonPhrase() {
        if (_directResult != null) return "";
        return baseResponse.getStatusLine().getReasonPhrase();
    }

    /**
     * 读取响应中的数据
     * @return 响应中的数据
     * @throws IOException IO异常{@link java.io.IOException}
     */
    public String readString() throws IOException {
        if (_directResult != null) return _directResult;
        if (baseStream == null) return null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(baseStream, charset));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            _directResult = sb.toString();
            if (requestUrl != null && HttpUtils.sHttpCache.isEnabled(requestMethod)) {
                HttpUtils.sHttpCache.put(requestUrl, _directResult, expiry);
            }
            return _directResult;
        } finally {
            IOUtils.closeQuietly(baseStream);
        }
    }

    /**
     * 读取响应中的数据到本地文件
     * @param savePath 本地文件的完整路径
     * @throws IOException IO异常{@link java.io.IOException}
     */
    public void readFile(String savePath) throws IOException {
        if (_directResult != null) return;
        if (baseStream == null) return;
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(savePath));
            BufferedInputStream ins = new BufferedInputStream(baseStream);
            byte[] buffer = new byte[4096];
            int len = 0;
            while ((len = ins.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(baseStream);
        }
    }

    @Override
    public int read() throws IOException {
        if (baseStream == null) return -1;
        return baseStream.read();
    }

    @Override
    public int available() throws IOException {
        if (baseStream == null) return 0;
        return baseStream.available();
    }

    @Override
    public void close() throws IOException {
        if (baseStream == null) return;
        baseStream.close();
    }

    @Override
    public void mark(int readLimit) {
        if (baseStream == null) return;
        baseStream.mark(readLimit);
    }

    @Override
    public boolean markSupported() {
        if (baseStream == null) return false;
        return baseStream.markSupported();
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        if (baseStream == null) return -1;
        return baseStream.read(buffer);
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (baseStream == null) return -1;
        return baseStream.read(buffer, offset, length);
    }

    @Override
    public synchronized void reset() throws IOException {
        if (baseStream == null) return;
        baseStream.reset();
    }

    @Override
    public long skip(long byteCount) throws IOException {
        if (baseStream == null) return 0;
        return baseStream.skip(byteCount);
    }

    /**
     * 获取响应内容的长度
     * @return 响应内容的长度
     */
    public long getContentLength() {
        if (baseStream == null) return 0;
        return baseResponse.getEntity().getContentLength();
    }
    
}
