package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {

    @Reference
    ManageService manageService;

    @Reference
    ListService listService;

    //@LoginRequire
    @RequestMapping("{skuId}.html")
    public String skuInfoPage(@PathVariable("skuId") String skuId, Model model){
        //根据skuid查询skuInfo
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        model.addAttribute("skuInfo",skuInfo);

        //根据skuinfo查询销售属性和销售属性值
        List<SpuSaleAttr> spuSaleAttrs= manageService.getSpuSaleAttrListCheckBySku(skuInfo);
        model.addAttribute("spuSaleAttrs",spuSaleAttrs);

        //根据spuId 获取商品销售属性值
        //{"113|115":"34","113|116":"33","114|117":"35"}
        String key ="";
        HashMap<String , String > map = new HashMap<>();
        List<SkuSaleAttrValue> skuSaleAttrValues = manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());

        for (int i = 0; i < skuSaleAttrValues.size(); i++) {
            // 拼接的规则：当前循环的skuId 与下一条skuId 相同则拼接，否则不拼接！ 当循环到数据末尾的时候，则停止拼接
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValues.get(i);

            if(key.length()>0){
                key +="|";
            }

            key +=skuSaleAttrValue.getSaleAttrValueId();

            if((i+1)==skuSaleAttrValues.size() || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValues.get(i+1).getSkuId())){

                map.put(key, skuSaleAttrValue.getSkuId());

                key="";
            }
        }
        //将map 变为json 字符串
        String valuesSkuJson = JSON.toJSONString(map);
        model.addAttribute("valuesSkuJson", valuesSkuJson);

        listService.incrHotScore(skuId);
        return "item";
    }

}
