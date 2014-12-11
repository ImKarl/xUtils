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

package com.lidroid.xutils.bitmap.core;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.BitmapGlobalConfig;
import com.lidroid.xutils.bitmap.factory.BitmapFactory;
import com.lidroid.xutils.cache.FileNameGenerator;
import com.lidroid.xutils.cache.LruDiskCache;
import com.lidroid.xutils.cache.LruMemoryCache;
import com.lidroid.xutils.util.IOUtils;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.util.OtherUtils;

import java.io.*;

/**
 * Bitmap位图缓存管理器（内存缓存、磁盘缓存）
 */
public class BitmapCache {

    private final int DISK_CACHE_INDEX = 0;

    private LruDiskCache mDiskLruCache;
    private LruMemoryCache<MemoryCacheKey, Bitmap> mMemoryCache;

    private final Object mDiskCacheLock = new Object();

    private BitmapGlobalConfig globalConfig;

    /**
     * 构造Bitmap位图缓存管理器
     * @param globalConfig 图片加载全局配置项{@link com.lidroid.xutils.bitmap.BitmapGlobalConfig}
     */
    public BitmapCache(BitmapGlobalConfig globalConfig) {
        if (globalConfig == null) throw new IllegalArgumentException("globalConfig may not be null");
        this.globalConfig = globalConfig;
    }


    /**
     * 初始化内存缓存
     */
    public void initMemoryCache() {
        if (!globalConfig.isMemoryCacheEnabled()) return;

        // Set up memory cache
        if (mMemoryCache != null) {
            try {
                clearMemoryCache();
            } catch (Throwable e) {
            }
        }
        mMemoryCache = new LruMemoryCache<MemoryCacheKey, Bitmap>(globalConfig.getMemoryCacheSize()) {
            /**
             * Measure item size in bytes rather than units which is more practical
             * for a bitmap cache
             */
            @Override
            protected int sizeOf(MemoryCacheKey key, Bitmap bitmap) {
                if (bitmap == null) return 0;
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };
    }

    /**
     * 初始化磁盘缓存
     * 
     * <pre>
     * 默认磁盘缓存被创建时，图片缓存不会立即初始化；当需要时，才被初始化。
     * </pre>
     * 
     * <pre>
     * 注意：这里包括磁盘访问，所以这不应该在主UI线程执行。
     * 默认情况下，磁盘缓存被创建的时候，图片缓存没有初始化；相反，你应该在后台线程调用initdiskcache()。
     * </pre>
     * 
     * <pre>
     * 原文：
     * Initializes the disk cache.  Note that this includes disk access so this should not be
     * executed on the main/UI thread. By default an ImageCache does not initialize the disk
     * cache when it is created, instead you should call initDiskCache() to initialize it on a
     * background thread.
     * </pre>
     */
    public void initDiskCache() {
        // Set up disk cache
        synchronized (mDiskCacheLock) {
            if (globalConfig.isDiskCacheEnabled() && (mDiskLruCache == null || mDiskLruCache.isClosed())) {
                File diskCacheDir = new File(globalConfig.getDiskCachePath());
                if (diskCacheDir.exists() || diskCacheDir.mkdirs()) {
                    long availableSpace = OtherUtils.getAvailableSpace(diskCacheDir);
                    long diskCacheSize = globalConfig.getDiskCacheSize();
                    diskCacheSize = availableSpace > diskCacheSize ? diskCacheSize : availableSpace;
                    try {
                        mDiskLruCache = LruDiskCache.open(diskCacheDir, 1, 1, diskCacheSize);
                        mDiskLruCache.setFileNameGenerator(globalConfig.getFileNameGenerator());
                        LogUtils.d("create disk cache success");
                    } catch (Throwable e) {
                        mDiskLruCache = null;
                        LogUtils.e("create disk cache error", e);
                    }
                }
            }
        }
    }

    /**
     * 设置最大内存缓存大小
     * @param maxSize 最大内存缓存大小
     */
    public void setMemoryCacheSize(int maxSize) {
        if (mMemoryCache != null) {
            mMemoryCache.setMaxSize(maxSize);
        }
    }

    /**
     * 设置最大磁盘缓存大小
     * @param maxSize 最大磁盘缓存大小
     */
    public void setDiskCacheSize(int maxSize) {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                mDiskLruCache.setMaxSize(maxSize);
            }
        }
    }

    /**
     * 设置文件名生成器
     * @param fileNameGenerator 文件名生成器{@link com.lidroid.xutils.cache.FileNameGenerator}
     */
    public void setDiskCacheFileNameGenerator(FileNameGenerator fileNameGenerator) {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null && fileNameGenerator != null) {
                mDiskLruCache.setFileNameGenerator(fileNameGenerator);
            }
        }
    }

    /**
     * 下载图片
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
     * @param config 图片显示配置项{@link com.lidroid.xutils.bitmap.BitmapDisplayConfig}
     * @param task 图片加载任务管理器{@link com.lidroid.xutils.BitmapUtils.BitmapLoadTask}
     * @return Bitmap位图{@link android.graphics.Bitmap}
     */
    public Bitmap downloadBitmap(String uri, BitmapDisplayConfig config, final BitmapUtils.BitmapLoadTask<?> task) {
        BitmapMeta bitmapMeta = new BitmapMeta();

        OutputStream outputStream = null;
        LruDiskCache.Snapshot snapshot = null;

        try {
            Bitmap bitmap = null;

            // try download to disk
            if (globalConfig.isDiskCacheEnabled()) {
                if (mDiskLruCache == null) {
                    initDiskCache();
                }

                if (mDiskLruCache != null) {
                    try {
                        snapshot = mDiskLruCache.get(uri);
                        if (snapshot == null) {
                            LruDiskCache.Editor editor = mDiskLruCache.edit(uri);
                            if (editor != null) {
                                outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
                                bitmapMeta.expiryTimestamp = globalConfig.getDownloader().downloadToStream(uri, outputStream, task);
                                if (bitmapMeta.expiryTimestamp < 0) {
                                    editor.abort();
                                    return null;
                                } else {
                                    editor.setEntryExpiryTimestamp(bitmapMeta.expiryTimestamp);
                                    editor.commit();
                                }
                                snapshot = mDiskLruCache.get(uri);
                            }
                        }
                        if (snapshot != null) {
                            bitmapMeta.inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                            bitmap = decodeBitmapMeta(bitmapMeta, config);
                            if (bitmap == null) {
                                bitmapMeta.inputStream = null;
                                mDiskLruCache.remove(uri);
                            }
                        }
                    } catch (Throwable e) {
                        LogUtils.e(e.getMessage(), e);
                    }
                }
            }

            // try download to memory stream
            if (bitmap == null) {
                outputStream = new ByteArrayOutputStream();
                bitmapMeta.expiryTimestamp = globalConfig.getDownloader().downloadToStream(uri, outputStream, task);
                if (bitmapMeta.expiryTimestamp < 0) {
                    return null;
                } else {
                    bitmapMeta.data = ((ByteArrayOutputStream) outputStream).toByteArray();
                    bitmap = decodeBitmapMeta(bitmapMeta, config);
                }
            }

            if (bitmap != null) {
                bitmap = rotateBitmapIfNeeded(uri, config, bitmap);
                bitmap = addBitmapToMemoryCache(uri, config, bitmap, bitmapMeta.expiryTimestamp);
            }
            return bitmap;
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(snapshot);
        }

        return null;
    }

    private Bitmap addBitmapToMemoryCache(String uri, BitmapDisplayConfig config, Bitmap bitmap, long expiryTimestamp) throws IOException {
        if (config != null) {
            BitmapFactory bitmapFactory = config.getBitmapFactory();
            if (bitmapFactory != null) {
                bitmap = bitmapFactory.cloneNew().createBitmap(bitmap);
            }
        }
        if (uri != null && bitmap != null && globalConfig.isMemoryCacheEnabled() && mMemoryCache != null) {
            MemoryCacheKey key = new MemoryCacheKey(uri, config);
            mMemoryCache.put(key, bitmap, expiryTimestamp);
        }
        return bitmap;
    }

    /**
     * 从内存缓存，获取图片
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址（将作为唯一标识符）
     * @param config 图片显示配置项{@link com.lidroid.xutils.bitmap.BitmapDisplayConfig}
     * @return Bitmap位图{@link android.graphics.Bitmap}（如果缓存不存在，则返回null）
     */
    public Bitmap getBitmapFromMemCache(String uri, BitmapDisplayConfig config) {
        if (mMemoryCache != null && globalConfig.isMemoryCacheEnabled()) {
            MemoryCacheKey key = new MemoryCacheKey(uri, config);
            return mMemoryCache.get(key);
        }
        return null;
    }

    /**
     * 从磁盘缓存，获取图片
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址（将作为唯一标识符）
     * @return 图片文件{@link java.io.File}（如果缓存不存在，则返回null）
     */
    public File getBitmapFileFromDiskCache(String uri) {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                return mDiskLruCache.getCacheFile(uri, DISK_CACHE_INDEX);
            } else {
                return null;
            }
        }
    }

    /**
     * 从磁盘缓存，获取图片
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址（将作为唯一标识符）
     * @param config 图片显示配置项{@link com.lidroid.xutils.bitmap.BitmapDisplayConfig}
     * @return Bitmap位图{@link android.graphics.Bitmap}（如果缓存不存在，则返回null）
     */
    public Bitmap getBitmapFromDiskCache(String uri, BitmapDisplayConfig config) {
        if (uri == null || !globalConfig.isDiskCacheEnabled()) return null;
        if (mDiskLruCache == null) {
            initDiskCache();
        }
        if (mDiskLruCache != null) {
            LruDiskCache.Snapshot snapshot = null;
            try {
                snapshot = mDiskLruCache.get(uri);
                if (snapshot != null) {
                    Bitmap bitmap = null;
                    if (config == null || config.isShowOriginal()) {
                        bitmap = BitmapDecoder.decodeFileDescriptor(
                                snapshot.getInputStream(DISK_CACHE_INDEX).getFD());
                    } else {
                        bitmap = BitmapDecoder.decodeSampledBitmapFromDescriptor(
                                snapshot.getInputStream(DISK_CACHE_INDEX).getFD(),
                                config.getBitmapMaxSize(),
                                config.getBitmapConfig());
                    }

                    bitmap = rotateBitmapIfNeeded(uri, config, bitmap);
                    bitmap = addBitmapToMemoryCache(uri, config, bitmap, mDiskLruCache.getExpiryTimestamp(uri));
                    return bitmap;
                }
            } catch (Throwable e) {
                LogUtils.e(e.getMessage(), e);
            } finally {
                IOUtils.closeQuietly(snapshot);
            }
        }
        return null;
    }

    /**
     * 清除内存、磁盘缓存
     * 
     * <pre>
     * 清除和这个图片缓存相关的内存、磁盘缓存；
     * 注意：这里包括磁盘访问，所以这不应该在主UI线程执行。
     * </pre>
     * 
     * <pre>
     * 原文：
     * Clears both the memory and disk cache associated with this ImageCache object.
     * Note that this includes disk access so this should not be executed on the main/UI thread.
     * </pre>
     */
    public void clearCache() {
        clearMemoryCache();
        clearDiskCache();
    }

    /**
     * 清除内存缓存
     */
    public void clearMemoryCache() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
        }
    }
    
    /**
     * 清除磁盘缓存
     * 
     * <pre>
     * 注意：这里包括磁盘访问，所以这不应该在主UI线程执行。
     * </pre>
     */
    public void clearDiskCache() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
                try {
                    mDiskLruCache.delete();
                    mDiskLruCache.close();
                } catch (Throwable e) {
                    LogUtils.e(e.getMessage(), e);
                }
                mDiskLruCache = null;
            }
        }
        initDiskCache();
    }


    /**
     * 清除指定URL的内存、磁盘缓存
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
     */
    public void clearCache(String uri) {
        clearMemoryCache(uri);
        clearDiskCache(uri);
    }

    /**
     * 清除指定URL的内存缓存
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
     */
    public void clearMemoryCache(String uri) {
        MemoryCacheKey key = new MemoryCacheKey(uri, null);
        if (mMemoryCache != null) {
            while (mMemoryCache.containsKey(key)) {
                mMemoryCache.remove(key);
            }
        }
    }

    /**
     * 清除指定URL的磁盘缓存
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
     */
    public void clearDiskCache(String uri) {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
                try {
                    mDiskLruCache.remove(uri);
                } catch (Throwable e) {
                    LogUtils.e(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 刷新磁盘缓存（强制清空文件系统缓冲区数据）
     * 
     * <pre>
     * 刷新磁盘缓存，强制清空和这个图片缓存相关的文件系统缓冲区数据；
     * 注意：这里包括磁盘访问，所以这不应该在主UI线程执行。
     * </pre>
     * 
     * <pre>
     * 原文：
     * Flushes the disk cache associated with this ImageCache object.
     * Note that this includes disk access so this should not be executed on the main/UI thread.
     * </pre>
     */
    public void flush() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    mDiskLruCache.flush();
                } catch (Throwable e) {
                    LogUtils.e(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 关闭缓存
     * 
     * <pre>
     * 关闭和这个图片缓存相关的磁盘缓存；
     * 注意：这里包括磁盘访问，所以这不应该在主UI线程执行。
     * </pre>
     * 
     * <pre>
     * 原文：
     * Closes the disk cache associated with this ImageCache object.
     * Note that this includes disk access so this should not be executed on the main/UI thread.
     * </pre>
     */
    public void close() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    if (!mDiskLruCache.isClosed()) {
                        mDiskLruCache.close();
                    }
                } catch (Throwable e) {
                    LogUtils.e(e.getMessage(), e);
                }
                mDiskLruCache = null;
            }
        }
    }

    private class BitmapMeta {
        public FileInputStream inputStream;
        public byte[] data;
        public long expiryTimestamp;
    }

    private Bitmap decodeBitmapMeta(BitmapMeta bitmapMeta, BitmapDisplayConfig config) throws IOException {
        if (bitmapMeta == null) return null;
        Bitmap bitmap = null;
        if (bitmapMeta.inputStream != null) {
            if (config == null || config.isShowOriginal()) {
                bitmap = BitmapDecoder.decodeFileDescriptor(bitmapMeta.inputStream.getFD());
            } else {
                bitmap = BitmapDecoder.decodeSampledBitmapFromDescriptor(
                        bitmapMeta.inputStream.getFD(),
                        config.getBitmapMaxSize(),
                        config.getBitmapConfig());
            }
        } else if (bitmapMeta.data != null) {
            if (config == null || config.isShowOriginal()) {
                bitmap = BitmapDecoder.decodeByteArray(bitmapMeta.data);
            } else {
                bitmap = BitmapDecoder.decodeSampledBitmapFromByteArray(
                        bitmapMeta.data,
                        config.getBitmapMaxSize(),
                        config.getBitmapConfig());
            }
        }
        return bitmap;
    }

    private synchronized Bitmap rotateBitmapIfNeeded(String uri, BitmapDisplayConfig config, Bitmap bitmap) {
        Bitmap result = bitmap;
        if (config != null && config.isAutoRotation()) {
            File bitmapFile = this.getBitmapFileFromDiskCache(uri);
            if (bitmapFile != null && bitmapFile.exists()) {
                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(bitmapFile.getPath());
                } catch (Throwable e) {
                    return result;
                }
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                int angle = 0;
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        angle = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        angle = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        angle = 270;
                        break;
                    default:
                        angle = 0;
                        break;
                }
                if (angle != 0) {
                    Matrix m = new Matrix();
                    m.postRotate(angle);
                    result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                    bitmap.recycle();
                    bitmap = null;
                }
            }
        }
        return result;
    }

    
    
    /**
     * 内存缓存键值
     */
    public class MemoryCacheKey {
        private String uri;
        private String subKey;

        private MemoryCacheKey(String uri, BitmapDisplayConfig config) {
            this.uri = uri;
            this.subKey = config == null ? null : config.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MemoryCacheKey)) return false;

            MemoryCacheKey that = (MemoryCacheKey) o;

            if (!uri.equals(that.uri)) return false;

            if (subKey != null && that.subKey != null) {
                return subKey.equals(that.subKey);
            }

            return true;
        }

        @Override
        public int hashCode() {
            return uri.hashCode();
        }
    }
}
