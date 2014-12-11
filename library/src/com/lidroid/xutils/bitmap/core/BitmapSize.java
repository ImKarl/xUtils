package com.lidroid.xutils.bitmap.core;

/**
 * 尺寸大小
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-11-7
 * Time: 下午1:20
 * </pre>
 * 
 * @author wyouflf
 */
public class BitmapSize {

    public static final BitmapSize ZERO = new BitmapSize(0, 0);

    private final int width;
    private final int height;
    
    /**
     * 构造尺寸大小
     * @param width 宽度
     * @param height 高度
     */
    public BitmapSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * 缩小为原来尺寸的N分之一
     * @param sampleSize 缩放比例
     * @return 新的实例
     */
    public BitmapSize scaleDown(int sampleSize) {
        return new BitmapSize(width / sampleSize, height / sampleSize);
    }

    /**
     * 放大为原来尺寸的N倍
     * @param scale 放大比例
     * @return 新的实例
     */
    public BitmapSize scale(float scale) {
        return new BitmapSize((int) (width * scale), (int) (height * scale));
    }

    /**
     * 获取宽度
     * @return 宽度值
     */
    public int getWidth() {
        return width;
    }

    /**
     * 获取高度
     * @return 高度值
     */
    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "_" + width + "_" + height;
    }
}
