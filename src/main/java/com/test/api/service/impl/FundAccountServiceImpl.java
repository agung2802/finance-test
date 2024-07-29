package com.test.api.service.impl;

import com.test.api.entity.FundAccount;
import com.test.api.mapper.FundAccountMapper;
import com.test.api.service.IFundAccountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 资金方电子账户 服务实现类
 * </p>
 *
 * @author Faisal Mulya Santosa
 * @since 2024-07-29
 */
@Service
public class FundAccountServiceImpl extends ServiceImpl<FundAccountMapper, FundAccount> implements IFundAccountService {

}
