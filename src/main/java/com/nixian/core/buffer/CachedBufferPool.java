/*
 * Copyright (C), 2002-2021, 苏宁易购电子商务有限公司
 * FileName: BufferPool.java
 * Author:   19041969
 * Date:     2021年1月14日 下午12:30:32
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.core.buffer;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.apache.http.nio.util.DirectByteBufferAllocator;
import org.apache.http.nio.util.HeapByteBufferAllocator;

import sun.nio.ch.DirectBuffer;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public abstract class CachedBufferPool {
    
    private static Logger logger = Logger.getLogger(CachedBufferPool.class.getName());
    
    public enum BufferType{
        HEAP,NATIVE,MULTIPART
    }
    
    static HeapByteBufferAllocator heap_allocator = new HeapByteBufferAllocator();
    
    static DirectByteBufferAllocator native_allocator = new DirectByteBufferAllocator();
    
    public static ThreadLocal<Map<CachedBufferPool, Recycle>> recycle = new ThreadLocal<Map<CachedBufferPool, Recycle>>() {
        protected Map<CachedBufferPool, Recycle> initialValue() {
            return new WeakHashMap<CachedBufferPool, Recycle>();
        }
    }; 

    public static Cached allocate() {
        return allocate(BufferType.MULTIPART);
    }
    
    public static Cached allocate(int size) {
        return allocate(size,BufferType.HEAP);
    }
    
    public static Cached allocate(int size,BufferType type) {
        Cached cb ;
        CachedBufferPool pool = null;
        
        switch(type) {
            case HEAP:
                pool = Heap.buffer.get();
                break;
            case NATIVE:
                pool = NonHeap.buffer.get();
                break;
            case MULTIPART:
                pool = Multipart.buffer.get();
                break;
            default :
                return null;
        }
        
        if(fillPool(pool)) {
//          从回收站内回收部分的资源用于补充buffer
//            logger.config("翻翻回收站1.....");
//            logger.config("成功从回收站淘到宝贝 ");
        }
        
        checkPool(pool);
        
        if(null==(cb= pool.getCached(size))) {
            (cb = pool.newCached(pool,size)).setThread(Thread.currentThread()).setPool(pool);
//            logger.config("来自新建的缓充1 "+cb.capacity()+" "+cb.hashCode());
        }else {
//            logger.config("来自池内的缓存1 "+cb.capacity()+" "+cb.hashCode());
        }
        return cb;
    }
    
    public static Cached allocate(BufferType type) {
        Cached cb ;
        CachedBufferPool pool = null;
        
        switch(type) {
            case HEAP:
                pool = Heap.buffer.get();
                break;
            case NATIVE:
                pool = NonHeap.buffer.get();
                break;
            case MULTIPART:
                pool = Multipart.buffer.get();
                break;
            default :
                return null;
        }
        
        if(fillPool(pool)) {
//          从回收站内回收部分的资源用于补充buffer
//            logger.config("翻翻回收站1.....");
//            logger.config("成功从回收站淘到宝贝 ");
        }
        
        checkPool(pool);
        
        if(null==(cb= pool.getCached())) {
            (cb = pool.newCached(pool)).setThread(Thread.currentThread()).setPool(pool);
//            logger.config("来自新建的缓充1 "+cb.capacity()+" "+cb.hashCode());
        }else {
//            logger.config("来自池内的缓存1 "+cb.capacity()+" "+cb.hashCode());
        }
        return cb;
    }
    
    public static boolean freeCached(Cached cached) {

        cached.recycleable = false;
        CachedBufferPool pool = cached.pool;
        if(cached.getThread()==Thread.currentThread())
        {
            if(pool.offerLast(cached)) 
                return true;
        }else {
            
//            放入当前的回收站内
            Recycle recycles = recycle.get().get(pool);
            if(null==recycles)
            {
                recycle.get().put(pool,recycles = new Recycle(Thread.currentThread(),pool));
            }
            
            recycles.add(cached);
        }
        cached.clear();
        cached = null;
        return false;
            
    }
    
    private static boolean fillPool(CachedBufferPool pool) {
        return 1>=pool.count() && pool.recycleCached();
    }
    
    private static boolean checkPool(CachedBufferPool pool) {
        if(pool.TEMP_BUF_POOL_SIZE == pool.count()) {
            pool.buffers[pool.start] = null;
            pool.start = pool.next(pool.start);
            return true;
        }
        return false;
    }
    
    protected Cached newCached(CachedBufferPool pool) {
        return newCached(pool,0);
    }
    
    protected abstract Cached newCached(CachedBufferPool pool,int i);
    
    protected abstract Cached selected(CachedBufferPool pool,int i);
    
    private CachedBufferPool() {
        buffers = new Cached[TEMP_BUF_POOL_SIZE];
    }
    
    int TEMP_BUF_POOL_SIZE = 10;
    
    private Cached[] buffers;

    private int count;

    private int start;
    
    private volatile Recycle recycles;
    
    private Recycle curr,prev;
    
    private int next(int i) {
        return (i + 1) % TEMP_BUF_POOL_SIZE;
    }
    
    private int relocate(int i) {
        return (i+count) % TEMP_BUF_POOL_SIZE;
    }

    private int count() {
        return this.count;
    }
    
    boolean isEmpty() {
        return count == 0;
    }
    
    private boolean recycleCached() {
        Recycle head = recycles;
        if(head==null)
        {
            return false;
        }
        
        Recycle curr = this.curr,prev=this.prev;
        while(null!=curr) {

            if(curr.recycle(this)) {
                this.curr = curr;
                this.prev = prev;
                return true;
            }
            
            if(null==curr.thread.get()) {
                if(!curr.isCleanUp())
                {
                    while(true) {
                        if(!curr.recycle(this)) {
                            break;
                        }
                    }
                }
                if(null!=prev)
                    prev.next = curr.next;
            }else {
                prev = curr;
            }
            curr = curr.next;
        }
        
        this.curr = head;
        this.prev = null;
        
        return false;
    }
    
    Cached getCached() {
        return getCached(0);
    }
    Cached getCached(int size) {

        if (count == 0)
            return null; 

        Cached buf = selected(this,size);
        
        if(buf==null){
            return null;
        }
        
        this.buffers[start] = null;
        start = next(start);
        count--;
        
        buf.clear();
        return buf;
    }

    boolean offerFirst(Cached buf) {

        if (count >= TEMP_BUF_POOL_SIZE) {
            if(buf.capacity()> buffers[start].capacity())
                return false;
            else {
                buffers[start].clean();
                buffers[start] = buf;
                return true;
            }
        } else {
            start = (start + TEMP_BUF_POOL_SIZE - 1) % TEMP_BUF_POOL_SIZE;
            buffers[start] = buf;
            count++;
            return true;
        }
    }

    
    boolean offerLast(Cached buf) {

        if (count >= TEMP_BUF_POOL_SIZE) {
            if(buf.capacity() > buffers[start].capacity()) {
                buf.clean();
                return false;
            }
            else {
                buffers[start].clean();
                buffers[start] = buf;
                return true;
            }
        } else {
            int next = (start + count) % TEMP_BUF_POOL_SIZE;
            buffers[next] = buf;
            count++;
            return true;
        }
    }

    Cached removeFirst() {
        assert count > 0;
        Cached buf = buffers[start];
        buffers[start] = null;
        start = next(start);
        count--;
        return buf;
    }
    
    static class Multipart extends CachedBufferPool{
        
        static ThreadLocal<CachedBufferPool> buffer = new ThreadLocal<CachedBufferPool>() {
            protected CachedBufferPool initialValue() {
                return new Multipart(100);
            }
        };
        
//        static ThreadLocal<Map<CachedBufferPool, Recycle>> recycle = CachedBufferPool.recycle = new ThreadLocal<Map<CachedBufferPool, Recycle>>() {
//            protected Map<CachedBufferPool, Recycle> initialValue() {
//                return new WeakHashMap<CachedBufferPool, Recycle>();
//            }
//        };
        
        int defaultSize = 200;
        
        private Multipart(int size) {
            if(size>0)
                this.defaultSize = size;
        }
        
        protected Cached newCached(CachedBufferPool pool,int size) {
            return new Cached<MultipartBufferImpl>(new MultipartBufferImpl(defaultSize,heap_allocator),pool) {
                @Override
                public int capacity() {
                    return getCached().capacity();
                }
                
                @Override
                public void clear() {
                    getCached().claer();
                }
                
                @Override 
                public void clean() {
                }
            };
        }
        
        protected Cached selected(CachedBufferPool pool,int size) {
            Cached[] buffers = pool.buffers;
            Cached buf = buffers[pool.start];
            int count = pool.count;
            int next = pool.next(pool.start);
            
            if (count > 1 && buf.capacity() > buffers[next].capacity()) {
                buf = buffers[next];
                pool.buffers[next] = buffers[pool.start];
            }
            
            return buf;
        }
    }
    
    static class Heap extends CachedBufferPool{
        
        static ThreadLocal<CachedBufferPool> buffer = new ThreadLocal<CachedBufferPool>() {
            protected CachedBufferPool initialValue() {
                return new Heap(0);
            }
        };
        
        
        int defaultSize = 4096;
        
        private Heap(int size) {
            if(size>0)
                this.defaultSize = size;
        }
        
        protected Cached newCached(CachedBufferPool pool,int size) {
            return new Cached<ByteBuffer>(heap_allocator.allocate(size>0?size:defaultSize),pool) {
                @Override
                public int capacity() {
                    return getCached().capacity();
                }
                
                @Override
                public void clear() {
                    getCached().clear();
                }
                
                @Override
                public void clean() {
                    
                }
            };
        }
        
        protected Cached selected(CachedBufferPool pool,int size) {
            Cached[] buffers = pool.buffers;
            Cached buf = buffers[pool.start];
            int count = pool.count;
            int next = pool.next(pool.start);
            int min = next;
            
            if(buf.capacity()>=size) {
                return buf; 
            }
            
            for(int i=1;i<count;i++)
            {
                if(buffers[min].capacity()>buffers[pool.next(pool.start-2+i)].capacity()) {
                    min = pool.next(pool.start-2+i);
                }
                
                if((buf=buffers[pool.next(pool.start-1+i)]).capacity()>=size) {
                    buffers[pool.next(pool.start-1+i)] = buffers[min];
                    buffers[min] = buffers[pool.start];
                    return buf;
                }
            }
            
            return null;
        }

    }
    
    static class NonHeap extends CachedBufferPool{
        
        static ThreadLocal<CachedBufferPool> buffer = new ThreadLocal<CachedBufferPool>() {
            protected CachedBufferPool initialValue() {
                return new NonHeap(0);
            }
        };
        
//        static ThreadLocal<Map<CachedBufferPool, Recycle>> recycle = CachedBufferPool.recycle = new ThreadLocal<Map<CachedBufferPool, Recycle>>() {
//            protected Map<CachedBufferPool, Recycle> initialValue() {
//                return new WeakHashMap<CachedBufferPool, Recycle>();
//            }
//        };
        
        int defaultSize = 4096;
        
        private NonHeap(int size) {
            if(size>0)
                this.defaultSize = size;
        }
        
        protected Cached newCached(CachedBufferPool pool,int size) {
            return new Cached<ByteBuffer>(native_allocator.allocate(size>0?size:defaultSize),pool) {
                @Override
                public int capacity() {
                    return getCached().capacity();
                }
                
                @Override
                public void clear() {
                    getCached().clear();
                }
                
                @Override
                public void clean() {
                    ((DirectBuffer)getCached()).cleaner().clean();
                }
            };
        }
        
//        protected Recycle getRecycle(CachedBufferPool pool) {
//            Recycle recycles = recycle.get().get(pool);
//            if(null==recycles)
//            {
//                recycle.get().put(pool,recycles = new Recycle(Thread.currentThread(),pool));
//            }
//            return recycles;
//        }
        
        protected Cached selected(CachedBufferPool pool,int size) {
            Cached[] buffers = pool.buffers;
            Cached buf = buffers[pool.start];
            int count = pool.count;
            int next = pool.next(pool.start);
            int min = next;
            
            if(buf.capacity()>=size) {
                return buf; 
            }
            
            for(int i=1;i<count;i++)
            {
                if(buffers[min].capacity()>buffers[pool.next(pool.start-2+i)].capacity()) {
                    min = pool.next(pool.start-2+i);
                }
                
                if((buf=buffers[pool.next(pool.start-1+i)]).capacity()>=size) {
                    buffers[pool.next(pool.start-1+i)] = buffers[min];
                    buffers[min] = buffers[pool.start];
                    return buf;
                }
            }
            
            return null;
        }

    }
    
    public static abstract class Cached<T> {
        
        private boolean recycleable = true;
        
        private T cached = null;
        
        private Thread thread = null;
        
        private CachedBufferPool pool = null;
        
        protected Thread getThread() {
            return this.thread;
        }
        
        protected Cached setThread(Thread thread) {
            this.thread = thread;
            return this;
        }
        
        protected Cached setPool(CachedBufferPool pool) {
            this.pool = pool;
            recycleable = true;
            return this;
        }
        
        protected Cached(T cached,CachedBufferPool pool) {
            this.cached = cached;
            this.pool = pool;
            recycleable = true;
        }
        
        public T getCached() {
            return cached;
        }
        
        public String toString() {
            String tostring = "pool:"+pool.toString();
            tostring += ",thread:"+thread.toString();
            tostring += ",capacity:";
            return tostring += String.valueOf(capacity());
        }
        
        public abstract int capacity();
        
        public abstract void clear();
        
        public abstract void clean();
        
        public boolean isFree() {
            return !recycleable;
        }
        
        public boolean free() {
            if(null!=pool && recycleable) {
                pool.freeCached(this);
                recycleable = false;
            }
            return isFree();
        }
    }
    
    static class Recycle {
        
        private Recycle next = null;
        
        private WeakReference<Thread> thread = null;
        
        private static final int LINK_CAPACITY = 16;

        private static final class Link extends AtomicInteger {
            private final Cached[] cacheds = new Cached[LINK_CAPACITY];

            private int readIndex;
            private Link next;
        }
        
        private Link head,tail;
        
        Recycle(Thread thread,CachedBufferPool pool) {
            head = tail = new Link();
            this.thread = new WeakReference(thread);
            synchronized(pool) {
                this.next = pool.recycles;
                pool.recycles = this;
            }
        }
        
        protected boolean isCleanUp() {
            return tail.readIndex == LINK_CAPACITY;
        }
        
        private boolean recycle(CachedBufferPool pool) {
            Link head = this.head;
            if(head == null)
                return false;
            
            if(head.readIndex==LINK_CAPACITY) {
                if(head.next==null)
                    return false;
                this.head = head = head.next;
            }
            
            int start = head.readIndex;
            int end = head.get();
            if (start == end) {
                return false;
            }

            int count = end - start;
            int poolStart = pool.start;
            
            if(count + pool.count > pool.TEMP_BUF_POOL_SIZE)
            {
                count = pool.TEMP_BUF_POOL_SIZE - pool.count;
                end = start + count;
            }
            
            Cached[] src = head.cacheds;
            Cached[] trg = pool.buffers;
            
            while (start < end) {
                Cached cached = src[start];
                cached.recycleable = true;
                if(null!=trg[pool.relocate(poolStart)]) {
                    trg[pool.relocate(poolStart)].clean();
                }
                trg[pool.relocate(poolStart++)] = cached;
                src[start++] = null;
            }
            pool.count += count;
            head.readIndex = end;

            if(end == LINK_CAPACITY && head.next != null)
            {
                this.head = head.next;
            }
            
            return count>0;
        }
        
        protected void add(Cached cached) {
            if(tail.get()==LINK_CAPACITY) {
                this.tail = tail.next = new Link();
            }
            tail.cacheds[tail.getAndIncrement()] = cached;
        }
        
    }
    
    
}
