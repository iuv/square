package com.jisuye.util;

import com.jisuye.annotations.*;
import com.jisuye.annotations.web.*;
import com.jisuye.core.*;
import com.jisuye.exception.SquareException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Bean初始化类
 * @author ixx
 * @date 2019-06-20
 */
public class BeansInitUtil {
    private static final Logger log = LoggerFactory.getLogger(BeansInitUtil.class);

    public static BeansMap init(Class clazz, BeansMap beansMap){
        String path = clazz.getResource("").getPath();
        log.info("===bean init path:{}", path);
        File root = new File(path);
        // 处理控制反转
        initFile(root, beansMap);
        // 处理依赖注入
        initDI(beansMap);
        return beansMap;
    }

    private static void initFile(File file, BeansMap map){
        File[] fs = file.listFiles();
        for (File f : fs) {
            if(f.isDirectory()){
                // 递归目录
                initFile(f, map);
            } else {
                // 处理class
                loadClass(f, map);
            }
        }
    }
    private static String getClassPath(File file){
        String path = file.getPath();
        path = path.substring(path.indexOf("classes")+8).replace(".class", "");
        path = path.replace("\\", ".");
        return path;
    }
    private static void loadClass(File file, BeansMap map){
        if(file == null){
            return;
        }
        try {
            BeanObject beanObject = new BeanObject();
            log.info("load bean path:{}", file.getPath());
            Class clzz = Class.forName(getClassPath(file));
            Annotation[] annotations = clzz.getAnnotations();
            if(annotations.length >0 && filterClassAnnotation(annotations)){
                beanObject.setAnnotaions(annotations);
                Object obj = clzz.newInstance();
                beanObject.setClass(clzz);
                beanObject.setObject(obj);
                // 按接口设置bean
                for (Class aClass : beanObject.getInterfacs()) {
                    BeanObject tmp = map.get(aClass.getName());
                    if(tmp != null){
                        beanObject.setNext(tmp);
                    }
                    map.put(aClass.getName(), beanObject);
                }
                // 按类设置bean
                map.put(beanObject.getClassName(), beanObject);
                String simpleName = firstToLowerCase(beanObject.getSimpleName());
                if(map.get(simpleName) != null){
                    throw new SquareException("There are duplicate beans ，beanName:"+simpleName);
                }
                map.put(simpleName, beanObject);
                // 按注解输入value设置bean
                for (Annotation annotation : annotations) {
                    String tmp_name = "";
                    if(annotation instanceof Service){
                        tmp_name = ((Service)annotation).value();
                    } else if(annotation instanceof Component) {
                        tmp_name = ((Component)annotation).value();
                    } else if(annotation instanceof Controller) {
                        initController(clzz, ((Controller)annotation).value(), map);
                    }
                    if(tmp_name != null && !tmp_name.equals("")) {
                        if(map.get(tmp_name) != null){
                            throw new SquareException("There are duplicate beans ，beanName:"+tmp_name);
                        }
                        map.put(tmp_name, beanObject);
                    }
                }
            }
        } catch (SquareException e) {
            throw e;
        } catch (Exception e){
            log.error("Bean init error...", e);
            throw new SquareException("Bean init error....");
        }
    }

    /**
     * 初始化Controller
     * @param clzz
     * @param classPath
     */
    private static void initController(Class clzz, String classPath, BeansMap beansMap){
        try {
            Method[] methods = clzz.getMethods();
            // 处理每一个方法
            for (Method method : methods) {
                Annotation[] annotations = method.getDeclaredAnnotations();
                String[] methodPath = getMethodAnnotationValue(annotations);
                // 说明是@GetMapping @PostMapping @PutMapping @DeleteMapping 中的一个
                if (methodPath != null) {
                    // 获取参数及注解
                    Parameter[] parameters = method.getParameters();
                    SquareParam[] params = new SquareParam[parameters.length];
                    int i = 0;
                    for (Parameter parameter : parameters) {
                        Annotation[] paramAnnotations = parameter.getAnnotations();
                        SquareParam param = getParam(paramAnnotations, parameter.getType());
                        params[i++] = param;
                    }
                    ControllerObject co = new ControllerObject();
                    co.setParams(params);
                    co.setHttpMethod(methodPath[0]);
                    co.setMethod(method);
                    co.setObject(beansMap.get(firstToLowerCase(clzz.getSimpleName())).getObject());
                    String key = methodPath[0] +":"+ classPath+methodPath[1];
                    beansMap.putController(key, co);
                }
            }
        } catch (Exception e){
            log.error("init controller error.", e);
            throw new SquareException("init controller error", e);
        }
    }
    /**
     * 处理关系依赖
     * @param map Bean容器
     */
    private static void initDI(BeansMap map){
        List<BeanObject> list = new ArrayList<>();
        BeanObject sqlBean = null;
        // 循环所有Bean处理依赖
        for(Map.Entry entry : map.entrySet()){
            BeanObject beanObject = (BeanObject)entry.getValue();
            // 如果已经处理过，则跳过
            if(list.contains(beanObject)){
                break;
            }
            // 添加到已处理列表
            list.add(beanObject);
            // 先判断是否有Resource注解
            for (Field field : beanObject.getFields()) {
                if(filterFieldAnnotation(field.getAnnotations())){
                    String name = getResourceName(field.getAnnotations());
                    BeanObject bean = null;
                    // 有指定bean名字按指定去取
                    if(name != null && !name.equals("")){
                        bean = map.get(firstToLowerCase(name));
                    } else {
                        // 没有指定按接口（如果有的话）或类型去取
                        Class fieldClass = field.getType();
                        bean = map.get(fieldClass.getName());
                        // 如果有next说明是有多个实现的接口，则要判断名字
                        if(bean != null && bean.getNext() != null){
                            String fieldName = field.getName();
                            while(bean != null){
                                if(firstToLowerCase(bean.getSimpleName()).equals(fieldName)){
                                    break;
                                }
                                bean = bean.getNext();
                            }
                            if(bean == null){
                                // 多于两个匹配的bean异常
                                log.error("无法确定的Bean依赖，field:{}, 存在多个依赖！", beanObject.getClassName()+"."+fieldName);
                                throw new SquareException("无法确定的Bean依赖，存在多个依赖！");
                            }
                        } else if(fieldClass.getName().equals(JdbcTemplate.class.getName())){
                            // 如果是JdbcTemplate依赖，则初始化DbUtil并初始化及注入JdbcTemplate
                            if(sqlBean == null) {
                                DbUtil.init();
                                sqlBean = new BeanObject();
                                sqlBean.setClass(JdbcTemplate.class);
                                sqlBean.setObject(new JdbcTemplate());
                            }
                            bean = sqlBean;
                        }
                    }
                    if(bean == null){
                        // 找不到依赖bean异常
                        log.error("无法找到Bean依赖，field:{}", beanObject.getClassName()+"."+field.getName());
                        throw new SquareException("无法找到Bean依赖");
                    }
                    // 注入依赖
                    try {
                        field.setAccessible(true);
                        field.set(beanObject.getObject(), bean.getObject());
                    } catch (IllegalAccessException e) {
                        log.error("Bean注入失败，field:{}", beanObject.getClassName()+"."+field.getName(), e);
                        throw new SquareException("Bean注入失败");
                    }
                }
            }
        }
        map.put(JdbcTemplate.class.getName(), sqlBean);
    }
    /** * 判断类上加的注解是不是要初始化为bean */
    private static boolean filterClassAnnotation(Annotation[] annotations){
        boolean b = false;
        for (Annotation annotation : annotations) {
            b = annotation instanceof Service || annotation instanceof Component;
            b = b || annotation instanceof Controller;
        }
        return b;
    }
    /** * 判断字段上加的注解是否需要做注入 */
    private static boolean filterFieldAnnotation(Annotation[] annotations){
        boolean b = false;
        for (Annotation annotation : annotations) {
            b = annotation instanceof Resource;
        }
        return b;
    }
    /** * 获取方法上的注解value */
    private static String[] getMethodAnnotationValue(Annotation[] annotations){
        String[] methodPath = null;
        for (Annotation annotation : annotations) {
            if(annotation instanceof DeleteMapping){
                methodPath = new String[]{"delete", ((DeleteMapping) annotation).value()};
            } else if(annotation instanceof GetMapping){
                methodPath = new String[]{"get", ((GetMapping) annotation).value()};
            } else if(annotation instanceof PostMapping){
                methodPath = new String[]{"post", ((PostMapping) annotation).value()};
            } else if(annotation instanceof PutMapping){
                methodPath = new String[]{"put", ((PutMapping) annotation).value()};
            }
        }
        return methodPath;
    }
    /** * 获取参数@RequestParam注解的value 如果没有，则取参数名*/
    private static SquareParam getParam(Annotation[] annotations, Class clazz){
        SquareParam param = null;
        for (Annotation annotation : annotations) {
            if(annotation instanceof RequestParam){
                param = new SquareParam(((RequestParam) annotation).value(), clazz);
            } else if(annotation instanceof RequestBody){
                param = new SquareParam(clazz);
            }
        }
        return param;
    }
    /**  获取注入注解上指定的Bean名字 */
    private static String getResourceName(Annotation[] annotations){
        String name = null;
        for (Annotation annotation : annotations) {
            name = ((Resource)annotation).name();
        }
        return name;
    }

    /** 首字段转大写*/
    public static String firstToUpperCase(String str){
        if(str == null || str.equals("")){
            return str;
        }
        char f = str.charAt(0);
        str = str.substring(1);
        if(f>'Z'){
            f = (char)(f-32);
        }
        return f+str;
    }

    /** 首字段转大写*/
    public static String firstToLowerCase(String str){
        if(str == null || str.equals("")){
            return str;
        }
        char f = str.charAt(0);
        str = str.substring(1);
        if(f<'a'){
            f = (char)(f+32);
        }
        return f+str;
    }
}
