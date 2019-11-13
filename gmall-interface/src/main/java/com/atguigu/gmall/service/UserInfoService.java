package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;

import java.util.List;

public interface UserInfoService {
    /**
     * 查找所有
     * @return
     */
    List<UserInfo> finAll();

    /**
     * 条件查询
     * @param userInfo
     * @return
     */
    List<UserInfo> findUserByName(UserInfo userInfo);

    /**
     * 万能
     * @param userInfo
     * @return
     */
    List<UserInfo> findUser(UserInfo userInfo);

    /**
     * 模糊查询
     * @param userInfo
     * @return
     */
    List<UserInfo> findUserLike(UserInfo userInfo);

    /**
     * 更新
     * @param userInfo
     */
    void updateUserInfo(UserInfo userInfo);

    /**
     * 增加
     * @param userInfo
     */
    void addUserInfo(UserInfo userInfo);


    /**
     * 删除
     * @param userInfo
     */
    void deleteUserInfo(UserInfo userInfo);

    /**
     * 根据用户id查询用户地址
     * @param id
     * @return
     */
    List<UserAddress> getUserAddressByUserId(String userId);

    /**
     * 登录
     * @param userInfo
     * @return
     */
    UserInfo login(UserInfo userInfo);

    /**
     * 根据UserID查询用户
     * @param userId
     * @return
     */
    UserInfo verify(String userId);
}
