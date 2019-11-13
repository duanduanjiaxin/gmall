package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {

    /**
     * 添加到购物车
     * @param skuId
     * @param userId
     * @param skuNum
     */
    public void addToCart(String skuId,String userId,Integer skuNum);

    /**
     * 获取购物车列表
     * @param userId
     * @return
     */
    List<CartInfo> getCartList(String userId);

    /**
     * 合并购物车
     * @param cartListFromCookie
     * @param userId
     * @return
     */
    List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId);

    /**
     * 更改购物车选择状态
     * @param skuId
     * @param isChecked
     * @param userId
     */
    void checkCart(String skuId, String isChecked, String userId);

    /**
     * 获取被选中的数据
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);
}
