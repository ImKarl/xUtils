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

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.lidroid.xutils.bitmap.core.BitmapSize;

import java.lang.reflect.Field;

/**
 * Bitmap图片处理通用工具类
 */
public class BitmapCommonUtils {

    private BitmapCommonUtils() {
    }

    private static BitmapSize screenSize = null;

    /**
     * 获取设备屏幕尺寸（单位：px）
     * @param context android.content.Context
     * @return 尺寸 {@link com.lidroid.xutils.bitmap.core.BitmapSize}
     */
    public static BitmapSize getScreenSize(Context context) {
        if (screenSize == null) {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            screenSize = new BitmapSize(displayMetrics.widthPixels, displayMetrics.heightPixels);
        }
        return screenSize;
    }

    /**
     * 根据控件{@link android.view.View}，获取最优的图片尺寸
     * @param view 控件{@link android.view.View}
     * @param maxImageWidth 图片最大宽度
     * @param maxImageHeight 图片最大高度
     * @return 最合适的图片尺寸{@link com.lidroid.xutils.bitmap.core.BitmapSize}
     */
    public static BitmapSize optimizeMaxSizeByView(View view, int maxImageWidth, int maxImageHeight) {
        int width = maxImageWidth;
        int height = maxImageHeight;

        if (width > 0 && height > 0) {
            return new BitmapSize(width, height);
        }

        final ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params != null) {
            if (params.width > 0) {
                width = params.width;
            } else if (params.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
                width = view.getWidth();
            }

            if (params.height > 0) {
                height = params.height;
            } else if (params.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                height = view.getHeight();
            }
        }

        if (width <= 0) width = getImageViewFieldValue(view, "mMaxWidth");
        if (height <= 0) height = getImageViewFieldValue(view, "mMaxHeight");

        BitmapSize screenSize = getScreenSize(view.getContext());
        if (width <= 0) width = screenSize.getWidth();
        if (height <= 0) height = screenSize.getHeight();

        return new BitmapSize(width, height);
    }

    private static int getImageViewFieldValue(Object object, String fieldName) {
        int value = 0;
        if (object instanceof ImageView) {
            try {
                Field field = ImageView.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                int fieldValue = (Integer) field.get(object);
                if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                    value = fieldValue;
                }
            } catch (Throwable e) {
            }
        }
        return value;
    }
}
