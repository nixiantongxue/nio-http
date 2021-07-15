package com.nixian.http.client.codecs;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.logging.Logger;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author nixian
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class EncoderHandler implements InvocationHandler {
    
    private static Logger logger = Logger.getLogger(EncoderHandler.class.getName());
    
    Object target;  

    public EncoderHandler(Object target) {
        this.target = target;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        before(method);
        if(method.getName().equals("complete")) {
            return true;
        }
        Object result = method.invoke(target, args);
        after(method);
        return result;  
    }
    
    private void before(Method method) {
        logger.info(String.format(method.getName()+" log start time [%s] ", new Date()));
    }
    
    private void after(Method method) {
        logger.info(String.format(method.getName()+" log end time [%s] ", new Date()));
    }
}