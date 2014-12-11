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

package com.lidroid.xutils.db.sqlite;

import com.lidroid.xutils.db.table.ColumnUtils;
import com.lidroid.xutils.db.table.Foreign;
import com.lidroid.xutils.db.table.Table;
import com.lidroid.xutils.exception.DbException;

import java.util.List;

/**
 * 数据库表的从表关联数据集懒加载
 * 
 * <pre>
 * 从表关联的主表数据，并不会立即加载；
 * 调用getFirstFromDb()或getAllFromDb()方法，会立即执行数据库查询，此时才真正获取到数据
 * </pre>
 * 
 * @param <T> 所属主表的实体类类型
 */
public class ForeignLazyLoader<T> {
    private final Foreign foreignColumn;
    private Object columnValue;

    /**
     * 构造从表关联数据集懒加载
     * @param foreignColumn 从表关联列的描述 {@link com.lidroid.xutils.db.table.Foreign}
     * @param value 关联列的值（一般为主表ID值）
     */
    public ForeignLazyLoader(Foreign foreignColumn, Object value) {
        this.foreignColumn = foreignColumn;
        this.columnValue = ColumnUtils.convert2DbColumnValueIfNeeded(value);
    }

    /**
     * 获取关联的所有主表数据
     * 
     * <pre>
     * 会立即执行数据库查询，此时真正获取到数据
     * </pre>
     * 
     * @return 主表数据集{@link java.util.List}
     * @throws DbException 数据库操作异常
     */
    public List<T> getAllFromDb() throws DbException {
        List<T> entities = null;
        Table table = foreignColumn.getTable();
        if (table != null) {
            entities = table.db.findAll(
                    Selector.from(foreignColumn.getForeignEntityType()).
                            where(foreignColumn.getForeignColumnName(), "=", columnValue)
            );
        }
        return entities;
    }

    /**
     * 获取关联的一条主表数据
     * 
     * <pre>
     * 会立即执行数据库查询，此时真正获取到数据
     * </pre>
     * 
     * @return 一条主表数据
     * @throws DbException 数据库操作异常
     */
    public T getFirstFromDb() throws DbException {
        T entity = null;
        Table table = foreignColumn.getTable();
        if (table != null) {
            entity = table.db.findFirst(
                    Selector.from(foreignColumn.getForeignEntityType()).
                            where(foreignColumn.getForeignColumnName(), "=", columnValue)
            );
        }
        return entity;
    }

    /**
     * 设置数据库关联列的值
     * @param value 关联列的值（一般为主表ID值）
     */
    public void setColumnValue(Object value) {
        this.columnValue = ColumnUtils.convert2DbColumnValueIfNeeded(value);
    }

    /**
     * 获取数据库关联列的值
     * @return 关联列的值（一般为主表ID值）
     */
    public Object getColumnValue() {
        return columnValue;
    }
    
}
