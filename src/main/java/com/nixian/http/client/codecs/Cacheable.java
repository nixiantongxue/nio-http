/*
 * Copyright (C), 2002-2021, 苏宁易购电子商务有限公司
 * FileName: Cacheable.java
 * Author:   19041969
 * Date:     2021年6月8日 下午8:00:04
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.http.client.codecs;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author xian
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public interface Cacheable<T> {
    
    public T key();
    
    public void remove();
}
