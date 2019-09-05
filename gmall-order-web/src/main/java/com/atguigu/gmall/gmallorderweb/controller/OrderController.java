package com.atguigu.gmall.gmallorderweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class OrderController {

    @Reference
    UserInfoService userInfoService;

    @RequestMapping("getUserAddress")
    @ResponseBody
    public List<UserAddress> getUserAddress(String id){
        List<UserAddress> userAddressByUserId = userInfoService.getUserAddressByUserId(id);
        return userAddressByUserId;
    }
}
