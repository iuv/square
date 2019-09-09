package com.jisuye.core;

import java.util.*;

/**
 * beans容器
 * @author ixx
 * @date 2019-07-14
 */
public class BeansMap {
    // bean容器
    private static HashMap<String, BeanObject> beans = new HashMap<>();

    // 保存遍历的所有class
    private static List<Class> classList = new ArrayList<>();

    // controller容器
    private static HashMap<String, ControllerObject> controllers = new HashMap<>();

    // aop容器
    private static HashMap<String, AspectObject> aops = new HashMap<>();

    public static void putAop(String key, AspectObject aspectObject){
        aops.put(key, aspectObject);
    }

    public static AspectObject getAop(String key){
        return aops.get(key);
    }

    public static void putController(String key, ControllerObject controllerObject){
        controllers.put(key, controllerObject);
    }

    public static ControllerObject getController(String key){
        return controllers.get(key);
    }

    public static void put(String key, BeanObject beanObject){
        beans.put(key, beanObject);
    }

    public static Set<String> keySet(){
        return beans.keySet();
    }

    public static BeanObject get(String key){
        return beans.get(key);
    }

    public static Set<Map.Entry<String, BeanObject>> entrySet(){
        return beans.entrySet();
    }

    public static int size(){
        return beans.size();
    }

    public static void addClass(Class clzz){
        classList.add(clzz);
    }
    public static List<Class> getClassList(){
        return classList;
    }
}
