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

package com.lidroid.xutils.bitmap;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;
import com.lidroid.xutils.bitmap.core.BitmapCache;
import com.lidroid.xutils.bitmap.download.DefaultDownloader;
import com.lidroid.xutils.bitmap.download.Downloader;
import com.lidroid.xutils.cache.FileNameGenerator;
import com.lidroid.xutils.task.Priority;
import com.lidroid.xutils.task.PriorityAsyncTask;
import com.lidroid.xutils.task.PriorityExecutor;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.util.OtherUtils;

import java.util.HashMap;

/**
 * 图片加载全局配置项
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-7-31
 * Time: 下午11:15
 * </pre>
 * 
 * @author wyouflf
 */
public class BitmapGlobalConfig {

    private String diskCachePath;
    /** 最小内存缓存大小：2M */
    public final static int MIN_MEMORY_CACHE_SIZE = 1024 * 1024 * 2; // 2M
    private int memoryCacheSize = 1024 * 1024 * 4; // 4MB
    /** 最小磁盘缓存大小：10M */
    public final static int MIN_DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10M
    private int diskCacheSize = 1024 * 1024 * 50;  // 50M

    private boolean memoryCacheEnabled = true;
    private boolean diskCacheEnabled = true;

    private Downloader downloader;
    private BitmapCache bitmapCache;

    private final static int DEFAULT_POOL_SIZE = 5;
    private final static PriorityExecutor BITMAP_LOAD_EXECUTOR = new PriorityExecutor(DEFAULT_POOL_SIZE);
    private final static PriorityExecutor DISK_CACHE_EXECUTOR = new PriorityExecutor(2);

    private long defaultCacheExpiry = 1000L * 60 * 60 * 24 * 30; // 30 days
    private int defaultConnectTimeout = 1000 * 15; // 15 sec
    private int defaultReadTimeout = 1000 * 15; // 15 sec

    private FileNameGenerator fileNameGenerator;

    private BitmapCacheListener bitmapCacheListener;

    private Context mContext;
    private final static HashMap<String, BitmapGlobalConfig> configMap = new HashMap<String, BitmapGlobalConfig>(1);

    /**
     * 构造图片加载全局配置项
     * @param context android.content.Context（如果为空，则抛出异常{@link java.lang.IllegalArgumentException}）
     * @param diskCachePath 本地磁盘缓存目录。（如果为空，默认使用"APP缓存目录/xBitmapCache"）
     */
    private BitmapGlobalConfig(Context context, String diskCachePath) {
        if (context == null) throw new IllegalArgumentException("context may not be null");
        this.mContext = context;
        this.diskCachePath = diskCachePath;
        initBitmapCache();
    }

    /**
     * 获取实例（根据diskCachePath的不同，创建多个实例）
     * @param context android.content.Context（如果为空，则抛出异常{@link java.lang.IllegalArgumentException}）
     * @param diskCachePath 本地磁盘缓存目录。（如果为空，默认使用"APP缓存目录/xBitmapCache"）
     * @return 图片加载全局配置项实例{@link com.lidroid.xutils.bitmap.BitmapGlobalConfig}
     */
    public synchronized static BitmapGlobalConfig getInstance(Context context, String diskCachePath) {

        if (TextUtils.isEmpty(diskCachePath)) {
            diskCachePath = OtherUtils.getDiskCacheDir(context, "xBitmapCache");
        }

        if (configMap.containsKey(diskCachePath)) {
            return configMap.get(diskCachePath);
        } else {
            BitmapGlobalConfig config = new BitmapGlobalConfig(context, diskCachePath);
            configMap.put(diskCachePath, config);
            return config;
        }
    }

    private void initBitmapCache() {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_INIT_MEMORY_CACHE);
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_INIT_DISK_CACHE);
    }

    /**
     * 获取本地磁盘缓存目录
     * @return 本地磁盘缓存目录
     */
    public String getDiskCachePath() {
        return diskCachePath;
    }

    /**
     * 获取图片下载器
     * @return 图片下载器{@link com.lidroid.xutils.bitmap.download.Downloader}
     */
    public Downloader getDownloader() {
        if (downloader == null) {
            downloader = new DefaultDownloader();
        }
        downloader.setContext(mContext);
        downloader.setDefaultExpiry(getDefaultCacheExpiry());
        downloader.setDefaultConnectTimeout(getDefaultConnectTimeout());
        downloader.setDefaultReadTimeout(getDefaultReadTimeout());
        return downloader;
    }

    /**
     * 设置图片下载器
     * @param downloader 图片下载器{@link com.lidroid.xutils.bitmap.download.Downloader}
     */
    public void setDownloader(Downloader downloader) {
        this.downloader = downloader;
    }

    /**
     * 获取默认缓存有效时间（默认：30 days）
     * @return 时间戳
     */
    public long getDefaultCacheExpiry() {
        return defaultCacheExpiry;
    }

    /**
     * 设置默认缓存有效时间
     * @param defaultCacheExpiry 时间戳
     */
    public void setDefaultCacheExpiry(long defaultCacheExpiry) {
        this.defaultCacheExpiry = defaultCacheExpiry;
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
     * 获取默认的读取数据超时时长
     * @return 读取数据超时时长
     */
    public int getDefaultReadTimeout() {
        return defaultReadTimeout;
    }

    /**
     * 设置默认的读取数据超时时长
     * @param defaultReadTimeout 读取数据超时时长
     */
    public void setDefaultReadTimeout(int defaultReadTimeout) {
        this.defaultReadTimeout = defaultReadTimeout;
    }

    /**
     * 获取Bitmap位图缓存管理器
     * @return Bitmap位图缓存管理器{@link com.lidroid.xutils.bitmap.core.BitmapCache}
     */
    public BitmapCache getBitmapCache() {
        if (bitmapCache == null) {
            bitmapCache = new BitmapCache(this);
        }
        return bitmapCache;
    }

    /**
     * 获取内存缓存大小（默认：4M）
     * @return 内存缓存大小(单位：byte)
     */
    public int getMemoryCacheSize() {
        return memoryCacheSize;
    }

    /**
     * 设置内存缓存大小
     * @param memoryCacheSize 内存缓存大小(单位：byte)（如果小于2M，随机使用内存大小的0.05~0.8）
     */
    public void setMemoryCacheSize(int memoryCacheSize) {
        if (memoryCacheSize >= MIN_MEMORY_CACHE_SIZE) {
            this.memoryCacheSize = memoryCacheSize;
            if (bitmapCache != null) {
                bitmapCache.setMemoryCacheSize(this.memoryCacheSize);
            }
        } else {
            this.setMemCacheSizePercent(0.3f);// Set default memory cache size.
        }
    }

    /**
     * 设置内存缓存占比
     * @param percent 内存缓存占用百分比。必须在0.05~0.8之间，否则抛出异常{@link java.lang.IllegalArgumentException}
     */
    public void setMemCacheSizePercent(float percent) {
        if (percent < 0.05f || percent > 0.8f) {
            throw new IllegalArgumentException("percent must be between 0.05 and 0.8 (inclusive)");
        }
        this.memoryCacheSize = Math.round(percent * getMemoryClass() * 1024 * 1024);
        if (bitmapCache != null) {
            bitmapCache.setMemoryCacheSize(this.memoryCacheSize);
        }
    }

    /**
     * 获取磁盘缓存大小（默认：50M）
     * @return 磁盘缓存大小(单位：byte)
     */
    public int getDiskCacheSize() {
        return diskCacheSize;
    }

    /**
     * 设置磁盘缓存大小
     * @param diskCacheSize 磁盘缓存大小(单位：byte)（如果小于10M，则忽略本次设置）
     */
    public void setDiskCacheSize(int diskCacheSize) {
        if (diskCacheSize >= MIN_DISK_CACHE_SIZE) {
            this.diskCacheSize = diskCacheSize;
            if (bitmapCache != null) {
                bitmapCache.setDiskCacheSize(this.diskCacheSize);
            }
        }
    }

    /**
     * 获取线程池大小
     * @return 线程池大小
     */
    public int getThreadPoolSize() {
        return BitmapGlobalConfig.BITMAP_LOAD_EXECUTOR.getPoolSize();
    }

    /**
     * 设置线程池大小
     * @param threadPoolSize 线程池大小
     */
    public void setThreadPoolSize(int threadPoolSize) {
        BitmapGlobalConfig.BITMAP_LOAD_EXECUTOR.setPoolSize(threadPoolSize);
    }

    /**
     * 获取图片加载线程池
     * @return 可调度优先级的线程池{@link com.lidroid.xutils.task.PriorityExecutor}
     */
    public PriorityExecutor getBitmapLoadExecutor() {
        return BitmapGlobalConfig.BITMAP_LOAD_EXECUTOR;
    }

    /**
     * 设置磁盘缓存线程池
     * @return 可调度优先级的线程池{@link com.lidroid.xutils.task.PriorityExecutor}
     */
    public PriorityExecutor getDiskCacheExecutor() {
        return BitmapGlobalConfig.DISK_CACHE_EXECUTOR;
    }

    /**
     * 判断内存缓存是否可用
     * @return 内存缓存是否可用（true:可用）
     */
    public boolean isMemoryCacheEnabled() {
        return memoryCacheEnabled;
    }

    /**
     * 设置内存缓存是否可用
     * @param memoryCacheEnabled 内存缓存是否可用（true:可用）
     */
    public void setMemoryCacheEnabled(boolean memoryCacheEnabled) {
        this.memoryCacheEnabled = memoryCacheEnabled;
    }

    /**
     * 判断磁盘缓存是否可用
     * @return 磁盘缓存是否可用（true:可用）
     */
    public boolean isDiskCacheEnabled() {
        return diskCacheEnabled;
    }

    /**
     * 设置磁盘缓存是否可用
     * @param diskCacheEnabled 磁盘缓存是否可用（true:可用）
     */
    public void setDiskCacheEnabled(boolean diskCacheEnabled) {
        this.diskCacheEnabled = diskCacheEnabled;
    }

    /**
     * 获取文件名生成器
     * @return 文件名生成器{@link com.lidroid.xutils.cache.FileNameGenerator}
     */
    public FileNameGenerator getFileNameGenerator() {
        return fileNameGenerator;
    }

    /**
     * 设置文件名生成器
     * @param fileNameGenerator 文件名生成器{@link com.lidroid.xutils.cache.FileNameGenerator}
     */
    public void setFileNameGenerator(FileNameGenerator fileNameGenerator) {
        this.fileNameGenerator = fileNameGenerator;
        if (bitmapCache != null) {
            bitmapCache.setDiskCacheFileNameGenerator(fileNameGenerator);
        }
    }

    /**
     * 获取图片缓存生命周期监听器
     * @return 图片缓存生命周期监听器{@link com.lidroid.xutils.bitmap.BitmapCacheListener}
     */
    public BitmapCacheListener getBitmapCacheListener() {
        return bitmapCacheListener;
    }

    /**
     * 设置图片缓存生命周期监听器
     * @param bitmapCacheListener 图片缓存生命周期监听器{@link com.lidroid.xutils.bitmap.BitmapCacheListener}
     */
    public void setBitmapCacheListener(BitmapCacheListener bitmapCacheListener) {
        this.bitmapCacheListener = bitmapCacheListener;
    }

    /**
     * 获取设备内存容量
     * @return 设备内存容量（单位：MB）
     */
    private int getMemoryClass() {
        return ((ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
    }

    ////////////////////////////////// bitmap cache management task ///////////////////////////////////////
    private class BitmapCacheManagementTask extends PriorityAsyncTask<Object, Void, Object[]> {
        public static final int MESSAGE_INIT_MEMORY_CACHE = 0;
        public static final int MESSAGE_INIT_DISK_CACHE = 1;
        public static final int MESSAGE_FLUSH = 2;
        public static final int MESSAGE_CLOSE = 3;
        public static final int MESSAGE_CLEAR = 4;
        public static final int MESSAGE_CLEAR_MEMORY = 5;
        public static final int MESSAGE_CLEAR_DISK = 6;
        public static final int MESSAGE_CLEAR_BY_KEY = 7;
        public static final int MESSAGE_CLEAR_MEMORY_BY_KEY = 8;
        public static final int MESSAGE_CLEAR_DISK_BY_KEY = 9;

        private BitmapCacheManagementTask() {
            this.setPriority(Priority.UI_TOP);
        }

        @Override
        protected Object[] doInBackground(Object... params) {
            if (params == null || params.length == 0) return params;
            BitmapCache cache = getBitmapCache();
            if (cache == null) return params;
            try {
                switch ((Integer) params[0]) {
                    case MESSAGE_INIT_MEMORY_CACHE:
                        cache.initMemoryCache();
                        break;
                    case MESSAGE_INIT_DISK_CACHE:
                        cache.initDiskCache();
                        break;
                    case MESSAGE_FLUSH:
                        cache.flush();
                        break;
                    case MESSAGE_CLOSE:
                        cache.clearMemoryCache();
                        cache.close();
                        break;
                    case MESSAGE_CLEAR:
                        cache.clearCache();
                        break;
                    case MESSAGE_CLEAR_MEMORY:
                        cache.clearMemoryCache();
                        break;
                    case MESSAGE_CLEAR_DISK:
                        cache.clearDiskCache();
                        break;
                    case MESSAGE_CLEAR_BY_KEY:
                        if (params.length != 2) return params;
                        cache.clearCache(String.valueOf(params[1]));
                        break;
                    case MESSAGE_CLEAR_MEMORY_BY_KEY:
                        if (params.length != 2) return params;
                        cache.clearMemoryCache(String.valueOf(params[1]));
                        break;
                    case MESSAGE_CLEAR_DISK_BY_KEY:
                        if (params.length != 2) return params;
                        cache.clearDiskCache(String.valueOf(params[1]));
                        break;
                    default:
                        break;
                }
            } catch (Throwable e) {
                LogUtils.e(e.getMessage(), e);
            }
            return params;
        }

        @Override
        protected void onPostExecute(Object[] params) {
            if (bitmapCacheListener == null || params == null || params.length == 0) return;
            try {
                switch ((Integer) params[0]) {
                    case MESSAGE_INIT_MEMORY_CACHE:
                        bitmapCacheListener.onInitMemoryCacheFinished();
                        break;
                    case MESSAGE_INIT_DISK_CACHE:
                        bitmapCacheListener.onInitDiskFinished();
                        break;
                    case MESSAGE_FLUSH:
                        bitmapCacheListener.onFlushCacheFinished();
                        break;
                    case MESSAGE_CLOSE:
                        bitmapCacheListener.onCloseCacheFinished();
                        break;
                    case MESSAGE_CLEAR:
                        bitmapCacheListener.onClearCacheFinished();
                        break;
                    case MESSAGE_CLEAR_MEMORY:
                        bitmapCacheListener.onClearMemoryCacheFinished();
                        break;
                    case MESSAGE_CLEAR_DISK:
                        bitmapCacheListener.onClearDiskCacheFinished();
                        break;
                    case MESSAGE_CLEAR_BY_KEY:
                        if (params.length != 2) return;
                        bitmapCacheListener.onClearCacheFinished(String.valueOf(params[1]));
                        break;
                    case MESSAGE_CLEAR_MEMORY_BY_KEY:
                        if (params.length != 2) return;
                        bitmapCacheListener.onClearMemoryCacheFinished(String.valueOf(params[1]));
                        break;
                    case MESSAGE_CLEAR_DISK_BY_KEY:
                        if (params.length != 2) return;
                        bitmapCacheListener.onClearDiskCacheFinished(String.valueOf(params[1]));
                        break;
                    default:
                        break;
                }
            } catch (Throwable e) {
                LogUtils.e(e.getMessage(), e);
            }
        }
    }

    /**
     * 清除内存、磁盘缓存
     */
    public void clearCache() {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR);
    }

    /**
     * 清除内存缓存
     */
    public void clearMemoryCache() {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR_MEMORY);
    }

    /**
     * 清除磁盘缓存
     */
    public void clearDiskCache() {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR_DISK);
    }

    /**
     * 清除指定URL的内存、磁盘缓存
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
     */
    public void clearCache(String uri) {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR_BY_KEY, uri);
    }

    /**
     * 清除指定URL的内存缓存
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
     */
    public void clearMemoryCache(String uri) {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR_MEMORY_BY_KEY, uri);
    }

    /**
     * 清除指定URL的磁盘缓存
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
     */
    public void clearDiskCache(String uri) {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR_DISK_BY_KEY, uri);
    }

    /**
     * 刷新磁盘缓存（强制清空文件系统缓冲区数据）
     */
    public void flushCache() {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_FLUSH);
    }

    /**
     * 关闭缓存。内存缓存将被清空，磁盘缓存的数据将保存在本地文件系统
     */
    public void closeCache() {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLOSE);
    }
    
}