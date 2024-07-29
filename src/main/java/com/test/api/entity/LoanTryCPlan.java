package com.test.api.entity;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29
* Class description
*/
@Data
public class LoanTryCPlan {
	/**
	 * 订单号
	 */
	private String loanNumber;
	/**
	 * C计划最小还款金额
	 */
	private BigDecimal minAmount;
	/**
	 * C计划最大还款金额
	 */
	private BigDecimal maxAmount;
	/**
	 * 短期展期的借款天数
	 */
	private int cPlanTenor;
	/**
	 * 每笔订单占的比例
	 */
	private BigDecimal proportion;
	/**
	 * C计划的减免逾期费比例
	 */
	private BigDecimal discountLFratio;
	/**
	 * C计划分摊到当前订单的还款金额
	 */
	private BigDecimal customRepaymAmount;
	/**
	 * C计划实际要还的逾期费比例
	 */
	private BigDecimal repayDiscount;
	/**
	 * C计划可以抵扣到的费用项
	 */
	private RepayDuesExt deductDues;
	/**
	 * C计划可以抵扣到的费用项
	 */
	private List<Dues> beforeDeductDues;
	/**
	 * 展期的还款计划
	 */
	private RepayDues nextDues;

}
