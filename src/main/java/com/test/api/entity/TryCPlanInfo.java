package com.test.api.entity;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29
* 类说明
*/
@Data
public class TryCPlanInfo {
	/**
	 * 用户自定义还款金额
	 */
	private String amount;
	/**
	 * 订单号以及减免比例
	 */
	private List<Map<String,Object>> tryCList ;
}
