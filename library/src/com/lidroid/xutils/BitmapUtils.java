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

package com.lidroid.xutils;

import java.io.File;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;

import com.lidroid.xutils.bitmap.BitmapCacheListener;
import com.lidroid.xutils.bitmap.BitmapCommonUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.BitmapGlobalConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;
import com.lidroid.xutils.bitmap.core.AsyncDrawable;
import com.lidroid.xutils.bitmap.core.BitmapSize;
import com.lidroid.xutils.bitmap.download.Downloader;
import com.lidroid.xutils.cache.FileNameGenerator;
import com.lidroid.xutils.task.PriorityAsyncTask;
import com.lidroid.xutils.task.PriorityExecutor;
import com.lidroid.xutils.task.TaskHandler;

/**
 * 图片加载工具包
 */
public class BitmapUtils implements TaskHandler {

    private boolean pauseTask = false;
    private boolean cancelAllTask = false;
    private final Object pauseTaskLock = new Object();

    private Context context;
    private BitmapGlobalConfig globalConfig;
    private BitmapDisplayConfig defaultDisplayConfig;

    /////////////////////////////////////////////// create ///////////////////////////////////////////////////
    /**
     * 实例化
     * @param context android.content.Context
     */
    public BitmapUtils(Context context) {
        this(context, null);
    }
    /**
     * 实例化（配置磁盘缓存目录）
     * @param context android.content.Context
     * @param diskCachePath 本地磁盘缓存目录完整路径。如果为空，默认使用"APP缓存目录/xBitmapCache"
     */
    public BitmapUtils(Context context, String diskCachePath) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }

        this.context = context.getApplicationContext();
        globalConfig = BitmapGlobalConfig.getInstance(this.context, diskCachePath);
        defaultDisplayConfig = new BitmapDisplayConfig();
    }
    /**
     * 实例化（配置磁盘缓存目录、内存缓存大小）
     * @param context android.content.Context
     * @param diskCachePath 本地磁盘缓存目录。如果为空，默认使用"APP缓存目录/xBitmapCache"
     * @param memoryCacheSize 内存缓存大小。如果小于2M，随机使用内存大小的0.05~0.8
     */
    public BitmapUtils(Context context, String diskCachePath, int memoryCacheSize) {
        this(context, diskCachePath);
        globalConfig.setMemoryCacheSize(memoryCacheSize);
    }
    /**
     * 实例化（配置磁盘缓存目录、内存缓存大小、磁盘缓存大小）
     * @param context android.content.Context
     * @param diskCachePath 本地磁盘缓存目录。如果为空，默认使用"APP缓存目录/xBitmapCache"
     * @param memoryCacheSize 内存缓存大小。如果小于2M，随机使用内存大小的0.05~0.8
     * @param diskCacheSize 磁盘缓存大小。如果小于10M，则忽略本次设置
     */
    public BitmapUtils(Context context, String diskCachePath, int memoryCacheSize, int diskCacheSize) {
        this(context, diskCachePath);
        globalConfig.setMemoryCacheSize(memoryCacheSize);
        globalConfig.setDiskCacheSize(diskCacheSize);
    }
    /**
     * 实例化（配置磁盘缓存目录、内存缓存占比）
     * @param context android.content.Context
     * @param diskCachePath 本地磁盘缓存目录。如果为空，默认使用"APP缓存目录/xBitmapCache"
     * @param memoryCachePercent 内存缓存占用百分比。必须在0.05~0.8之间，否则抛出异常{@link java.lang.IllegalArgumentException}
     */
    public BitmapUtils(Context context, String diskCachePath, float memoryCachePercent) {
        this(context, diskCachePath);
        globalConfig.setMemCacheSizePercent(memoryCachePercent);
    }
    /**
     * 实例化（配置磁盘缓存目录、内存缓存占比、磁盘缓存大小）
     * @param context android.content.Context
     * @param diskCachePath 本地磁盘缓存目录。如果为空，默认使用"APP缓存目录/xBitmapCache"
     * @param memoryCachePercent 内存缓存占用百分比。必须在0.05~0.8之间，否则抛出异常{@link java.lang.IllegalArgumentException}
     * @param diskCacheSize 磁盘缓存大小。如果小于10M，则忽略本次设置
     */
    public BitmapUtils(Context context, String diskCachePath, float memoryCachePercent, int diskCacheSize) {
        this(context, diskCachePath);
        globalConfig.setMemCacheSizePercent(memoryCachePercent);
        globalConfig.setDiskCacheSize(diskCacheSize);
    }

    //////////////////////////////////////// config ////////////////////////////////////////////////////////////////////
    /**
     * 配置默认的加载中显示的图片
     * @param drawable android.graphics.drawable.Drawable
     * @return 当前实例
     */
    public BitmapUtils configDefaultLoadingImage(Drawable drawable) {
        defaultDisplayConfig.setLoadingDrawable(drawable);
        return this;
    }
    /**
     * 配置默认的加载中显示的图片
     * @param resId 资源文件ID
     * @return 当前实例
     */
    public BitmapUtils configDefaultLoadingImage(int resId) {
        defaultDisplayConfig.setLoadingDrawable(context.getResources().getDrawable(resId));
        return this;
    }
    /**
     * 配置默认的加载中显示的图片
     * @param bitmap android.graphics.Bitmap
     * @return 当前实例
     */
    public BitmapUtils configDefaultLoadingImage(Bitmap bitmap) {
        defaultDisplayConfig.setLoadingDrawable(new BitmapDrawable(context.getResources(), bitmap));
        return this;
    }
    
    /**
     * 配置默认的加载失败显示的图片
     * @param drawable android.graphics.drawable.Drawable
     * @return 当前实例
     */
    public BitmapUtils configDefaultLoadFailedImage(Drawable drawable) {
        defaultDisplayConfig.setLoadFailedDrawable(drawable);
        return this;
    }
    /**
     * 配置默认的加载失败显示的图片
     * @param resId 资源文件ID
     * @return 当前实例
     */
    public BitmapUtils configDefaultLoadFailedImage(int resId) {
        defaultDisplayConfig.setLoadFailedDrawable(context.getResources().getDrawable(resId));
        return this;
    }
    /**
     * 配置默认的加载失败显示的图片
     * @param bitmap android.graphics.Bitmap
     * @return 当前实例
     */
    public BitmapUtils configDefaultLoadFailedImage(Bitmap bitmap) {
        defaultDisplayConfig.setLoadFailedDrawable(new BitmapDrawable(context.getResources(), bitmap));
        return this;
    }

    /**
     * 配置默认的图片最大尺寸
     * @param maxWidth 图片最大宽度
     * @param maxHeight 图片最大高度
     * @return 当前实例
     */
    public BitmapUtils configDefaultBitmapMaxSize(int maxWidth, int maxHeight) {
        defaultDisplayConfig.setBitmapMaxSize(new BitmapSize(maxWidth, maxHeight));
        return this;
    }
    /**
     * 配置默认的图片最大尺寸
     * @param maxSize 图片最大尺寸{@link com.lidroid.xutils.bitmap.core.BitmapSize}
     * @return 当前实例
     */
    public BitmapUtils configDefaultBitmapMaxSize(BitmapSize maxSize) {
        defaultDisplayConfig.setBitmapMaxSize(maxSize);
        return this;
    }

    /**
     * 配置默认的图片加载动画
     * @param animation 图片加载动画{@link android.view.animation.Animation}
     * @return 当前实例
     */
    public BitmapUtils configDefaultImageLoadAnimation(Animation animation) {
        defaultDisplayConfig.setAnimation(animation);
        return this;
    }

    /**
     * 配置默认是否自动旋转图片
     * @param autoRotation 图片是否自动旋转
     * @return 当前实例
     */
    public BitmapUtils configDefaultAutoRotation(boolean autoRotation) {
        defaultDisplayConfig.setAutoRotation(autoRotation);
        return this;
    }

    /**
     * 配置默认的是否显示原图
     * @param showOriginal 是否显示原图
     * @return 当前实例
     */
    public BitmapUtils configDefaultShowOriginal(boolean showOriginal) {
        defaultDisplayConfig.setShowOriginal(showOriginal);
        return this;
    }

    /**
     * 配置默认的Bitmap参数设置
     * @param config Bitmap参数设置{@link android.graphics.Bitmap.Config}
     * @return 当前实例
     */
    public BitmapUtils configDefaultBitmapConfig(Bitmap.Config config) {
        defaultDisplayConfig.setBitmapConfig(config);
        return this;
    }

    /**
     * 配置默认的图片显示配置项
     * @param displayConfig 图片显示配置项{@link com.lidroid.xutils.bitmap.BitmapDisplayConfig}
     * @return 当前实例
     */
    public BitmapUtils configDefaultDisplayConfig(BitmapDisplayConfig displayConfig) {
        defaultDisplayConfig = displayConfig;
        return this;
    }

    /**
     * 配置图片下载器
     * @param downloader 图片下载器{@link com.lidroid.xutils.bitmap.download.Downloader}
     * @return 当前实例
     */
    public BitmapUtils configDownloader(Downloader downloader) {
        globalConfig.setDownloader(downloader);
        return this;
    }

    /**
     * 配置默认的缓存到期时间
     * @param defaultExpiry 缓存到期时间
     * @return 当前实例
     */
    public BitmapUtils configDefaultCacheExpiry(long defaultExpiry) {
        globalConfig.setDefaultCacheExpiry(defaultExpiry);
        return this;
    }

    /**
     * 配置默认的连接超时时长
     * @param connectTimeout 连接超时时长
     * @return 当前实例
     */
    public BitmapUtils configDefaultConnectTimeout(int connectTimeout) {
        globalConfig.setDefaultConnectTimeout(connectTimeout);
        return this;
    }

    /**
     * 配置默认的读取数据超时时长
     * @param readTimeout 读取数据超时时长
     * @return 当前实例
     */
    public BitmapUtils configDefaultReadTimeout(int readTimeout) {
        globalConfig.setDefaultReadTimeout(readTimeout);
        return this;
    }

    /**
     * 配置线程池大小
     * @param threadPoolSize 线程池大小
     * @return 当前实例
     */
    public BitmapUtils configThreadPoolSize(int threadPoolSize) {
        globalConfig.setThreadPoolSize(threadPoolSize);
        return this;
    }

    /**
     * 配置是否开启内存缓存
     * @param enabled 是否开启内存缓存
     * @return 当前实例
     */
    public BitmapUtils configMemoryCacheEnabled(boolean enabled) {
        globalConfig.setMemoryCacheEnabled(enabled);
        return this;
    }

    /**
     * 配置是否开启磁盘缓存
     * @param enabled 是否开启磁盘缓存
     * @return 当前实例
     */
    public BitmapUtils configDiskCacheEnabled(boolean enabled) {
        globalConfig.setDiskCacheEnabled(enabled);
        return this;
    }

    /**
     * 配置磁盘缓存文件名生成器
     * @param fileNameGenerator 文件名生成器{@link com.lidroid.xutils.cache.FileNameGenerator}
     * @return 当前实例
     */
    public BitmapUtils configDiskCacheFileNameGenerator(FileNameGenerator fileNameGenerator) {
        globalConfig.setFileNameGenerator(fileNameGenerator);
        return this;
    }

    /**
     * 配置图片缓存生命周期监听
     * @param listener 图片缓存生命周期监听器{@link com.lidroid.xutils.bitmap.BitmapCacheListener}
     * @return 当前实例
     */
    public BitmapUtils configBitmapCacheListener(BitmapCacheListener listener) {
        globalConfig.setBitmapCacheListener(listener);
        return this;
    }

    ////////////////////////// display ////////////////////////////////////
    /**
     * 显示图片到指定控件
     * <pre>
     * 如果是ImageView，会调用setImageBitmap(bitmap)；其他控件使用setBackgroundDrawable(drawable)
     * </pre>
     * @param container 控件{@link android.view.View}
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
     */
    public <T extends View> void display(T container, String uri) {
        display(container, uri, null, null);
    }
    /**
     * 显示图片到指定控件
     * <pre>
     * 如果是ImageView，会调用setImageBitmap(bitmap)；其他控件使用setBackgroundDrawable(drawable)
     * </pre>
     * @param container 控件{@link android.view.View}
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
     * @param displayConfig 图片显示配置项{@link com.lidroid.xutils.bitmap.BitmapDisplayConfig}
     */
    public <T extends View> void display(T container, String uri, BitmapDisplayConfig displayConfig) {
        display(container, uri, displayConfig, null);
    }
    /**
     * 显示图片到指定控件
     * <pre>
     * 如果是ImageView，会调用setImageBitmap(bitmap)；其他控件使用setBackgroundDrawable(drawable)
     * </pre>
     * @param container 控件{@link android.view.View}
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
     * @param callBack 图片加载回调接口{@link com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack}
     */
    public <T extends View> void display(T container, String uri, BitmapLoadCallBack<T> callBack) {
        display(container, uri, null, callBack);
    }
    /**
     * 显示图片到指定控件
     * <pre>
     * 如果是ImageView，会调用setImageBitmap(bitmap)；其他控件使用setBackgroundDrawable(drawable)
     * </pre>
     * @param container 控件{@link android.view.View}
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
     * @param displayConfig 图片显示配置项{@link com.lidroid.xutils.bitmap.BitmapDisplayConfig}
     * @param callBack 图片加载回调接口{@link com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack}
     */
    public <T extends View> void display(T container, String uri, BitmapDisplayConfig displayConfig, BitmapLoadCallBack<T> callBack) {
        if (container == null) {
            return;
        }

        if (callBack == null) {
            callBack = new DefaultBitmapLoadCallBack<T>();
        }

        if (displayConfig == null || displayConfig == defaultDisplayConfig) {
            displayConfig = defaultDisplayConfig.cloneNew();
        }

        // Optimize Max Size
        BitmapSize size = displayConfig.getBitmapMaxSize();
        displayConfig.setBitmapMaxSize(BitmapCommonUtils.optimizeMaxSizeByView(container, size.getWidth(), size.getHeight()));

        container.clearAnimation();

        if (TextUtils.isEmpty(uri)) {
            callBack.onLoadFailed(container, uri, displayConfig.getLoadFailedDrawable());
            return;
        }

        // start loading
        callBack.onPreLoad(container, uri, displayConfig);

        // find bitmap from mem cache.
        Bitmap bitmap = globalConfig.getBitmapCache().getBitmapFromMemCache(uri, displayConfig);

        if (bitmap != null) {
            callBack.onLoadStarted(container, uri, displayConfig);
            callBack.onLoadCompleted(
                    container,
                    uri,
                    bitmap,
                    displayConfig,
                    BitmapLoadFrom.MEMORY_CACHE);
        } else if (!bitmapLoadTaskExist(container, uri, callBack)) {

            final BitmapLoadTask<T> loadTask = new BitmapLoadTask<T>(container, uri, displayConfig, callBack);

            // get executor
            PriorityExecutor executor = globalConfig.getBitmapLoadExecutor();
            File diskCacheFile = this.getBitmapFileFromDiskCache(uri);
            boolean diskCacheExist = diskCacheFile != null && diskCacheFile.exists();
            if (diskCacheExist && executor.isBusy()) {
                executor = globalConfig.getDiskCacheExecutor();
            }
            // set loading image
            Drawable loadingDrawable = displayConfig.getLoadingDrawable();
            callBack.setDrawable(container, new AsyncDrawable<T>(loadingDrawable, loadTask));

            loadTask.setPriority(displayConfig.getPriority());
            loadTask.executeOnExecutor(executor);
        }
    }

    /////////////////////////////////////////////// cache /////////////////////////////////////////////////////////////////
    /**
     * 清空内存、磁盘缓存
     */
    public void clearCache() {
        globalConfig.clearCache();
    }
    /**
     * 清空内存缓存
     */
    public void clearMemoryCache() {
        globalConfig.clearMemoryCache();
    }
    /**
     * 清空磁盘缓存
     */
    public void clearDiskCache() {
        globalConfig.clearDiskCache();
    }
    
    /**
     * 清除指定URL的内存、磁盘缓存
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
     */
    public void clearCache(String uri) {
        globalConfig.clearCache(uri);
    }
    /**
     * 清除指定URL的内存缓存
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
     */
    public void clearMemoryCache(String uri) {
        globalConfig.clearMemoryCache(uri);
    }
    /**
     * 清除指定URL的磁盘缓存
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
     */
    public void clearDiskCache(String uri) {
        globalConfig.clearDiskCache(uri);
    }
    
    /**
     * 刷新磁盘缓存（强制清空文件系统缓冲区数据）
     */
    public void flushCache() {
        globalConfig.flushCache();
    }

    /**
     * 关闭缓存。内存缓存将被清空，磁盘缓存的数据将保存在本地文件系统
     */
    public void closeCache() {
        globalConfig.closeCache();
    }
    
    /**
     * 从磁盘缓存获取图片文件
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
     * @return 图片文件{@link java.io.File}
     */
    public File getBitmapFileFromDiskCache(String uri) {
        return globalConfig.getBitmapCache().getBitmapFileFromDiskCache(uri);
    }
    /**
     * 从内存缓存获取图片
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
     * @param config 图片显示配置项{@link com.lidroid.xutils.bitmap.BitmapDisplayConfig}
     * @return 图片对象{@link android.graphics.Bitmap}
     */
    public Bitmap getBitmapFromMemCache(String uri, BitmapDisplayConfig config) {
        if (config == null) {
            config = defaultDisplayConfig;
        }
        return globalConfig.getBitmapCache().getBitmapFromMemCache(uri, config);
    }

    ////////////////////////////////////////// tasks //////////////////////////////////////////////////////////////////////
    /**
     * 是否支持暂停任务
     * @return true
     */
    @Override
    public boolean supportPause() {
        return true;
    }
    /**
     * 是否支持恢复任务
     * @return true
     */
    @Override
    public boolean supportResume() {
        return true;
    }
    /**
     * 是否支持取消任务
     * @return true
     */
    @Override
    public boolean supportCancel() {
        return true;
    }
    
    /**
     * 暂停任务
     */
    @Override
    public void pause() {
        pauseTask = true;
        flushCache();
    }
    /**
     * 恢复任务
     */
    @Override
    public void resume() {
        pauseTask = false;
        synchronized (pauseTaskLock) {
            pauseTaskLock.notifyAll();
        }
    }
    /**
     * 取消任务
     */
    @Override
    public void cancel() {
        pauseTask = true;
        cancelAllTask = true;
        synchronized (pauseTaskLock) {
            pauseTaskLock.notifyAll();
        }
    }
    
    /**
     * 任务是否处于暂停状态
     * @return 暂停:true，否则false
     */
    @Override
    public boolean isPaused() {
        return pauseTask;
    }
    /**
     * 任务是否处于取消状态
     * @return 已取消:true，否则false
     */
    @Override
    public boolean isCancelled() {
        return cancelAllTask;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 给指定控件创建图片加载任务
     * @param container 控件{@link android.view.View}
     * @param callBack 图片加载回调接口{@link com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack}
     * @return 图片加载任务{@link com.lidroid.xutils.BitmapUtils.BitmapLoadTask}
     */
    @SuppressWarnings("unchecked")
    private static <T extends View> BitmapLoadTask<T> getBitmapTaskFromContainer(T container, BitmapLoadCallBack<T> callBack) {
        if (container != null) {
            final Drawable drawable = callBack.getDrawable(container);
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable<T> asyncDrawable = (AsyncDrawable<T>) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    private static <T extends View> boolean bitmapLoadTaskExist(T container, String uri, BitmapLoadCallBack<T> callBack) {
        final BitmapLoadTask<T> oldLoadTask = getBitmapTaskFromContainer(container, callBack);

        if (oldLoadTask != null) {
            final String oldUrl = oldLoadTask.uri;
            if (TextUtils.isEmpty(oldUrl) || !oldUrl.equals(uri)) {
                oldLoadTask.cancel(true);
            } else {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 图片加载任务管理器
     */
    public class BitmapLoadTask<T extends View> extends PriorityAsyncTask<Object, Object, Bitmap> {
        private static final int PROGRESS_LOAD_STARTED = 0;
        private static final int PROGRESS_LOADING = 1;

        private final String uri;
        private final WeakReference<T> containerReference;
        private final BitmapLoadCallBack<T> callBack;
        private final BitmapDisplayConfig displayConfig;

        private BitmapLoadFrom from = BitmapLoadFrom.DISK_CACHE;
        
        /**
         * 实例化图片加载任务管理器
         * @param container 控件{@link android.view.View}
         * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
         * @param config 图片显示配置项{@link com.lidroid.xutils.bitmap.BitmapDisplayConfig}
         * @param callBack 图片加载回调接口{@link com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack}
         */
        public BitmapLoadTask(T container, String uri, BitmapDisplayConfig config, BitmapLoadCallBack<T> callBack) {
            if (container == null || uri == null || config == null || callBack == null) {
                throw new IllegalArgumentException("args may not be null");
            }

            this.containerReference = new WeakReference<T>(container);
            this.callBack = callBack;
            this.uri = uri;
            this.displayConfig = config;
        }
        
        /**
         * 后台异步执行
         * @param params 调用时传入的参数
         * @return 图片{@link android.graphics.Bitmap}
         */
        @Override
        protected Bitmap doInBackground(Object... params) {
            synchronized (pauseTaskLock) {
                while (pauseTask && !this.isCancelled()) {
                    try {
                        pauseTaskLock.wait();
                        if (cancelAllTask) {
                            return null;
                        }
                    } catch (Throwable e) {
                    }
                }
            }

            Bitmap bitmap = null;

            // get cache from disk cache
            if (!this.isCancelled() && this.getTargetContainer() != null) {
                this.publishProgress(PROGRESS_LOAD_STARTED);
                bitmap = globalConfig.getBitmapCache().getBitmapFromDiskCache(uri, displayConfig);
            }

            // download image
            if (bitmap == null && !this.isCancelled() && this.getTargetContainer() != null) {
                bitmap = globalConfig.getBitmapCache().downloadBitmap(uri, displayConfig, this);
                from = BitmapLoadFrom.URI;
            }

            return bitmap;
        }

        /**
         * 更新进度
         * @param total 图片文件总大小（byte）
         * @param current 当前下载的大小（byte）
         */
        public void updateProgress(long total, long current) {
            this.publishProgress(PROGRESS_LOADING, total, current);
        }

        /**
         * 进度更新
         * @param values 传递过来的数据
         */
        @Override
        protected void onProgressUpdate(Object... values) {
            if (values == null || values.length == 0) return;

            final T container = this.getTargetContainer();
            if (container == null) return;

            switch ((Integer) values[0]) {
                case PROGRESS_LOAD_STARTED:
                    callBack.onLoadStarted(container, uri, displayConfig);
                    break;
                case PROGRESS_LOADING:
                    if (values.length != 3) return;
                    callBack.onLoading(container, uri, displayConfig, (Long) values[1], (Long) values[2]);
                    break;
                default:
                    break;
            }
        }
        
        /**
         * 任务执行完成后的通知回调
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            final T container = this.getTargetContainer();
            if (container != null) {
                if (bitmap != null) {
                    callBack.onLoadCompleted(
                            container,
                            this.uri,
                            bitmap,
                            displayConfig,
                            from);
                } else {
                    callBack.onLoadFailed(
                            container,
                            this.uri,
                            displayConfig.getLoadFailedDrawable());
                }
            }
        }

        /**
         * 任务取消的通知回调
         */
        @Override
        protected void onCancelled(Bitmap bitmap) {
            synchronized (pauseTaskLock) {
                pauseTaskLock.notifyAll();
            }
        }

        /**
         * 获取任务的目标控件
         * @return 控件{@link android.view.View}
         */
        public T getTargetContainer() {
            final T container = containerReference.get();
            final BitmapLoadTask<T> bitmapWorkerTask = getBitmapTaskFromContainer(container, callBack);

            if (this == bitmapWorkerTask) {
                return container;
            }

            return null;
        }
    }
}
