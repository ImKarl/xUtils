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

/**
 * 网络请求进度更新通知接口
 */
public interface RequestCallBackHandler {
    
    /**
     * 更新进度
     * @param total 数据总大小（byte）
     * @param current 当前读取大小（byte）
     * @param forceUpdateUI 是否强制更新UI
     * @return 是否继续执行（false:取消读取数据，否则继续执行）
     */
    boolean updateProgress(long total, long current, boolean forceUpdateUI);
    
}
