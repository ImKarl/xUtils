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

package com.lidroid.xutils.bitmap.callback;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;

/**
 * 图片加载回调接口
 * @param <T> 控件类型{@link android.view.View}
 */
public abstract class BitmapLoadCallBack<T extends View> {

    /**
     * 图片加载前回调通知
     *
     * @param container 控件{@link android.view.View}
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
     * @param config 图片显示配置项{@link com.lidroid.xutils.bitmap.BitmapDisplayConfig}
     */
    public void onPreLoad(T container, String uri, BitmapDisplayConfig config) {
    }

    /**
     * 图片开始加载时回调通知
     *
     * @param container 控件{@link android.view.View}
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
     * @param config 图片显示配置项{@link com.lidroid.xutils.bitmap.BitmapDisplayConfig}
     */
    public void onLoadStarted(T container, String uri, BitmapDisplayConfig config) {
    }

    /**
     * 图片加载时，进度更新回调通知
     *
     * @param container 控件{@link android.view.View}
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
     * @param config 图片显示配置项{@link com.lidroid.xutils.bitmap.BitmapDisplayConfig}
     * @param total 图片总大小（byte）
     * @param current 当前下载大小（byte）
     */
    public void onLoading(T container, String uri, BitmapDisplayConfig config, long total, long current) {
    }

    /**
     * 图片加载成功时回调通知
     *
     * @param container 控件{@link android.view.View}
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
     * @param bitmap Bitmap位图{@link android.graphics.Bitmap}
     * @param config 图片显示配置项{@link com.lidroid.xutils.bitmap.BitmapDisplayConfig}
     * @param from 图片来源
     */
    public abstract void onLoadCompleted(T container, String uri, Bitmap bitmap, BitmapDisplayConfig config, BitmapLoadFrom from);

    /**
     * 图加载失败时回调通知
     * 
     * @param container 控件{@link android.view.View}
     * @param uri 本地文件完整路径，assets文件路径(assets/xxx)，或者URL地址
     * @param drawable 图片{@link android.graphics.drawable.Drawable}
     */
    public abstract void onLoadFailed(T container, String uri, Drawable drawable);

    private BitmapSetter<T> bitmapSetter;

    /**
     * 设置Bitmap位图显示器
     * @param bitmapSetter Bitmap位图显示器{@link com.lidroid.xutils.bitmap.callback.BitmapSetter}
     */
    public void setBitmapSetter(BitmapSetter<T> bitmapSetter) {
        this.bitmapSetter = bitmapSetter;
    }

    /**
     * 显示图片到控件
     * @param container 控件{@link android.view.View}
     * @param bitmap 位图{@link android.graphics.Bitmap}
     */
    public void setBitmap(T container, Bitmap bitmap) {
        if (bitmapSetter != null) {
            bitmapSetter.setBitmap(container, bitmap);
        } else if (container instanceof ImageView) {
            ((ImageView) container).setImageBitmap(bitmap);
        } else {
            container.setBackgroundDrawable(new BitmapDrawable(container.getResources(), bitmap));
        }
    }

    /**
     * 显示图片到控件
     * @param container 控件{@link android.view.View}
     * @param drawable 图片{@link android.graphics.drawable.Drawable}
     */
    public void setDrawable(T container, Drawable drawable) {
        if (bitmapSetter != null) {
            bitmapSetter.setDrawable(container, drawable);
        } else if (container instanceof ImageView) {
            ((ImageView) container).setImageDrawable(drawable);
        } else {
            container.setBackgroundDrawable(drawable);
        }
    }

    /**
     * 获取图片
     * @param container 控件{@link android.view.View}
     * @return 图片{@link android.graphics.drawable.Drawable}
     */
    public Drawable getDrawable(T container) {
        if (bitmapSetter != null) {
            return bitmapSetter.getDrawable(container);
        } else if (container instanceof ImageView) {
            return ((ImageView) container).getDrawable();
        } else {
            return container.getBackground();
        }
    }
    
}
