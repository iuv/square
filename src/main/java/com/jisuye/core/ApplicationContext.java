package com.jisuye.core;

import java.util.Map;

/**
 * 应用程序上下文
 * @author ixx
 * @date 2019-07-01
 */
public class ApplicationContext {
    private static Map<String, Object> CONF_MAP;

    public static void init(Map<String, Object> conf){
        CONF_MAP = conf;
    }

    public static BeanObject getBean(String name){
        return BeansMap.get(name);
    }

    public static BeanObject getBean(Class clazz){
        return BeansMap.get(clazz.getName());
    }

    public static Object getConf(String key){
        return CONF_MAP.get(key);
    }
}
