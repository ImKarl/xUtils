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

package com.lidroid.xutils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.lidroid.xutils.db.sqlite.*;
import com.lidroid.xutils.db.table.DbModel;
import com.lidroid.xutils.db.table.Id;
import com.lidroid.xutils.db.table.Table;
import com.lidroid.xutils.db.table.TableUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.util.IOUtils;
import com.lidroid.xutils.util.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * DB(SQLite)操作工具包
 */
public class DbUtils {

    //*************************************** create instance ****************************************************

    /**
     * key: dbName
     */
    private static HashMap<String, DbUtils> daoMap = new HashMap<String, DbUtils>();

    private SQLiteDatabase database;
    private DaoConfig daoConfig;
    private boolean debug = false;
    private boolean allowTransaction = false;

    private DbUtils(DaoConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("daoConfig may not be null");
        }
        this.database = createDatabase(config);
        this.daoConfig = config;
    }

    private synchronized static DbUtils getInstance(DaoConfig daoConfig) {
        DbUtils dao = daoMap.get(daoConfig.getDbName());
        if (dao == null) {
            dao = new DbUtils(daoConfig);
            daoMap.put(daoConfig.getDbName(), dao);
        } else {
            dao.daoConfig = daoConfig;
        }

        // update the database if needed
        SQLiteDatabase database = dao.database;
        int oldVersion = database.getVersion();
        int newVersion = daoConfig.getDbVersion();
        if (oldVersion != newVersion) {
            if (oldVersion != 0) {
                DbUpgradeListener upgradeListener = daoConfig.getDbUpgradeListener();
                if (upgradeListener != null) {
                    upgradeListener.onUpgrade(dao, oldVersion, newVersion);
                } else {
                    try {
                        dao.dropDb();
                    } catch (DbException e) {
                        LogUtils.e(e.getMessage(), e);
                    }
                }
            }
            database.setVersion(newVersion);
        }

        return dao;
    }

    /**
     * 创建DbUtils单例
     * 
     * <pre>
     * 默认：
     * dbName="xUtils.db",
     * dbVersion=1,
     * DB文件保存路径=“APP内部缓存目录”（/data/data/youpackage/cache）
     * </pre>
     * 
     * @param context android.content.Context
     * @return DbUtils实例{@link com.lidroid.xutils.DbUtils}
     */
    public static DbUtils create(Context context) {
        DaoConfig config = new DaoConfig(context);
        return getInstance(config);
    }
    /**
     * 创建DbUtils单例（根据dbName的不同，创建多个实例）
     * 
     * <pre>
     * 默认：
     * dbVersion=1,
     * DB文件保存路径=“APP内部缓存目录”（/data/data/youpackage/cache）
     * </pre>
     * 
     * @param context android.content.Context
     * @param dbName 数据库文件名
     * @return DbUtils实例{@link com.lidroid.xutils.DbUtils}
     */
    public static DbUtils create(Context context, String dbName) {
        DaoConfig config = new DaoConfig(context);
        config.setDbName(dbName);
        return getInstance(config);
    }
    /**
     * 创建DbUtils单例（根据dbName的不同，创建多个实例）
     * 
     * <pre>
     * 默认：
     * dbVersion=1
     * </pre>
     * 
     * @param context android.content.Context
     * @param dbDir 数据库文件存储路径
     * @param dbName 数据库文件名
     * @return DbUtils实例{@link com.lidroid.xutils.DbUtils}
     */
    public static DbUtils create(Context context, String dbDir, String dbName) {
        DaoConfig config = new DaoConfig(context);
        config.setDbDir(dbDir);
        config.setDbName(dbName);
        return getInstance(config);
    }
    /**
     * 创建DbUtils单例（根据dbName的不同，创建多个实例）
     * 
     * <pre>
     * 默认：
     * DB文件保存路径=“APP内部缓存目录”（/data/data/youpackage/cache）
     * </pre>
     * 
     * @param context android.content.Context
     * @param dbName 数据库文件名
     * @param dbVersion 数据库版本号
     * @param dbUpgradeListener 数据库版本升级通知接口
     * @return DbUtils实例{@link com.lidroid.xutils.DbUtils}
     */
    public static DbUtils create(Context context, String dbName, int dbVersion, DbUpgradeListener dbUpgradeListener) {
        DaoConfig config = new DaoConfig(context);
        config.setDbName(dbName);
        config.setDbVersion(dbVersion);
        config.setDbUpgradeListener(dbUpgradeListener);
        return getInstance(config);
    }
    /**
     * 创建DbUtils单例（根据dbName的不同，创建多个实例）
     * 
     * @param context android.content.Context
     * @param dbDir 数据库文件存储路径
     * @param dbName 数据库文件名
     * @param dbVersion 数据库版本号
     * @param dbUpgradeListener 数据库版本升级通知接口
     * @return DbUtils实例{@link com.lidroid.xutils.DbUtils}
     */
    public static DbUtils create(Context context, String dbDir, String dbName, int dbVersion, DbUpgradeListener dbUpgradeListener) {
        DaoConfig config = new DaoConfig(context);
        config.setDbDir(dbDir);
        config.setDbName(dbName);
        config.setDbVersion(dbVersion);
        config.setDbUpgradeListener(dbUpgradeListener);
        return getInstance(config);
    }
    /**
     * 创建DbUtils单例（根据dbName的不同，创建多个实例）
     * 
     * @param daoConfig DB操作的配置项{@link com.lidroid.xutils.DbUtils.DaoConfig}
     * @return DbUtils实例{@link com.lidroid.xutils.DbUtils}
     */
    public static DbUtils create(DaoConfig daoConfig) {
        return getInstance(daoConfig);
    }
    
    /**
     * 配置是否调试模式（打印SQL）
     * @param debug 是否调试模式（通过LogUtils.d(sql)打印执行的SQL语句）
     * @return 当前实例
     */
    public DbUtils configDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    /**
     * 配置是否允许开启事务
     * @param allowTransaction 是否允许开启事务（不允许则使用程序锁）
     * @return 当前实例
     */
    public DbUtils configAllowTransaction(boolean allowTransaction) {
        this.allowTransaction = allowTransaction;
        return this;
    }
    
    /**
     * 获取当前的数据库DB
     * @return {@link android.database.sqlite.SQLiteDatabase}
     */
    public SQLiteDatabase getDatabase() {
        return database;
    }

    /**
     * 获取当前的DB操作配置项
     * @return DB操作的配置项{@link com.lidroid.xutils.DbUtils.DaoConfig}
     */
    public DaoConfig getDaoConfig() {
        return daoConfig;
    }

    //*********************************************** operations ********************************************************
    /**
     * 保存或更新实体到DB（replace or insert）
     * 
     * <pre>
     * 根据实体类注解，自动创建表；
     * 根据ID判断replace还是insert；
     * 只持久化基本数据类型、java.lang.*等，不能处理的类型自动忽略；
     * 对静态属性、添加忽略注解属性自动忽略。
     * </pre>
     * 
     * @param entity 实体类实例
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     * @see com.lidroid.xutils.DbUtils#saveOrUpdateAll(List)
     * @see com.lidroid.xutils.DbUtils#replace(Object)
     */
    public void saveOrUpdate(Object entity) throws DbException {
        try {
            beginTransaction();

            createTableIfNotExist(entity.getClass());
            saveOrUpdateWithoutTransaction(entity);

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }
    /**
     * 保存或更新实体到DB（replace or insert）
     * 
     * @param entities 实体类实例集合
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     * @see com.lidroid.xutils.DbUtils#saveOrUpdate(Object)
     * @see com.lidroid.xutils.DbUtils#replaceAll(List)
     */
    public void saveOrUpdateAll(List<?> entities) throws DbException {
        if (entities == null || entities.size() == 0) return;
        try {
            beginTransaction();

            createTableIfNotExist(entities.get(0).getClass());
            for (Object entity : entities) {
                saveOrUpdateWithoutTransaction(entity);
            }

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    /**
     * 更新实体到DB（replace）
     * 
     * <pre>
     * 根据实体类注解，自动创建表；
     * 只持久化基本数据类型、java.lang.*等，不能处理的类型自动忽略；
     * 对静态属性、添加忽略注解属性自动忽略。
     * </pre>
     * 
     * @param entity 实体类实例
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     * @see com.lidroid.xutils.DbUtils#replaceAll(List)
     * @see com.lidroid.xutils.DbUtils#saveOrUpdate(Object)
     */
    public void replace(Object entity) throws DbException {
        try {
            beginTransaction();

            createTableIfNotExist(entity.getClass());
            execNonQuery(SqlInfoBuilder.buildReplaceSqlInfo(this, entity));

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }
    /**
     * 更新实体到DB（replace）
     * 
     * @param entities 实体类实例集合
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     * @see com.lidroid.xutils.DbUtils#replace(Object)
     * @see com.lidroid.xutils.DbUtils#saveOrUpdateAll(List)
     */
    public void replaceAll(List<?> entities) throws DbException {
        if (entities == null || entities.size() == 0) return;
        try {
            beginTransaction();

            createTableIfNotExist(entities.get(0).getClass());
            for (Object entity : entities) {
                execNonQuery(SqlInfoBuilder.buildReplaceSqlInfo(this, entity));
            }

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    /**
     * 保存实体到DB（insert）
     * 
     * <pre>
     * 根据实体类注解，自动创建表；
     * 只持久化基本数据类型、java.lang.*等，不能处理的类型自动忽略；
     * 对静态属性、添加忽略注解属性自动忽略。
     * </pre>
     * 
     * @param entity 实体类实例
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     * @see com.lidroid.xutils.DbUtils#saveAll(List)
     * @see com.lidroid.xutils.DbUtils#saveOrUpdate(Object)
     * @see com.lidroid.xutils.DbUtils#saveBindingId(Object)
     */
    public void save(Object entity) throws DbException {
        try {
            beginTransaction();

            createTableIfNotExist(entity.getClass());
            execNonQuery(SqlInfoBuilder.buildInsertSqlInfo(this, entity));

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }
    /**
     * 保存实体到DB（insert）
     * 
     * @param entities 实体类实例集合
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     * @see com.lidroid.xutils.DbUtils#save(Object)
     * @see com.lidroid.xutils.DbUtils#saveBindingIdAll(List)
     */
    public void saveAll(List<?> entities) throws DbException {
        if (entities == null || entities.size() == 0) return;
        try {
            beginTransaction();

            createTableIfNotExist(entities.get(0).getClass());
            for (Object entity : entities) {
                execNonQuery(SqlInfoBuilder.buildInsertSqlInfo(this, entity));
            }

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    /**
     * 保存实体到DB，并获取当前主键ID的值（insert）
     * 
     * <pre>
     * 根据实体类注解，自动创建表；
     * 只持久化基本数据类型、java.lang.*等，不能处理的类型自动忽略；
     * 对静态属性、添加忽略注解属性自动忽略。
     * </pre>
     * 
     * @param entity 实体类实例
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     * @see com.lidroid.xutils.DbUtils#saveBindingIdWithoutTransaction(Object)
     * @see com.lidroid.xutils.DbUtils#save(Object)
     * @see com.lidroid.xutils.DbUtils#saveBindingIdAll(List)
     */
    public boolean saveBindingId(Object entity) throws DbException {
        boolean result = false;
        try {
            beginTransaction();

            createTableIfNotExist(entity.getClass());
            result = saveBindingIdWithoutTransaction(entity);

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
        return result;
    }
    /**
     * 保存实体到DB，并获取当前主键ID的值（insert）
     * 
     * @param entities 实体类实例集合
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     * @see com.lidroid.xutils.DbUtils#saveBindingId(Object)
     * @see com.lidroid.xutils.DbUtils#saveAll(List)
     * @see com.lidroid.xutils.DbUtils#saveOrUpdateAll(List)
     */
    public void saveBindingIdAll(List<?> entities) throws DbException {
        if (entities == null || entities.size() == 0) return;
        try {
            beginTransaction();

            createTableIfNotExist(entities.get(0).getClass());
            for (Object entity : entities) {
                if (!saveBindingIdWithoutTransaction(entity)) {
                    throw new DbException("saveBindingId error, transaction will not commit!");
                }
            }

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    /**
     * 根据主键删除记录
     * 
     * <pre>
     * 主键ID的值为空时，抛出异常{@link com.lidroid.xutils.exception.DbException}
     * </pre>
     * 
     * @param entityType 实体类类型
     * @param idValue 主键的值
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     */
    public void deleteById(Class<?> entityType, Object idValue) throws DbException {
        if (!tableIsExist(entityType)) return;
        try {
            beginTransaction();

            execNonQuery(SqlInfoBuilder.buildDeleteSqlInfo(this, entityType, idValue));

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }
    /**
     * 根据主键删除记录
     * 
     * <pre>
     * 主键ID的值为空时，抛出异常{@link com.lidroid.xutils.exception.DbException}
     * </pre>
     * 
     * @param entity 实体类实例
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     */
    public void delete(Object entity) throws DbException {
        if (!tableIsExist(entity.getClass())) return;
        try {
            beginTransaction();

            execNonQuery(SqlInfoBuilder.buildDeleteSqlInfo(this, entity));

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }
    /**
     * 根据WHERE条件删除记录
     * 
     * <pre>
     * WHERE为空时，删除所有记录
     * </pre>
     * 
     * @param entityType 实体类类型
     * @param whereBuilder WHERE条件{@link com.lidroid.xutils.db.sqlite.WhereBuilder}
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     */
    public void delete(Class<?> entityType, WhereBuilder whereBuilder) throws DbException {
        if (!tableIsExist(entityType)) return;
        try {
            beginTransaction();

            execNonQuery(SqlInfoBuilder.buildDeleteSqlInfo(this, entityType, whereBuilder));

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }
    
    /**
     * 根据主键删除记录
     * 
     * <pre>
     * 主键ID的值为空时，抛出异常{@link com.lidroid.xutils.exception.DbException}
     * </pre>
     * 
     * @param entities 实体类实例集合
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     */
    public void deleteAll(List<?> entities) throws DbException {
        if (entities == null || entities.size() == 0 || !tableIsExist(entities.get(0).getClass())) return;
        try {
            beginTransaction();

            for (Object entity : entities) {
                execNonQuery(SqlInfoBuilder.buildDeleteSqlInfo(this, entity));
            }

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }
    /**
     * 删除表中所有记录
     * @param entityType 实体类类型
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     */
    public void deleteAll(Class<?> entityType) throws DbException {
        delete(entityType, null);
    }

    /**
     * 根据主键更新记录
     * 
     * <pre>
     * 主键ID的值为空时，抛出异常{@link com.lidroid.xutils.exception.DbException}；
     * updateColumnNames为空时，更新所有字段的值
     * </pre>
     * 
     * @param entity 实体类实例
     * @param updateColumnNames 需要更新的字段名
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     */
    public void update(Object entity, String... updateColumnNames) throws DbException {
        if (!tableIsExist(entity.getClass())) return;
        try {
            beginTransaction();

            execNonQuery(SqlInfoBuilder.buildUpdateSqlInfo(this, entity, updateColumnNames));

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }
    /**
     * 根据主键更新记录
     * 
     * <pre>
     * 主键ID的值为空时，抛出异常{@link com.lidroid.xutils.exception.DbException}；
     * updateColumnNames为空时，更新所有字段的值
     * </pre>
     * 
     * @param entity 实体类实例
     * @param whereBuilder WHERE条件{@link com.lidroid.xutils.db.sqlite.WhereBuilder}
     * @param updateColumnNames 需要更新的字段名
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     */
    public void update(Object entity, WhereBuilder whereBuilder, String... updateColumnNames) throws DbException {
        if (!tableIsExist(entity.getClass())) return;
        try {
            beginTransaction();

            execNonQuery(SqlInfoBuilder.buildUpdateSqlInfo(this, entity, whereBuilder, updateColumnNames));

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }
    /**
     * 根据主键更新记录
     * 
     * <pre>
     * 主键ID的值为空时，抛出异常{@link com.lidroid.xutils.exception.DbException}；
     * updateColumnNames为空时，更新所有字段的值
     * </pre>
     * 
     * @param entities 实体类实例集合
     * @param updateColumnNames 需要更新的字段名
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     */
    public void updateAll(List<?> entities, String... updateColumnNames) throws DbException {
        if (entities == null || entities.size() == 0 || !tableIsExist(entities.get(0).getClass())) return;
        try {
            beginTransaction();

            for (Object entity : entities) {
                execNonQuery(SqlInfoBuilder.buildUpdateSqlInfo(this, entity, updateColumnNames));
            }

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }
    /**
     * 根据主键更新记录
     * 
     * <pre>
     * 主键ID的值为空时，抛出异常{@link com.lidroid.xutils.exception.DbException}；
     * updateColumnNames为空时，更新所有字段的值
     * </pre>
     * 
     * @param entities 实体类实例集合
     * @param whereBuilder WHERE条件{@link com.lidroid.xutils.db.sqlite.WhereBuilder}
     * @param updateColumnNames 需要更新的字段名
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     */
    public void updateAll(List<?> entities, WhereBuilder whereBuilder, String... updateColumnNames) throws DbException {
        if (entities == null || entities.size() == 0 || !tableIsExist(entities.get(0).getClass())) return;
        try {
            beginTransaction();

            for (Object entity : entities) {
                execNonQuery(SqlInfoBuilder.buildUpdateSqlInfo(this, entity, whereBuilder, updateColumnNames));
            }

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    /**
     * 根据主键查找记录
     * @param entityType 实体类类型
     * @param idValue 主键ID的值（值为空时，抛出异常{@link com.lidroid.xutils.exception.DbException}）
     * @return 实体类实例
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     */
    @SuppressWarnings("unchecked")
    public <T> T findById(Class<T> entityType, Object idValue) throws DbException {
        if (!tableIsExist(entityType)) return null;

        Table table = Table.get(this, entityType);
        Selector selector = Selector.from(entityType).where(table.id.getColumnName(), "=", idValue);

        String sql = selector.limit(1).toString();
        long seq = CursorUtils.FindCacheSequence.getSeq();
        findTempCache.setSeq(seq);
        Object obj = findTempCache.get(sql);
        if (obj != null) {
            return (T) obj;
        }

        Cursor cursor = execQuery(sql);
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    T entity = (T) CursorUtils.getEntity(this, cursor, entityType, seq);
                    findTempCache.put(sql, entity);
                    return entity;
                }
            } catch (Throwable e) {
                throw new DbException(e);
            } finally {
                IOUtils.closeQuietly(cursor);
            }
        }
        return null;
    }
    /**
     * 根据SQL查询条件查找记录
     * 
     * <pre>
     * 多条记录时，获取第一条
     * </pre>
     * 
     * @param selector SQL查询条件描述 {@link com.lidroid.xutils.db.sqlite.Selector}
     * @return 实体类实例
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     */
    @SuppressWarnings("unchecked")
    public <T> T findFirst(Selector selector) throws DbException {
        if (!tableIsExist(selector.getEntityType())) return null;

        String sql = selector.limit(1).toString();
        long seq = CursorUtils.FindCacheSequence.getSeq();
        findTempCache.setSeq(seq);
        Object obj = findTempCache.get(sql);
        if (obj != null) {
            return (T) obj;
        }

        Cursor cursor = execQuery(sql);
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    T entity = (T) CursorUtils.getEntity(this, cursor, selector.getEntityType(), seq);
                    findTempCache.put(sql, entity);
                    return entity;
                }
            } catch (Throwable e) {
                throw new DbException(e);
            } finally {
                IOUtils.closeQuietly(cursor);
            }
        }
        return null;
    }
    /**
     * 根据实体类类型查找记录
     * 
     * <pre>
     * 多条记录时，获取第一条
     * </pre>
     * 
     * @param entityType 实体类类型{@link java.lang.Class}
     * @return 实体类实例
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     */
    public <T> T findFirst(Class<T> entityType) throws DbException {
        return findFirst(Selector.from(entityType));
    }

    /**
     * 根据SQL查询条件查找记录
     * 
     * @param selector SQL查询条件描述 {@link com.lidroid.xutils.db.sqlite.Selector}
     * @return 实体类实例集合
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> findAll(Selector selector) throws DbException {
        if (!tableIsExist(selector.getEntityType())) return null;

        String sql = selector.toString();
        long seq = CursorUtils.FindCacheSequence.getSeq();
        findTempCache.setSeq(seq);
        Object obj = findTempCache.get(sql);
        if (obj != null) {
            return (List<T>) obj;
        }

        List<T> result = new ArrayList<T>();

        Cursor cursor = execQuery(sql);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    T entity = (T) CursorUtils.getEntity(this, cursor, selector.getEntityType(), seq);
                    result.add(entity);
                }
                findTempCache.put(sql, result);
            } catch (Throwable e) {
                throw new DbException(e);
            } finally {
                IOUtils.closeQuietly(cursor);
            }
        }
        return result;
    }
    /**
     * 根据实体类类型查找记录
     * 
     * @param entityType 实体类类型{@link java.lang.Class}
     * @return 实体类实例集合
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     */
    public <T> List<T> findAll(Class<T> entityType) throws DbException {
        return findAll(Selector.from(entityType));
    }
    
    /**
     * 根据SQL语句查找记录
     * 
     * <pre>
     * 统一数据存储为为String类型；
     * 统一通过{@link android.database.Cursor#getString(int)}获取数据。
     * </pre>
     * 
     * @param sqlInfo SQL语句描述{@link com.lidroid.xutils.db.sqlite.SqlInfo}
     * @return DB数据模型{@link com.lidroid.xutils.db.table.DbModel}
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     * @see android.database.Cursor#getString(int)
     */
    public DbModel findDbModelFirst(SqlInfo sqlInfo) throws DbException {
        Cursor cursor = execQuery(sqlInfo);
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    return CursorUtils.getDbModel(cursor);
                }
            } catch (Throwable e) {
                throw new DbException(e);
            } finally {
                IOUtils.closeQuietly(cursor);
            }
        }
        return null;
    }
    /**
     * 根据SQL查询条件查找记录
     * 
     * <pre>
     * 统一数据存储为为String类型；
     * 统一通过{@link android.database.Cursor#getString(int)}获取数据。
     * </pre>
     * 
     * @param selector DB数据模型SQL查询条件描述{@link com.lidroid.xutils.db.sqlite.DbModelSelector}
     * @return DB数据模型{@link com.lidroid.xutils.db.table.DbModel}
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     * @see android.database.Cursor#getString(int)
     */
    public DbModel findDbModelFirst(DbModelSelector selector) throws DbException {
        if (!tableIsExist(selector.getEntityType())) return null;

        Cursor cursor = execQuery(selector.limit(1).toString());
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    return CursorUtils.getDbModel(cursor);
                }
            } catch (Throwable e) {
                throw new DbException(e);
            } finally {
                IOUtils.closeQuietly(cursor);
            }
        }
        return null;
    }
    /**
     * 根据SQL语句条件查找记录
     * 
     * <pre>
     * 统一数据存储为为String类型；
     * 统一通过{@link android.database.Cursor#getString(int)}获取数据。
     * </pre>
     * 
     * @param sqlInfo SQL语句描述{@link com.lidroid.xutils.db.sqlite.SqlInfo}
     * @return DB数据模型集合{@link java.util.List}
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     * @see android.database.Cursor#getString(int)
     */
    public List<DbModel> findDbModelAll(SqlInfo sqlInfo) throws DbException {
        List<DbModel> dbModelList = new ArrayList<DbModel>();

        Cursor cursor = execQuery(sqlInfo);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    dbModelList.add(CursorUtils.getDbModel(cursor));
                }
            } catch (Throwable e) {
                throw new DbException(e);
            } finally {
                IOUtils.closeQuietly(cursor);
            }
        }
        return dbModelList;
    }
    /**
     * 根据SQL查询条件查找记录
     * 
     * <pre>
     * 统一数据存储为为String类型；
     * 统一通过{@link android.database.Cursor#getString(int)}获取数据。
     * </pre>
     * 
     * @param selector DB数据模型SQL查询条件描述{@link com.lidroid.xutils.db.sqlite.DbModelSelector}
     * @return DB数据模型集合{@link java.util.List}
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     * @see android.database.Cursor#getString(int)
     */
    public List<DbModel> findDbModelAll(DbModelSelector selector) throws DbException {
        if (!tableIsExist(selector.getEntityType())) return null;
        
        List<DbModel> dbModelList = new ArrayList<DbModel>();
        
        Cursor cursor = execQuery(selector.toString());
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    dbModelList.add(CursorUtils.getDbModel(cursor));
                }
            } catch (Throwable e) {
                throw new DbException(e);
            } finally {
                IOUtils.closeQuietly(cursor);
            }
        }
        return dbModelList;
    }
    
    /**
     * 根据SQL查询条件统计记录条数
     * 
     * @param selector SQL查询条件描述{@link com.lidroid.xutils.db.sqlite.Selector}
     * @return 记录条数
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     */
    public long count(Selector selector) throws DbException {
        Class<?> entityType = selector.getEntityType();
        if (!tableIsExist(entityType)) return 0;

        Table table = Table.get(this, entityType);
        DbModelSelector dmSelector = selector.select("count(" + table.id.getColumnName() + ") as count");
        return findDbModelFirst(dmSelector).getLong("count");
    }
    /**
     * 根据SQL查询条件统计记录条数
     * 
     * @param entityType 实体类类型{@link java.lang.Class}
     * @return 记录条数
     * @throws DbException DB操作异常{@link com.lidroid.xutils.exception.DbException}
     */
    public long count(Class<?> entityType) throws DbException {
        return count(Selector.from(entityType));
    }

    //******************************************** config ******************************************************
    /**
     * DB操作的配置项管理
     */
    public static class DaoConfig {
        private Context context;
        private String dbName = "xUtils.db"; // default db name
        private int dbVersion = 1;
        private DbUpgradeListener dbUpgradeListener;

        private String dbDir;

        /**
         * 构造DB操作的配置项管理
         * @param context android.content.Context
         */
        public DaoConfig(Context context) {
            this.context = context.getApplicationContext();
        }

        /**
         * 获取当前引用的Context
         * @return android.content.Context
         */
        public Context getContext() {
            return context;
        }

        /**
         * 获取数据库文件名
         * @return 数据库文件名
         */
        public String getDbName() {
            return dbName;
        }

        /**
         * 设置数据库文件名
         * @param dbName 数据库文件名
         */
        public void setDbName(String dbName) {
            if (!TextUtils.isEmpty(dbName)) {
                this.dbName = dbName;
            }
        }

        /**
         * 获取数据库版本号
         * @return 数据库版本号
         */
        public int getDbVersion() {
            return dbVersion;
        }

        /**
         * 设置数据库版本号
         * @param dbVersion 数据库版本号
         */
        public void setDbVersion(int dbVersion) {
            this.dbVersion = dbVersion;
        }

        /**
         * 获取数据库版本升级通知接口
         * @return 版本升级通知接口
         */
        public DbUpgradeListener getDbUpgradeListener() {
            return dbUpgradeListener;
        }

        /**
         * 设置数据库版本升级通知接口
         * @param dbUpgradeListener 数据库版本升级通知接口{@link com.lidroid.xutils.DbUtils.DbUpgradeListener}
         */
        public void setDbUpgradeListener(DbUpgradeListener dbUpgradeListener) {
            this.dbUpgradeListener = dbUpgradeListener;
        }
        
        /**
         * 获取数据库文件存储路径
         * @return 数据库文件存储路径
         */
        public String getDbDir() {
            return dbDir;
        }

        /**
         * 设置数据库文件存储路径
         * @param dbDir 数据库文件存储路径（默认：“APP内部缓存目录”，即/data/data/youpackage/cache）
         */
        public void setDbDir(String dbDir) {
            this.dbDir = dbDir;
        }
    }

    /**
     * DB版本升级通知接口
     */
    public interface DbUpgradeListener {
        /**
         * DB版本升级时执行
         * @param db DbUtils实例{@link com.lidroid.xutils.DbUtils}
         * @param oldVersion 旧版本号
         * @param newVersion 新版本号
         */
        public void onUpgrade(DbUtils db, int oldVersion, int newVersion);
    }

    private SQLiteDatabase createDatabase(DaoConfig config) {
        SQLiteDatabase result = null;

        String dbDir = config.getDbDir();
        if (!TextUtils.isEmpty(dbDir)) {
            File dir = new File(dbDir);
            if (dir.exists() || dir.mkdirs()) {
                File dbFile = new File(dbDir, config.getDbName());
                result = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
            }
        } else {
            result = config.getContext().openOrCreateDatabase(config.getDbName(), 0, null);
        }
        return result;
    }

    //***************************** private operations with out transaction *****************************
    private void saveOrUpdateWithoutTransaction(Object entity) throws DbException {
        Table table = Table.get(this, entity.getClass());
        Id id = table.id;
        if (id.isAutoIncrement()) {
            if (id.getColumnValue(entity) != null) {
                execNonQuery(SqlInfoBuilder.buildUpdateSqlInfo(this, entity));
            } else {
                saveBindingIdWithoutTransaction(entity);
            }
        } else {
            execNonQuery(SqlInfoBuilder.buildReplaceSqlInfo(this, entity));
        }
    }

    private boolean saveBindingIdWithoutTransaction(Object entity) throws DbException {
        Class<?> entityType = entity.getClass();
        Table table = Table.get(this, entityType);
        Id idColumn = table.id;
        if (idColumn.isAutoIncrement()) {
            execNonQuery(SqlInfoBuilder.buildInsertSqlInfo(this, entity));
            long id = getLastAutoIncrementId(table.tableName);
            if (id == -1) {
                return false;
            }
            idColumn.setAutoIncrementId(entity, id);
            return true;
        } else {
            execNonQuery(SqlInfoBuilder.buildInsertSqlInfo(this, entity));
            return true;
        }
    }

    //************************************************ tools ***********************************

    private long getLastAutoIncrementId(String tableName) throws DbException {
        long id = -1;
        Cursor cursor = execQuery("SELECT seq FROM sqlite_sequence WHERE name='" + tableName + "'");
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    id = cursor.getLong(0);
                }
            } catch (Throwable e) {
                throw new DbException(e);
            } finally {
                IOUtils.closeQuietly(cursor);
            }
        }
        return id;
    }

    /**
     * 创建数据库表，当表不存在时
     * @param entityType 实体类类型{@link java.lang.Class}
     * @throws DbException 数据库操作异常{@link com.lidroid.xutils.exception.DbException}
     */
    public void createTableIfNotExist(Class<?> entityType) throws DbException {
        if (!tableIsExist(entityType)) {
            SqlInfo sqlInfo = SqlInfoBuilder.buildCreateTableSqlInfo(this, entityType);
            execNonQuery(sqlInfo);
            String execAfterTableCreated = TableUtils.getExecAfterTableCreated(entityType);
            if (!TextUtils.isEmpty(execAfterTableCreated)) {
                execNonQuery(execAfterTableCreated);
            }
        }
    }

    /**
     * 检查数据库表是否存在
     * @param entityType 实体类类型{@link java.lang.Class}
     * @throws DbException 数据库操作异常{@link com.lidroid.xutils.exception.DbException}
     */
    public boolean tableIsExist(Class<?> entityType) throws DbException {
        Table table = Table.get(this, entityType);
        if (table.isCheckedDatabase()) {
            return true;
        }

        Cursor cursor = execQuery("SELECT COUNT(*) AS c FROM sqlite_master WHERE type='table' AND name='" + table.tableName + "'");
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    int count = cursor.getInt(0);
                    if (count > 0) {
                        table.setCheckedDatabase(true);
                        return true;
                    }
                }
            } catch (Throwable e) {
                throw new DbException(e);
            } finally {
                IOUtils.closeQuietly(cursor);
            }
        }

        return false;
    }

    /**
     * 清空当前数据库（删除所有表）
     * @throws DbException 数据库操作异常{@link com.lidroid.xutils.exception.DbException}
     */
    public void dropDb() throws DbException {
        Cursor cursor = execQuery("SELECT name FROM sqlite_master WHERE type='table' AND name<>'sqlite_sequence'");
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    try {
                        String tableName = cursor.getString(0);
                        execNonQuery("DROP TABLE " + tableName);
                        Table.remove(this, tableName);
                    } catch (Throwable e) {
                        LogUtils.e(e.getMessage(), e);
                    }
                }

            } catch (Throwable e) {
                throw new DbException(e);
            } finally {
                IOUtils.closeQuietly(cursor);
            }
        }
    }
    /**
     * 删除数据库表
     * @param entityType 实体类类型{@link java.lang.Class}
     * @throws DbException 数据库操作异常{@link com.lidroid.xutils.exception.DbException}
     */
    public void dropTable(Class<?> entityType) throws DbException {
        if (!tableIsExist(entityType)) return;
        String tableName = TableUtils.getTableName(entityType);
        execNonQuery("DROP TABLE " + tableName);
        Table.remove(this, entityType);
    }
    
    /**
     * 关闭当前数据库连接
     */
    public void close() {
        String dbName = this.daoConfig.getDbName();
        if (daoMap.containsKey(dbName)) {
            daoMap.remove(dbName);
            this.database.close();
        }
    }

    ///////////////////////////////////// exec sql /////////////////////////////////////////////////////
    private void debugSql(String sql) {
        if (debug) {
            LogUtils.d(sql);
        }
    }

    private Lock writeLock = new ReentrantLock();
    private volatile boolean writeLocked = false;

    private void beginTransaction() {
        if (allowTransaction) {
            database.beginTransaction();
        } else {
            writeLock.lock();
            writeLocked = true;
        }
    }

    private void setTransactionSuccessful() {
        if (allowTransaction) {
            database.setTransactionSuccessful();
        }
    }

    private void endTransaction() {
        if (allowTransaction) {
            database.endTransaction();
        }
        if (writeLocked) {
            writeLock.unlock();
            writeLocked = false;
        }
    }

    /**
     * 执行SQL更新（非查询）
     * @param sqlInfo SQL语句描述{@link com.lidroid.xutils.db.sqlite.SqlInfo}
     * @throws DbException 数据库操作异常{@link com.lidroid.xutils.exception.DbException}
     * @see #execNonQuery(String)
     * @see #execQuery(SqlInfo)
     */
    public void execNonQuery(SqlInfo sqlInfo) throws DbException {
        debugSql(sqlInfo.getSql());
        try {
            if (sqlInfo.getBindArgs() != null) {
                database.execSQL(sqlInfo.getSql(), sqlInfo.getBindArgsAsArray());
            } else {
                database.execSQL(sqlInfo.getSql());
            }
        } catch (Throwable e) {
            throw new DbException(e);
        }
    }
    /**
     * 执行SQL更新（非查询）
     * @param sql SQL语句
     * @throws DbException 数据库操作异常{@link com.lidroid.xutils.exception.DbException}
     * @see #execNonQuery(SqlInfo)
     * @see #execQuery(String)
     */
    public void execNonQuery(String sql) throws DbException {
        debugSql(sql);
        try {
            database.execSQL(sql);
        } catch (Throwable e) {
            throw new DbException(e);
        }
    }
    
    /**
     * 执行SQL查询
     * @param sqlInfo SQL语句描述{@link com.lidroid.xutils.db.sqlite.SqlInfo}
     * @return {@link android.database.Cursor}
     * @throws DbException 数据库操作异常{@link com.lidroid.xutils.exception.DbException}
     * @see #execQuery(String)
     * @see #execNonQuery(SqlInfo)
     */
    public Cursor execQuery(SqlInfo sqlInfo) throws DbException {
        debugSql(sqlInfo.getSql());
        try {
            return database.rawQuery(sqlInfo.getSql(), sqlInfo.getBindArgsAsStrArray());
        } catch (Throwable e) {
            throw new DbException(e);
        }
    }
    /**
     * 执行SQL查询
     * @param sql SQL语句
     * @return {@link android.database.Cursor}
     * @throws DbException 数据库操作异常{@link com.lidroid.xutils.exception.DbException}
     * @see #execQuery(SqlInfo)
     * @see #execNonQuery(String)
     */
    public Cursor execQuery(String sql) throws DbException {
        debugSql(sql);
        try {
            return database.rawQuery(sql, null);
        } catch (Throwable e) {
            throw new DbException(e);
        }
    }

    /////////////////////// temp cache ////////////////////////////////////////////////////////////////
    private final FindTempCache findTempCache = new FindTempCache();

    private class FindTempCache {
        private FindTempCache() {
        }

        /**
         * key: sql;
         * value: find result
         */
        private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<String, Object>();

        private long seq = 0;

        public void put(String sql, Object result) {
            if (sql != null && result != null) {
                cache.put(sql, result);
            }
        }

        public Object get(String sql) {
            return cache.get(sql);
        }

        public void setSeq(long seq) {
            if (this.seq != seq) {
                cache.clear();
                this.seq = seq;
            }
        }
    }

}
