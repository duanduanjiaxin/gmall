package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class SpuManageController {

    @Reference
    private ManageService manageService;


    //http://localhost:8082/spuList?catalog3Id=62
    @RequestMapping("spuList")
    public List<SpuInfo> spuList(String catalog3Id){
        return manageService.spuList(catalog3Id);
    }

    //http://localhost:8082/baseSaleAttrList
    @RequestMapping("baseSaleAttrList")
    public List<BaseSaleAttr> baseSaleAttrList(){
        return manageService.getBaseSaleAttrList();
    }

    //http://localhost:8082/saveSpuInfo
    @RequestMapping("saveSpuInfo")
    public void savaSpuInfo(@RequestBody SpuInfo spuInfo ){
         manageService.saveSpuInfo(spuInfo);
    }
}
