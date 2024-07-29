package com.test.api.common;
/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* Class description
*/
public final class Urls {
	
	public static final String ROOT = "/awsom-test";
	public static final String MODULE = "/job";
	
	public static final String JOB_LIST = MODULE+"/getJobList";
	public static final String JOB_LIST_DESC = "Job configuration";
	
	public static final String TRIGGER_JOB = MODULE+"/triggerJob";
	public static final String TRIGGER_JOB_DESC = "trigger job";

	public static final String Finance_MODULE = "/finance";
	/**
	 * Trial calculation interface
	 */
	public static final String Repay_Calculate = Finance_MODULE +"/repayCalculate";
	/**
	 * Plan C Trial Calculation
	 */
	public static final String CPLAN_Repay_Calculate = Finance_MODULE +"/cPlanRepayCalculate";
	/**
	 * E plan trial calculation
	 */
	public static final String EPLAN_Repay_Calculate = Finance_MODULE +"/ePlanRepayCalculate";

	/**
	 * Plan F Trial Calculation
	 */
	public static final String FPLAN_Repay_Calculate = Finance_MODULE +"/fPlanRepayCalculate";
	/**
	 *Settlement trial in advance
	 */
	public static final String Advance_Repay_Calculate = Finance_MODULE +"/advanceRepayCalculate";
	/**
	 *
	 */
	public static final String Tob_MODULE = "/tob";

	public static final String Upload = Tob_MODULE+"/upload";
}
