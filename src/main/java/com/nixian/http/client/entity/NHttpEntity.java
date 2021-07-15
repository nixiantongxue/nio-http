package com.nixian.http.client.entity;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.EntityAsyncContentProducer;
import org.apache.http.nio.entity.HttpAsyncContentProducer;
import org.apache.http.nio.entity.NByteArrayEntity;
import org.apache.http.nio.entity.NFileEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.entity.ProducingNHttpEntity;

import com.nixian.core.buffer.CachedBufferPool;
import com.nixian.core.buffer.CachedBufferPool.BufferType;
import com.nixian.core.buffer.CachedBufferPool.Cached;
/**
 * 
 * {@link ProducingNHttpEntity} compatibility adaptor for blocking HTTP
 * entities.
 *
 * First: 兼容B-IO ,supportWriteTo = true
 * Second:B-IO 下的HttpEntity 向上兼容N-IO
 *
 * @author nixian
 * @since 4.0
 *
 * @deprecated (4.2) use {@link EntityAsyncContentProducer}
 */
@Deprecated
public class NHttpEntity extends com.nixian.http.client.entity.HttpEntityWrapper 
                         implements ProducingNHttpEntity,HttpAsyncContentProducer {

    private final ReadableByteChannel channel;
    
    private final int TRANSFER_SIZE = 8192;
    
    public NHttpEntity(final HttpEntity httpEntity) throws IOException {
        super(httpEntity);
        if(httpEntity instanceof SupportableHttpEntity) {
            setSupportWriteTo(((SupportableHttpEntity)httpEntity).supportWriteTo());
            setIsWindowsFile(((SupportableHttpEntity) httpEntity).isWindowsFile());
        }else {
            setIsWindowsFile(setSupportWriteTo(false));
        }
        this.channel = Channels.newChannel(httpEntity.getContent());
    }

    public NHttpEntity(final String str, final ContentType contentType ) throws IOException{
        super(new NStringEntity(str,contentType));
        this.channel = Channels.newChannel(super.getContent());
        setIsWindowsFile(setSupportWriteTo(true));
    }
    
    public NHttpEntity(final byte[] b, final ContentType contentType) throws IOException {
        super(new NByteArrayEntity(b,contentType));
        this.channel = Channels.newChannel(super.getContent());
        setIsWindowsFile(setSupportWriteTo(true));
    }
    
    public NHttpEntity(final File file, final ContentType contentType) throws IOException{
        super(new NFileEntity(file,contentType));
        this.channel = Channels.newChannel(super.getContent());
        setIsWindowsFile(setSupportWriteTo(true));
    }
    
    public NHttpEntity(final InputStream stream,final ContentType contentType) throws IOException
    {
        super(new NStreamEntity(stream,contentType));
        this.channel = Channels.newChannel(super.getContent());
        setIsWindowsFile(setSupportWriteTo(true));
    }
    
    
    /**
     * This method throws {@link UnsupportedOperationException}.
     */
    @Override
    public InputStream getContent() throws IOException, UnsupportedOperationException {
        if(!supportWriteTo())
            throw new UnsupportedOperationException("Does not support getContent() methods");
        return super.getContent();
    }

    @Override
    public boolean isStreaming() {
        return true;
    }

    /**
     * 
     * This method throws {@link UnsupportedOperationException}.
     */
    @Override
    public void writeTo(final OutputStream out) throws IOException, UnsupportedOperationException {
        if(!supportWriteTo())
            throw new UnsupportedOperationException("Does not support writeTo() methods");
        super.getEntity().writeTo(out);
    }
    
    @Override
    public void produceContent(final ContentEncoder encoder,final IOControl ioctrl) throws IOException {
        
        if(!(super.getEntity() instanceof ProducingNHttpEntity) ||
                this.channel instanceof FileChannel && !isWindowsFile()) {
            produceContent_backup(encoder,ioctrl);
        }else {
            ((ProducingNHttpEntity)super.getEntity()).produceContent(encoder, ioctrl);
        }
    }

    /**
                这个实现多了一次 cpu-copy
     *          
     *适用于将IO下的HttpEntity 以 B 的方式
     * A. 1channel,2buffer,3encoder,4sessionoutbuffer,5direct,6kernel,7nic
     * 
     * B. 1,2,3,(4),6,7
     * 
     * C. 2,3,(4),6,7 
     * 
     * D. 7       
     */
    public void produceContent_backup(final ContentEncoder encoder,final IOControl ioctrl) throws IOException {
        Cached cd = null;
        try {
            ByteBuffer buffer =   (ByteBuffer)(cd= CachedBufferPool.allocate(TRANSFER_SIZE,BufferType.NATIVE)).getCached();
            int nr,tw=0;
            while((nr = this.channel.read(buffer))!=-1) {
                buffer.flip();
                int nw = encoder.write(buffer);
                if(nr!=nw)
                    throw new IOException("读写异常!");
                tw += nw;
                buffer.clear();
            }
            encoder.complete();
        }finally {
            this.channel.close();
            if(null!=cd)
                cd.free();
        }
    }
    
//    private void erase(ByteBuffer bb) {
//        Unsafe.getUnsafe().setMemory(((DirectBuffer)bb).address(), bb.capacity(), (byte)0);
//    }

    @Override
    public void finish() throws IOException {
        close();
    }

    @Override
    public void close() throws IOException {
        try {
            if(super.getEntity() instanceof Closeable) {
                ((Closeable)super.getEntity()).close();
            } 
        }finally{
            this.channel.close();
        }
        
        
    }

    

}
