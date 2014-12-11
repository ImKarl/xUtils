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

package com.lidroid.xutils.http.callback;


import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;

/**
 * 网络请求回调通知接口
 * @param <T> 网络请求返回的数据类型
 */
public abstract class RequestCallBack<T> {

    private static final int DEFAULT_RATE = 1000;

    private static final int MIN_RATE = 200;

    private String requestUrl;

    protected Object userTag;

    /**
     * 构造网络请求回调
     * 
     * <pre>
     * 默认：更新任务进度的速率：1s
     * </pre>
     */
    public RequestCallBack() {
        this.rate = DEFAULT_RATE;
    }
    /**
     * 构造网络请求回调
     * @param rate 更新任务进度的速率（单位：ms）
     */
    public RequestCallBack(int rate) {
        this.rate = rate;
    }
    /**
     * 构造网络请求回调
     * 
     * <pre>
     * 默认：更新任务进度的速率：1s
     * </pre>
     * 
     * @param userTag 用户标记标签（便于使用者识别请求）
     */
    public RequestCallBack(Object userTag) {
        this.rate = DEFAULT_RATE;
        this.userTag = userTag;
    }
    /**
     * 构造网络请求回调
     * @param rate 更新任务进度的速率（单位：ms）
     * @param userTag 用户标记标签（便于使用者识别请求）
     */
    public RequestCallBack(int rate, Object userTag) {
        this.rate = rate;
        this.userTag = userTag;
    }

    private int rate;

    /**
     * 获取更新任务进度的速率
     * @return 速率（单位：ms）
     */
    public final int getRate() {
        if (rate < MIN_RATE) {
            return MIN_RATE;
        }
        return rate;
    }
    /**
     * 设置更新任务进度的速率
     * @param rate 速率（单位：ms）
     */
    public final void setRate(int rate) {
        this.rate = rate;
    }

    /**
     * 获取用户标记标签
     * @return 用户标记标签（便于使用者识别请求）
     */
    public Object getUserTag() {
        return userTag;
    }
    /**
     * 设置用户标记标签
     * @param userTag 用户标记标签（便于使用者识别请求）
     */
    public void setUserTag(Object userTag) {
        this.userTag = userTag;
    }

    /**
     * 获取请求的URL
     * @return URL地址
     */
    public final String getRequestUrl() {
        return requestUrl;
    }
    /**
     * 设置请求的URL
     * @param requestUrl URL地址
     */
    public final void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    /**
     * 开始发送网络请求（需覆盖重写）
     */
    public void onStart() {
    }
    /**
     * 已取消网络请求（需覆盖重写）
     */
    public void onCancelled() {
    }
    /**
     * 更新任务进度（需覆盖重写）
     * @param total 读取数据总大小（byte）
     * @param current 当前读取数据大小（byte）
     * @param isUploading 是否上传操作
     */
    public void onLoading(long total, long current, boolean isUploading) {
    }
    /**
     * 网络请求执行成功（需覆盖重写）
     * @param responseInfo 网络请求响应信息{@link com.lidroid.xutils.http.ResponseInfo}
     */
    public abstract void onSuccess(ResponseInfo<T> responseInfo);
    /**
     * 网络请求执行失败（需覆盖重写）
     * @param error 网络请求异常{@link com.lidroid.xutils.exception.HttpException}
     * @param msg 异常信息描述
     */
    public abstract void onFailure(HttpException error, String msg);
    
}
