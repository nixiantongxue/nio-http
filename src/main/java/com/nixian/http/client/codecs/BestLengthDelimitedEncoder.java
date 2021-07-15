/*
 * Copyright (C), 2002-2021, 苏宁易购电子商务有限公司
 * FileName: BaseLengthDelimitContentEncoder.java
 * Author:   19041969
 * Date:     2021年1月11日 下午5:42:26
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.http.client.codecs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.impl.nio.codecs.LengthDelimitedEncoder;
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
public class BestLengthDelimitedEncoder extends LengthDelimitedEncoder implements BestEncoder{

    private WritableByteChannel channel = null;
    private long remaining;
    private ContentEncoder original;
    
    /**
     * @param channel
     * @param buffer
     * @param metrics
     * @param contentLength
     * @param fragementSizeHint
     */
    public BestLengthDelimitedEncoder(WritableByteChannel channel, SessionOutputBuffer buffer,
            HttpTransportMetricsImpl metrics, long contentLength, int fragementSizeHint,ContentEncoder original) {
        super(channel, buffer, metrics, contentLength, fragementSizeHint);
        this.channel = channel;
        this.remaining = contentLength;
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
            int tw = 0;
            int rw = src.remaining();
            int po = src.position();
            for(;;) {
                int i = 0;
                int nw = channel.write(src);
                if((tw+=nw)==rw)
                    return tw;
                src.position(po+tw);
                try {
                    Thread.sleep(++i);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        return 0;
    }
    
//    @Override
//    public long transfer(
//            final FileChannel src,
//            final long position,
//            final long count) throws IOException
//    {
//        long position0 = position;
////        for(;!isCompleted();) {
//            try {
//                if(position0/80960>0)
//                    Thread.sleep(position0/80960);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//            position0 += super.transfer(src, position0, count);
////        }
//        return position0;
//    }
    
    @Override
    public int write(ByteBuffer src) throws IOException {
        int w =super.write(src);
        return w;
    }

    private int competeStep = BestEncoder.CompleteHead;
    
    @Override
    public int complete(int step) throws IOException {
        if(step==competeStep && competeStep==BestEncoder.CompleteBody){
            return competeStep; 
        }
        
        if(step-competeStep==1 && ++competeStep<=BestEncoder.CompleteEnd) {
            if(competeStep==BestEncoder.CompleteEnd) super.complete();
        }else
            throw new IllegalStateException("BestEnCoder 使用过程中状态错误 检查是否按照指定步骤");
        return competeStep;
    }
    
    @Override
    public int completeStep() {
        return competeStep;
    }
    
    @Override
    public void complete() throws IOException{
        if(complete(BestEncoder.CompleteBody) == BestEncoder.CompleteEnd && this instanceof Cacheable) {
            this.remove();
        }
    } 
    
    @Override
    public int resetStep() {
        competeStep = BestEncoder.CompleteHead;
        return competeStep;
    }
    
    @Override
    public long transfer(
            final FileChannel src,
            final long position,
            final long count) throws IOException {

        if (src == null) {
            return 0;
        }
        assertNotCompleted();

        flushToChannel();
        if (this.buffer.hasData()) {
            return 0;
        }

        final long chunk = Math.min(this.remaining, count);
        final long bytesWritten = src.transferTo(position, chunk, this.channel);
        if (bytesWritten > 0) {
            this.metrics.incrementBytesTransferred(bytesWritten);
        }
        this.remaining -= bytesWritten;
        if (this.remaining <= 0) {
            complete();
        }
        return bytesWritten;
    }

}
