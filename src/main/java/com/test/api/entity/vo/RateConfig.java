package com.test.api.entity.vo;

import java.math.BigDecimal;

import lombok.Data;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
@Data
public class RateConfig {
	/**
	 * 二级产品
	 */
	private String secondProdCode;
	/**
	 * 产品id
	 */
	private long productRateId;
	/**
	 * 资金方id
	 */
	private long fundId;
	/**
	 * 砍头费方式
	 */
	private int adminType;
	/**
	 * 管理费和保证金的占比
	 */
	private String adminProvisionProportion;
	/**
	 * 砍头费费率
	 */
	private BigDecimal upfrontFeeRate;
	/**
	 * 逾期费费率
	 */
	private BigDecimal lateRate;
	/**
	 * 提前结清折扣费率
	 */
	private BigDecimal advanceRate;
	/**
	 * 资金方费率
	 */
	private BigDecimal fundInterestRate;
	/**
	 * 服务费费率
	 */
	private BigDecimal serviceFeeRate;
	/**
	 * 
	 */
	private BigDecimal fundProvisionRate;
	/**
	 * 扩展信息
	 */
	private String extend;
	/**
	 * 砍头费中的资金方利率
	 */
	private BigDecimal upfrontFundInterestRate;
	/**
	 * 砍头费中的服务费
	 */
	private BigDecimal upfrontServiceFeeRate;
	/**
	 * 砍头费中的保证金费率
	 */
	private BigDecimal upfrontProvisionRate;


}
