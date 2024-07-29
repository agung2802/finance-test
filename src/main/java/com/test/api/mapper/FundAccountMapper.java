package com.test.api.mapper;

import com.test.api.entity.FundAccount;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 资金方电子账户 Mapper 接口
 * </p>
 *
 * @author Faisal Mulya Santosa
 * @since 2024-07-29
 */
@DS("slave_1")
public interface FundAccountMapper extends BaseMapper<FundAccount> {

}
