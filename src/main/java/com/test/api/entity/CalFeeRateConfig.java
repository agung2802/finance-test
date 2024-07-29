package com.test.api.entity;

import java.math.BigDecimal;

import lombok.Data;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* Class description
*/
@Data
public class CalFeeRateConfig {

	/**
	 * Beheading rate
	 */
	private BigDecimal upfrontFeeRate;
	/**
	 * The interest rate used to calculate the funding side's interest in a repayment plan
	 */
	private BigDecimal duesFundInterestRate;
	/**
	 * Calculate the interest rate for platform service fees in the repayment plan
	 */
	private BigDecimal duesServiceFeeRate;
	/**
	 * Calculate the interest rate on the security deposit in the repayment plan
	 */
	private BigDecimal duesProvisionRate;
	/**
	 * Late fee rate
	 */
	private BigDecimal lateRate;
	/**
	 * Early settlement ratio
	 */
	private BigDecimal advanceRate;
	/**
	 * Management fee in V1 product: margin
	 */
	private String provisionRate;
	/**
	 * Expanded fields in beheading fee
	 */
	private ExtendUpfrontRate extendUpfrontRate;


}
