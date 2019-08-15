package com.jisuye.util;

/**
 * String 处理工具类
 * @author ixx
 * @date 2019-08-11
 */
public class StringUtil {
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
