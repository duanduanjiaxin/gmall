package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
@CrossOrigin
@Controller
public class ManageController {

    @Reference
    ListService listService;

    @Reference
    ManageService manageService;

    @RequestMapping("index")
    public String index(){

        return "index";
    }

    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getCatalog1(){
        return manageService.getBaseCatalog1();
    }

    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<BaseCatalog2> getCatalog2(String catalog1Id){
        return manageService.getBaseCatalog2(catalog1Id);
    }

    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<BaseCatalog3> getCatalog3(String catalog2Id){
        return manageService.getBaseCatalog3(catalog2Id);
    }

    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<BaseAttrInfo> getBaseAttrInfo(String catalog3Id){
        return manageService.getBaseAttrInfo(catalog3Id);
    }

    @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<BaseAttrValue> getBaseAttrValue(String attrId){
        return manageService.getBaseAttrValue(attrId);
    }

    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo ){
        manageService.saveAttrInfo(baseAttrInfo);
    }

    @RequestMapping("onSale")
    @ResponseBody
    public String onSale (String skuId){

        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        SkuLsInfo skuLsInfo = new SkuLsInfo();
        BeanUtils.copyProperties(skuInfo, skuLsInfo);

        listService.saveSkuLsInfo(skuLsInfo);

        return "ok";
    }
}
