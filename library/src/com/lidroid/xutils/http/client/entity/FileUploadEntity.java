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

package com.lidroid.xutils.http.client.entity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import org.apache.http.entity.FileEntity;

import com.lidroid.xutils.http.callback.RequestCallBackHandler;
import com.lidroid.xutils.util.IOUtils;

/**
 * 文件上传参数实体
 * 
 * <pre>
 * Created with IntelliJ IDEA.
 * User: wyouflf
 * Date: 13-6-24
 * Time: 下午4:45
 * </pre>
 * 
 * @author wyouflf
 */
public class FileUploadEntity extends FileEntity implements UploadEntity {

    /**
     * 构造文件上传参数实体
     * @param file 要上传的文件
     * @param contentType 内容类型（CONTENT-TYPE）
     */
    public FileUploadEntity(File file, String contentType) {
        super(file, contentType);
        fileSize = file.length();
    }

    private long fileSize;
    private long uploadedSize = 0;

    /**
     * 将参数内容写入到输出流
     * @param outStream IO输出流 {@link java.io.OutputStream}
     * @throws IOException IO流操作异常{@link java.io.IOException}
     */
    @Override
    public void writeTo(OutputStream outStream) throws IOException {
        if (outStream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        BufferedInputStream inStream = null;
        try {
            inStream = new BufferedInputStream(new FileInputStream(this.file));
            byte[] tmp = new byte[4096];
            int len;
            while ((len = inStream.read(tmp)) != -1) {
                outStream.write(tmp, 0, len);
                uploadedSize += len;
                if (callBackHandler != null) {
                    if (!callBackHandler.updateProgress(fileSize, uploadedSize, false)) {
                        throw new InterruptedIOException("cancel");
                    }
                }
            }
            outStream.flush();
            if (callBackHandler != null) {
                callBackHandler.updateProgress(fileSize, uploadedSize, true);
            }
        } finally {
            IOUtils.closeQuietly(inStream);
        }
    }

    private RequestCallBackHandler callBackHandler = null;

    @Override
    public void setCallBackHandler(RequestCallBackHandler callBackHandler) {
        this.callBackHandler = callBackHandler;
    }
    
}