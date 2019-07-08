package com.jisuye.service.impl;

import com.jisuye.annotations.Service;
import com.jisuye.core.JdbcTemplate;
import com.jisuye.service.Abc;
import com.jisuye.service.ClassDi;
import com.jisuye.service.Def;

import javax.annotation.Resource;

@Service
public class AbcImpl implements Abc {
    // 名字对不上会报异常
    @Resource
    private Def defImpl;
    // 名字对不上可以使用注解中指定bean名字的方式
    @Resource(name = "def2Impl")
    private Def defByName;

    // 添加jdbcTemplate依赖
    @Resource
    private JdbcTemplate jdbcTemplate;

    // 注入Class类实例
    @Resource
    private ClassDi classDi;

    @Override
    public int test(String name) {
        jdbcTemplate.insert("insert into abc(`name`) values('ixx')");
        System.out.println(defImpl.exe(name));
        System.out.println(defByName.exe(name));
        System.out.println(classDi.exe(name));
        return 0;
    }
}
