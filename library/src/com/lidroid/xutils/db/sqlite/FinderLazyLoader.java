package com.lidroid.xutils.db.sqlite;

import com.lidroid.xutils.db.table.ColumnUtils;
import com.lidroid.xutils.db.table.Finder;
import com.lidroid.xutils.db.table.Table;
import com.lidroid.xutils.exception.DbException;

import java.util.List;

/**
 * 数据库表的主表关联数据集懒加载
 * 
 * <pre>
 * 主表关联的从表数据，并不会立即加载；
 * 调用getFirstFromDb()或getAllFromDb()方法，会立即执行数据库查询，此时才真正获取到数据
 * </pre>
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-9-10
 * Time: 下午10:50
 * </pre>
 * 
 * @param <T> 关联从表对应的实体类类型
 * 
 * @author wyouflf
 */
public class FinderLazyLoader<T> {
    private final Finder finderColumn;
    private final Object finderValue;

    /**
     * 构造主表关联数据集懒加载
     * @param finderColumn 主表关联列的描述 {@link com.lidroid.xutils.db.table.Finder}
     * @param value 数据库列值
     */
    public FinderLazyLoader(Finder finderColumn, Object value) {
        this.finderColumn = finderColumn;
        this.finderValue = ColumnUtils.convert2DbColumnValueIfNeeded(value);
    }

    /**
     * 获取关联的所有从表数据
     * 
     * <pre>
     * 会立即执行数据库查询，此时真正获取到数据
     * </pre>
     * 
     * @return 从表数据集{@link java.util.List}
     * @throws DbException 数据库操作异常
     */
    public List<T> getAllFromDb() throws DbException {
        List<T> entities = null;
        Table table = finderColumn.getTable();
        if (table != null) {
            entities = table.db.findAll(
                    Selector.from(finderColumn.getTargetEntityType()).
                            where(finderColumn.getTargetColumnName(), "=", finderValue)
            );
        }
        return entities;
    }

    /**
     * 获取关联的一条从表数据
     * 
     * <pre>
     * 会立即执行数据库查询，此时真正获取到数据
     * </pre>
     * 
     * @return 一条从表数据
     * @throws DbException 数据库操作异常
     */
    public T getFirstFromDb() throws DbException {
        T entity = null;
        Table table = finderColumn.getTable();
        if (table != null) {
            entity = table.db.findFirst(
                    Selector.from(finderColumn.getTargetEntityType()).
                            where(finderColumn.getTargetColumnName(), "=", finderValue)
            );
        }
        return entity;
    }
    
}
