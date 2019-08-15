package com.jisuye.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * 处理项目路径问题
 * @author ixx
 * @date 2019-06-15
 */
public class ClassesPathUtil {
    private static final Logger log = LoggerFactory.getLogger(ClassesPathUtil.class);
    /**
     * 项目目录(.../classes)
     */
    private String projectPath;
    /**
     * 静态资源目录(.../classes/public)
     */
    private String publicPath;

    public ClassesPathUtil(Class clzz){
        String basePath = clzz.getResource("").getPath();
        log.info("basePath+++++{}", basePath);
        //  ..../classes
        if(basePath.indexOf("classes")>0) {
            projectPath = basePath.substring(0, basePath.indexOf("classes") + 7);
        } else {
            projectPath = basePath.substring(0, basePath.indexOf("!")+1);
        }
        publicPath = setPublic(projectPath, "/public");
    }

    private String setPublic(String basePath, String path){
        File publicFile = new File(basePath+path);
        if(!publicFile.exists()){
            publicFile.mkdirs();
        }
        return basePath+path;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getPublicPath() {
        return publicPath;
    }

}
