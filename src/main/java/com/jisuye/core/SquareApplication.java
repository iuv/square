package com.jisuye.core;

import com.jisuye.util.ArgsToKVUtil;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;

/**
 * 项目启动类
 * @author ixx
 * @date 2019-6-13
 */
public class SquareApplication {
    private static final Logger log = LoggerFactory.getLogger(SquareApplication.class);
    private static Tomcat tomcat = null;
    private static String CONTEXT_PATH = "/";
    private static String ENCODING = "UTF-8";
    private static int TOMCAT_PORT = 8080;

    public static void run(Class clzz, String[] args) {
        try {
            long startTime = System.currentTimeMillis();
            // 初始化参数
            setArgs(args);
            String project_path = clzz.getResource("").getPath();
            project_path = project_path.substring(0, project_path.indexOf("classes"));
            // 输出banner
            printBanner(project_path);
            tomcat = new Tomcat();
            // 设置Tomcat的工作目录:工程根目录/Tomcat
            tomcat.setBaseDir(project_path + "/Tomcat");
            tomcat.setPort(TOMCAT_PORT);
            tomcat.addWebapp(CONTEXT_PATH, project_path);
            // 执行这句才能支持JDNI查找
            tomcat.enableNaming();
            tomcat.getConnector().setURIEncoding(ENCODING);
            tomcat.start();
            log.info("Tomcat started on port(s): {} with context path '{}'", TOMCAT_PORT, CONTEXT_PATH);
            log.info("Started Application in {} ms." , (System.currentTimeMillis() - startTime));
            // 保持服务器进程
            tomcat.getServer().await();
        } catch (Exception e) {
            log.error("Application startup failed...", e);
        }
    }

    /**
     * 初始化参数
     * @param args
     */
    private static void setArgs(String[] args){
        Map<String, String> map = ArgsToKVUtil.convert(args);
        if(map.get("--server.port") != null){
            TOMCAT_PORT = Integer.parseInt(map.get("--server.port"));
        }
    }

    /**
     * 输出banner图
     */
    private static void printBanner(String projectPath){
        BufferedReader br = null;
        try{
            File f = new File(projectPath + "/classes/default-banner.txt");
            br = new BufferedReader(new FileReader(f));
            StringBuilder stringBuilder = new StringBuilder("\n");
            String line;
            while ((line = br.readLine()) != null){
                stringBuilder.append(line).append("\n");
            }
            log.info(stringBuilder.toString());
        } catch (Exception e){
            log.info("load banner file error!!", e);
            if(br != null){
                try {
                    br.close();
                } catch (IOException e1) {
                }
            }
        }
    }

}
