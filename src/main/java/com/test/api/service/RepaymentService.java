package com.test.api.service;

import java.util.List;

import com.test.api.entity.LoanTryFPlan;
import com.test.api.entity.vo.LoanEPlanVo;
import com.test.api.entity.vo.TryEPlanVo;
import com.test.api.entity.vo.TryFPlanVo;
import org.apache.poi.ss.formula.functions.T;

import com.test.api.entity.TryCPlanInfo;
import com.test.api.entity.vo.TryCPlan;
import com.test.api.entity.LoanTryEPlan;


/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29
* 类说明
*/
public interface RepaymentService {
	/**
	 * 提前结清
	 * @param loanNumber
	 * @return
	 */

	Object repayByadvance(String loanNumber);
	/**
	 * C计划还款
	 * @param tryCPlanInfo
	 * @return
	 */

	TryCPlan repayByCplan(TryCPlanInfo tryCPlanInfo);
	/**
	 * E计划还款
	 * @param ePlanlist
	 * @return
	 */
	TryEPlanVo repayByEplan(List<LoanTryEPlan> ePlanlist);
	/**
	 * F计划还款
	 * @param loanTryFPlan
	 * @return
	 */
	TryFPlanVo repayByFplan(LoanTryFPlan loanTryFPlan);
	/**
	 * 通过优惠码还款
	 * @param loanNumber
	 * @return
	 */
	Object repayByPromotionCode(String loanNumber);
}
