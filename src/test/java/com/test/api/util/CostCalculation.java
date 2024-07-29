/**
 * 
 */
package com.test.api.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.test.api.entity.Dues;
import com.test.api.entity.FinanceLend;
import com.test.api.entity.FinanceUserBankcard;
import com.test.api.entity.LoanDetail;
import com.test.api.entity.LoanDetailExtend;
import com.test.api.entity.Loans;
import com.test.api.entity.TryInfo;
import com.test.api.entity.UpfrontExtendFee;
import com.test.api.entity.vo.CheckStatus;
import com.test.api.entity.vo.DuesVo;
import com.test.api.entity.vo.LoanVo;
import com.test.api.entity.vo.LoanVoExt;
import com.test.api.entity.vo.RateConfig;
import com.test.api.entity.vo.TransferVo;
import com.test.api.entity.vo.UpfrontDuesVo;
import com.test.api.entity.vo.UpfrontFeeVO;
import com.test.api.mapper.DuesMapper;
import com.test.api.mapper.FinanceLendMapper;
import com.test.api.mapper.FinanceTradingMapper;
import com.test.api.mapper.FinanceUserBankcardMapper;
import com.test.api.mapper.FundAccountMapper;
import com.test.api.mapper.LoanDetailExtendMapper;
import com.test.api.mapper.LoanDetailMapper;
import com.test.api.mapper.LoansMapper;
import com.test.api.service.CostCalculationService;

import lombok.extern.slf4j.Slf4j;

/**
* @author Faisal Mulya Santosa:
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
//@SpringBootTest
@Slf4j
@Component
public class CostCalculation {
	
	@Resource
	LoanDetailMapper loanDetailMapper;
	@Resource
	private FundAccountMapper fundAccountMapper;
	@Autowired
	DuesMapper duesMapper;
	@Autowired
	LoanDetailExtendMapper loanDetailExtendMapper;
	@Autowired
	FinanceLendMapper financeLendMapper;
	@Autowired
	FinanceUserBankcardMapper financeUserBankcardMapper;
	@Autowired
	LoansMapper loansMapper;
	@Autowired
	FinanceTradingMapper financeTradingMapper;
	@Autowired
	CostCalculationService costCalculationService;
//	@org.junit.jupiter.api.Test
	//CL21120614144303898010423
	public LoanVo buildLoanVo(LoanVo loanVo)  {
//    public void buildLoanVo()  {
	
//		LoanVo loanVo = new LoanVo();
//		loanVo.setLoanNumber("CL21120614144303898010423");
		//试算结果 
		TryInfo tryInfo = new TryInfo();
		tryInfo.setLoanNumber(loanVo.getLoanNumber());
		UpfrontDuesVo calculateUpfrontDues = costCalculationService.CalculateUpfrontDues(tryInfo);
		loanVo.setCalUpfrontDuesVo(calculateUpfrontDues);
		//查询V1费率
		//砍头费
		UpfrontFeeVO upfrontFeeVO = new UpfrontFeeVO();
		loanVo.setUpfrontFeeVO(upfrontFeeVO);
		//还款计划
		DuesVo duesVo = new DuesVo();
		QueryWrapper<LoanDetail> queryLoanDetailWrapper = new QueryWrapper<>();
		log.info("订单号码{}",loanVo.getLoanNumber());
		log.info("订单号码{},{}",loanDetailMapper,fundAccountMapper);
		queryLoanDetailWrapper.eq("loan_number", loanVo.getLoanNumber());
		LoanDetail loanDetail = loanDetailMapper.selectOne(queryLoanDetailWrapper);
		log.info("loanDetail{}",loanDetail);
		//查询V2费率
		QueryWrapper<LoanDetailExtend> queryLoanDetailExtWrapper =  new QueryWrapper<>();
		queryLoanDetailExtWrapper.eq("loan_number", loanVo.getLoanNumber());
		LoanDetailExtend loanDetailExtend = loanDetailExtendMapper.selectOne(queryLoanDetailExtWrapper);
		log.info("loanDetailExtend{}", loanDetailExtend);
		//查询还款计划中的费用
		
//		long totalFundInterestFee = duesMapper.getTotalFee(loanVo.getLoanNumber(), "Fund_Interest");
//		long totalServiceFee = duesMapper.getTotalFee(loanVo.getLoanNumber(), "Service_Fee");
		QueryWrapper<Dues> queryWrapperDues = new  QueryWrapper<>();
		queryWrapperDues.eq("loan_id", loanVo.getLoanNumber());
		List<Dues> duesList = duesMapper.selectList(queryWrapperDues);
		long totalFundInterestFee = 0;
		long totalServiceFee = 0;
		long totalProvisionFee = 0;
		for (Dues dues : duesList) {
			if("Fund_Interest".equals(dues.getDueType())) {
				totalFundInterestFee = totalFundInterestFee+dues.getAmount();
			}else if("Service_Fee".equals(dues.getDueType())) {
				totalServiceFee = totalServiceFee+dues.getAmount();

			}else if("Provision_fee".equals(dues.getDueType())) {
				totalProvisionFee = totalProvisionFee+dues.getAmount();
			}
		}
		log.info("fundInterestFee:{},ServiceFee:{},ProvisionFee:{}",totalFundInterestFee,totalServiceFee,totalProvisionFee);

		log.info("totalFundInterestFee:{},totalServiceFee:{}",totalFundInterestFee,totalServiceFee);
		loanVo.setFundInterestFee(new BigDecimal(totalFundInterestFee));
		loanVo.setServiceFee(new BigDecimal(totalServiceFee));
		loanVo.setRepayProvisionFee((new BigDecimal(totalProvisionFee)));;
		//检查还款计划
		
		
		loanVo.setFundId(loanDetail.getFundId());
		loanVo.setSecondProductCode(loanDetail.getSecondProdCode());
		loanVo.setAmount(loanDetail.getAmount());
		loanVo.setUpfrontFeeRate(loanDetail.getAdminRate());
		BigDecimal fundInterestRate = loanDetail.getFundInterestRate().compareTo(BigDecimal.ZERO)==0 ? 
				new BigDecimal(0): loanDetail.getFundInterestRate();
		loanVo.setFundInterestRate(fundInterestRate);
		BigDecimal serviceFeeRate = loanDetail.getServiceFeeRate().compareTo(BigDecimal.ZERO)==0?
				new BigDecimal(0): loanDetail.getServiceFeeRate();
		loanVo.setServiceFeeRate(serviceFeeRate);

		if(loanDetail.getAdvanceRate()!=null) {
			loanVo.setAdvanceRate(loanDetail.getAdvanceRate());
		}
		loanVo.setLateRate(loanDetail.getLateRate());
		loanVo.setDisbursedAt(loanDetail.getLendAt());
		loanVo.setTenor(loanDetail.getPeriod());
		loanVo.setUserId(loanDetail.getUserId());
		loanVo.setLendType(loanDetail.getLendType());
//		loanVo.setUpfrontFeeRate(loanDetail.getAdminRate());
//		loanVo.setFundInterestRate(loanDetail.getFundInterestRate());
//		loanVo.setServiceFeeRate(loanDetail.getServiceFeeRate());
		// 砍头费
		loanVo.setUpfrontFee(loanDetail.getAdminFee());
		upfrontFeeVO.setUpfrontFee(new BigDecimal(loanDetail.getAdminFee()));
		if(loanVo.getLendType()==1&&loanVo.getLoanNumber().startsWith("CL")) {	
			QueryWrapper<FinanceUserBankcard> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("user_id", loanVo.getUserId());
			FinanceUserBankcard userBankcard = financeUserBankcardMapper.selectOne(queryWrapper);
			log.info("userBankcard{}",userBankcard);
			loanVo.setBankCode(userBankcard.getBankCode());
			loanVo.setBankAccountNumber(userBankcard.getBankAccountNumber());
		}else if(loanVo.getLendType()==0&&loanVo.getLoanNumber().startsWith("CL")) {
			loanVo.setBankCode("FIF");
			loanVo.setBankAccountNumber("7000000000");
		}
		loanVo.setTransferToUser((int)(loanVo.getAmount()-loanVo.getUpfrontFee()));
		
		loanVo.setSalaryDay(loanDetailExtend.getSalaryDay());
		loanVo.setProvisionFeeRate(loanDetailExtend.getFundProvisionRate());
		loanVo.setAdPvProportion(loanDetailExtend.getProvisionRate());
		loanVo.setLoanVoExt(new LoanVoExt());
		loanVo.setTransferVo(new TransferVo());
		loanVo.setAssertionMap(new HashMap<>());
		loanVo.setRateConfig(new RateConfig());
		
		buildRateConfig(loanVo,loanDetail,loanDetailExtend);
		QueryWrapper<FinanceLend> queryFinanceLendWrapper = new QueryWrapper<>();
		FinanceLend financeLend =null;
		try {
			for (int i = 0; i < 10; i++) {
				if(loanVo.getLoanNumber().startsWith("CL")) {
					queryFinanceLendWrapper.eq("loan_number", loanVo.getLoanNumber()).eq("mode", "1");
				}else if(loanVo.getLoanNumber().startsWith("PL")) {
					queryFinanceLendWrapper.eq("loan_number", loanVo.getLoanNumber()).eq("mode", "3");
				}
				financeLend = financeLendMapper.selectOne(queryFinanceLendWrapper);
				log.info("financeLend{}", financeLend);
				if(null!=financeLend) {
					break;
				}else {
					log.info("重新查询financeLend",i);
					Thread.sleep(1000);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			log.info("=====financeLend");
			log.info("=====financeLend{}",e);
		}
		QueryWrapper<Loans> queryLoans = new QueryWrapper<>();
		queryLoans.eq("loan_number", loanVo.getLoanNumber());
		Loans loans = loansMapper.selectOne(queryLoans);
		log.info("loans{}",loans);

		//数据状态
		loanVo.setCheckStatus(new CheckStatus());
		loanVo.setLendStatus(loanDetail.getLendStatus());
		loanVo.setRepayStatus(loanDetail.getRepayStatus());
		loanVo.setLendStatus(financeLend.getStatus());
		loanVo.setState(loans.getState());
		//初始化V1产品砍头费中的管理费和保证金
		if(loanVo.getSecondProductCode().contains("v1")&&loanVo.getLoanNumber().startsWith("CL")) {
			loanVo.setAdminfee(financeLend.getOtherFee());
			loanVo.setProvisionFee(financeLend.getProvisionFee());
			//砍头费中费用
			String extFee = loanDetailExtend.getExt();
			HashMap<String,Object> extFeeMap = (HashMap<String, Object>) JSON.parseObject(extFee,Map.class);
			upfrontFeeVO.setUpfrontAdminfee(new BigDecimal(extFeeMap.get("adminFee").toString()));
			upfrontFeeVO.setUpfrontAdminfeeVat(new BigDecimal(extFeeMap.get("adminFeeVAT").toString()));
			upfrontFeeVO.setUpfrontProvisionFee(new BigDecimal(extFeeMap.get("provisionFee").toString()));
			upfrontFeeVO.setUpfrontCarryFee(new BigDecimal(extFeeMap.get("carryFee").toString()));
			upfrontFeeVO.setUpfrontCarryFeeVat(new BigDecimal(extFeeMap.get("carryFeeVAT").toString()));
		}
		
		if(loanVo.getSecondProductCode().contains("v2")&&loanVo.getLoanNumber().startsWith("CL")) {
			UpfrontExtendFee upfrontExtendFee = new UpfrontExtendFee();
			String extendRate = loanDetailExtend.getExtend();
			String extFee = loanDetailExtend.getExt();
			HashMap<String,Object> extendRateMap = (HashMap<String, Object>) JSON.parseObject(extendRate,Map.class);
			HashMap<String,Object> extFeeMap = (HashMap<String, Object>) JSON.parseObject(extFee,Map.class);
			for(Entry<String,Object> entry :extendRateMap.entrySet()) {
				log.info("entry.getKey():{},entry.getValue:(){}",entry.getKey(),entry.getValue());
			}
			for(Entry<String,Object> entryExt :extFeeMap.entrySet()) {
				log.info("entry.getKey():{},entry.getValue:(){}",entryExt.getKey(),entryExt.getValue());
			}
			loanVo.setUpfrontFundInterestRate(new BigDecimal(extendRateMap.get("fundInterestRate").toString()));
			loanVo.setUpfrontServiceFeeRate(new BigDecimal(extendRateMap.get("serviceFeeRate").toString()));
			loanVo.setUpfrontProvisionFeeRate(new BigDecimal(extendRateMap.get("provisionRate").toString()));	
			loanVo.setUpfrontAdmin(new BigDecimal(extFeeMap.get("adminFee").toString()));
			loanVo.setUpfrontAdminVat(new BigDecimal(extFeeMap.get("adminFeeVAT").toString()));
			loanVo.setUpfrontProvisionFee(new BigDecimal(extFeeMap.get("provisionFee").toString()));
			loanVo.setUpfronCarryFeeNoVat(new BigDecimal(extFeeMap.get("carryFee").toString()));
			loanVo.setUpfronCarryFeeVat(new BigDecimal(extFeeMap.get("carryFeeVAT").toString()));
			
			loanVo.setUpfrontFundInterest(new BigDecimal(extFeeMap.get("fundInterestFee").toString()));
			loanVo.setUpfrontServiceFee(new BigDecimal(extFeeMap.get("serviceFee").toString()));
			loanVo.setUpfrontServiceFeeVat(new BigDecimal(extFeeMap.get("serviceFeeVAT").toString()));
			loanVo.setUpfrontFundInterestWht(new BigDecimal(extFeeMap.get("fundInterestFeeWHT").toString()));
			
			upfrontExtendFee.setUpfrontAdminFee(new BigDecimal(extFeeMap.get("adminFee").toString()));
			upfrontExtendFee.setUpfrontAdminFeeVAT(new BigDecimal(extFeeMap.get("adminFeeVAT").toString()));
			upfrontExtendFee.setUpfrontProvisionFee(new BigDecimal(extFeeMap.get("provisionFee").toString()));
			upfrontExtendFee.setUpfrontCarryFee(new BigDecimal(extFeeMap.get("carryFee").toString()));
			upfrontExtendFee.setUpfrontCarryFeeVAT(new BigDecimal(extFeeMap.get("carryFeeVAT").toString()));
			upfrontExtendFee.setUpfrontFundInterestFee(new BigDecimal(extFeeMap.get("fundInterestFee").toString()));
			upfrontExtendFee.setUpFundInterestFeeWHT(new BigDecimal(extFeeMap.get("fundInterestFeeWHT").toString()));
			upfrontExtendFee.setUpfrontServiceFee(new BigDecimal(extFeeMap.get("serviceFee").toString()));
			upfrontExtendFee.setUpfrontServiceFeeVAT(new BigDecimal(extFeeMap.get("serviceFeeVAT").toString()));
			upfrontFeeVO.setExtendFee(upfrontExtendFee);
		}
		if(loanVo.getSecondProductCode().contains("v1")&&loanVo.getLoanNumber().startsWith("PL")) {
			BigDecimal provisionFeeRate = loanDetailExtend.getFundProvisionRate().compareTo(BigDecimal.ZERO)==0?
					new BigDecimal(0): loanDetailExtend.getFundProvisionRate();
			loanVo.setProvisionFeeRate(provisionFeeRate);
		}
//		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		Date disburseAt = null;
		try {
			if(null==loanVo.getDisbursedAt()) {
				//查询结果为空，默认为当天放款成功
				disburseAt = new Date();
			}else {
				disburseAt = dateFormat.parse(loanVo.getDisbursedAt().toString());
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			log.info("disburseAt{}",e);
			e.printStackTrace();
		}
		Calendar calendar = Calendar.getInstance();
		try {
			calendar.setTime(disburseAt);
		} catch (Exception e) {
			log.info("loanVo{}",loanVo);
			log.info("e{}",e);

			// TODO: handle exception
		}
		log.info("loanVo{}",loanVo);

		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		System.out.println("year   ::"+year+"   ::"+month+"day   ::"+day);
		int salaryDay = Integer.parseInt(loanVo.getSalaryDay());
		String nextDueDate="";
		Calendar calendar2 = Calendar.getInstance();
		calendar2.set(Calendar.HOUR_OF_DAY, 0);
		calendar2.clear(Calendar.MINUTE);
		calendar2.clear(Calendar.SECOND);
		calendar2.set(year, month, salaryDay);
		int criticalValue = 0;
		if(loanVo.getSecondProductCode().contains("short")) {
			criticalValue = 15;
		}else if(loanVo.getSecondProductCode().contains("long")) {
			criticalValue = 10;
		}
		if(salaryDay<day) {
			calendar2.set(year, month+1, salaryDay);
			if(getDiffDay(calendar,calendar2)<criticalValue) {
				calendar2.set(year, month+1, salaryDay);
				if(loanVo.getSecondProductCode().contains("long")) {
					loanVo.setDdmPlan("A");
				}
			}else {
				if(loanVo.getSecondProductCode().contains("long")) {
					loanVo.setDdmPlan("B");
				}
			}
		}else {
			if(getDiffDay(calendar,calendar2)<criticalValue) {
				calendar2.set(year, month+1, salaryDay);
				if(loanVo.getSecondProductCode().contains("long")) {
					loanVo.setDdmPlan("A");
				}
			}else {
				if(loanVo.getSecondProductCode().contains("long")) {
					loanVo.setDdmPlan("B");
				}
			}
		}
		nextDueDate = dateFormat.format(calendar2.getTime());
		System.out.println("nextDueDate::"+nextDueDate);
		Integer subTener = Integer.valueOf(loanVo.getTenor().substring(0,loanVo.getTenor().length()-1));
		//下一期时间
//		 calendar2.set(year, calendar2.get(Calendar.MONTH)+subTener-1, salaryDay);
		 calendar2.add(Calendar.MONTH, subTener-1);
		 loanVo.setNextDueDateDDM(nextDueDate);
		calendar.add(Calendar.MONTH, 1);
		loanVo.setNextDueDate(dateFormat.format(calendar.getTime()));
		//最后一期还款时间
		if(loanVo.getSecondProductCode().contains("long")) {
			loanVo.setFinallyDueDateDDM(dateFormat.format( calendar2.getTime()));
			calendar.add(Calendar.MONTH, subTener-1);
			loanVo.setFinallyDueDate(dateFormat.format(calendar.getTime()));
			loanVo.setDdmDay(Math.abs(getDiffDay(calendar,calendar2)));
		}
		System.out.println(loanVo.getFinallyDueDateDDM());
		System.out.println(loanVo);
		return loanVo;
	}
	
	public int getDiffDay(Calendar cal1,Calendar cal2) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.format(cal1.getTime());
		System.out.println(""+dateFormat.format(cal1.getTime())+"        "+dateFormat.format(cal2.getTime()));
		System.out.println(""+cal2.getTime().getTime()+"        "+cal1.getTime().getTime());

//		long diff = (cal2.getTime().getTime()-cal1.getTime().getTime())/(24*60*60*1000);
		BigDecimal multiply = new BigDecimal(24).multiply(new BigDecimal(60)).multiply(new BigDecimal(60))
		.multiply(new BigDecimal(1000));
		BigDecimal  diff =new BigDecimal(cal2.getTime().getTime()).subtract(new BigDecimal(cal1.getTime().getTime()))
		.divide(multiply, 0, BigDecimal.ROUND_HALF_UP);
		log.info("diff:"+diff);
		return diff.intValue();
	}
	
	public Boolean checkAssertionMap(LoanVo loanVo) {
		log.info("checkAssertionMap");
		HashMap<String, Boolean> assertionMap = loanVo.getAssertionMap();
		Set<Entry<String, Boolean>> entrySet = assertionMap.entrySet();
		for(Entry<String, Boolean> entry:assertionMap.entrySet()) {
			log.info("entry.getKey(){},entry.getValue()==={}",entry.getKey(),entry.getValue());

			if(entry.getValue()==false) {
				log.info("entry.getValue()==={}",entry.getValue());
				return false;
			}
		}
		return true;
	}
//	@org.junit.jupiter.api.Test
	public void test() {
		LoanVo loanVo = new LoanVo();
		loanVo.setLoanNumber("CL21110916575886846716911");
		QueryWrapper<Dues> queryWrapperDues = new  QueryWrapper<>();
		queryWrapperDues.eq("loan_id", "PL22032023223743073341928");
		List<Dues> duesList = duesMapper.selectList(queryWrapperDues);
		long fundInterestFee = 0;
		long ServiceFee = 0;
		long ProvisionFee = 0;
		for (Dues dues : duesList) {
			if("Fund_Interest".equals(dues.getDueType())) {
				fundInterestFee = fundInterestFee+dues.getAmount();
			}else if("Service_Fee".equals(dues.getDueType())) {
				ServiceFee = ServiceFee+dues.getAmount();

			}else if("Provision_fee".equals(dues.getDueType())) {
				ProvisionFee = ProvisionFee+dues.getAmount();
			}
		}
		log.info("fundInterestFee:{},ServiceFee:{},ProvisionFee:{}",fundInterestFee,ServiceFee,ProvisionFee);

	}
//	@Test
	public void buildRateConfig(LoanVo loanVo,LoanDetail loanDetail,LoanDetailExtend loanDetailExtend) {
//		LoanVo loanVo = new LoanVo();
//		loanVo.setSecondProductCode("NS_maucash_long_B1-1_v2");
//		loanVo.setRateConfig(new RateConfig());
//		loanVo.setFundId(5);
		HashMap<String, Object> findProductRateConfig = financeTradingMapper.findProductRateConfig(loanVo.getSecondProductCode());
		
		log.info("findProductRateConfig{}",JSON.toJSONString(findProductRateConfig));
		long productRateId = (long) findProductRateConfig.get("id");
		HashMap<String, Object> findFundRateConfig = financeTradingMapper.findFundRateConfig(loanVo.getFundId(), productRateId);
		log.info("findFundRateConfig{}",JSON.toJSONString(findFundRateConfig));
		loanVo.getRateConfig().setSecondProdCode(loanVo.getSecondProductCode());
		loanVo.getRateConfig().setFundId(loanVo.getFundId());
		loanVo.getRateConfig().setUpfrontFeeRate((BigDecimal)findProductRateConfig.get("admin_rate"));
		loanVo.getRateConfig().setLateRate((BigDecimal)findProductRateConfig.get("late_rate"));

		if(loanDetail.getPromotionCode()!=null&&loanDetail.getPromotionCode().length()>0) {
			if(loanDetailExtend.getPreferentialType()==1) {
				loanVo.getRateConfig().setLateRate(loanVo.getRateConfig().getLateRate().multiply(new BigDecimal(loanDetailExtend.getDisLateFee())));
			}else if(loanDetailExtend.getPreferentialType()==2) {
				loanVo.getRateConfig().setLateRate(new BigDecimal(loanDetailExtend.getDisLateFee()));
			}
		}
		loanVo.getRateConfig().setAdvanceRate((BigDecimal)findProductRateConfig.get("advance_rate"));
		loanVo.getRateConfig().setProductRateId((long) findProductRateConfig.get("id"));
		loanVo.getRateConfig().setAdminType((int) findProductRateConfig.get("admin_type"));
		String adminProvisionProportion = (String) findProductRateConfig.get("provision_rate");
		log.info("adminProvisionProportion{}",adminProvisionProportion);
		if(findProductRateConfig.get("provision_rate")!=null) {
			
			loanVo.getRateConfig().setAdminProvisionProportion(findProductRateConfig.get("provision_rate").toString());
		}
		String extend = findFundRateConfig.get("extend").toString();
		loanVo.getRateConfig().setExtend(extend);
		BigDecimal fundInterestRate = new BigDecimal(findFundRateConfig.get("fund_interest_rate").toString());
		fundInterestRate = fundInterestRate.compareTo(BigDecimal.ZERO)==0 ? new BigDecimal(0):fundInterestRate;
		loanVo.getRateConfig().setFundInterestRate(fundInterestRate);
		BigDecimal fundProvisionRate = (BigDecimal)findFundRateConfig.get("fund_provision_rate");
		if(fundProvisionRate!=null) {
			
			loanVo.getRateConfig().setFundProvisionRate(new BigDecimal(findFundRateConfig.get("fund_provision_rate").toString()));
		}
		BigDecimal serviceFeeRate = new BigDecimal(findFundRateConfig.get("service_fee_rate").toString());
		serviceFeeRate = serviceFeeRate.compareTo(BigDecimal.ZERO)==0?new BigDecimal(0):serviceFeeRate;
		loanVo.getRateConfig().setServiceFeeRate(serviceFeeRate);
		if(extend.length()>2) {
			BigDecimal upfrontProvisionRate =  new BigDecimal( JSONPath.read(extend, "$.provisionRate").toString()) ;
			BigDecimal upfrontFundInterestRate = new BigDecimal( JSONPath.read(extend, "$.fundInterestRate").toString());
			BigDecimal upfrontServiceFeeRate = new BigDecimal(JSONPath.read(extend, "$.serviceFeeRate").toString());
			loanVo.getRateConfig().setUpfrontFundInterestRate(upfrontFundInterestRate);
			loanVo.getRateConfig().setUpfrontProvisionRate(upfrontProvisionRate);
			loanVo.getRateConfig().setUpfrontServiceFeeRate(upfrontServiceFeeRate);

		}
		log.info("loanVo{}",loanVo);
		
	}
	public static void main(String[] args) throws ParseException {
		CostCalculation calculation = new CostCalculation();
		
		LoanVo loanVo = new LoanVo();
		loanVo.setLoanNumber("CL21110916575886846716911");
		
//		calculation.buildLoanVo(loanVo);
//		System.out.println("===============");
//		String finaDdm="2022-09-25";
//		SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
//		Date date =sdf.parse(finaDdm);
//		Calendar calendar1 = Calendar.getInstance();
//		calendar1.setTime(date);	
//		String finallyDueDate="2022-09-17";
//		SimpleDateFormat sdfs= new SimpleDateFormat("yyyy-MM-dd");
//		Date date2 =sdfs.parse(finallyDueDate);
//		Calendar calendar2 = Calendar.getInstance();
//		calendar2.setTime(date2);
//		calculation.getDiffDay(calendar1, calendar2);
		
	}
}
