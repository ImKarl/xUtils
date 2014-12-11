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

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.animation.Animation;
import com.lidroid.xutils.bitmap.core.BitmapSize;
import com.lidroid.xutils.bitmap.factory.BitmapFactory;
import com.lidroid.xutils.task.Priority;

/**
 * 图片显示配置项
 */
public class BitmapDisplayConfig {

    private BitmapSize bitmapMaxSize;
    private Animation animation;
    private Drawable loadingDrawable;
    private Drawable loadFailedDrawable;
    private boolean autoRotation = false;
    private boolean showOriginal = false;
    private Bitmap.Config bitmapConfig = Bitmap.Config.RGB_565;
    private BitmapFactory bitmapFactory;

    private Priority priority;

    /**
     * 构造图片显示配置项
     */
    public BitmapDisplayConfig() {
    }

    /**
     * 获取图片的最大尺寸
     * @return 尺寸大小{@link com.lidroid.xutils.bitmap.core.BitmapSize}
     */
    public BitmapSize getBitmapMaxSize() {
        return bitmapMaxSize == null ? BitmapSize.ZERO : bitmapMaxSize;
    }

    /**
     * 设置图片的最大尺寸
     * @param bitmapMaxSize 尺寸大小{@link com.lidroid.xutils.bitmap.core.BitmapSize}
     */
    public void setBitmapMaxSize(BitmapSize bitmapMaxSize) {
        this.bitmapMaxSize = bitmapMaxSize;
    }

    /**
     * 获取图片加载动画
     * @return 加载动画{@link android.view.animation.Animation}
     */
    public Animation getAnimation() {
        return animation;
    }

    /**
     * 设置图片加载动画
     * @param animation 加载动画{@link android.view.animation.Animation}
     */
    public void setAnimation(Animation animation) {
        this.animation = animation;
    }

    /**
     * 获取加载中显示的图片
     * @return {@link android.graphics.drawable.Drawable}
     */
    public Drawable getLoadingDrawable() {
        return loadingDrawable;
    }

    /**
     * 设置加载中显示的图片
     * @param loadingDrawable {@link android.graphics.drawable.Drawable}
     */
    public void setLoadingDrawable(Drawable loadingDrawable) {
        this.loadingDrawable = loadingDrawable;
    }

    /**
     * 获取加载失败显示的图片
     * @return {@link android.graphics.drawable.Drawable}
     */
    public Drawable getLoadFailedDrawable() {
        return loadFailedDrawable;
    }

    /**
     * 设置加载失败显示的图片
     * @param loadingDrawable {@link android.graphics.drawable.Drawable}
     */
    public void setLoadFailedDrawable(Drawable loadFailedDrawable) {
        this.loadFailedDrawable = loadFailedDrawable;
    }

    /**
     * 判断图片是否自动旋转
     * @return 图片是否自动旋转
     */
    public boolean isAutoRotation() {
        return autoRotation;
    }

    /**
     * 设置图片是否自动旋转
     * @param autoRotation 图片是否自动旋转
     */
    public void setAutoRotation(boolean autoRotation) {
        this.autoRotation = autoRotation;
    }

    /**
     * 判断是否显示原图
     * @return 是否显示原图
     */
    public boolean isShowOriginal() {
        return showOriginal;
    }

    /**
     * 设置是否显示原图
     * @param showOriginal 是否显示原图
     */
    public void setShowOriginal(boolean showOriginal) {
        this.showOriginal = showOriginal;
    }

    /**
     * 获取Bitmap参数设置
     * @return Bitmap参数设置{@link android.graphics.Bitmap.Config}
     */
    public Bitmap.Config getBitmapConfig() {
        return bitmapConfig;
    }

    /**
     * 设置Bitmap参数设置
     * @param bitmapConfig Bitmap参数设置{@link android.graphics.Bitmap.Config}
     */
    public void setBitmapConfig(Bitmap.Config bitmapConfig) {
        this.bitmapConfig = bitmapConfig;
    }

    /**
     * 获取Bitmap图片处理工厂
     * @return 图片处理工厂{@link com.lidroid.xutils.bitmap.factory.BitmapFactory}
     */
    public BitmapFactory getBitmapFactory() {
        return bitmapFactory;
    }

    /**
     * 设置Bitmap图片处理工厂
     * @param bitmapFactory 图片处理工厂{@link com.lidroid.xutils.bitmap.factory.BitmapFactory}
     */
    public void setBitmapFactory(BitmapFactory bitmapFactory) {
        this.bitmapFactory = bitmapFactory;
    }

    /**
     * 获取线程优先级
     * @return 线程优先级{@link com.lidroid.xutils.task.Priority}
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * 设置线程优先级
     * @param priority 线程优先级{@link com.lidroid.xutils.task.Priority}
     */
    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return (isShowOriginal() ? "" : bitmapMaxSize.toString()) +
                (bitmapFactory == null ? "" : bitmapFactory.getClass().getName());
    }

    /**
     * 克隆
     * @return 图片显示配置项{@link com.lidroid.xutils.bitmap.BitmapDisplayConfig}
     */
    public BitmapDisplayConfig cloneNew() {
        BitmapDisplayConfig config = new BitmapDisplayConfig();
        config.bitmapMaxSize = this.bitmapMaxSize;
        config.animation = this.animation;
        config.loadingDrawable = this.loadingDrawable;
        config.loadFailedDrawable = this.loadFailedDrawable;
        config.autoRotation = this.autoRotation;
        config.showOriginal = this.showOriginal;
        config.bitmapConfig = this.bitmapConfig;
        config.bitmapFactory = this.bitmapFactory;
        config.priority = this.priority;
        return config;
    }
}
