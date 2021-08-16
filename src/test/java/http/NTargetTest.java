package http;
/*
 * Copyright (C), 2002-2020, 苏宁易购电子商务有限公司
 * FileName: ConnectionLeakTest.java
 * Author:   19041969
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
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.nixian.core.concurrent.Call;
import com.nixian.core.concurrent.Callback;
import com.nixian.http.HttpClients;
import com.nixian.http.client.methods.BestHttpAsyncMethods;
import com.nixian.http.client.param.FileUploadParam;
import com.nixian.http.client.param.HeaderParam;
import com.nixian.http.client.param.MultipartParam;
import com.nixian.http.client.param.StringParam;


/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author 19041969
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class NTargetTest {
    
    private static Logger logger = Logger.getLogger(NTargetTest.class.getName()); 
    
    long start = System.currentTimeMillis();
    
    public static void main(String args[]) {
        
        NTargetTest test = new NTargetTest();
        
        try {
            logger.info("start test");
            //start
            
            HttpGetHeadInfo infoMulList = null;
//            for(int i=0;i<CommonCase.testCount;i++) {
//                infoMulList = test.uploadOne(CommonCase.testUrl,null);
//            }
            
            downloadFile(CommonCase.testUrl,CommonCase.localPath,null)
            .get();
            
            
//            HttpGetHeadInfo infoOne = test.uploadOne(testUrl, null);
            
//            HttpGetHeadInfo infoGet= test.get(testUrl,null);

//            HttpGetHeadInfo infoUploadFile = test.uploadFile(testUrl, null);  
            
//            HttpGetHeadInfo infoPost = test.post(testUrl, null);
            
            TimeUnit.MINUTES.sleep(1);
           
//                System.gc();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            test.shutdown();
        }
    }
   
    public void shutdown() {
        if(null!=HttpClients.getClient()) {
            try {
                HttpClients.getClient().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
   
    class TEST{
        public TEST() {
        }
        
        String testString="XYZ";
        Integer testInteger=456;
    }
    
    public HttpGetHeadInfo post(String url,Map<String ,String> map) {
        HttpGetHeadInfo info = new HttpGetHeadInfo();
        OSSObject object = new OSSObject();
        info.setObject(object);
        info.setnConnectionLeakTest(this);
        
        Callback back,call;
        call = (back = Callback.create()).thencall(res->{return getHeadInfo((HttpResponse)res,info);});
        
        try {
        
              CloseableHttpAsyncClient httpClient = HttpClients.getPoolClient(10000,10000,false);
            
            
              TEST test = new TEST();
              String str2 = new Gson().toJson(test);
//            ByteArrayEntity entity = null;
//            try {
//                entity = new ByteArrayEntity(jsonObject.toString().getBytes("UTF-8"));
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
            
//            HttpPost post = new HttpPost(url);
//            NStringEntity entity = new NStringEntity(str1);
//            post.setEntity(entity);
//            
//            httpClient.execute(post, back);
            
//            String a = "{testString:上海,age:33}" ;
//        
            httpClient.execute(
                    BestHttpAsyncMethods.createPost(url,str2.getBytes("UTF-8"),ContentType.create("application/json", "UTF-8")),
                    BestHttpAsyncMethods.createConsumer(),
                    back);
        } catch (UnsupportedCharsetException | UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return info;
        
    }
    
    public HttpGetHeadInfo get(String url, Map<String, String> map) throws MalformedURLException
    {
        HttpGetHeadInfo info = new HttpGetHeadInfo();
        OSSObject object = new OSSObject();
        info.setObject(object);
        info.setnConnectionLeakTest(this);
        
        Callback back,call;
        call = (back = Callback.create()).thencall(res->{return getHeadInfo((HttpResponse)res,info);});
       
        CloseableHttpAsyncClient httpClient = HttpClients.getPoolClient(10000,10000,false);
        try {
            httpClient.execute( BestHttpAsyncMethods.createGet(url),
                                BestHttpAsyncMethods.createConsumer(),
                                back);
            
        }
        catch (Exception e) {
            object.setSuccess(false);
            e.printStackTrace();
        }
        return info;
    }
    
    public HttpGetHeadInfo uploadFile(String url, Map<String, String> map) throws MalformedURLException{
        
        HttpGetHeadInfo info = new HttpGetHeadInfo();
        OSSObject object = new OSSObject();
        info.setObject(object);
        info.setnConnectionLeakTest(this);
        
        Callback back,call;
        call = (back = Callback.create()).thencall(res->{return getHeadInfo((HttpResponse)res,info);});
        
        CloseableHttpAsyncClient httpClient = HttpClients.getPoolClient(10000,10000,false);
        File file = new File(CommonCase.testFile[1]);
                  
        try {
            httpClient.execute(BestHttpAsyncMethods.createZeroCopyPost(url, file, ContentType.create("application/x-www-form-urlencoded", "UTF-8")),
                               BestHttpAsyncMethods.createConsumer(),
                               back);
        }
        catch (Exception e) {
            info.getOssObject().setSuccess(false);
            e.printStackTrace();
        }
        return info;
    } 
   
    public HttpGetHeadInfo uploadList(String url, Map<String, String> map) {
        HttpGetHeadInfo info = new HttpGetHeadInfo();
        OSSObject object = new OSSObject();
        info.setObject(object);
        info.setnConnectionLeakTest(this);
        
        Callback back,call;
        call = (back = Callback.create()).thencall(res->{return getHeadInfo((HttpResponse)res,info);});
      
        List<MultipartParam> params = new ArrayList<MultipartParam>();
       
        params.add( FileUploadParam.create(new File(CommonCase.testFile[2])).setCharset("utf-8")
                    .setMimeType("rar").setName("fileBodyL"));
        
        params.add( FileUploadParam.create(new File(CommonCase.testFile[0])).setCharset("utf-8")
                .setMimeType("txt").setName("fileBodyL"));
        
        params.add( FileUploadParam.create(new File(CommonCase.testFile[3])).setCharset("utf-8")
                .setMimeType("jpeg").setName("fileBodyL"));
        
        params.add( StringParam.create("TEST").setCharset("utf-8").setMimeType("text/plain").setName("testString"));
        params.add( StringParam.create("123").setCharset("utf-8").setMimeType("text/plain").setName("testInteger"));
        params.add( StringParam.create("123").setCharset("utf-8").setMimeType("application/json").setName("testIntegerOther"));
//        
        
        CloseableHttpAsyncClient httpClient = HttpClients.getPoolClient(10000,10000,false);
        try {
           httpClient.execute(
                   BestHttpAsyncMethods.createMultipartPostUploadStrict(
                           params,
                           HeaderParam.create(url).setCharset("utf-8")
                   ),
                   BestHttpAsyncMethods.createConsumer(),
                   back
            );
        }
        catch (Exception e) {
            info.getOssObject().setSuccess(false);
            e.printStackTrace();
        }
        return info;
    }
   
    public HttpGetHeadInfo uploadOne(String url, Map<String, String> map) throws MalformedURLException{
        Callback back,call;
        HttpGetHeadInfo info = new HttpGetHeadInfo();
        call = (back = Callback.create()).thencall(res->{return getHeadInfo((HttpResponse)res,info);});
        OSSObject object = new OSSObject();
        info.setObject(object);
        info.setnConnectionLeakTest(this);
       
        CloseableHttpAsyncClient httpClient = HttpClients.getPoolClient(10000,10000,false);
        try {
            httpClient.execute(
                    BestHttpAsyncMethods.createMultipartPostUpload(
                            FileUploadParam.create(new File(CommonCase.testFile[2]))
                                                .setCharset("utf-8")
                                                .setMimeType("xls").setName("fileBody"),
                            HeaderParam.create(url).setCharset("utf-8")
                    ),
                    BestHttpAsyncMethods.createConsumer(),
                    back
             );
        }
        catch (Exception e) {
             info.getOssObject().setSuccess(false);
             e.printStackTrace();
        }
        return info;
    }
   
   public static Callback<HttpResponse> downloadFile(String url,String localPath,Map<String,String>map)
   {
       CloseableHttpAsyncClient httpClient = HttpClients.getPoolClient(10000,10000,false);       
       
       Callback back,call;
       
       call = (back = Callback.create()).thencall(res->{
               return res;
           }
       );
       try {
       
           httpClient.execute(
               BestHttpAsyncMethods.createGet(HeaderParam.create(url).setCharset("utf-8")),
               BestHttpAsyncMethods.createDownlaod(localPath),
               back);
       }catch(Exception e){
           e.printStackTrace();
           call = back.thencall(res->{return null;});
       }
       return call;
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

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public HttpGetHeadInfo getHeadInfo1(Object res)
    {
        return getHeadInfo((HttpResponse)res,null);
    }
    
    public HttpGetHeadInfo getHeadInfo(HttpResponse httpRes,HttpGetHeadInfo info) {
        try {
            OSSObject object = info.getOssObject();
            int statusCode = httpRes.getStatusLine().getStatusCode();
            info.setStatusCode(statusCode);
            if ((200 <= statusCode) && (300 > statusCode)) {
                HttpEntity entity1 = httpRes.getEntity();
                           
                InputStream ins = entity1.getContent();
                info.setIn(ins);
                
                if (httpRes.containsHeader("last-modified")) {
                    String lastModifide = httpRes.getFirstHeader("last-modified").getValue();
                    info.setLastModified(Date.parse(lastModifide));
                }
                           
                object.setContentLength(entity1.getContentLength());
                object.setSuccess(true);
                info.setObject(object);
            }else {
                object.setSuccess(false);
                info.setObject(object);
            }
            info.end = System.currentTimeMillis();
            logger.info("once cost is:"+(info.end-info.start));
            logger.info("total cost maybe:"+(info.end-info.getnConnectionLeakTest().start));
//            StringBuffer heads = new StringBuffer();
//            Arrays.asList(httpRes.getAllHeaders()).stream().forEach(header->{
//                heads.append(header.getName()+":"+header.getValue()+";");
//            });
//            logger.config(heads.toString());
            logger.info("Body:"+EntityUtils.toString(httpRes.getEntity()));

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return info;
        
        
    }
    
    
    
    
    
    static class HttpGetHeadInfo{
     
        
        
        private long lastModifie;
        /**
      * @return the lastModifie
      */
     public long getLastModifie() {
         return lastModifie;
     }
     /**
      * @param lastModifie the lastModifie to set
      */
     public void setLastModified(long lastModifie) {
         this.lastModifie = lastModifie;
     }
     /**
      * @return the inputStream
      */
     public InputStream getInputStream() {
         return inputStream;
     }
     /**
      * @param inputStream the inputStream to set
      */
     public void setIn(InputStream inputStream) {
         this.inputStream = inputStream;
     }
     /**
      * @return the statusCode
      */
     public int getStatusCode() {
         return statusCode;
     }
     /**
      * @param statusCode the statusCode to set
      */
     public void setStatusCode(int statusCode) {
         this.statusCode = statusCode;
     }
     /**
      * @return the ossObject
      */
     public OSSObject getOssObject() {
         return ossObject;
     }
     /**
      * @param ossObject the ossObject to set
      */
     public void setObject(OSSObject ossObject) {
         this.ossObject = ossObject;
     }
     
     /**
      * @return the nConnectionLeakTest
      */
     public NTargetTest getnConnectionLeakTest() {
         return nConnectionLeakTest;
     }
     /**
      * @param nConnectionLeakTest the nConnectionLeakTest to set
      */
     public void setnConnectionLeakTest(NTargetTest nConnectionLeakTest) {
         this.nConnectionLeakTest = nConnectionLeakTest;
     }
     
     public long start = System.currentTimeMillis();
     public long end;
     
     private InputStream inputStream;
        private int statusCode;
        private OSSObject ossObject;
        
        private NTargetTest nConnectionLeakTest;
        
    }
    
    static class OSSObject{
        /**
      * @return the contentLength
      */
     public long getContentLength() {
         return contentLength;
     }
     /**
      * @param contentLength the contentLength to set
      */
     public void setContentLength(long contentLength) {
         this.contentLength = contentLength;
     }
     /**
      * @return the success
      */
     public boolean isSuccess() {
         return success;
     }
     /**
      * @param success the success to set
      */
     public void setSuccess(boolean success) {
         this.success = success;
     }
     private long contentLength;
        private boolean success;
    }
}
