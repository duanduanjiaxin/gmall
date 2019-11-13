package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.config.CookieUtil;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartCookieHandler {

    // 定义购物车名称
    private String cookieCartName = "CART";
    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE=7*24*3600;
    @Reference
    private ManageService manageService;

    /**
     *  未登录的时候，添加到购物车
     * @param request
     * @param response
     * @param skuId
     * @param userId
     * @param skuNum
     */
    public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, String userId, int skuNum) {

        //判断cookie中是否有购物车 有可能有中文，所有要进行序列化

        String cartJson = CookieUtil.getCookieValue(request, cookieCartName, true);
        List<CartInfo> cartInfoList = new ArrayList<>();
        boolean ifExist=false;
        if (cartJson!=null){
            cartInfoList = JSON.parseArray(cartJson, CartInfo.class);
            for (CartInfo cartInfo : cartInfoList) {
                if (cartInfo.getSkuId().equals(skuId)){
                    cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
                    // 价格设置
                    cartInfo.setSkuPrice(cartInfo.getCartPrice());
                    ifExist=true;

                }
            }
        }
        // //购物车里没有对应的商品 或者 没有购物车
        if (!ifExist){
            //把商品信息取出来，新增到购物车
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo=new CartInfo();

            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());

            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(skuNum);
            cartInfoList.add(cartInfo);
        }
        // 把购物车写入cookie
        String newCartJson = JSON.toJSONString(cartInfoList);
        CookieUtil.setCookie(request,response,cookieCartName,newCartJson,COOKIE_CART_MAXAGE,true);
    }

    public List<CartInfo> getCartList(HttpServletRequest request) {

        String cartJson = CookieUtil.getCookieValue(request, cookieCartName, true);
        List<CartInfo> cartInfoList = JSON.parseArray(cartJson, CartInfo.class);
        return cartInfoList;
    }

    /**
     * 删除cookie数据
     * @param request
     * @param response
     */
    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request, response, cookieCartName);

    }

    /**
     * 未登录cookie中的商品状态
     * @param skuId
     * @param isChecked
     * @param request
     * @param response
     */
    public void checkCart(String skuId, String isChecked, HttpServletRequest request, HttpServletResponse response) {
        /**
         * 1.获取所有商品
         * 2.给商品赋值
         * 3.存入cookie
         */
        List<CartInfo> cartList = getCartList(request);
        if (cartList != null && cartList.size()>0){
            for (CartInfo cartInfo : cartList) {
                if (cartInfo.getSkuId().equals(skuId)){
                    cartInfo.setIsChecked(isChecked);
                }
            }
            //放入cookie
            CookieUtil.setCookie(request, response, cookieCartName, JSON.toJSONString(cartList), COOKIE_CART_MAXAGE, true);

        }
    }
}
