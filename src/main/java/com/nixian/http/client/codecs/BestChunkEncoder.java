/*
 * Copyright (C), 2002-2021, 苏宁易购电子商务有限公司
 * FileName: BestIdentityEncoder.java
 * Author:   19041969
 * Date:     2021年1月27日 下午2:00:24
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.http.client.codecs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.impl.nio.codecs.ChunkEncoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.reactor.SessionOutputBuffer;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class BestChunkEncoder  extends ChunkEncoder implements BestEncoder{

    private WritableByteChannel channel;
    private ContentEncoder original;
    
    public BestChunkEncoder(WritableByteChannel channel,SessionOutputBuffer buffer,
            HttpTransportMetricsImpl metrics, int fragementSizeHint,ContentEncoder original) {
        super(channel, buffer, metrics, fragementSizeHint);
        this.channel = channel;
        this.original = original;
    }
    
    public ContentEncoder key() {
        return this.original;
    }
    
    public void remove() {
        CachebleFactory.remove(this.original);
    }
    
    protected int flushToChannel() throws IOException {
        return super.flushToChannel();
    }
    
    public int write0(ByteBuffer src) throws IOException {
        flushToChannel();
        
        if(null!=channel) {
            return channel.write(src);
        }
        
        return 0;
    }
    
    @Override
    public int write(ByteBuffer src) throws IOException {
        return super.write(src);
    }

    private int competeStep = BestEncoder.CompleteHead;
    
    public int complete(int step) throws IOException {
        complete();
        if(this instanceof Cacheable) {
            this.remove();
        }
        return BestEncoder.CompleteEnd;
    }
    
    @Override
    public int completeStep() {
        return competeStep;
    }
    
    @Override
    public int resetStep() {
        competeStep = BestEncoder.CompleteHead;
        return competeStep;
    }
    
}
