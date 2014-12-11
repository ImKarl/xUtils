package com.lidroid.xutils.http;

import org.apache.http.*;

import java.util.Locale;

/**
 * 网络请求响应信息
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-10-26
 * Time: 下午3:20
 * </pre>
 * 
 * @param <T> 响应的数据类型
 * 
 * @author wyouflf
 */
public final class ResponseInfo<T> {

    private final HttpResponse response;
    /** 响应的数据内容 */
    public T result;
    /** 响应的数据来源 */
    public final boolean resultFormCache;
    /** 语言信息 */
    public final Locale locale;

    // status line
    /** HTTP响应状态码 */
    public final int statusCode;
    /** 协议版本 */
    public final ProtocolVersion protocolVersion;
    /** 状态行的原因短语 */
    public final String reasonPhrase;

    // entity
    /** 响应内容的长度 */
    public final long contentLength;
    /** CONTENT-TYPE头信息 */
    public final Header contentType;
    /** CONTENT-ENCODING头信息 */
    public final Header contentEncoding;

    /**
     * 获取所有的头信息
     * @return 头信息{@link org.apache.http.Header}
     */
    public Header[] getAllHeaders() {
        if (response == null) return null;
        return response.getAllHeaders();
    }

    /**
     * 获取头信息
     * @param name 请求头的名称
     * @return 头信息{@link org.apache.http.Header}
     */
    public Header[] getHeaders(String name) {
        if (response == null) return null;
        return response.getHeaders(name);
    }

    /**
     * 获取第一个头信息（如果有多个的话）
     * @param name 请求头的名称
     * @return 头信息{@link org.apache.http.Header}
     */
    public Header getFirstHeader(String name) {
        if (response == null) return null;
        return response.getFirstHeader(name);
    }

    /**
     * 获取最后一个头信息（如果有多个的话）
     * @param name 请求头的名称
     * @return 头信息{@link org.apache.http.Header}
     */
    public Header getLastHeader(String name) {
        if (response == null) return null;
        return response.getLastHeader(name);
    }

    /**
     * 构造网络请求响应信息
     * @param response HTTP响应实体{@link org.apache.http.HttpResponse}
     * @param result 响应数据
     * @param resultFormCache 是否来自缓存
     */
    public ResponseInfo(final HttpResponse response, T result, boolean resultFormCache) {
        this.response = response;
        this.result = result;
        this.resultFormCache = resultFormCache;

        if (response != null) {
            locale = response.getLocale();

            // status line
            StatusLine statusLine = response.getStatusLine();
            if (statusLine != null) {
                statusCode = statusLine.getStatusCode();
                protocolVersion = statusLine.getProtocolVersion();
                reasonPhrase = statusLine.getReasonPhrase();
            } else {
                statusCode = 0;
                protocolVersion = null;
                reasonPhrase = null;
            }

            // entity
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                contentLength = entity.getContentLength();
                contentType = entity.getContentType();
                contentEncoding = entity.getContentEncoding();
            } else {
                contentLength = 0;
                contentType = null;
                contentEncoding = null;
            }
        } else {
            locale = null;

            // status line
            statusCode = 0;
            protocolVersion = null;
            reasonPhrase = null;

            // entity
            contentLength = 0;
            contentType = null;
            contentEncoding = null;
        }
    }

}
