package com.jisuye.service.impl;

import com.jisuye.annotations.Service;
import com.jisuye.core.JdbcTemplate;
import com.jisuye.service.AbcEntity;
import com.jisuye.service.Def;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DefImpl implements Def {
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public String exe(String name) {
        List<AbcEntity> list = jdbcTemplate.select("select * from abc where name=?", AbcEntity.class, name);
        System.out.println(list.size());
        System.out.println(list.get(0).getId());
        System.out.println(list.get(0).getName());
        return "Interface DI... "+name;
    }
}
