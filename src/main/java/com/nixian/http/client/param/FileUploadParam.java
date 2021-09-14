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

import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.util.TextUtils;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @since [产品/模块版本] （可选）
 */
public class FileUploadParam extends MultipartParam {
    
    File file;
    
    public static FileUploadParam create(File file){
        return new FileUploadParam(file);
    }

    public static FileUploadParam create(String name,String mimeType,String charset,String fileName,File data) {
        return new FileUploadParam(name, mimeType,charset,fileName,data);
    }
    
    public FileUploadParam(String name,String mimeType,String charset,String fileName,File data) {
        super(name, mimeType,charset,fileName);
        this.file = data;
        if(StringUtils.isEmpty(fileName))
            this.fileName = file.getName();
    }
    
    public FileUploadParam(File file) {
        this.file  = file;
    }
    
    public ContentBody getContentBody() {
        this.fileName = TextUtils.isBlank(this.fileName) ? file.getName() : this.fileName;
        return new FileBody(this.file,this.fileName,getMimeType(), getCharset());
    }
    
}
