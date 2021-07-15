/*
 * Copyright (C), 2002-2021, 苏宁易购电子商务有限公司
 * FileName: BaseRequestHttpMethods.java
 * Author:   19041969
 * Date:     2021年1月11日 下午3:56:18
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.http.client.methods;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.entity.EntityAsyncContentProducer;
import org.apache.http.nio.entity.HttpAsyncContentProducer;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

import com.nixian.http.client.codecs.BestEncoder;
import com.nixian.http.client.codecs.EncoderWrapperFactory;
import com.nixian.http.client.entity.NHttpEntity;
import com.nixian.http.client.entity.NMultipartEntity;
import com.nixian.http.client.param.HeaderParam;
import com.nixian.http.client.param.MultipartParam;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
final public class BestHttpAsyncMethods{
    
    public static HttpAsyncRequestProducer cerateDelete(HeaderParam headerParam) {
        HttpDelete httpDelete = new HttpDelete(headerParam.getUrl());
        headerParam.header.forEach((key,value)->{
            httpDelete.setHeader((String)key, (String)value);
        });
        return BestRequestProducer.create(httpDelete);
    }
    
    public static HttpAsyncRequestProducer createPostUpload(InputStream data, String mimeType, long length,
            HeaderParam headerParam) throws UnsupportedCharsetException, IOException{
        HttpPost httpPost = new HttpPost(headerParam.getUrl());
        NHttpEntity nEntity = new NHttpEntity(data, ContentType.create(mimeType, "UTF-8"));
        nEntity.setContentLength(length);
        headerParam.header.forEach((key,value)->{
            httpPost.setHeader((String)key, (String)value);
        });
        httpPost.setEntity(nEntity);
        return BestRequestProducer.create(httpPost);
    }
    
    public static BestRequestProducer createMultipartPostUpload(MultipartParam uploadParam,HeaderParam headerParam) {
        HttpPost httpPost = new HttpPost(headerParam.getUrl());
        NMultipartEntity nEntity = new NMultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, createCharset(headerParam.getCharset()));
        nEntity.addPart(uploadParam.getName(), uploadParam.getContentBody());
        headerParam.header.forEach((key,value)->{
            httpPost.setHeader((String)key, (String)value);
        });
        httpPost.setEntity(nEntity);
        return BestRequestProducer.create(httpPost);
    }
    
    public static BestRequestProducer createMultipartPostUpload(List<MultipartParam> uploadParams,HeaderParam headerParam) {
        HttpPost httpPost = new HttpPost(headerParam.getUrl());
        NMultipartEntity nEntity = new NMultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, createCharset(headerParam.getCharset()));
        uploadParams.stream().forEach(param->{
            nEntity.addPart(param.getName(),param.getContentBody());
        });
        headerParam.header.forEach((key,value)->{
            httpPost.setHeader((String)key, (String)value);
        });
        httpPost.setEntity(nEntity);
        return BestRequestProducer.create(httpPost);
    }
    
    public static BestRequestProducer createMultipartPostUploadStrict(List<MultipartParam> uploadParams,HeaderParam headerParam) {
        HttpPost httpPost = new HttpPost(headerParam.getUrl());
        NMultipartEntity nEntity = new NMultipartEntity(HttpMultipartMode.STRICT, null, createCharset(headerParam.getCharset()));
        uploadParams.stream().forEach(param->{
            nEntity.addPart(param.getName(),param.getContentBody());
        });
        headerParam.header.forEach((key,value)->{
            httpPost.setHeader((String)key, (String)value);
        });
        httpPost.setEntity(nEntity);
        return BestRequestProducer.create(httpPost);
    }
    
    public static BestRequestProducer createMultipartPutUpload(MultipartParam uploadParam,HeaderParam headerParam) {
        HttpPut httpPut = new HttpPut(headerParam.getUrl());
        NMultipartEntity nEntity = new NMultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, createCharset(headerParam.getCharset()));
        nEntity.addPart(uploadParam.getName(), uploadParam.getContentBody());
        httpPut.setEntity(nEntity);
        headerParam.header.forEach((key,value)->{
            httpPut.setHeader((String)key, (String)value);
        });
        return BestRequestProducer.create(httpPut);
    }
    
    public static BestRequestProducer createMultipartPutUpload(List<MultipartParam> uploadParams,HeaderParam headerParam) {
        HttpPut httpPut = new HttpPut(headerParam.getUrl());
        NMultipartEntity nEntity = new NMultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, createCharset(headerParam.getCharset()));
        uploadParams.stream().forEach(param->{
            nEntity.addPart(param.getName(), param.getContentBody());
        });
        headerParam.header.forEach((key,value)->{
            httpPut.setHeader((String)key, (String)value);
        });
        httpPut.setEntity(nEntity);
        return BestRequestProducer.create(httpPut);
    }
    
    public static HttpAsyncRequestProducer createPutUpload(InputStream data, String mimeType, long length,
            HeaderParam headerParam) throws UnsupportedCharsetException, IOException{
        HttpPut httpPut = new HttpPut(headerParam.getUrl());
        NHttpEntity nEntity = new NHttpEntity(data, ContentType.create(mimeType, "UTF-8"));
        nEntity.setContentLength(length);
        headerParam.header.forEach((key,value)->{
            httpPut.setHeader((String)key, (String)value);
        });
        httpPut.setEntity(nEntity);
        return BestRequestProducer.create(httpPut);
    }
    
    public static Charset createCharset(String charset) {
        Charset cs = null;
        try {
            cs = Charset.forName(charset);
        } catch (Exception e) {
            cs = MIME.DEFAULT_CHARSET;
        }
        return cs;
    }
    
    public static HttpAsyncResponseConsumer createDownlaod(final File file) throws FileNotFoundException{
        return HttpAsyncMethods.createZeroCopyConsumer(file);
    }
    
    
//    ==================================================================================================
    
    
    public static HttpAsyncRequestProducer create(final HttpHost target, final HttpRequest request) {
        return HttpAsyncMethods.create(target,request);
    }

    public static HttpAsyncRequestProducer create(final HttpUriRequest request) {
        return HttpAsyncMethods.create(request);
    }

    public static HttpAsyncRequestProducer createGet(final URI requestURI) {
        return HttpAsyncMethods.createGet(requestURI);
    }

    public static HttpAsyncRequestProducer createGet(final String requestURI) {
        return HttpAsyncMethods.createGet(requestURI);
    }

    public static HttpAsyncRequestProducer createHead(final URI requestURI) {
        return HttpAsyncMethods.createHead(requestURI);
    }

    public static HttpAsyncRequestProducer createHead(final String requestURI) {
        return HttpAsyncMethods.createHead(requestURI);
    }

    public static HttpAsyncRequestProducer createDelete(final URI requestURI) {
        return HttpAsyncMethods.createDelete(requestURI);
    }

    public static HttpAsyncRequestProducer createDelete(final String requestURI) {
        return HttpAsyncMethods.createDelete(requestURI);
    }

    public static HttpAsyncRequestProducer createOptions(final URI requestURI) {
        return HttpAsyncMethods.createOptions(requestURI);
    }

    public static HttpAsyncRequestProducer createOptions(final String requestURI) {
        return HttpAsyncMethods.createOptions(requestURI);
    }

    public static HttpAsyncRequestProducer createTrace(final URI requestURI) {
        return HttpAsyncMethods.createTrace(requestURI);
    }

    public static HttpAsyncRequestProducer createTrace(final String requestURI) {
        return HttpAsyncMethods.createTrace(requestURI);
    }

    public static HttpAsyncRequestProducer createPost(
            final URI requestURI,
            final String content,
            final ContentType contentType) throws UnsupportedEncodingException {
        return HttpAsyncMethods.createPost(requestURI, content, contentType);
    }

    public static HttpAsyncRequestProducer createPost(
            final String requestURI,
            final String content,
            final ContentType contentType) throws UnsupportedEncodingException {
        return HttpAsyncMethods.createPost(requestURI, content, contentType);
    }

    public static HttpAsyncRequestProducer createPost(
            final URI requestURI,
            final byte[] content,
            final ContentType contentType) {
        return HttpAsyncMethods.createPost(requestURI, content, contentType);
    }

    public static HttpAsyncRequestProducer createPost(
            final String requestURI,
            final byte[] content,
            final ContentType contentType) {
        return HttpAsyncMethods.createPost(requestURI, content, contentType);
    }

    public static HttpAsyncRequestProducer createPut(
            final URI requestURI,
            final String content,
            final ContentType contentType) throws UnsupportedEncodingException {
        return HttpAsyncMethods.createPut(requestURI, content, contentType);
    }

    public static HttpAsyncRequestProducer createPut(
            final String requestURI,
            final String content,
            final ContentType contentType) throws UnsupportedEncodingException {
        return HttpAsyncMethods.createPut(requestURI, content, contentType);
    }

    public static HttpAsyncRequestProducer createPut(
            final URI requestURI,
            final byte[] content,
            final ContentType contentType) {
        return HttpAsyncMethods.createPut(requestURI, content, contentType);
    }

    public static HttpAsyncRequestProducer createPut(
            final String requestURI,
            final byte[] content,
            final ContentType contentType) {
        return HttpAsyncMethods.createPut(requestURI, content, contentType);
    }

    public static HttpAsyncRequestProducer createZeroCopyPost(
            final URI requestURI,
            final File content,
            final ContentType contentType) throws FileNotFoundException {
        return HttpAsyncMethods.createZeroCopyPost(requestURI, content, contentType);
    }

    public static HttpAsyncRequestProducer createZeroCopyPost(
            final String requestURI,
            final File content,
            final ContentType contentType) throws FileNotFoundException {
        return HttpAsyncMethods.createZeroCopyPost(requestURI, content, contentType);
    }

    public static HttpAsyncRequestProducer createZeroCopyPut(
            final URI requestURI,
            final File content,
            final ContentType contentType) throws FileNotFoundException {
        return HttpAsyncMethods.createZeroCopyPut(requestURI, content, contentType);
    }

    public static HttpAsyncRequestProducer createZeroCopyPut(
            final String requestURI,
            final File content,
            final ContentType contentType) throws FileNotFoundException {
        return HttpAsyncMethods.createZeroCopyPut(requestURI, content, contentType);
    }

    public static HttpAsyncResponseConsumer<HttpResponse> createConsumer() {
        return HttpAsyncMethods.createConsumer();
    }

    public static HttpAsyncResponseConsumer<HttpResponse> createZeroCopyConsumer(
            final File file) throws FileNotFoundException {
        return HttpAsyncMethods.createZeroCopyConsumer(file);
    }
    
    
    
//    ===========================================================================================
    
    
    
    static class BestRequestProducer implements HttpAsyncRequestProducer{

        private final HttpHost target;
        private final HttpRequest request;
        private final HttpAsyncContentProducer producer;

        public static BestRequestProducer create(final HttpUriRequest httpRequest) {
            return new BestRequestProducer(URIUtils.extractHost(httpRequest.getURI()),httpRequest); 
        }
        /**
         * Creates a producer that can be used to transmit the given request
         * message. The given content producer will be used to stream out message
         * content. Please note that the request message is expected to enclose
         * an {@link HttpEntity} whose properties are consistent with the behavior
         * of the content producer.
         *
         * @param target target host.
         * @param request request message.
         * @param producer request content producer.
         */
        protected BestRequestProducer(
                final HttpHost target,
                final HttpEntityEnclosingRequest request,
                final HttpAsyncContentProducer producer) {
            super();
            Args.notNull(target, "HTTP host");
            Args.notNull(request, "HTTP request");
            Args.notNull(producer, "HTTP content producer");
            this.target = target;
            this.request = request;
            this.producer = producer;
        }

        /**
         * Creates a producer that can be used to transmit the given request
         * message. If the request message encloses an {@link HttpEntity}
         * it is also expected to implement {@link HttpAsyncContentProducer}.
         *
         * @param target target host.
         * @param request request message.
         */
        public BestRequestProducer(final HttpHost target, final HttpRequest request) {
            Args.notNull(target, "HTTP host");
            Args.notNull(request, "HTTP request");
            this.target = target;
            this.request = request;
            if (request instanceof HttpEntityEnclosingRequest) {
                final HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                if (entity != null) {
                    if (entity instanceof HttpAsyncContentProducer) {
                        this.producer = (HttpAsyncContentProducer) entity;
                    } else {
                        this.producer = new EntityAsyncContentProducer(entity);
                    }
                } else {
                    this.producer = null;
                }
            } else {
                this.producer = null;
                throw new IllegalArgumentException("request 参数有误");
            }
        }

        @Override
        public HttpRequest generateRequest() {
            return this.request;
        }

        @Override
        public HttpHost getTarget() {
            return this.target;
        }

        @Override
        public void produceContent(
                final ContentEncoder encoder, final IOControl ioControl) throws IOException {
            BestEncoder wrapper;
            try {
                wrapper = (BestEncoder)EncoderWrapperFactory.build(encoder);
                
                if (this.producer != null) {
                    this.producer.produceContent(wrapper, ioControl);
                    if (wrapper.isCompleted()) {
                        this.producer.close();
                    }
                }
                
            } catch (Exception e) {
                throw new IOException(e);
            }
            
        }

        @Override
        public void requestCompleted(final HttpContext context) {
        }

        @Override
        public void failed(final Exception ex) {
            ex.printStackTrace();
        }

        @Override
        public boolean isRepeatable() {
            return this.producer == null || this.producer.isRepeatable();
        }

        @Override
        public void resetRequest() throws IOException {
            if (this.producer != null) {
                this.producer.close();
            }
        }

        @Override
        public void close() throws IOException {
            if (this.producer != null) {
                this.producer.close();
            }
        }

        public BestRequestProducer add(MultipartParam param) throws IllegalArgumentException{
            if (request instanceof HttpEntityEnclosingRequest) {
                final HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                if (entity != null) {
                    if(entity instanceof NMultipartEntity) {
                        ((NMultipartEntity) entity).addPart(param.getName(),param.getContentBody());
                    }else
                        throw new IllegalArgumentException("entity Not best!");
                } else {
                }
            }else {
               throw new IllegalArgumentException("request 参数有误");
            }
            
            return this;
        }
        
        public BestRequestProducer add(String name,ContentBody bodyPart) throws IllegalArgumentException{
            
            if (request instanceof HttpEntityEnclosingRequest) {
                final HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                if (entity != null) {
                    if(entity instanceof NMultipartEntity) {
                        ((NMultipartEntity) entity).addPart(name,bodyPart);
                    }else
                        throw new IllegalArgumentException("entity Not best!");
                } else {
                }
            }else {
               throw new IllegalArgumentException("request 参数有误");
            }
            
            return this;
        }
        
        
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append(this.target);
            sb.append(' ');
            sb.append(this.request);
            if (this.producer != null) {
                sb.append(' ');
                sb.append(this.producer);
            }
            return sb.toString();
        }
    }



   

}
