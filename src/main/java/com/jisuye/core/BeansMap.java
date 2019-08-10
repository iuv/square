package com.jisuye.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * beans容器
 * @author ixx
 * @date 2019-07-14
 */
public class BeansMap {
    // bean容器
    private static HashMap<String, BeanObject> beans = new HashMap<>();

    // controller容器
    private static HashMap<String, ControllerObject> controllers = new HashMap<>();

    // aop容器
    private static HashMap<String, AspectObject> aops = new HashMap<>();

    public void putAop(String key, AspectObject aspectObject){
        aops.put(key, aspectObject);
    }

    public static AspectObject getAop(String key){
        return aops.get(key);
    }

    public void putController(String key, ControllerObject controllerObject){
        controllers.put(key, controllerObject);
    }

    public ControllerObject getController(String key){
        return controllers.get(key);
    }

    public void put(String key, BeanObject beanObject){
        beans.put(key, beanObject);
    }

    public BeanObject get(String key){
        return beans.get(key);
    }

    public Set<Map.Entry<String, BeanObject>> entrySet(){
        return beans.entrySet();
    }

    public int size(){
        return beans.size();
    }
}
