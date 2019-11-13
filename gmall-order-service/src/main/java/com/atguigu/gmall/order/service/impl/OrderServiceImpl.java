package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.enums.OrderStatus;
import com.atguigu.gmall.bean.enums.ProcessStatus;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    @Transactional
    public String savaOrder(OrderInfo orderInfo) {

        //设置创建时间
        orderInfo.setCreateTime(new Date());
        //设置过期时间
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DATE,1);
        orderInfo.setExpireTime(instance.getTime());
        //订单状态
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        //进程状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        // 生成第三方支付编号
        String outTradeNo="ATGUIGU"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        //设置总金额
        orderInfo.sumTotalAmount();

        orderInfoMapper.insertSelective(orderInfo);

        //保存orderDetail
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if (orderDetailList !=null && orderDetailList.size() > 0){
            for (OrderDetail orderDetail : orderDetailList) {
                orderDetail.setOrderId(orderInfo.getId());
                orderDetailMapper.insertSelective(orderDetail);
            }
        }

        return orderInfo.getId();
    }

    @Override
    public String getTradeNo(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey="user:"+userId+":tradeCode";
        //生成流水号
        String tradeCode = UUID.randomUUID().toString().replace("-", "");
        jedis.set(tradeNoKey,tradeCode);
        jedis.close();

        return tradeCode;
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCodeNo) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey="user:"+userId+":tradeCode";
        String tradeCode = jedis.get(tradeNoKey);
        jedis.close();

        if (tradeCode != null && tradeCode.equals(tradeCodeNo)){
            return true;
        } else{
            return false;
        }
    }

    @Override
    public void delTradeCode(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey="user:"+userId+":tradeCode";
        jedis.del(tradeNoKey);
        jedis.close();
    }
}
