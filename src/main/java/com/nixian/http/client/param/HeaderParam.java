/*
 * Copyright (C), 2002-2021, nixian,email nixiantongxue@163.com
 * FileName: HeaderParam.java
 * Author:   nixian
 * Date:     2021年6月4日 下午4:21:14
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.http.client.param;

import java.util.HashMap;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @since [产品/模块版本] （可选）
 */
public class HeaderParam {
    
    String charset = "ISO-8859-1";
    String url;
    public Map header = new HashMap<String,String>();
    
    public static HeaderParam create(String url){
        return new HeaderParam(url);
    }
    
    public static HeaderParam create(String url,String charset) {
        return new HeaderParam(url,charset);
    }
    
    public HeaderParam(String url) {
        this.url = url;
    }
    
    public HeaderParam(String url,String charset) {
        this.charset = charset;
        this.url = url;
    }
    
    public HeaderParam addHeader(String headerName,String headerValue) {
        header.put(headerName,headerValue);
        return this;
    }
    
    /**
     * @return the charset
     */
    public String getCharset() {
        return charset;
    }

    /**
     * @param charset the charset to set
     */
    public HeaderParam setCharset(String charset) {
        this.charset = charset;
        return this;
    }
    
    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public HeaderParam setUrl(String url) {
        this.url = url;
        return this;
    }

    
}
