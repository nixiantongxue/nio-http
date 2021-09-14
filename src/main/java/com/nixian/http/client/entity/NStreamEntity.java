/*
 * Copyright (C), 2002-2021, nixian,email nixiantongxue@163.com
 * FileName: NStreamEntity.java
 * Author:   nixian
 * Date:     2021年1月26日 下午6:35:27
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.http.client.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.HttpAsyncContentProducer;
import org.apache.http.nio.entity.ProducingNHttpEntity;

import com.nixian.core.buffer.CachedBufferPool;
import com.nixian.core.buffer.CachedBufferPool.BufferType;
import com.nixian.core.buffer.CachedBufferPool.Cached;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @since [产品/模块版本] （可选）
 */
public class NStreamEntity extends AbstractHttpEntity
                           implements HttpAsyncContentProducer, ProducingNHttpEntity, SupportableHttpEntity {

    private static final int TRANSFER_SIZE = 4096;
    private InputStream in;
    
    public NStreamEntity(InputStream in,ContentType contentType) {
        super();
        this.in = in;
        setContentType(contentType.toString());
    }
    /* (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() throws IOException {
        if(null!=in)
            in.close();
    }

    /* (non-Javadoc)
     * @see org.apache.http.HttpEntity#getContentLength()
     */
    @Override
    public long getContentLength() {
        return -1;
    }

    /* (non-Javadoc)
     * @see org.apache.http.HttpEntity#getContent()
     */
    @Override
    public InputStream getContent() throws IOException, UnsupportedOperationException {
        return in;
    }

    /* (non-Javadoc)
     * @see org.apache.http.HttpEntity#writeTo(java.io.OutputStream)
     */
    @Override
    public void writeTo(OutputStream outstream) throws IOException {
        if(null==in)
            throw new IllegalStateException("Input stream may not be null"); 
        
        if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        final byte[] buff = new byte[TRANSFER_SIZE];
        int l;
        // consume until EOF
        while ((l = in.read(buff)) != -1) {
            outstream.write(buff, 0, l);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.http.HttpEntity#isStreaming()
     */
    @Override
    public boolean isStreaming() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.http.nio.entity.ProducingNHttpEntity#finish()
     */
    @Override
    public void finish() throws IOException {
        close();
    }

    /* (non-Javadoc)
     * @see org.apache.http.nio.entity.HttpAsyncContentProducer#produceContent(org.apache.http.nio.ContentEncoder, org.apache.http.nio.IOControl)
     */
    @Override
    public void produceContent(ContentEncoder encoder, IOControl ioctrl) throws IOException {
        Cached cd = null;
        
        try {
            
            ByteBuffer buffer =  (ByteBuffer)(cd= CachedBufferPool.allocate(TRANSFER_SIZE,BufferType.NATIVE)).getCached();
            int i;

            int len = buffer.remaining();
            int totalRead = 0;
            int bytesRead = 0;
            byte buf[] = new byte[0];
            while(bytesRead!=-1) {
                
                while (totalRead < len) {
                    int bytesToRead = Math.min((len - totalRead),TRANSFER_SIZE);
                    if (buf.length < bytesToRead)
                        buf = new byte[bytesToRead];
                    if ((totalRead > 0) && !(in.available() > 0))
                        break; 

                    bytesRead = in.read(buf, 0, bytesToRead);
                    if (bytesRead < 0)
                        break;
                    else
                        totalRead += bytesRead;
                    buffer.put(buf, 0, bytesRead);
                    buffer.flip();
                }
                encoder.write(buffer);
                buffer.clear();
                
            }
            
            
            
            
        }finally {
            if(null!=cd)
                cd.free();
        }
        
    }

    /* (non-Javadoc)
     * @see org.apache.http.nio.entity.HttpAsyncContentProducer#isRepeatable()
     */
    @Override
    public boolean isRepeatable() {
        return false;
    }
    
    @Override
    public boolean supportWriteTo() {
        return true;
    }
    
    @Override
    public boolean isWindowsFile() {
        return false;
    }
    
    
    
}
