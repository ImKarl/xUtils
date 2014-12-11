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

import android.database.Cursor;
import com.lidroid.xutils.db.converter.ColumnConverter;
import com.lidroid.xutils.db.converter.ColumnConverterFactory;
import com.lidroid.xutils.db.sqlite.ColumnDbType;
import com.lidroid.xutils.util.LogUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 数据库表中列的描述
 */
public class Column {

    private Table table;

    private int index = -1;

    protected final String columnName;
    private final Object defaultValue;

    protected final Method getMethod;
    protected final Method setMethod;

    protected final Field columnField;
    @SuppressWarnings("rawtypes")
    protected final ColumnConverter columnConverter;

    /* package */ Column(Class<?> entityType, Field field) {
        this.columnField = field;
        this.columnConverter = ColumnConverterFactory.getColumnConverter(field.getType());
        this.columnName = ColumnUtils.getColumnNameByField(field);
        if (this.columnConverter != null) {
            this.defaultValue = this.columnConverter.getFieldValue(ColumnUtils.getColumnDefaultValue(field));
        } else {
            this.defaultValue = null;
        }
        this.getMethod = ColumnUtils.getColumnGetMethod(entityType, field);
        this.setMethod = ColumnUtils.getColumnSetMethod(entityType, field);
    }

    /**
     * 设置该数据库列对应的实体类属性的值，从数据库查询游标{@link android.database.Cursor}获取数据
     * @param entity 实体类实例
     * @param cursor 游标{@link android.database.Cursor}
     * @param index 索引
     */
    public void setValue2Entity(Object entity, Cursor cursor, int index) {
        this.index = index;
        Object value = columnConverter.getFieldValue(cursor, index);
        if (value == null && defaultValue == null) return;

        if (setMethod != null) {
            try {
                setMethod.invoke(entity, value == null ? defaultValue : value);
            } catch (Throwable e) {
                LogUtils.e(e.getMessage(), e);
            }
        } else {
            try {
                this.columnField.setAccessible(true);
                this.columnField.set(entity, value == null ? defaultValue : value);
            } catch (Throwable e) {
                LogUtils.e(e.getMessage(), e);
            }
        }
    }

    /**
     * 获取该数据库列的值，从实体类示例中获取数据
     * @param entity 实体类实例
     * @return 数据库中列的值
     */
    @SuppressWarnings("unchecked")
    public Object getColumnValue(Object entity) {
        Object fieldValue = getFieldValue(entity);
        return columnConverter.fieldValue2ColumnValue(fieldValue);
    }

    /**
     * 获取该数据库列对应的实体类属性的值，从实体类实例中获取数据
     * @param entity 实体类实例
     * @return 数据库中列的值
     */
    public Object getFieldValue(Object entity) {
        Object fieldValue = null;
        if (entity != null) {
            if (getMethod != null) {
                try {
                    fieldValue = getMethod.invoke(entity);
                } catch (Throwable e) {
                    LogUtils.e(e.getMessage(), e);
                }
            } else {
                try {
                    this.columnField.setAccessible(true);
                    fieldValue = this.columnField.get(entity);
                } catch (Throwable e) {
                    LogUtils.e(e.getMessage(), e);
                }
            }
        }
        return fieldValue;
    }

    /**
     * 获取所属数据库表的描述
     * @return 数据库表的描述{@link com.lidroid.xutils.db.table.Table}
     */
    public Table getTable() {
        return table;
    }

    /**
     * 设置所属数据库表的描述
     * @param table 数据库表的描述{@link com.lidroid.xutils.db.table.Table}
     */
    /* package */ void setTable(Table table) {
        this.table = table;
    }

    /**
     * 获取数据库查询游标{@link android.database.Cursor}中列的索引
     * 
     * <pre>
     * 这个值是通过方法{@link #setValue2Entity(Object, Cursor, int)}设置的
     * </pre>
     * 
     * @return 列的索引（未设置时，返回：-1）
     */
    public int getIndex() {
        return index;
    }

    /**
     * 获取数据库的列名
     * @return 列名
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * 获取数据库列的默认值
     * @return 默认值
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * 获取数据库列对应的实体类属性
     * @return 实体类属性{@link java.lang.reflect.Field}
     */
    public Field getColumnField() {
        return columnField;
    }

    /**
     * 获取数据库列的数据转换器
     * @return 数据转换器{@link com.lidroid.xutils.db.converter.ColumnConverter}
     */
    @SuppressWarnings("rawtypes")
    public ColumnConverter getColumnConverter() {
        return columnConverter;
    }

    /**
     * 获取数据库中列的类型
     * @return 列的类型{@link com.lidroid.xutils.db.sqlite.ColumnDbType}
     */
    public ColumnDbType getColumnDbType() {
        return columnConverter.getColumnDbType();
    }
    
}
