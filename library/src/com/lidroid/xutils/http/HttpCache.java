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

import android.text.TextUtils;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.cache.LruMemoryCache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP请求缓存管理器（内存缓存）
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-8-1
 * Time: 下午12:04
 * </pre>
 * 
 * @author wyouflf
 */
public class HttpCache {

    /**
     * key: url
     * value: response result
     */
    private final LruMemoryCache<String, String> mMemoryCache;

    private final static int DEFAULT_CACHE_SIZE = 1024 * 100;// string length
    private final static long DEFAULT_EXPIRY_TIME = 1000 * 60; // 60 seconds

    private int cacheSize = DEFAULT_CACHE_SIZE;

    private static long defaultExpiryTime = DEFAULT_EXPIRY_TIME;

    /**
     * 构造HTTP请求缓存管理器
     * 
     * <pre>
     * 默认：cacheSize=100KB，defaultExpiryTime=60s
     * 即构造：HttpCache(HttpCache.DEFAULT_CACHE_SIZE, HttpCache.DEFAULT_EXPIRY_TIME);
     * </pre>
     */
    public HttpCache() {
        this(HttpCache.DEFAULT_CACHE_SIZE, HttpCache.DEFAULT_EXPIRY_TIME);
    }
    /**
     * 构造HTTP请求缓存管理器
     * @param strLength 最大内存缓存大小
     * @param defaultExpiryTime 默认过期时长
     */
    public HttpCache(int strLength, long defaultExpiryTime) {
        this.cacheSize = strLength;
        HttpCache.defaultExpiryTime = defaultExpiryTime;

        mMemoryCache = new LruMemoryCache<String, String>(this.cacheSize) {
            @Override
            protected int sizeOf(String key, String value) {
                if (value == null) return 0;
                return value.length();
            }
        };
    }

    /**
     * 设置内存缓存大小
     * @param strLength 最大内存缓存大小
     */
    public void setCacheSize(int strLength) {
        mMemoryCache.setMaxSize(strLength);
    }

    /**
     * 设置默认缓存过期时长
     * @param defaultExpiryTime 缓存过期时长
     */
    public static void setDefaultExpiryTime(long defaultExpiryTime) {
        HttpCache.defaultExpiryTime = defaultExpiryTime;
    }

    /**
     * 获取默认缓存过期时长
     * @return 缓存过期时长
     */
    public static long getDefaultExpiryTime() {
        return HttpCache.defaultExpiryTime;
    }

    /**
     * 添加内存缓存记录（使用默认过期时长）
     * @param url URL地址
     * @param result URL对应的数据内容
     */
    public void put(String url, String result) {
        put(url, result, defaultExpiryTime);
    }
    /**
     * 添加内存缓存记录
     * @param url URL地址
     * @param result URL对应的数据内容
     * @param expiry 过期时长
     */
    public void put(String url, String result, long expiry) {
        if (url == null || result == null || expiry < 1) return;

        mMemoryCache.put(url, result, System.currentTimeMillis() + expiry);
    }

    /**
     * 获取内存缓存记录
     * 
     * <pre>
     * 没有记录或超过有效期，则返回null
     * </pre>
     * 
     * @param url URL地址
     * @return URL对应的数据内容
     */
    public String get(String url) {
        return (url != null) ? mMemoryCache.get(url) : null;
    }

    /**
     * 清空内存缓存
     */
    public void clear() {
        mMemoryCache.evictAll();
    }

    /**
     * 判断是否支持缓存
     * @param method HTTP请求类型{@link com.lidroid.xutils.http.client.HttpRequest.HttpMethod}
     * @return true:支持，否则false
     * @see #isEnabled(String)
     * @see com.lidroid.xutils.http.client.HttpRequest.HttpMethod
     */
    public boolean isEnabled(HttpRequest.HttpMethod method) {
        if (method == null) return false;

        Boolean enabled = httpMethod_enabled_map.get(method.toString());
        return enabled == null ? false : enabled;
    }
    /**
     * 判断是否支持缓存
     * @param method HTTP请求类型（忽略大小写）{@link com.lidroid.xutils.http.client.HttpRequest.HttpMethod}
     * @return true:支持，否则false
     * @see #isEnabled(com.lidroid.xutils.http.client.HttpRequest.HttpMethod)
     * @see com.lidroid.xutils.http.client.HttpRequest.HttpMethod
     */
    public boolean isEnabled(String method) {
        if (TextUtils.isEmpty(method)) return false;

        Boolean enabled = httpMethod_enabled_map.get(method.toUpperCase());
        return enabled == null ? false : enabled;
    }
    /**
     * 设置是否支持缓存
     * @param method HTTP请求类型（忽略大小写）{@link com.lidroid.xutils.http.client.HttpRequest.HttpMethod}
     * @param enabled 是否支持缓存（true:支持，否则false）
     * @see com.lidroid.xutils.http.client.HttpRequest.HttpMethod
     */
    public void setEnabled(HttpRequest.HttpMethod method, boolean enabled) {
        httpMethod_enabled_map.put(method.toString(), enabled);
    }

    private final static ConcurrentHashMap<String, Boolean> httpMethod_enabled_map;

    static {
        httpMethod_enabled_map = new ConcurrentHashMap<String, Boolean>(10);
        httpMethod_enabled_map.put(HttpRequest.HttpMethod.GET.toString(), true);
    }
}
