package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo;

public interface OrderService {

    /**
     * 保存用户订单
     * @param orderInfo
     * @return
     */
    public String savaOrder(OrderInfo orderInfo);

    /**
     * 生成流水号
     * @param userId
     * @return
     */
    public String getTradeNo(String userId);

    /**
     * 检查流水号
     * @param userId
     * @param tradeCodeNo
     * @return
     */
    public boolean  checkTradeCode(String userId,String tradeCodeNo);

    /**
     * 删除流水号
     * @param userId
     */
    public void  delTradeCode(String userId);
}
