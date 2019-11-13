package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import sun.plugin2.util.NativeLibLoader;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;

    @RequestMapping("list.html")
    public String getList(SkuLsParams skuLsParams, Model model){
        //设置显示条数
        skuLsParams.setPageSize(4);

        SkuLsResult skuLsResult  = listService.search(skuLsParams);
        // 获取sku属性值列表
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();
        model.addAttribute("skuLsInfoList",skuLsInfoList);

        //获取平台属性值集合ids
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> baseAttrInfoList = manageService.getAttrList(attrValueIdList);

        ArrayList<BaseAttrValue> baseAttrValueArrayList = new ArrayList<>();

        // 已选的属性值列表
        String urlParam = makeUrlParam(skuLsParams);

        //比较baseAttrInfoList.attrValueList.id与skuLsParams.getvalueId()是否相等 相等将数据删除
        for (Iterator<BaseAttrInfo> baseAttrInfoIterator = baseAttrInfoList.iterator(); baseAttrInfoIterator.hasNext(); ) {
            BaseAttrInfo baseAttrInfo = baseAttrInfoIterator.next();
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            //获取平台属性值
            for (BaseAttrValue attrValue : attrValueList) {
                if(skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
                    //获取被选中的平台属性值
                    for (String valueId : skuLsParams.getValueId()) {
                        //选中的属性值 和 查询结果的属性值
                        if (attrValue.getId().equals(valueId)){
                            baseAttrInfoIterator.remove();

                            BaseAttrValue baseAttrValueed = new BaseAttrValue();
                            //生成面包屑
                            baseAttrValueed.setValueName(baseAttrInfo.getAttrName()+attrValue.getValueName());

                            //调用makeUrlParam
                            String newUrlParam = makeUrlParam(skuLsParams, valueId);
                            //将最新的UrlParam赋值给BaseAttrValue
                            baseAttrValueed.setUrlParam(newUrlParam);
                            baseAttrValueArrayList.add(baseAttrValueed);
                        }
                    }
                }

            }
        }

        //分页条件
        model.addAttribute("totalPage", skuLsResult.getTotalPages());
        model.addAttribute("pageNo",skuLsParams.getPageNo());


        //保存keyword
        model.addAttribute("keyword", skuLsParams.getKeyword());
        //面包屑的添加
        model.addAttribute("baseAttrValueArrayList", baseAttrValueArrayList);

        model.addAttribute("baseAttrInfoList", baseAttrInfoList);


        model.addAttribute("urlParam",urlParam);


        return "list";
    }

    /**
     * 制作urlParam
     * @param skuLsParams
     * @return
     */
    private String makeUrlParam(SkuLsParams skuLsParams,String... excludeValueIds) {

        String urlParam = "";
        //http://list.gmall.com/list.html?keyword=小米
        if(skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length()>0){
            urlParam += "keyword="+skuLsParams.getKeyword();
        }

        //http://list.gmall.com/list.html?catalog3Id=61
        if (skuLsParams.getCatalog3Id() != null && skuLsParams.getCatalog3Id().length()>0){
            urlParam += "catalog3Id=" + skuLsParams.getCatalog3Id();
        }

        //
        if(skuLsParams.getValueId() !=null && skuLsParams.getValueId().length>0){
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String valueId  = skuLsParams.getValueId()[i];

                if(excludeValueIds!=null && excludeValueIds.length>0){
                    String excludeValueId = excludeValueIds[0];
                    if (excludeValueId.equals(valueId)){
                        // 跳出代码，后面的参数则不会继续追加【后续代码不会执行】
                        // 不能写break；如果写了break；其他条件则无法拼接！
                        continue;
                    }
                }

                if(urlParam.length()>0){
                    urlParam += "&";
            }
                    urlParam += "valueId="+valueId;
            }

        }
        return urlParam;
    }
}
