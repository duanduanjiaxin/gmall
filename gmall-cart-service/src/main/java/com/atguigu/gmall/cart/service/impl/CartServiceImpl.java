package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.cart.constant.CartConst;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartInfoMapper cartInfoMapper;

    @Autowired
    RedisUtil redisUtil;

    @Reference
    ManageService manageService;

    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {

        Jedis jedis = redisUtil.getJedis();
        //1.查看购物车是否有商品
        //2.true 有商品 数量加1
        //  false 没有商品新增
        //放入redis
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        CartInfo cartInfoExist  = cartInfoMapper.selectOne(cartInfo);

        String key = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        if (cartInfoExist!=null){
            //数据库有商品，数量+1
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);
            //给实时价格赋值
            cartInfoExist.setSkuPrice(cartInfoExist.getCartPrice());
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);


        }else {
            // 如果不存在，保存购物车
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo1 = new CartInfo();
            cartInfo1.setSkuId(skuId);
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setSkuName(skuInfo.getSkuName());
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setUserId(userId);
            cartInfo1.setSkuNum(skuNum);
            // 插入数据库
            cartInfoMapper.insertSelective(cartInfo1);
            cartInfoExist = cartInfo1;
        }

        //放入缓存
        jedis.hset(key,cartInfoExist.getSkuId(), JSON.toJSONString(cartInfoExist));


        //给购物车设置一个过期时间
        String userInfoKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USERINFOKEY_SUFFIX;
        Long ttl = jedis.ttl(userInfoKey);
        jedis.expire(key,ttl.intValue());

        jedis.close();
    }

    @Override
    public List<CartInfo> getCartList(String userId) {
        // 从redis中取得，
        Jedis jedis = redisUtil.getJedis();
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        List<String> cartJsons = jedis.hvals(userCartKey);

        if (cartJsons!=null&&cartJsons.size()>0){
            List<CartInfo> cartInfoList = new ArrayList<>();
            for (String cartJson : cartJsons) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartInfoList.add(cartInfo);
            }
            // 排序
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return Long.compare(Long.parseLong(o2.getId()),Long.parseLong(o1.getId()));
                }
            });
            return cartInfoList;
        }else{
            // 从数据库中查询，其中cart_price 可能是旧值，所以需要关联sku_info 表信息。
            List<CartInfo> cartInfoList = loadCartCache(userId);
            jedis.close();
            return  cartInfoList;
        }
    }

    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId) {

        List<CartInfo> cartInfoListDB = cartInfoMapper.selectCartListWithCurPrice(userId);
        // 循环开始匹配
        for (CartInfo cartInfoCk : cartListFromCookie) {
            boolean isMatch =false;
            for (CartInfo cartInfoDB : cartInfoListDB) {
                if (cartInfoDB.getSkuId().equals(cartInfoCk.getSkuId())){
                    cartInfoDB.setSkuNum(cartInfoCk.getSkuNum()+cartInfoDB.getSkuNum());
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                    isMatch = true;
                }
            }
            // 数据库中没有购物车，则直接将cookie中购物车添加到数据库
            if (!isMatch){
                cartInfoCk.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCk);
            }
        }
        // 从新在数据库中查询并返回数据
        List<CartInfo> cartInfoList = loadCartCache(userId);

        //勾选合并
        //遍历DB中的数据和Ck的数据 skuid =skuid
        //选中的状态以cookie中的数据为准
        for (CartInfo cartInfoDB : cartInfoListDB) {
            for (CartInfo cartInfoCK : cartListFromCookie) {
                if (cartInfoDB.getSkuId().equals(cartInfoCK.getSkuId())){
                    //勾选才会被更改
                    if ("1".equals(cartInfoCK.getIsChecked())){
                        cartInfoDB.setIsChecked(cartInfoCK.getIsChecked());
                        //更新redis中的isChecked
                        checkCart(cartInfoCK.getSkuId(),"1", userId);
                    }
                }
            }
        }


        return cartInfoList;
    }

    @Override
    public void checkCart(String skuId, String isChecked, String userId) {

        /**
         * 1.获取redis中的商品数据
         * 2.给商品赋值状态
         * 3.放入redis
         * --------------
         * 4.将被选中的商品存入redis
         */
        Jedis jedis = redisUtil.getJedis();
        String userCartKey =CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        String cartJson  = jedis.hget(userCartKey, skuId);
        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
        cartInfo.setIsChecked(isChecked);
        //修改后放入缓存
        jedis.hset(userCartKey, skuId, JSON.toJSONString(cartInfo));

        //将被选中的商品放入redis
        String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        if ("1".equals(isChecked)){
            //被选中的
            jedis.hset(userCheckedKey, skuId, JSON.toJSONString(cartInfo));
        }else {
            //未被选中的
            jedis.hdel(userCheckedKey, skuId);
        }
        jedis.close();
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        // 获得redis中的key
        String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        List<String> cartCheckedList  = jedis.hvals(userCheckedKey);

        List<CartInfo> newCartList = new ArrayList<>();

        for (String cartJson : cartCheckedList) {
            CartInfo cartInfo = JSON.parseObject(cartJson,CartInfo.class);
            newCartList.add(cartInfo);
        }
        return newCartList;

    }

    /**
     * 购物车查询，在数据库中查找
     * @param userId
     * @return
     */
    public  List<CartInfo> loadCartCache(String userId){
        // 使用实时价格：将skuInfo.price 价格赋值 cartInfo.skuPrice
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);

        if (cartInfoList==null || cartInfoList.size()==0){
            return  null;
        }
        // 获取jedis
        Jedis jedis = redisUtil.getJedis();
        // 定义key：user:userId:cart
        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;

        // 循环遍历数据添加到缓存
//        for (CartInfo cartInfo : cartInfoList) {
//            jedis.hset(cartKey,cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
//        }
        HashMap<String, String> map = new HashMap<>();
        for (CartInfo cartInfo : cartInfoList) {
            map.put(cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
        }
        jedis.hmset(cartKey,map);
        jedis.close();
        return cartInfoList;
    }
}
