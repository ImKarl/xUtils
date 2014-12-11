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

/**
 * 上传参数实体的接口定义
 * 
 * <pre>
 * Created with IntelliJ IDEA.
 * User: wyouflf
 * Date: 13-7-3
 * Time: 下午1:40
 * </pre>
 * 
 * @author wyouflf
 */
public interface UploadEntity {
    
    /**
     * 设置进度更新监听
     * @param callBackHandler 网络请求进度更新通知接口{@link com.lidroid.xutils.http.callback.RequestCallBackHandler}
     */
    void setCallBackHandler(RequestCallBackHandler callBackHandler);
    
}
