package com.jisuye.util;

import com.jisuye.annotations.*;
import com.jisuye.annotations.aop.Aspect;
import com.jisuye.annotations.aop.Before;
import com.jisuye.annotations.aop.Pointcut;
import com.jisuye.annotations.web.*;
import com.jisuye.core.*;
import com.jisuye.exception.SquareException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Bean初始化类
 * @author ixx
 * @date 2019-06-20
 */
public class BeansInitUtil {
    private static final Logger log = LoggerFactory.getLogger(BeansInitUtil.class);

    public static void init(Class clazz){
        String path = clazz.getResource("").getPath();
        log.info("===bean init path:{}", path);
        if(path.indexOf("!")<0) {
            File root = new File(path);
            // 处理控制反转(加载aop切面,controller)
            initFile(root);
        } else {
            // 处理jar包内的反射逻辑
            initJar(path);
        }
        // 处理aop类
        initAop();
        // 处理config
        initConfig();
        // 处理依赖注入
        initDI();
    }

    private static void initConfig(){
        // 循环所有Bean处理Config
        Set<String> keySet = BeansMap.keySet();
        String[] keys = new String[keySet.size()];
        keySet.toArray(keys);
        List<Object> list = new ArrayList<>();
        for (String key : keys) {
            BeanObject beanObject = BeansMap.get(key);
            if(list.contains(beanObject)){
                continue;
            }
            list.add(beanObject);
            for (Annotation annotaion : beanObject.getAnnotaions()) {
                // 如果是配置则调用config()方法
                if(annotaion instanceof Config){
                    try {
                        Method method = beanObject.getBeanClass().getMethod("config");
                        method.invoke(beanObject.getObject());
                    } catch (Exception e) {
                        log.error("execute config method error!!", e);
                    }
                }
            }
        }
    }

    private static void initAop(){
        // 循环所有Bean处理Aop
        for(Map.Entry entry : BeansMap.entrySet()) {
            BeanObject beanObject = (BeanObject) entry.getValue();
            // 如果已经处理过，则跳过
            if (beanObject.getObject() != null) {
                continue;
            }
            beanObject.setObject(getInstance(beanObject.getBeanClass(), beanObject.getSrcObj()));
        }
    }

    private static void loadAop(Class clzz){
        try {
            Annotation[] annotations = clzz.getAnnotations();
            if (annotations.length > 0 && filterAspectAnnotation(annotations)) {
                Object obj = clzz.newInstance();
                Method[] methods = clzz.getMethods();
                for (Method method : methods) {
                    Annotation[] methodAnnotations = method.getAnnotations();
                    if(methodAnnotations != null && methodAnnotations.length > 0){
                        // 切点
                        if(methodAnnotations[0] instanceof Pointcut){
                            AspectObject aspectObject = BeansMap.getAop(method.getName());
                            if(aspectObject == null){
                                aspectObject = new AspectObject();
                                aspectObject.setAspectBean(obj);
                            }
                            String packageStr = setPointPackageAndMethod(((Pointcut)methodAnnotations[0]).value(), aspectObject, method);
                            BeansMap.putAop(packageStr, aspectObject);
                            BeansMap.putAop(method.getName(), aspectObject);
                        } else if(methodAnnotations[0] instanceof Before){
                            // Before 处理
                            String val = ((Before)methodAnnotations[0]).value();
                            val = val.substring(0, val.indexOf("("));
                            AspectObject aspectObject1 = BeansMap.getAop(val);
                            if(aspectObject1 == null){
                                aspectObject1 = new AspectObject();
                                aspectObject1.setAspectBean(obj);
                            }
                            aspectObject1.setType("before");
                            aspectObject1.setMethodMap(val, method);
                            BeansMap.putAop(val, aspectObject1);
                        }
                    }
                }
            }
        } catch (Exception e){
            log.error("load aop error!!", e);
        }
    }

    private static String setPointPackageAndMethod(String ex, AspectObject aspectObject, Method method){
        if(ex == null || ex.equals("")){
            throw new SquareException("Aop Acpect config is error, pointCut must value!");
        }
        ex = ex.replace("excution(", "");
        ex = ex.substring(0, ex.length()-1);
        String[] exs = ex.split(" ");
        if(exs.length != 3){
            throw new SquareException("Aop Acpect config is error!");
        } else {
            String packages = exs[2];
            String classStr = packages.replaceAll("\\.[a-zA-Z0-9\\*]*\\(.*\\)", "");
            String methods = packages.substring(classStr.length());
            methods = methods.substring(1).replace("*", ".*");
            String packageStr = classStr.replaceAll("\\.[a-zA-Z0-9\\*]*$", "");
            classStr = classStr.substring(packageStr.length());

            // 设置 包名 返回类型匹配 类名匹配 方法匹配
            aspectObject.setPackagePath(packageStr);
            aspectObject.setMethodEx(methods);
            aspectObject.setMethodMap(methods, method);
            aspectObject.setRetClass(exs[1].replace("*", ".*"));
            aspectObject.setClassName(classStr.substring(1).replace("*", ".*"));
            return packageStr;
        }
    }
    private static void initJar(String jarPath){
        try {
            log.info("jarPath :{}", jarPath);
            String packageStr = jarPath.substring(jarPath.indexOf("!")+2).replaceAll("/", ".");
            log.info("packageStr :{}", packageStr);
            jarPath = jarPath.substring(0, jarPath.indexOf("!")).replace("file:", "");
            log.info("jar file path:{}", jarPath);
            JarFile jarFile = new JarFile(new File(jarPath));
            // 获取jar文件条目
            Enumeration<JarEntry> enumFiles = jarFile.entries();
            JarEntry entry;
            while(enumFiles.hasMoreElements()){
                entry = enumFiles.nextElement();
                String className = entry.getName().replaceAll("/", ".");
                // 只处理自己包下的class文件
                if(className.startsWith(packageStr) && className.indexOf(".class")>=0){
                    className = className.substring(0,className.length()-6).replace("/", ".");
                    log.info("class:{}", className);
                    loadClass(className);
                }
            }
        } catch (IOException e) {
            log.error("load jar file error!", e);
        }
    }
    private static void initFile(File file){
        File[] fs = file.listFiles();
        for (File f : fs) {
            if(f.isDirectory()){
                // 递归目录
                initFile(f);
            } else {
                // 处理class
                if(f.getPath().endsWith("class")) {
                    loadClass(getClassPath(f.getPath()));
                }
            }
        }
    }
    private static String getClassPath(String filePath){
        String path = filePath;
        path = path.substring(path.indexOf("classes")+8).replace(".class", "");
        path = path.replace(File.separatorChar+"", ".");
        return path;
    }
    private static void loadClass(String className){
        if(className == null){
            return;
        }
        try {
            log.info("load bean class:{}", className);
            Class clzz = Class.forName(className);
            Annotation[] annotations = clzz.getAnnotations();
            // 保存所有类
            BeansMap.addClass(clzz);
            if(annotations.length >0 && filterClassAnnotation(annotations)){
                BeanObject beanObject = new BeanObject(clzz, clzz.newInstance());
                beanObject.setAnnotaions(annotations);
                // 按接口设置bean
                for (Class aClass : beanObject.getInterfacs()) {
                    BeanObject tmp = BeansMap.get(aClass.getName());
                    if(tmp != null){
                        beanObject.setNext(tmp);
                    }
                    BeansMap.put(aClass.getName(), beanObject);
                }
                // 按类设置bean
                BeansMap.put(beanObject.getClassName(), beanObject);
                String simpleName = StringUtil.firstToLowerCase(beanObject.getSimpleName());
                if(BeansMap.get(simpleName) != null){
                    throw new SquareException("There are duplicate beans ，beanName:"+simpleName);
                }
                BeansMap.put(simpleName, beanObject);
                // 按注解输入value设置bean
                for (Annotation annotation : annotations) {
                    String tmp_name = "";
                    if(annotation instanceof Service){
                        tmp_name = ((Service)annotation).value();
                    } else if(annotation instanceof Component) {
                        tmp_name = ((Component)annotation).value();
                    } else if(annotation instanceof Controller) {
                        initController(clzz, ((Controller)annotation).value());
                    } else if(annotation instanceof Aspect){
                        loadAop(clzz);
                    }
                    if(tmp_name != null && !tmp_name.equals("")) {
                        if(BeansMap.get(tmp_name) != null){
                            throw new SquareException("There are duplicate beans ，beanName:"+tmp_name);
                        }
                        BeansMap.put(tmp_name, beanObject);
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
     * 获取反射实例，判断是否符合切面，是否需要做代理处理
     * @param clzz
     * @return
     */
    private static Object getInstance(Class clzz, Object obj){
        String packageStr = clzz.getPackage().getName();
        // 如果不符合切面规则，则直接反射生成 不使用代理
        if(BeansMap.getAop(packageStr) == null){
            return obj;
        }
        // 使用JDK动态代理
        Class[] interfaces = clzz.getInterfaces();
        if(interfaces == null || interfaces.length == 0){
            log.warn("{} Aop proxy class must implements interface!!", clzz.getName());
            return obj;
        }
        SquareProxyHandler squareProxyHandler = new SquareProxyHandler(obj);
        Object newProxyInstance = Proxy.newProxyInstance(interfaces[0].getClassLoader(), new Class[]{interfaces[0]}, squareProxyHandler);
        return newProxyInstance;
    }
    /**
     * 初始化Controller
     * @param clzz
     * @param classPath
     */
    private static void initController(Class clzz, String classPath){
        try {
            classPath = classPath.equals("/") ? "" : classPath;
            Method[] methods = clzz.getMethods();
            log.info("classPath:{}, methods.length:{}", classPath, methods.length);
            // 处理每一个方法
            for (Method method : methods) {
                String[] methodPath = getMethodAnnotationValue(method.getDeclaredAnnotations());
                // 说明是@GetMapping @PostMapping @PutMapping @DeleteMapping 中的一个
                if (methodPath != null) {
                    // 获取参数及注解
                    Parameter[] parameters = method.getParameters();
                    SquareParam[] params = new SquareParam[parameters.length];
                    int i = 0;
                    for (Parameter parameter : parameters) {
                        params[i++] = getParam(parameter.getAnnotations(), parameter.getType());
                    }
                    ControllerObject co = new ControllerObject(params, methodPath[0], method, StringUtil.firstToLowerCase(clzz.getSimpleName()));
                    String key = methodPath[0] +":"+ classPath+methodPath[1];
                    log.info("add controller key:{}", key);
                    BeansMap.putController(key, co);
                }
            }
        } catch (Exception e){
            log.error("init controller error.", e);
            throw new SquareException("init controller error", e);
        }
    }
    /**
     * 处理关系依赖
     */
    private static void initDI(){
        List<BeanObject> list = new ArrayList<>();
        BeanObject sqlBean = null;
        // 循环所有Bean处理依赖
        for(Map.Entry entry : BeansMap.entrySet()){
            BeanObject beanObject = (BeanObject)entry.getValue();
            // 如果已经处理过，则跳过
            if(list.contains(beanObject)){
                continue;
            }
            // 添加到已处理列表
            list.add(beanObject);
            // 先判断是否有Resource注解
            for (Field field : beanObject.getFields()) {
                if(filterFieldAnnotation(field.getAnnotations())){
                    String name = getResourceName(field.getAnnotations());
                    BeanObject bean;
                    // 有指定bean名字按指定去取
                    if(name != null && !name.equals("")){
                        bean = BeansMap.get(StringUtil.firstToLowerCase(name));
                    } else {
                        // 没有指定按接口（如果有的话）或类型去取
                        Class fieldClass = field.getType();
                        bean = BeansMap.get(fieldClass.getName());
                        // 如果有next说明是有多个实现的接口，则要判断名字
                        if(bean != null && bean.getNext() != null){
                            String fieldName = field.getName();
                            while(bean != null){
                                if(StringUtil.firstToLowerCase(bean.getSimpleName()).equals(fieldName)){
                                    break;
                                }
                                bean = bean.getNext();
                            }
                            if(bean == null){
                                log.error("无法确定的Bean依赖，field:{}, 存在多个依赖！", beanObject.getClassName()+"."+fieldName);
                                throw new SquareException("无法确定的Bean依赖，存在多个依赖！");
                            }
                        } else if(fieldClass.getName().equals(JdbcTemplate.class.getName())){
                            // 如果是JdbcTemplate依赖，则初始化DbUtil并初始化及注入JdbcTemplate
                            if(sqlBean == null) {
                                DbUtil.init();
                                JdbcTemplate jdbcTemplate = new JdbcTemplate();
                                sqlBean = new BeanObject(JdbcTemplate.class, jdbcTemplate);
                                sqlBean.setObject(jdbcTemplate);
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
                        field.set(beanObject.getSrcObj(), bean.getObject());
                    } catch (IllegalAccessException e) {
                        log.error("Bean注入失败，field:{}", beanObject.getClassName()+"."+field.getName(), e);
                        throw new SquareException("Bean注入失败");
                    }
                }
            }
        }
        BeansMap.put(JdbcTemplate.class.getName(), sqlBean);
    }
    /** * 判断类上加的注解是不是要初始化为bean */
    private static boolean filterClassAnnotation(Annotation[] annotations){
        boolean b = false;
        for (Annotation annotation : annotations) {
            b = annotation instanceof Service || annotation instanceof Component;
            b = b || annotation instanceof Controller;
            b = b || annotation instanceof Config;
            if(b){
                break;
            }
        }
        return b;
    }
    /** * 判断字段上加的注解是否需要做注入 */
    private static boolean filterFieldAnnotation(Annotation[] annotations){
        boolean b = false;
        for (Annotation annotation : annotations) {
            b = annotation instanceof Resource;
            if(b){
                break;
            }
        }
        return b;
    }
    /** * 判断类上加的注解是不是Aspect */
    private static boolean filterAspectAnnotation(Annotation[] annotations){
        boolean b = false;
        for (Annotation annotation : annotations) {
            b = annotation instanceof Aspect;
            if(b){
                break;
            }
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
            } else {
                continue;
            }
            break;
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
}