package com.test.api.service.impl;

import com.test.api.entity.UserInfo;
import com.test.api.mapper.UserInfoMapper;
import com.test.api.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author Faisal Mulya Santosa
 * @since 2024-07-29
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
