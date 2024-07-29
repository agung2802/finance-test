package com.test.api.entity;
/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29
* 类说明
*/

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TransferAmount {
	/**
	 * 转管理费（管理费不含税+进位费不含税）  V1:管理费不含税+进位费不含税+保证金  V2:管理费不含税+进位费不含税
	 */
	private  BigDecimal transferAdminFee;
	/**
	 * 转保证金额
	 */
	private  BigDecimal transferProvisionFee;
	/**
	 * 转资金方利息（不含税）
	 */
	private  BigDecimal transferFundInterestFee;
	/**
	 * 转平台服务费（不含税）
	 */
	private  BigDecimal transferServiceFee;
	/**
	 * 转税费（管理费税费+资金方税费+进位费税费）
	 */
	private  BigDecimal transferVatWht;
	
	/**
	 * 用户收到的钱
	 */
	private  BigDecimal transferToUserAmount;
	/**
	 * 充值金额
	 */
	private  BigDecimal transferRechargeAmount;
	/**
	 * rebook转回老订单的本金
	 */
	private BigDecimal repayTransferToFunder;
	/**
	 * 砍头费中的保费
	 */
	private BigDecimal  premiumFee;
}
