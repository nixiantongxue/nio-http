/*
 * Copyright (C), 2002-2021, 苏宁易购电子商务有限公司
 * FileName: MultipartBufferImpl.java
 * Author:   19041969
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

import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.nio.util.ExpandableBuffer;
import org.apache.http.util.Args;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class MultipartBufferImpl extends ExpandableBuffer implements MultipartBuffer{
    
    
    /**
     * @param buffersize
     * @param allocator
     */
    public MultipartBufferImpl(int buffersize, ByteBufferAllocator allocator) {
        super(buffersize, allocator);
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
