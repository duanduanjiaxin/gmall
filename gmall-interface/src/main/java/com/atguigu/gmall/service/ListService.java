package com.atguigu.gmall.service;


import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;

public interface ListService {
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo);

    /**
     * 全文索引
     * @param skuLsParams
     * @return
     */
    public SkuLsResult search(SkuLsParams skuLsParams);

    /**
     * 商品访问次数
     * @param skuId
     */
    public void incrHotScore(String skuId);


}
