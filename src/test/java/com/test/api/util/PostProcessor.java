package com.test.api.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSONPath;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.test.api.entity.Dues;
import com.test.api.entity.FinanceTrading;
import com.test.api.entity.FundAccount;
import com.test.api.entity.GatewayTask;
import com.test.api.entity.RepayDues;
import com.test.api.entity.UpfrontExtendFee;
import com.test.api.entity.vo.LoanVo;
import com.test.api.entity.vo.UpfrontDuesVo;
import com.test.api.entity.vo.UpfrontFeeVO;
import com.test.api.mapper.DuesMapper;
import com.test.api.mapper.FinanceTradingMapper;
import com.test.api.mapper.FundAccountMapper;
import com.test.api.mapper.GatewayTaskMapper;

import lombok.extern.slf4j.Slf4j;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
//@SpringBootTest
@Slf4j
@Component
public class PostProcessor {
	@Autowired
	private FinanceTradingMapper financeTradingMapper;
	@Autowired
	private FundAccountMapper fundAccountMapper;
	@Autowired
	private GatewayTaskMapper gatewayTaskMapper;
	@Autowired
	CostCalculation calculation;
	@Autowired
	DuesMapper duesMapper;
	public   void getAdminFeeAssertion(LoanVo loanVo) {
		System.out.println("=====getAdminFeeAssertion方法被执行");
	}
	/**
	 * 校验费率
	 * @param loanVo
	 */
	public void checkRateConfig(LoanVo loanVo) {
		log.info("校验费率");
		Boolean upfrontFeeRateAssertion = loanVo.getUpfrontFeeRate().compareTo(loanVo.getRateConfig().getUpfrontFeeRate())==0? true: false;
		loanVo.getAssertionMap().put("upfrontFeeRateAssertion", upfrontFeeRateAssertion);

		//v1砍头费中管理费和保证金的比例
		if(loanVo.getRateConfig().getSecondProdCode().contains("v1")&&loanVo.getLoanNumber().startsWith("CL")) {
			
			Boolean adminProvisionProportionAssertion = loanVo.getAdPvProportion().equalsIgnoreCase
					(loanVo.getRateConfig().getAdminProvisionProportion());
			loanVo.getAssertionMap().put("adminProvisionProportionAssertion", adminProvisionProportionAssertion);
		}
		Boolean fundInterestRateAsserttion = loanVo.getFundInterestRate().compareTo
				(loanVo.getRateConfig().getFundInterestRate())==0? true:false;
		Boolean serviceFeeRateAeertion = loanVo.getServiceFeeRate().compareTo(
				loanVo.getRateConfig().getServiceFeeRate())==0? true:false;
		loanVo.getAssertionMap().put("fundInterestRateAsserttion", fundInterestRateAsserttion);
		loanVo.getAssertionMap().put("serviceFeeRateAeertion", serviceFeeRateAeertion);

		if(loanVo.getRateConfig().getFundProvisionRate()!=null) {
			Boolean fundProvisionRateAssertion = loanVo.getProvisionFeeRate().compareTo(
					loanVo.getRateConfig().getFundProvisionRate())==0? true: false;
			loanVo.getAssertionMap().put("fundProvisionRateAssertion", fundProvisionRateAssertion);

		}
		if(loanVo.getRateConfig().getSecondProdCode().contains("v2")&&loanVo.getLoanNumber().startsWith("CL")) {
			Boolean upfrontFundInterestRateAssertion=loanVo.getUpfrontFundInterestRate().compareTo(
					loanVo.getRateConfig().getUpfrontFundInterestRate())==0? true:false;
			Boolean upfrontServiceFeeRateAssertion=loanVo.getUpfrontServiceFeeRate().compareTo(
					loanVo.getRateConfig().getUpfrontServiceFeeRate())==0? true:false;
			Boolean upfrontProvisionFeeRateAssertion=loanVo.getUpfrontProvisionFeeRate().compareTo(
					loanVo.getRateConfig().getUpfrontProvisionRate())==0? true:false;	
			loanVo.getAssertionMap().put("upfrontFundInterestRateAssertion", upfrontFundInterestRateAssertion);
			loanVo.getAssertionMap().put("upfrontServiceFeeRateAssertion", upfrontServiceFeeRateAssertion);
			loanVo.getAssertionMap().put("upfrontProvisionFeeRateAssertion", upfrontProvisionFeeRateAssertion);

		}
		Boolean lateRateAssertion = loanVo.getLateRate().compareTo(loanVo.getRateConfig().getLateRate())==0?true: false;
		loanVo.getAssertionMap().put("lateRateAssertion", lateRateAssertion);
		if(loanVo.getAdvanceRate()!=null) {
			Boolean advanceRateAssertion = loanVo.getAdvanceRate().compareTo(loanVo.getRateConfig().getAdvanceRate())==0?true: false;
			loanVo.getAssertionMap().put("advanceRateAssertion", advanceRateAssertion);
		}
		log.info("校验费率完成");

	}
	/**
	 * NS_astra_maucash_short_B1-2_v2  CL21110119274480706229512
	 * NS_maucash_long_A2-3_v2   CL21110815371579303655847  平台服务费差异 162002 162000 
	 * NS_maucash_short_A1-3_v1   CL21110816043716069152420  无差异
	 * NS_astra_maucash_long_B1-2_v1 CL21111014464426325044371  平台服务费有差异54105  54102
	 * 线下放款  CL21111717104634673442723
	 */
	/**
	 * 检查loandetai中状态
	 * @param loanVo
	 */
	public void checkLoanDetailstatus(LoanVo loanVo) {
		Boolean flag = loanVo.getLendStatus().compareTo(3)==0? true:false;
		Boolean flag2 = loanVo.getRepayStatus().compareTo(1)==0? true:false;
		loanVo.getCheckStatus().setCheckLendStatusAssertion(flag);
		loanVo.getCheckStatus().setCheckRepayStatusAssertion(flag2);
		loanVo.getAssertionMap().put("LendStatusAssertion", flag);
		loanVo.getAssertionMap().put("RepayStatusAssertion", flag2);


	}
	
	public void checkLendstatus(LoanVo loanVo) {
		Boolean flag = loanVo.getLendStatus().compareTo(3)==0? true: false;
		loanVo.getCheckStatus().setCheckStatusAssertion(flag);
		loanVo.getAssertionMap().put("financeLendStatusAssertion", flag);
	}
	
	public void checkLoansState(LoanVo loanVo) {
		Boolean flag = loanVo.getState().compareTo("disbursed")==0? true: false;
		loanVo.getCheckStatus().setCheckLoansStateAssertion(flag);
		loanVo.getAssertionMap().put("LoansStateAssertion", flag);
	}
	

//	@Test
	public  void checkFundInterest(LoanVo loanVo)  {
		System.out.println("=====getFundInterestAssertion方法被执行");
//		LoanVo loanVo = new LoanVo();
//		loanVo.setLoanNumber("CL21110119274480706229512");
//		calculation.buildLoanVo(loanVo);
		BigDecimal UpfrontFundInterestAct = null;
		String tenor = loanVo.getTenor().substring(0, loanVo.getTenor().length()-1);
		BigDecimal fundInterestRate = null;
		BigDecimal fundInterestFee = null;
		if(loanVo.getSecondProductCode().contains("v2")) {
			fundInterestRate = loanVo.getUpfrontFundInterestRate();
			fundInterestFee = loanVo.getUpfrontFundInterest();
		}else if(loanVo.getSecondProductCode().contains("v1")) {
			fundInterestRate = loanVo.getFundInterestRate();
			fundInterestFee = loanVo.getFundInterestFee();
		}
//		if(loanVo.getSecondProductCode().contains("v2")) {
			UpfrontFundInterestAct = new BigDecimal(loanVo.getAmount())
					.multiply(fundInterestRate).multiply(new BigDecimal(tenor));

		 if(loanVo.getSecondProductCode().contains("long")){
//			UpfrontFundInterestAct = new BigDecimal(loanVo.getAmount())
//					.multiply(loanVo.getUpfrontFundInterestRate()).multiply(new BigDecimal(tenor));
			//额外的资金方利息
			 BigDecimal UpfrontFundInterestExtAct = fundInterestRate.divide(new BigDecimal(30), 6,BigDecimal.ROUND_HALF_UP)
					 .multiply(new BigDecimal(loanVo.getDdmDay())).multiply(new BigDecimal(loanVo.getAmount()));
			if(loanVo.getDdmPlan().equalsIgnoreCase("A")) {
				UpfrontFundInterestAct =  UpfrontFundInterestAct.add(UpfrontFundInterestExtAct);
				log.info("计划：{}，差值：{}，额外的资金方利息{}",loanVo.getDdmPlan(),loanVo.getDdmDay(),UpfrontFundInterestExtAct);
			}else if(loanVo.getDdmPlan().equalsIgnoreCase("B")) {
				UpfrontFundInterestAct =  UpfrontFundInterestAct.subtract(UpfrontFundInterestExtAct);
				log.info("计划：{}，差值：{}，额外的资金方利息{}",loanVo.getDdmPlan(),loanVo.getDdmDay(),UpfrontFundInterestExtAct);
			}	
		}
//		 Boolean AssertionResult = UpfrontFundInterestAct.compareTo(fundInterestFee)==0 ?true:false;
		//差值小于10，默认两金额相等UpfrontFundInterestAct.compareTo(fundInterestFee)
		 Boolean AssertionResult = Math.abs(UpfrontFundInterestAct.subtract(fundInterestFee).intValue())<10 ?true:false;
		 if(loanVo.getSecondProductCode().contains("v2")) {	 
			 loanVo.getLoanVoExt().setUpfrontFundInterest(UpfrontFundInterestAct);
			 loanVo.getLoanVoExt().setUpfrontFundInterestAssertion(AssertionResult);
			 loanVo.getAssertionMap().put("UpfrontFundInterestAssertion", AssertionResult);
			 log.info("getUpfrontFundInterestAssertion:::{}",loanVo);
		 }else if(loanVo.getSecondProductCode().contains("v1")) {
			 loanVo.getLoanVoExt().setFundInterestFee(UpfrontFundInterestAct);
			 loanVo.getLoanVoExt().setFundInterestFeeAssertion(AssertionResult);
			 loanVo.getAssertionMap().put("FundInterestFeeAssertion",AssertionResult);
			 log.info("getUpfrontFundInterestAssertion:::{}",loanVo);
		 }
	}

//	@Test
	public  void checkUpfrontServiceFee(LoanVo loanVo)  {
		System.out.println("=========getServiceFeeAssertion方法被执行");
//		LoanVo loanVo = new LoanVo();
//		loanVo.setLoanNumber("CL21110119274480706229512");
//		calculation.buildLoanVo(loanVo);
		BigDecimal UpfrontServiceFeeAct = null;
		String tenor = loanVo.getTenor().substring(0, loanVo.getTenor().length()-1);
		BigDecimal ServiceFeeRate = null;
		BigDecimal ServiceFee = null;
		if(loanVo.getSecondProductCode().contains("v2")) {
			ServiceFeeRate = loanVo.getUpfrontServiceFeeRate();
			ServiceFee = loanVo.getUpfrontServiceFee();
		}else if(loanVo.getSecondProductCode().contains("v1")) {
			ServiceFeeRate = loanVo.getServiceFeeRate();
			ServiceFee = loanVo.getServiceFee();
		}
		UpfrontServiceFeeAct = new BigDecimal(loanVo.getAmount())
				.multiply(ServiceFeeRate).multiply(new BigDecimal(tenor));
		if(loanVo.getSecondProductCode().contains("v2")) {
			// Boolean AssertionResult = UpfrontServiceFeeAct.compareTo(loanVo.getUpfrontServiceFee())==0 ?true:false;
			//差值小于10，默认两金额相等
			Boolean AssertionResult = Math.abs(UpfrontServiceFeeAct.subtract(loanVo.getUpfrontServiceFee()).intValue())<10 ?true:false;

			loanVo.getLoanVoExt().setUpfrontServiceFee(UpfrontServiceFeeAct);
			loanVo.getLoanVoExt().setUpfrontServiceFeeAssertion(AssertionResult);
			loanVo.getAssertionMap().put("UpfrontServiceFeeAssertion", AssertionResult);
			//如果是short V2只需比较砍头费中的平台服务费
//			if(loanVo.getSecondProductCode().contains("short")) {
//				log.info("getUpfrontServiceFeeAssertion:{}",loanVo);
//				return;
//			}
		}
		if(loanVo.getSecondProductCode().contains("long")) {
			if(loanVo.getSecondProductCode().contains("v2")) {
				UpfrontServiceFeeAct = new BigDecimal(loanVo.getAmount())
						.multiply(loanVo.getServiceFeeRate()).multiply(new BigDecimal(tenor));
				ServiceFeeRate = loanVo.getServiceFeeRate();
				ServiceFee = loanVo.getServiceFee();
			}
			 BigDecimal UpfrontServiceExtAct =ServiceFeeRate.divide(new BigDecimal(30), 
					 6, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(loanVo.getDdmDay()))
					 .multiply(new BigDecimal(loanVo.getAmount()));
			 log.info("ServiceFeeRate:{}",ServiceFeeRate);

			 log.info("UpfrontServiceExtAct:{}",UpfrontServiceExtAct);
			 if("A".equalsIgnoreCase(loanVo.getDdmPlan())) {
				 UpfrontServiceFeeAct = UpfrontServiceFeeAct.add(UpfrontServiceExtAct);
				 log.info("UpfrontServiceFeeAct:{},UpfrontServiceExtAct{}",UpfrontServiceFeeAct,UpfrontServiceExtAct);

			 }else if("B".equalsIgnoreCase(loanVo.getDdmPlan())) {
				 UpfrontServiceFeeAct = UpfrontServiceFeeAct.subtract(UpfrontServiceExtAct);
				 log.info("UpfrontServiceFeeAct:{},UpfrontServiceExtAct:{}",UpfrontServiceFeeAct,UpfrontServiceExtAct);
			 }
		}
		//Boolean AssertionResult = UpfrontServiceFeeAct.compareTo(ServiceFee)==0 ?true:false;
		//差值小于10 ，默认两值相等
		Boolean AssertionResult = Math.abs(UpfrontServiceFeeAct.subtract(ServiceFee).intValue())<10 ?true:false;
		loanVo.getLoanVoExt().setServiceFee(UpfrontServiceFeeAct);
		loanVo.getLoanVoExt().setServiceFeeAssertion(AssertionResult);
		loanVo.getAssertionMap().put("ServiceFeeAssertion", AssertionResult);
		log.info("getUpfrontServiceFeeAssertion:{}",loanVo);

	}
//	@Test
	public  void checkUpfrontProvisionFee(LoanVo loanVo)  {
		System.out.println("======getProvisionFeeAssertion方法被执行");
//		LoanVo loanVo = new LoanVo();
//		loanVo.setLoanNumber("CL21110816043716069152420");
//		calculation.buildLoanVo(loanVo);
		BigDecimal UpfrontProvisionFeeAct = null;
		String tenor = loanVo.getTenor().substring(0, loanVo.getTenor().length()-1);
		if(loanVo.getSecondProductCode().contains("v2")) {
			UpfrontProvisionFeeAct = new BigDecimal(loanVo.getAmount())
					.multiply(loanVo.getUpfrontProvisionFeeRate());
			log.info("UpfrontProvisionFeeAct::::{}",UpfrontProvisionFeeAct);
			Boolean AssertionResult = UpfrontProvisionFeeAct.compareTo(loanVo.getUpfrontProvisionFee())==0 ?true:false;
//			loanVo.getLoanVoExt().setUpfrontServiceFee(new BigDecimal(86800));
			
			loanVo.getLoanVoExt().setUpfrontProvisionFee(UpfrontProvisionFeeAct);
			loanVo.getLoanVoExt().setUpfrontProvisionFeeAssertion(AssertionResult);
			loanVo.getAssertionMap().put("UpfrontProvisionFeeAssertion",AssertionResult);
			log.info("getUpfrontProvisionFeeAssertion:::{}",loanVo);
		}else if(loanVo.getSecondProductCode().contains("v1")&&loanVo.getLoanNumber().startsWith("CL")){
			String adPvProportion = loanVo.getAdPvProportion();
			String[] split = adPvProportion.split(":");
			int admin = Integer.valueOf(split[0]);
			int provision = Integer.valueOf(split[1]);
			int adminAll= admin+provision;
//			BigDecimal adminFee = new BigDecimal(provision).multiply(new BigDecimal(loanVo.getAmount()))
//					.divide(new BigDecimal(adminAll), 6, BigDecimal.ROUND_HALF_UP)
//					.multiply(loanVo.getUpfrontFeeRate());
			BigDecimal upfrontfee = loanVo.getUpfrontFeeRate().multiply(new BigDecimal(loanVo.getAmount()));
			// 先向上取整
			upfrontfee = upfrontfee.setScale(-3, BigDecimal.ROUND_UP);
//			BigDecimal adminFee = new BigDecimal(loanVo.getAmount())
//					.multiply(new BigDecimal(admin)).divide(new BigDecimal(adminAll), 6, BigDecimal.ROUND_HALF_UP)
//					.multiply(loanVo.getUpfrontFeeRate()).setScale(0, BigDecimal.ROUND_HALF_UP);
			BigDecimal adminFee =upfrontfee.multiply(new BigDecimal(admin))
					.divide(new BigDecimal(adminAll), 6, BigDecimal.ROUND_HALF_UP).setScale(0, BigDecimal.ROUND_UP);
			
			BigDecimal provisionFee = upfrontfee.subtract(adminFee);
			log.info("==provision{},{},{},{},{}",provision,adminAll,loanVo.getAmount(),provisionFee,loanVo.getProvisionFee());
			boolean resultAssertion = provisionFee.compareTo(new BigDecimal(loanVo.getProvisionFee()))==0? true: false;
			loanVo.getLoanVoExt().setProvisionFee(provisionFee.longValue());
			loanVo.getLoanVoExt().setProvisionFeeAssertion(resultAssertion);
			loanVo.getAssertionMap().put("provisionFee",resultAssertion);
			log.info("getUpfrontProvisionFeeAssertion:::{}",loanVo);
			}else if(loanVo.getLoanNumber().startsWith("PL")) {
				BigDecimal provisionFeeAct = null;
				//String tenor = loanVo.getTenor().substring(0, loanVo.getTenor().length()-1);
				BigDecimal provisionFeeRate =loanVo.getProvisionFeeRate();
				long provisionFee = loanVo.getProvisionFee();
//				if(loanVo.getSecondProductCode().contains("v2")) {
				provisionFeeAct = new BigDecimal(loanVo.getAmount())
							.multiply(provisionFeeRate).multiply(new BigDecimal(tenor));
				
				 if(loanVo.getSecondProductCode().contains("long")){
						//额外的保证金
						 BigDecimal provisionFeeExtAct = provisionFeeRate.divide(new BigDecimal(30), 6,BigDecimal.ROUND_HALF_UP)
								 .multiply(new BigDecimal(loanVo.getDdmDay())).multiply(new BigDecimal(loanVo.getAmount()));
						if(loanVo.getDdmPlan().equalsIgnoreCase("A")) {
							provisionFeeAct =  provisionFeeAct.add(provisionFeeExtAct);
							log.info("计划：{}，差值：{}，额外的资金方利息{}",loanVo.getDdmPlan(),loanVo.getDdmDay(),provisionFeeAct);
						}else if(loanVo.getDdmPlan().equalsIgnoreCase("B")) {
							provisionFeeAct =  provisionFeeAct.subtract(provisionFeeExtAct);
							log.info("计划：{}，差值：{}，额外的资金方利息{}",loanVo.getDdmPlan(),loanVo.getDdmDay(),provisionFeeAct);
						}	
					}
//					 Boolean AssertionResult = UpfrontFundInterestAct.compareTo(fundInterestFee)==0 ?true:false;
					//差值小于10，默认两金额相等UpfrontFundInterestAct.compareTo(fundInterestFee)
					 Boolean AssertionResult = Math.abs(provisionFeeAct.subtract(new BigDecimal(provisionFee)).intValue())<10 ?true:false;
						loanVo.getLoanVoExt().setProvisionFee(provisionFee);
						loanVo.getLoanVoExt().setProvisionFeeAssertion(AssertionResult);
						loanVo.getAssertionMap().put("provisionFee",AssertionResult);
						log.info("getUpfrontProvisionFeeAssertion:::{}",loanVo);
			}
	}
//	@Test
	public void checkUpfrontAdmin(LoanVo loanVo) {
		System.out.println("======getupfrontAdminAssertion方法被执行");
//		LoanVo loanVo = new LoanVo();
//		loanVo.setLoanNumber("CL21110816043716069152420");
//		calculation.buildLoanVo(loanVo);
		String tenor = loanVo.getTenor().substring(0, loanVo.getTenor().length()-1);
		//总砍头费
		log.info("loanVo.getUpfrontAdmin()::{},new BigDecimal(30){},loanVo.getAmount(){},tenor{}",loanVo.getUpfrontAdmin()
				,new BigDecimal(30),loanVo.getAmount(),tenor);
		if(loanVo.getSecondProductCode().contains("v2")) {
			BigDecimal upfrontfee =null;
			BigDecimal UpfrontFundInterestAct =null;
			//UpfrontFundInterest
			UpfrontFundInterestAct = new BigDecimal(loanVo.getAmount())
					.multiply(loanVo.getUpfrontFundInterestRate()).multiply(new BigDecimal(tenor));
			if(loanVo.getSecondProductCode().contains("short")) {		
				 upfrontfee = new BigDecimal(tenor).multiply(loanVo.getUpfrontFeeRate()).divide(new BigDecimal(30), 6,BigDecimal.ROUND_HALF_UP)
						.multiply(new BigDecimal(loanVo.getAmount()));
			}else if(loanVo.getSecondProductCode().contains("long")){
				 upfrontfee = loanVo.getUpfrontFeeRate().multiply(new BigDecimal(loanVo.getAmount()));
					//额外的资金方利息
				 BigDecimal UpfrontFundInterestExtAct = loanVo.getUpfrontFundInterestRate().divide(new BigDecimal(30), 6,BigDecimal.ROUND_HALF_UP)
						 .multiply(new BigDecimal(loanVo.getDdmDay())).multiply(new BigDecimal(loanVo.getAmount()));
				if(loanVo.getDdmPlan().equalsIgnoreCase("A")) {
					UpfrontFundInterestAct =  UpfrontFundInterestAct.add(UpfrontFundInterestExtAct);
					log.info("计划：{}，差值：{}，额外的资金方利息{}",loanVo.getDdmPlan(),loanVo.getDdmDay(),UpfrontFundInterestExtAct);
				}else if(loanVo.getDdmPlan().equalsIgnoreCase("B")) {
					UpfrontFundInterestAct =  UpfrontFundInterestAct.subtract(UpfrontFundInterestExtAct);
					log.info("计划：{}，差值：{}，额外的资金方利息{}",loanVo.getDdmPlan(),loanVo.getDdmDay(),UpfrontFundInterestExtAct);
				}
				 
				//UpfrontFundInterest
			}
			//总砍头费向上取整
			upfrontfee = upfrontfee.setScale(-3, BigDecimal.ROUND_UP);
			//UpfrontServiceFee
			BigDecimal UpfrontServiceFeeAct = new BigDecimal(loanVo.getAmount())
//					.multiply(new BigDecimal(loanVo.getUpfrontFee()))
					.multiply(loanVo.getUpfrontServiceFeeRate()).multiply(new BigDecimal(tenor));
			//UpfrontProvision
			BigDecimal UpfrontProvisionFeeAct = new BigDecimal(loanVo.getAmount())
					.multiply(loanVo.getUpfrontProvisionFeeRate());
			log.info("资金方利息：{}，平台服务费：{}，保证金：{},总砍头费：{}",UpfrontFundInterestAct,UpfrontServiceFeeAct,UpfrontProvisionFeeAct,upfrontfee.intValue());
			//
			BigDecimal upfrontAdmin = upfrontfee.subtract(UpfrontFundInterestAct)
					.subtract(UpfrontServiceFeeAct).subtract(UpfrontProvisionFeeAct);
			int intValue = upfrontAdmin.setScale(0, BigDecimal.ROUND_UP).intValue();
//			int intValue2 = upfrontfee.setScale(-3, BigDecimal.ROUND_UP).intValue();
			//差值小于10，默认两金额相等差值小于10，默认两金额相等
			//Boolean AssertionResult = upfrontAdmin.setScale(0, BigDecimal.ROUND_UP).compareTo(loanVo.getUpfrontAdmin())==0 ? true: false;
			Boolean AssertionResult = Math.abs(upfrontAdmin.subtract(loanVo.getUpfrontAdmin()).intValue())<10 ?true:false;
			loanVo.getAssertionMap().put("upfrontAdmin",AssertionResult);
			loanVo.getLoanVoExt().setUpfrontAdminFee(new BigDecimal(intValue));
			loanVo.getLoanVoExt().setUpfrontFee(upfrontfee.longValue());
			loanVo.getLoanVoExt().setUpfrontAdminFeeAssertion(AssertionResult);
			log.info("getupfrontAdminAssertion:::{}",loanVo);
		}else if(loanVo.getSecondProductCode().contains("v1")) {
			String adPvProportion = loanVo.getAdPvProportion();
			String[] split = adPvProportion.split(":");
			int admin = Integer.valueOf(split[0]);
			int provision = Integer.valueOf(split[1]);
			int adminAll= admin+provision;
//			BigDecimal adminFee = new BigDecimal(loanVo.getAmount())
//					.multiply(new BigDecimal(admin)).divide(new BigDecimal(adminAll), 6, BigDecimal.ROUND_HALF_UP)
//					.multiply(loanVo.getUpfrontFeeRate()).setScale(0, BigDecimal.ROUND_HALF_UP);
			// 先向上取整
			BigDecimal upfrontfee = loanVo.getUpfrontFeeRate().multiply(new BigDecimal(loanVo.getAmount()));
			upfrontfee = upfrontfee.setScale(-3, BigDecimal.ROUND_UP);
			BigDecimal adminFee =upfrontfee.multiply(new BigDecimal(admin))
					.divide(new BigDecimal(adminAll), 6, BigDecimal.ROUND_HALF_UP).setScale(0, BigDecimal.ROUND_UP);
			
			log.info("==adminFee{}",adminFee);
			// boolean resultAssertion = adminFee.compareTo(new BigDecimal(loanVo.getAdminfee()))==0? true: false;
			 boolean resultAssertion = adminFee.compareTo(new BigDecimal(loanVo.getAdminfee()))==0? true: false;
			loanVo.getLoanVoExt().setAdminFee(adminFee.longValue());
			loanVo.getLoanVoExt().setAdminFeeAssertion(resultAssertion);
			loanVo.getAssertionMap().put("AdminFee",resultAssertion);
			log.info("getupfrontAdminAssertion:::{}",loanVo);
		}


	}
//	@Test
	public void checkUpfrontFee(LoanVo loanVo) {
		System.out.println("======getUpfrontFeeAssertion方法被执行");
//		LoanVo loanVo = new LoanVo();
//		loanVo.setLoanNumber("CL21110816043716069152420");
//		calculation.buildLoanVo(loanVo);
		String tenor = loanVo.getTenor().substring(0, loanVo.getTenor().length()-1);
		//总砍头费
		BigDecimal upfrontfee = null;
		log.info("loanVo.getUpfrontAdmin()::{},new BigDecimal(30){},loanVo.getAmount(){},tenor{}",loanVo.getUpfrontAdmin()
				,new BigDecimal(30),loanVo.getAmount(),tenor);
		//V2短期，砍头费需根据天数计算
		if(loanVo.getSecondProductCode().contains("v2")&&loanVo.getSecondProductCode().contains("short")) {
			 upfrontfee = loanVo.getUpfrontFeeRate().multiply(new BigDecimal(tenor)).divide(new BigDecimal(30), 6,BigDecimal.ROUND_HALF_UP)
						.multiply(new BigDecimal(loanVo.getAmount()));
		}else {
			//V1,长期V1,V2
			upfrontfee = loanVo.getUpfrontFeeRate().multiply(new BigDecimal(loanVo.getAmount()));
		}
		//总砍头费向上取整
		upfrontfee = upfrontfee.setScale(-3, BigDecimal.ROUND_UP);
		log.info("upfrontfee{}",upfrontfee);
//		boolean resultAssertion = Long.compare(upfrontfee.longValue(), loanVo.getUpfrontFee()) ==0 ? true: false;
		boolean resultAssertion = ((Long)upfrontfee.longValue()).compareTo( loanVo.getUpfrontFee()) ==0 ? true: false;
		loanVo.getLoanVoExt().setUpfrontFee(upfrontfee.longValue());
		loanVo.getLoanVoExt().setUpfrontFeeAssertion(resultAssertion);
		loanVo.getAssertionMap().put("UpfrontFeeAssertion",resultAssertion);
		log.info("getUpfrontFeeAssertion{}",loanVo);

	}
	/**
	 * 检查放款中的砍头费
	 * @param loanVo
	 */
	public void checkDisbUpfrontFee(LoanVo loanVo) {
		//v1产品检查：总的砍头费   管理费 管理费税费 保证金  砍头费中 进位费 进位费税费
		//v1产品检查：总的砍头费   管理费 管理费税费 保证金  砍头费中 进位费 进位费税费   资金方利息 资金方税费  服务费  服务费费税费 
		//CL 产品校验放款砍头费
		if(loanVo.getLoanNumber().startsWith("CL")) {
			//砍头费
			BigDecimal upfrontFee = loanVo.getUpfrontFeeVO().getUpfrontFee();
			BigDecimal calUpfrontFee = loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getUpfrontFee();
			Boolean UpfrontFeeAssertion = upfrontFee.compareTo(calUpfrontFee)==0?true:false;
			loanVo.getAssertionMap().put("UpfrontFeeAssertion",UpfrontFeeAssertion);
			//
			if(loanVo.getSecondProductCode().contains("v1")) {
				//管理费以及管理费税费
				BigDecimal upfrontAdminfee = loanVo.getUpfrontFeeVO().getUpfrontAdminfee();
				BigDecimal upfrontAdminfeeVat = loanVo.getUpfrontFeeVO().getUpfrontAdminfeeVat();
				
				BigDecimal calUpfrontAdminfee = loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getUpfrontAdminfee();
				BigDecimal calUpfrontAdminfeeVat = loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getUpfrontAdminfeeVat();
				boolean upfrontAdminfeeAssertion = upfrontAdminfee.compareTo(calUpfrontAdminfee)==0?true:false;
				boolean upfrontAdminfeeVatAssertion = upfrontAdminfeeVat.compareTo(calUpfrontAdminfeeVat)==0?true:false;
				loanVo.getAssertionMap().put("upfrontAdminfeeAssertion",upfrontAdminfeeAssertion);
				loanVo.getAssertionMap().put("upfrontAdminfeeVatAssertion",upfrontAdminfeeVatAssertion);

				//保证金
				BigDecimal upfrontProvisionFee = loanVo.getUpfrontFeeVO().getUpfrontProvisionFee();
				BigDecimal calUpfrontProvisionFee = loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getUpfrontProvisionFee();
				boolean upfrontProvisionFeeAssertion = upfrontProvisionFee.compareTo(calUpfrontProvisionFee)==0?true:false;
				loanVo.getAssertionMap().put("upfrontProvisionFeeAssertion",upfrontProvisionFeeAssertion);
				//进位费 以及税费
				BigDecimal upfrontCarryFee = loanVo.getUpfrontFeeVO().getUpfrontCarryFee();
				BigDecimal upfrontCarryFeeVat = loanVo.getUpfrontFeeVO().getUpfrontCarryFeeVat();
				
				BigDecimal calUpfrontCarryFee = loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getUpfrontCarryFee();
				BigDecimal calUpfrontCarryFeeVat = loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getUpfrontCarryFeeVat();
				
				boolean upfrontCarryFeeAssertion = upfrontCarryFee.compareTo(calUpfrontCarryFee)==0?true:false;
				boolean upfrontCarryFeeVatAssertion = upfrontCarryFeeVat.compareTo(calUpfrontCarryFeeVat)==0?true:false;
				loanVo.getAssertionMap().put("upfrontCarryFeeAssertion",upfrontCarryFeeAssertion);
				loanVo.getAssertionMap().put("upfrontCarryFeeVatAssertion",upfrontCarryFeeVatAssertion);
			}else if(loanVo.getSecondProductCode().contains("v2")) {
				//管理费以及税费
				BigDecimal upfrontAdminFee = loanVo.getUpfrontFeeVO().getExtendFee().getUpfrontAdminFee();
				BigDecimal upfrontAdminFeeVAT = loanVo.getUpfrontFeeVO().getExtendFee().getUpfrontAdminFeeVAT();

				BigDecimal calUpfrontAdminFee = loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getExtendFee().getUpfrontAdminFee();
				BigDecimal calUpfrontAdminFeeVAT = loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getExtendFee().getUpfrontAdminFeeVAT();
				boolean upfrontAdminfeeAssertion = compareBigDecimal(upfrontAdminFee, calUpfrontAdminFee);
				boolean upfrontAdminfeeVatAssertion =  compareBigDecimal(upfrontAdminFeeVAT, calUpfrontAdminFeeVAT);
				loanVo.getAssertionMap().put("upfrontAdminfeeAssertion",upfrontAdminfeeAssertion);
				loanVo.getAssertionMap().put("upfrontAdminfeeVatAssertion",upfrontAdminfeeVatAssertion);
				//保证金
				BigDecimal upfrontProvisionFee = loanVo.getUpfrontFeeVO().getExtendFee().getUpfrontProvisionFee();
				BigDecimal calUpfrontProvisionFee = loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getExtendFee().getUpfrontProvisionFee();
				boolean upfrontProvisionFeeAssertion =  compareBigDecimal(upfrontProvisionFee, calUpfrontProvisionFee);
				loanVo.getAssertionMap().put("upfrontProvisionFeeAssertion",upfrontProvisionFeeAssertion);

				//进位费 以及税费
				
				 BigDecimal upfrontCarryFee = loanVo.getUpfrontFeeVO().getExtendFee().getUpfrontCarryFee();
				 BigDecimal upfrontCarryFeeVAT = loanVo.getUpfrontFeeVO().getExtendFee().getUpfrontCarryFeeVAT();
				 
				 BigDecimal calUpfrontCarryFee = loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getExtendFee().getUpfrontCarryFee();
				 BigDecimal calUupfrontCarryFeeVat = loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getExtendFee().getUpfrontCarryFeeVAT();
				 
				 boolean upfrontCarryFeeAssertion = compareBigDecimal(upfrontCarryFee, calUpfrontCarryFee);
				 boolean upfrontCarryFeeVATAssertion = compareBigDecimal(upfrontCarryFeeVAT, calUupfrontCarryFeeVat);
				 loanVo.getAssertionMap().put("upfrontCarryFeeAssertion",upfrontCarryFeeAssertion);
				 loanVo.getAssertionMap().put("upfrontCarryFeeVATAssertion",upfrontCarryFeeVATAssertion);
				//资金方利息，资金方税费
				 BigDecimal upfrontFundInterestFee = loanVo.getUpfrontFeeVO().getExtendFee().getUpfrontFundInterestFee();
				 BigDecimal upFundInterestFeeWHT = loanVo.getUpfrontFeeVO().getExtendFee().getUpFundInterestFeeWHT();
				 
				 BigDecimal calUpfrontFundInterestFee = loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getExtendFee().getUpfrontFundInterestFee();
				 BigDecimal calUpFundInterestFeeWHT = loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getExtendFee().getUpFundInterestFeeWHT();
				
				 boolean upfrontFundInterestFeeAssertion = compareBigDecimal(upfrontFundInterestFee, calUpfrontFundInterestFee);
				 boolean upFundInterestFeeWHTAssertion = compareBigDecimal(upFundInterestFeeWHT, calUpFundInterestFeeWHT);
				 loanVo.getAssertionMap().put("upfrontFundInterestFeeAssertion",upfrontFundInterestFeeAssertion);
				 loanVo.getAssertionMap().put("upFundInterestFeeWHTAssertion",upFundInterestFeeWHTAssertion);
				//平台服务费 平台服务费税费
				 BigDecimal upfrontServiceFee = loanVo.getUpfrontFeeVO().getExtendFee().getUpfrontServiceFee();
				 BigDecimal upfrontServiceFeeVAT = loanVo.getUpfrontFeeVO().getExtendFee().getUpfrontServiceFeeVAT();
				 
				 BigDecimal calUpfrontServiceFee = loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getExtendFee().getUpfrontServiceFee();
				 BigDecimal calUpfrontServiceFeeVAT = loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getExtendFee().getUpfrontServiceFeeVAT();
				 boolean upfrontServiceFeeAssertion = compareBigDecimal(upfrontServiceFee, calUpfrontServiceFee);
				 boolean upfrontServiceFeeVATAssertion = compareBigDecimal(upfrontServiceFeeVAT, calUpfrontServiceFeeVAT);

				 loanVo.getAssertionMap().put("upfrontServiceFeeAssertion",upfrontServiceFeeAssertion);
				 loanVo.getAssertionMap().put("upfrontServiceFeeVATAssertion",upfrontServiceFeeVATAssertion);
			
			}
		}
		
	}
	//比较两个数
	public boolean compareBigDecimal(BigDecimal bigDecimal,BigDecimal bigDecimal2) {
		
		return bigDecimal.compareTo(bigDecimal2)==0?true:false;
	}
	public void checkDues(LoanVo loanVo) {
		//查询dues  
		QueryWrapper<Dues> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("loan_id", loanVo.getLoanNumber());
		List<Dues> duesList = duesMapper.selectList(queryWrapper);
		UpfrontDuesVo calUpfrontDuesVo = loanVo.getCalUpfrontDuesVo();
		RepayDues normalDues = calUpfrontDuesVo.getDuesVo().getNormalDues();
		RepayDues ddmDues = calUpfrontDuesVo.getDuesVo().getDDMDues();
		List<String> duedateList = loanVo.getCalUpfrontDuesVo().getDuesVo().getDuedateList();
		HashMap<String, Boolean> assertionMap = loanVo.getAssertionMap();

		//如果是短期只需校验normalDues,长期校验normalDues(取第二期校验，如果是B计划取 第一校验),还需校验dDMDues
		if(loanVo.getSecondProductCode().contains("short")) {
			boolean firstDuedateAssertion = (localDateToString(duesList).equalsIgnoreCase(normalDues.getDueDate()))== true ? true:false;
			 assertionMap.put("firstDuedateAssertion", firstDuedateAssertion);
			 checkDateAndFee(duesList, normalDues, assertionMap,"normal");
		}else if(loanVo.getSecondProductCode().contains("long")) {
			if(calUpfrontDuesVo.getDdmRelation().getPlanType().equalsIgnoreCase("B")&&loanVo.getSecondProductCode().contains("v2")) {
				 //正常期的dues  第一期dues
				List<Dues> firstDues = duesList.stream().filter(x->x.getDueIndex()==1).collect(Collectors.toList());
				boolean firstDuedateAssertion = (localDateToString(firstDues).equalsIgnoreCase(normalDues.getDueDate()))== true ? true:false;
				 assertionMap.put("firstDuedateAssertionB", firstDuedateAssertion);
				 checkDateAndFee(firstDues, normalDues, assertionMap,"normal");
				//DDM期数   最后一期
				 List<Dues> lastDues = duesList.stream().filter(x->x.getDueIndex()==duedateList.size()).collect(Collectors.toList());
				 boolean lastDuedateAssertion = (localDateToString(lastDues).equalsIgnoreCase(ddmDues.getDueDate()))== true ? true:false;
				assertionMap.put("lastDuedateAssertionB", lastDuedateAssertion);
				checkDateAndFee(lastDues, ddmDues, assertionMap,"ddm");
			}else if(calUpfrontDuesVo.getDdmRelation().getPlanType().equalsIgnoreCase("B")&&loanVo.getSecondProductCode().contains("v1")) {
				 //正常期的dues  第一期dues
				List<Dues> firstDues = duesList.stream().filter(x->x.getDueIndex()==2).collect(Collectors.toList());
				boolean firstDuedateAssertion = (localDateToString(firstDues).equalsIgnoreCase(normalDues.getDueDate()))== true ? true:false;
				 assertionMap.put("firstDuedateAssertionB", firstDuedateAssertion);
				 checkDateAndFee(firstDues, normalDues, assertionMap,"normal");
				//DDM期数   最后一期
				 List<Dues> lastDues = duesList.stream().filter(x->x.getDueIndex()==1).collect(Collectors.toList());
				 boolean lastDuedateAssertion = (localDateToString(lastDues).equalsIgnoreCase(ddmDues.getDueDate()))== true ? true:false;
				assertionMap.put("lastDuedateAssertionB", lastDuedateAssertion);
				checkDateAndFee(lastDues, ddmDues, assertionMap,"ddm");
			}else if(calUpfrontDuesVo.getDdmRelation().getPlanType().equalsIgnoreCase("A")) {
				//ddm期
				List<Dues> firstDues = duesList.stream().filter(x->x.getDueIndex()==1).collect(Collectors.toList());
				boolean firstDuedateAssertion = (duedateList.get(0).equalsIgnoreCase(ddmDues.getDueDate()))== true ? true:false;
				 assertionMap.put("firstDuedateAssertionA", firstDuedateAssertion);
				 checkDateAndFee(firstDues, ddmDues, assertionMap,"ddm");
				//第二期
					List<Dues> secondDues = duesList.stream().filter(x->x.getDueIndex()==2).collect(Collectors.toList());

				 boolean lastDuedateAssertion = (localDateToString(secondDues).equalsIgnoreCase(normalDues.getDueDate()))== true ? true:false;
				assertionMap.put("normalDuedateAssertionA", lastDuedateAssertion);
				checkDateAndFee(secondDues, normalDues, assertionMap,"normal");
			}
		}
		
	}
	public String localDateToString(List<Dues> firstDues) {
		LocalDate dueDate = firstDues.get(0).getDueDate();
		DateTimeFormatter ofPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		return  dueDate.format(ofPattern);
	}
	public void checkDateAndFee(List<Dues> duesList, RepayDues repayDues, HashMap<String, Boolean> assertionMap,String normalOrDdm) {
		for (Dues dues: duesList) {
			 String dueType = dues.getDueType();
			 checkDuesFee(repayDues, assertionMap, dues, dueType,normalOrDdm);
		}
	}
	public void checkDuesFee(RepayDues repayDues, HashMap<String, Boolean> assertionMap, Dues dues, String dueType,String normalOrDdm) {
		if(normalOrDdm.equalsIgnoreCase("normal")) {
			if(dueType.equalsIgnoreCase("Fund_Interest")) {
				 boolean duesFundInterestAssertion  = dues.getAmount().compareTo(repayDues.getFundInterest().longValue())==0? true:false;
				 assertionMap.put("duesFundInterestAssertion", duesFundInterestAssertion);
			 }else if(dueType.equalsIgnoreCase("Service_Fee")) {
				 boolean duesServiceFeeAssertion  = dues.getAmount().compareTo(repayDues.getServiceFee().longValue())==0? true:false;
				 assertionMap.put("duesServiceFeeAssertion", duesServiceFeeAssertion);
			 }else if(dueType.equalsIgnoreCase("Service_Fee_VAT")) {
				 boolean duesServiceFeeVATAssertion  = dues.getAmount().compareTo(repayDues.getServiceFeeVAT().longValue())==0? true:false;
				 assertionMap.put("duesServiceFeeVATAssertion", duesServiceFeeVATAssertion);
			 }else if(dueType.equalsIgnoreCase("Principal")) {
				 boolean duesPrincipalAssertion  = dues.getAmount().compareTo(repayDues.getPrincipal().longValue())==0? true:false;
				 assertionMap.put("duesPrincipalAssertion", duesPrincipalAssertion);
			 }else if(dueType.equalsIgnoreCase("Carrying_Fee_VAT")) {
				 boolean duesCarryingFeeVATssertion  = dues.getAmount().compareTo(repayDues.getCarryingFeeVAT().longValue())==0? true:false;
				 assertionMap.put("duesPrincipalVATAssertion", duesCarryingFeeVATssertion);
			 }else if(dueType.equalsIgnoreCase("Carrying_Fee")) {
				 boolean duesCarryingFeessertion  = dues.getAmount().compareTo(repayDues.getCarryingFee().longValue())==0? true:false;
				 assertionMap.put("duesCarryingFeessertion", duesCarryingFeessertion);		 
			 }else if(dueType.equalsIgnoreCase("Provision_fee")) {
				 boolean duesProvisionfeessertion  = dues.getAmount().compareTo(repayDues.getProvisionFee().longValue())==0? true:false;
				 assertionMap.put("duesProvisionfeessertion", duesProvisionfeessertion);	  
			 }else if(dueType.equalsIgnoreCase("Admin_Fee")) {
				 boolean duesAdminFeessertion  = dues.getAmount().compareTo(repayDues.getAdminFee().longValue())==0? true:false;
				 assertionMap.put("duesAdminFeessertion", duesAdminFeessertion);	  
			 }else if(dueType.equalsIgnoreCase("Admin_Fee_VAT")) {
				 boolean duesAdminFeeVATssertion  = dues.getAmount().compareTo(repayDues.getAdminFee().longValue())==0? true:false;
				 assertionMap.put("duesAdminFeeVATssertion", duesAdminFeeVATssertion);	  
			 }
		}else if(normalOrDdm.equalsIgnoreCase("ddm")) {
			if(dueType.equalsIgnoreCase("Fund_Interest")) {
				 boolean duesFundInterestAssertion  = dues.getAmount().compareTo(repayDues.getFundInterest().longValue())==0? true:false;
				 assertionMap.put("ddmduesFundInterestAssertion", duesFundInterestAssertion);
			 }else if(dueType.equalsIgnoreCase("Service_Fee")) {
				 boolean duesServiceFeeAssertion  = dues.getAmount().compareTo(repayDues.getServiceFee().longValue())==0? true:false;
				 assertionMap.put("ddmduesServiceFeeAssertion", duesServiceFeeAssertion);
			 }else if(dueType.equalsIgnoreCase("Service_Fee_VAT")) {
				 boolean duesServiceFeeVATAssertion  = dues.getAmount().compareTo(repayDues.getServiceFeeVAT().longValue())==0? true:false;
				 assertionMap.put("ddmduesServiceFeeVATAssertion", duesServiceFeeVATAssertion);
			 }else if(dueType.equalsIgnoreCase("Principal")) {
				 boolean duesPrincipalAssertion  = dues.getAmount().compareTo(repayDues.getPrincipal().longValue())==0? true:false;
				 assertionMap.put("ddmduesPrincipalAssertion", duesPrincipalAssertion);
			 }else if(dueType.equalsIgnoreCase("Carrying_Fee_VAT")) {
				 boolean duesCarryingFeeVATssertion  = dues.getAmount().compareTo(repayDues.getCarryingFeeVAT().longValue())==0? true:false;
				 assertionMap.put("ddmduesCarryingFeeVATssertion", duesCarryingFeeVATssertion);
			 }else if(dueType.equalsIgnoreCase("Carrying_Fee")) {
				 boolean duesCarryingFeessertion  = dues.getAmount().compareTo(repayDues.getCarryingFee().longValue())==0? true:false;
				 assertionMap.put("ddmduesCarryingFeessertion", duesCarryingFeessertion);		 
			 }else if(dueType.equalsIgnoreCase("Provision_fee")) {
				 boolean duesProvisionfeessertion  = dues.getAmount().compareTo(repayDues.getProvisionFee().longValue())==0? true:false;
				 assertionMap.put("ddmduesProvisionfeessertion", duesProvisionfeessertion);	  
			 }else if(dueType.equalsIgnoreCase("Admin_Fee")) {
				 boolean duesAdminFeessertion  = dues.getAmount().compareTo(repayDues.getAdminFee().longValue())==0? true:false;
				 assertionMap.put("ddmduesAdminFeessertion", duesAdminFeessertion);	  
			 }else if(dueType.equalsIgnoreCase("Admin_Fee_VAT")) {
				 boolean duesAdminFeeVATssertion  = dues.getAmount().compareTo(repayDues.getAdminFee().longValue())==0? true:false;
				 assertionMap.put("ddmduesAdminFeeVATssertion", duesAdminFeeVATssertion);	  
			 }
		}
	}
	public  void checkTransferRecord(LoanVo loanVo) {
		System.out.println("======getTransferRecordAssertion方法被执行");
		log.info("loan:{}", loanVo);
		//资金方账号
		FundAccount funderAccount = null;
		//BNI放款账号
		FundAccount bniAccount = null;
		//adwa收款账号
		FundAccount adwaAccount = null;
		//TAX_ACCOUNT账号
		FundAccount taxAccount = null;
		//财务发起的转账记录
		List<FinanceTrading> financeTradingList = null;
		//网关实际转账记录
		List<GatewayTask> gatewayTaskList = null;
		//充值，准给adwa记录
		List<GatewayTask> transferToAdwa = null;
		////转账给用户
		List<GatewayTask> transferToUser= null;
		//V1短期放款涉及充值，转管理费，给客户转钱
		//先校验转出转入账号
		funderAccount = funderAccount(loanVo);
		bniAccount = bniAccount();
		adwaAccount = adwaAccount();
		taxAccount = taxAccount();
		log.info("funderAccount:{},bniAccount:{},adwaAccount:{}", funderAccount,bniAccount,adwaAccount);
		//校验充值记录,以及网关实际转账记录
		financeTradingList = financeTradingList(loanVo);
		gatewayTaskList = gatewayTaskList(loanVo);
		//充值，准给adwa记录
		 transferToAdwa = gatewayTaskList.stream().filter(x->x.getType()==6)
				.collect(Collectors.toList());
		//转账给用户
		transferToUser = gatewayTaskList.stream().filter(x->x.getType()==3)
				.collect(Collectors.toList());				
		log.info("financeTradingList:{},和gatewayTaskList{},transferToAdwa{},transferToUser{}"
				,financeTradingList,gatewayTaskList,transferToAdwa,transferToUser);
		//转账类型校验
		boolean transferTypeFlag =false;
		//发起充值转入转出账号
		boolean rechargeTransferAccountFlag = false;
		//发起非充值转账转入转出账号
		boolean transferAccountFlag = false;
		//实际充值转入转出账号
		boolean rechargeTransferAccountGatewayFlag = false;
		//实际非充值转账转入转出账号
		boolean transferAccountGatewayFlag = false;
		//发起转账记录
		List<Integer> typeList =  new ArrayList<>();
		financeTradingList.forEach(x->{
			x.getType();
			typeList.add(x.getType());
		});
		if(loanVo.getLoanNumber().contains("CL")) {
			if(loanVo.getSecondProductCode().contains("v1")) {
				//检查财务发起的转账类型
				if(typeList.contains(4)&&typeList.contains(1)&&typeList.contains(35)) {
					transferTypeFlag = true;
					log.info("转账类型正确:{}",typeList);
					loanVo.getTransferVo().setTransferTypeList(typeList);
					loanVo.getTransferVo().setTransferTypeAssertion(true);
					loanVo.getAssertionMap().put("TransferTypeAssertion", true);
				}else {
					loanVo.getTransferVo().setTransferTypeList(typeList);
					loanVo.getTransferVo().setTransferTypeAssertion(false);
					loanVo.getAssertionMap().put("TransferTypeAssertion", false);
				}
				//检查财务发起的转账账号和金额
				for (FinanceTrading item : financeTradingList) {
							if(item.getType()==4) {
								//财务发起的充值账号以及金额
								rechargeTransferAccountFlag = checkRechargeAccountAndAmount(loanVo, funderAccount, bniAccount,
										rechargeTransferAccountFlag, item);
								
							}else if(item.getType()==1)  {
								transferAccountFlag = checkNorechergeAccountAndAmount(funderAccount,bniAccount,taxAccount, adwaAccount,
										transferAccountFlag, item,loanVo);
							}else if(item.getType()==35)  {
								transferAccountFlag = checkNorechergeAccountAndAmount(funderAccount,bniAccount,taxAccount, adwaAccount,
										transferAccountFlag, item,loanVo);
							}
					if(!rechargeTransferAccountFlag&&!transferAccountFlag) {
						log.info("转入转出账号错误");	
						log.info("财务发起充值记录转入转出账号,转出账号1:{},转出账号2:{},转入账号1:{},转入账号2:{}"
								,item.getOutAccountNo(),funderAccount.getAccountNo(),item.getInAccountNo(),bniAccount.getAccountNo());			
						log.info("财务发起非充值记录转入转出账号，转出账号1:{},转出账号2:{},转入账号1:{},转入账号2:{}"
								,item.getOutAccountNo(),bniAccount.getAccountNo(),item.getInAccountNo(),adwaAccount.getAccountNo());	
						break;
					}
				}		
			}else if(loanVo.getSecondProductCode().contains("v2")) {
				//检查财务发起的转账类型
				if(typeList.contains(4)&&typeList.contains(1)&&typeList.contains(9)
						&&typeList.contains(10)&&typeList.contains(13)&&typeList.contains(35)) {
					transferTypeFlag = true;
					log.info("转账类型正确:{}",typeList);
					loanVo.getTransferVo().setTransferTypeList(typeList);
					loanVo.getTransferVo().setTransferTypeAssertion(true);
					loanVo.getAssertionMap().put("TransferTypeAssertion", true);
				}else {
					loanVo.getTransferVo().setTransferTypeList(typeList);
					loanVo.getTransferVo().setTransferTypeAssertion(false);
					loanVo.getAssertionMap().put("TransferTypeAssertion", false);
				}
				//检查财务发起的转账账号和金额
				for (FinanceTrading item : financeTradingList) {
							if(item.getType()==4) {
								//财务发起的充值账号以及金额
								rechargeTransferAccountFlag = checkRechargeAccountAndAmount(loanVo, funderAccount, bniAccount,
										rechargeTransferAccountFlag, item);
								
							}else if(item.getType()==1)  {
								transferAccountFlag = checkNorechergeAccountAndAmount(funderAccount,bniAccount,taxAccount, adwaAccount,
										transferAccountFlag, item,loanVo);
							}else if(item.getType()==9)  {
								transferAccountFlag = checkNorechergeAccountAndAmount(funderAccount,bniAccount,taxAccount,  adwaAccount,
										transferAccountFlag, item,loanVo);
							}else if(item.getType()==10)  {
								transferAccountFlag = checkNorechergeAccountAndAmount(funderAccount,bniAccount, taxAccount, adwaAccount,
										transferAccountFlag, item,loanVo);
							}else if(item.getType()==13)  {
								if(loanVo.getUpfrontServiceFee().intValue()>0) {		
									transferAccountFlag = checkNorechergeAccountAndAmount(funderAccount,bniAccount,taxAccount,  adwaAccount,
											transferAccountFlag, item,loanVo);
								}
							}else if(item.getType()==35)  {
								if(loanVo.getUpfrontServiceFee().intValue()>0) {		
									transferAccountFlag = checkNorechergeAccountAndAmount(funderAccount,bniAccount,taxAccount,  adwaAccount,
											transferAccountFlag, item,loanVo);
								}
							}
					if(!rechargeTransferAccountFlag&&!transferAccountFlag) {
						log.info("转入转出账号错误");	
						log.info("财务发起充值记录转入转出账号,转出账号1:{},转出账号2:{},转入账号1:{},转入账号2:{}"
								,item.getOutAccountNo(),funderAccount.getAccountNo(),item.getInAccountNo(),bniAccount.getAccountNo());			
						log.info("财务发起非充值记录转入转出账号，转出账号1:{},转出账号2:{},转入账号1:{},转入账号2:{}"
								,item.getOutAccountNo(),bniAccount.getAccountNo(),item.getInAccountNo(),adwaAccount.getAccountNo());	
						break;
					}
				}		
			}
		}
		//检查实际转账记录是否和发起的记录相同
		for (FinanceTrading item : financeTradingList) {
			for (GatewayTask gatewayTaskdetail : transferToAdwa) {
				if(gatewayTaskdetail.getExtendId().equals(item.getId().toString())) {
					//校验实际转账账号和转账金额
					String json = gatewayTaskdetail.getParams();
					if(item.getType()==4) {
						log.info("转账类型{}",item.getType().toString());
						//检查实际转入转出账号
						if(item.getOutAccountNo().equals(JSONPath.read(json, "$.debitAccountNo"))
								&&item.getInAccountNo().equals(JSONPath.read(json, "$.creditAccountNo"))) {
							rechargeTransferAccountGatewayFlag = true;
							log.info("实际充值记录转入转出账号正确,转出账号1:{},转出账号2:{},转入账号1:{},转入账号2:{}"
									,item.getOutAccountNo(),JSONPath.read(json, "$.debitAccountNo"),item.getInAccountNo(),JSONPath.read(json, "$.creditAccountNo"));	
							loanVo.getTransferVo().setActTransferAccount4Assersion(true);
							loanVo.getAssertionMap().put("ActTransferAccount4Assersion",true);

						}else {
							loanVo.getTransferVo().setActTransferAccount4Assersion(false);
							loanVo.getAssertionMap().put("ActTransferAccount4Assersion",false);
						}
					}else  {
						log.info("转账类型:{}",item.getType().toString());
						if(!item.getOutAccountNo().equals(JSONPath.read(json, "$.debitAccountNo"))||
								!item.getInAccountNo().equals(JSONPath.read(json, "$.creditAccountNo"))) {
							transferAccountGatewayFlag = false;
							log.info("实际非充值记录转入转出账号错误,转出账号1:{},转出账号2:{},转入账号1:{},转入账号2:{}"
									,item.getOutAccountNo(),JSONPath.read(json, "$.debitAccountNo"),item.getInAccountNo(),JSONPath.read(json, "$.creditAccountNo"));
							if(item.getType()==1) {
								loanVo.getTransferVo().setActTransferAccount1Assersion(false);
								loanVo.getAssertionMap().put("ActTransferAccount1Assersion",false);
							}else if(item.getType()==9) {
								loanVo.getTransferVo().setActTransferAccount9Assersion(false);
								loanVo.getAssertionMap().put("ActTransferAccount9Assersion",false);
							}else if(item.getType()==10) {
								loanVo.getTransferVo().setActTransferAccount10Assersion(false);
								loanVo.getAssertionMap().put("ActTransferAccount10Assersion",false);
							}else if(item.getType()==13) {
								loanVo.getTransferVo().setActTransferAccount13Assersion(false);
								loanVo.getAssertionMap().put("ActTransferAccount13Assersion",false);
							}else if(item.getType()==35) {
								loanVo.getTransferVo().setActTransferAccount13Assersion(false);
								loanVo.getAssertionMap().put("ActTransferAccount35Assersion",false);
							}
							break;
						}else {
							transferAccountGatewayFlag = true;
							log.info("实际充值记录转入转出账号正确");	
							log.info("实际非充值记录转入转出账号错误,转出账号1:{},转出账号2:{},转入账号1:{},转入账号2:{}"
									,item.getOutAccountNo(),JSONPath.read(json, "$.debitAccountNo"),item.getInAccountNo(),JSONPath.read(json, "$.creditAccountNo"));	
							if(item.getType()==1) {
								loanVo.getTransferVo().setActTransferAccount1Assersion(true);
								loanVo.getAssertionMap().put("ActTransferAccount1Assersion",true);
							}else if(item.getType()==9) {
								loanVo.getTransferVo().setActTransferAccount9Assersion(true);
								loanVo.getAssertionMap().put("ActTransferAccount9Assersion",true);
							}else if(item.getType()==10) {
								loanVo.getTransferVo().setActTransferAccount10Assersion(true);
								loanVo.getAssertionMap().put("ActTransferAccount10Assersion",true);
							}else if(item.getType()==13) {
								loanVo.getTransferVo().setActTransferAccount13Assersion(true);
								loanVo.getAssertionMap().put("ActTransferAccount13Assersion",true);
							}
						}
					}
					//校验转账金额展示
					int expectedAmount = (int) JSONPath.read(json, "$.amount");
					Boolean amountFlag = ((Integer)expectedAmount).compareTo(item.getAmount().intValue())==0? true:false;

						log.info("转账金额正确,实际发起转账金额:{},应该转账金额:{}",item.getAmount(),expectedAmount);
						if(item.getType()==1) {
							loanVo.getTransferVo().setActTransferAmount1Assersion(amountFlag);
							loanVo.getAssertionMap().put("ActTransferAmount1Assersion",amountFlag);
						}else if(item.getType()==9) {
							loanVo.getTransferVo().setActTransferAmount9Assersion(amountFlag);
							loanVo.getAssertionMap().put("ActTransferAmount9Assersion",amountFlag);
						}else if(item.getType()==10) {
							loanVo.getTransferVo().setActTransferAmount10Assersion(amountFlag);
							loanVo.getAssertionMap().put("ActTransferAmount10Assersion",amountFlag);
						}else if(item.getType()==13) {
							loanVo.getTransferVo().setActTransferAmount13Assersion(amountFlag);
							loanVo.getAssertionMap().put("ActTransferAmount13Assersion",amountFlag);
						}else if(item.getType()==4) {
							loanVo.getTransferVo().setActTransferAmount4Assersion(amountFlag);
							loanVo.getAssertionMap().put("ActTransferAmount4Assersion",amountFlag);
						}else if(item.getType()==35) {
							loanVo.getTransferVo().setActTransferAmount4Assersion(amountFlag);
							loanVo.getAssertionMap().put("ActTransferAmount35Assersion",amountFlag);
						}
					
					
				}
			}
			if(!rechargeTransferAccountGatewayFlag&&!transferAccountGatewayFlag) {
				log.info("转入转出账号错误");	
				break;
			}
		}
		//检查给用户的转账账户和金额
		for(GatewayTask gatewayTask :transferToUser) {
			checkGatewayAccountAndAmount(loanVo, bniAccount, gatewayTask);
		}

	}
	//非充值记录的转入转出账号
	public boolean checkNorechergeAccountAndAmount(FundAccount funderAccount,FundAccount bniAccount,FundAccount taxAccount, FundAccount adwaAccount,
			boolean transferAccountFlag, FinanceTrading item,LoanVo loanVo) {
		log.info("转账类型:{}",item.getType().toString());
		if(item.getType()!=9&& item.getType()!=35) {
			if(item.getOutAccountNo().equals(bniAccount.getAccountNo())&&
					item.getInAccountNo().equals(adwaAccount.getAccountNo())) {
				transferAccountFlag = true;
				log.info("财务发起非充值记录转入转出账号正确，转出账号1:{},转出账号2:{},转入账号1:{},转入账号2:{}"
						,item.getOutAccountNo(),bniAccount.getAccountNo(),item.getInAccountNo(),adwaAccount.getAccountNo());
				if(item.getType()==1) {
					loanVo.getTransferVo().setTransferAccount1Assersion(true);
					loanVo.getAssertionMap().put("TransferAccount1Assersion", true);
				}else if(item.getType()==9) {
					loanVo.getTransferVo().setTransferAccount9Assersion(true);
					loanVo.getAssertionMap().put("TransferAccount9Assersion", true);
				}else if(item.getType()==10) {
					loanVo.getTransferVo().setTransferAccount10Assersion(true);
					loanVo.getAssertionMap().put("TransferAccount10Assersion", true);
				}else if(item.getType()==13) {
					loanVo.getTransferVo().setTransferAccount13Assersion(true);
					loanVo.getAssertionMap().put("TransferAccount13Assersion", true);
				}
			}else {
				log.info("财务发起非充值记录转入转出账号错误，转出账号1:{},转出账号2:{},转入账号1:{},转入账号2:{}"
						,item.getOutAccountNo(),bniAccount.getAccountNo(),item.getInAccountNo(),adwaAccount.getAccountNo());
				if(item.getType()==1) {
					loanVo.getTransferVo().setTransferAccount1Assersion(false);
					loanVo.getAssertionMap().put("TransferAccount1Assersion", false);
				}else if(item.getType()==9) {
					loanVo.getTransferVo().setTransferAccount9Assersion(false);
					loanVo.getAssertionMap().put("TransferAccount9Assersion", false);
				}else if(item.getType()==10) {
					loanVo.getTransferVo().setTransferAccount10Assersion(false);
					loanVo.getAssertionMap().put("TransferAccount10Assersion", false);
				}else if(item.getType()==13) {
					loanVo.getTransferVo().setTransferAccount13Assersion(false);
					loanVo.getAssertionMap().put("TransferAccount13Assersion", false);
				}
			}
		}else if(item.getType()==9) {
			if(item.getOutAccountNo().equals(bniAccount.getAccountNo())&&
					item.getInAccountNo().equals(funderAccount.getAccountNo())) {
				loanVo.getTransferVo().setTransferAccount9Assersion(true);
				loanVo.getAssertionMap().put("TransferAccount9Assersion", true);
			}else {
				loanVo.getTransferVo().setTransferAccount9Assersion(false);
				loanVo.getAssertionMap().put("TransferAccount9Assersion", false);
			}
		}else if(item.getType()==35) {
			boolean TransferAccount35Assersion =item.getOutAccountNo().equals(bniAccount.getAccountNo())&&
			item.getInAccountNo().equals(taxAccount.getAccountNo())== true?true:false;
			loanVo.getAssertionMap().put("TransferAccount35Assersion", TransferAccount35Assersion);

		}
		
		if(item.getType()==1) {
			if(loanVo.getSecondProductCode().contains("v1")) {
				//从试算中获取  转账管理费
				BigDecimal transferAdmin = loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getUpfrontAdminfee()
				.add(loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getUpfrontCarryFee())
				.add(loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getUpfrontProvisionFee());
				boolean flag=	item.getAmount().compareTo(transferAdmin.longValue())== 0 ? true: false;
				loanVo.getTransferVo().setTransferAmount1Assersion(flag);
				loanVo.getAssertionMap().put("TransferAmount1Assersion",flag);
			}else if(loanVo.getSecondProductCode().contains("v2")) {
				log.info("财务发起转账金额v2");
				//从试算中获取  转账管理费
				BigDecimal transferAdmin = loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getExtendFee().getUpfrontAdminFee()
				.add(loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getExtendFee().getUpfrontCarryFee());
				log.info("财务发起转账{}，应转账金额{}",item.getAmount(),transferAdmin.longValue());
				
				boolean flag=	item.getAmount().compareTo(transferAdmin.longValue())== 0 ? true: false;
				log.info("财务发起转账金额v2 1：：{}"+flag);
				loanVo.getTransferVo().setTransferAmount1Assersion(flag);
				loanVo.getAssertionMap().put("TransferAmount1Assersion",flag);
			}
		}else if(item.getType()==9) {
			log.info("财务发起转账金额");
			//从试算中获取  资金方利息
			boolean flag=	item.getAmount().compareTo(loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getExtendFee().getUpfrontFundInterestFee().longValue())== 0 ? true: false;
			log.info("财务发起转账金额v2 9：：{}"+flag);
			loanVo.getTransferVo().setTransferAmount9Assersion(flag);
			loanVo.getAssertionMap().put("TransferAmount9Assersion",flag);
		}else if(item.getType()==10) {
			//从试算中获取  保证金
			boolean flag=	item.getAmount().compareTo(loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getExtendFee().getUpfrontProvisionFee().longValue())== 0 ? true: false;
			loanVo.getTransferVo().setTransferAmount10Assersion(flag);
			loanVo.getAssertionMap().put("TransferAmount10Assersion",flag);
			log.info("财务发起转账金额v2  10：：{}"+flag);
		}else if(item.getType()==13) {
			//从试算中获取  服务费
			boolean flag=	item.getAmount().compareTo(loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO().getExtendFee().getUpfrontServiceFee().longValue())== 0 ? true: false;
			loanVo.getTransferVo().setTransferAmount13Assersion(flag);
			loanVo.getAssertionMap().put("TransferAmount13Assersion",flag);
			log.info("财务发起转账金额v2  13：：{}"+flag);
		}else if(item.getType()==35) {
			//从试算中获取  税费(管理费税费  进位费税费  服务费税费 资金方税费)
			 UpfrontFeeVO upfrontFeeVO = loanVo.getCalUpfrontDuesVo().getUpfrontFeeVO();
			UpfrontExtendFee extendFee = upfrontFeeVO.getExtendFee();
			BigDecimal taxFee =null;
			if(loanVo.getSecondProductCode().contains("v1")) {
				taxFee = upfrontFeeVO.getUpfrontAdminfeeVat().add(upfrontFeeVO.getUpfrontCarryFeeVat());
			}else if(loanVo.getSecondProductCode().contains("v2")) {
				 taxFee = extendFee.getUpfrontAdminFeeVAT().add(extendFee.getUpfrontCarryFeeVAT())
						.add(extendFee.getUpfrontServiceFeeVAT()).add(extendFee.getUpFundInterestFeeWHT());
			}
	
			boolean flag=	item.getAmount().compareTo(taxFee.longValue())== 0 ? true: false;
			loanVo.getAssertionMap().put("TransferAmount35Assersion",flag);
			log.info("财务发起转账金额v2  13：：{}"+flag);
		}
		return transferAccountFlag;
	}
	//检查网关给用户的转账账号以及转账金额
	public void checkGatewayAccountAndAmount(LoanVo loanVo, FundAccount bniAccount, GatewayTask gatewayTask) {
		String json = gatewayTask.getParams();
		//检查账户，金额
		String outTransfer = (String) JSONPath.read(json, "$.debitAccountNo");
		String accountNumber = (String) JSONPath.read(json, "$.accountNumber");
		int transferAmount = (int) JSONPath.read(json, "$.amount");
		if(bniAccount.getAccountNo().equals(outTransfer)&&accountNumber.equals(loanVo.getBankAccountNumber())) {
			log.info("实际给用户的转入转出账号正确,转出账号1:{},转出账号2:{},转入账号1:{},转入账号2:{}"
					,bniAccount.getAccountNo(),outTransfer,accountNumber,loanVo.getBankAccountNumber());
			loanVo.getTransferVo().setActTransferAccountToUser(true);
			loanVo.getAssertionMap().put("ActTransferAccountToUser",true);
		}else {
			log.info("实际给用户的转入转出账号错误,转出账号1:{},转出账号2:{},转入账号1:{},转入账号2:{}"
					,bniAccount.getAccountNo(),outTransfer,accountNumber,loanVo.getBankAccountNumber());
			loanVo.getTransferVo().setActTransferAccountToUser(false);
			loanVo.getAssertionMap().put("ActTransferAccountToUser",false);
		}
//		boolean flag = transferAmount==loanVo.getTransferToUser()? true:false;
		boolean flag =  Integer.compare(transferAmount, loanVo.getTransferToUser())==0?true:false;
		log.info("转的{}，查询的{}",transferAmount,loanVo.getTransferToUser());
		loanVo.getTransferVo().setActTransferAmountToUser(flag);
		loanVo.getAssertionMap().put("ActTransferAmountToUser",flag);

//		try {
//			Assert.assertEquals(transferAmount, loanVo.getTransferToUser());
//			
//			log.info("给客户的转账金额有误,实际转账金额:{},应转账金额：{}",transferAmount,loanVo.getTransferToUser());
//			loanVo.getTransferVo().setActTransferAmountToUser(true);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			log.info("给客户的转账金额有误,实际转账金额:{},应转账金额：{}",transferAmount,loanVo.getTransferToUser());
//			loanVo.getTransferVo().setActTransferAmountToUser(false);
//		}
	}
	//检查充值记录转入转出账号，以及金额
	public boolean checkRechargeAccountAndAmount(LoanVo loanVo, FundAccount funderAccount, FundAccount bniAccount,
			boolean rechargeTransferAccountFlag, FinanceTrading item) {
		//检查转出账号
		if(item.getOutAccountNo().equals(funderAccount.getAccountNo())
				&&item.getInAccountNo().equals(bniAccount.getAccountNo())) {
			rechargeTransferAccountFlag = true;
			loanVo.getTransferVo().setTransferAccount4Assersion(true);
			loanVo.getAssertionMap().put("rechargeTransferAccount",true);
			log.info("转账类型{}",item.getType());
			log.info("财务发起充值记录转入转出账号正确,转出账号1:{},转出账号2:{},转入账号1:{},转入账号2:{}"
					,item.getOutAccountNo(),funderAccount.getAccountNo(),item.getInAccountNo(),bniAccount.getAccountNo());			
		}else {
			loanVo.getTransferVo().setTransferAccount4Assersion(false);
			loanVo.getAssertionMap().put("rechargeTransferAccount",false);
			log.info("财务发起充值记录转入转出账号错误,转出账号1:{},转出账号2:{},转入账号1:{},转入账号2:{}"
					,item.getOutAccountNo(),funderAccount.getAccountNo(),item.getInAccountNo(),bniAccount.getAccountNo());			
		}
		//校验充值金额金额
		int transferAmount = (int) loanVo.getAmount();
		if(loanVo.getBankCode().equals("BNI")||loanVo.getBankCode().equals("BNI SYARIAH")||loanVo.getLendType()==0) {
			log.info("转账金额正确,财务发起转账金额:{},应该转账金额:{}",item.getAmount(),transferAmount);
			 Boolean flag = ((Integer)transferAmount).compareTo(item.getAmount().intValue())==0 ? true: false;
			 loanVo.getTransferVo().setTransferAmount4Assersion(flag);
			 loanVo.getAssertionMap().put("rechargeTransferAmount",flag);
		}else {
			//修改跨行手续费为0
//			 transferAmount = (int) (loanVo.getAmount()+6500);
			 transferAmount = (int) (loanVo.getAmount()+0);
			 Boolean flag = ((Integer)transferAmount).compareTo(item.getAmount().intValue())==0 ? true: false;
			 log.info("转账金额正确,实际发起转账金额:{},应该转账金额:{}",item.getAmount(),transferAmount);
			 loanVo.getTransferVo().setTransferAmount4Assersion(flag);
			 loanVo.getAssertionMap().put("rechargeTransferAmount",flag);	 
		}
		return rechargeTransferAccountFlag;
		}
	
	//查询网关发起的转账记录
	public List<GatewayTask> gatewayTaskList(LoanVo loanVo) {
		List<GatewayTask> gatewayTaskList;
		QueryWrapper<GatewayTask> gatewayTask = new QueryWrapper<>();
		gatewayTask.eq("loan_num",  loanVo.getLoanNumber());
		 gatewayTaskList = gatewayTaskMapper.selectList(gatewayTask);
		return gatewayTaskList;
	}
	//查询财务发起的转账记录
	public List<FinanceTrading> financeTradingList(LoanVo loanVo) {
		List<FinanceTrading> financeTradingList;
		QueryWrapper<FinanceTrading> financeTrading = new QueryWrapper<>();
		financeTrading.eq("loan_number", loanVo.getLoanNumber());
		 financeTradingList = financeTradingMapper.selectList(financeTrading);
		return financeTradingList;
	}
	//查询adwa账号
	public FundAccount adwaAccount() {
		FundAccount adwaAccount;
		QueryWrapper<FundAccount> queryAwdaAccount = new QueryWrapper<>();
		queryAwdaAccount.eq("fund_code","FUND_AWDA").eq("gateway_type", 2);
		 adwaAccount = fundAccountMapper.selectOne(queryAwdaAccount);
		return adwaAccount;
	}
	//查询BNI账号
	public FundAccount bniAccount() {
		FundAccount bniAccount;
		QueryWrapper<FundAccount> queryBNIAccount = new QueryWrapper<>();
		queryBNIAccount.eq("fund_code", "BNI_LOANS");
		 bniAccount = fundAccountMapper.selectOne(queryBNIAccount);
		return bniAccount;
	}
	//查询tax账号
	public FundAccount taxAccount() {
		FundAccount taxAccount;
		QueryWrapper<FundAccount> queryAwdaAccount = new QueryWrapper<>();
		queryAwdaAccount.eq("fund_code","TAX_ACCOUNT").eq("gateway_type", 2);
		taxAccount = fundAccountMapper.selectOne(queryAwdaAccount);
		return taxAccount;
	}
	//查询资金方账号
	public FundAccount funderAccount(LoanVo loanVo) {
		FundAccount funderAccount;
		QueryWrapper<FundAccount> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("fund_base_id", loanVo.getFundId()).eq("account_type", 2);
		 funderAccount = fundAccountMapper.selectOne(queryWrapper);
		return funderAccount;
	}
	public  static void main(String[] args) {
		BigDecimal bigDecimal = new BigDecimal("1.5835");
		BigDecimal setScale = bigDecimal.setScale(3, BigDecimal.ROUND_UNNECESSARY);
		log.info("setScale1 BigDecimal.ROUND_HALF_UP{}",setScale);
		
		BigDecimal bigDecimal2 = new BigDecimal("1.5845");
		BigDecimal setScale2 = bigDecimal2.setScale(3, BigDecimal.ROUND_UNNECESSARY);
		log.info("setScale2 BigDecimal.ROUND_HALF_UP{}",setScale2);
		BigDecimal bigDecimal3 = new BigDecimal("1.5849");
//		BigDecimal setScale3 = bigDecimal.setScale(3, bigDecimal3.ROUND_UNNECESSARY);
//		log.info("setScale3 BigDecimal.ROUND_HALF_UP{}",setScale3);
		
		
//		BigDecimal setScale = bigDecimal.setScale(-3, BigDecimal.ROUND_UP);
//		log.info("setScale{}",setScale.intValue());
//		Class<PostProcessor> clazz = PostProcessor.class;
//		PostProcessor newInstance = null;
//		try {
//			 newInstance = clazz.newInstance();
//		} catch (InstantiationException | IllegalAccessException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		Method[] declaredMethods = clazz.getDeclaredMethods();
//		LoanVo loanVo = new LoanVo();
//		for (Method method : declaredMethods) {
////			list.add(method.getName());
//			try {
//				if(!"main".equals(method.getName())) {
//					
//					method.invoke(newInstance,loanVo);
//				}
//			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}

}
