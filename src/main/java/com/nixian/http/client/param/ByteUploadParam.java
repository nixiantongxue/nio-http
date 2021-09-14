/*
 * Copyright (C), 2002-2021, nixian,email nixiantongxue@163.com
 * FileName: FileUploadParam.java
 * Author:   nixian
 * Date:     2021年6月9日 下午4:59:38
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.http.client.param;

import java.io.File;

import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @since [产品/模块版本] （可选）
 */
public class ByteUploadParam extends MultipartParam {
    
    byte[] data;

    public static ByteUploadParam create(byte[] data){
        return new ByteUploadParam(data);
    }

    public static ByteUploadParam create(String name,String mimeType,String charset,String fileName,byte[] data) {
        return new ByteUploadParam(name, mimeType,charset,fileName,data);
    }
    
    public ByteUploadParam(String name,String mimeType,String charset,String fileName,byte[] data) {
        super(name, mimeType,charset,fileName);
        this.data = data;
    }
    
    public ByteUploadParam(byte[] data) {
        this.data = data;
    }
    
    public ContentBody getContentBody() {
        return new ByteArrayBody((byte[])data,mimeType,fileName);
    }
    

    
}
