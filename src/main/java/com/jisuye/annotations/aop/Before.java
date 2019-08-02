package com.jisuye.annotations.aop;

/**
 * 前置aop处理
 * @author ixx
 * @date 2019-07-21
 */
public @interface Before {
    public String pointcut() default "";
}
