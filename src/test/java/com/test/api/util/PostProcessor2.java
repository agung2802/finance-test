package com.test.api.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSONPath;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.test.api.entity.FinanceTrading;
import com.test.api.entity.FundAccount;
import com.test.api.entity.GatewayTask;
import com.test.api.entity.vo.LoanVo;
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
public class PostProcessor2 {
	@Autowired
	private FinanceTradingMapper financeTradingMapper;
	@Autowired
	private FundAccountMapper fundAccountMapper;
	@Autowired
	private GatewayTaskMapper gatewayTaskMapper;
	@Autowired
	CostCalculation calculation;
	
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

			BigDecimal adminFee = new BigDecimal(loanVo.getAmount())
					.multiply(new BigDecimal(admin)).divide(new BigDecimal(adminAll), 6, BigDecimal.ROUND_HALF_UP)
					.multiply(loanVo.getUpfrontFeeRate()).setScale(0, BigDecimal.ROUND_HALF_UP);
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
			BigDecimal adminFee = new BigDecimal(loanVo.getAmount())
					.multiply(new BigDecimal(admin)).divide(new BigDecimal(adminAll), 6, BigDecimal.ROUND_HALF_UP)
					.multiply(loanVo.getUpfrontFeeRate()).setScale(0, BigDecimal.ROUND_HALF_UP);
			log.info("==adminFee{}",adminFee);
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
	public  void checkTransferRecord(LoanVo loanVo) {
		System.out.println("======getTransferRecordAssertion方法被执行");
		log.info("loan:{}", loanVo);
		//资金方账号
		FundAccount funderAccount = null;
		//BNI放款账号
		FundAccount bniAccount = null;
		//adwa收款账号
		FundAccount adwaAccount = null;
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
				if(typeList.contains(4)&&typeList.contains(1)) {
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
								transferAccountFlag = checkNorechergeAccountAndAmount(funderAccount,bniAccount, adwaAccount,
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
						&&typeList.contains(10)&&typeList.contains(13)) {
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
								transferAccountFlag = checkNorechergeAccountAndAmount(funderAccount,bniAccount, adwaAccount,
										transferAccountFlag, item,loanVo);
							}else if(item.getType()==9)  {
								transferAccountFlag = checkNorechergeAccountAndAmount(funderAccount,bniAccount, adwaAccount,
										transferAccountFlag, item,loanVo);
							}else if(item.getType()==10)  {
								transferAccountFlag = checkNorechergeAccountAndAmount(funderAccount,bniAccount, adwaAccount,
										transferAccountFlag, item,loanVo);
							}else if(item.getType()==13)  {
								if(loanVo.getUpfrontServiceFee().intValue()>0) {		
									transferAccountFlag = checkNorechergeAccountAndAmount(funderAccount,bniAccount, adwaAccount,
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
	public boolean checkNorechergeAccountAndAmount(FundAccount funderAccount,FundAccount bniAccount, FundAccount adwaAccount,
			boolean transferAccountFlag, FinanceTrading item,LoanVo loanVo) {
		log.info("转账类型:{}",item.getType().toString());
		if(item.getType()!=9) {
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
		}
		
		if(item.getType()==1) {
			if(loanVo.getSecondProductCode().contains("v1")) {
				boolean flag=	item.getAmount().compareTo(loanVo.getUpfrontFee())== 0 ? true: false;
				loanVo.getTransferVo().setTransferAmount1Assersion(flag);
				loanVo.getAssertionMap().put("TransferAmount1Assersion",flag);
			}else if(loanVo.getSecondProductCode().contains("v2")) {
				log.info("财务发起转账金额v2");
				log.info("财务发起转账{}，应转账金额{}",item.getAmount(),loanVo.getUpfrontAdmin().longValue());
				boolean flag=	item.getAmount().compareTo(loanVo.getUpfrontAdmin().longValue())== 0 ? true: false;
				log.info("财务发起转账金额v2 1：：{}"+flag);
				loanVo.getTransferVo().setTransferAmount1Assersion(flag);
				loanVo.getAssertionMap().put("TransferAmount1Assersion",flag);
			}
		}else if(item.getType()==9) {
			log.info("财务发起转账金额");
			boolean flag=	item.getAmount().compareTo(loanVo.getUpfrontFundInterest().longValue())== 0 ? true: false;
			log.info("财务发起转账金额v2 9：：{}"+flag);
			loanVo.getTransferVo().setTransferAmount9Assersion(flag);
			loanVo.getAssertionMap().put("TransferAmount9Assersion",flag);
		}else if(item.getType()==10) {
			boolean flag=	item.getAmount().compareTo(loanVo.getUpfrontProvisionFee().longValue())== 0 ? true: false;
			loanVo.getTransferVo().setTransferAmount10Assersion(flag);
			loanVo.getAssertionMap().put("TransferAmount10Assersion",flag);
			log.info("财务发起转账金额v2  10：：{}"+flag);
		}else if(item.getType()==13) {
			boolean flag=	item.getAmount().compareTo(loanVo.getUpfrontServiceFee().longValue())== 0 ? true: false;
			loanVo.getTransferVo().setTransferAmount13Assersion(flag);
			loanVo.getAssertionMap().put("TransferAmount13Assersion",flag);
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
