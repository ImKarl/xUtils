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

import android.text.TextUtils;
import com.lidroid.xutils.http.client.entity.BodyParamsEntity;
import com.lidroid.xutils.http.client.multipart.HttpMultipartMode;
import com.lidroid.xutils.http.client.multipart.MultipartEntity;
import com.lidroid.xutils.http.client.multipart.content.ContentBody;
import com.lidroid.xutils.http.client.multipart.content.FileBody;
import com.lidroid.xutils.http.client.multipart.content.InputStreamBody;
import com.lidroid.xutils.http.client.multipart.content.StringBody;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.task.Priority;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 网络请求参数
 */
public class RequestParams {

    private String charset = HTTP.UTF_8;

    private List<HeaderItem> headers;
    private List<NameValuePair> queryStringParams;
    private HttpEntity bodyEntity;
    private List<NameValuePair> bodyParams;
    private HashMap<String, ContentBody> fileParams;

    private Priority priority;

    /**
     * 构造网络请求参数
     * 
     * <pre>
     * 默认编码：UTF-8
     * </pre>
     */
    public RequestParams() {
    }
    /**
     * 构造网络请求参数
     * @param charset HTTP请求的字符编码（为空时，使用默认编码：UTF-8）
     * @see java.nio.charset.Charset
     */
    public RequestParams(String charset) {
        if (!TextUtils.isEmpty(charset)) {
            this.charset = charset;
        }
    }
    
    /**
     * 获取线程优先级
     * @return 线程优先级{@link com.lidroid.xutils.task.Priority}
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * 设置线程优先级
     * @param priority 线程优先级{@link com.lidroid.xutils.task.Priority}
     */
    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    /**
     * 获取HTTP请求的字符编码
     * @return 字符编码
     * @see java.nio.charset.Charset
     */
    public String getCharset() {
        return charset;
    }

    /**
     * 设置HTTP请求的内容类型Content-Type
     * @param contentType HTTP请求的内容类型Content-Type
     */
    public void setContentType(String contentType) {
        this.setHeader("Content-Type", contentType);
    }

    /**
     * 增加一个Header表头信息，将被附加在所有Header最后
     * @param header HTTP请求的表头信息{@link org.apache.http.Header}
     * @see #addHeader(String, String)
     * @see #addHeaders(List)
     * @see #setHeader(Header)
     * @see org.apache.http.Header
     */
    public void addHeader(Header header) {
        if (this.headers == null) {
            this.headers = new ArrayList<HeaderItem>();
        }
        this.headers.add(new HeaderItem(header));
    }

    /**
     * 增加一个Header表头信息，将被附加在所有Header最后
     * @param name Header的名称
     * @param value Header的值
     * @see #addHeader(Header)
     * @see #addHeaders(List)
     * @see #setHeader(String, String)
     * @see org.apache.http.Header
     */
    public void addHeader(String name, String value) {
        if (this.headers == null) {
            this.headers = new ArrayList<HeaderItem>();
        }
        this.headers.add(new HeaderItem(name, value));
    }

    /**
     * 增加所有的Header表头信息，将被附加在所有Header最后
     * @param headers HTTP请求的表头信息
     * @see #addHeader(String, String)
     * @see #addHeader(Header)
     * @see #setHeaders(List)
     * @see org.apache.http.Header
     */
    public void addHeaders(List<Header> headers) {
        if (this.headers == null) {
            this.headers = new ArrayList<HeaderItem>();
        }
        for (Header header : headers) {
            this.headers.add(new HeaderItem(header));
        }
    }

    /**
     * 覆盖Header表头信息，如果不存在，将被附加在所有Header最后
     * 
     * <pre>
     * Overwrites the first header with the same name.
     * The new header will be appended to the end of the list, if no header with the given name can be found.
     * </pre>
     * 
     * @param header
     * @see #setHeader(String, String)
     * @see #addHeader(Header)
     */
    public void setHeader(Header header) {
        if (this.headers == null) {
            this.headers = new ArrayList<HeaderItem>();
        }
        this.headers.add(new HeaderItem(header, true));
    }

    /**
     * 覆盖Header表头信息，如果不存在，将被附加在所有Header最后
     * 
     * <pre>
     * Overwrites the first header with the same name.
     * The new header will be appended to the end of the list, if no header with the given name can be found.
     * </pre>
     * 
     * @param name
     * @param value
     * @see #addHeader(String, String)
     * @see #setHeader(Header)
     */
    public void setHeader(String name, String value) {
        if (this.headers == null) {
            this.headers = new ArrayList<HeaderItem>();
        }
        this.headers.add(new HeaderItem(name, value, true));
    }

    /**
     * 覆盖Header表头信息
     * 
     * <pre>
     * Overwrites all the headers in the message.
     * </pre>
     *
     * @param headers
     */
    public void setHeaders(List<Header> headers) {
        if (this.headers == null) {
            this.headers = new ArrayList<HeaderItem>();
        }
        for (Header header : headers) {
            this.headers.add(new HeaderItem(header, true));
        }
    }

    /**
     * 添加URL参数
     * @param name 参数名
     * @param value 参数对应的值
     */
    public void addQueryStringParameter(String name, String value) {
        if (queryStringParams == null) {
            queryStringParams = new ArrayList<NameValuePair>();
        }
        queryStringParams.add(new BasicNameValuePair(name, value));
    }
    /**
     * 添加URL参数
     * @param nameValuePair 参数键值对
     */
    public void addQueryStringParameter(NameValuePair nameValuePair) {
        if (queryStringParams == null) {
            queryStringParams = new ArrayList<NameValuePair>();
        }
        queryStringParams.add(nameValuePair);
    }
    /**
     * 添加URL参数
     * @param nameValuePairs 参数键值对集合
     */
    public void addQueryStringParameter(List<NameValuePair> nameValuePairs) {
        if (queryStringParams == null) {
            queryStringParams = new ArrayList<NameValuePair>();
        }
        if (nameValuePairs != null && nameValuePairs.size() > 0) {
            for (NameValuePair pair : nameValuePairs) {
                queryStringParams.add(pair);
            }
        }
    }

    /**
     * 添加POST参数
     * @param name 参数名
     * @param value 参数对应的值
     */
    public void addBodyParameter(String name, String value) {
        if (bodyParams == null) {
            bodyParams = new ArrayList<NameValuePair>();
        }
        bodyParams.add(new BasicNameValuePair(name, value));
    }
    /**
     * 添加POST参数
     * @param nameValuePair 参数键值对
     */
    public void addBodyParameter(NameValuePair nameValuePair) {
        if (bodyParams == null) {
            bodyParams = new ArrayList<NameValuePair>();
        }
        bodyParams.add(nameValuePair);
    }
    /**
     * 添加POST参数
     * @param nameValuePairs 参数键值对集合
     */
    public void addBodyParameter(List<NameValuePair> nameValuePairs) {
        if (bodyParams == null) {
            bodyParams = new ArrayList<NameValuePair>();
        }
        if (nameValuePairs != null && nameValuePairs.size() > 0) {
            for (NameValuePair pair : nameValuePairs) {
                bodyParams.add(pair);
            }
        }
    }
    /**
     * 添加POST参数（上传文件）
     * @param name 参数名
     * @param file 要上传的文件{@link java.io.File}
     */
    public void addBodyParameter(String key, File file) {
        if (fileParams == null) {
            fileParams = new HashMap<String, ContentBody>();
        }
        fileParams.put(key, new FileBody(file));
    }
    /**
     * 添加POST参数（上传文件）
     * @param name 参数名
     * @param file 要上传的文件{@link java.io.File}
     * @param mimeType 文件类型{@link com.lidroid.xutils.http.client.multipart.MIME}
     */
    public void addBodyParameter(String key, File file, String mimeType) {
        if (fileParams == null) {
            fileParams = new HashMap<String, ContentBody>();
        }
        fileParams.put(key, new FileBody(file, mimeType));
    }
    /**
     * 添加POST参数（上传文件）
     * @param name 参数名
     * @param file 要上传的文件{@link java.io.File}
     * @param mimeType 文件类型{@link com.lidroid.xutils.http.client.multipart.MIME}
     * @param charset 文件编码
     */
    public void addBodyParameter(String key, File file, String mimeType, String charset) {
        if (fileParams == null) {
            fileParams = new HashMap<String, ContentBody>();
        }
        fileParams.put(key, new FileBody(file, mimeType, charset));
    }
    /**
     * 添加POST参数（上传文件）
     * @param name 参数名
     * @param file 要上传的文件{@link java.io.File}
     * @param fileName 文件名
     * @param mimeType 文件类型{@link com.lidroid.xutils.http.client.multipart.MIME}
     * @param charset 文件编码
     */
    public void addBodyParameter(String key, File file, String fileName, String mimeType, String charset) {
        if (fileParams == null) {
            fileParams = new HashMap<String, ContentBody>();
        }
        fileParams.put(key, new FileBody(file, fileName, mimeType, charset));
    }
    /**
     * 添加POST参数（数据流）
     * @param name 参数名
     * @param stream 数据流{@link java.io.InputStream}
     * @param length 数据流的长度
     */
    public void addBodyParameter(String key, InputStream stream, long length) {
        if (fileParams == null) {
            fileParams = new HashMap<String, ContentBody>();
        }
        fileParams.put(key, new InputStreamBody(stream, length));
    }
    /**
     * 添加POST参数（数据流）
     * @param name 参数名
     * @param stream 数据流{@link java.io.InputStream}
     * @param length 数据流的长度
     * @param fileName 文件名
     */
    public void addBodyParameter(String key, InputStream stream, long length, String fileName) {
        if (fileParams == null) {
            fileParams = new HashMap<String, ContentBody>();
        }
        fileParams.put(key, new InputStreamBody(stream, length, fileName));
    }
    /**
     * 添加POST参数（数据流）
     * @param name 参数名
     * @param stream 数据流{@link java.io.InputStream}
     * @param length 数据流的长度
     * @param fileName 文件名
     * @param mimeType 文件类型{@link com.lidroid.xutils.http.client.multipart.MIME}
     */
    public void addBodyParameter(String key, InputStream stream, long length, String fileName, String mimeType) {
        if (fileParams == null) {
            fileParams = new HashMap<String, ContentBody>();
        }
        fileParams.put(key, new InputStreamBody(stream, length, fileName, mimeType));
    }

    /**
     * 设置参数主体
     * @param bodyEntity 参数主体{@link org.apache.http.HttpEntity}
     */
    public void setBodyEntity(HttpEntity bodyEntity) {
        this.bodyEntity = bodyEntity;
        if (bodyParams != null) {
            bodyParams.clear();
            bodyParams = null;
        }
        if (fileParams != null) {
            fileParams.clear();
            fileParams = null;
        }
    }

    /**
     * 获取参数主体
     * @return 返回包含所有请求参数的对象{@link org.apache.http.HttpEntity}
     */
    public HttpEntity getEntity() {

        if (bodyEntity != null) {
            return bodyEntity;
        }

        HttpEntity result = null;

        if (fileParams != null && !fileParams.isEmpty()) {

            MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.STRICT, null, Charset.forName(charset));

            if (bodyParams != null && !bodyParams.isEmpty()) {
                for (NameValuePair param : bodyParams) {
                    try {
                        multipartEntity.addPart(param.getName(), new StringBody(param.getValue()));
                    } catch (UnsupportedEncodingException e) {
                        LogUtils.e(e.getMessage(), e);
                    }
                }
            }

            for (ConcurrentHashMap.Entry<String, ContentBody> entry : fileParams.entrySet()) {
                multipartEntity.addPart(entry.getKey(), entry.getValue());
            }

            result = multipartEntity;
        } else if (bodyParams != null && !bodyParams.isEmpty()) {
            result = new BodyParamsEntity(bodyParams, charset);
        }

        return result;
    }

    /**
     * 获取所有URL参数
     * @return URL参数集合
     */
    public List<NameValuePair> getQueryStringParams() {
        return queryStringParams;
    }

    /**
     * 获取所有请求头信息
     * @return 请求头信息集合
     */
    public List<HeaderItem> getHeaders() {
        return headers;
    }

    
    
    /**
     * 请求头信息描述
     */
    public class HeaderItem {
        public final boolean overwrite;
        public final Header header;

        public HeaderItem(Header header) {
            this.overwrite = false;
            this.header = header;
        }

        public HeaderItem(Header header, boolean overwrite) {
            this.overwrite = overwrite;
            this.header = header;
        }

        public HeaderItem(String name, String value) {
            this.overwrite = false;
            this.header = new BasicHeader(name, value);
        }

        public HeaderItem(String name, String value, boolean overwrite) {
            this.overwrite = overwrite;
            this.header = new BasicHeader(name, value);
        }
    }
}