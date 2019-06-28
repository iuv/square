package com.jisuye.service.impl;

import com.jisuye.annotations.Service;
import com.jisuye.service.Def;

@Service
public class Def2Impl implements Def {
    @Override
    public String exe(String name) {
        return "def2 "+name;
    }
}
