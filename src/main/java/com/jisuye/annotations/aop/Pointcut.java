package com.jisuye.annotations.aop;

/**
 * 切入点
 * @author ixx
 * @date 2019-07-21
 */
public @interface Pointcut {
    String execution() default "";
}
