/*
 * Copyright (C), 2002-2021, nixian,email nixiantongxue@163.com
 * FileName: MultipartBuffer.java
 * Author:   nixian
 * Date:     2021年1月12日 下午6:59:49
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

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @since [产品/模块版本] （可选）
 */
public interface MultipartBuffer{
    
    public void write(final ByteBuffer src);
    
    public int flush(final WritableByteChannel channel) throws IOException ;
    
    public int flush(ContentEncoder encoder) throws IOException;
    
    public int available();
}
