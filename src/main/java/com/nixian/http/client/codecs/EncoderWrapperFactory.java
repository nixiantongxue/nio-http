/*
 * Copyright (C), 2002-2021, nixian,email nixiantongxue@163.com
 * FileName: BaseLengthDelimitedEncoder.java
 * Author:   nixian
 * Date:     2021年1月11日 下午4:25:59
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.http.client.codecs;

import java.lang.reflect.Field;
import java.nio.channels.WritableByteChannel;

import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.impl.nio.codecs.AbstractContentEncoder;
import org.apache.http.impl.nio.codecs.ChunkEncoder;
import org.apache.http.impl.nio.codecs.IdentityEncoder;
import org.apache.http.impl.nio.codecs.LengthDelimitedEncoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.reactor.SessionOutputBuffer;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @since [产品/模块版本] （可选）
 */
public class EncoderWrapperFactory extends CachebleFactory{

    
    static public BestEncoder build(ContentEncoder encoder) throws Exception {
  
        return build(encoder,new Wrapper() {
            @Override
            public BestEncoder wrap(ContentEncoder encoder) throws Exception {
                Class encoderClass = encoder.getClass();
                long contentLength = 0l;
                
                Field f1 = encoder.getClass().getSuperclass().getDeclaredField("channel");
                f1.setAccessible(true);
                WritableByteChannel channel = (WritableByteChannel) f1.get(encoder);
                
                Field f2 = encoder.getClass().getSuperclass().getDeclaredField("buffer");
                f2.setAccessible(true);
                SessionOutputBuffer sessionOutputBuffer = (SessionOutputBuffer) f2.get(encoder);
                
                Field f3 = encoder.getClass().getSuperclass().getDeclaredField("metrics");
                f3.setAccessible(true);
                HttpTransportMetricsImpl metrics = (HttpTransportMetricsImpl) f3.get(encoder);
                
                Field f4 = encoder.getClass().getSuperclass().getDeclaredField("completed");
                f4.setAccessible(true);
                boolean completed = (boolean) f4.get(encoder);
                
                if(encoderClass.equals(LengthDelimitedEncoder.class)) {
                    Field f5 = encoder.getClass().getDeclaredField("contentLength");
                    f5.setAccessible(true);
                    contentLength = (long) f5.get(encoder);
                }
                
                Field f6 = encoder.getClass().getDeclaredField("fragHint");
                f6.setAccessible(true);
                int fragementSizeHint = (int) f6.get(encoder);
                
                return prepareEncoder(channel,sessionOutputBuffer,metrics,contentLength,fragementSizeHint,completed,encoder,encoderClass);
         
            }
        });
               
//        return new EncoderWrapper(prepareEncoder(channel,sessionOutputBuffer,metrics,contentLength,fragementSizeHint,completed,encoder,encoderClass),encoder);
    }
    
    /**
     * @param encoder
     */
    public EncoderWrapperFactory(ContentEncoder encoder) {
        try {
            build(encoder);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }catch (Exception ee) {
            ee.printStackTrace();
        }
    }
    
    protected static BestEncoder prepareEncoder(WritableByteChannel channel, SessionOutputBuffer buffer,
            HttpTransportMetricsImpl metrics, long contentLength, int fragementSizeHint,boolean completed,ContentEncoder encoder,Class klass)
    {
        if(contentLength>0)
            return new BestLengthDelimitedEncoder(channel,buffer,metrics,contentLength,fragementSizeHint,encoder);
        if(klass.equals(ChunkEncoder.class))
            return new BestChunkEncoder(channel,buffer,metrics,fragementSizeHint,encoder);
        else if(klass.equals(IdentityEncoder.class))
            return new BestIdentityEncoder(channel,buffer,metrics,fragementSizeHint,encoder);
        throw new IllegalArgumentException("不支持额外的内容编码");
    }


}
