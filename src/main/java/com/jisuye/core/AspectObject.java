package com.jisuye.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AspectObject 封装
 * @author ixx
 * @date 2019-08-03
 */
public class AspectObject {
    /**
     * 包路径
     */
    private String packagePath;

    /**
     * 方法切面规则
     */
    private List<String> methodEx = new ArrayList<>();

    /**
     * 保存Aspect对象
     */
    private Object AspectBean;

    /**
     * 执行方法集
     * key : methodEx
     * value : AspectBean.method()
     */
    private Map<String, Method> methodMap = new HashMap<>();

    /**
     * 类匹配
     */
    private String className;

    /**
     * 返回类型
     */
    private String retClass;

    /**
     * 切面类型（before、after...)
     */
    private String type;

    public String getPackagePath() {
        return packagePath;
    }

    public void setPackagePath(String packagePath) {
        this.packagePath = packagePath;
    }

    public List<String> getMethodEx() {
        return methodEx;
    }

    public void setMethodEx(String methodEx) {
        this.methodEx.add(methodEx);
    }

    public Object getAspectBean() {
        return AspectBean;
    }

    public void setAspectBean(Object aspectBean) {
        AspectBean = aspectBean;
    }

    public Map<String, Method> getMethodMap() {
        return methodMap;
    }

    public void setMethodMap(String key, Method method) {
        this.methodMap.put(key, method);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRetClass() {
        return retClass;
    }

    public void setRetClass(String retClass) {
        this.retClass = retClass;
    }
}
