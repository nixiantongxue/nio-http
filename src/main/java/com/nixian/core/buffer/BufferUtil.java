/*
 * Copyright (C), 2002-2021, 苏宁易购电子商务有限公司
 * FileName: BufferUtil.java
 * Author:   19041969
 * Date:     2021年1月16日 下午5:16:54
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.core.buffer;

import java.io.IOException;

import com.nixian.core.buffer.CachedBufferPool.Cached;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class BufferUtil {
    
    final static int CareCheck = 0;
    final static int OpenCheck = 1;
    
    public static Cached checkBuffer(Cached buffer,int checkMode) throws IOException {
        if(buffer==null) {
            if(checkMode > CareCheck)
                return (buffer = CachedBufferPool.allocate());
            throw new BufferBeNullException();
        }
        return buffer;
    }
    
    public static class BufferBeNullException extends IOException{
        
        final static String error = "BufferUtil.BufferBeNullException: 所操作的Buffer 可能已经回收";
        
        BufferBeNullException(){
            super(error);
        }
    }
}
