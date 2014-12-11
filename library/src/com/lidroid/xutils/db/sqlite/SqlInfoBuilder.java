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

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.table.*;
import com.lidroid.xutils.exception.DbException;

import java.util.*;

/**
 * SQL语句生成器
 * 
 * <pre>
 * 可生成："insert", "replace",，"update", "delete" and "create" sql.
 * <pre>
 */
public class SqlInfoBuilder {

    private SqlInfoBuilder() {
    }

    //*********************************************** insert sql ***********************************************
    /**
     * 生成INSERT语句
     * @param db DB操作工具包{@link com.lidroid.xutils.DbUtils}
     * @param entity 实体类实例
     * @return SQL语句描述（INSERT）{@link com.lidroid.xutils.db.sqlite.SqlInfo}
     * @throws DbException 数据库操作异常
     */
    public static SqlInfo buildInsertSqlInfo(DbUtils db, Object entity) throws DbException {
        List<KeyValue> keyValueList = entity2KeyValueList(db, entity);
        if (keyValueList.size() == 0) return null;

        SqlInfo result = new SqlInfo();
        StringBuffer sqlBuffer = new StringBuffer();

        sqlBuffer.append("INSERT INTO ");
        sqlBuffer.append(TableUtils.getTableName(entity.getClass()));
        sqlBuffer.append(" (");
        for (KeyValue kv : keyValueList) {
            sqlBuffer.append(kv.key).append(",");
            result.addBindArgWithoutConverter(kv.value);
        }
        sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
        sqlBuffer.append(") VALUES (");

        int length = keyValueList.size();
        for (int i = 0; i < length; i++) {
            sqlBuffer.append("?,");
        }
        sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
        sqlBuffer.append(")");

        result.setSql(sqlBuffer.toString());

        return result;
    }

    //*********************************************** replace sql ***********************************************
    /**
     * 生成REPLACE语句
     * @param db DB操作工具包{@link com.lidroid.xutils.DbUtils}
     * @param entity 实体类实例
     * @return SQL语句描述（REPLACE）{@link com.lidroid.xutils.db.sqlite.SqlInfo}
     * @throws DbException 数据库操作异常
     */
    public static SqlInfo buildReplaceSqlInfo(DbUtils db, Object entity) throws DbException {
        List<KeyValue> keyValueList = entity2KeyValueList(db, entity);
        if (keyValueList.size() == 0) return null;

        SqlInfo result = new SqlInfo();
        StringBuffer sqlBuffer = new StringBuffer();

        sqlBuffer.append("REPLACE INTO ");
        sqlBuffer.append(TableUtils.getTableName(entity.getClass()));
        sqlBuffer.append(" (");
        for (KeyValue kv : keyValueList) {
            sqlBuffer.append(kv.key).append(",");
            result.addBindArgWithoutConverter(kv.value);
        }
        sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
        sqlBuffer.append(") VALUES (");

        int length = keyValueList.size();
        for (int i = 0; i < length; i++) {
            sqlBuffer.append("?,");
        }
        sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
        sqlBuffer.append(")");

        result.setSql(sqlBuffer.toString());

        return result;
    }

    //*********************************************** delete sql ***********************************************

    private static String buildDeleteSqlByTableName(String tableName) {
        return "DELETE FROM " + tableName;
    }
    /**
     * 生成DELETE语句
     * @param db DB操作工具包{@link com.lidroid.xutils.DbUtils}
     * @param entity 实体类实例
     * @return SQL语句描述（DELETE）{@link com.lidroid.xutils.db.sqlite.SqlInfo}
     * @throws DbException 数据库操作异常
     */
    public static SqlInfo buildDeleteSqlInfo(DbUtils db, Object entity) throws DbException {
        SqlInfo result = new SqlInfo();

        Class<?> entityType = entity.getClass();
        Table table = Table.get(db, entityType);
        Id id = table.id;
        Object idValue = id.getColumnValue(entity);

        if (idValue == null) {
            throw new DbException("this entity[" + entity.getClass() + "]'s id value is null");
        }
        StringBuilder sb = new StringBuilder(buildDeleteSqlByTableName(table.tableName));
        sb.append(" WHERE ").append(WhereBuilder.b(id.getColumnName(), "=", idValue));

        result.setSql(sb.toString());

        return result;
    }
    /**
     * 生成DELETE语句
     * @param db DB操作工具包{@link com.lidroid.xutils.DbUtils}
     * @param entityType 实体类类型{@link java.lang.Class}
     * @param idValue 实体类主键ID的值（为null时，抛出异常{@link com.lidroid.xutils.exception.DbException}）
     * @return SQL语句描述（DELETE）{@link com.lidroid.xutils.db.sqlite.SqlInfo}
     * @throws DbException 数据库操作异常
     */
    public static SqlInfo buildDeleteSqlInfo(DbUtils db, Class<?> entityType, Object idValue) throws DbException {
        SqlInfo result = new SqlInfo();

        Table table = Table.get(db, entityType);
        Id id = table.id;

        if (null == idValue) {
            throw new DbException("this entity[" + entityType + "]'s id value is null");
        }
        StringBuilder sb = new StringBuilder(buildDeleteSqlByTableName(table.tableName));
        sb.append(" WHERE ").append(WhereBuilder.b(id.getColumnName(), "=", idValue));

        result.setSql(sb.toString());

        return result;
    }
    /**
     * 生成DELETE语句
     * @param db DB操作工具包{@link com.lidroid.xutils.DbUtils}
     * @param entityType 实体类类型{@link java.lang.Class}
     * @param whereBuilder WHERE条件{@link com.lidroid.xutils.db.sqlite.WhereBuilder}
     * @return SQL语句描述（DELETE）{@link com.lidroid.xutils.db.sqlite.SqlInfo}
     * @throws DbException 数据库操作异常
     */
    public static SqlInfo buildDeleteSqlInfo(DbUtils db, Class<?> entityType, WhereBuilder whereBuilder) throws DbException {
        Table table = Table.get(db, entityType);
        StringBuilder sb = new StringBuilder(buildDeleteSqlByTableName(table.tableName));

        if (whereBuilder != null && whereBuilder.getWhereItemSize() > 0) {
            sb.append(" WHERE ").append(whereBuilder.toString());
        }

        return new SqlInfo(sb.toString());
    }

    //*********************************************** update sql ***********************************************

    /**
     * 生成UPDATE语句
     * 
     * <pre>
     * updateColumnNames为空时，更新所有字段的值
     * </pre>
     * 
     * @param db DB操作工具包{@link com.lidroid.xutils.DbUtils}
     * @param entity 实体类实例
     * @param updateColumnNames 需要更新的字段名
     * @return SQL语句描述（UPDATE）{@link com.lidroid.xutils.db.sqlite.SqlInfo}
     * @throws DbException 数据库操作异常
     */
    public static SqlInfo buildUpdateSqlInfo(DbUtils db, Object entity, String... updateColumnNames) throws DbException {
        List<KeyValue> keyValueList = entity2KeyValueList(db, entity);
        if (keyValueList.size() == 0) return null;

        HashSet<String> updateColumnNameSet = null;
        if (updateColumnNames != null && updateColumnNames.length > 0) {
            updateColumnNameSet = new HashSet<String>(updateColumnNames.length);
            Collections.addAll(updateColumnNameSet, updateColumnNames);
        }

        Class<?> entityType = entity.getClass();
        Table table = Table.get(db, entityType);
        Id id = table.id;
        Object idValue = id.getColumnValue(entity);

        if (null == idValue) {
            throw new DbException("this entity[" + entity.getClass() + "]'s id value is null");
        }

        SqlInfo result = new SqlInfo();
        StringBuffer sqlBuffer = new StringBuffer("UPDATE ");
        sqlBuffer.append(table.tableName);
        sqlBuffer.append(" SET ");
        for (KeyValue kv : keyValueList) {
            if (updateColumnNameSet == null || updateColumnNameSet.contains(kv.key)) {
                sqlBuffer.append(kv.key).append("=?,");
                result.addBindArgWithoutConverter(kv.value);
            }
        }
        sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
        sqlBuffer.append(" WHERE ").append(WhereBuilder.b(id.getColumnName(), "=", idValue));

        result.setSql(sqlBuffer.toString());
        return result;
    }
    /**
     * 生成UPDATE语句
     * 
     * <pre>
     * updateColumnNames为空时，更新所有字段的值
     * </pre>
     * 
     * @param db DB操作工具包{@link com.lidroid.xutils.DbUtils}
     * @param entity 实体类实例
     * @param whereBuilder WHERE条件{@link com.lidroid.xutils.db.sqlite.WhereBuilder}
     * @param updateColumnNames 需要更新的字段名
     * @return SQL语句描述（UPDATE）{@link com.lidroid.xutils.db.sqlite.SqlInfo}
     * @throws DbException 数据库操作异常
     */
    public static SqlInfo buildUpdateSqlInfo(DbUtils db, Object entity, WhereBuilder whereBuilder, String... updateColumnNames) throws DbException {

        List<KeyValue> keyValueList = entity2KeyValueList(db, entity);
        if (keyValueList.size() == 0) return null;

        HashSet<String> updateColumnNameSet = null;
        if (updateColumnNames != null && updateColumnNames.length > 0) {
            updateColumnNameSet = new HashSet<String>(updateColumnNames.length);
            Collections.addAll(updateColumnNameSet, updateColumnNames);
        }

        Class<?> entityType = entity.getClass();
        String tableName = TableUtils.getTableName(entityType);

        SqlInfo result = new SqlInfo();
        StringBuffer sqlBuffer = new StringBuffer("UPDATE ");
        sqlBuffer.append(tableName);
        sqlBuffer.append(" SET ");
        for (KeyValue kv : keyValueList) {
            if (updateColumnNameSet == null || updateColumnNameSet.contains(kv.key)) {
                sqlBuffer.append(kv.key).append("=?,");
                result.addBindArgWithoutConverter(kv.value);
            }
        }
        sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
        if (whereBuilder != null && whereBuilder.getWhereItemSize() > 0) {
            sqlBuffer.append(" WHERE ").append(whereBuilder.toString());
        }

        result.setSql(sqlBuffer.toString());
        return result;
    }

    //*********************************************** others ***********************************************
    /**
     * 生成CREATE语句
     * 
     * @param db DB操作工具包{@link com.lidroid.xutils.DbUtils}
     * @param entityType 实体类类型{@link java.lang.Class}
     * @return SQL语句描述（CREATE）{@link com.lidroid.xutils.db.sqlite.SqlInfo}
     * @throws DbException 数据库操作异常
     */
    public static SqlInfo buildCreateTableSqlInfo(DbUtils db, Class<?> entityType) throws DbException {
        Table table = Table.get(db, entityType);
        Id id = table.id;

        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("CREATE TABLE IF NOT EXISTS ");
        sqlBuffer.append(table.tableName);
        sqlBuffer.append(" ( ");

        if (id.isAutoIncrement()) {
            sqlBuffer.append("\"").append(id.getColumnName()).append("\"  ").append("INTEGER PRIMARY KEY AUTOINCREMENT,");
        } else {
            sqlBuffer.append("\"").append(id.getColumnName()).append("\"  ").append(id.getColumnDbType()).append(" PRIMARY KEY,");
        }

        Collection<Column> columns = table.columnMap.values();
        for (Column column : columns) {
            if (column instanceof Finder) {
                continue;
            }
            sqlBuffer.append("\"").append(column.getColumnName()).append("\"  ");
            sqlBuffer.append(column.getColumnDbType());
            if (ColumnUtils.isUnique(column.getColumnField())) {
                sqlBuffer.append(" UNIQUE");
            }
            if (ColumnUtils.isNotNull(column.getColumnField())) {
                sqlBuffer.append(" NOT NULL");
            }
            String check = ColumnUtils.getCheck(column.getColumnField());
            if (check != null) {
                sqlBuffer.append(" CHECK(").append(check).append(")");
            }
            sqlBuffer.append(",");
        }

        sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
        sqlBuffer.append(" )");
        return new SqlInfo(sqlBuffer.toString());
    }

    private static KeyValue column2KeyValue(Object entity, Column column) {
        KeyValue kv = null;
        String key = column.getColumnName();
        if (key != null) {
            Object value = column.getColumnValue(entity);
            value = value == null ? column.getDefaultValue() : value;
            kv = new KeyValue(key, value);
        }
        return kv;
    }

    /**
     * 根据实体类，获取数据库列值的键值集
     * @param db DB操作工具包{@link com.lidroid.xutils.DbUtils}
     * @param entity 实体类实例
     * @return <数据库列名,值>的键值集
     */
    public static List<KeyValue> entity2KeyValueList(DbUtils db, Object entity) {
        List<KeyValue> keyValueList = new ArrayList<KeyValue>();

        Class<?> entityType = entity.getClass();
        Table table = Table.get(db, entityType);
        Id id = table.id;

        if (!id.isAutoIncrement()) {
            Object idValue = id.getColumnValue(entity);
            KeyValue kv = new KeyValue(id.getColumnName(), idValue);
            keyValueList.add(kv);
        }

        Collection<Column> columns = table.columnMap.values();
        for (Column column : columns) {
            if (column instanceof Finder) {
                continue;
            }
            KeyValue kv = column2KeyValue(entity, column);
            if (kv != null) {
                keyValueList.add(kv);
            }
        }

        return keyValueList;
    }
    
}
