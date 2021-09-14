
package com.nixian.http.client.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.Args;

/**
 * 增强了 HTTP Entity 
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @since [产品/模块版本] （可选）
 */
public class HttpEntityWrapper  implements SupportableHttpEntity,HttpEntity{

    protected HttpEntity wrappedEntity;
    
    private long contentLength = -1;
    
    private boolean supportWriteTo = false;

    private boolean windowsFile = false;
    
    public HttpEntityWrapper(final HttpEntity wrappedEntity) {
        super();
        this.wrappedEntity = Args.notNull(wrappedEntity, "Wrapped entity");
    } 

    public HttpEntity getEntity() {
        return this.wrappedEntity;
    }
    
    @Override
    public boolean isRepeatable() {
        return wrappedEntity.isRepeatable();
    }

    @Override
    public boolean isChunked() {
        return wrappedEntity.isChunked();
    }
    
    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    @Override
    public long getContentLength() {
        long length = contentLength;
        if(length>-1)
            length = wrappedEntity.getContentLength();
        return length;
    }

    @Override
    public Header getContentType() {
        return wrappedEntity.getContentType();
    }

    @Override
    public Header getContentEncoding() {
        return wrappedEntity.getContentEncoding();
    }

    @Override
    public InputStream getContent()
        throws IOException {
        return wrappedEntity.getContent();
    }

    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        wrappedEntity.writeTo(outstream);
    }

    @Override
    public boolean isStreaming() {
        return wrappedEntity.isStreaming();
    }

    @Override
    @Deprecated
    public void consumeContent() throws IOException {
        wrappedEntity.consumeContent();
    }
    
    public boolean setSupportWriteTo(boolean supportWriteTo) {
        this.supportWriteTo = supportWriteTo;
        return this.supportWriteTo;
    }

    public boolean setIsWindowsFile(boolean windowsFile) {
        this.windowsFile = windowsFile;
        return this.windowsFile;
    }
    
    @Override
    public boolean supportWriteTo() {
        return this.supportWriteTo;
    }
    
    @Override
    public boolean isWindowsFile() {
        return this.windowsFile;
    }
    
    
}
