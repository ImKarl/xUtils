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

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.protocol.HTTP;

import javax.net.ssl.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.security.cert.X509Certificate;
import java.util.Locale;

/**
 * 其他处理工具类
 * 
 * <pre>
 * Created by wyouflf on 13-8-30.
 * </pre>
 * 
 * @author wyouflf
 */
public class OtherUtils {
    private OtherUtils() {
    }

    /**
     * 获取HTTP请求的用户代理头信息
     * @param context if null, use the default format
     *                (Mozilla/5.0 (Linux; U; Android %s) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 %sSafari/534.30).
     * @return HTTP请求的用户代理头信息
     */
    public static String getUserAgent(Context context) {
        String webUserAgent = null;
        if (context != null) {
            try {
                Class sysResCls = Class.forName("com.android.internal.R$string");
                Field webUserAgentField = sysResCls.getDeclaredField("web_user_agent");
                Integer resId = (Integer) webUserAgentField.get(null);
                webUserAgent = context.getString(resId);
            } catch (Throwable ignored) {
            }
        }
        if (TextUtils.isEmpty(webUserAgent)) {
            webUserAgent = "Mozilla/5.0 (Linux; U; Android %s) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 %sSafari/533.1";
        }

        Locale locale = Locale.getDefault();
        StringBuffer buffer = new StringBuffer();
        // Add version
        final String version = Build.VERSION.RELEASE;
        if (version.length() > 0) {
            buffer.append(version);
        } else {
            // default to "1.0"
            buffer.append("1.0");
        }
        buffer.append("; ");
        final String language = locale.getLanguage();
        if (language != null) {
            buffer.append(language.toLowerCase());
            final String country = locale.getCountry();
            if (country != null) {
                buffer.append("-");
                buffer.append(country.toLowerCase());
            }
        } else {
            // default to "en"
            buffer.append("en");
        }
        // add the model for the release build
        if ("REL".equals(Build.VERSION.CODENAME)) {
            final String model = Build.MODEL;
            if (model.length() > 0) {
                buffer.append("; ");
                buffer.append(model);
            }
        }
        final String id = Build.ID;
        if (id.length() > 0) {
            buffer.append(" Build/");
            buffer.append(id);
        }
        return String.format(webUserAgent, buffer, "Mobile ");
    }

    /**
     * 获取磁盘缓存目录
     * 
     * <pre>
     * 优先外置存储（SDCard）；
     * 如果没有外置存储，则使用手机的应用缓存目录。
     * </pre>
     * 
     * @param context android.content.Context
     * @param dirName 仅仅是文件夹的名字，不是完整路径
     * @return APP缓存目录（app_cache_path/dirName）
     */
    public static String getDiskCacheDir(Context context, String dirName) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File externalCacheDir = context.getExternalCacheDir();
            if (externalCacheDir != null) {
                cachePath = externalCacheDir.getPath();
            }
        }
        if (cachePath == null) {
            File cacheDir = context.getCacheDir();
            if (cacheDir != null && cacheDir.exists()) {
                cachePath = cacheDir.getPath();
            }
        }
        return cachePath + File.separator + dirName;
    }

    /**
     * 获取可用空间
     * @param dir 文件目录{@link java.io.File}
     * @return 可用空间大小（单位：byte）（计算失败时，返回-1）
     */
    public static long getAvailableSpace(File dir) {
        try {
            final StatFs stats = new StatFs(dir.getPath());
            return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
            return -1;
        }
    }

    /**
     * 判断HTTP响应是否支持断点续传
     * @param response HTTP响应{@link org.apache.http.HttpResponse}
     * @return 是否支持断点续传
     */
    public static boolean isSupportRange(final HttpResponse response) {
        if (response == null) return false;
        Header header = response.getFirstHeader("Accept-Ranges");
        if (header != null) {
            return "bytes".equals(header.getValue());
        }
        header = response.getFirstHeader("Content-Range");
        if (header != null) {
            String value = header.getValue();
            return value != null && value.startsWith("bytes");
        }
        return false;
    }

    /**
     * 获取HTTP响应反馈的文件名
     * @param response HTTP响应{@link org.apache.http.HttpResponse}
     * @return 文件名
     */
    public static String getFileNameFromHttpResponse(final HttpResponse response) {
        if (response == null) return null;
        String result = null;
        Header header = response.getFirstHeader("Content-Disposition");
        if (header != null) {
            for (HeaderElement element : header.getElements()) {
                NameValuePair fileNamePair = element.getParameterByName("filename");
                if (fileNamePair != null) {
                    result = fileNamePair.getValue();
                    // try to get correct encoding str
                    result = CharsetUtils.toCharset(result, HTTP.UTF_8, result.length());
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 获取HTTP请求的字符编码
     * @param request HTTP请求{@link org.apache.http.client.methods.HttpRequestBase}
     * @return 字符编码{@link java.nio.charset.Charset}
     * @see java.nio.charset.Charset
     */
    public static Charset getCharsetFromHttpRequest(final HttpRequestBase request) {
        if (request == null) return null;
        String charsetName = null;
        Header header = request.getFirstHeader("Content-Type");
        if (header != null) {
            for (HeaderElement element : header.getElements()) {
                NameValuePair charsetPair = element.getParameterByName("charset");
                if (charsetPair != null) {
                    charsetName = charsetPair.getValue();
                    break;
                }
            }
        }

        boolean isSupportedCharset = false;
        if (!TextUtils.isEmpty(charsetName)) {
            try {
                isSupportedCharset = Charset.isSupported(charsetName);
            } catch (Throwable e) {
            }
        }

        return isSupportedCharset ? Charset.forName(charsetName) : null;
    }

    private static final int STRING_BUFFER_LENGTH = 100;

    /**
     * 获取字符串字节长度
     * @param str 字符串
     * @param charset 字符编码
     * @return 字节长度
     * @throws UnsupportedEncodingException 编码转换异常，或不被支持的编码
     */
    public static long sizeOfString(final String str, String charset) throws UnsupportedEncodingException {
        if (TextUtils.isEmpty(str)) {
            return 0;
        }
        int len = str.length();
        if (len < STRING_BUFFER_LENGTH) {
            return str.getBytes(charset).length;
        }
        long size = 0;
        for (int i = 0; i < len; i += STRING_BUFFER_LENGTH) {
            int end = i + STRING_BUFFER_LENGTH;
            end = end < len ? end : len;
            String temp = getSubString(str, i, end);
            size += temp.getBytes(charset).length;
        }
        return size;
    }

    /**
     * 获取裁剪后的字符串（新的实例）
     * 
     * <pre>
     * get the sub string for large string
     * </pre>
     * 
     * @param str 原始字符串
     * @param start 开始的索引
     * @param end 结束的索引
     * @return 裁剪后的字符串（新的实例）
     */
    public static String getSubString(final String str, int start, int end) {
        return new String(str.substring(start, end));
    }

    /**
     * 获取当前调用者（方法）的栈堆信息
     * @return 方法的栈堆信息描述
     */
    public static StackTraceElement getCurrentStackTraceElement() {
        return Thread.currentThread().getStackTrace()[3];
    }

    /**
     * 获取调用当前调用者（方法）的栈堆信息
     * @return 方法的栈堆信息描述
     */
    public static StackTraceElement getCallerStackTraceElement() {
        return Thread.currentThread().getStackTrace()[4];
    }

    private static SSLSocketFactory sslSocketFactory;

    /**
     * 信任所有HttpS连接
     */
    public static void trustAllHttpsURLConnection() {
        // Create a trust manager that does not validate certificate chains
        if (sslSocketFactory == null) {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs,
                                               String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs,
                                               String authType) {
                }
            }};
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, null);
                sslSocketFactory = sslContext.getSocketFactory();
            } catch (Throwable e) {
                LogUtils.e(e.getMessage(), e);
            }
        }

        if (sslSocketFactory != null) {
            HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
            HttpsURLConnection.setDefaultHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        }
    }
    
}
