package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.*;

import java.util.List;

public interface ManageService {
    /**
     * 获取一级分类
     * @return
     */
    public List<BaseCatalog1> getBaseCatalog1();

    /**
     * 根据一级分类id获取二级分类
    * @param catalog1Id
     * @return
     */
    public List<BaseCatalog2> getBaseCatalog2(String catalog1Id);

    /**
     * 根据二级分类id获取三级分类
     * @param catalog2Id
     * @return
     */
    public List<BaseCatalog3> getBaseCatalog3(String catalog2Id);

    /**
     * 根据三级分类id获取属性
     * @param catalog3Id
     * @return
     */
    public List<BaseAttrInfo> getBaseAttrInfo(String catalog3Id);

    /**
     * 根据属性id获取属性值
     * @param attrId
     * @return
     */
    public List<BaseAttrValue> getBaseAttrValue(String attrId);

    /**
     * 保存属性和属性值
     * @param baseAttrInfo
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 根据三级Id获取商品信息
     * @param catalog3Id
     * @return
     */
    List<SpuInfo> spuList(String catalog3Id);


    /**
     * 获取商品销售属性
     * @return
     */
    List<BaseSaleAttr> getBaseSaleAttrList();

    /**
     * 大保存（保存商品信息，销售属性，销售属性值，商品图片）
     * @param spuInfo
     * @return
     */
    void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 获取图片列表
     * @param spuId
     * @return
     */
    List<SpuImage> getSpuImageList(String spuId);

    /**
     * 根据spuId获取商品销售id
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    /**
     * 大保存sku信息
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /**
     * 根据skuid获取skuinfo
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfo(String skuId);

    /**
     * 根据skuId spuId查询商品属性集合
     * @param skuInfo
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo);

    /**
     * 根据spuId 获取商品销售属性值
     * @param spuId
     * @return
     */
    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);

    /**
     * 获取平台属性id集合
     * @param attrValueIdList
     * @return
     */
    List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);
}
