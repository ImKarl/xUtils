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

import android.os.SystemClock;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.callback.*;
import com.lidroid.xutils.task.PriorityAsyncTask;
import com.lidroid.xutils.util.OtherUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * 网络请求处理器
 * @param <T> 网络请求返回的数据类型
 */
public class HttpHandler<T> extends PriorityAsyncTask<Object, Object, Void> implements RequestCallBackHandler {

    private final AbstractHttpClient client;
    private final HttpContext context;

    private HttpRedirectHandler httpRedirectHandler;

    /**
     * 设置HTTP重定向处理器
     * @param httpRedirectHandler HTTP重定向处理器（为空时，忽略本次设置）
     */
    public void setHttpRedirectHandler(HttpRedirectHandler httpRedirectHandler) {
        if (httpRedirectHandler != null) {
            this.httpRedirectHandler = httpRedirectHandler;
        }
    }

    private String requestUrl;
    private String requestMethod;
    private HttpRequestBase request;
    private boolean isUploading = true;
    private RequestCallBack<T> callback;

    private int retriedCount = 0;
    private String fileSavePath = null;
    private boolean isDownloadingFile = false;
    private boolean autoResume = false; // Whether the downloading could continue from the point of interruption.
    private boolean autoRename = false; // Whether rename the file by response header info when the download completely.
    private String charset; // The default charset of response header info.

    /**
     * 构造网络请求处理器
     * @param client HTTP客户端编程工具包{@link org.apache.http.impl.client.AbstractHttpClient}
     * @param context HTTP请求上下文{@link org.apache.http.protocol.HttpContext}
     * @param charset HTTP响应头默认编码
     * @param callback 网络请求回调通知接口
     */
    public HttpHandler(AbstractHttpClient client, HttpContext context, String charset, RequestCallBack<T> callback) {
        this.client = client;
        this.context = context;
        this.callback = callback;
        this.charset = charset;
        this.client.setRedirectHandler(notUseApacheRedirectHandler);
    }

    private State state = State.WAITING;

    /**
     * 获取网络请求状态
     * @return 网络请求状态{@link com.lidroid.xutils.http.HttpHandler.State}
     */
    public State getState() {
        return state;
    }

    private long expiry = HttpCache.getDefaultExpiryTime();

    /**
     * 设置缓存过期时长
     * @param expiry 缓存过期时长
     */
    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    /**
     * 设置网络请求回调监听
     * @param callback 网络请求回调通知接口{@link com.lidroid.xutils.http.callback.RequestCallBack}
     */
    public void setRequestCallBack(RequestCallBack<T> callback) {
        this.callback = callback;
    }

    /**
     * 获取网络请求回调监听器
     * @return 网络请求回调监听器{@link com.lidroid.xutils.http.callback.RequestCallBack}
     */
    public RequestCallBack<T> getRequestCallBack() {
        return this.callback;
    }

    /**
     * 发起网络请求
     * @param request 网络请求描述{@link org.apache.http.client.methods.HttpRequestBase}
     * @return 网络请求响应信息（不为空）{@link com.lidroid.xutils.http.ResponseInfo}
     * @throws HttpException 网络请求异常{@link com.lidroid.xutils.exception.HttpException}
     */
    @SuppressWarnings("unchecked")
    private ResponseInfo<T> sendRequest(HttpRequestBase request) throws HttpException {
        HttpRequestRetryHandler retryHandler = client.getHttpRequestRetryHandler();
        while (true) {
            if (autoResume && isDownloadingFile) {
                File downloadFile = new File(fileSavePath);
                long fileLen = 0;
                if (downloadFile.isFile() && downloadFile.exists()) {
                    fileLen = downloadFile.length();
                }
                if (fileLen > 0) {
                    request.setHeader("RANGE", "bytes=" + fileLen + "-");
                }
            }

            boolean retry = true;
            IOException exception = null;
            try {
                requestMethod = request.getMethod();
                if (HttpUtils.sHttpCache.isEnabled(requestMethod)) {
                    String result = HttpUtils.sHttpCache.get(requestUrl);
                    if (result != null) {
                        return new ResponseInfo<T>(null, (T) result, true);
                    }
                }

                ResponseInfo<T> responseInfo = null;
                if (!isCancelled()) {
                    HttpResponse response = client.execute(request, context);
                    responseInfo = handleResponse(response);
                }
                return responseInfo;
            } catch (UnknownHostException e) {
                exception = e;
                retry = retryHandler.retryRequest(exception, ++retriedCount, context);
            } catch (IOException e) {
                exception = e;
                retry = retryHandler.retryRequest(exception, ++retriedCount, context);
            } catch (NullPointerException e) {
                exception = new IOException(e.getMessage());
                exception.initCause(e);
                retry = retryHandler.retryRequest(exception, ++retriedCount, context);
            } catch (HttpException e) {
                throw e;
            } catch (Throwable e) {
                exception = new IOException(e.getMessage());
                exception.initCause(e);
                retry = retryHandler.retryRequest(exception, ++retriedCount, context);
            }
            if (!retry) {
                throw new HttpException(exception);
            }
        }
    }

    @Override
    protected Void doInBackground(Object... params) {
        if (this.state == State.CANCELLED || params == null || params.length == 0) return null;

        if (params.length > 3) {
            fileSavePath = String.valueOf(params[1]);
            isDownloadingFile = fileSavePath != null;
            autoResume = (Boolean) params[2];
            autoRename = (Boolean) params[3];
        }

        try {
            if (this.state == State.CANCELLED) return null;
            // init request & requestUrl
            request = (HttpRequestBase) params[0];
            requestUrl = request.getURI().toString();
            if (callback != null) {
                callback.setRequestUrl(requestUrl);
            }

            this.publishProgress(UPDATE_START);

            lastUpdateTime = SystemClock.uptimeMillis();

            ResponseInfo<T> responseInfo = sendRequest(request);
            if (responseInfo != null) {
                this.publishProgress(UPDATE_SUCCESS, responseInfo);
                return null;
            }
        } catch (HttpException e) {
            this.publishProgress(UPDATE_FAILURE, e, e.getMessage());
        }

        return null;
    }

    private final static int UPDATE_START = 1;
    private final static int UPDATE_LOADING = 2;
    private final static int UPDATE_FAILURE = 3;
    private final static int UPDATE_SUCCESS = 4;

    @Override
    @SuppressWarnings("unchecked")
    protected void onProgressUpdate(Object... values) {
        if (this.state == State.CANCELLED || values == null || values.length == 0 || callback == null) return;
        switch ((Integer) values[0]) {
            case UPDATE_START:
                this.state = State.STARTED;
                callback.onStart();
                break;
            case UPDATE_LOADING:
                if (values.length != 3) return;
                this.state = State.LOADING;
                callback.onLoading(
                        Long.valueOf(String.valueOf(values[1])),
                        Long.valueOf(String.valueOf(values[2])),
                        isUploading);
                break;
            case UPDATE_FAILURE:
                if (values.length != 3) return;
                this.state = State.FAILURE;
                callback.onFailure((HttpException) values[1], (String) values[2]);
                break;
            case UPDATE_SUCCESS:
                if (values.length != 2) return;
                this.state = State.SUCCESS;
                callback.onSuccess((ResponseInfo<T>) values[1]);
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private ResponseInfo<T> handleResponse(HttpResponse response) throws HttpException, IOException {
        if (response == null) {
            throw new HttpException("response is null");
        }
        if (isCancelled()) return null;

        StatusLine status = response.getStatusLine();
        int statusCode = status.getStatusCode();
        if (statusCode < 300) {
            Object result = null;
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                isUploading = false;
                if (isDownloadingFile) {
                    autoResume = autoResume && OtherUtils.isSupportRange(response);
                    String responseFileName = autoRename ? OtherUtils.getFileNameFromHttpResponse(response) : null;
                    FileDownloadHandler downloadHandler = new FileDownloadHandler();
                    result = downloadHandler.handleEntity(entity, this, fileSavePath, autoResume, responseFileName);
                } else {
                    StringDownloadHandler downloadHandler = new StringDownloadHandler();
                    result = downloadHandler.handleEntity(entity, this, charset);
                    if (HttpUtils.sHttpCache.isEnabled(requestMethod)) {
                        HttpUtils.sHttpCache.put(requestUrl, (String) result, expiry);
                    }
                }
            }
            return new ResponseInfo<T>(response, (T) result, false);
        } else if (statusCode == 301 || statusCode == 302) {
            if (httpRedirectHandler == null) {
                httpRedirectHandler = new DefaultHttpRedirectHandler();
            }
            HttpRequestBase request = httpRedirectHandler.getDirectRequest(response);
            if (request != null) {
                return this.sendRequest(request);
            }
        } else if (statusCode == 416) {
            throw new HttpException(statusCode, "maybe the file has downloaded completely");
        } else {
            throw new HttpException(statusCode, status.getReasonPhrase());
        }
        return null;
    }

    /**
     * 取消网络请求任务
     */
    @Override
    public void cancel() {
        this.state = State.CANCELLED;

        if (request != null && !request.isAborted()) {
            try {
                request.abort();
            } catch (Throwable e) {
            }
        }
        if (!this.isCancelled()) {
            try {
                this.cancel(true);
            } catch (Throwable e) {
            }
        }

        if (callback != null) {
            callback.onCancelled();
        }
    }

    private long lastUpdateTime;

    /**
     * 更新任务进度
     * @param total 读取数据总大小（byte）
     * @param current 当前读取数据大小（byte）
     * @param forceUpdateUI 是否强制更新UI
     * @return 是否继续执行（true:继续执行，否则取消执行）
     */
    @Override
    public boolean updateProgress(long total, long current, boolean forceUpdateUI) {
        if (callback != null && this.state != State.CANCELLED) {
            if (forceUpdateUI) {
                this.publishProgress(UPDATE_LOADING, total, current);
            } else {
                long currTime = SystemClock.uptimeMillis();
                if (currTime - lastUpdateTime >= callback.getRate()) {
                    lastUpdateTime = currTime;
                    this.publishProgress(UPDATE_LOADING, total, current);
                }
            }
        }
        return this.state != State.CANCELLED;
    }

    /**
     * 网络请求状态
     */
    public enum State {
        /** 正在等待 */
        WAITING(0),
        /** 已开始 */
        STARTED(1),
        /** 加载中 */
        LOADING(2),
        /** 失败 */
        FAILURE(3),
        /** 已取消 */
        CANCELLED(4),
        /** 成功 */
        SUCCESS(5);
        private int value = 0;

        State(int value) {
            this.value = value;
        }

        public static State valueOf(int value) {
            switch (value) {
                case 0:
                    return WAITING;
                case 1:
                    return STARTED;
                case 2:
                    return LOADING;
                case 3:
                    return FAILURE;
                case 4:
                    return CANCELLED;
                case 5:
                    return SUCCESS;
                default:
                    return FAILURE;
            }
        }

        public int value() {
            return this.value;
        }
    }

    private static final NotUseApacheRedirectHandler notUseApacheRedirectHandler = new NotUseApacheRedirectHandler();

    private static final class NotUseApacheRedirectHandler implements RedirectHandler {
        @Override
        public boolean isRedirectRequested(HttpResponse httpResponse, HttpContext httpContext) {
            return false;
        }

        @Override
        public URI getLocationURI(HttpResponse httpResponse, HttpContext httpContext) throws ProtocolException {
            return null;
        }
    }
}
