package com.test.api.controller;

import com.test.api.entity.LoanTryEPlan;
import com.test.api.entity.LoanTryFPlan;
import com.test.api.entity.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.test.api.common.Urls;
import com.test.api.entity.TryCPlanInfo;
import com.test.api.entity.TryInfo;
import com.test.api.service.CostCalculationService;
import com.test.api.service.RepaymentService;
import com.test.api.utils.Response;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29
* 类说明
*/
@RestController
public class FinaneTrial {
	@Autowired
	CostCalculationService costCalculationService;
	@Autowired
	RepaymentService repaymentService;

	/**
	 * 还款计划试算
	 * @param tryInfo
	 * @return
	 */
	@PostMapping(Urls.Repay_Calculate)
	public Response repayCalculate(@RequestBody TryInfo tryInfo) {
		UpfrontDuesVo calculateUpfrontDues = costCalculationService.CalculateUpfrontDues(tryInfo);
		return new Response(calculateUpfrontDues);
	}

	/**
	 * C 计划试算
	 * @param tryCPlanInfo
	 * @return
	 */
	@PostMapping(Urls.CPLAN_Repay_Calculate)
	public Response cPlanRepayCalculate(@RequestBody TryCPlanInfo tryCPlanInfo) {
		TryCPlan repayByCplan = repaymentService.repayByCplan(tryCPlanInfo);
		return new Response(repayByCplan);
	}

	/**
	 * E 计划试算
	 * @param ePlanlist
	 * @return
	 */
	@PostMapping(Urls.EPLAN_Repay_Calculate)
	public Response ePlanRepayCalculate(@RequestBody List<LoanTryEPlan> ePlanlist){
		TryEPlanVo tryEPlanVo = repaymentService.repayByEplan(ePlanlist);
		return new Response(tryEPlanVo);
	}

	/**
	 * F 计划试算
	 * @param loanTryFPlan
	 * @return
	 */
	@PostMapping(value = Urls.FPLAN_Repay_Calculate, produces = "application/json;charset=UTF-8")
	public Response fPlanRepayCalculate(@RequestBody LoanTryFPlan loanTryFPlan){
		TryFPlanVo tryFPlanVo = repaymentService.repayByFplan(loanTryFPlan);
		return new Response(tryFPlanVo);
	}
	@GetMapping(value=Urls.Advance_Repay_Calculate, produces = "application/json;charset=UTF-8")
	public Response advanceRepayCalculate(@RequestParam String loanNumber){
		RepayByadvanceVo repayByadvanceVo = (RepayByadvanceVo) repaymentService.repayByadvance(loanNumber);
		return new Response(repayByadvanceVo);
	}
}
