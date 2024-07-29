package com.test.api.mapper;

import com.test.api.entity.LoanDetail;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 贷款详情 Mapper 接口
 * </p>
 *
 * @author Faisal Mulya Santosa
 * @since 2024-07-29
 */
@DS("master")
public interface LoanDetailMapper extends BaseMapper<LoanDetail> {

}
