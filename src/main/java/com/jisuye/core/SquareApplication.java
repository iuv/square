package com.jisuye.core;

import com.jisuye.exception.SquareException;
import com.jisuye.util.*;
import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.Wrapper;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.StandardRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 项目启动类
 * @author ixx
 * @date 2019-6-13
 */
public class SquareApplication {
    private static final Logger log = LoggerFactory.getLogger(SquareApplication.class);
    private static Map<String, Object> CONF_MAP = new HashMap<>();
    private static Tomcat tomcat = null;
    private static String CONTEXT_PATH = "/";
    private static String ENCODING = "UTF-8";
    private static String SUFFIXS = "htm,html,css,js,jpg,jpeg,png,gif,json,mp4,mp3,avi,rm";
    private static int TOMCAT_PORT = 8080;
    private static ClassesPathUtil classesPathUtil;
    public static String TEMP_TOMCAT_DIR = "/tmp/square/tomcat";

    public static void run(Class clzz, String[] args) {
        try {
            long startTime = System.currentTimeMillis();
            ApplicationContext.init(CONF_MAP);
            classesPathUtil = new ClassesPathUtil(clzz);
            // 加载配置
            loadYaml(classesPathUtil.getProjectPath());
            // 初始化参数
            setArgs(args);
            // 输出banner
            printBanner(classesPathUtil.getProjectPath());
            BeansInitUtil.init(clzz);
            log.info("beans size is:{}", BeansMap.size());
            //查看bean是否注入成功
            tomcat = new Tomcat();
            tomcat.setPort(TOMCAT_PORT);
            // 设置Tomcat工作目录
            File f = new File(TEMP_TOMCAT_DIR);
            if(!f.exists()){
                f.mkdirs();
            }
            f.deleteOnExit();
            tomcat.setBaseDir(f.getPath());
            Context context = tomcat.addWebapp(CONTEXT_PATH, classesPathUtil.getPublicPath());
            // jar包中静态文件特殊处理
            if(classesPathUtil.getProjectPath().indexOf("!")>0) {
                String jar = "jar:"+classesPathUtil.getProjectPath()+"/";
                URL url = new URL(jar);
                context.setResources(new StandardRoot(context));
                context.getResources().createWebResourceSet(WebResourceRoot.ResourceSetType.RESOURCE_JAR, "/", url, "/public");
            }
            // 添加DsipatcherServlet
            Wrapper wrapper = Tomcat.addServlet(context, "DispatcherServlet", new DispatcherServlet());
            wrapper.addMapping("/");
            // 设置静态资源走默认Servlet处理
            Tomcat.addServlet(context, "DefaultServlet", new DefaultServlet());
            String[] suffixs = SUFFIXS.split(",");
            for (String suffix : suffixs) {
                context.addServletMappingDecoded("*."+suffix, "DefaultServlet");
            }
            // 执行这句才能支持JDNI查找
            tomcat.enableNaming();
            tomcat.getConnector().setURIEncoding(ENCODING);
            tomcat.start();
            log.info("Tomcat started on port(s): {} with context path '{}'", TOMCAT_PORT, CONTEXT_PATH);
            log.info("Started Application in {} ms." , (System.currentTimeMillis() - startTime));
            // 保持服务器进程
            tomcat.getServer().await();
        } catch (Exception e) {
            if(e instanceof SquareException){
                log.error(((SquareException) e).getMsg());
            }
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
     * 加载配置文件
     * @param projectPath
     */
    private static void loadYaml(String projectPath){
        CONF_MAP.putAll(LoadApplicationYmlUtil.load(projectPath));
        if(CONF_MAP.get("server.port") != null){
            TOMCAT_PORT = (Integer)CONF_MAP.get("server.port");
        }
        if(CONF_MAP.get("server.servlet.context-path") != null){
            CONTEXT_PATH = (String)CONF_MAP.get("server.servlet.context-path");
        }
    }

    /**
     * 输出Banner图
     * @param projectPath
     */
    private static void printBanner(String projectPath){
        BufferedReader br = null;
        try{
            File f = new File(projectPath+"/default-banner.txt");
            if(f.exists()){
                br = new BufferedReader(new FileReader(f));
            } else {
                InputStream is = SquareApplication.class.getClassLoader().getResourceAsStream("default-banner.txt");
                br = new BufferedReader(new InputStreamReader(is));
            }
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
