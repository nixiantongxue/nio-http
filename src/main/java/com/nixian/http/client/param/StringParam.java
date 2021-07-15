/*
 * Copyright (C), 2002-2021, 苏宁易购电子商务有限公司
 * FileName: StringParam.java
 * Author:   19041969
 * Date:     2021年6月10日 上午12:07:54
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.http.client.param;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

import com.nixian.http.client.methods.BestHttpAsyncMethods;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class StringParam extends MultipartParam {

    String value;
    
    public static StringParam create(String value){
        return new StringParam(value);
    }

    public static StringParam create(String name,String mimeType,String charset,String value) {
        return new StringParam(name, mimeType,charset,value);
    }
    
    public StringParam(String name,String mimeType,String charset,String value) {
        super(name, mimeType,charset,value);
        this.value = value;
    }
    
    public StringParam(String value) {
        this.value = value;
    }
    
    public ContentBody getContentBody() {
        return StringBody.create(value,mimeType,BestHttpAsyncMethods.createCharset(charset));
    }

}
