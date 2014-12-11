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

package com.lidroid.xutils.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.protocol.HTTP;

/**
 * 字符编码处理-工具类
 * 
 * <pre>
 * Created by wyouflf on 13-8-30.
 * </pre>
 * 
 * @author wyouflf
 */
public class CharsetUtils {

    private CharsetUtils() {
    }

    /**
     * 将字符串转换为指定编码
     * @param str 原始字符串
     * @param charset 字符编码
     * @param judgeCharsetLength 预估的字符长度
     * @return 编码转换后的字符串
     * @see java.nio.charset.Charset
     */
    public static String toCharset(final String str, final String charset, int judgeCharsetLength) {
        try {
            String oldCharset = getEncoding(str, judgeCharsetLength);
            return new String(str.getBytes(oldCharset), charset);
        } catch (Throwable ex) {
            LogUtils.w(ex);
            return str;
        }
    }

    /**
     * 判断字符串的编码
     * @param str 原始字符串
     * @param judgeCharsetLength 预估的字符长度
     * @return 字符编码
     * @see java.nio.charset.Charset
     */
    public static String getEncoding(final String str, int judgeCharsetLength) {
        String encode = CharsetUtils.DEFAULT_ENCODING_CHARSET;
        for (String charset : SUPPORT_CHARSET) {
            if (isCharset(str, charset, judgeCharsetLength)) {
                encode = charset;
                break;
            }
        }
        return encode;
    }

    /**
     * 判断是否指定编码
     * @param str 原始字符串
     * @param charset 字符编码
     * @param judgeCharsetLength 预估的字符长度
     * @return 是否该字符编码
     * @see java.nio.charset.Charset
     */
    public static boolean isCharset(final String str, final String charset, int judgeCharsetLength) {
        try {
            String temp = str.length() > judgeCharsetLength ? str.substring(0, judgeCharsetLength) : str;
            return temp.equals(new String(temp.getBytes(charset), charset));
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * 默认字符编码（ISO-8859-1）
     */
    public static final String DEFAULT_ENCODING_CHARSET = HTTP.DEFAULT_CONTENT_CHARSET;

    /**
     * 支持的字符编码集
     */
    public static final List<String> SUPPORT_CHARSET = new ArrayList<String>();

    static {
        SUPPORT_CHARSET.add("ISO-8859-1");

        SUPPORT_CHARSET.add("GB2312");
        SUPPORT_CHARSET.add("GBK");
        SUPPORT_CHARSET.add("GB18030");

        SUPPORT_CHARSET.add("US-ASCII");
        SUPPORT_CHARSET.add("ASCII");

        SUPPORT_CHARSET.add("ISO-2022-KR");

        SUPPORT_CHARSET.add("ISO-8859-2");

        SUPPORT_CHARSET.add("ISO-2022-JP");
        SUPPORT_CHARSET.add("ISO-2022-JP-2");

        SUPPORT_CHARSET.add("UTF-8");
    }
    
}
