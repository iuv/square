package com.jisuye.service.aop;

import com.alibaba.fastjson.JSON;
import com.jisuye.annotations.Component;
import com.jisuye.annotations.aop.Aspect;
import com.jisuye.annotations.aop.Before;
import com.jisuye.annotations.aop.Pointcut;
import com.jisuye.core.RequestContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * 定义切面类,测试切面
 */
@Aspect
@Component
public class WebLogAspect {

    private static final Logger log = LoggerFactory.getLogger(WebLogAspect.class);

    /**
     * 测试com.jisuye.service.impl包下的所以类的abc开头方法（所有参数）添加AOP
     */
    @Pointcut("excution(public * com.jisuye.service.impl.*.abc*(..))")
    public void log(){}

    @Before("log()")
    public void doBefore(){
        HttpServletRequest request = RequestContextHolder.getRequest();
        if(request != null) {
            log.info("AOP exe... url:{}, params:{}", request.getRequestURI(), JSON.toJSONString(request.getParameterMap()));
        }
    }
}
