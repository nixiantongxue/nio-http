package com.nixian.core.concurrent;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.Args;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public abstract class Callback<T> implements FutureCallback<T>,Future{
    
    public static com.nixian.core.concurrent.Call create() {
        return new Call();
    }
    
    public static com.nixian.core.concurrent.Call create(boolean mixMode) {
        Call call = null;
        if(mixMode) {
            (call = new Call()).completed(null);
        }
        return call;
    }
    
    protected Callback next = null;
    protected Callback replaced = null;
    protected Executor executor = null;
    protected short status = 0;
    protected volatile boolean completed = false;

    abstract public void completed(T result);
    abstract public void failed(Exception ex);
    abstract public void cancelled();
    
    abstract public <R> Callback thencall(Function<? super T,? extends R> fn);
    abstract public <R> Callback thencallAsync(Function<? super T,? extends R> fn);
    abstract public Callback tail();
    abstract public Call head();
    abstract boolean execute();
    abstract protected T result() throws ExecutionException;
    
    final T actualResult() throws ExecutionException{
        Object result = result();
        if(result instanceof Callback)
            return (T)(((Callback)result).actualResult());

        return (T)result;
    }
    
    protected synchronized Callback notifyGet() {
        this.notifyAll();
        return this;
    }
    /* (non-Javadoc)
     * @see java.util.concurrent.Future#cancel(boolean)
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.util.concurrent.Future#isCancelled()
     */
    @Override
    public boolean isCancelled() {
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.util.concurrent.Future#isDone()
     */
    @Override
    public boolean isDone() {
        return completed;
    }
    
    /* (non-Javadoc)
     * @see java.util.concurrent.Future#get()
     */
    @Override
    public synchronized T get() throws InterruptedException, ExecutionException {
        while (!this.completed) {
            wait();
        }
        return actualResult();
    }
    
    /* (non-Javadoc)
     * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public synchronized T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
       
        Args.notNull(unit, "Time unit");
        final long msecs = unit.toMillis(timeout);
        final long startTime = (msecs <= 0) ? 0 : System.currentTimeMillis();
        long waitTime = msecs;
        if (this.completed) {
            return actualResult();
        } else if (waitTime <= 0) {
            throw new TimeoutException();
        } else {
            for (;;) {
                wait(waitTime);
                if (this.completed) {
                    return actualResult();
                } else {
                    waitTime = msecs - (System.currentTimeMillis() - startTime);
                    if (waitTime <= 0) {
                        throw new TimeoutException();
                    }
                }
            }
        }
    }
    
//    public final Callback replace(Callback callback) {
        
//        this.replaced = callback;
//    }
    
    public final Callback merge(Callback callback) {
//        Callback call = this.next;
//        while(!UNSAFE.compareAndSwapObject(this, NEXT, call,callback)) {}
//        callback.execute();
        
        callback.head().tail.next = callback.head();
        this.head().tail = callback.head().tail;
        while(callback.head().tail !=this.head().tail) {this.head().tail = callback.head().tail;};
        callback.execute();
        
        
        return callback;
    }
    
    protected final boolean tryRuning() {
        return UNSAFE.compareAndSwapInt(this, STATUS, status,1);
    }
    
    protected final boolean reRunable() {
        return UNSAFE.compareAndSwapInt(this, STATUS, 1,0);
    }
    
    private static final sun.misc.Unsafe UNSAFE;
    private static final long EXECUTOR;
    private static final long NEXT;
    private static final Long STATUS;
    static {
        try {
            final sun.misc.Unsafe u;
            UNSAFE = u = getUnsafe();//sun.misc.Unsafe.getUnsafe();
            Class<?> k = Callback.class;
            EXECUTOR = u.objectFieldOffset(k.getDeclaredField("executor"));
            NEXT = u.objectFieldOffset(k.getDeclaredField("next"));
            STATUS = u.objectFieldOffset(k.getDeclaredField("status"));
        } catch (Exception x) {
            throw new Error(x);
        }
    }
    
    static private sun.misc.Unsafe getUnsafe() throws Exception {
        Class<?> unsafeClass = sun.misc.Unsafe.class;
        for (Field f : unsafeClass.getDeclaredFields()) {
            if ("theUnsafe".equals(f.getName())) {
                f.setAccessible(true);
                return (sun.misc.Unsafe) f.get(null);
            }
        }
        throw new IllegalAccessException("no declared field: theUnsafe");
    }
}