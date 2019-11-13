package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;

import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class CartController {

    @Reference
    CartService cartService;
    @Reference
    ManageService manageService;

    @Autowired
    CartCookieHandler cartCookieHandler;

    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request , HttpServletResponse response){
        String userId = (String) request.getAttribute("userId");
        String skuId = request.getParameter("skuId");
        String skuNum = request.getParameter("skuNum");
        if(StringUtils.isNotEmpty(userId)){
            //登录
            cartService.addToCart(skuId,userId,Integer.parseInt(skuNum));
        }else {
            //未登录
            cartCookieHandler.addToCart(request,response,skuId,userId,Integer.parseInt(skuNum));
        }


        // 取得sku信息对象
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);

        return "success";
    }

    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public  String cartList(HttpServletRequest request,HttpServletResponse response){
        // 判断用户是否登录，登录了从redis中，redis中没有，从数据库中取
        // 没有登录，从cookie中取得
        String userId = (String) request.getAttribute("userId");
        if (userId!=null){
            // 从cookie中查找购物车
            List<CartInfo> cartListFromCookie = cartCookieHandler.getCartList(request);
            List<CartInfo> cartList = null;
            if (cartListFromCookie!=null && cartListFromCookie.size()>0){
                // 开始合并
                cartList=cartService.mergeToCartList(cartListFromCookie,userId);
                // 删除cookie中的购物车
                cartCookieHandler.deleteCartCookie(request,response);
            }else{
                // 从redis中取得，或者从数据库中
                cartList= cartService.getCartList(userId);
            }
            request.setAttribute("cartList",cartList);
        }else{
            List<CartInfo> cartList = cartCookieHandler.getCartList(request);
            request.setAttribute("cartList",cartList);
        }
        return "cartList";
    }

    @RequestMapping("checkCart")
    @ResponseBody
    @LoginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request,HttpServletResponse response){
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");

        String userId = (String) request.getAttribute("userId");

        if (userId != null){
            //已登录
            cartService.checkCart(skuId,isChecked,userId);
        }else {
            //未登录
            cartCookieHandler.checkCart(skuId,isChecked,request,response);
        }
    }


    @RequestMapping("toTrade")
    @LoginRequire
    public String toTrade(HttpServletRequest request ,HttpServletResponse response){
        String skuId = (String) request.getAttribute("skuId");
        /**
         * 合并被勾选的商品
         * 1.获取未登录的数据
         * 2.合并
         * 3.删除
         */
        //获取
        List<CartInfo> cartList = cartCookieHandler.getCartList(request);
        if (cartList!=null && cartList.size()>0){
           //合并
            cartService.mergeToCartList(cartList, skuId);
            //删除
            cartCookieHandler.deleteCartCookie(request ,response);
        }


        return "redirect://trade.gmall.com/trade";
    }
}
