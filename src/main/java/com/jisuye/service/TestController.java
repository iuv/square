package com.jisuye.service;

import com.jisuye.annotations.web.*;
import com.jisuye.core.JdbcTemplate;

import javax.annotation.Resource;
import java.util.List;

@Controller("/test")
public class TestController {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/hello")
    public List<AbcEntity> test(@RequestParam("name") String name, @RequestParam("a") String age){
        List<AbcEntity> list = jdbcTemplate.select("select * from abc where name=?", AbcEntity.class, name);
        return list;
    }

    @DeleteMapping
    public String testDel(@RequestParam("id") int id){
        int i = jdbcTemplate.delete("delete from abc where id=?", id);
        return "delete id : "+id+" is success";
    }

    @PostMapping("/post")
    public ResponseVo testPost(@RequestParam("id") int id, @RequestBody TestVo vo){
        ResponseVo responseVo = new ResponseVo();
        responseVo.setResId(id*10);
        responseVo.setResAge(vo.getAge()*2);
        responseVo.setResName(vo.getName());
        return responseVo;
    }
}
