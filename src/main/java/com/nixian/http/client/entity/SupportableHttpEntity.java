/*
 * Copyright (C), 2002-2021, nixian,email nixiantongxue@163.com
 * FileName: SeletableHttpEntity.java
 * Author:   nixian
 * Date:     2021年1月26日 下午4:26:04
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.http.client.entity;

import org.apache.http.HttpEntity;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @since [产品/模块版本] （可选）
 */
public interface SupportableHttpEntity extends HttpEntity{
    
    public boolean supportWriteTo();
    
    public boolean isWindowsFile();
    
}
