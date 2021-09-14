/*
 * Copyright (C), 2002-2021, nixian,email nixiantongxue@163.com
 * FileName: MultipartBufferComposite.java
 * Author:   nixian
 * Date:     2021年1月12日 下午6:59:07
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.core.buffer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.nio.ContentEncoder;

import com.nixian.core.buffer.CachedBufferPool.Cached;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @since [产品/模块版本] （可选）
 */
public class MultipartBufferPair {

    private Cached buffer1 = CachedBufferPool.allocate();
    private Cached buffer2 = CachedBufferPool.allocate();

    private final int checkMode = BufferUtil.OpenCheck;
//    private Cached buffer3 = CachedBufferPool.allocate(4096);
//    private Cached buffer4 = CachedBufferPool.allocate(4096);

    public static MultipartBufferComposite create() {
        return new MultipartBufferPair().new MultipartBufferComposite();
    }
    
    
    private void checkPair(MultipartBufferPair pair) throws IOException  {
        if(null==pair)
            throw new BufferPairBeNullException();
    }
    
    public void writeHead(ByteBuffer b) throws IOException {
        buffer1 = BufferUtil.checkBuffer(buffer1,this.checkMode);
        ((MultipartBuffer)buffer1.getCached()).write(b);
    }
    
    public void writeFoot(ByteBuffer b) throws IOException  {
        buffer2 = BufferUtil.checkBuffer(buffer2,this.checkMode);
        ((MultipartBuffer)buffer2.getCached()).write(b);
    }
    
    public int flushHead(ContentEncoder encoder) throws IOException {
        int fh = 0;
        buffer1 = BufferUtil.checkBuffer(buffer1,this.checkMode);
        try {
            fh = ((MultipartBuffer)buffer1.getCached()).flush(encoder);
        }finally{
            CachedBufferPool.freeCached(buffer1);
            buffer1 = null;
        }
        
        return fh;
    }
    
    public int flushFoot(ContentEncoder encoder) throws IOException {
        int ff = 0;
        buffer2 = BufferUtil.checkBuffer(buffer2,this.checkMode);
        try {
            ff = ((MultipartBuffer)buffer2.getCached()).flush(encoder);
        }finally{
            CachedBufferPool.freeCached(buffer2);
            buffer2 = null;
        }
        return ff;
    }
    
    public boolean isClean() {
        return null==buffer1 && null == buffer2;
    }
    
    public boolean close() {
        if(null!=buffer1)
        {
            buffer1.free();
            buffer1 = null;
        }
        
        if(null!=buffer2)
        {
            buffer2.free();
            buffer2 = null;
        }
        
        return isClean();
    }
    
    protected long getLength() {
        return ((MultipartBufferImpl)buffer1.getCached()).length()+
                ((MultipartBufferImpl)buffer2.getCached()).length();
    }
    
    public static class BufferPairBeNullException extends IOException{
        
        final static String error = "MultipartBufferPair.BufferBeNullException: 所操作的BufferPair 可能已经回收";
        
        BufferPairBeNullException(){
            super(error);
        }
    }
    
    public class MultipartBufferComposite{
        
        private Map<String,MultipartBufferPair> composite = new HashMap<String,MultipartBufferPair>(8);
        private Cached common = CachedBufferPool.allocate();  
        
        private volatile String currentName = null;
        private volatile boolean isAvailable = false;
        
        public void end(ByteBuffer b) {
            b.rewind();
            if(null==common) common = CachedBufferPool.allocate();
            ((MultipartBuffer)common.getCached()).write(b);
        }
        
        public String currentName() {
            return currentName;
        }
        
        public MultipartBufferPair current(String name) {
            MultipartBufferPair pair = null;

            if(name==null|| 
                    (pair = composite.get(name))==null 
                        && composite.put(name,pair = new MultipartBufferPair())==pair
                            || null==pair)
            {
                return null;
            }
            
            if(!isAvailable)
                isAvailable = true;
            
            currentName = name;
            
            return pair;
        }
        
        public long getLength() {
            
            if(null==composite)
                return 0;
            
            long totLength = 0;
            for (Iterator<Map.Entry<String, MultipartBufferPair>> it = composite.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, MultipartBufferPair> entry = it.next();
                MultipartBufferPair pair = entry.getValue();
                totLength += pair.getLength();
            }
           
            return totLength+((MultipartBufferImpl)common.getCached()).length();
        }
        
        public void writeHead(ByteBuffer b,String name) throws IOException{
            MultipartBufferPair.this.checkPair(composite.get(name));
            composite.get(name).writeHead(b);
        }
        
        public void writeFoot(ByteBuffer b,String name) throws IOException{
            MultipartBufferPair.this.checkPair(composite.get(name));
            composite.get(name).writeFoot(b);
        }
        
        public int flushHead(ContentEncoder encoder,String name) throws IOException {
            MultipartBufferPair.this.checkPair(composite.get(name));
            int fh = composite.get(name).flushHead(encoder);
            if(composite.get(name).isClean());
                composite.remove(name);
             return fh;
        }
        
        public int flushFoot(ContentEncoder encoder,String name) throws IOException {
            MultipartBufferPair.this.checkPair(composite.get(name));
            int ff= composite.get(name).flushFoot(encoder);
            if(composite.get(name).isClean());
                composite.remove(name);
            return ff;
        }
        
        public void writeHead(ByteBuffer b) throws IOException{
            MultipartBufferPair.this.checkPair(composite.get(currentName));
            composite.get(currentName).writeHead(b);
        }
        
        public void writeFoot(ByteBuffer b) throws IOException{
            MultipartBufferPair.this.checkPair(composite.get(currentName));
            composite.get(currentName).writeFoot(b);
        }
        
        public int flushHead(ContentEncoder encoder) throws IOException {
            MultipartBufferPair.this.checkPair(composite.get(currentName));
            int fh = composite.get(currentName).flushHead(encoder);
            if(composite.get(currentName).isClean())
            {
                composite.remove(currentName);
            } 
            return fh;
        }
        
        public int flushFoot(ContentEncoder encoder) throws IOException {
            MultipartBufferPair.this.checkPair(composite.get(currentName));
            int ff = composite.get(currentName).flushFoot(encoder);
            if(composite.get(currentName).isClean());
                composite.remove(currentName);
            return ff;
        }
        
        public int flushEnd(ContentEncoder encoder) throws IOException{
            int fe = 0;
            try {
                if(common ==null)
                    throw new BufferUtil.BufferBeNullException();
                fe = ((MultipartBuffer)common.getCached()).flush(encoder);
            }finally{
                CachedBufferPool.freeCached(common);
                common = null;
            }
            
            return fe;
        }
        
        public boolean isAvailable() {
            return isAvailable;
        }
        
        public void close() {
            for (Iterator<Map.Entry<String, MultipartBufferPair>> it = composite.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, MultipartBufferPair> entry = it.next();
                String name = entry.getKey();
                MultipartBufferPair pair = entry.getValue();
                pair.close();
                it.remove();
            }

            if(null!=common) {
                common.free();
                common = null;
            }
            
            currentName = null;
            
            if(isAvailable)
                isAvailable = false;
        }
        
        
    }
    
}
