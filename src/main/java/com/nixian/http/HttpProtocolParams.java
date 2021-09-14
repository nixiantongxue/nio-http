/*
 * Copyright (C), 2002-2021, nixian,email nixiantongxue@163.com
 * FileName: HttpProtocolParams.java
 * Author:   nixian
 * Date:     2021年2月3日 下午4:23:45
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.http;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.params.CoreProtocolPNames;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @since [产品/模块版本] （可选）
 */
public class HttpProtocolParams {
    
    public static void setUserAgent(HttpRequestBase base,String userAgent) {
        if(null == base || userAgent == null)
            return;
        base.setHeader(CoreProtocolPNames.USER_AGENT, userAgent);
    }
    
}
