package com.lidroid.xutils.view;

import android.content.Context;
import android.view.animation.AnimationUtils;

/**
 * 资源文件的内容加载器
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-11-9
 * Time: 下午3:12
 * </pre>
 * 
 * @author wyouflf
 */
public class ResLoader {

    /**
     * 加载资源文件的内容
     * @param type 资源文件类型{@link com.lidroid.xutils.view.ResType}
     * @param context android.content.Context
     * @param id 资源文件ID
     * @return 资源文件的内容
     */
    public static Object loadRes(ResType type, Context context, int id) {
        if (context == null || id < 1) return null;
        switch (type) {
            case Animation:
                return AnimationUtils.loadAnimation(context, id);
            case Boolean:
                return context.getResources().getBoolean(id);
            case Color:
                return context.getResources().getColor(id);
            case ColorStateList:
                return context.getResources().getColorStateList(id);
            case Dimension:
                return context.getResources().getDimension(id);
            case DimensionPixelOffset:
                return context.getResources().getDimensionPixelOffset(id);
            case DimensionPixelSize:
                return context.getResources().getDimensionPixelSize(id);
            case Drawable:
                return context.getResources().getDrawable(id);
            case Integer:
                return context.getResources().getInteger(id);
            case IntArray:
                return context.getResources().getIntArray(id);
            case Movie:
                return context.getResources().getMovie(id);
            case String:
                return context.getResources().getString(id);
            case StringArray:
                return context.getResources().getStringArray(id);
            case Text:
                return context.getResources().getText(id);
            case TextArray:
                return context.getResources().getTextArray(id);
            case Xml:
                return context.getResources().getXml(id);
            default:
                break;
        }

        return null;
    }

}
