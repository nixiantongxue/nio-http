package com.nixian.http.client.entity;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.util.Args;

import com.nixian.core.buffer.CachedInputStream;


public class ContentCachedEntity extends BasicHttpEntity {

    private final HttpEntity wrappedEntity;

    /**
     * Creates new instance of ContentBufferEntity.
     *
     * @param entity the original entity.
     * @param buffer the content buffer.
     * @throws IOException 
     */
    public ContentCachedEntity(final HttpEntity entity) throws IOException {
        super();
        Args.notNull(entity, "HTTP entity");
        this.wrappedEntity = entity;
        setContent(new CachedInputStream());
    }
    
    public int consumeContent(final ContentDecoder decoder) throws IOException{
        return  ((CachedInputStream)getContent()).consumeContent(decoder);
    }

    @Override
    public boolean isChunked() {
        return this.wrappedEntity.isChunked();
    }

    @Override
    public long getContentLength() {
        return this.wrappedEntity.getContentLength();
    }

    @Override
    public Header getContentType() {
        return this.wrappedEntity.getContentType();
    }

    @Override
    public Header getContentEncoding() {
        return this.wrappedEntity.getContentEncoding();
    }

}