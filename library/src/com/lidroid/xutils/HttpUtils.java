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

package com.lidroid.xutils;

import android.text.TextUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.*;
import com.lidroid.xutils.http.callback.HttpRedirectHandler;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.DefaultSSLSocketFactory;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.http.client.RetryHandler;
import com.lidroid.xutils.http.client.entity.GZipDecompressingEntity;
import com.lidroid.xutils.task.PriorityExecutor;
import com.lidroid.xutils.util.OtherUtils;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.File;
import java.io.IOException;

/**
 * HTTP请求处理工具包
 */
public class HttpUtils {
    
    /** HTTP请求内存缓存管理器 */
    public final static HttpCache sHttpCache = new HttpCache();

    private final DefaultHttpClient httpClient;
    private final HttpContext httpContext = new BasicHttpContext();

    private HttpRedirectHandler httpRedirectHandler;

    /**
     * 构造HTTP请求处理工具包
     */
    public HttpUtils() {
        this(HttpUtils.DEFAULT_CONN_TIMEOUT, null);
    }
    /**
     * 构造HTTP请求处理工具包
     * 
     * <pre>
     * 自动构建userAgent:{@link com.lidroid.xutils.util.OtherUtils#getUserAgent(android.content.Context)}
     * </pre>
     * 
     * @param connTimeout 连接超时时长
     */
    public HttpUtils(int connTimeout) {
        this(connTimeout, null);
    }
    /**
     * 构造HTTP请求处理工具包
     * 
     * <pre>
     * 默认连接超时时长为：15s；
     * userAgent为空时，自动构建{@link com.lidroid.xutils.util.OtherUtils#getUserAgent(android.content.Context)}
     * </pre>
     * 
     * @param userAgent HTTP请求的用户代理头信息
     */
    public HttpUtils(String userAgent) {
        this(HttpUtils.DEFAULT_CONN_TIMEOUT, userAgent);
    }
    /**
     * 构造HTTP请求处理工具包
     * 
     * <pre>
     * userAgent为空时，自动构建{@link com.lidroid.xutils.util.OtherUtils#getUserAgent(android.content.Context)}
     * </pre>
     * 
     * @param connTimeout 连接超时时长
     * @param userAgent HTTP请求的用户代理头信息
     */
    public HttpUtils(int connTimeout, String userAgent) {
        HttpParams params = new BasicHttpParams();

        ConnManagerParams.setTimeout(params, connTimeout);
        HttpConnectionParams.setSoTimeout(params, connTimeout);
        HttpConnectionParams.setConnectionTimeout(params, connTimeout);

        if (TextUtils.isEmpty(userAgent)) {
            userAgent = OtherUtils.getUserAgent(null);
        }
        HttpProtocolParams.setUserAgent(params, userAgent);

        ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(10));
        ConnManagerParams.setMaxTotalConnections(params, 10);

        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpConnectionParams.setSocketBufferSize(params, 1024 * 8);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", DefaultSSLSocketFactory.getSocketFactory(), 443));

        httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, schemeRegistry), params);

        httpClient.setHttpRequestRetryHandler(new RetryHandler(DEFAULT_RETRY_TIMES));

        httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
            @Override
            public void process(org.apache.http.HttpRequest httpRequest, HttpContext httpContext) throws org.apache.http.HttpException, IOException {
                if (!httpRequest.containsHeader(HEADER_ACCEPT_ENCODING)) {
                    httpRequest.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
                }
            }
        });

        httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
            @Override
            public void process(HttpResponse response, HttpContext httpContext) throws org.apache.http.HttpException, IOException {
                final HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return;
                }
                final Header encoding = entity.getContentEncoding();
                if (encoding != null) {
                    for (HeaderElement element : encoding.getElements()) {
                        if (element.getName().equalsIgnoreCase("gzip")) {
                            response.setEntity(new GZipDecompressingEntity(response.getEntity()));
                            return;
                        }
                    }
                }
            }
        });
    }

    // ************************************    default settings & fields ****************************

    private String responseTextCharset = HTTP.UTF_8;

    private long currentRequestExpiry = HttpCache.getDefaultExpiryTime();

    private final static int DEFAULT_CONN_TIMEOUT = 1000 * 15; // 15s

    private final static int DEFAULT_RETRY_TIMES = 3;

    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";

    private final static int DEFAULT_POOL_SIZE = 3;
    private final static PriorityExecutor EXECUTOR = new PriorityExecutor(DEFAULT_POOL_SIZE);

    /**
     * 获取HTTP客户端编程工具包
     * @return {@link org.apache.http.client.HttpClient}
     */
    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    // ***************************************** config *******************************************
    /**
     * 配置HTTP响应的文本编码
     * @param charSet 文本编码
     * @return 当前实例
     */
    public HttpUtils configResponseTextCharset(String charSet) {
        if (!TextUtils.isEmpty(charSet)) {
            this.responseTextCharset = charSet;
        }
        return this;
    }
    
    /**
     * 配置HTTP重定向处理器
     * @param httpRedirectHandler HTTP重定向处理器
     * @return 当前实例
     */
    public HttpUtils configHttpRedirectHandler(HttpRedirectHandler httpRedirectHandler) {
        this.httpRedirectHandler = httpRedirectHandler;
        return this;
    }

    /**
     * 配置HTTP内存缓存大小
     * @param httpCacheSize HTTP内存缓存大小
     * @return 当前实例
     */
    public HttpUtils configHttpCacheSize(int httpCacheSize) {
        sHttpCache.setCacheSize(httpCacheSize);
        return this;
    }

    /**
     * 配置默认HTTP缓存过期时长（同时更新当前缓存过期时间）
     * @param defaultExpiry HTTP缓存过期时长
     * @return 当前实例
     */
    public HttpUtils configDefaultHttpCacheExpiry(long defaultExpiry) {
        HttpCache.setDefaultExpiryTime(defaultExpiry);
        currentRequestExpiry = HttpCache.getDefaultExpiryTime();
        return this;
    }

    /**
     * 配置当前HTTP缓存过期时长
     * @param currRequestExpiry HTTP缓存过期时长
     * @return 当前实例
     */
    public HttpUtils configCurrentHttpCacheExpiry(long currRequestExpiry) {
        this.currentRequestExpiry = currRequestExpiry;
        return this;
    }

    /**
     * 配置HTTP Cookie信息
     * @param cookieStore Cookie
     * @return 当前实例
     */
    public HttpUtils configCookieStore(CookieStore cookieStore) {
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        return this;
    }

    /**
     * 配置HTTP请求的用户代理头信息
     * @param userAgent HTTP请求的用户代理头信息
     * @return 当前实例
     */
    public HttpUtils configUserAgent(String userAgent) {
        HttpProtocolParams.setUserAgent(this.httpClient.getParams(), userAgent);
        return this;
    }

    /**
     * 配置HTTP连接超时时长
     * @param timeout 连接超时时长
     * @return 当前实例
     */
    public HttpUtils configTimeout(int timeout) {
        final HttpParams httpParams = this.httpClient.getParams();
        ConnManagerParams.setTimeout(httpParams, timeout);
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
        return this;
    }

    /**
     * 配置HTTP读取超时时长
     * @param timeout 读取超时时长
     * @return 当前实例
     */
    public HttpUtils configSoTimeout(int timeout) {
        final HttpParams httpParams = this.httpClient.getParams();
        HttpConnectionParams.setSoTimeout(httpParams, timeout);
        return this;
    }

    /**
     * 配置注册网络协议
     * @param scheme 网络协议标识
     * @return 当前实例
     */
    public HttpUtils configRegisterScheme(Scheme scheme) {
        this.httpClient.getConnectionManager().getSchemeRegistry().register(scheme);
        return this;
    }

    /**
     * 配置SSL加密通道创建工厂
     * @param sslSocketFactory SSL加密通道创建工厂
     * @return 当前实例
     */
    public HttpUtils configSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        Scheme scheme = new Scheme("https", sslSocketFactory, 443);
        this.httpClient.getConnectionManager().getSchemeRegistry().register(scheme);
        return this;
    }

    /**
     * 配置HTTP请求失败的重试次数
     * @param count HTTP请求失败的重试次数
     * @return 当前实例
     */
    public HttpUtils configRequestRetryCount(int count) {
        this.httpClient.setHttpRequestRetryHandler(new RetryHandler(count));
        return this;
    }

    /**
     * 配置HTTP连接的线程池大小
     * @param threadPoolSize 线程池大小
     * @return 当前实例
     */
    public HttpUtils configRequestThreadPoolSize(int threadPoolSize) {
        HttpUtils.EXECUTOR.setPoolSize(threadPoolSize);
        return this;
    }

    // ***************************************** send request *******************************************
    /**
     * 发起异步网络请求
     * @param method HTTP请求类型{@link com.lidroid.xutils.http.client.HttpRequest.HttpMethod}
     * @param url URL地址
     * @param callBack 网络请求回调通知接口{@link com.lidroid.xutils.http.callback.RequestCallBack}
     * @return 网络请求处理器{@link com.lidroid.xutils.http.HttpHandler}
     */
    public <T> HttpHandler<T> send(HttpRequest.HttpMethod method, String url,
                                   RequestCallBack<T> callBack) {
        return send(method, url, null, callBack);
    }
    /**
     * 发起异步网络请求
     * @param method HTTP请求类型{@link com.lidroid.xutils.http.client.HttpRequest.HttpMethod}
     * @param url URL地址
     * @param params 网络请求传入的参数
     * @param callBack 网络请求回调通知接口{@link com.lidroid.xutils.http.callback.RequestCallBack}
     * @return 网络请求处理器{@link com.lidroid.xutils.http.HttpHandler}
     */
    public <T> HttpHandler<T> send(HttpRequest.HttpMethod method, String url, RequestParams params,
                                   RequestCallBack<T> callBack) {
        if (url == null) throw new IllegalArgumentException("url may not be null");

        HttpRequest request = new HttpRequest(method, url);
        return sendRequest(request, params, callBack);
    }
    
    /**
     * 发起同步网络请求
     * @param method HTTP请求类型{@link com.lidroid.xutils.http.client.HttpRequest.HttpMethod}
     * @param url URL地址
     * @return 网络请求数据读取通道{@link com.lidroid.xutils.http.ResponseStream}
     * @throws HttpException 网络请求异常{@link com.lidroid.xutils.exception.HttpException}
     */
    public ResponseStream sendSync(HttpRequest.HttpMethod method, String url) throws HttpException {
        return sendSync(method, url, null);
    }
    /**
     * 发起同步网络请求
     * @param method HTTP请求类型{@link com.lidroid.xutils.http.client.HttpRequest.HttpMethod}
     * @param url URL地址
     * @param params 网络请求传入的参数
     * @return 网络请求数据读取通道{@link com.lidroid.xutils.http.ResponseStream}
     * @throws HttpException 网络请求异常{@link com.lidroid.xutils.exception.HttpException}
     */
    public ResponseStream sendSync(HttpRequest.HttpMethod method, String url, RequestParams params) throws HttpException {
        if (url == null) throw new IllegalArgumentException("url may not be null");

        HttpRequest request = new HttpRequest(method, url);
        return sendSyncRequest(request, params);
    }

    // ***************************************** download *******************************************
    /**
     * 下载网络文件
     * @param url URL地址
     * @param target 下载文件的目标路径（完整保存路径）
     * @param callback 网络请求回调通知接口{@link com.lidroid.xutils.http.callback.RequestCallBack}
     * @return 网络请求处理器{@link com.lidroid.xutils.http.HttpHandler}
     */
    public HttpHandler<File> download(String url, String target,
                                      RequestCallBack<File> callback) {
        return download(HttpRequest.HttpMethod.GET, url, target, null, false, false, callback);
    }
    /**
     * 下载网络文件
     * @param url URL地址
     * @param target 下载文件的目标路径（完整保存路径）
     * @param autoResume 是否支持恢复任务
     * @param callback 网络请求回调通知接口{@link com.lidroid.xutils.http.callback.RequestCallBack}
     * @return 网络请求处理器{@link com.lidroid.xutils.http.HttpHandler}
     */
    public HttpHandler<File> download(String url, String target,
                                      boolean autoResume, RequestCallBack<File> callback) {
        return download(HttpRequest.HttpMethod.GET, url, target, null, autoResume, false, callback);
    }
    /**
     * 下载网络文件
     * @param url URL地址
     * @param target 下载文件的目标路径（完整保存路径）
     * @param autoResume 是否支持恢复任务
     * @param autoRename 是否支持文件重命名（根据响应信息获取文件名）
     * @param callback 网络请求回调通知接口{@link com.lidroid.xutils.http.callback.RequestCallBack}
     * @return 网络请求处理器{@link com.lidroid.xutils.http.HttpHandler}
     */
    public HttpHandler<File> download(String url, String target,
                                      boolean autoResume, boolean autoRename, RequestCallBack<File> callback) {
        return download(HttpRequest.HttpMethod.GET, url, target, null, autoResume, autoRename, callback);
    }
    /**
     * 下载网络文件
     * @param url URL地址
     * @param target 下载文件的目标路径（完整保存路径）
     * @param params 网络请求参数{@link com.lidroid.xutils.http.RequestParams}
     * @param callback 网络请求回调通知接口{@link com.lidroid.xutils.http.callback.RequestCallBack}
     * @return 网络请求处理器{@link com.lidroid.xutils.http.HttpHandler}
     */
    public HttpHandler<File> download(String url, String target,
                                      RequestParams params, RequestCallBack<File> callback) {
        return download(HttpRequest.HttpMethod.GET, url, target, params, false, false, callback);
    }
    /**
     * 下载网络文件
     * @param url URL地址
     * @param target 下载文件的目标路径（完整保存路径）
     * @param params 网络请求参数{@link com.lidroid.xutils.http.RequestParams}
     * @param autoResume 是否支持恢复任务
     * @param callback 网络请求回调通知接口{@link com.lidroid.xutils.http.callback.RequestCallBack}
     * @return 网络请求处理器{@link com.lidroid.xutils.http.HttpHandler}
     */
    public HttpHandler<File> download(String url, String target,
                                      RequestParams params, boolean autoResume, RequestCallBack<File> callback) {
        return download(HttpRequest.HttpMethod.GET, url, target, params, autoResume, false, callback);
    }
    /**
     * 下载网络文件
     * @param url URL地址
     * @param target 下载文件的目标路径（完整保存路径）
     * @param params 网络请求参数{@link com.lidroid.xutils.http.RequestParams}
     * @param autoResume 是否支持恢复任务
     * @param autoRename 是否支持文件重命名（根据响应信息获取文件名）
     * @param callback 网络请求回调通知接口{@link com.lidroid.xutils.http.callback.RequestCallBack}
     * @return 网络请求处理器{@link com.lidroid.xutils.http.HttpHandler}
     */
    public HttpHandler<File> download(String url, String target,
                                      RequestParams params, boolean autoResume, boolean autoRename, RequestCallBack<File> callback) {
        return download(HttpRequest.HttpMethod.GET, url, target, params, autoResume, autoRename, callback);
    }
    /**
     * 下载网络文件
     * @param method HTTP请求类型{@link com.lidroid.xutils.http.client.HttpRequest.HttpMethod}
     * @param url URL地址
     * @param target 下载文件的目标路径（完整保存路径）
     * @param params 网络请求参数{@link com.lidroid.xutils.http.RequestParams}
     * @param callback 网络请求回调通知接口{@link com.lidroid.xutils.http.callback.RequestCallBack}
     * @return 网络请求处理器{@link com.lidroid.xutils.http.HttpHandler}
     */
    public HttpHandler<File> download(HttpRequest.HttpMethod method, String url, String target,
                                      RequestParams params, RequestCallBack<File> callback) {
        return download(method, url, target, params, false, false, callback);
    }
    /**
     * 下载网络文件
     * @param method HTTP请求类型{@link com.lidroid.xutils.http.client.HttpRequest.HttpMethod}
     * @param url URL地址
     * @param target 下载文件的目标路径（完整保存路径）
     * @param params 网络请求参数{@link com.lidroid.xutils.http.RequestParams}
     * @param autoResume 是否支持恢复任务
     * @param callback 网络请求回调通知接口{@link com.lidroid.xutils.http.callback.RequestCallBack}
     * @return 网络请求处理器{@link com.lidroid.xutils.http.HttpHandler}
     */
    public HttpHandler<File> download(HttpRequest.HttpMethod method, String url, String target,
                                      RequestParams params, boolean autoResume, RequestCallBack<File> callback) {
        return download(method, url, target, params, autoResume, false, callback);
    }
    /**
     * 下载网络文件
     * 
     * <pre>
     * url、target为空时，抛出异常{@link java.lang.IllegalArgumentException}
     * </pre>
     * 
     * @param method HTTP请求类型{@link com.lidroid.xutils.http.client.HttpRequest.HttpMethod}
     * @param url URL地址
     * @param target 下载文件的目标路径（完整保存路径）
     * @param params 网络请求参数{@link com.lidroid.xutils.http.RequestParams}
     * @param autoResume 是否支持恢复任务
     * @param autoRename 是否支持文件重命名（根据响应信息获取文件名）
     * @param callback 网络请求回调通知接口{@link com.lidroid.xutils.http.callback.RequestCallBack}
     * @return 网络请求处理器{@link com.lidroid.xutils.http.HttpHandler}
     */
    public HttpHandler<File> download(HttpRequest.HttpMethod method, String url, String target,
                                      RequestParams params, boolean autoResume, boolean autoRename, RequestCallBack<File> callback) {

        if (url == null) throw new IllegalArgumentException("url may not be null");
        if (target == null) throw new IllegalArgumentException("target may not be null");

        HttpRequest request = new HttpRequest(method, url);

        HttpHandler<File> handler = new HttpHandler<File>(httpClient, httpContext, responseTextCharset, callback);

        handler.setExpiry(currentRequestExpiry);
        handler.setHttpRedirectHandler(httpRedirectHandler);

        if (params != null) {
            request.setRequestParams(params, handler);
            handler.setPriority(params.getPriority());
        }
        handler.executeOnExecutor(EXECUTOR, request, target, autoResume, autoRename);
        return handler;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private <T> HttpHandler<T> sendRequest(HttpRequest request, RequestParams params, RequestCallBack<T> callBack) {

        HttpHandler<T> handler = new HttpHandler<T>(httpClient, httpContext, responseTextCharset, callBack);

        handler.setExpiry(currentRequestExpiry);
        handler.setHttpRedirectHandler(httpRedirectHandler);
        request.setRequestParams(params, handler);

        if (params != null) {
            handler.setPriority(params.getPriority());
        }
        handler.executeOnExecutor(EXECUTOR, request);
        return handler;
    }

    private ResponseStream sendSyncRequest(HttpRequest request, RequestParams params) throws HttpException {

        SyncHttpHandler handler = new SyncHttpHandler(httpClient, httpContext, responseTextCharset);

        handler.setExpiry(currentRequestExpiry);
        handler.setHttpRedirectHandler(httpRedirectHandler);
        request.setRequestParams(params);

        return handler.sendRequest(request);
    }
}
