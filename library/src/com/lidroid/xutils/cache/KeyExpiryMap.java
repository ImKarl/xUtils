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

package com.lidroid.xutils.cache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 过期时间键值对集合
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-8-1
 * Time: 上午11:25
 * </pre>
 * 
 * @author wyouflf
 */
public class KeyExpiryMap<K, V> extends ConcurrentHashMap<K, Long> {
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_CONCURRENCY_LEVEL = 16;

    /**
     * 构造过期时间键值对集合
     * @param initialCapacity 初始容量
     * @param loadFactor 加载因子
     * @param concurrencyLevel 并发级别
     */
    public KeyExpiryMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        super(initialCapacity, loadFactor, concurrencyLevel);
    }
    /**
     * 构造过期时间键值对集合
     * @param initialCapacity
     * @param loadFactor
     */
    public KeyExpiryMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor, DEFAULT_CONCURRENCY_LEVEL);
    }
    /**
     * 构造过期时间键值对集合
     * @param initialCapacity
     */
    public KeyExpiryMap(int initialCapacity) {
        super(initialCapacity);
    }
    /**
     * 构造过期时间键值对集合
     */
    public KeyExpiryMap() {
        super();
    }

    
    /**
     * 获取键值对应的过期时间
     * @param key 键值
     * @return 过期时间
     */
    @Override
    public synchronized Long get(Object key) {
        if (this.containsKey(key)) {
            return super.get(key);
        } else {
            return null;
        }
    }

    /**
     * 添加过期时间键值对
     * @param key 键值
     * @param expiryTimestamp 过期时间
     * @return 过期时间
     */
    @Override
    public synchronized Long put(K key, Long expiryTimestamp) {
        if (this.containsKey(key)) {
            this.remove(key);
        }
        return super.put(key, expiryTimestamp);
    }

    /**
     * 根据键值判断是否存在该记录
     * @param key 键值
     * @return 是否存在该记录
     */
    @Override
    public synchronized boolean containsKey(Object key) {
        boolean result = false;
        Long expiryTimestamp = super.get(key);
        if (expiryTimestamp != null && System.currentTimeMillis() < expiryTimestamp) {
            result = true;
        } else {
            this.remove(key);
        }
        return result;
    }

    /**
     * 根据键值判断删除记录
     * @param key 键值
     * @return 该键值对应的过期时间
     */
    @Override
    public synchronized Long remove(Object key) {
        return super.remove(key);
    }

    /**
     * 清空记录
     */
    @Override
    public synchronized void clear() {
        super.clear();
    }
    
}
