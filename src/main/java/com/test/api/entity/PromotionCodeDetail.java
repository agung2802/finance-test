package com.test.api.entity;

import java.math.BigDecimal;

import lombok.Data;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* Class description
*/
@Data
public class PromotionCodeDetail {
	/**
	 * 打折  替换 减免的砍头费
	 */
	private BigDecimal  upfrontFee;
	/**
	 * 打折  替换 减免的服务费（资金方利息+平台服务费）
	 */
	private BigDecimal  interest;
	/**
	 * 打折  替换 的逾期费率（没有打折）
	 */
	private BigDecimal  overdue;
	/**
	 * 打折  替换 减免的管理费  现在没有这个配置
	 */
	private BigDecimal  adminFee;
	private BigDecimal principal;
	/**
	 * 优惠码类型打折 1 替换2 减免3 
	 */
	private int   preferentialType;


}
