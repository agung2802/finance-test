/**
 * 
 */
package com.test.api.entity;

import lombok.Data;

/**  
 * @ClassName: LoanTryEPlan
 * @Description: TODO(描述)
 * @author Faisal Mulya Santosa
 * @date 2024-07-29 
*/
@Data
public class LoanTryEPlan {
	/**
	 * 减免的逾期费比例，应还的逾期费比例为  1-discountLFratio
	 */
	private String discountLFratio;
	/**
	 * 还款比例
	 */
	private String disDownPaymentRatio;
	/**
	 * 新订单期数
	 */
	private String tenor;
	/**
	 * 老订单
	 */
	private String loanNumber;

	
}
