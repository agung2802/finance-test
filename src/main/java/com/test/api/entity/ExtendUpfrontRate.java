package com.test.api.entity;

import java.math.BigDecimal;

import lombok.Data;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29
* Class description
*/
@Data
public class ExtendUpfrontRate {
	/**
	 * 砍头费中资金方费率
	 */
	private BigDecimal fundInterestRate;
	/**
	 * 砍头费中保证金费率
	 */
	private BigDecimal provisionRate;
	/**
	 * 砍头费中平台服务费率
	 */
	private BigDecimal serviceFeeRate;
}
