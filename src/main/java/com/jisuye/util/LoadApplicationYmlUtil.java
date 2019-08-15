package com.jisuye.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 加载application.yml文件
 * @author ixx
 * @date 2019-06-15
 */
public class LoadApplicationYmlUtil {
    private static final Logger log = LoggerFactory.getLogger(LoadApplicationYmlUtil.class);
    public static Map<String, Object> load(String projectPath){
        Map<String, Object> retMap = new HashMap<>();
        try {
            InputStream is;
            if(projectPath != null &&  projectPath.indexOf("!") > 0){
                is = ClassLoader.getSystemResourceAsStream("application.yml");
            } else {
                projectPath += "/application.yml";
                log.info("load yml file path:{}", projectPath);
                is = new FileInputStream(projectPath);
            }
            Yaml yaml = new Yaml();
            Map<String, Object> map = (Map<String, Object>)yaml.load(is);
            if(map != null && map.size()>0){
                for(Map.Entry e : map.entrySet()) {
                    convert("", retMap, e);
                }
            }
        } catch (FileNotFoundException e) {
            log.error("load application.yml file error.", e);
        }
        return retMap;
    }

    /**
     * 递归组装配置参数
     * @param key 父级key路径（类似server.servlet)
     * @param retMap 要返回的map
     * @param entry
     */
    private static void convert(String key, Map<String, Object> retMap, Map.Entry entry){
        if(entry.getValue() instanceof Map){
            key = key+entry.getKey()+".";
            for(Map.Entry e : ((Map<String, Object>)entry.getValue()).entrySet()){
                convert(key, retMap, e);
            }
        } else {
            key = key+entry.getKey();
            retMap.put(key, entry.getValue());
        }
    }
}
