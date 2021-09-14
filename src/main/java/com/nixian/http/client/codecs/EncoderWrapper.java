/*
 * Copyright (C), 2002-2021, nixian,email nixiantongxue@163.com
 * FileName: ContentEncoderWrapper.java
 * Author:   nixian
 * Date:     2021年1月11日 下午3:22:07
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.http.client.codecs;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.impl.nio.codecs.AbstractContentEncoder;
import org.apache.http.impl.nio.codecs.ChunkEncoder;
import org.apache.http.impl.nio.codecs.IdentityEncoder;
import org.apache.http.impl.nio.codecs.LengthDelimitedEncoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.reactor.SessionOutputBuffer;
import org.apache.http.util.Asserts;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @since [产品/模块版本] （可选）
 */
public class EncoderWrapper implements ContentEncoder{

    private AbstractContentEncoder original = null;
    private ContentEncoder wapper = null;
    
    /* (non-Javadoc)
     * @see org.apache.http.nio.ContentEncoder#write(java.nio.ByteBuffer)
     */
    public EncoderWrapper(AbstractContentEncoder encoder) {
        this.original = encoder;
        this.wapper = (ContentEncoder) Proxy.newProxyInstance(encoder.getClass().getClassLoader(), 
                encoder.getClass().getInterfaces(), new EncoderHandler(encoder));
    }
    
    public EncoderWrapper(BestEncoder bestEncoder,AbstractContentEncoder encoder) {
        this.original = encoder;
        this.wapper = (ContentEncoder)bestEncoder;
    }
    
    
    @Override
    public int write(ByteBuffer src) throws IOException {
        assertNotInitalied();
        return BestEncoder.write(src, this.wapper);
    }

    /* (non-Javadoc)
     * @see org.apache.http.nio.ContentEncoder#complete()
     */
    @Override
    public void complete() throws IOException {
        assertNotInitalied();
        this.original.complete();
        wapper.complete();
    }

    /* (non-Javadoc)
     * @see org.apache.http.nio.ContentEncoder#isCompleted()
     */
    @Override
    public boolean isCompleted() {
        assertNotInitalied();
        return wapper.isCompleted();
    }

    protected void assertNotInitalied() {
        Asserts.check(null!=this.original, "original has not been initalied");
    }
    
    
    
    
    
    
    
    
    
    
    
}
