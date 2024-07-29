package com.test.api.entity;

import java.math.BigDecimal;

import lombok.Data;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29
* Class description
*/
@Data
public class DiscountUpfrontFeeVo {
	/**
	 * Reduced beheading fee
	 */
	private BigDecimal discountUpfrontFee;
	/**
	 * Deducted funding interest
	 */
	private BigDecimal discountFundInterest;
	/**
	 * Reduced platform service fees
	 */
	private BigDecimal discountUpfrontServiceFee;
	/**
	 * Reduced security deposit
	 */
	private BigDecimal discountUpfrontProvisionFee;
	/**
	 * Reduced management fees
	 */
	private BigDecimal discountUpfrontAdminFee;
	
	

}
