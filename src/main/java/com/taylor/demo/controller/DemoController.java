package com.taylor.demo.controller;

import com.taylor.annotation.MyAutowired;
import com.taylor.annotation.MyController;
import com.taylor.annotation.MyRequestMapping;
import com.taylor.annotation.MyRequestParam;
import com.taylor.demo.service.IDemoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author taylor
 * @Title: DemoController
 * @Package: com.taylor.demo.controller
 * @Description: TODO
 * @version V1.0
 * @date 2019/7/18 0018 22:09
 **/

@MyController
@MyRequestMapping("/demo")
public class DemoController {

    @MyAutowired
    private IDemoService demoService;

    @MyRequestMapping("/getInfo")
    public void getInfo(HttpServletRequest request, HttpServletResponse response,
                    @MyRequestParam("name") String name){

        String result = demoService.getName(name);

        try {
            response.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @MyRequestMapping("/addInfo")
    public void addInfo( @MyRequestParam("name") String name){
        System.out.println("添加信息="+name);
    }
}
