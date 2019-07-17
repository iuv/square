package com.jisuye.core;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 统一请求Servlet处理
 * @author ixx
 * @date 2019-07-14
 */
public class DispatcherServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(DispatcherServlet.class);

    private BeansMap beansMap = new BeansMap();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 解析url
        String contextPath = req.getContextPath();
        String httpMethod = req.getMethod();
        String uri = req.getRequestURI();
        // 匹配到对应的controller
        String controllerKey = httpMethod.toLowerCase()+":"+uri.replace(contextPath, "");
        ControllerObject controllerObject = beansMap.getController(controllerKey);
        // 如果没有匹配，返回404
        if(controllerObject == null){
            resp.sendError(404);
        } else {
            // 执行对应方法
            Object obj = controllerObject.invoke(req);
            // 处理返回结果
            String json;
            if (obj instanceof String) {
                json = (String) obj;
            } else {
                json = JSON.toJSONString(obj);
                resp.setHeader("content-type", "application/json;charset=UTF-8");
            }
            log.info("http request path:" + controllerKey);
            log.info("exec method ：" + controllerObject.getMethod().getName());
            log.info("response:" + json);
            resp.getWriter().print(json);
        }
    }
}
