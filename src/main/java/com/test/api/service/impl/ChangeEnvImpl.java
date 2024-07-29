package com.test.api.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.test.api.entity.TbUser;
import com.test.api.entity.UserInfo;
import com.test.api.mapper.TbUserMapper;
import com.test.api.mapper.UserInfoMapper;
import com.test.api.service.ChangeEnv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: Faisal Mulya Santosa
 * @create: 2023-09-15 14:05
 * @Description:
 */
@Slf4j
@Service
public class ChangeEnvImpl implements ChangeEnv {
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    TbUserMapper tbUserMapper;

    @Override
    @DS("slave4")
    public int updateUserfat(UserInfo userInfo, QueryWrapper<UserInfo> qw){
        return  userInfoMapper.update(userInfo, qw);
    }
    @Override
    @DS("devslave4")
    public int updateUserdev(UserInfo userInfo,QueryWrapper<UserInfo> qw){
        return  userInfoMapper.update(userInfo, qw);

    }
    @Override
    @DS("slave5")
    public TbUser  getUserfat(QueryWrapper<TbUser> queryTbUser){
        return tbUserMapper.selectOne(queryTbUser);
    }
    @Override
    @DS("devslave5")
    public TbUser  getUserdev(QueryWrapper<TbUser> queryTbUser){
        return tbUserMapper.selectOne(queryTbUser);
    }
}
