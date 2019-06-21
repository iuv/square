package com.jisuye.core;

import java.lang.annotation.Annotation;

/**
 * 封装Bean对象
 * @author ixx
 * @date 2019-06-20
 */
public class BeanObject {
    /**
     * 类全名（带包路径）
     */
    private String className;
    /**
     * 类名
     */
    private String simpleName;
    /**
     * 实际对象
     */
    private Object object;
    /**
     * 包路径(com.jisuye)
     */
    private String packages;
    /**
     * 注解类型集合
     */
    private Annotation[] annotaions;

    /**
     * 接口名
     */
    private Class[] interfacs;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getPackages() {
        return packages;
    }

    public void setPackages(String packages) {
        this.packages = packages;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public Annotation[] getAnnotaions() {
        return annotaions;
    }

    public void setAnnotaions(Annotation[] annotaions) {
        this.annotaions = annotaions;
    }

    public Class[] getInterfacs() {
        return interfacs;
    }

    public void setInterfacs(Class[] interfacs) {
        this.interfacs = interfacs;
    }
}
