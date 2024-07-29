package com.test.api.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.util.NumberUtil;
import com.test.api.entity.*;
import com.test.api.entity.vo.*;
import com.test.api.service.CostCalculationService;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.test.api.mapper.DuesMapper;
import com.test.api.mapper.LoanDetailExtendMapper;
import com.test.api.mapper.LoanDetailMapper;
import com.test.api.mapper.UpfrontDuesMapper;
import com.test.api.service.RepaymentService;

import lombok.extern.slf4j.Slf4j;
import sun.applet.Main;

import javax.swing.text.DateFormatter;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29
* 类说明
*/
@Slf4j
@Service
public class RepaymentServiceImpl implements RepaymentService{
	@Autowired
	DuesMapper duesMapper;
	@Autowired
	LoanDetailMapper detailMapper;
	@Autowired
	LoanDetailExtendMapper detailExtendMapper;

	@Autowired
	UpfrontDuesMapper upfrontDuesMapper;

	@Autowired
	CostCalculationServiceImpl costCalculationServiceImpl;

	/**
	 *
	 * 试算还是有差异  看着像是资金方利息，平台服务费（平台服务费+平台服务费vat）乘以提前结清比例向上进位，
	 * @param loanNumber
	 * @return
	 */

	@Override
	public Object repayByadvance(String loanNumber) {
		// 试算,先计算服务费明细
		QueryWrapper<Dues> queryWrapper = new QueryWrapper<Dues>();
		queryWrapper.eq("loan_id",loanNumber);
		queryWrapper.eq("is_close",0);
		List<Dues> dues = duesMapper.selectList(queryWrapper);
		//对dues 排序
		Collections.sort(dues, new Comparator<Dues>() {
			@Override
			public int compare(Dues o1, Dues o2) {
				if(o1.getDueIndex()==o2.getDueIndex()){
					return o1.getRepayIndex().compareTo(o2.getRepayIndex());
				}
				return o1.getDueIndex().compareTo(o2.getDueIndex());
			}
		});
		BigDecimal serverSum = new BigDecimal("0");
		BigDecimal serverVatSum = new BigDecimal("0");
		BigDecimal serverSumDiscounts = new BigDecimal("0");
		BigDecimal principalSum = new BigDecimal("0");
		//资金方利息
		BigDecimal fundInterest = new BigDecimal("0");
		//保证金
		BigDecimal provisionFee = new BigDecimal("0");
		//逾期费
		BigDecimal lateFee = new BigDecimal("0");
		BigDecimal totalAmount  = new BigDecimal("0");;
		//减免后不含进位费总还款金额
		BigDecimal sum1 = new BigDecimal("0");
		//减免后含进位费的总还款金额
		BigDecimal sum2 = new BigDecimal("0");;
		//减免后含税费的进位费
		BigDecimal carryFee = new BigDecimal("0");;
		//减免后不含税费的进位费
		BigDecimal carryFeeUncludeVAt = new BigDecimal("0");

		//先把每期的服务费加起来再乘打折比例   再向上进位
		BigDecimal serviceFee = new BigDecimal("0");
		BigDecimal tempServiceFee = new BigDecimal("0");
		//最后一期的dueindex
		Integer dueIndex1 = dues.get(dues.size() - 1).getDueIndex();
		//拿出每一期的费用
		for (Dues due : dues) {
			Integer dueIndex = due.getDueIndex();
			String dueType = due.getDueType();
			if (dueType.equals("Service_Fee")){
				//log.info("总的服务费"+due.getAmount());
				if(isOverDue(due.getDueDate())){
					due.setRemainingAmount(new BigDecimal(due.getRemainingAmount()).multiply(new BigDecimal("0.8")).setScale(0,RoundingMode.UP).longValue());
				}
				serverSum = serverSum.add(new BigDecimal(due.getRemainingAmount()));
			}else if (dueType.equals("Service_Fee_VAT")) {
				//log.info("总的服务费税费"+due.getAmount());
				if(isOverDue(due.getDueDate())){
					due.setRemainingAmount(new BigDecimal(due.getRemainingAmount()).multiply(new BigDecimal("0.8")).setScale(0,RoundingMode.UP).longValue());
				}
				serverVatSum = serverVatSum.add(new BigDecimal(due.getRemainingAmount()));
			}else if (dueType.equals("Principal")){
				//2、计算Amount funded
				principalSum = principalSum.add(new BigDecimal(due.getRemainingAmount()));
				//log.info("总的本金" + principalSum);
			}else if("Fund_Interest".equalsIgnoreCase(dueType)){
				//资金方利息
				if(isOverDue(due.getDueDate())){
					due.setRemainingAmount(new BigDecimal(due.getRemainingAmount()).multiply(new BigDecimal("0.8")).setScale(0,RoundingMode.UP).longValue());
				}
				fundInterest = fundInterest.add(new BigDecimal(due.getRemainingAmount()));
			}else if("Provision_fee".equalsIgnoreCase(dueType)){
				//保证金
				provisionFee = provisionFee.add(new BigDecimal(due.getRemainingAmount()));
			}else if(dueType.contains("Late_Fee")){
				//保证金
				lateFee = lateFee.add(new BigDecimal(due.getRemainingAmount()));
			}else if("Carrying_Fee_VAT".equalsIgnoreCase(dueType)){
				due.setRemainingAmount(0L);
				if(dueIndex1==due.getDueIndex()){
					//先算出优惠后的服务费
					serverSumDiscounts = NumberUtil.round((serverSum.add(serverVatSum).add(fundInterest)),0).add(provisionFee) ;
					log.info("优惠后的金额:{}" ,serverSumDiscounts );
					log.info("总本金:{}" ,principalSum );
					//优惠后的服务费(资金方利息+平台服务费)+本金+服务费
					sum1 = serverSumDiscounts.add(principalSum).add(lateFee);
					//(优惠后的服务费+本金)向上进位千位符得到的值
					sum2= NumberUtil.round(sum1.setScale(-3, RoundingMode.UP),0);
					//再用进位后得到的值-（优惠后的服务费+本金）=进位费
					carryFee= sum2.subtract(sum1);
					//进位费不含税费
					carryFeeUncludeVAt= carryFee.divide(new BigDecimal("1.11"),0,RoundingMode.HALF_UP);
					due.setRemainingAmount(carryFee.subtract(carryFeeUncludeVAt).longValue());
				}
			}else if("Carrying_Fee".equalsIgnoreCase(dueType)){
				due.setRemainingAmount(0L);
				if(dueIndex1==due.getDueIndex()){
					due.setRemainingAmount(carryFeeUncludeVAt.longValue());

				}
			}

		}
//		//先算出优惠后的服务费
//		serverSumDiscounts = NumberUtil.round((serverSum.add(serverVatSum).add(fundInterest)),0).add(provisionFee) ;
//		log.info("优惠后的金额:{}" ,serverSumDiscounts );
//		log.info("总本金:{}" ,principalSum );
//		//优惠后的服务费(资金方利息+平台服务费)+本金+服务费
//		 sum1 = serverSumDiscounts.add(principalSum);
//		//(优惠后的服务费+本金)向上进位千位符得到的值
//		 sum2= NumberUtil.round(sum1.setScale(-3, RoundingMode.UP),0);
//		//再用进位后得到的值-（优惠后的服务费+本金）=进位费
//		 carryFee= sum2.subtract(sum1);
		log.info("进位费是:{}",carryFee );
		totalAmount = NumberUtil.round(sum1.add(carryFee),0);
		log.info("优惠后总的还款金额:{}",totalAmount);
		HashMap<String, BigDecimal> resultMap = new HashMap<String, BigDecimal>();
		resultMap.put("totalAmount",totalAmount);
		resultMap.put("interestFee",serverSumDiscounts);
		resultMap.put("principalSum",principalSum);
		resultMap.put("carryFee",carryFee);
		resultMap.put("lateFee",lateFee);

		//资金方利息
		resultMap.put("fundInterest",fundInterest);
		//保证金额
		resultMap.put("provisionfee",provisionFee);
		//平台服务费（需包含平台服务费Vat）
		resultMap.put("serviceFeeINcludeVat",serverSum.add(serverVatSum));
		//进位费需包进位费vat）
		resultMap.put("Vat",serverVatSum.add(carryFee.subtract(carryFeeUncludeVAt)));
		//返回提前结清试算
		RepayByadvanceVo repayByadvanceVo = new RepayByadvanceVo();
		repayByadvanceVo.setMap(resultMap);
		repayByadvanceVo.setDues(dues);

		return repayByadvanceVo;

	}

	public boolean  isOverDue(LocalDate localDate){
//		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//		log.info("now{},localDate{}",LocalDate.now().format(dateTimeFormatter),localDate.format(dateTimeFormatter));
		return LocalDate.now().compareTo(localDate)<=0 ? true:false;
	}

	@Override
	public TryCPlan repayByCplan(TryCPlanInfo tryCPlanInfo) {
		// TODO Auto-generated method stub
		//校验是否能试算  
		//每笔订单的最低还款金额 和最大还款金额
//		String loanNumber = "CL22042620391709453787846";
		
		List<Map<String, Object>> tryCList = tryCPlanInfo.getTryCList();
		Collections.sort(tryCList, new Comparator<Map<String, Object>>() {

			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				// TODO Auto-generated method stub
				int compareTo = o2.get("loanNumber").toString().substring(0, 1).compareTo(o1.get("loanNumber").toString().substring(0, 1));
				if(compareTo==0) {
					compareTo = o1.get("loanNumber").toString().compareTo(o2.get("loanNumber").toString());
				}
				return compareTo;
			}
		});
		TryCPlan tryCPlan = new TryCPlan();
		List<LoanTryCPlan> loanTryCPlanList = new ArrayList<LoanTryCPlan>();
		tryCPlan.setTotalMinAmount(new BigDecimal(0));
		tryCPlan.setTotalMaxAmount(new BigDecimal(0));
		
		tryCPlan.setTotalInterest(new BigDecimal(0));
		tryCPlan.setTotalPrincipal(new BigDecimal(0));
		tryCPlan.setTotalCarryingFee(new BigDecimal(0));
		tryCPlan.setNextTotalAmount(new BigDecimal(0));
		tryCPlan.setTotalplanAmount(new BigDecimal(0));
		
		tryCPlan.setLoanTryCPlanList(loanTryCPlanList);
		
		//查询税率
		VatWht vatWht = getVatWht();
		BigDecimal vat = vatWht.getVAT();
		
		for (int i = 0; i < tryCList.size(); i++) {
			LoanTryCPlan loanTryCPlan = new LoanTryCPlan();
			log.info("tryCList:{}", tryCList.size());
			Map<String, Object> map = tryCList.get(i);
			BigDecimal repayDiscount = null;
			if(map.get("discountLFratio")!=null&&StringUtils.isNotEmpty(map.get("discountLFratio").toString())) {
				repayDiscount = new BigDecimal(1).subtract(new BigDecimal(map.get("discountLFratio").toString()));
				loanTryCPlan.setDiscountLFratio(new BigDecimal(map.get("discountLFratio").toString()));
				loanTryCPlan.setRepayDiscount(repayDiscount);
			}else {
				repayDiscount = new BigDecimal(0.5);
				loanTryCPlan.setRepayDiscount(repayDiscount);
			}
			String loanNumber = map.get("loanNumber").toString();
			loanTryCPlan.setLoanNumber(loanNumber);
			List<Dues> duesList = fingUncloseDues(loanNumber);
		
			//只处理逾期的期数  取最近一期
			Integer dueIndex = duesList.get(0).getDueIndex();
			duesList = duesList.stream().filter(x->x.getDueDate().compareTo(LocalDate.now())<0&&x.getDueIndex()==dueIndex).collect(Collectors.toList());
			log.info("排序后{}",duesList);
			loanTryCPlan.setBeforeDeductDues(duesList);
			calMaxAndMin(loanTryCPlanList, loanNumber, loanTryCPlan, duesList,repayDiscount);
			//所有订单的最小还款金额  最大还款金额
			tryCPlan.setTotalMinAmount(tryCPlan.getTotalMinAmount().add(loanTryCPlanList.get(i).getMinAmount()));
			tryCPlan.setTotalMaxAmount(tryCPlan.getTotalMaxAmount().add(loanTryCPlanList.get(i).getMaxAmount()));
		}
		//所有订单的最小还款金额  最大还款金额   
		loanTryCPlanList = tryCPlan.getLoanTryCPlanList();
		log.info("ryCPlanInfo.getAmount()==null:{},tryCPlanInfo.getAmount().trim().length()==0:{}", tryCPlanInfo.getAmount()==null,tryCPlanInfo.getAmount().trim().length()==0);
		if(tryCPlanInfo.getAmount()==null||tryCPlanInfo.getAmount().trim().length()==0||Integer.valueOf(tryCPlanInfo.getAmount().toString())<tryCPlan.getTotalMinAmount().intValue()) {
			System.out.println("tryCPlan.getTotalMinAmount():"+tryCPlan.getTotalMinAmount());
			tryCPlanInfo.setAmount(tryCPlan.getTotalMinAmount().toString());
		}
		//比例
		BigDecimal subtractSum = new BigDecimal(1);
		//还款金额
		BigDecimal subtractCustomSum = new BigDecimal(tryCPlanInfo.getAmount());
		for (int i = 0; i < loanTryCPlanList.size(); i++) {
			LoanTryCPlan loanTryCPlan = loanTryCPlanList.get(i);
			List<Dues> beforeDeductDues = loanTryCPlan.getBeforeDeductDues();
			RepayDuesExt repayDuesExt = new RepayDuesExt();
			loanTryCPlan.setDeductDues(repayDuesExt);
			RepayDues nextDues = new RepayDues();
			loanTryCPlan.setNextDues(nextDues);
			//查询费率以及借款金额
			QueryWrapper<LoanDetail> queryDetail = new QueryWrapper<>();
			queryDetail.eq("loan_number", loanTryCPlan.getLoanNumber());
			LoanDetail loanDetail = detailMapper.selectOne(queryDetail);
			//查询费率
			QueryWrapper<LoanDetailExtend> queryExtend = new QueryWrapper<>();
			queryExtend.eq("loan_number", loanTryCPlan.getLoanNumber());
			LoanDetailExtend loanExtend = detailExtendMapper.selectOne(queryExtend);
			Map<String,Object> extendMap =  new HashMap<String, Object>();
	
			if(loanDetail.getSecondProdCode().contains("v2")) {
				String extend = loanExtend.getExtend();
				 extendMap = JSONObject.parseObject(extend, Map.class);
			}
			//每笔订单所占的还款比例  以及分摊到的还款金额
			if(i<loanTryCPlanList.size()-1) {
				BigDecimal divide = loanTryCPlan.getMinAmount().divide(tryCPlan.getTotalMinAmount(), 6, RoundingMode.UP);
				loanTryCPlan.setProportion(divide);
				log.info("divide:{}", divide);
				subtractSum = subtractSum.subtract(loanTryCPlan.getProportion());
				BigDecimal custom = new BigDecimal(tryCPlanInfo.getAmount()).multiply(loanTryCPlan.getProportion()).setScale(-3, RoundingMode.DOWN);
				loanTryCPlan.setCustomRepaymAmount(custom);
				subtractCustomSum = subtractCustomSum.subtract(custom);
			}else if(i==loanTryCPlanList.size()-1) {
				loanTryCPlan.setProportion(subtractSum);
				loanTryCPlan.setCustomRepaymAmount(subtractCustomSum);
			}
			Long customRepay = (Long)loanTryCPlan.getCustomRepaymAmount().longValue();
			// 根据上面计算出来的每笔订单的还款金额进行 抵扣
			deductDues(loanTryCPlan.getRepayDiscount(), beforeDeductDues, repayDuesExt, nextDues, customRepay);
			//试算展期的还款计划
			//处理费率  天数 
			BigDecimal fundInRate = null;
			BigDecimal serviceRate = null;
			BigDecimal provisionfeeRate = null;
			BigDecimal costFeePrincipal = new BigDecimal(loanDetail.getAmount());
			BigDecimal tenor = null;
			LocalDate now = LocalDate.now();
			LocalDate salaryDate = LocalDate.of(now.getYear(), now.getMonthValue(), Integer.parseInt(loanExtend.getSalaryDay()));
			if(salaryDate.compareTo(now)<=0) {
				salaryDate = salaryDate.plusMonths(1);
				if((salaryDate.toEpochDay()-now.toEpochDay())<=15) {
					salaryDate = salaryDate.plusMonths(1);
				}
			}else if(salaryDate.compareTo(now)>0) {
				if((salaryDate.toEpochDay()-now.toEpochDay())<=15) {
					salaryDate = salaryDate.plusMonths(1);
				}
			}
			DateTimeFormatter ofPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			tenor = new BigDecimal(salaryDate.toEpochDay()-now.toEpochDay());
			String salaryStr = salaryDate.format(ofPattern);
			LocalDate dueDateLoc = beforeDeductDues.get(0).getDueDate();
			dueDateLoc = dueDateLoc.plusMonths(1);
			String  dueDate =dueDateLoc.format(ofPattern);
			if(loanDetail.getSecondProdCode().contains("v1")) {
				fundInRate = loanDetail.getFundInterestRate();
				serviceRate = loanDetail.getServiceFeeRate();
				if(loanDetail.getSecondProdCode().contains("short")) {
					dueDate = salaryStr;
				}else if(loanDetail.getSecondProdCode().contains("long")){
					tenor = new BigDecimal(1);
				}
				if(loanDetail.getLoanNumber().startsWith("PL")) {
					provisionfeeRate = loanExtend.getFundProvisionRate();
				}
			}else if(loanDetail.getSecondProdCode().contains("v2")) {
				fundInRate = new BigDecimal(extendMap.get("fundInterestRate").toString());
				if(loanDetail.getSecondProdCode().contains("short")) {
					dueDate = salaryStr;
					serviceRate = new BigDecimal(extendMap.get("serviceFeeRate").toString());
				}else if(loanDetail.getSecondProdCode().contains("long")){
					serviceRate = loanDetail.getServiceFeeRate();
					tenor = new BigDecimal(1);
				}
				if(loanDetail.getLoanNumber().startsWith("PL")) {
					provisionfeeRate = new BigDecimal(loanExtend.getProvisionRate());
				}
			}
//			BigDecimal fundInRate = null;
//			BigDecimal serviceRate = null;
//			BigDecimal provisionfeeRate = null;
//			BigDecimal costFeePrincipal = new BigDecimal(loanDetail.getAmount());
//			BigDecimal tenor = null;
			//资金方利息   
			BigDecimal fundInterest = costFeePrincipal.multiply(fundInRate).multiply(tenor);
			//平台服务费
			BigDecimal serviceFee = costFeePrincipal.multiply(serviceRate).multiply(tenor).setScale(0, RoundingMode.HALF_UP);
			//平台服务费税费
			BigDecimal serviceFeeVat = serviceFee.multiply(vat).setScale(0, RoundingMode.HALF_UP);
			BigDecimal ProvisionFee = null;
			//不含进位费的金额
			log.info("fundInterest:{},serviceFee:{},serviceFeeVat:{},ProvisionFee:{}", fundInterest,serviceFee,serviceFeeVat,ProvisionFee);
			log.info("nextDues.getPrincipal():{}", nextDues.getPrincipal());
			BigDecimal  amountNoCarry = fundInterest.add(serviceFee).add(serviceFeeVat).add(nextDues.getPrincipal());
			if(provisionfeeRate!=null){
				ProvisionFee = costFeePrincipal.multiply(provisionfeeRate).multiply(tenor);
				amountNoCarry = amountNoCarry.add(ProvisionFee);
				tryCPlan.setTotalInterest(ProvisionFee);
				nextDues.setProvisionFee(ProvisionFee);
			}
			BigDecimal temp = amountNoCarry;
			//进位费
			BigDecimal carryIncludeVat = amountNoCarry.setScale(-3, RoundingMode.UP).subtract(temp);
			//进位费税费
			BigDecimal carryNoVat = carryIncludeVat.divide((new BigDecimal(1).add(vat)), 0, RoundingMode.HALF_UP);
			BigDecimal carryVat = carryIncludeVat.subtract(carryNoVat);
			nextDues.setFundInterest(fundInterest);
			nextDues.setServiceFee(serviceFee);
			nextDues.setServiceFeeVAT(serviceFeeVat);
			nextDues.setCarryingFeeIncludeVAT(carryIncludeVat);
			nextDues.setCarryingFee(carryNoVat);
			nextDues.setCarryingFeeVAT(carryVat);
			nextDues.setDueDate(dueDate);
			
			
			tryCPlan.setTotalInterest(tryCPlan.getTotalInterest().add(fundInterest)
					.add(serviceFee).add(serviceFeeVat));
			tryCPlan.setTotalPrincipal(tryCPlan.getTotalPrincipal().add(nextDues.getPrincipal()));
			tryCPlan.setTotalCarryingFee(tryCPlan.getTotalCarryingFee().add(nextDues.getCarryingFeeIncludeVAT()));
			tryCPlan.setNextTotalAmount(tryCPlan.getNextTotalAmount().add(amountNoCarry.setScale(-3, RoundingMode.UP)));
			tryCPlan.setTotalplanAmount(tryCPlan.getTotalplanAmount()
					.add(loanTryCPlan.getCustomRepaymAmount()).add(amountNoCarry.setScale(-3, RoundingMode.UP)));
			
		}
		log.info("tryCPlan{}",JSONObject.toJSONString(tryCPlan));
		return tryCPlan;
	}

	/**
	 * 查询vat和wht费率
	 * @return
	 */
	public VatWht getVatWht() {
		String vatWhtConfig = upfrontDuesMapper.getVatWht("vat_tax_rate_configuration");
		return JSON.parseObject(vatWhtConfig, VatWht.class);
	}

	/**通过订单号 查询到有序的duelist
	 * @param loanNumber 订单号
	 * @return
	 */
	public List<Dues> fingUncloseDues(String loanNumber) {
		QueryWrapper<Dues> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("loan_id", loanNumber);
		queryWrapper.eq("is_close", 0);
		List<Dues> duesList = duesMapper.selectList(queryWrapper);
		//对查询出来的duelist排序
		log.info("排序前{}",duesList);
//			Collections.sort(duesList, (o1,o2)->o1.getRepayIndex().compareTo(o2.getRepayIndex()));
		Collections.sort(duesList, new Comparator<Dues>() {
			@Override
			public int compare(Dues o1, Dues o2) {
				// TODO Auto-generated method stub
				int compareTo = o1.getDueIndex().compareTo(o2.getDueIndex());
				if(compareTo==0) {
					compareTo = o1.getRepayIndex().compareTo( o2.getRepayIndex());
				}
				return compareTo;
			}
		});
		return duesList;
	}

	public void deductDues(BigDecimal repayDiscount, List<Dues> beforeDeductDues, RepayDuesExt repayDuesExt,
			RepayDues nextDues, Long customRepay) {
		for(Dues dues: beforeDeductDues) {
			if(dues.getDueType().equals("Admin_Fee")) {

				if(customRepay.compareTo(dues.getRemainingAmount())>0) {
					repayDuesExt.setAdminFee(new BigDecimal(dues.getRemainingAmount()));
					customRepay = customRepay-dues.getRemainingAmount();
				}else {
					repayDuesExt.setAdminFee(new BigDecimal(customRepay));
					break;
				}
			}else if(dues.getDueType().equals("Admin_Fee_VAT")) {
				if(customRepay.compareTo(dues.getRemainingAmount())>0) {
					repayDuesExt.setAdminFeeVAT(new BigDecimal(dues.getRemainingAmount()));
					customRepay = customRepay-dues.getRemainingAmount();
				}else {
					repayDuesExt.setAdminFeeVAT(new BigDecimal(customRepay));
					break;
				}
				
			}else if(dues.getDueType().equals("Provision_fee")) {
				if(customRepay.compareTo(dues.getRemainingAmount())>0) {
					repayDuesExt.setProvisionFee(new BigDecimal(dues.getRemainingAmount()));
					customRepay = customRepay-dues.getRemainingAmount();
				}else {
					repayDuesExt.setFundInterest(new BigDecimal(customRepay));
					break;
				}
				
			}else if(dues.getDueType().equals("Fund_Interest")) {
				if(customRepay.compareTo(dues.getRemainingAmount())>0) {
					repayDuesExt.setFundInterest(new BigDecimal(dues.getRemainingAmount()));
					customRepay = customRepay-dues.getRemainingAmount();
				}else {
					repayDuesExt.setFundInterest(new BigDecimal(customRepay));
					break;
				}
				
			}else if(dues.getDueType().equals("Service_Fee")) {
				if(customRepay.compareTo(dues.getRemainingAmount())>0) {
					repayDuesExt.setServiceFee(new BigDecimal(dues.getRemainingAmount()));
					customRepay = customRepay-dues.getRemainingAmount();
				}else {
					repayDuesExt.setServiceFee(new BigDecimal(customRepay));
					break;
				}	
			}else if(dues.getDueType().equals("Service_Fee_VAT")) {
				if(customRepay.compareTo(dues.getRemainingAmount())>0) {
					repayDuesExt.setServiceFeeVAT(new BigDecimal(dues.getRemainingAmount()));
					customRepay = customRepay-dues.getRemainingAmount();
				}else {
					repayDuesExt.setServiceFeeVAT(new BigDecimal(customRepay));
					break;
				}
				
			}else if(dues.getDueType().equals("Late_Fee")) {
				BigDecimal lateFee= new BigDecimal(dues.getRemainingAmount()).multiply(repayDiscount)
				.setScale(0, RoundingMode.HALF_UP);
				if(customRepay.compareTo(lateFee.longValue())>0) {
					repayDuesExt.setLateFee(lateFee);
					customRepay = customRepay-lateFee.longValue();
				}else {
					repayDuesExt.setLateFee(new BigDecimal(customRepay));
					break;
				}
			}else if(dues.getDueType().equals("Late_Fee_VAT")) {
				BigDecimal lateFeeVat= new BigDecimal(dues.getRemainingAmount()).multiply(repayDiscount)
						.setScale(0, RoundingMode.HALF_UP);
//				long lateFeeLong = beforeDeductDues.stream().filter(t->t.getDueType()
//						.equalsIgnoreCase("Late_Fee")).collect(Collectors.toList()).get(0).getRemainingAmount();
//						BigDecimal lateFee = new BigDecimal(lateFeeLong).multiply(repayDiscount).setScale(0,RoundingMode.HALF_UP);
//						BigDecimal lateFeeVat = lateFee.multiply(new BigDecimal(0.11)).setScale(0,RoundingMode.HALF_UP);
						if(customRepay.compareTo(lateFeeVat.longValue())>0) {
							repayDuesExt.setLateFeeVAT(lateFeeVat);
							customRepay = customRepay-lateFeeVat.longValue();
						}else {
							repayDuesExt.setLateFeeVAT(new BigDecimal(customRepay));
							break;
						}

			}else if(dues.getDueType().equals("Principal")) {
				if(customRepay.compareTo(dues.getRemainingAmount())>0) {
					repayDuesExt.setPrincipal(new BigDecimal(dues.getRemainingAmount()));
					customRepay = customRepay-dues.getRemainingAmount();
				}else {
					repayDuesExt.setPrincipal(new BigDecimal(customRepay));
					if(nextDues!=null){
						nextDues.setPrincipal(new BigDecimal(dues.getRemainingAmount()).subtract(new BigDecimal(customRepay)));
					}
					break;
				}
			}
		}
	}
	//计算C计划中每笔订单最小最大值
	/**
	 * 
	 * @param loanTryCPlanList
	 * @param loanNumber
	 * @param loanTryCPlan
	 * @param duesList
	 * @param repayDiscount   应还的逾期费比例（应还逾期费=应还逾期费*repayDiscount）
	 */
	public void calMaxAndMin(List<LoanTryCPlan> loanTryCPlanList, String loanNumber, LoanTryCPlan loanTryCPlan,
			List<Dues> duesList,BigDecimal repayDiscount) {
		//逾期费  逾期费税费
		long[] lateFeeArr = {0};
		long[] lateFeeVatArr = {0};
		long[] principalArr = {0};
		//计算最低还款金额
		duesList.forEach(x->{
			if(x.getDueType().equalsIgnoreCase("Late_Fee")) {
				lateFeeArr[0] = x.getRemainingAmount();
			}else if(x.getDueType().equalsIgnoreCase("Late_Fee_VAT")) {
				lateFeeVatArr[0] = x.getRemainingAmount();
			}else if(x.getDueType().equalsIgnoreCase("Principal")) {
				principalArr[0] = x.getRemainingAmount();
			}
		});
		//把逾期期数的资金方利息 平台服务费 服务费税费  保证金
		long sum = duesList.stream().filter(x->{
//				System.out.println("x.getDueType()"+x.getDueType());
			return 	!(x.getDueType().equals("Late_Fee")||x.getDueType().equals("Late_Fee_VAT")||x.getDueType().equals("Principal")
					||x.getDueType().equals("Carrying_Fee_VAT")||x.getDueType().equals("Carrying_Fee"));
		}).collect(Collectors.toList()).stream().mapToLong(Dues::getRemainingAmount).sum();

		log.info("sum:{}",sum);
		// C计划的A公式
		BigDecimal aSum = new BigDecimal(sum).add(new BigDecimal(lateFeeArr[0]).multiply(repayDiscount)
				.multiply(new BigDecimal(1.11))).setScale(-3, RoundingMode.UP);
		// C计划的B公式
		BigDecimal bSum =  new BigDecimal(sum).add(new BigDecimal(lateFeeArr[0])).add(new BigDecimal(lateFeeVatArr[0]))
				.add( new BigDecimal(principalArr[0]));
		//还款的最大值为 资金方利息+平台服务费+本金+保证金+减免后的逾期费以及逾期费税费
		BigDecimal maxrepay = new BigDecimal(sum).add(new BigDecimal(lateFeeArr[0]).multiply(repayDiscount)
				.multiply(new BigDecimal(1.11))).add(new BigDecimal(principalArr[0])).setScale(-3, RoundingMode.UP);
		bSum = bSum.multiply(new BigDecimal(0.35)).setScale(-3, RoundingMode.UP);
		BigDecimal abMax = aSum.compareTo(bSum)>=0?aSum:bSum;
		int cSum = 0;
		if(loanNumber.startsWith("CL")) {
//			cSum = 250000;
			//maucash 5.4.3 去掉了cl订单最低25k的校验
			cSum =0;
		}else if(loanNumber.startsWith("PL")) {
			cSum = 50000;
		}
		BigDecimal minRepay = abMax.compareTo(new BigDecimal(cSum))>=0?abMax:new BigDecimal(cSum);
		log.info("A:{},B:{},C:{},最小还款金额:{},最大值:{}",aSum.longValue(),bSum.longValue()
				,new BigDecimal(cSum).longValue(),minRepay.longValue(),maxrepay.longValue());
		loanTryCPlan.setMinAmount(minRepay);
		loanTryCPlan.setMaxAmount(maxrepay);
		loanTryCPlanList.add(loanTryCPlan);
	}
	// 876120403    677534   CL22090113412776124264961
	//8567093924     679835
	@Override
	public TryEPlanVo repayByEplan(List<LoanTryEPlan> ePlanlist) {
		// TODO Auto-generated method stub


		TryEPlanVo tryEPlanVo = new TryEPlanVo();
		List<LoanEPlanVo> list = new ArrayList<>();

		//查询vat费率
		VatWht vatWht = getVatWht();
		BigDecimal vatRate = vatWht.getVAT();
		for (LoanTryEPlan loanTryEPlan:ePlanlist) {
			LoanEPlanVo loanEPlanVo = new LoanEPlanVo();
			loanEPlanVo.setLoanNumber(loanTryEPlan.getLoanNumber());
			list.add(loanEPlanVo);

			//逾期费减免比例

			//查询当前订单未还资金方利息
			Long unPayInterestFunder =0L;
			//查询当前订单未还平台服务费
			Long unPayPlatformfee =0L;
			//查询当前订单未还保证金
			Long unPayProvisionFee =0L;
			//未还逾期费,
			Long unPayLateFee =0L;
			/**
			 * 因E计划返回给催收前端的unPayLateFee（应还*需要还的比例之和.longValue()）和抵扣时应还逾期费（应还*需要还的比例四舍五入）不一样
			 *逾期未还的逾期费
			 */
			//未还逾期费,
			Long unPayLateFeeDeduct =0L;
			//未还折后逾期费,
			Long unPayLateFeeByDiscount =0L;
			//查询当前订单未还款本金
			Long unPayAmountFund =0L;
			//查询当前订单未还款总金额（所有期）T
			Long unPayTotalOutstandingFee =0L;
			//最近一期未还款金额(不包含税费)
			Long unPayFirstDuesAmount =0L;
			List<Dues> fingUncloseDues = fingUncloseDues(loanTryEPlan.getLoanNumber());
			unPayTotalOutstandingFee = fingUncloseDues.stream().mapToLong(Dues::getRemainingAmount).sum();

			//逾期的资金方利息
			Long overdueUnPayInterestFunder =0L;
			//逾期的平台服务费（包含税费）
			Long overdueUnPayPlatformfee =0L;
			//逾期的保证金
			Long overdueUnPayProvisionFee =0L;
			//判断是否逾期   当前时间和duedate时间
			LocalDate now = LocalDate.now();
			for (Dues  dues:fingUncloseDues) {
				//最近一期未还款金额(不包进位费，进位费税费)
					if(dues.getDueIndex()==fingUncloseDues.get(0).getDueIndex()&&!(dues.getDueType().equalsIgnoreCase("Carrying_Fee")
							||dues.getDueType().equalsIgnoreCase("Carrying_Fee_VAT"))){
						unPayFirstDuesAmount = unPayFirstDuesAmount+dues.getRemainingAmount();
					}
					//逾期期数的资金方利息，平台服务费，逾期费，本金（不考虑部分还款）

					//当前订单未还款金额
					if(dues.getDueType().equalsIgnoreCase("Fund_Interest")){
						unPayInterestFunder =unPayInterestFunder +dues.getRemainingAmount();
						//判断是否逾期
						if(dues.getDueDate().compareTo(now)<0){
							DateTimeFormatter dateFormater = DateTimeFormatter.ofPattern("yyyyMMdd");
							String nowFormat = now.format(dateFormater);
							String DueDateFormat = dues.getDueDate().format(dateFormater);
							log.info("当前时间nowFormat:{}，还款时间duedate：{}",nowFormat,DueDateFormat);
							overdueUnPayInterestFunder = overdueUnPayInterestFunder + dues.getRemainingAmount();
						}
					}else if(dues.getDueType().equalsIgnoreCase("Service_Fee")||
							dues.getDueType().equalsIgnoreCase("Service_Fee_VAT")){
						unPayPlatformfee = unPayPlatformfee +dues.getRemainingAmount();
						//判断是否逾期
						if(dues.getDueDate().compareTo(now)<0){
							overdueUnPayPlatformfee = overdueUnPayPlatformfee + dues.getRemainingAmount();
						}
					}else if(dues.getDueType().equalsIgnoreCase("Provision_fee")){
						unPayProvisionFee = unPayProvisionFee +dues.getRemainingAmount();
						//判断是否逾期
						if(dues.getDueDate().compareTo(now)<0){
							overdueUnPayProvisionFee = overdueUnPayProvisionFee + dues.getRemainingAmount();
						}
					}else if(dues.getDueType().equalsIgnoreCase("Principal")){
						unPayAmountFund = unPayAmountFund +dues.getRemainingAmount();
					}else if(dues.getDueType().contains("Late_Fee")){
						unPayLateFee =unPayLateFee+dues.getRemainingAmount();
						/**
						 * 因E计划返回给催收前端的lateFeeAmount（应还*需要还的比例之和.longValue()）和抵扣时应还逾期费（应还*需要还的比例四舍五入）不一样
						 *逾期未还的逾期费
						 */
						unPayLateFeeDeduct = new BigDecimal(dues.getRemainingAmount()).multiply(new BigDecimal(1).subtract(new BigDecimal(loanTryEPlan.getDiscountLFratio())))
								.setScale(0,RoundingMode.HALF_UP).longValue()+unPayLateFeeDeduct;
					}
			}
			//E计划未还的资金方利息，平台服务费，保证金，本金，未还款金额（未还的所有期）
			loanEPlanVo.setAmountFund(unPayAmountFund);
			loanEPlanVo.setInterestFunder(unPayInterestFunder);
			loanEPlanVo.setPlatformfee(unPayPlatformfee);
			loanEPlanVo.setProvisionFee(unPayProvisionFee);
			loanEPlanVo.setTotalOutstandingFee(unPayTotalOutstandingFee);
			//最低还款金额=资金方利息+平台服务费+保证金额+逾期费+本金（当前应还的第一期  含税费  不含进位费以及进位费税费）
			BigDecimal minimumPaymentbigDecimal = new BigDecimal(unPayFirstDuesAmount)
					.multiply(new BigDecimal(loanTryEPlan.getDisDownPaymentRatio()))
					.setScale(-3, RoundingMode.UP);
			loanEPlanVo.setMinimumPayment(minimumPaymentbigDecimal.longValue());
			BigDecimal lateFeeDiscount = new BigDecimal(unPayLateFee).multiply(new BigDecimal(1).subtract(new BigDecimal(loanTryEPlan.getDiscountLFratio())));
//			BigDecimal unPayLateFeeByDiscountBig = lateFeeDiscount.add(lateFeeDiscount.multiply(vatRate)).setScale(0,RoundingMode.HALF_UP);
			loanEPlanVo.setLateFeeAfterDiscount(lateFeeDiscount.longValue());
			log.info("loanEPlanVo:{}",loanEPlanVo);
			log.info("逾期的资金方利息{}，逾期的平台服务费{}，逾期的保证金{}，折后应还逾期费：{},未还款本金{}",overdueUnPayInterestFunder,
					overdueUnPayPlatformfee, overdueUnPayProvisionFee,loanEPlanVo.getLateFeeAfterDiscount(),loanEPlanVo.getAmountFund());
			long beforeDeductionAmount = overdueUnPayPlatformfee + overdueUnPayProvisionFee + loanEPlanVo.getLateFeeAfterDiscount() + loanEPlanVo.getAmountFund();
			//模拟还款   新订单本金    未还款本金
			//可以抵扣到的费用项
			RepayDuesExt repayDuesExt = new RepayDuesExt();
			//应还的逾期费比例，未还的逾期的第一期，可以抵扣的金额，null,最低还款金额
			deductDues(new BigDecimal(1).subtract(new BigDecimal(loanTryEPlan.getDiscountLFratio())),
					fingUncloseDues.stream().filter(t->t.getDueIndex()==fingUncloseDues.get(0).getDueIndex()).collect(Collectors.toList())
					, repayDuesExt,null ,loanEPlanVo.getMinimumPayment());
			//新订单本金
			loanEPlanVo.setAmountTransfer(beforeDeductionAmount-loanEPlanVo.getMinimumPayment());
			//新订单本金中不含本金的金额转回给adwa
			//抵扣后未还逾期的资金方利息，抵扣后未还逾期的平台服务费，抵扣后未还逾期的保证金，抵扣后未还折后应还逾期费，抵扣后未还的本金
			long overdueUnPayInterestrAfterdeduct =0L;
			long overdueUnPayPlatformfeeAfterdeduct =0L;
			long overdueUnPayProvisionFeeAfterdeduct =0L;
			long overdueUnPayLateFeeAfterdeduct =0L;
			long unPayPrincipalAfterdeduct =0L;
			if(repayDuesExt.getFundInterest()!=null){
				overdueUnPayInterestrAfterdeduct = overdueUnPayInterestFunder-repayDuesExt.getFundInterest().longValue();
			}
			if(repayDuesExt.getServiceFee()!=null){
				overdueUnPayPlatformfeeAfterdeduct = overdueUnPayPlatformfee-repayDuesExt.getServiceFee().longValue()
						-repayDuesExt.getServiceFeeVAT().longValue();
			}
			if(repayDuesExt.getProvisionFee()!=null){
				overdueUnPayProvisionFeeAfterdeduct = overdueUnPayProvisionFee-repayDuesExt.getProvisionFee().longValue();
			}
			if(repayDuesExt.getLateFee()!=null){
				//
				long latefeeAfterdeduct = unPayLateFeeDeduct-repayDuesExt.getLateFee().longValue()
						-repayDuesExt.getLateFeeVAT().longValue();
				overdueUnPayLateFeeAfterdeduct = latefeeAfterdeduct<0L?0L:latefeeAfterdeduct;
			}
			if(repayDuesExt.getLateFee()!=null){
				unPayPrincipalAfterdeduct = loanEPlanVo.getAmountFund()-repayDuesExt.getPrincipal().longValue();
			}
			//新订单本金
			long newAmount = overdueUnPayInterestrAfterdeduct+overdueUnPayPlatformfeeAfterdeduct+overdueUnPayProvisionFeeAfterdeduct
					+overdueUnPayLateFeeAfterdeduct+unPayPrincipalAfterdeduct ;
			loanEPlanVo.setAmountTransfer(newAmount);
			log.info("最低还款金额{}",loanEPlanVo.getMinimumPayment());
			log.info("抵扣后未还的资金方利息{}，抵扣后未还的平台服务费{}，抵扣后未还的保证金{}，抵扣后未还的逾期费{}" +
					"，抵扣后未还的本金{}",overdueUnPayInterestrAfterdeduct,overdueUnPayPlatformfeeAfterdeduct
					,overdueUnPayProvisionFeeAfterdeduct,overdueUnPayLateFeeAfterdeduct,unPayPrincipalAfterdeduct);
			log.info("新订单本金{}",newAmount);
			//老订单未还本金
			loanEPlanVo.setEPlanOldOrderUnpayPrincipal(unPayPrincipalAfterdeduct);
			//转回给adwa(未还平台服务费+未还保证金+未还逾期费)
			loanEPlanVo.setEplanToAdwa(overdueUnPayPlatformfeeAfterdeduct+overdueUnPayProvisionFeeAfterdeduct
					+overdueUnPayLateFeeAfterdeduct);
			//费率
			String secondeProductCode = loanTryEPlan.getLoanNumber().startsWith("PL")? "paylater_long_rs":"NS_maucash_long_rs";
			CalFeeRateConfig calFeeRateConfig = new CalFeeRateConfig();
			//1、费率buildCalFeeRateConfig
			costCalculationServiceImpl.buildCalFeeRateConfig(secondeProductCode,calFeeRateConfig,5);
			TryInfo tryInfo = new TryInfo();
			tryInfo.setSecondProductCode(secondeProductCode);
			tryInfo.setTenor(loanTryEPlan.getTenor());
			tryInfo.setLoanNumber(loanTryEPlan.getLoanNumber());
			//使用未还款本金计算费用（多期逾期，新订单本金>未还款本金）
			tryInfo.setAmount(loanEPlanVo.getEPlanOldOrderUnpayPrincipal());
			UpfrontDuesVo upfrontDuesVo = new UpfrontDuesVo();
			upfrontDuesVo.setUpfrontFeeVO(new UpfrontFeeVO());
			upfrontDuesVo.setDuesVo(new DuesVo());
			upfrontDuesVo.setDdmRelation(new DdmRelation());
			DuesVo duesVo = upfrontDuesVo.getDuesVo();
			upfrontDuesVo.setEPlanOldOrderUnpayPrincipal(loanEPlanVo.getAmountTransfer());
			List<String> duedateList = new ArrayList<String>();
			duesVo.setDuedateList(duedateList);
			//2、处理DDM  handleDdm
			costCalculationServiceImpl.handleDdm(tryInfo,upfrontDuesVo,duedateList,null);
			//3、还款试算
			costCalculationServiceImpl.calRepayDues(tryInfo, calFeeRateConfig, vatWht, upfrontDuesVo);
			loanEPlanVo.setFirstNewDueDate(upfrontDuesVo.getDdmRelation().getNextDueDate());
			loanEPlanVo.setFirstNewInstallment(upfrontDuesVo.getDuesVo().getRepaymentAmountDdm().longValue());
			log.info("E计划试算费率calFeeRateConfig:{}",calFeeRateConfig);
			log.info("E计划试算返回upfrontDuesVo:{}",JSONObject.toJSONString(upfrontDuesVo));
			loanEPlanVo.setUpfrontDuesVo(upfrontDuesVo);
		}

		//新订单  dues
		log.info("List<LoanEPlanVo>:{}",list);
		tryEPlanVo.setLoanEPlanVoList(list);
		long sumMinimumPayment = list.stream().mapToLong(LoanEPlanVo::getMinimumPayment).sum();
		tryEPlanVo.setTotalMinimumPayment(sumMinimumPayment);
		log.info("tryEPlanVo:{}",JSONObject.toJSONString(tryEPlanVo));
		return tryEPlanVo;
	}

	@Override
	public TryFPlanVo repayByFplan(LoanTryFPlan loanTryFPlan) {
		// TODO Auto-generated method stub
		TryFPlanVo tryFPlanVo = new TryFPlanVo();
		log.info("loanTryFPlan:{}",JSONObject.toJSONString(loanTryFPlan));
		List<String> loanNumbers = loanTryFPlan.getLoanNumbers();
		log.info("loanNumbers:{}",loanNumbers);
		//校验传入的优惠比例
		if(StringUtils.isNumeric(loanTryFPlan.getDiscountLFRatio())
				&& compareStringAndNum(loanTryFPlan.getDiscountLFRatio(),"1")){
			tryFPlanVo.setDiscountLFRatio(loanTryFPlan.getDiscountLFRatio());
		}else{
			tryFPlanVo.setDiscountLFRatio("1");
		}
		//校验传入的优惠比例
		if(StringUtils.isNumeric(loanTryFPlan.getDiscountServiceFeeRatio())
				&& compareStringAndNum(loanTryFPlan.getDiscountServiceFeeRatio(),"1")){
			tryFPlanVo.setDiscountServiceFeeRatio(loanTryFPlan.getDiscountServiceFeeRatio());
		}else{
			tryFPlanVo.setDiscountServiceFeeRatio("1");
		}
		//校验传入的优惠比例
		if(StringUtils.isNumeric(loanTryFPlan.getDiscountPrincipalRatio())
				&& compareStringAndNum(loanTryFPlan.getDiscountPrincipalRatio(),"1")){
			tryFPlanVo.setDiscountPrincipalRatio(loanTryFPlan.getDiscountPrincipalRatio());
		}else{
			tryFPlanVo.setDiscountPrincipalRatio("0.3");
		}
		List<LoanFPlanVo> list= new ArrayList<>();

		//查询vat费率
		VatWht vatWht = getVatWht();
		BigDecimal vatRate = vatWht.getVAT();
		for (int i = 0; i < loanNumbers.size(); i++) {
			LoanFPlanVo loanFPlanVo = new LoanFPlanVo();
			List<Dues> dues = fingUncloseDues(loanNumbers.get(i));
			//查询当前订单未还资金方利息（isClose=0）
			Long unPayInterestFunder =0L;
			//查询当前订单未还平台服务费isClose=0）
			Long unPayPlatformfee =0L;
			//查询当前订单未还保证金isClose=0）
			Long unPayProvisionFee =0L;
			//未还逾期费isClose=0）
			Long unPayLateFee =0L;
			//未还本金isClose=0）
			Long unPayPrincipal =0L;
			//未还进位费isClose=0）
			Long unPayCarryFee =0L;
			//每期还款金额  不含逾期费
			long dueAmount =0L;
			for (Dues due : dues) {
				if("Fund_Interest".equalsIgnoreCase(due.getDueType())){
					due.setRemainingAmount(due.getRemainingAmount());
					dueAmount = dueAmount+due.getRemainingAmount();
				}else if(due.getDueType().contains("Service_Fee")){
					due.setRemainingAmount(new BigDecimal(due.getRemainingAmount()).multiply(new BigDecimal(1)
							.subtract(new BigDecimal(tryFPlanVo.getDiscountServiceFeeRatio()))).longValue());
					dueAmount = dueAmount+due.getRemainingAmount();
				}else if ("Provision_fee".equalsIgnoreCase(due.getDueType())){
					due.setRemainingAmount(due.getRemainingAmount());
					dueAmount = dueAmount+due.getRemainingAmount();
				}else if (due.getDueType().contains("Late_Fee")){
					due.setRemainingAmount(new BigDecimal(due.getRemainingAmount()).multiply(new BigDecimal(1)
							.subtract(new BigDecimal(tryFPlanVo.getDiscountLFRatio()))).longValue());
					dueAmount = dueAmount+due.getRemainingAmount();
				}else if (due.getDueType().contains("Principal")){
					due.setRemainingAmount(new BigDecimal(due.getRemainingAmount()).multiply(new BigDecimal(1)
							.subtract(new BigDecimal(tryFPlanVo.getDiscountPrincipalRatio()))).longValue());
					dueAmount = dueAmount+due.getRemainingAmount();
				}else if("Carrying_Fee_VAT".equalsIgnoreCase(due.getDueType())){
					BigDecimal carryFeeIncludeVat = new BigDecimal(dueAmount).setScale(-3, RoundingMode.UP).subtract(new BigDecimal(dueAmount));
					BigDecimal carryFeeVat = carryFeeIncludeVat.multiply(vatRate).setScale(0, RoundingMode.HALF_UP);
					due.setRemainingAmount(carryFeeVat.longValue());
				}else if("Carrying_Fee".equalsIgnoreCase(due.getDueType())){
					BigDecimal carryFeeIncludeVat = new BigDecimal(dueAmount).setScale(-3, RoundingMode.UP).subtract(new BigDecimal(dueAmount));
					log.info("期数：{},carryFeeIncludeVat:{}",due.getDueIndex(),carryFeeIncludeVat);
					BigDecimal carryFee = carryFeeIncludeVat.subtract(carryFeeIncludeVat.multiply(vatRate).setScale(0, RoundingMode.HALF_UP));
					due.setRemainingAmount(carryFee.longValue());
					dueAmount =0L;
				}
			}
			for (Dues due : dues) {
				if("Fund_Interest".equalsIgnoreCase(due.getDueType())){
					unPayInterestFunder = unPayInterestFunder +due.getRemainingAmount();
				}else if(due.getDueType().contains("Service_Fee")){
					unPayPlatformfee = due.getRemainingAmount()+unPayPlatformfee;
				}else if ("Provision_fee".equalsIgnoreCase(due.getDueType())){
					unPayProvisionFee = unPayProvisionFee + due.getRemainingAmount();
				}else if (due.getDueType().contains("Late_Fee")){
					unPayLateFee  = due.getRemainingAmount()+unPayLateFee;
				}else if (due.getDueType().contains("Principal")){
					unPayPrincipal = due.getRemainingAmount()+unPayPrincipal;
				}else if("Carrying_Fee".equalsIgnoreCase(due.getDueType())||"Carrying_Fee_VAT".equalsIgnoreCase(due.getDueType())){
					unPayCarryFee = unPayCarryFee+due.getRemainingAmount();
				}
			}
			loanFPlanVo.setLoanNumber(loanNumbers.get(i));
			loanFPlanVo.setFundInterest(unPayInterestFunder);
			loanFPlanVo.setProvisionFee(unPayProvisionFee);
			loanFPlanVo.setServiceFee(unPayPlatformfee);
			loanFPlanVo.setLateFee(unPayLateFee);
			loanFPlanVo.setPrincipal(unPayPrincipal);
			loanFPlanVo.setCarryFee(unPayCarryFee);
			list.add(loanFPlanVo);
		}
		tryFPlanVo.setLoanFPlanVos(list);
		tryFPlanVo.setTotalFundInterest(list.stream().mapToLong(LoanFPlanVo::getFundInterest).sum());
		tryFPlanVo.setTotalServiceFee(list.stream().mapToLong(LoanFPlanVo::getServiceFee).sum());
		tryFPlanVo.setTotalProvisionFee(list.stream().mapToLong(LoanFPlanVo::getProvisionFee).sum());
		tryFPlanVo.setTotalLateFee(list.stream().mapToLong(LoanFPlanVo::getLateFee).sum());
		tryFPlanVo.setTotalPrincipal(list.stream().mapToLong(LoanFPlanVo::getPrincipal).sum());
		tryFPlanVo.setTotalCarryFee(list.stream().mapToLong(LoanFPlanVo::getCarryFee).sum());
		tryFPlanVo.setTotalAmount(tryFPlanVo.getTotalFundInterest()+tryFPlanVo.getTotalServiceFee()+tryFPlanVo.getTotalProvisionFee()
				+tryFPlanVo.getTotalPrincipal()+tryFPlanVo.getTotalLateFee()+tryFPlanVo.getTotalCarryFee());
		log.info("loanTryFPlan:{}",JSONObject.toJSONString(loanTryFPlan));
		log.info("tryFPlanVo:{}",JSONObject.toJSONString(tryFPlanVo));
		return tryFPlanVo;
	}

	public boolean compareStringAndNum(String stringNum,String targetNum) {

		return new BigDecimal(stringNum).compareTo(new BigDecimal(targetNum))<=0;
	}

	@Override
	public Object repayByPromotionCode(String loanNumber) {
		// TODO Auto-generated method stub
		return null;
	}


}
