package com.test.api.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.api.entity.Loans;

/**
 * <p>
 * 借款信息表 Mapper 接口
 * </p>
 *
 * @author Faisal Mulya Santosa
 * @since 2024-07-29
 */
@DS("slave_3")
public interface LoansMapper extends BaseMapper<Loans> {

    int insertWhitelist(String mobile,String nik);
}
