package com.test.api.entity;

import java.math.BigDecimal;

import lombok.Data;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29
* Class description
*/
@Data
public class DiscountRepayAmount {
	
	/**
	 * Monthly repayment discount amount
	 */
	private BigDecimal discountRepaymentAmount;
	/**
	 * Reduced principal
	 */
	private BigDecimal discountPrincipal  ;
	/**
	 * Reduced service fee
	 */
	private BigDecimal discountServiceFee;
	/**
	 * Deducted funding interest
	 */
	private BigDecimal discountFundInterest;
	/**
	 * Reduced management fees
	 */
	private BigDecimal discountAdminFee;
	/**
	 * Reduced late fees
	 */
	private BigDecimal discountLateFee;

	
}
