package com.lidroid.xutils.bitmap.factory;

import android.graphics.Bitmap;

/**
 * Bitmap图片处理工厂
 * 
 * <pre>
 * Created with IntelliJ IDEA.
 * User: wyouflf
 * Date: 14-05-20
 * Time: 下午4:26
 * </pre>
 * 
 * @author wyouflf
 */
public interface BitmapFactory {
    
    /**
     * 克隆一个新的实例
     * @return 新的实例
     */
    BitmapFactory cloneNew();

    /**
     * 创建{@link android.graphics.Bitmap}图片
     * @param rawBitmap 图片{@link android.graphics.Bitmap}
     * @return 图片{@link android.graphics.Bitmap}
     */
    Bitmap createBitmap(Bitmap rawBitmap);
    
}
