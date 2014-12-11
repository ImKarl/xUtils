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

package com.lidroid.xutils.http.client.multipart.content;

import com.lidroid.xutils.http.client.multipart.MultipartEntity;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 网络请求的内容主体接口
 * 
 * @since 4.0
 */
public interface ContentBody extends ContentDescriptor {

    /**
     * 获取文件名
     * @return 文件名
     */
    String getFilename();

    /**
     * 将内容写入到输出流
     * @param out IO输出流 {@link java.io.OutputStream}
     * @throws IOExceptionIO流操作异常{@link java.io.IOException}
     */
    void writeTo(OutputStream out) throws IOException;

    /**
     * 设置上传监听器
     * @param callBackInfo 上传的回调函数{@link com.lidroid.xutils.http.client.multipart.MultipartEntity.CallBackInfo}
     */
    void setCallBackInfo(MultipartEntity.CallBackInfo callBackInfo);

}
