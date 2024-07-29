package com.test.api.entity;

import java.math.BigDecimal;

import lombok.Data;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
@Data
public class RepayDues {
	/**
	 * 资金方利息
	 */
	private BigDecimal FundInterest;
	/**
	 * 保证金
	 */
	private BigDecimal ProvisionFee;
	/**
	 * 平台服务费
	 */
	private BigDecimal ServiceFee;
	/**
	 * 平台服务税费
	 */
	private BigDecimal ServiceFeeVAT;
	/**
	 * 本金
	 */
	private BigDecimal Principal;
	/**
	 * 进位费
	 */
	private BigDecimal CarryingFee;
	/**
	 * 进位费税费
	 */
	private BigDecimal CarryingFeeVAT;
	/**
	 * 进位费含税费税费
	 */
	private BigDecimal CarryingFeeIncludeVAT;
	/**
	 * 管理费
	 */
	private BigDecimal AdminFee;
	/**
	 * 管理费税费
	 */
	private BigDecimal AdminFeeVAT;
	private String dueDate;

}
