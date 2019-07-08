package com.jisuye.core;

import java.util.Map;

/**
 * 应用程序上下文
 * @author ixx
 * @date 2019-07-01
 */
public class ApplicationContext {
    private static Map<String, Object> CONF_MAP;
    private static Map<String, BeanObject> BEAN_MAP;

    public static void init(Map<String, Object> conf, Map<String, BeanObject> bean){
        CONF_MAP = conf;
        BEAN_MAP = bean;
    }

    public static BeanObject getBean(String name){
        return BEAN_MAP.get(name);
    }

    public static BeanObject betBean(Class clazz){
        return BEAN_MAP.get(clazz.getName());
    }

    public static Object getConf(String key){
        return CONF_MAP.get(key);
    }
}
