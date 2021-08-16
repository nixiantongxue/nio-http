/*
http * Copyright (C), 2002-2021, 苏宁易购电子商务有限公司
 * FileName: Upload.java
 * Author:   19041969
 * Date:     2021年6月1日 下午8:16:38
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.http.client.param;

import java.io.File;
import java.io.InputStream;

import org.apache.http.entity.mime.content.ContentBody;

import com.nixian.http.client.mime.Mimes;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
abstract public class MultipartParam {
    
    String name;
    String mimeType = "application/octet-stream";
    String charset = "ISO-8859-1";
    String fileName;
    
    public MultipartParam() {
    }
    
    public MultipartParam(String name,String mimeType,String charset,String fileName) {
        this.name = name;
        this.mimeType = mimeType;
        this.charset = charset;
        this.fileName = fileName;
    }
    
    public abstract ContentBody getContentBody();
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public MultipartParam setName(String name) {
        this.name = name;
        return this;
    }
    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return Mimes.put(mimeType);
    }
    /**
     * @param mimeType the mimeType to set
     */
    public MultipartParam setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }
    /**
     * @return the charset
     */
    public String getCharset() {
        return charset;
    }
    /**
     * @param charset the charset to set
     */
    public MultipartParam setCharset(String charset) {
        this.charset = charset;
        return this;
    }
    
    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public MultipartParam setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }
    
    public static MultipartParam create(String name,String mimeType,String charset,String fileName,Object data){
        
        if (data instanceof java.io.InputStream) {
            return InputStreamUploadParam.create(name, mimeType, charset, fileName, data);
        } else if (data instanceof java.io.File) {
            return FileUploadParam.create(name, mimeType, charset, fileName, data);
        } else if(data instanceof byte[]){
            return ByteUploadParam.create(name, mimeType, charset, fileName, data);
        }
        throw new IllegalArgumentException("暂时不支持该类型data");
    }
    
    public static MultipartParam create(Object data){
        
        if (data instanceof java.io.InputStream) {
            return InputStreamUploadParam.create((InputStream)data);
        } else if (data instanceof java.io.File) {
            return FileUploadParam.create((File)data);
        } else if(data instanceof byte[]){
            return ByteUploadParam.create((byte[])data);
        }
        throw new IllegalArgumentException("暂时不支持该类型data");
    }
    
}
