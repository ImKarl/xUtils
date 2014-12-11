package com.lidroid.xutils.cache;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5摘要文件名生成器
 * 
 * <pre>
 * Author: wyouflf
 * Date: 14-5-16
 * Time: 上午11:25
 * </pre>
 * 
 * @author wyouflf
 */
public class MD5FileNameGenerator implements FileNameGenerator {
    
    /**
     * 构建MD5摘要文件名生成器
     */
    public MD5FileNameGenerator() {
    }

    /**
     * 根据KEY键值，生成文件名
     * @param key KEY键值
     * @return 文件名（不包含后缀）
     */
    public String generate(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
    
}
