package com.jisuye.core;

/**
 * 封装参数类型
 * @author ixx
 * @date 2019-07-14
 */
public class SquareParam {
    /**
     * 是否是url参数
     */
    private boolean param;

    /**
     * 参数类型
     */
    private Class clazz;

    /**
     * 参数名（只有是url参数是才有）
     */
    private String paramName;

    public SquareParam(Class clazz){
        this.param = false;
        this.clazz = clazz;
    }

    public SquareParam(String paramName, Class clazz){
        this.param = true;
        this.paramName = paramName;
        this.clazz = clazz;
    }

    public boolean isParam() {
        return param;
    }

    public void setParam(boolean param) {
        this.param = param;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }
}
