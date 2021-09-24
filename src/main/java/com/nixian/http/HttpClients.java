/*
 * Copyright (C), 2002-2020, nixian,email nixiantongxue@163.com
 * FileName: bestHttpClient.java
 * Author:   nixian
 * Date:     2020年12月29日 下午6:29:31
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.http;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.nio.NHttpConnectionBase;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.conn.ManagedNHttpClientConnection;
import org.apache.http.nio.conn.NHttpConnectionFactory;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactorException;

import com.nixian.http.client.codecs.CachebleFactory;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @since [产品/模块版本] （可选）
 */
public class HttpClients {
    
    private static CloseableHttpAsyncClient bestHttpClient;
    
    private final static AtomicReference status = new AtomicReference(Status.NULL);
    
    public static void shutdown() throws IOException {
        if(status.compareAndSet(Status.ACTIVE, Status.STOPPED))
        {
            bestHttpClient.close();
        }
    }
    
    public static CloseableHttpAsyncClient getClient() throws IllegalStateException{
        if(status.get()==Status.INACTIVE) {
            synchronized (HttpClients.class) {
                if(status.get()==Status.ACTIVE)
                    return bestHttpClient;
            }
        }else if(status.get()==Status.ACTIVE)
            return bestHttpClient;
        throw new IllegalStateException("可能尚未创建HTTP-CLIENT,优先调用方法getPoolClient(int timeout, long idleTime, boolean tcpNoDelay)");
    }
    
    public static CloseableHttpAsyncClient getPoolClient(int timeout, long idleTime, boolean tcpNoDelay)
    {
        
        if (null == bestHttpClient) {
            synchronized (HttpClients.class) {
                if(status.compareAndSet(Status.NULL, Status.INACTIVE)) {
                    ConnectionConfig defaultConfig = null; 
                    defaultConfig = ConnectionConfig.custom().setBufferSize(8192).build();
                    
    //                NHttpConnectionFactory<ManagedNHttpClientConnection> connFactory = new ManagedNHttpClientConnectionFactory() {
    //                      
    //                      private ConnectionConfig defaultConfig0 = defaultConfig;
    //                      
    //                      @Override
    //                      public ManagedNHttpClientConnection create(
    //                              final IOSession iosession, final ConnectionConfig config) {
    //                          return super.create(iosession, defaultConfig0);
    //                      }
    //                };
    
                    RequestConfig requestConfig = RequestConfig.custom()
                           .setConnectTimeout(timeout)
                           .setSocketTimeout(timeout)
                           .setConnectionRequestTimeout(1000)
                           .setStaleConnectionCheckEnabled(true)
                           .build();
        
                        //配置io线程
                    IOReactorConfig ioReactorConfig = IOReactorConfig.custom().
                            setIoThreadCount(Runtime.getRuntime().availableProcessors()*3)
                            .setSoKeepAlive(true)
                            .setSelectInterval(5000)
                            .setTcpNoDelay(tcpNoDelay)
                            .build();
                    
                    //设置连接池大小
                    ConnectingIOReactor ioReactor=null;
                    try {
                        ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
                    } catch (IOReactorException e) {
                        e.printStackTrace();
                    }
                        
                    PoolingNHttpClientConnectionManager connManager = new NiPoolingNHttpClientConnectionManager(ioReactor,idleTime);
                    connManager.setMaxTotal(100);
                    connManager.setDefaultMaxPerRoute(100);
        
                    final CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                            .setConnectionManager(connManager)
                            .setDefaultRequestConfig(requestConfig)
                            .setDefaultConnectionConfig(defaultConfig)
                            .build();
        
                    client.start();
                    bestHttpClient = client;
                    status.compareAndSet(Status.INACTIVE, Status.ACTIVE);
                }
            }
        }
        
        return getClient();
    }
    
    static enum Status {NULL,INACTIVE, ACTIVE, STOPPED}
    
    static class NiPoolingNHttpClientConnectionManager extends PoolingNHttpClientConnectionManager{
        private IdleConnectionMonitorThread monitor = null;
        
        private final AtomicReference<Status> status;
        private NiPoolingNHttpClientConnectionManager(final ConnectingIOReactor ioreactor,
                final NHttpConnectionFactory<ManagedNHttpClientConnection> connFactory,long idleTime) {
            super(ioreactor,connFactory);
            this.monitor = new IdleConnectionMonitorThread(this,idleTime);
            this.monitor.start();
            status= new AtomicReference(Status.INACTIVE);
        }
        
        private NiPoolingNHttpClientConnectionManager(final ConnectingIOReactor ioreactor,final long idleTime) {
            this(ioreactor,null,idleTime);
        }
        
        private NiPoolingNHttpClientConnectionManager(final ConnectingIOReactor ioreactor) {
            this(ioreactor,30);
        }
        
        @Override
        public void execute(final IOEventDispatch eventDispatch) throws IOException {
            if(status.compareAndSet(Status.INACTIVE, Status.ACTIVE)) {
                super.execute(eventDispatch);
            }
        }
        
        @Override
        public Future<NHttpClientConnection> requestConnection(
                final HttpRoute route,
                final Object state,
                final long connectTimeout,
                final long leaseTimeout,
                final TimeUnit tunit,
                final FutureCallback<NHttpClientConnection> callback) {
            return super.requestConnection(route, state, connectTimeout, leaseTimeout, tunit, 
                    
                    new FutureCallback<NHttpClientConnection>() {

                        @Override
                        public void completed(final NHttpClientConnection result) {
//                             辅助提前释放decoder 
                            result.resetOutput();
                            callback.completed(result);
                        }
        
                        @Override
                        public void failed(final Exception ex) {
                            callback.failed(ex);
                        }
        
                        @Override
                        public void cancelled() {
                            callback.cancelled();
                        }
        
                    });
        }
        
        public void shutdown()  throws IOException {
            if (status.compareAndSet(Status.ACTIVE, Status.STOPPED)) {
                this.monitor.shutdown();
                super.shutdown();
                try {
                    this.monitor.join();
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        class IdleConnectionMonitorThread extends Thread {
            
            private final PoolingNHttpClientConnectionManager connMgr;
            private volatile boolean shutdown;
            private final long idleTime;
            
            public IdleConnectionMonitorThread(PoolingNHttpClientConnectionManager connMgr,long idleTime) {
                super();
                this.connMgr = connMgr;
                this.idleTime = idleTime;
            }

            @Override
            public void run() {
                try {
                    while (!shutdown) {
                            Thread.sleep(500);
                            connMgr.closeExpiredConnections();
                            connMgr.closeIdleConnections(this.idleTime, TimeUnit.SECONDS);
                            CachebleFactory.clean();
                    }
                } catch (InterruptedException ex) {
                }
            }
            
            public void shutdown() {
                shutdown = true;
            }
        }
    }
    
    private CloseableHttpAsyncClient demo() {
        if (null == bestHttpClient) {
            
            synchronized (HttpClients.class) {

                ConnectionConfig defaultConfig = ConnectionConfig.custom()
                        .setBufferSize(70)
                        .build();
                
//                NHttpConnectionFactory<ManagedNHttpClientConnection> connFactory = new ManagedNHttpClientConnectionFactory() {
//                    
//                    private ConnectionConfig defaultConfig0 = defaultConfig;
//                    
//                    @Override
//                    public ManagedNHttpClientConnection create(
//                            final IOSession iosession, final ConnectionConfig config) {
//                        return super.create(iosession, defaultConfig0);
//                    }
//                };
                
                
                
                RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectTimeout(501)
                        .setSocketTimeout(502)
                        .setConnectionRequestTimeout(401)
                        .setStaleConnectionCheckEnabled(true)
                        .build();

                //配置io线程
                IOReactorConfig ioReactorConfig = IOReactorConfig.custom().
                        setIoThreadCount(Runtime.getRuntime().availableProcessors())
                        .setSoKeepAlive(true)
                        .setSoTimeout(5030)
                        .setConnectTimeout(5040)
                        .setSelectInterval(6010)
                        .build();
                
                //设置连接池大小
                ConnectingIOReactor ioReactor=null;
                try {
                    ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
                } catch (IOReactorException e) {
                    e.printStackTrace();
                }
                PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(ioReactor);
                connManager.setMaxTotal(100);
                connManager.setDefaultMaxPerRoute(100);
    
                final CloseableHttpAsyncClient client = HttpAsyncClients.custom().
                        setConnectionManager(connManager)
                        .setDefaultRequestConfig(requestConfig)
                        .setDefaultConnectionConfig(defaultConfig)
                        .build();
    
                //start
                client.start();
    
                bestHttpClient = client;

           }
         }
              return bestHttpClient;
       }
}
