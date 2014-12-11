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

import com.lidroid.xutils.db.table.TableUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL查询条件描述
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-8-9
 * Time: 下午10:19
 * </pre>
 * 
 * @author wyouflf
 */
public class Selector {

    protected Class<?> entityType;
    protected String tableName;

    protected WhereBuilder whereBuilder;
    protected List<OrderBy> orderByList;
    protected int limit = 0;
    protected int offset = 0;

    /**
     * 构造SQL查询条件
     * @param entityType 实体类类型{@link java.lang.Class}
     */
    private Selector(Class<?> entityType) {
        this.entityType = entityType;
        this.tableName = TableUtils.getTableName(entityType);
    }

    /**
     * 实例化SQL查询条件
     * @param entityType 实体类类型{@link java.lang.Class}
     * @return SQL查询条件{@link com.lidroid.xutils.db.sqlite.Selector}
     */
    public static Selector from(Class<?> entityType) {
        return new Selector(entityType);
    }

    /**
     * 设置WHERE条件
     * @param whereBuilder WHERE条件{@link com.lidroid.xutils.db.sqlite.WhereBuilder}
     * @return 当前实例
     */
    public Selector where(WhereBuilder whereBuilder) {
        this.whereBuilder = whereBuilder;
        return this;
    }
    /**
     * 设WHERE条件
     * @param columnName 列名
     * @param op SQL运算符（包括：算数、比较、逻辑运算符，如: "=","<","LIKE","IN","BETWEEN"...）
     * @param value 对应的值
     * @return 当前实例
     */
    public Selector where(String columnName, String op, Object value) {
        this.whereBuilder = WhereBuilder.b(columnName, op, value);
        return this;
    }

    /**
     * AND方式，添加WHERE条件
     * @param columnName 列名
     * @param op SQL运算符（包括：算数、比较、逻辑运算符，如: "=","<","LIKE","IN","BETWEEN"...）
     * @param value 对应的值
     * @return 当前实例
     */
    public Selector and(String columnName, String op, Object value) {
        this.whereBuilder.and(columnName, op, value);
        return this;
    }
    /**
     * AND方式，添加WHERE条件
     * @param whereBuilder WHERE条件{@link com.lidroid.xutils.db.sqlite.WhereBuilder}
     * @return 当前实例
     */
    public Selector and(WhereBuilder where) {
        this.whereBuilder.expr("AND (" + where.toString() + ")");
        return this;
    }

    /**
     * OR方式，添加WHERE条件
     * @param columnName 列名
     * @param op SQL运算符（包括：算数、比较、逻辑运算符，如: "=","<","LIKE","IN","BETWEEN"...）
     * @param value 对应的值
     * @return 当前实例
     */
    public Selector or(String columnName, String op, Object value) {
        this.whereBuilder.or(columnName, op, value);
        return this;
    }
    /**
     * OR方式，添加WHERE条件
     * @param whereBuilder WHERE条件{@link com.lidroid.xutils.db.sqlite.WhereBuilder}
     * @return 当前实例
     */
    public Selector or(WhereBuilder where) {
        this.whereBuilder.expr("OR (" + where.toString() + ")");
        return this;
    }

    /**
     * 表达式方式，添加WHERE条件（无连接词）
     * @param expr SQL表达式（如：name='admin'）
     * @return 当前实例
     */
    public Selector expr(String expr) {
        if (this.whereBuilder == null) {
            this.whereBuilder = WhereBuilder.b();
        }
        this.whereBuilder.expr(expr);
        return this;
    }
    /**
     * 表达式方式，添加WHERE条件（无连接词）
     * @param columnName 列名
     * @param op SQL运算符（包括：算数、比较、逻辑运算符，如: "=","<","LIKE","IN","BETWEEN"...）
     * @param value 对应的值
     * @return 当前实例
     */
    public Selector expr(String columnName, String op, Object value) {
        if (this.whereBuilder == null) {
            this.whereBuilder = WhereBuilder.b();
        }
        this.whereBuilder.expr(columnName, op, value);
        return this;
    }

    /**
     * 设置GROUP分组条件
     * @param columnName 列名
     * @return 创建新的实例（原有条件将丢失）{@link com.lidroid.xutils.db.sqlite.DbModelSelector}
     */
    public DbModelSelector groupBy(String columnName) {
        return new DbModelSelector(this, columnName);
    }

    /**
     * 设置要查询的列
     * @param columnExpressions 列名
     * @return 创建新的实例（原有条件将丢失）{@link com.lidroid.xutils.db.sqlite.DbModelSelector}
     */
    public DbModelSelector select(String... columnExpressions) {
        return new DbModelSelector(this, columnExpressions);
    }

    /**
     * 设置ORDER排序条件（升序）
     * @param columnName 列名
     * @return 当前实例
     */
    public Selector orderBy(String columnName) {
        if (orderByList == null) {
            orderByList = new ArrayList<OrderBy>(2);
        }
        orderByList.add(new OrderBy(columnName));
        return this;
    }
    /**
     * 设置ORDER排序条件
     * @param columnName 列名
     * @param desc 是否降序排序
     * @return 当前实例
     */
    public Selector orderBy(String columnName, boolean desc) {
        if (orderByList == null) {
            orderByList = new ArrayList<OrderBy>(2);
        }
        orderByList.add(new OrderBy(columnName, desc));
        return this;
    }

    /**
     * 设置LIMIT限量大小条件
     * @param limit 限量大小
     * @return 当前实例
     */
    public Selector limit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * 设置OFFSET基准点条件
     * @param offset 基准点（开始位置）
     * @return 当前实例
     */
    public Selector offset(int offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("SELECT ");
        result.append("*");
        result.append(" FROM ").append(tableName);
        if (whereBuilder != null && whereBuilder.getWhereItemSize() > 0) {
            result.append(" WHERE ").append(whereBuilder.toString());
        }
        if (orderByList != null) {
            for (int i = 0; i < orderByList.size(); i++) {
                result.append(" ORDER BY ").append(orderByList.get(i).toString());
            }
        }
        if (limit > 0) {
            result.append(" LIMIT ").append(limit);
            result.append(" OFFSET ").append(offset);
        }
        return result.toString();
    }

    /**
     * 当前查询对应的实体类类型
     * @return 实体类类型{@link java.lang.Class}
     */
    public Class<?> getEntityType() {
        return entityType;
    }

    protected class OrderBy {
        private String columnName;
        private boolean desc;

        public OrderBy(String columnName) {
            this.columnName = columnName;
        }

        public OrderBy(String columnName, boolean desc) {
            this.columnName = columnName;
            this.desc = desc;
        }

        @Override
        public String toString() {
            return columnName + (desc ? " DESC" : " ASC");
        }
    }
}
