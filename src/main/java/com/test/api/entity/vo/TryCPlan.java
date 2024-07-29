package com.test.api.entity.vo;

import java.math.BigDecimal;
import java.util.List;

import com.test.api.entity.LoanTryCPlan;

import lombok.Data;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29
* 类说明
*/
@Data
public class TryCPlan {
	/**
	 * C计划试算列表
	 */
	public List<LoanTryCPlan> loanTryCPlanList;
	/**
	 * C计划最小还款金额
	 */
	private BigDecimal totalMinAmount;
	/**
	 * C计划最大还款金额
	 */
	private BigDecimal totalMaxAmount;
	/**
	 * C计划所有订单下一期要还的总利息(资金方利息+平台服务费+平台服务费税费+保证金)
	 */
	private BigDecimal totalInterest;
	/**
	 * C计划所有订单下一期应还本金
	 */
	private BigDecimal totalPrincipal;
	/**
	 * C计划所有订单下一期应还进位费（包含进位费税费）
	 */
	private BigDecimal totalCarryingFee;
	/**
	 * C计划所有订单下一期应还金额之和
	 */
	private BigDecimal totalplanAmount;
	/**
	 * C计划所有订单下一期应还金额之和和C计划还款金额
	 */
	private BigDecimal nextTotalAmount;
}
