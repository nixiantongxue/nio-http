/*
 * Copyright (C), 2002-2021, 苏宁易购电子商务有限公司
 * FileName: ContentBodyWrapper.java
 * Author:   19041969
 * Date:     2021年2月4日 下午3:15:05
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.http.client.entity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.AbstractContentBody;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.ProducingNHttpEntity;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class ContentBodyWrapper extends AbstractContentBody{
    
    private ContentBody acb;
    private ProducingNHttpEntity pne;
    
    public static ContentBodyWrapper wrap(ContentBody contentBody) throws IOException{
        
        if(contentBody instanceof ContentBodyWrapper) {
            return (ContentBodyWrapper)contentBody;
        }
        
        Object data = null;
        try {
            if(contentBody instanceof FileBody) {
                data = ((FileBody)contentBody).getFile();
            }else if(contentBody instanceof InputStreamBody) {
                Field f3 = contentBody.getClass().getDeclaredField("in");
                f3.setAccessible(true);
                data = (InputStream) f3.get(contentBody);
            }else if(contentBody instanceof ByteArrayBody) {
                Field f3 = contentBody.getClass().getDeclaredField("data");
                f3.setAccessible(true);
                data = (byte[]) f3.get(contentBody);
            }else if(contentBody instanceof StringBody){
                Field f3 = contentBody.getClass().getDeclaredField("content");
                f3.setAccessible(true);
                data = (byte[]) f3.get(contentBody);
            }else {
                throw new IllegalArgumentException("不支持的类型contentBody");
            }
            
            return ContentBodyWrapper.wrap(contentBody.getMimeType(), data, contentBody.getFilename(),0, contentBody.getContentLength(),contentBody.getCharset(),contentBody);
        
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new IOException("入参错误",e);
        }catch(ClassNotFoundException cnf) {
            throw new IOException("引入错误",cnf);
        }
    }
    
    public static  ContentBodyWrapper wrap(String mimeType,Object data,String fileName,long blockSize,long length,String charSet) throws IOException, ClassNotFoundException {
        
        ContentBody contentBody;
        
        ContentBodyWrapper inputBody;
        
        if (Class.forName("java.io.InputStream").isInstance(data)) {
            contentBody = new InputStreamBody((InputStream)data,mimeType,fileName);
            inputBody = new ContentBodyWrapper(mimeType,(InputStream)data,fileName,blockSize,length,charSet,contentBody) {
                public long getContentLength() {
                    return length;
                }
            };
        } else if (Class.forName("java.io.File").isInstance(data)) {
            contentBody = new FileBody((File)data,fileName,mimeType,charSet);
            inputBody = new ContentBodyWrapper(mimeType,(File)data,fileName,blockSize,length,charSet,contentBody);
        } else {
            contentBody = new ByteArrayBody((byte[])data,fileName,mimeType);
            inputBody = new ContentBodyWrapper(mimeType,(byte[])data,fileName,blockSize,length,charSet,contentBody);
        }
        return inputBody;
    }
    
    
    public static  ContentBodyWrapper wrap(String mimeType,Object data,String fileName,long blockSize,long length,String charSet,ContentBody contentBody) throws IOException, ClassNotFoundException {
        
        ContentBodyWrapper inputBody;
        
        if (Class.forName("java.io.InputStream").isInstance(data)) {
            inputBody = new ContentBodyWrapper(mimeType,(InputStream)data,fileName,blockSize,length,charSet,contentBody) {
                public long getContentLength() {
                    return length;
                }
            };
        } else if (Class.forName("java.io.File").isInstance(data)) {
            inputBody = new ContentBodyWrapper(mimeType,(File)data,fileName,blockSize,length,charSet,contentBody);
        } else {
            inputBody = new ContentBodyWrapper(mimeType,(byte[])data,fileName,blockSize,length,charSet,contentBody);
        }
        return inputBody;
    }
    
    
    private ContentBodyWrapper(String mimeType,File data,String fileName,long blockSize,long length,String charSet,ContentBody contentBody) throws  IOException {
        super(mimeType);
        this.acb = contentBody;
        this.pne = new NHttpEntity(data,ContentType.create(mimeType,charSet));
    }
    
    private ContentBodyWrapper(String mimeType,byte[] data,String fileName,long blockSize,long length,String charSet,ContentBody contentBody) throws ClassNotFoundException, IOException {
        super(mimeType);
        this.acb = contentBody;
        this.pne = new NHttpEntity(data,ContentType.create(mimeType,charSet)); 
    }
    
    private ContentBodyWrapper(String mimeType,java.io.InputStream data,String fileName,long blockSize,long length,String charSet,ContentBody contentBody) throws ClassNotFoundException, IOException {
        super(mimeType);
        this.acb = contentBody;
        this.pne = new NHttpEntity(data,ContentType.create(mimeType,charSet)); 
    }
    
    public void produceContent(
            final ContentEncoder encoder,
            final IOControl ioctrl) throws IOException{
        this.pne.produceContent(encoder, ioctrl);
    }
    
    @Override
    public String getFilename() {
        return this.acb.getFilename();
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        this.acb.writeTo(out);
    }

    @Override
    public String getCharset() {
        return this.acb.getCharset();
    }

    @Override
    public String getTransferEncoding() {
        return this.acb.getTransferEncoding();
    }

    @Override
    public long getContentLength() {
        return this.acb.getContentLength();
    }
    
}
