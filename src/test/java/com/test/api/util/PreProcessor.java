package com.test.api.util;

import java.util.HashMap;

import com.test.api.pojo.ApiConfig;
import com.test.api.util.preUtils.EpalnPreProcess;
import com.test.api.util.preUtils.PreProcessByCPlan;
import org.eclipse.jetty.util.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.test.api.cases.BaseCase;
import com.test.api.util.preUtils.PreProcessorUtil;

import lombok.extern.slf4j.Slf4j;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29
* 类说明
*/
@Slf4j
@Component
//@SpringBootTest
public class PreProcessor {
/**
 * 前置参数：标签，产品类型，借款金额，借款期数，放款方式，
 */
	@Autowired
	PreProcessorUtil preProcessorUtil;
	@Autowired
	PreProcessByCPlan preProcessByCPlan;

	@Autowired
	EpalnPreProcess epalnPreProcess;

	public void disburse(HashMap<String,Object>  paramtersMap, ApiConfig apiConfigTest) {
		try {
			preProcessorUtil.applyLimit(paramtersMap,apiConfigTest);
			preProcessorUtil.approvaLimit(paramtersMap,apiConfigTest);
			if(!paramtersMap.containsValue("paylater_long_v1")&&!paramtersMap.containsValue("paylater_short_v1")) {
				if (paramtersMap.containsKey("amount")) {
					preProcessorUtil.useLimit(paramtersMap,apiConfigTest);
				} else {
					preProcessorUtil.randomAmountTenor(paramtersMap,apiConfigTest);
					preProcessorUtil.useLimit(paramtersMap,apiConfigTest);
				}
			}else if(paramtersMap.containsValue("paylater_long_v1")||paramtersMap.containsValue("paylater_short_v1")) {
				preProcessorUtil.useLimitByPL(paramtersMap,apiConfigTest);
			}
		} catch (Exception e) {
			BaseCase.abnormalNum++;
			throw new RuntimeException(e);
			// TODO: handle exception
		}
		for(int i=0;i<=3;i++) {
			preProcessorUtil.triggerScheduledTask();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
/**
  * @Desc
  * @Params
  * @Return
  * @author Faisal Mulya Santosa
  * @Date 2024-07-29
  **/
	public void rapayByAdvance(HashMap<String,Object> paramtersMap,ApiConfig apiConfigTest){
		disburse(paramtersMap,apiConfigTest);
		if(paramtersMap.containsKey("overDueday")){
			preProcessorUtil.setOverDue(paramtersMap);
		}
        preProcessorUtil.buildAdvance(paramtersMap);
	}

/**
  * @Desc   C计划还款
  * @Params
  * @Return
  * @author Faisal Mulya Santosa
  * @Date 2024-07-29
  **/
	public void rapayByCplanApp(HashMap<String,Object> paramtersMap,ApiConfig apiConfigTest){
		disburse(paramtersMap,apiConfigTest);
		if(paramtersMap.containsKey("overDueday")){
			preProcessorUtil.setOverDue(paramtersMap);
		}
		preProcessByCPlan.applyCPlanByApp(paramtersMap);
	}


	public void rapayByCplanBackground(HashMap<String,Object> paramtersMap,ApiConfig apiConfigTest){
		disburse(paramtersMap,apiConfigTest);
		if(paramtersMap.containsKey("overDueday")){
			preProcessorUtil.setOverDue(paramtersMap);
		}
		preProcessByCPlan.applyCPlanBackground(paramtersMap);
		preProcessByCPlan.confirmCplanByapp(paramtersMap);
	}


	/**
	 * app提交E计划申请    后台再确定E计划
	 * @param paramtersMap
	 */
	public  void repayByEplan(HashMap<String,Object> paramtersMap,ApiConfig apiConfigTest){
		disburse(paramtersMap,apiConfigTest);
		if(paramtersMap.containsKey("overDueday")){
			preProcessorUtil.setOverDue(paramtersMap);
		}
		epalnPreProcess.preApplyEplan(paramtersMap);
		epalnPreProcess.submitEplan(paramtersMap);
		epalnPreProcess.confirmEplan(paramtersMap);

	}
}
