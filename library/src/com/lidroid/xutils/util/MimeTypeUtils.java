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

import android.webkit.MimeTypeMap;

/**
 * MIME类型处理工具
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-7-26
 * Time: 下午2:31
 * </pre>
 */
public class MimeTypeUtils {

    private MimeTypeUtils() {
    }

    /**
     * 获取文件MIME类型
     * @param fileName 文件名（文件完整路径）
     * @return MIME类型{@link com.lidroid.xutils.http.client.multipart.MIME}
     */
    public static String getMimeType(final String fileName) {
        String result = "application/octet-stream";
        int extPos = fileName.lastIndexOf(".");
        if (extPos != -1) {
            String ext = fileName.substring(extPos + 1);
            result = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        }
        return result;
    }
    
}
