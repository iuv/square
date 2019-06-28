package com.jisuye.service;

import com.jisuye.annotations.Service;

@Service
public class ClassDi {

    public String exe(String name){
        return "Class DI "+name;
    }
}
