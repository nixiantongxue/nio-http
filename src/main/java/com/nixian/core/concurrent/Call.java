package com.nixian.core.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;

/**
 * THREAD-SAFE
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class Call extends Callback<HttpResponse>{
    
    private static Logger logger = Logger.getLogger(Call.class.getName());
    
    volatile Callback tail = this;
    
    private long start = System.currentTimeMillis();
    
    private HttpResponse response;
    
    private Exception ex;
    
    private Retry retry;
    
    Call(){
    }
    
    public <T,R> Callback retry(BiFunction<? super Exception,? super HttpResponse,Boolean> fn,int retryTime) {
        if(null==retry)
            this.retry = new Retry(fn,retryTime,this);
        return this;
    }
    
    public <R> Callback<R> thencall(Function<? super HttpResponse,? extends R> fn){
        
        CommonCallback<R,HttpResponse> call = null;
        synchronized (this) {
            tail = call = new CommonCallback<R,HttpResponse>(this,tail,this,fn);
            call.src.next = call;
        }
        call.execute();
        
        return call;
    }
    
    public <R> Callback<R> thencallAsync(Function<? super HttpResponse,? extends R> fn){
        
        if(null==executor) executor = stpe;
        
        CommonCallback<R,HttpResponse> call = null;
        synchronized (this) {
            tail = call = new CommonCallback<R,HttpResponse>(this,tail,this,fn);
            call.src.next = call;
            call.executor = stpe;
        }
        
        call.execute();
        return call;
    }
    
    @Override
    public Callback tail() {
        return tail;
    }
    
    @Override 
    public Call head() {
        return this;
    }
    
    public HttpResponse response() {
        return response;
    }

    public HttpResponse result() throws ExecutionException{
        if(ex!=null)
            throw new ExecutionException(ex);
        return response();
    }
    
    boolean execute() {
        return completed;
    }
    
    public void completed(HttpResponse httpResponse) {
        
        completed = true;
        this.response = httpResponse;
        
        if(this.retry!=null && this.retry.retry(null,httpResponse))
        {
            return;
        }
        
        if(this.next!=null)
        {
            this.next.execute();
        }
    }

    public void failed(Exception e) {
        this.ex = e;
        if(this.retry!=null) 
            this.retry.retry(e,null);
    }

    public void cancelled() {
        
    }
    
    protected static Executor stpe  = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue (20),new CallerRunsPolicy());
    
    class Retry<R>{
        BiFunction<? super Exception,? super HttpResponse,Boolean> fn;
        int retryTime = 0;
        Call back;
        
        Retry(BiFunction<? super Exception,? super HttpResponse,Boolean> fn,int retryTime,Call back){
            this.back = back;
            this.fn = fn;
            this.retryTime = retryTime;
            if(this.retryTime<0 || this.retryTime>100) {
                this.retryTime = 3;
            }
        }
        
        boolean retry(Exception e,HttpResponse res){
            
            back.response = res;
            back.ex = e;
            
            if(this.retryTime-->0)
            {
                if(!this.fn.apply(null,res)) {
                    this.retryTime++;
                }else {
                    return true;
                }
            }
            
            return false;
        }
    }
    
    class CommonCallback<R,T> extends Callback<R> implements Runnable{
        
        private Callback src = null;
        private Call back = null;
        private Callback<T> dep = null;
        Function<? super T,? extends R> fn;
        
        private R result;
        
        CommonCallback(Call back,Function<? super T,? extends R> fn){
            this(back,back,back,fn);
        }
        
        CommonCallback(Callback dep,Callback src,Call back,Function<? super T,? extends R> fn){
            this.dep = dep;
            this.src = src;
            this.back = back;
            this.fn = fn;
        }
        
        public <V> Callback<V> thencall(Function<? super R,? extends V> fn){
           
            CommonCallback<V,R> call = null;
            synchronized (this.back) {
                this.back.tail = call = new CommonCallback<V,R>(this,this.back.tail,this.back,fn);
                call.src.next = call;
            }
            call.execute();
            
            return call;
        }
        
        public <V> Callback<V> thencallAsync(Function<? super R,? extends V> fn){
            
            if(null==executor) executor = stpe;
            
            CommonCallback<V,R>call = null;
            
            synchronized (this.back) {
                this.back.tail = call = new CommonCallback<V,R>(this,this.back.tail,this.back,fn);
                call.src.next = call;
            }
            
            call.executor = stpe;
            call.execute();
            return call;
        }
        
        public HttpResponse response() {
            return this.back.response;
        }
        
        final boolean execute()
        {
            CommonCallback nxt = this;
            do{
                try {
                    
                    if((nxt.back.result() instanceof Exception)) {
                        throw (Exception)(nxt.back.result());
                    }
                    
                    if(nxt.src!=null && nxt.src.completed || nxt.src==null ) {
                        if(nxt.completed) continue;
                        nxt.completed(null);
                    }else{
                        return true;
                    }
                    
                    if(nxt.next==null || nxt.back==null) return true;
                    
                    if(nxt.back.executor==nxt.executor && !nxt.execute()) {
                        this.executor.execute(nxt);
                    }
                } catch (Exception e) {
                    logger.severe(e.toString());
                    failed(e);
                }
            }while((nxt = (CommonCallback)(nxt.next))!=null);
            
            return true;
        }
        
        public final void run() {execute();}
        
        @Override
        public  void completed(R result){
            
            if(tryRuning())  try { this.result = this.fn.apply(this.dep==null?null:this.dep.result());} catch (ExecutionException e) {logger.severe(e.toString());}
            else
                return;
            
            completed = true;
        }

        @Override
        public void failed(Exception ex) {
            back.ex = ex;
            completed = true;
        }

        public void cancelled() {
            
        };
        
        @Override
        public R result() throws ExecutionException{
            if(back.ex!=null)
                throw new ExecutionException(back.ex);
            return result;
        }
        
        @Override
        public Callback tail() {
            return back.tail();
        }
        
        @Override 
        public Call head() {
            return back;
        }
    }

    
        
}