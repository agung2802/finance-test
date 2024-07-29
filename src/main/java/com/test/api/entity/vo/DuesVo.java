package com.test.api.entity.vo;

import java.math.BigDecimal;
import java.util.List;

import com.test.api.entity.RepayDues;

import lombok.Data;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29
* 类说明
*/
@Data
public class DuesVo {
	/**
	 * 总的服务费：资金方利息+平台服务费+平台服务税费
	 */
	private BigDecimal totaServiceFee;
	/**
	 * 打折前的总服务费
	 */
	private BigDecimal befDisTotaServiceFee;
	/**
	 * 每月还款金额非DDM每月还款金额
	 */
	private BigDecimal repaymentAmount;
	/**
	 * 每月还款金额DDM每月还款金额
	 */
	private BigDecimal repaymentAmountDdm;
	/**
	 * 打折前的还款金额
	 */
	private BigDecimal befDisRepaymentAmount;
	/**
	 * 打折减免的金额
	 */
	private BigDecimal disRepaymentAmount;
	/**
	 * 正常的还款计划(非DDM)
	 */
	private RepayDues normalDues;
	/**
	 * ddm还款计划（长期的第一期或者最后一期）
	 */
	private RepayDues dDMDues;
	private List<String> duedateList;


}
