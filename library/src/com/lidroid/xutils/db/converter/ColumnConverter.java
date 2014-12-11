package com.lidroid.xutils.db.converter;

import android.database.Cursor;
import com.lidroid.xutils.db.sqlite.ColumnDbType;

/**
 * 数据库列的数据转换器
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-11-4
 * Time: 下午8:57
 * </pre>
 * 
 * @author wyouflf
 */
public interface ColumnConverter<T> {

    /**
     * 从数据库查询游标{@link android.database.Cursor}，获取字段的值
     * @param cursor 游标{@link android.database.Cursor}
     * @param index 索引
     * @return 字段的值
     */
    T getFieldValue(final Cursor cursor, int index);

    /**
     * 从字段的字符串值，获取字段的值
     * @param fieldStringValue 字符串
     * @return 字段的值
     */
    T getFieldValue(String fieldStringValue);

    /**
     * 将字段的值，转换为数据库中列的值
     * @param fieldValue 字段的值
     * @return 数据库中列的值
     */
    Object fieldValue2ColumnValue(T fieldValue);

    /**
     * 数据库中列的类型
     * @return 列的类型{@link com.lidroid.xutils.db.sqlite.ColumnDbType}
     */
    ColumnDbType getColumnDbType();
    
}
