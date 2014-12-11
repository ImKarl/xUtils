package com.lidroid.xutils.view;

/**
 * 控件注入{@link android.view.View}注解信息
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-12-5
 * Time: 下午11:25
 * </pre>
 * 
 * @author wyouflf
 */
public class ViewInjectInfo {
    
    /**
     * 控件ID的值
     */
    public Object value;
    
    /**
     * 所属父控件的ID
     */
    public int parentId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ViewInjectInfo)) return false;

        ViewInjectInfo that = (ViewInjectInfo) o;

        if (parentId != that.parentId) return false;
        if (value == null) return (null == that.value);

        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + parentId;
        return result;
    }
}
