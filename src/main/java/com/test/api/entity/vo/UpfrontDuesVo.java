package com.test.api.entity.vo;

import com.test.api.entity.PromotionCodeDetail;
import com.test.api.entity.TransferAmount;

import lombok.Data;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 
* 类说明
*/
@Data
public class  UpfrontDuesVo {
	/**
	 * 砍头费
	 */
	private UpfrontFeeVO upfrontFeeVO;
	/**
	 * 还款计划
	 */
	private DuesVo duesVo;
	/**
	 * DDM相关
	 */
	private DdmRelation ddmRelation;
	/**
	 * 转账金额
	 */
	private TransferAmount transfer;
	/**
	 * 优惠码信息
	 */
	private PromotionCodeDetail promotionCodeDetail;
	/**
	 * rebook相关费用
	 */
	private RebookVo  rebookVo;
	/**
	 * E计划老订单未结清本金
	 */
	private long  ePlanOldOrderUnpayPrincipal;
}
