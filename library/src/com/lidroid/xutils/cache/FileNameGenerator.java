package com.lidroid.xutils.cache;

/**
 * 文件名生成器
 * 
 * <pre>
 * Author: wyouflf
 * Date: 14-5-16
 * Time: 上午11:25
 * </pre>
 * 
 * @author wyouflf
 */
public interface FileNameGenerator {
    
    /**
     * 根据KEY键值，生成文件名
     * @param key KEY键值
     * @return 文件名（不包含后缀）
     */
    public String generate(String key);
    
}
