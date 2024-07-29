package com.test.api.mapper;

import java.util.List;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.api.entity.GatewayTask;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Faisal Mulya Santosa
 * @since 2024-07-29
 */
@DS("slave_2")
public interface GatewayTaskMapper extends BaseMapper<GatewayTask> {

	List<GatewayTask> getGateTaskList();
}
