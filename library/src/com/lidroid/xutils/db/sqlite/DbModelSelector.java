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

import android.text.TextUtils;

/**
 * DB数据模型SQL查询条件描述
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-8-10
 * Time: 下午2:15
 * </pre>
 * 
 * @author wyouflf
 */
public class DbModelSelector {

    private String[] columnExpressions;
    private String groupByColumnName;
    private WhereBuilder having;

    private Selector selector;

    private DbModelSelector(Class<?> entityType) {
        selector = Selector.from(entityType);
    }

    protected DbModelSelector(Selector selector, String groupByColumnName) {
        this.selector = selector;
        this.groupByColumnName = groupByColumnName;
    }

    protected DbModelSelector(Selector selector, String[] columnExpressions) {
        this.selector = selector;
        this.columnExpressions = columnExpressions;
    }

    /**
     * 根据实体类类型，创建对应SQL查询条件
     * @param entityType 实体类类型{@link java.lang.Class}
     * @return SQL查询条件{@link com.lidroid.xutils.db.sqlite.DbModelSelector}
     */
    public static DbModelSelector from(Class<?> entityType) {
        return new DbModelSelector(entityType);
    }

    /**
     * 设置WHERE条件
     * @param whereBuilder WHERE条件{@link com.lidroid.xutils.db.sqlite.WhereBuilder}
     * @return 当前实例
     */
    public DbModelSelector where(WhereBuilder whereBuilder) {
        selector.where(whereBuilder);
        return this;
    }

    /**
     * 设置WHERE条件
     * @param columnName 列名
     * @param op SQL运算符（包括：算数、比较、逻辑运算符，如: "=","<","LIKE","IN","BETWEEN"...）
     * @param value 对应的值
     * @return 当前实例
     */
    public DbModelSelector where(String columnName, String op, Object value) {
        selector.where(columnName, op, value);
        return this;
    }

    /**
     * AND方式，添加WHERE条件
     * @param columnName 列名
     * @param op SQL运算符（包括：算数、比较、逻辑运算符，如: "=","<","LIKE","IN","BETWEEN"...）
     * @param value 对应的值
     * @return 当前实例
     */
    public DbModelSelector and(String columnName, String op, Object value) {
        selector.and(columnName, op, value);
        return this;
    }
    /**
     * AND方式，添加WHERE条件
     * @param where WHERE条件{@link com.lidroid.xutils.db.sqlite.WhereBuilder}
     * @return 当前实例
     */
    public DbModelSelector and(WhereBuilder where) {
        selector.and(where);
        return this;
    }

    /**
     * OR方式，添加WHERE条件
     * @param columnName 列名
     * @param op SQL运算符（包括：算数、比较、逻辑运算符，如: "=","<","LIKE","IN","BETWEEN"...）
     * @param value 对应的值
     * @return 当前实例
     */
    public DbModelSelector or(String columnName, String op, Object value) {
        selector.or(columnName, op, value);
        return this;
    }
    /**
     * OR方式，添加WHERE条件
     * @param where WHERE条件{@link com.lidroid.xutils.db.sqlite.WhereBuilder}
     * @return 当前实例
     */
    public DbModelSelector or(WhereBuilder where) {
        selector.or(where);
        return this;
    }

    /**
     * 表达式方式，添加WHERE条件（无连接词）
     * @param expr SQL表达式（如：name='admin'）
     * @return 当前实例
     */
    public DbModelSelector expr(String expr) {
        selector.expr(expr);
        return this;
    }
    /**
     * 表达式方式，添加WHERE条件（无连接词）
     * @param columnName 列名
     * @param op SQL运算符（包括：算数、比较、逻辑运算符，如: "=","<","LIKE","IN","BETWEEN"...）
     * @param value 对应的值
     * @return 当前实例
     */
    public DbModelSelector expr(String columnName, String op, Object value) {
        selector.expr(columnName, op, value);
        return this;
    }

    /**
     * 设置GROUP分组的列
     * @param columnName 列名
     * @return 当前实例
     */
    public DbModelSelector groupBy(String columnName) {
        this.groupByColumnName = columnName;
        return this;
    }

    /**
     * 设置HAVING分组过滤条件
     * @param whereBuilder WHERE条件{@link com.lidroid.xutils.db.sqlite.WhereBuilder}
     * @return 当前实例
     */
    public DbModelSelector having(WhereBuilder whereBuilder) {
        this.having = whereBuilder;
        return this;
    }

    /**
     * 设置查询的列描述
     * @param columnExpressions 列描述表达式
     * @return 当前实例
     */
    public DbModelSelector select(String... columnExpressions) {
        this.columnExpressions = columnExpressions;
        return this;
    }

    /**
     * 设置ORDER排序条件（升序）
     * @param columnName 列名
     * @return 当前实例
     */
    public DbModelSelector orderBy(String columnName) {
        selector.orderBy(columnName);
        return this;
    }
    /**
     * 设置ORDER排序条件
     * @param columnName 列名
     * @param desc 是否降序排序（true:降序，false:升序）
     * @return 当前实例
     */
    public DbModelSelector orderBy(String columnName, boolean desc) {
        selector.orderBy(columnName, desc);
        return this;
    }

    /**
     * 设置LIMIT限量大小条件
     * @param limit 限量大小
     * @return 当前实例
     */
    public DbModelSelector limit(int limit) {
        selector.limit(limit);
        return this;
    }

    /**
     * 设置OFFSET基准点条件
     * @param offset 基准点（开始位置）
     * @return 当前实例
     */
    public DbModelSelector offset(int offset) {
        selector.offset(offset);
        return this;
    }

    /**
     * 当前查询对应的实体类类型
     * @return 实体类类型{@link java.lang.Class}
     */
    public Class<?> getEntityType() {
        return selector.getEntityType();
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("SELECT ");
        if (columnExpressions != null && columnExpressions.length > 0) {
            for (int i = 0; i < columnExpressions.length; i++) {
                result.append(columnExpressions[i]);
                result.append(",");
            }
            result.deleteCharAt(result.length() - 1);
        } else {
            if (!TextUtils.isEmpty(groupByColumnName)) {
                result.append(groupByColumnName);
            } else {
                result.append("*");
            }
        }
        result.append(" FROM ").append(selector.tableName);
        if (selector.whereBuilder != null && selector.whereBuilder.getWhereItemSize() > 0) {
            result.append(" WHERE ").append(selector.whereBuilder.toString());
        }
        if (!TextUtils.isEmpty(groupByColumnName)) {
            result.append(" GROUP BY ").append(groupByColumnName);
            if (having != null && having.getWhereItemSize() > 0) {
                result.append(" HAVING ").append(having.toString());
            }
        }
        if (selector.orderByList != null) {
            for (int i = 0; i < selector.orderByList.size(); i++) {
                result.append(" ORDER BY ").append(selector.orderByList.get(i).toString());
            }
        }
        if (selector.limit > 0) {
            result.append(" LIMIT ").append(selector.limit);
            result.append(" OFFSET ").append(selector.offset);
        }
        return result.toString();
    }
    
}
