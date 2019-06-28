package com.jisuye.service.impl;

import com.jisuye.annotations.Service;
import com.jisuye.service.Def;

@Service
public class DefImpl implements Def {
    @Override
    public String exe(String name) {
        return "Interface DI... "+name;
    }
}
