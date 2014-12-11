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

package com.lidroid.xutils.db.table;

import android.text.TextUtils;

import java.util.Date;
import java.util.HashMap;

/**
 * DB数据模型（K-V：列名-值）
 * 
 * <pre>
 * 以key-value形式存取；
 * 统一key、value为String类型
 * </pre>
 */
public class DbModel {

    /**
     * key: columnName
     * value: valueStr
     */
    private HashMap<String, String> dataMap = new HashMap<String, String>();

    /**
     * 根据列名，获取对应的数据库值
     * @param columnName 数据库列名
     * @return 数据库存储的值
     */
    public String getString(String columnName) {
        return dataMap.get(columnName);
    }

    /**
     * 根据列名，获取对应的数据库值
     * @param columnName 数据库列名
     * @return 数据库存储的值
     */
    public int getInt(String columnName) {
        return Integer.valueOf(dataMap.get(columnName));
    }

    /**
     * 根据列名，获取对应的数据库值
     * @param columnName 数据库列名
     * @return 数据库存储的值
     */
    public boolean getBoolean(String columnName) {
        String value = dataMap.get(columnName);
        if (value != null) {
            return value.length() == 1 ? "1".equals(value) : Boolean.valueOf(value);
        }
        return false;
    }

    /**
     * 根据列名，获取对应的数据库值
     * @param columnName 数据库列名
     * @return 数据库存储的值
     */
    public double getDouble(String columnName) {
        return Double.valueOf(dataMap.get(columnName));
    }

    /**
     * 根据列名，获取对应的数据库值
     * @param columnName 数据库列名
     * @return 数据库存储的值
     */
    public float getFloat(String columnName) {
        return Float.valueOf(dataMap.get(columnName));
    }

    /**
     * 根据列名，获取对应的数据库值
     * @param columnName 数据库列名
     * @return 数据库存储的值
     */
    public long getLong(String columnName) {
        return Long.valueOf(dataMap.get(columnName));
    }

    /**
     * 根据列名，获取对应的数据库值
     * @param columnName 数据库列名
     * @return 数据库存储的值
     */
    public Date getDate(String columnName) {
        long date = Long.valueOf(dataMap.get(columnName));
        return new Date(date);
    }

    /**
     * 根据列名，获取对应的数据库值
     * @param columnName 数据库列名
     * @return 数据库存储的值
     */
    public java.sql.Date getSqlDate(String columnName) {
        long date = Long.valueOf(dataMap.get(columnName));
        return new java.sql.Date(date);
    }

    /**
     * 添加列的数据
     * @param columnName 数据库列名
     * @param valueStr 数据库存储的值
     */
    public void add(String columnName, String valueStr) {
        dataMap.put(columnName, valueStr);
    }

    /**
     * 获取数据库数据集
     * @return 数据库列名-数据的集合（key：列名，value：存储的值）
     */
    public HashMap<String, String> getDataMap() {
        return dataMap;
    }

    /**
     * 根据列名，查找存在数据且不为空
     * @param columnName 列名
     * @return 值为空:true，否则:false
     */
    public boolean isEmpty(String columnName) {
        return TextUtils.isEmpty(dataMap.get(columnName));
    }
    
}
