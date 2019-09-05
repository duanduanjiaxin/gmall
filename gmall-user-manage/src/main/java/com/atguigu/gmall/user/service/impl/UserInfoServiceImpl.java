package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserInfoService;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    UserAddressMapper userAddressMapper;


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
    public List<UserAddress> getUserAddressByUserId(String id) {

        UserAddress userAddress = new UserAddress();
        userAddress.setId("1");

        return  userAddressMapper.select(userAddress);
    }
}
