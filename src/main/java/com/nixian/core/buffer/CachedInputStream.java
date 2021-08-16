/*
 * Copyright (C), 2002-2021, 苏宁易购电子商务有限公司
 * FileName: CachedInputStream.java
 * Author:   19041969
 * Date:     2021年8月16日 下午1:42:23
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.core.buffer;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.io.BufferInfo;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.util.ContentInputBuffer;
import org.apache.http.util.Args;

import com.nixian.core.buffer.CachedBufferPool.Cached;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class CachedInputStream extends InputStream {
    
    private Cached cached;
    private ContentInputBuffer buffer;

    private final int checkMode = BufferUtil.OpenCheck;
    
    public CachedInputStream(Cached cached) throws IOException {
        super();
        this.cached = BufferUtil.checkBuffer(cached,this.checkMode);
        this.buffer = (ContentInputBuffer)cached.getCached();
    }
    
    public CachedInputStream() throws IOException {
        super();
        this.cached = BufferUtil.checkBuffer(null,this.checkMode);
        this.buffer = (ContentInputBuffer)cached.getCached();
    }

    public int consumeContent(final ContentDecoder decoder) throws IOException{
        return this.buffer.consumeContent(decoder);
    }
    
    @Override
    public int available() throws IOException {
        if (this.buffer instanceof BufferInfo) {
            return ((BufferInfo) this.buffer).length();
        } else {
            return super.available();
        }
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        return this.buffer.read(b, off, len);
    }

    @Override
    public int read(final byte[] b) throws IOException {
        if (b == null) {
            return 0;
        }
        return this.buffer.read(b, 0, b.length);
    }

    @Override
    public int read() throws IOException {
        return this.buffer.read();
    }

    @Override
    public void close() throws IOException {
        // read and discard the remainder of the message
        final byte tmp[] = new byte[1024];
        while (this.buffer.read(tmp, 0, tmp.length) >= 0) {
        }
        cached.free();
        super.close();
    }
    
    
    
}
