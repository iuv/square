package com.jisuye.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 参数转kv工具类
 * @author ixx
 * @date 2019-06-13
 */
public class ArgsToKVUtil {
    public static Map<String, String> convert(String[] args){
        Map<String, String> map = new HashMap<String, String>(args.length);
        for (String arg : args) {
            String[] tmp = arg.split("=");
            if(tmp.length == 2){
                map.put(tmp[0], tmp[1]);
            }
        }
        return map;
    }
}
