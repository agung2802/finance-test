package com.test.api.entity;

import java.math.BigDecimal;

import lombok.Data;
import lombok.ToString;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
@ToString(callSuper = true)
@Data
public class RepayDuesExt extends RepayDues {

	/**
	 * 逾期费
	 */
	private BigDecimal LateFee;
	/**
	 * 逾期费税费
	 */
	private BigDecimal LateFeeVAT;
	/**
	 * 期数
	 */
	private int dueIndex;

}
