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
 * 框架内抛出异常的基类
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-7-24
 * Time: 下午3:00
 * </pre>
 * 
 * @author wyouflf
 */
public class BaseException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * 构造异常实例
     */
    public BaseException() {
    }
    /**
     * 构造异常实例
     * @param detailMessage 异常信息描述
     */
    public BaseException(String detailMessage) {
        super(detailMessage);
    }
    /**
     * 构造异常实例
     * @param detailMessage 异常信息描述
     * @param throwable 异常内容（包含栈堆跟踪）
     */
    public BaseException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
    /**
     * 构造异常实例
     * @param throwable 异常内容（包含栈堆跟踪）
     */
    public BaseException(Throwable throwable) {
        super(throwable);
    }
    
}
