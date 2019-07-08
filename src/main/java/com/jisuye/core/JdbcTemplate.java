package com.jisuye.core;

import com.jisuye.util.DbUtil;

import java.util.List;

/**
 * 添加数据库支持(默认不加载该Bean，有其它Bean引用时再加载)
 * @author ixx
 * @date 2019-07-05
 */
public class JdbcTemplate {

    public int insert(String sql){
        return DbUtil.update(sql) ? 1 : 0;
    }
    public int insert(String sql, Object... params){
        return DbUtil.update(sql, params) ? 1 : 0;
    }

    public int update(String sql, Object... params){
        return DbUtil.update(sql, params) ? 1 : 0;
    }
    public int update(String sql){
        return DbUtil.update(sql) ? 1 : 0;
    }

    public int delete(String sql){
        return DbUtil.update(sql) ? 1 : 0;
    }
    public int delete(String sql, Object... params){
        return DbUtil.update(sql, params) ? 1 : 0;
    }

    public <T> List<T> select(String sql,Class<T> clazz, Object... param){
        return DbUtil.select(sql, clazz, param);
    }
}
