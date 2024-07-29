/**
 * 
 */
package com.test.api.entity.vo;

import lombok.Data;

import java.util.List;

/**  
 * @ClassName: TryEPlanVo
 * @Description: TODO(描述)
 * @author Faisal Mulya Santosa
 * @date 2024-07-29 
*/
@Data
public class TryEPlanVo {
	/**
	 * 每笔订单试算结果
	 */
	private List<LoanEPlanVo>   LoanEPlanVoList;
	/**
	 * 总的最低还款金额
	 */
	private long totalMinimumPayment;
}
