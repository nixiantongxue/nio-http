/*
 * Copyright (C), 2002-2021, nixian,email nixiantongxue@163.com
 * FileName: AbstracMultipart.java
 * Author:   nixian
 * Date:     2021年5月31日 下午3:45:14
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.http.client.entity;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.IOControl;

import com.nixian.core.buffer.MultipartBufferPair;

/**
 * Multipart 基于N-IO  基础抽象类, 可以使用BEST*ContentEncoder 发送内容
     *   衍生类可以定义Header 的发送模式:
     *   1. NativeHeadHttpMultipart 直接发送【原生】
     *   2. NMullitipart 使用缓存发送 
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @since [产品/模块版本] （可选）
 */
public abstract class NAbstractContentMultipart {

    final ByteBuffer FIELD_SEP = encode(MIME.DEFAULT_CHARSET, ": ");
    final ByteBuffer CR_LF = encode(MIME.DEFAULT_CHARSET, "\r\n");
    final ByteBuffer TWO_DASHES = encode(MIME.DEFAULT_CHARSET, "--");

    final String subType;
    final Charset charset;
    final String boundary;
    final List<FormBodyPart> parts;
    final HttpMultipartMode mode;
    
    volatile boolean bufferAvailable = false;
    MultipartBufferPair.MultipartBufferComposite buffer = MultipartBufferPair.create();
    Context context = new Context();
    
//  从配置中获取
    private boolean windowsEnv = false;
    
    enum Mode {
        NCM,
        BEST;
    }
    
    public NAbstractContentMultipart(final String subType, final Charset charset, final String boundary, HttpMultipartMode mode) {
        if (subType == null) {
            throw new IllegalArgumentException("Multipart subtype may not be null");
        }
        if (boundary == null) {
            throw new IllegalArgumentException("Multipart boundary may not be null");
        }
        this.subType = subType;
        this.charset = charset != null ? charset : MIME.DEFAULT_CHARSET;
        this.boundary = boundary;
        this.parts = new ArrayList<FormBodyPart>();
        this.mode = mode;
    }
    
    public String getBoundary() {
        return this.boundary;
    }
    
    ByteBuffer encode(
            final Charset charset, final String string) {
        ByteBuffer encoded = charset.encode(CharBuffer.wrap(string));
        return encoded;
    }
    
    void produceContent(final ContentEncoder encoder,final IOControl ioControl,ContentBody contentBody)throws IOException{
        (null==context.producer? (context.producer=ContentBodyWrapper.wrap(contentBody)):context.producer).produceContent(encoder, ioControl);
    }
    
    public void close() throws IOException{
        if(null!=buffer) {
            buffer.close();
        }
        bufferAvailable = false;
    }
    
    
    abstract public void produceContent(final ContentEncoder encoder, final IOControl ioControl) throws IOException;
    
    
    public String getSubType() {
        return this.subType;
    }

    public Charset getCharset() {
        return this.charset;
    }

    public HttpMultipartMode getMode() {
        return this.mode;
    }

    public List<FormBodyPart> getBodyParts() {
        return this.parts;
    }
    
    class Context{
         int partIdx;
         int partTot;
         ContentBodyWrapper producer;
         int step;
         int willNextStep;
    }
    
}
