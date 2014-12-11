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

package com.lidroid.xutils.exception;

/**
 * 网络请求异常
 */
public class HttpException extends BaseException {
    private static final long serialVersionUID = 1L;

    private int exceptionCode;

    /**
     * 构造网络请求异常
     */
    public HttpException() {
    }
    /**
     * 构造网络请求异常
     * @param detailMessage 异常信息描述
     */
    public HttpException(String detailMessage) {
        super(detailMessage);
    }
    /**
     * 构造网络请求异常
     * @param detailMessage 异常信息描述
     * @param throwable 异常内容（包含栈堆跟踪）
     */
    public HttpException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
    /**
     * 构造网络请求异常
     * @param throwable 异常内容（包含栈堆跟踪）
     */
    public HttpException(Throwable throwable) {
        super(throwable);
    }
    

    /**
     * 构造网络请求异常
     * @param exceptionCode HTTP响应状态码（0：表示请求出错，或没有响应）
     */
    public HttpException(int exceptionCode) {
        this.exceptionCode = exceptionCode;
    }

    /**
     * 构造网络请求异常
     * @param exceptionCode HTTP响应状态码（0：表示请求出错，或没有响应）
     * @param detailMessage 异常信息描述
     */
    public HttpException(int exceptionCode, String detailMessage) {
        super(detailMessage);
        this.exceptionCode = exceptionCode;
    }
    /**
     * 构造网络请求异常
     * @param exceptionCode HTTP响应状态码（0：表示请求出错，或没有响应）
     * @param detailMessage 异常信息描述
     * @param throwable 异常内容（包含栈堆跟踪）
     */
    public HttpException(int exceptionCode, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.exceptionCode = exceptionCode;
    }
    /**
     * 构造网络请求异常
     * @param exceptionCode HTTP响应状态码（0：表示请求出错，或没有响应）
     * @param throwable 异常内容（包含栈堆跟踪）
     */
    public HttpException(int exceptionCode, Throwable throwable) {
        super(throwable);
        this.exceptionCode = exceptionCode;
    }

    /**
     * 获取异常信息代码
     * @return HTTP响应状态码（0：表示请求出错，或没有响应）
     */
    public int getExceptionCode() {
        return exceptionCode;
    }
    
}
