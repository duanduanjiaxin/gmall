package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    /**
     *
     * @param spuId
     * @return
     */
     List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpu(String spuId);
}
