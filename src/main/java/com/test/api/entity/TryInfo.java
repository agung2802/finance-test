package com.test.api.entity;

import lombok.Data;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29
* 类说明
*/
@Data
public class TryInfo {
	/**
	 * 二级产品
	 */
	private  String secondProductCode;
	/**
	 * 借款金额、E计划新订单本金
	 */
	private long amount;
	/**
	 * 借款期数
	 */
	private String tenor;
	/**
	 * 订单号
	 */
	private String loanNumber;

	/**
	 * rebook的老订单号
	 */
	private String rebookOLdLoanNumber;
	/**
	 * 优惠码
	 */
	private String promotionCode;
	/**
	 *    1计算使用优惠码前的优惠金额
	 */
	private int tryFlag;
	
	/**
	 * 优惠码详情
	 */
	private PromotionCodeDetail promotionCodeDetail;
	/**
	 * 是否选中保费
	 */
	private boolean insuranceSelect;
	/**
	 * 保费金额
	 */
	private long insurancePremiumAmount;
}
