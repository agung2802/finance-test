package com.test.api.entity;
/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29
* 类说明
*/

import java.math.BigDecimal;

import lombok.Data;
@Data
public class UpfrontExtendFee {
	/**
	 * 管理费
	 */
	private BigDecimal upfrontAdminFee;
	/**
	 * 管理费税费
	 */
	private BigDecimal upfrontAdminFeeVAT;
	/**
	 * 进位费含税
	 */
	private BigDecimal upfrontCarryFeeIncVat ;
	/**
	 * 进位费不含税
	 */
	private BigDecimal upfrontCarryFee ;
	/**
	 * 进位费税费
	 */
	private BigDecimal upfrontCarryFeeVAT;
	/**
	 * 含税资金方利息
	 */
	private BigDecimal upFundInterestFeeIncludeWHT;
	/**
	 * 不含税的资金方利息
	 */
	private BigDecimal upfrontFundInterestFee;
	/**
	 * 资金方利息税费
	 */
	private BigDecimal upFundInterestFeeWHT;
	/**
	 * 砍头费中保证金
	 */
	private BigDecimal upfrontProvisionFee;
	/**
	 * 砍头费中平台服务费
	 */
	private BigDecimal upfrontServiceFee;
	/**
	 * 砍头费中平台服务费税费
	 */
	private BigDecimal upfrontServiceFeeVAT;

	
}
