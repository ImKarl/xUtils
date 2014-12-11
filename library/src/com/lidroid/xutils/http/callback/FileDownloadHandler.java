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

import android.text.TextUtils;
import com.lidroid.xutils.util.IOUtils;
import org.apache.http.HttpEntity;

import java.io.*;

/**
 * 网络文件下载处理器
 */
public class FileDownloadHandler {

    /**
     * 处理网络文件下载
     * 
     * <pre>
     * 保存到目标路径（target）；
     * 文件可重命名时，修改文件名（responseFileName）；
     * 下载失败时，返回null。
     * </pre>
     * 
     * @param entity 网络请求实体{@link org.apache.http.HttpEntity}
     * @param callBackHandler 网络请求进度更新通知接口{@link com.lidroid.xutils.http.callback.RequestCallBackHandler}
     * @param target 下载文件的目标路径（完整保存路径）
     * @param isResume 是否支持恢复
     * @param responseFileName 文件下载完成后的文件名（如果文件已存在，则添加当前时间戳前缀）
     * @return 保存到target的路径；文件可重命名时，修改文件名为responseFileName；下载失败时，返回null。{@link java.io.File}
     * @throws IOException IO读取异常{@link java.io.IOException}
     */
    public File handleEntity(HttpEntity entity,
                             RequestCallBackHandler callBackHandler,
                             String target,
                             boolean isResume,
                             String responseFileName) throws IOException {
        if (entity == null || TextUtils.isEmpty(target)) {
            return null;
        }

        File targetFile = new File(target);

        if (!targetFile.exists()) {
            File dir = targetFile.getParentFile();
            if (dir.exists() || dir.mkdirs()) {
                targetFile.createNewFile();
            }
        }

        long current = 0;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            FileOutputStream fileOutputStream = null;
            if (isResume) {
                current = targetFile.length();
                fileOutputStream = new FileOutputStream(target, true);
            } else {
                fileOutputStream = new FileOutputStream(target);
            }
            long total = entity.getContentLength() + current;
            bis = new BufferedInputStream(entity.getContent());
            bos = new BufferedOutputStream(fileOutputStream);

            if (callBackHandler != null && !callBackHandler.updateProgress(total, current, true)) {
                return targetFile;
            }

            byte[] tmp = new byte[4096];
            int len;
            while ((len = bis.read(tmp)) != -1) {
                bos.write(tmp, 0, len);
                current += len;
                if (callBackHandler != null) {
                    if (!callBackHandler.updateProgress(total, current, false)) {
                        return targetFile;
                    }
                }
            }
            bos.flush();
            if (callBackHandler != null) {
                callBackHandler.updateProgress(total, current, true);
            }
        } finally {
            IOUtils.closeQuietly(bis);
            IOUtils.closeQuietly(bos);
        }

        if (targetFile.exists() && !TextUtils.isEmpty(responseFileName)) {
            File newFile = new File(targetFile.getParent(), responseFileName);
            while (newFile.exists()) {
                newFile = new File(targetFile.getParent(), System.currentTimeMillis() + responseFileName);
            }
            return targetFile.renameTo(newFile) ? newFile : targetFile;
        } else {
            return targetFile;
        }
    }

}
