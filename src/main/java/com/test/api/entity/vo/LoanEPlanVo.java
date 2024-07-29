/**
 * 
 */
package com.test.api.entity.vo;

import java.util.List;
import java.util.Map;

import com.test.api.entity.Dues;
import com.test.api.entity.RepayDuesExt;

import lombok.Data;

/**  
 * @ClassName: LoanEPlanVo
 * @Description: TODO(描述)
 * @author Faisal Mulya Santosa
 * @date 2024-07-29 
*/
@Data
public class LoanEPlanVo {
	/**
	 * 当前订单未还本金
	 */
	private long  amountFund;
	/**
	 * 当前订单未还的资金方利息
	 */
	private long  interestFunder;
	/**
	 * 当前订单未还的平台服务费和税费
	 */
	private long  platformfee;
	/**
	 * 当前订单未还的保证金
	 */
	private long  provisionFee;
	/**
	 * 当前订单未还的减免后应还的逾期费以及应还的逾期费税费
	 */
	private long  lateFeeAfterDiscount;
	/**
	 * 当前订单未还金额包含税费
	 */
	private long  totalOutstandingFee;
	/**
	 * 新订单第一期应还款
	 */
	private long   firstNewInstallment;
	/**
	 * 新订单第一期还款时间
	 */
	private String  firstNewDueDate;
	
	/**
	 * 新订单本金
	 */
	private long   amountTransfer ;
	/**
	 * 新订单本金中的各项费用：oldOrderUnPayMap
	老订单未结清本金：principal
	老订单未结清逾期费: lateFeeIncludeVat
	老订单未结清资金方利息: fundInterest
	老订单未结清平台服务费: serviceFee
	老订单未结清保证金: provisionFee
	 */
	private Map<String,Object> oldOrderUnPayMap;
	
	/**
	 * 最低还款金额
	 */
	private long   minimumPayment ;
	/**
	 * 需转回给adwa的费用
	 */
	private long   EplanToAdwa ;
	/**
	 * E计划老订单未结清本金
	 */
	private long  ePlanOldOrderUnpayPrincipal;
	/**
	 * E计划抵扣后的dues
	 */
	private List<RepayDuesExt> deductDues;
	/**
	 * E计划需还款的dues
	 */
	private List<Dues> beforeDeductDues;
	/**
	 * 老订单
	 */
	private String loanNumber;
	/**
	 * E计划试算结果
	 */
	private UpfrontDuesVo upfrontDuesVo;
}
