/*
 * Copyright (C), 2002-2021, nixian,email nixiantongxue@163.com
 * FileName: BeseIdentityEncoder.java
 * Author:   nixian
 * Date:     2021年1月27日 下午1:48:28
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
import org.apache.http.impl.nio.codecs.IdentityEncoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.reactor.SessionOutputBuffer;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @since [产品/模块版本] （可选）
 */
public class BestIdentityEncoder extends IdentityEncoder implements BestEncoder{
    
    private WritableByteChannel channel = null;
    private ContentEncoder original;
    
    public BestIdentityEncoder(WritableByteChannel channel, SessionOutputBuffer buffer,
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
    public void complete() throws IOException{
        if(complete(BestEncoder.CompleteBody) == BestEncoder.CompleteEnd && this instanceof Cacheable) {
            this.remove();
        }
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
