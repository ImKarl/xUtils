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

package com.lidroid.xutils.bitmap.download;

import android.content.Context;
import com.lidroid.xutils.BitmapUtils;

import java.io.OutputStream;

/**
 * 图片下载器
 */
public abstract class Downloader {

    /**
     * 根据URL下载Bitmap位图
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
     * @param outputStream 图片IO输出流
     * @param task 图片加载任务管理器
     * @return 过期时间的时间戳（如果下载失败，返回-1）
     */
    public abstract long downloadToStream(String uri, OutputStream outputStream, final BitmapUtils.BitmapLoadTask<?> task);

    private Context context;
    private long defaultExpiry;
    private int defaultConnectTimeout;
    private int defaultReadTimeout;

    /**
     * 获取{@link andorid.content.Context}
     * @return {@link andorid.content.Context}
     */
    public Context getContext() {
        return context;
    }

    /**
     * 设置{@link andorid.content.Context}
     * @param context andorid.content.Context
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * 设置默认过期时间
     * @param expiry 时间戳
     */
    public void setDefaultExpiry(long expiry) {
        this.defaultExpiry = expiry;
    }

    /**
     * 获取默认过期时间
     * @return 时间戳
     */
    public long getDefaultExpiry() {
        return this.defaultExpiry;
    }

    /**
     * 获取默认连接超时时长
     * @return 连接超时时长
     */
    public int getDefaultConnectTimeout() {
        return defaultConnectTimeout;
    }

    /**
     * 设置默认连接超时时长
     * @param defaultConnectTimeout 连接超时时长
     */
    public void setDefaultConnectTimeout(int defaultConnectTimeout) {
        this.defaultConnectTimeout = defaultConnectTimeout;
    }

    /**
     * 获取默认读取数据超时时长
     * @return 读取数据超时时长
     */
    public int getDefaultReadTimeout() {
        return defaultReadTimeout;
    }

    /**
     * 设置默认读取数据超时时长
     * @param defaultReadTimeout 读取数据超时时长
     */
    public void setDefaultReadTimeout(int defaultReadTimeout) {
        this.defaultReadTimeout = defaultReadTimeout;
    }
}
