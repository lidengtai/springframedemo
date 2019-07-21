package com.taylor.demo.service.impl;

import com.taylor.annotation.MyService;
import com.taylor.demo.service.IDemoService;

/**
 * @author taylor
 * @version V1.0
 * @Title: DemoServiceImpl
 * @Description: TODO
 * @date 2019/7/18 0018 22:23
 **/
@MyService
public class DemoServiceImpl implements IDemoService {

    @Override
    public String getName(String name) {
        return "I am "+ name;
    }

}
