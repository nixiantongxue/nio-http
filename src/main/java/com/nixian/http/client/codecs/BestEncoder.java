/*
 * Copyright (C), 2002-2021, nixian,email nixiantongxue@163.com
 * FileName: ZeroCppyEncoder.java
 * Author:   nixian
 * Date:     2021年1月13日 下午4:45:16
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.http.client.codecs;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.http.nio.ContentEncoder;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @since [产品/模块版本] （可选）
 */
public interface BestEncoder extends ContentEncoder, Cacheable<ContentEncoder>{
    
    public final static int CompleteHead = 0;
    public final static int CompleteBody = 1;
    public final static int CompleteEnd = 2;
    
    public int complete(int step) throws IOException;
    
    public int completeStep();
    
    public int resetStep();
    
    public int write0(ByteBuffer src) throws IOException;
    
    public static int write(ByteBuffer src,ContentEncoder encoder) throws IOException{
        if(encoder instanceof BestEncoder) {
            return ((BestEncoder)encoder).write0(src);
        }
        return encoder.write(src);
    }
    
    
}
