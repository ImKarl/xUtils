package com.lidroid.xutils.db.sqlite;

/**
 * 数据库中列的类型
 * 
 * <pre>
 * Created by wyouflf on 14-2-20.
 * </pre>
 * 
 * @author wyouflf
 */
public enum ColumnDbType {

    /** 整形型 */
    INTEGER("INTEGER"),
    /** 浮点型 */
    REAL("REAL"),
    /** 文本型 */
    TEXT("TEXT"),
    /** 数据块型 */
    BLOB("BLOB");

    private String value;

    ColumnDbType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
