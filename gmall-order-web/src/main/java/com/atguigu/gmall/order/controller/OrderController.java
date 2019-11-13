package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.CartInfoService;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    UserInfoService userInfoService;

    @Reference
    OrderService orderService;

    @Reference
    CartService cartService;



    @RequestMapping("trade")
    //@ResponseBody
    @LoginRequire
    public String getUserAddress(HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        //收货人地址
        List<UserAddress> userAddressList = userInfoService.getUserAddressByUserId(userId);
        // 得到选中的购物车列表
        List<CartInfo> cartInfoList = cartService.getCartCheckedList(userId);
        // 订单信息集合
        List<OrderDetail> orderDetailList=new ArrayList<>();
        for (CartInfo cartInfo : cartInfoList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            orderDetailList.add(orderDetail);
        }
        //订单详情
        request.setAttribute("orderDetailArrayList",orderDetailList);

        OrderInfo orderInfo=new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();
        //商品总金额
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        //用户地址
        request.setAttribute("userAddressList",userAddressList);

        //生成流水号
        String tradeNo = orderService.getTradeNo(userId);
        request.setAttribute("tradeNo",tradeNo);
        return "trade";
    }

    //http://trade.gmall.com/submitOrder
    @RequestMapping("submitOrder")
    @LoginRequire
    public String saveOrder(HttpServletRequest request,OrderInfo orderInfo){
        String userId = (String) request.getAttribute("userId");
        String tradeNo = request.getParameter("tradeNo");

        boolean result = orderService.checkTradeCode(userId, tradeNo);
        //比较不成功
        if (!result){
            request.setAttribute("errMsg", "请勿重复提交订单");
            return "tradeFail";
        }
        //删除流水号
        orderService.delTradeCode(userId);

        orderInfo.setUserId(userId);
        String orderId = orderService.savaOrder(orderInfo);

        return "redirect://payment.gmall.com/index?orderId="+orderId;

    }

}
