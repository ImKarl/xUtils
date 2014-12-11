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

package com.lidroid.xutils.http.callback;

import com.lidroid.xutils.util.IOUtils;
import com.lidroid.xutils.util.OtherUtils;
import org.apache.http.HttpEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 网络文本数据下载处理器
 */
public class StringDownloadHandler {

    /**
     * 处理网络文本数据下载
     * 
     * @param entity 网络请求实体{@link org.apache.http.HttpEntity}
     * @param callBackHandler 网络请求进度更新通知接口{@link com.lidroid.xutils.http.callback.RequestCallBackHandler}
     * @param charset 字符编码
     * @return 下载得到的文本数据（下载失败时，返回null）
     * @throws IOException IO读取异常{@link java.io.IOException}
     */
    public String handleEntity(HttpEntity entity, RequestCallBackHandler callBackHandler, String charset) throws IOException {
        if (entity == null) return null;

        long current = 0;
        long total = entity.getContentLength();

        if (callBackHandler != null && !callBackHandler.updateProgress(total, current, true)) {
            return null;
        }

        InputStream inputStream = null;
        StringBuilder sb = new StringBuilder();
        try {
            inputStream = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
                current += OtherUtils.sizeOfString(line, charset);
                if (callBackHandler != null) {
                    if (!callBackHandler.updateProgress(total, current, false)) {
                        break;
                    }
                }
            }
            if (callBackHandler != null) {
                callBackHandler.updateProgress(total, current, true);
            }
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return sb.toString().trim();
    }

}
