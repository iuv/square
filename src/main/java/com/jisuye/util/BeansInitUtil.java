package com.jisuye.util;

import com.jisuye.annotations.Component;
import com.jisuye.annotations.Service;
import com.jisuye.core.BeanObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Bean初始化类
 * @author ixx
 * @date 2019-06-20
 */
public class BeansInitUtil {
    private static final Logger log = LoggerFactory.getLogger(BeansInitUtil.class);

    public static Map<String, BeanObject> init(Class clazz){
        Map<String, BeanObject> beansMap = new HashMap<>();
        String path = clazz.getResource("").getPath();
        log.info("===bean init path:{}", path);
        File root = new File(path);
        initFile(root, beansMap);
        return beansMap;
    }

    private static void initFile(File file, Map<String, BeanObject> map){
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
    private static void loadClass(File file, Map<String, BeanObject> map){
        if(file == null){
            return;
        }
        try {
            BeanObject beanObject = new BeanObject();
            log.info("load bean path:{}", file.getPath());
            String path = file.getPath();
            path = path.substring(path.indexOf("classes")+8).replace(".class", "");
            path = path.replace("\\", ".");
            Class clzz = Class.forName(path);
            Annotation[] annotations = clzz.getAnnotations();
            if(annotations.length >0 && filterAnnotation(annotations)){
                beanObject.setAnnotaions(annotations);
                beanObject.setSimpleName(clzz.getSimpleName());
                beanObject.setClassName(clzz.getName());
                beanObject.setInterfacs(clzz.getInterfaces());
                beanObject.setPackages(clzz.getPackage().toString());
                Object obj = clzz.newInstance();
                beanObject.setObject(obj);
                // 按接口设置bean
                for (Class aClass : beanObject.getInterfacs()) {
                    map.put(aClass.getName(), beanObject);
                }
                // 按类设置bean
                map.put(beanObject.getClassName(), beanObject);
                // 按注解输入value设置bean
                for (Annotation annotation : annotations) {
                    String tmp_name = "";
                    if(annotation instanceof Service){
                        ((Service)annotation).value();
                    } else if(annotation instanceof Component) {
                        ((Component) annotation).value();
                    }
                    if(tmp_name != null && !tmp_name.equals("")) {
                        map.put(tmp_name, beanObject);
                    }
                }
            }
        } catch (Exception e) {
            log.error("init bean error:{}", file.getPath(), e);
        }
    }
    private static boolean filterAnnotation(Annotation[] annotations){
        boolean b = false;
        for (Annotation annotation : annotations) {
            b = annotation instanceof Service || annotation instanceof Component;
        }
        return b;
    }
}
