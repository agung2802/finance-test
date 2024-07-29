package com.test.api.mapper;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.api.entity.FinanceTrading;

/**
 * <p>
 * 交易流水表 Mapper 接口
 * </p>
 *
 * @author Faisal Mulya Santosa
 * @since 2024-07-29
 */
@DS("master")
public interface FinanceTradingMapper extends BaseMapper<FinanceTrading> {
	
	//财务转账记录
	List<FinanceTrading> getFinanceTradingList();
	//账号
	int updateCreditLimit(String wdDeviceId,String loanNumber);
	//插入审批填写金额
	int insertMonthlySalary(BigInteger userId, int monthlySalary, Timestamp createdAt, Timestamp updatedAt);
	//插入卡号
	int insertBankAccount(String accountNumber);
	//查看产品配置的砍头费以及保证金比例，逾期费率，提前结清费率
	HashMap<String,Object> findProductRateConfig(String secondProdCode);
	//资金方费率
	HashMap<String,Object> findFundRateConfig(long fundId,long productRateId);
	//查询保费
	HashMap<String,Object> findinsuranceConfig(String secondProdCode);

}
