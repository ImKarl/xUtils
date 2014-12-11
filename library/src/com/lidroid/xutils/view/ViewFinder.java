package com.lidroid.xutils.view;

import android.app.Activity;
import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.view.View;

/**
 * 控件{@link android.view.View}查找辅助工具
 * 
 * Author: wyouflf
 * Date: 13-9-9
 * Time: 下午12:29
 */
public class ViewFinder {

    private View view;
    private Activity activity;
    private PreferenceGroup preferenceGroup;
    private PreferenceActivity preferenceActivity;

    /**
     * 构造控件查找辅助工具
     * @param view {@link android.view.View}
     */
    public ViewFinder(View view) {
        this.view = view;
    }
    /**
     * 构造控件查找辅助工具
     * @param activity {@link android.app.Activity}
     */
    public ViewFinder(Activity activity) {
        this.activity = activity;
    }
    /**
     * 构造控件查找辅助工具
     * @param preferenceGroup {@link android.preference.PreferenceGroup}
     */
    public ViewFinder(PreferenceGroup preferenceGroup) {
        this.preferenceGroup = preferenceGroup;
    }
    /**
     * 构造控件查找辅助工具
     * @param preferenceActivity {@link android.preference.PreferenceActivity}
     */
    public ViewFinder(PreferenceActivity preferenceActivity) {
        this.preferenceActivity = preferenceActivity;
        this.activity = preferenceActivity;
    }

    public View findViewById(int id) {
        return activity == null ? view.findViewById(id) : activity.findViewById(id);
    }

    /**
     * 查找控件
     * @param info 控件注入注解信息（只用于单个ID注解，否则抛出异常{@link java.lang.ClassCastException}）
     * @return 控件{@link android.view.View}
     */
    public View findViewByInfo(ViewInjectInfo info) {
        return findViewById((Integer) info.value, info.parentId);
    }

    public View findViewById(int id, int pid) {
        View pView = null;
        if (pid > 0) {
            pView = this.findViewById(pid);
        }

        View view = null;
        if (pView != null) {
            view = pView.findViewById(id);
        } else {
            view = this.findViewById(id);
        }
        return view;
    }

    /**
     * 查找首选项控件
     * @param key 首选项标签名
     * @return 首选项控件{@link android.preference.Preference}
     */
    public Preference findPreference(CharSequence key) {
        return preferenceGroup == null ? preferenceActivity.findPreference(key) : preferenceGroup.findPreference(key);
    }

    public Context getContext() {
        if (view != null) return view.getContext();
        if (activity != null) return activity;
        if (preferenceActivity != null) return preferenceActivity;
        return null;
    }
    
}
