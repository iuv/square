package com.jisuye.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 保存当前线程对应Request请求
 * @author ixx
 * @date 2019-07-27
 */
public class RequestContextHolder {
    private static ThreadLocal<HttpServletRequest> requestThreadLocal;

    private static ThreadLocal<HttpServletResponse> responseThreadLocal;


    public static HttpServletRequest getRequest() {
        if(requestThreadLocal == null){
            return null;
        }
        return requestThreadLocal.get();
    }

    public static void setRequest(HttpServletRequest request) {
        requestThreadLocal = ThreadLocal.withInitial(() -> request);
    }

    public static HttpServletResponse getResponse() {
        if(responseThreadLocal == null){
            return null;
        }
        return responseThreadLocal.get();
    }

    public static void setResponse(HttpServletResponse response) {
        responseThreadLocal = ThreadLocal.withInitial(() -> response);
    }

    public static void init(HttpServletRequest request, HttpServletResponse response){
        requestThreadLocal = ThreadLocal.withInitial(() -> request);
        responseThreadLocal = ThreadLocal.withInitial(() -> response);
    }
}
