package com.lidroid.xutils.bitmap;

/**
 * 图片缓存生命周期监听器
 * 
 * <pre>
 * Created with IntelliJ IDEA.
 * User: wyouflf
 * Date: 13-10-16
 * Time: 下午4:26
 * </pre>
 * 
 * @author wyouflf
 */
public interface BitmapCacheListener {
    /**
     * 内存缓存初始化完成
     */
    void onInitMemoryCacheFinished();
    /**
     * 磁盘缓存初始化完成
     */
    void onInitDiskFinished();
    
    
    /**
     * 清空缓存（内存、磁盘）完成
     */
    void onClearCacheFinished();
    /**
     * 清空内存缓存完成
     */
    void onClearMemoryCacheFinished();
    /**
     * 清空磁盘缓存完成
     */
    void onClearDiskCacheFinished();
    
    
    /**
     * 清除指定URL的内存、磁盘缓存完成
     */
    void onClearCacheFinished(String uri);
    /**
     * 清除指定URL的内存缓存完成
     */
    void onClearMemoryCacheFinished(String uri);
    /**
     * 清除指定URL的磁盘缓存完成
     */
    void onClearDiskCacheFinished(String uri);

    
    /**
     * 清空文件系统缓冲区数据完成
     */
    void onFlushCacheFinished();
    
    
    /**
     * 缓存关闭
     */
    void onCloseCacheFinished();
    
}
