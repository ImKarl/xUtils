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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * HTTP重定向处理器
 * 
 * <pre>
 * Created with IntelliJ IDEA.
 * User: wyouflf
 * Date: 13-7-17
 * Time: 上午10:36
 * </pre>
 * 
 * @author wyouflf
 */
public interface HttpRedirectHandler {
    /**
     * 获取重定向的HTTP请求描述
     * @param response 重定向响应信息{@link org.apache.http.HttpResponse}
     * @return HTTP请求描述{@link org.apache.http.client.methods.HttpRequestBase}
     */
    HttpRequestBase getDirectRequest(HttpResponse response);
}
