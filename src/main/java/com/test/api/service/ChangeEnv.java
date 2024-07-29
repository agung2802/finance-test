package com.test.api.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.test.api.entity.TbUser;
import com.test.api.entity.UserInfo;

public interface ChangeEnv {
    int updateUserfat(UserInfo userInfo, QueryWrapper<UserInfo> qw);
    int updateUserdev(UserInfo userInfo,QueryWrapper<UserInfo> qw);

    TbUser  getUserfat(QueryWrapper<TbUser> queryTbUser);
    TbUser  getUserdev(QueryWrapper<TbUser> queryTbUser);

}
