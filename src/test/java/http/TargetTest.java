package http;
/*
 * Copyright (C), 2002-2020, nixian,email nixiantongxue@163.com
 * FileName: ConnectionLeakTest.java
 * Author:   nixian
 * Date:     2020年12月15日 下午4:36:59
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.AbstractContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.nixian.http.client.methods.BestHttpAsyncMethods;


/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @since [产品/模块版本] （可选）
 */
public class TargetTest {

    private static Logger logger = Logger.getLogger(TargetTest.class.getName()); 
    
    long start = System.currentTimeMillis();
    
    public static void main(String args[]) {
       
		TargetTest test = new TargetTest();
		try {
			logger.info("start test");
			NTargetTest.HttpGetHeadInfo info1 = null;
//          while(true) {
			
           for(int i=0;i<CommonCase.testCount;i++) {
                 info1= test.upload(CommonCase.testUrl,null);
           }
             //             info1.getInputStream().close();
//             logger.config("返回1：");
//             logger.config(info1.getStatusCode());
            logger.info("total cost is:"+(System.currentTimeMillis()-test.start)+"");
//             if(info1.getStatusCode()==1)
//             {
//                 break;
//             }
             
//          }
        
//        HttpGetHeadInfo info2 = test.sendHttpGetRequest("http://uedtooldev.cnsuning.com:8020//querier/design/testOOO",null);
//            logger.config("返回2：");
//            logger.config(info2.getStatusCode());
//            HttpGetHeadInfo info3 = test.sendHttpGetRequest("http://uedtooldev.cnsuning.com:8020//querier/design/testOOO",null);
//            logger.config("返回3：");
//            logger.config(info3.getStatusCode());
            
            TimeUnit.MINUTES.sleep(1);
//            System.gc();
		} catch (Exception e) {
            e.printStackTrace();
        }finally {
            test.shutdown();
        }
       
	}
   
	public void shutdown() {
		if(null!=customerHttpClient) {
            customerHttpClient.close();
		}
	}
   
	private HttpPost generationUpload(String url) {
       
		HttpPost httpPost = new HttpPost(url);
       
		MultipartEntity nEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
		AbstractContentBody inputBody2 = new FileBody(new File(CommonCase.testFile[2]), "rar", "utf-8");
		nEntity.addPart("fileBodyL", inputBody2);
		AbstractContentBody inputBody0 = new FileBody(new File(CommonCase.testFile[0]), "txt", "utf-8");
        nEntity.addPart("fileBodyL", inputBody0);
        AbstractContentBody inputBody3 = new FileBody(new File(CommonCase.testFile[3]), "jpeg", "utf-8");
        nEntity.addPart("fileBodyL", inputBody3);
        
        try {
            AbstractContentBody testString = new StringBody("TEST", "text/plain",BestHttpAsyncMethods.createCharset("utf-8"));
            nEntity.addPart("testString", testString);
            
            AbstractContentBody testInteger = new StringBody("123", "text/plain", BestHttpAsyncMethods.createCharset("utf-8"));
            nEntity.addPart("testInteger", testInteger);
            
            AbstractContentBody testIntegerOther = new StringBody("123", "application/json", BestHttpAsyncMethods.createCharset("utf-8"));
            nEntity.addPart("testIntegerOther", testIntegerOther);
        
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
		httpPost.setEntity(nEntity);
		return httpPost;
	}
   
	public NTargetTest.HttpGetHeadInfo upload(String url, Map<String, String> map) throws MalformedURLException
	{
		NTargetTest.HttpGetHeadInfo info = new NTargetTest.HttpGetHeadInfo();
		NTargetTest.OSSObject object = new NTargetTest.OSSObject();

		long start = System.currentTimeMillis();
		HttpClient httpClient = getPoolClient(10000,10000,false);
		HttpResponse httpRes = null;
		try {
		    
			httpRes = httpClient.execute(generationUpload(url));
   
			logger.info("once cost is:"+(System.currentTimeMillis()-start)+":"+EntityUtils.toString(httpRes.getEntity()));

			if (null != httpRes) {
				int statusCode = httpRes.getStatusLine().getStatusCode();
				info.setStatusCode(statusCode);
				if ((200 <= statusCode) && (300 > statusCode)) {
					HttpEntity entity = httpRes.getEntity();
			   
					InputStream ins = entity.getContent();
					info.setIn(ins);
			   
	  
					if (httpRes.containsHeader("last-modified")) {
						String lastModifide = httpRes.getFirstHeader("last-modified").getValue();
						info.setLastModified(Date.parse(lastModifide));
					}
			   
					object.setContentLength(entity.getContentLength());
					object.setSuccess(true);
					info.setObject(object);
				}
				else {
					object.setSuccess(false);
					info.setObject(object);
				}
			}
		}catch (Exception e) {
			object.setSuccess(false);
			info.setObject(object);
            e.printStackTrace();
		}
		return info;
	}
   
	public NTargetTest.HttpGetHeadInfo sendHttpGetRequest(String url, Map<String, String> map) throws MalformedURLException
	{
		NTargetTest.HttpGetHeadInfo info = new NTargetTest.HttpGetHeadInfo();
		NTargetTest.OSSObject object = new NTargetTest.OSSObject();
     
		HttpGet httpGet = new HttpGet(url);
     
		HttpClient httpClient = getPoolClient(10000,10000,false);
		HttpResponse httpRes = null;
		try {
			httpRes = httpClient.execute(httpGet);
			if (null != httpRes) {
				int statusCode = httpRes.getStatusLine().getStatusCode();
				info.setStatusCode(statusCode);
				if ((200 <= statusCode) && (300 > statusCode)) {
					HttpEntity entity = httpRes.getEntity();
           
					InputStream ins = entity.getContent();
					info.setIn(ins);
            
					if (httpRes.containsHeader("last-modified")) {
						String lastModifide = httpRes.getFirstHeader("last-modified").getValue();
						info.setLastModified(Date.parse(lastModifide));
					}
           
					object.setContentLength(entity.getContentLength());
					object.setSuccess(true);
					info.setObject(object);
				}else {
					object.setSuccess(false);
					info.setObject(object);
				}
			}
		}catch (Exception e) {
			object.setSuccess(false);
			info.setObject(object);
            e.printStackTrace();
		}
		return info;
	}
   
    private static DefaultHttpClient customerHttpClient;
    
    private static DefaultHttpClient getPoolClient(int timeout, long idleTime, boolean tcpNoDelay)
    {
		if (null == customerHttpClient) {
			synchronized (TargetTest.class) {
				if (null == customerHttpClient) {
				HttpParams params = new BasicHttpParams();
				
				HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
				HttpProtocolParams.setContentCharset(params, "UTF-8");
				
				HttpConnectionParams.setStaleCheckingEnabled(params, false);
				
				HttpProtocolParams.setUseExpectContinue(params, false);
				
				HttpConnectionParams.setConnectionTimeout(params, timeout);
				
				HttpConnectionParams.setSoTimeout(params, timeout);
				
				HttpConnectionParams.setSoKeepalive(params, true);
				
				HttpConnectionParams.setTcpNoDelay(params, tcpNoDelay);
				
		
				SchemeRegistry schemeRegistry = new SchemeRegistry();
				schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
				schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
				
				PoolingClientConnectionManager conMgr = new PoolingClientConnectionManager(schemeRegistry) {
			
					protected void finalize() throws Throwable {
						try {
							logger.info(this.toString());
							shutdown();
							logger.info(this.toString());
						} finally {
							super.finalize();
						}
					}
				};
				
				 conMgr.setMaxTotal(1000000);
				
				 conMgr.setDefaultMaxPerRoute(1000000);
				
				 customerHttpClient = new DefaultHttpClient(conMgr, params);
				 customerHttpClient.setHttpRequestRetryHandler(getRetryHandler());
			  }
			}
		}
		if (null != customerHttpClient) {
			customerHttpClient.getConnectionManager().closeExpiredConnections();
			customerHttpClient.getConnectionManager().closeIdleConnections(idleTime, TimeUnit.MILLISECONDS);
		}
		return customerHttpClient;
    }
    
    
    private static HttpRequestRetryHandler getRetryHandler() {
        return new HttpRequestRetryHandler()
        { 
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context)
            {
               return false;
            }
        };
    }
 
}
