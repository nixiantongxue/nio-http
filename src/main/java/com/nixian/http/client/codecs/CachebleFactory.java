/*
 * Copyright (C), 2002-2021, nixian,email nixiantongxue@163.com
 * FileName: CachebleFactory.java
 * Author:   nixian
 * Date:     2021年6月4日 下午6:11:46
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.nixian.http.client.codecs;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.nio.ContentEncoder;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @since [产品/模块版本] （可选）
 */
public class CachebleFactory {
    
//    static private Map<ContentEncoder,WeakReference<BestEncoder>> factory = new ConcurrentHashMap<ContentEncoder,WeakReference<BestEncoder>>();
    
    static private Map<ContentEncoder,BestEncoder> factory = new ConcurrentHashMap<ContentEncoder,BestEncoder>();

    static private boolean add(BestEncoder encoder)
    {
        if(encoder instanceof Cacheable) {
            factory.put((ContentEncoder)encoder.key(), encoder);
            return true;
        }
        
        return false;
    }
    
    static protected void remove(ContentEncoder original) {
        factory.remove(original);
    }
    
    public static void clean() {
        Iterator<Map.Entry<ContentEncoder,BestEncoder>> factoryIt = factory.entrySet().iterator();
        while(factoryIt.hasNext()) {
            Map.Entry<ContentEncoder,BestEncoder> e = factoryIt.next();
            if(e.getKey().isCompleted())
                factoryIt.remove();
        }
    }
    
    static private BestEncoder get(ContentEncoder original)
    {
//        WeakReference<BestEncoder> reference = null;
//        return (reference = factory.get(original))==null?null:reference.get();
        return factory.get(original);
    }
    
    static public BestEncoder build(ContentEncoder original,Wrapper wrapper) throws Exception 
    {
        BestEncoder bester = null; 
        if((bester=get(original))!=null) {
            return bester;
        }
        
        bester = wrapper.wrap(original);
        
        if(null!=bester)
            add(bester);
        
        return bester;
    }
    
    interface Wrapper{
        BestEncoder wrap(ContentEncoder original) throws Exception;
    }
    
}