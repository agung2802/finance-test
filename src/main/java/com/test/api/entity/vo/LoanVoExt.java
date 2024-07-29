/**
 * 
 */
package com.test.api.entity.vo;

import java.math.BigDecimal;

import lombok.Data;

/**  
 * @ClassName: LoanVoExt
 * @Description: TODO(描述)
 * @author Faisal Mulya Santosa
 * @date 2024-07-29 
*/

@Data
public class LoanVoExt {

	/**
	 * 砍头费中的保证金费率
	 */
	private BigDecimal upfrontProvisionFeeRate;

	/**
	 * 砍头费中资金方利率
	 */
	private BigDecimal upfrontFundInterestRate;
	/**
	 * 砍头费中平台服务费率
	 */
	private BigDecimal upfrontServiceFeeRate;
	
	
	/**
	 * 砍头费中的保证金
	 */
	private BigDecimal upfrontProvisionFee;

	/**
	 * 砍头费中资金方
	 */
	private BigDecimal upfrontFundInterest;
	/**
	 * 砍头费中平台服务费
	 */
	private BigDecimal upfrontServiceFee;
	/**
	 * 砍头费中管理费
	 */
	private BigDecimal upfrontAdminFee;
	/**
	 *总砍头费
	 */
	private long upfrontFee;

	
	private Boolean upfrontFundInterestAssertion;
	private Boolean upfrontServiceFeeAssertion;
	private Boolean upfrontProvisionFeeAssertion;
	private Boolean upfrontAdminFeeAssertion;
	private Boolean upfrontFeeAssertion;
	
	/**
	 * V1产品，砍头费中的管理费，保证金
	 */
	private Long adminFee;
	private Long provisionFee;
	/**
	 * 资金方利息
	 */
	private BigDecimal fundInterestFee;
	/**
	 * 平台服务费
	 */
	private BigDecimal serviceFee;
	/**
	 * 资金方利率
	 */
	private BigDecimal fundInterestRate;
	/**
	 * 平台服务费率
	 */
	private BigDecimal serviceFeeRate;
	
	
	private Boolean adminFeeAssertion;
	private Boolean provisionFeeAssertion;
	private Boolean fundInterestFeeAssertion;
	private Boolean serviceFeeAssertion;

}
