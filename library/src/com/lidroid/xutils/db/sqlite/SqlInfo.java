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

import java.util.LinkedList;

/**
 * SQL语句描述
 */
public class SqlInfo {

    private String sql;
    private LinkedList<Object> bindArgs;

    /**
     * 构造SQL语句描述
     */
    public SqlInfo() {
    }
    /**
     * 构造SQL语句描述
     * @param sql SQL语句
     */
    public SqlInfo(String sql) {
        this.sql = sql;
    }
    /**
     * 构造SQL语句描述
     * @param sql SQL语句
     * @param bindArgs SQL参数的值
     */
    public SqlInfo(String sql, Object... bindArgs) {
        this.sql = sql;
        addBindArgs(bindArgs);
    }

    /**
     * 获取SQL语句
     * @return SQL语句
     */
    public String getSql() {
        return sql;
    }

    /**
     * 设置SQL语句
     * @param sql SQL语句
     */
    public void setSql(String sql) {
        this.sql = sql;
    }

    /**
     * 获取SQL参数的值
     * @return SQL参数的值{@link java.util.LinkedList}（可能为null）
     */
    public LinkedList<Object> getBindArgs() {
        return bindArgs;
    }

    /**
     * 获取SQL参数的值
     * @return SQL参数的值（可能为null）
     */
    public Object[] getBindArgsAsArray() {
        if (bindArgs != null) {
            return bindArgs.toArray();
        }
        return null;
    }

    /**
     * 获取SQL参数的值
     * @return SQL参数的值（可能为null）
     */
    public String[] getBindArgsAsStrArray() {
        if (bindArgs != null) {
            String[] strings = new String[bindArgs.size()];
            for (int i = 0; i < bindArgs.size(); i++) {
                Object value = bindArgs.get(i);
                strings[i] = value == null ? null : value.toString();
            }
            return strings;
        }
        return null;
    }

    /**
     * 添加SQL参数的值（自动转换为数据库可操作的数据）
     * @param arg SQL参数的值（实体类属性值）
     */
    public void addBindArg(Object arg) {
        if (bindArgs == null) {
            bindArgs = new LinkedList<Object>();
        }

        bindArgs.add(ColumnUtils.convert2DbColumnValueIfNeeded(arg));
    }

    /**
     * 添加SQL参数的值（无需再处理）
     * @param arg SQL参数的值（数据库可操作的数据）
     */
    /* package */ void addBindArgWithoutConverter(Object arg) {
        if (bindArgs == null) {
            bindArgs = new LinkedList<Object>();
        }

        bindArgs.add(arg);
    }

    /**
     * 添加SQL参数的值（自动转换为数据库可操作的数据）
     * @param bindArgs SQL参数的值（实体类属性值）
     */
    public void addBindArgs(Object... bindArgs) {
        if (bindArgs != null) {
            for (Object arg : bindArgs) {
                addBindArg(arg);
            }
        }
    }

}
