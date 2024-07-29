package com.test.api.service.impl;

import com.test.api.entity.TbUser;
import com.test.api.mapper.TbUserMapper;
import com.test.api.service.ITbUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 合作渠道用户信息 服务实现类
 * </p>
 *
 * @author Faisal Mulya Santosa
 * @since 2024-07-29
 */
@Service
public class TbUserServiceImpl extends ServiceImpl<TbUserMapper, TbUser> implements ITbUserService {

}
