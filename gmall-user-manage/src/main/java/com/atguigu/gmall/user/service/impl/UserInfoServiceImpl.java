package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.UserInfoService;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;

    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    UserAddressMapper userAddressMapper;
    @Autowired
    RedisUtil redisUtil;



    @Override
    public List<UserInfo> finAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserInfo> findUserByName(UserInfo userInfo) {

        Example example=new Example(UserInfo.class);
        example.createCriteria().andEqualTo("name", userInfo.getName());

        return userInfoMapper.selectByExample(example);
    }

    @Override
    public List<UserInfo> findUser(UserInfo userInfo) {
        return userInfoMapper.select(userInfo);
    }

    @Override
    public List<UserInfo> findUserLike(UserInfo userInfo) {

        Example example = new Example(UserInfo.class);
        example.createCriteria().andLike("name","%"+ userInfo.getName()+"%");
        return userInfoMapper.selectByExample(example);

    }

    @Override
    public void updateUserInfo(UserInfo userInfo) {

        //userInfoMapper.updateByPrimaryKeySelective(userInfo);
        Example example = new Example(UserInfo.class);
        example.createCriteria().andEqualTo("name", userInfo.getName());
        userInfoMapper.updateByExampleSelective(userInfo, example);
    }

    @Override
    public void addUserInfo(UserInfo userInfo) {
        userInfoMapper.insertSelective(userInfo);
    }

    @Override
    public void deleteUserInfo(UserInfo userInfo) {
        //userInfoMapper.deleteByPrimaryKey(userInfo.getId());

        Example example = new Example(UserInfo.class);
        example.createCriteria().andLike("name", "%"+ userInfo.getName()+"%");
        userInfoMapper.deleteByExample(example);
    }

    @Override
    public List<UserAddress> getUserAddressByUserId(String userId) {

        UserAddress userAddress = new UserAddress();
        userAddress.setId(userId);

        return  userAddressMapper.select(userAddress);
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        /*1. 用户输入的信息与后台数据库进行匹配
        select * from userInfo where userName = ? and password = ?
        2. 如果匹配结果
        2.1 true 将信息放入redis ，返回userInfo 对象
        2.2 false 返回null
        3.	控制器中登录成功之后，返回token！*/

        //加密密码,与数据库中的密码匹配
        String password   = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        userInfo.setPasswd(password);
        UserInfo info = userInfoMapper.selectOne(userInfo);

        if (info != null){
            //不为空，放入redis中
            String userKey =userKey_prefix+info.getId()+userinfoKey_suffix;
            Jedis jedis = redisUtil.getJedis();
            jedis.setex(userKey,userKey_timeOut , JSON.toJSONString(info));
            jedis.close();
            return info;
        }

        return null;
    }

    @Override
    public UserInfo verify(String userId) {
        //获取jedis
        //用解密出来的userId 获取redis 中的数据 {user:userId:info}
        // UserInfo userInfo = JSON.toJsonString(jedis.get(user:userId:info));
        Jedis jedis = redisUtil.getJedis();


        String userKey =userKey_prefix+userId+userinfoKey_suffix;
        String userJson  = jedis.get(userKey);
        if(userJson !=null){
            //延长失效
            jedis.expire(userKey,userKey_timeOut );
            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
            return userInfo;
        }


        return null;
    }
}
