package com.lidroid.xutils.bitmap.callback;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Bitmap位图显示器
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-11-1
 * Time: 下午11:05
 * </pre>
 * 
 * @author wyouflf
 */
public interface BitmapSetter<T extends View> {

    /**
     * 显示图片到控件
     * @param container 控件{@link android.view.View}
     * @param bitmap 位图{@link android.graphics.Bitmap}
     */
    void setBitmap(T container, Bitmap bitmap);

    /**
     * 显示图片到控件
     * @param container 控件{@link android.view.View}
     * @param drawable 图片{@link android.graphics.drawable.Drawable}
     */
    void setDrawable(T container, Drawable drawable);

    /**
     * 获取图片
     * @param container 控件{@link android.view.View}
     * @return 图片{@link android.graphics.drawable.Drawable}
     */
    Drawable getDrawable(T container);
    
}
