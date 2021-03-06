package com.jisuye.util;

import com.jisuye.core.ApplicationContext;
import com.jisuye.exception.SquareException;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库操作工具
 * @author ixx
 * @date 2019-07-01
 */
public class DbUtil {
    private static final Logger log = LoggerFactory.getLogger(DbUtil.class);
    private static Connection connection;
    private static DataSource dataSource;

    /** 初始化方法*/
    public static void init(){
        try {
            if(connection != null && dataSource != null){
                return;
            }
            String url = ApplicationContext.getConf("square.datasource.url").toString();
            String username = ApplicationContext.getConf("square.datasource.username").toString();
            String password = ApplicationContext.getConf("square.datasource.password").toString();

            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl(url);
            ds.setUsername(username);
            ds.setPassword(password);
            // HikariCP提供的优化设置
            ds.addDataSourceProperty("cachePrepStmts", "true");
            ds.addDataSourceProperty("prepStmtCacheSize", "250");
            ds.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            connection = ds.getConnection();
            dataSource = ds;
        } catch (Exception e) {
            log.error("mysql connection init error..", e);
            throw new SquareException("mysql connection init error....");
        }
    }

    /** install/update 带参数占位符方法*/
    public static boolean update(String sql, Object... params){
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(sql);
            if(params != null) {
                for (int i = 1; i <= params.length; i++) {
                    statement.setObject(i, params[i - 1]);
                }
            }
            return statement.execute();
        } catch (Exception e) {
            log.error("install/update exception.", e);
        }
        return false;
    }
    /** install/update 无参数占位符方法*/
    public static boolean update(String sql){
        return update(sql, null);
    }

    /**
     * 通用查询方法
     * @param sql sql语句
     * @param clazz 返回列表类型
     * @param params 参数列表
     * @param <T> 返回列表类型
     * @return
     */
    public static <T> List<T> select(String sql,Class<T> clazz, Object... params){
        List<T> list = new ArrayList<>();
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(sql);
            for(int i=1; i<= params.length; i++){
                statement.setObject(i, params[i-1]);
            }
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                T t = clazz.newInstance();
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if(method.getName().startsWith("set")){
                        String field = StringUtil.firstToLowerCase(method.getName().substring(3));
                        String paramClass = method.getParameterTypes()[0].getSimpleName();
                        switch (paramClass){
                            case "Integer":
                                method.invoke(t, rs.getInt(field)); break;
                            case "Float":
                                method.invoke(t, rs.getFloat(field)); break;
                            case "Double":
                                method.invoke(t, rs.getDouble(field)); break;
                            case "Short":
                                method.invoke(t, rs.getShort(field)); break;
                            case "Byte":
                                method.invoke(t, rs.getByte(field)); break;
                            case "Long":
                                method.invoke(t, rs.getLong(field)); break;
                            case "Boolean":
                                method.invoke(t, rs.getBoolean(field)); break;
                            default:
                                method.invoke(t, rs.getObject(field));
                        }
                    }
                }
                list.add(t);
            }
        } catch (Exception e) {
            log.error("select exception.", e);
        }
        return list.size()>0 ? list : null;
    }
    public static DataSource getDataSource(){
        if (dataSource == null) {
            init();
        }
        return dataSource;
    }
}
