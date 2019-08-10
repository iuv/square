package com.jisuye.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Square 代理类，使用JDK自带代理，只支持接口方式
 * @author ixx
 * @date 2019-08-03
 */
public class SquareProxyHandler implements InvocationHandler {

    private Object obj;

    public SquareProxyHandler(Object obj){
        this.obj = obj;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 处理Aop 逻辑
        String packageClass = obj.getClass().getName();
        // 包名
        String packageStr = packageClass.substring(0, packageClass.lastIndexOf("."));
        // 类名
        String classStr = packageClass.substring(packageStr.length()+1);
        // 获取AspectObject
        AspectObject aspectObject = BeansMap.getAop(packageStr);
        if(aspectObject != null){
            // 匹配类名，方法名，返回值类型  看是否需符合切面规则
            Method aspectMethod = verification(method, classStr, aspectObject);
            if(aspectMethod != null){
                // 前置
                if(aspectObject.getType().equals("before")){
                    aspectMethod.invoke(aspectObject.getAspectBean(), null);
                    Object o = method.invoke(obj, args);
                    return o;
                }
            }
        }
        Object o = method.invoke(obj, args);
        return o;
    }

    /**
     * 验证是否符合切面规则
     * @param method 当前要执行的方法
     * @param aspectObject 切面对象
     * @return
     */
    private Method verification(Method method, String className, AspectObject aspectObject){
        // 判断类名
        if(!Pattern.matches(aspectObject.getClassName(), className)){
            return null;
        }
        // 判断返回值类型
        if(!Pattern.matches(aspectObject.getRetClass(), method.getReturnType().getSimpleName())){
            return null;
        }
        // 判断方法名
        List<String> list = aspectObject.getMethodEx();
        String methodName = method.getName()+"(..)";
        for (String s : list) {
            if(Pattern.matches(s, methodName)){
                Method aspectMethod = aspectObject.getMethodMap().get(s);
                return aspectObject.getMethodMap().get(aspectMethod.getName());
            }
        }
        return null;
    }
}
