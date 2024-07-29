package com.test.api.mapper;

import com.test.api.entity.Dues;

import org.apache.ibatis.annotations.Param;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 还款计划表 Mapper 接口
 * </p>
 *
 * @author Faisal Mulya Santosa
 * @since 2024-07-29
 */
@DS("master")
public interface DuesMapper extends BaseMapper<Dues> {

	long getTotalFee(@Param("loanId")String loanId,@Param("dueType")String dueType);
}
