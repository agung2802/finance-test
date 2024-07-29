package com.test.api.entity.vo;

import lombok.Data;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
@Data
public class DdmRelation {
	/**
	 * A ，B计划
	 */
	private String planType ;
	/**
	 * 发薪日和放款时间的差值
	 */
	private int dateDiff;
	/**
	 * 计算额外费用的差值
	 */
	private int ddmDays;
	/**
	 * 下一期还款时间
	 */
	private String nextDueDate;
	/**
	 * 最后一期还款时间(非ddm)
	 */
	private String lastDueDate;
	/**
	 * 最后一期还款时间（DDM计算出来的天数）
	 */
	private String lastDueDateDdm;

}
