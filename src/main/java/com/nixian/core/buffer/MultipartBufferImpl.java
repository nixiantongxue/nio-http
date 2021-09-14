/*
 * Copyright (C), 2002-2021, nixian,email nixiantongxue@163.com
 * FileName: MultipartBufferImpl.java
 * Author:   nixian
 * Date:     2021年1月12日 下午6:53:03
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.core.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.nio.util.ContentInputBuffer;
import org.apache.http.nio.util.ExpandableBuffer;
import org.apache.http.util.Args;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @since [产品/模块版本] （可选）
 */
public class MultipartBufferImpl extends ExpandableBuffer implements MultipartBuffer,ContentInputBuffer{
    
    private boolean endOfStream = false;
    /**
     * @param buffersize
     * @param allocator
     */
    public MultipartBufferImpl(int buffersize, ByteBufferAllocator allocator) {
        super(buffersize, allocator);
    }

    
    @Override
    public void reset() {
        this.endOfStream = false;
        super.clear();
    }

    @Override
    public int consumeContent(final ContentDecoder decoder) throws IOException {
        setInputMode();
        int totalRead = 0;
        int bytesRead;
        while ((bytesRead = decoder.read(this.buffer)) != -1) {
            if (bytesRead == 0) {
                if (!this.buffer.hasRemaining()) {
                    expand();
                } else {
                    break;
                }
            } else {
                totalRead += bytesRead;
            }
        }
        if (bytesRead == -1 || decoder.isCompleted()) {
            this.endOfStream = true;
        }
        return totalRead;
    }

    public boolean isEndOfStream() {
        return !hasData() && this.endOfStream;
    }

    @Override
    public int read() throws IOException {
        if (isEndOfStream()) {
            return -1;
        }
        setOutputMode();
        return this.buffer.get() & 0xff;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        if (isEndOfStream()) {
            return -1;
        }
        if (b == null) {
            return 0;
        }
        setOutputMode();
        int chunk = len;
        if (chunk > this.buffer.remaining()) {
            chunk = this.buffer.remaining();
        }
        this.buffer.get(b, off, chunk);
        return chunk;
    }

    public int read(final byte[] b) throws IOException {
        if (isEndOfStream()) {
            return -1;
        }
        if (b == null) {
            return 0;
        }
        return read(b, 0, b.length);
    }

    public void shutdown() {
        this.endOfStream = true;
    }
    
    
    
    
    
    
    
    
    public void write(final ByteBuffer src) {
        if (src == null) {
            return;
        }
        setInputMode();
        final int requiredCapacity = this.buffer.position() + src.remaining();
        ensureCapacity(requiredCapacity);
        this.buffer.put(src);
    }
    
    public int flush(final WritableByteChannel channel) throws IOException {
        Args.notNull(channel, "Channel");
        setOutputMode();
        return channel.write(this.buffer);
    }
    
    public int flush(ContentEncoder encoder) throws IOException {
        Args.notNull(encoder, "Encoder");
        setOutputMode();
        return encoder.write(this.buffer);
    }

    public void claer() {
        buffer.limit(0);
    }
    
}
