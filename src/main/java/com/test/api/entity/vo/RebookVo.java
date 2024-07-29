package com.test.api.entity.vo;

import lombok.Data;

/**
 * @author Faisal Mulya Santosa
 * @version Creation time: 2024-07-29
 * Class description
 */
@Data
public class RebookVo {
	/**
	 * 老订单相关
	 */
	// 老订单借款本金
	private long oldApplyAmount;
	//老订单未结清本金
	private long oldPrincipal;
	//老订单未结清资金方利息
	private long oldFundInterest;
	//老订单未结清平台服务费(不含税费)
	private long oldServiceFee;
	//老订单未结清平台服务费(税费)
	private long oldServiceFeeVat;
	//老订单未结清应还款金额
	private long remainingAmount;
	//老订单未结清应还款金额（不含进位费）
	private long remainingAmountNotIncludeCarryFee;

	/**
	 * 新订单相关:
	 */
	//新订单结算费用相关金额
	private long realAmount;
	//新订单砍头费
	private long upfrontFee;
	//用户收到的金额
	private long receivedAmount;
	//放款时需转给资金方的本金
	private long repayTransferToFunder;
	// 新订单借款本金
	private long newApplyAmount;
	
}
