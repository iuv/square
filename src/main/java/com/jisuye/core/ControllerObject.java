package com.jisuye.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jisuye.exception.SquareException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * controller对象
 * @author ixx
 * @date 2019-07-14
 */
public class ControllerObject {
    private static final Logger log = LoggerFactory.getLogger(ControllerObject.class);

    /**
     * bean 实例
     */
    private Object object;

    /**
     * 具体方法
     */
    private Method method;

    /**
     * http请求方式
     */
    private String httpMethod;

    /**
     * 参数列表（获取@RequestParam指定的参数名，或者@RequestBody指定的类）
     */
    private SquareParam[] params;

    /**
     * 反射执行controller方法
     * @param req
     * @return
     */
    public Object invoke(HttpServletRequest req){
        Object[] os = new Object[params.length];
        int i = 0;
        for (SquareParam param : params) {
            // 如果是String类型则当然参数名从req中取值 否则 当做类 去反射生成
            if(param.isParam()){
                os[i++] = toBasicDataType(req.getParameter(param.getParamName()), param.getClazz());
            } else {
                String body = getBody(req);
                Object tmp = JSON.toJavaObject(JSONObject.parseObject(body), param.getClazz());
                os[i++] = tmp;
            }
        }
        try {
            Object o = this.getMethod().invoke(this.getObject(), os);
            return o;
        } catch (Exception e) {
            log.error("Controller method.invoke() is error!", e);
            throw new SquareException("Controller method.invoke() is error!", e);
        }
    }

    private Object toBasicDataType(Object obj, Class clazz){
        if(obj == null || clazz == null){
            return obj;
        }
        switch (clazz.getName()){
            case "int":
            case "java.lang.Integer" : obj = Integer.parseInt(obj.toString()); break;
            case "long":
            case "java.lang.Long" : obj = Long.parseLong(obj.toString()); break;
            case "double":
            case "java.lang.Double" : obj = Double.parseDouble(obj.toString()); break;
            case "float":
            case "java.lang.Float" : obj = Float.parseFloat(obj.toString()); break;
            case "boolean":
            case "java.lang.Boolean" : obj = Boolean.parseBoolean(obj.toString()); break;
            case "char":
            case "java.lang.Character" : obj = obj.toString().charAt(0); break;
            case "byte":
            case "java.lang.Byte" : obj = Byte.parseByte(obj.toString()); break;
            default: break;
        }
        return obj;
    }
    
    public String getBody(HttpServletRequest req){
        String body = "";
        try {
            BufferedReader br = req.getReader();
            String tmp;
            while ((tmp = br.readLine()) != null){
                body += tmp;
            }
        } catch (IOException e) {
            log.error("getBody data error!", e);
            throw new SquareException("Controller parameter getBody data error!", e);
        }
        return body;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public SquareParam[] getParams() {
        return params;
    }

    public void setParams(SquareParam[] params) {
        this.params = params;
    }
}
