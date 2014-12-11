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
import com.lidroid.xutils.DbUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库表的描述
 */
public class Table {

	/** DB操作工具包{@link com.lidroid.xutils.DbUtils} */
    public final DbUtils db;
	/** 数据库的表名 */
    public final String tableName;
    /** 数据库的主键ID */
    public final Id id;

    /**
     * 列名-列信息存储（key：数据库列名）
     */
    public final HashMap<String, Column> columnMap;

    /**
     * 列名-主表关联信息存储（key：数据库列名）
     */
    public final HashMap<String, Finder> finderMap;

    /**
     * 数据库表描述信息存储（key：dbName#className）
     */
    private static final HashMap<String, Table> tableMap = new HashMap<String, Table>();

    private Table(DbUtils db, Class<?> entityType) {
        this.db = db;
        this.tableName = TableUtils.getTableName(entityType);
        this.id = TableUtils.getId(entityType);
        this.columnMap = TableUtils.getColumnMap(entityType);

        finderMap = new HashMap<String, Finder>();
        for (Column column : columnMap.values()) {
            column.setTable(this);
            if (column instanceof Finder) {
                finderMap.put(column.getColumnName(), (Finder) column);
            }
        }
    }

    /**
     * 根据实体类类型，获取数据库表描述信息（优先表信息缓存中获取）
     * @param db DB操作工具包{@link com.lidroid.xutils.DbUtils}
     * @param entityType 实体类类型{@link java.lang.Class}
     * @return 数据库表描述信息{@link com.lidroid.xutils.db.table.Table}
     */
    public static synchronized Table get(DbUtils db, Class<?> entityType) {
        String tableKey = db.getDaoConfig().getDbName() + "#" + entityType.getName();
        Table table = tableMap.get(tableKey);
        if (table == null) {
            table = new Table(db, entityType);
            tableMap.put(tableKey, table);
        }

        return table;
    }

    /**
     * 根据实体类类型，删除数据库表信息缓存
     * @param db DB操作工具包{@link com.lidroid.xutils.DbUtils}
     * @param entityType 实体类类型{@link java.lang.Class}
     */
    public static synchronized void remove(DbUtils db, Class<?> entityType) {
        String tableKey = db.getDaoConfig().getDbName() + "#" + entityType.getName();
        tableMap.remove(tableKey);
    }
    /**
     * 根据数据库表名，删除数据库表信息缓存
     * 
     * <pre>
     * 数据库表名，默认为实体类类名；
     * 有数据库表的注解，则以注解的表名为实际表名。
     * </pre>
     * 
     * @param db DB操作工具包{@link com.lidroid.xutils.DbUtils}
     * @param tableName 实体类表名
     */
    public static synchronized void remove(DbUtils db, String tableName) {
        if (tableMap.size() > 0) {
            String key = null;
            for (Map.Entry<String, Table> entry : tableMap.entrySet()) {
                Table table = entry.getValue();
                if (table != null && table.tableName.equals(tableName)) {
                    key = entry.getKey();
                    if (key.startsWith(db.getDaoConfig().getDbName() + "#")) {
                        break;
                    }
                }
            }
            if (TextUtils.isEmpty(key)) {
                tableMap.remove(key);
            }
        }
    }

    private boolean checkedDatabase;

    /**
     * 判断数据库是否存在该表
     * @return true:数据库已存在该表，否则不存在
     */
    public boolean isCheckedDatabase() {
        return checkedDatabase;
    }

    /**
     * 设置数据库是否存在该表
     * @param checkedDatabase 数据库是否存在该表（true:已存在，false:不存在）
     */
    public void setCheckedDatabase(boolean checkedDatabase) {
        this.checkedDatabase = checkedDatabase;
    }

}
