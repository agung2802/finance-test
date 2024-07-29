package com.test.api.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.test.api.entity.CalFeeRateConfig;
import com.test.api.entity.Dues;
import com.test.api.entity.ExtendUpfrontRate;
import com.test.api.entity.LoanDetail;
import com.test.api.entity.LoanDetailExtend;
import com.test.api.entity.PromotionCodeDetail;
import com.test.api.entity.RepayDues;
import com.test.api.entity.TransferAmount;
import com.test.api.entity.TryInfo;
import com.test.api.entity.UpfrontExtendFee;
import com.test.api.entity.VatWht;
import com.test.api.entity.Wht;
import com.test.api.entity.vo.DdmRelation;
import com.test.api.entity.vo.DuesVo;
import com.test.api.entity.vo.RebookVo;
import com.test.api.entity.vo.UpfrontDuesVo;
import com.test.api.entity.vo.UpfrontFeeVO;
import com.test.api.mapper.DuesMapper;
import com.test.api.mapper.FinanceTradingMapper;
import com.test.api.mapper.LoanDetailExtendMapper;
import com.test.api.mapper.LoanDetailMapper;
import com.test.api.mapper.UpfrontDuesMapper;
import com.test.api.service.CostCalculationService;

import lombok.extern.slf4j.Slf4j;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 
* 类说明
*/
@Slf4j
@Service
public class CostCalculationServiceImpl implements CostCalculationService {

	@Autowired
	UpfrontDuesMapper upfrontDuesMapper;
	@Autowired
	LoanDetailMapper loanDetailMapper;
	@Autowired
	DuesMapper duesMapper;
	@Autowired
	LoanDetailExtendMapper loanDetailExtendMapper;
	@Autowired
	FinanceTradingMapper  financeTradingMapper;
	@Override
	public UpfrontDuesVo CalculateUpfrontDues(TryInfo tryInfo) {
		log.info("tryInfo{}",tryInfo);
		
		// TODO Auto-generated method stub
		UpfrontDuesVo upfrontDuesVo = new UpfrontDuesVo();
		upfrontDuesVo.setUpfrontFeeVO(new UpfrontFeeVO());
		upfrontDuesVo.setDuesVo(new DuesVo());
		upfrontDuesVo.setDdmRelation(new DdmRelation());
		DuesVo duesVo = upfrontDuesVo.getDuesVo();
		
		List<String> duedateList = new ArrayList<String>();
		duesVo.setDuedateList(duedateList);

		// rebook相关信息
		upfrontDuesVo.setRebookVo(new RebookVo());
		RebookVo rebookVo = upfrontDuesVo.getRebookVo();
		UpfrontFeeVO upfrontFeeVO = upfrontDuesVo.getUpfrontFeeVO();
		//rebook老订单以及新订单相关信息
		if(tryInfo.getRebookOLdLoanNumber()!=null&&StringUtils.isNotEmpty(tryInfo.getRebookOLdLoanNumber())) {
			//初始化新老订单相关金额
			initRebookOldOrder(tryInfo, rebookVo);
		}
		//处理费率
		CalFeeRateConfig calFeeRateConfig = new CalFeeRateConfig();
		if(tryInfo.getLoanNumber()!=null&&tryInfo.getLoanNumber().trim().length()>0) {
			QueryWrapper<LoanDetail> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("loan_number", tryInfo.getLoanNumber());
			LoanDetail loandetail = loanDetailMapper.selectOne(queryWrapper);
			String promotionCode = loandetail.getPromotionCode();
			//
			//资金方id
			upfrontDuesVo.getUpfrontFeeVO().setFundId(loandetail.getFundId().intValue());
			//计算使用优惠码前的应还款，将优惠码置为null
			if(tryInfo.getTryFlag()==1) {
				promotionCode = null;
			}
			String secondProdCode = loandetail.getSecondProdCode();
			Long amount = loandetail.getAmount();
			String period = loandetail.getPeriod();
			// 借款金额 期数  二级产品  
			tryInfo.setAmount(amount);
			tryInfo.setSecondProductCode(secondProdCode);
			tryInfo.setTenor(period);
			buildCalFeeRateConfig(tryInfo.getSecondProductCode(), calFeeRateConfig,loandetail.getFundId().intValue());
			if(promotionCode!=null) {
				//如果有优惠码  按优惠码重置费率
				tryInfo.setPromotionCode(promotionCode);
				buildPromotionRate(tryInfo, calFeeRateConfig,upfrontDuesVo);
				log.info("替换后的优惠码{}",calFeeRateConfig);
			}
			//处理DDM   
			handleDdm(tryInfo, upfrontDuesVo, duedateList, loandetail);
			//判断是否是rebook新订单，是的话初始化老订单
			if(loandetail.getBizType()!=null&&loandetail.getBizType()==1){
				tryInfo.setRebookOLdLoanNumber(loandetail.getPreviousLoan());
				//初始化新老订单相关金额
				initRebookOldOrder(tryInfo, rebookVo);
			}
			//处理保费
			QueryWrapper queryLoanExt =new QueryWrapper();
			queryLoanExt.eq("loan_number", tryInfo.getLoanNumber());
			LoanDetailExtend loanDetailExtend = loanDetailExtendMapper.selectOne(queryLoanExt);
			if(loanDetailExtend.getIsPremium()==1) {
				tryInfo.setInsuranceSelect(true);
				getInsurancePremiumAmount(tryInfo);
			}
		}else{
			//没生成订单用默认的资金方id  0
			buildCalFeeRateConfig(tryInfo.getSecondProductCode(), calFeeRateConfig,0);
			// 判断是否有优惠码     `preferential_type` tinyint(2) NOT NULL COMMENT '优惠类型 ( 1-discount , 2-rate, 3 - decrease )',
			if(tryInfo.getPromotionCode()!=null&&StringUtils.isNoneEmpty(tryInfo.getPromotionCode())) {
				// 替换相应的费率
				buildPromotionRate(tryInfo, calFeeRateConfig,upfrontDuesVo);
			}
			//判断是否有保费  初始化保费
			if(tryInfo.isInsuranceSelect()==true) {
				getInsurancePremiumAmount(tryInfo);
			}
		}
		log.info("calFeeRateConfig:{}", calFeeRateConfig);
		//查询税率
		String vatWhtConfig = upfrontDuesMapper.getVatWht("vat_tax_rate_configuration");
		VatWht vatWht = JSON.parseObject(vatWhtConfig, VatWht.class);
		HashMap<Integer,BigDecimal> fundIdAndWhtMap = new HashMap<>();
		log.info("vatRate{}", vatWht);
		vatWht.getWHT().forEach(x->fundIdAndWhtMap.put(x.getFundId(), x.getRate()));
		fundIdAndWhtMap.entrySet().forEach(x->log.info("key:{}value:{}",x.getKey(),x.getValue()));
		//DDM相关

		//砍头费
		BigDecimal  adminRate=calFeeRateConfig.getUpfrontFeeRate();
		/**
		 * 计算v1产品的保证金 adminfee
		 */
		if(adminRate.compareTo(BigDecimal.ZERO)>0&&calFeeRateConfig.getProvisionRate()!=null&&calFeeRateConfig.getProvisionRate().length()>0) {
			int tenorDay = Integer.valueOf(tryInfo.getTenor().substring(0, tryInfo.getTenor().length()-1));
			int tenorM = Integer.valueOf(tryInfo.getTenor().substring(0, tryInfo.getTenor().length()-1));
			//计算砍头费中的费用
			calV1UpfrontFee(tryInfo,  calFeeRateConfig, vatWht, upfrontDuesVo);
		}else if(tryInfo.getSecondProductCode().contains("v2")) {
			System.out.println("v2产品");
			
			//借款天数  周期
			int tenorDay = Integer.valueOf(tryInfo.getTenor().substring(0, tryInfo.getTenor().length()-1));
			if(tryInfo.getSecondProductCode().contains("short")) {
//				tryInfo.setTenor("30D");
				if(StringUtils.isEmpty(tryInfo.getLoanNumber())) {
					//查询期望还款日 计算借款天数
					tryInfo.setTenor("30D");
					tenorDay = 30;
				}
				tenorDay = Integer.valueOf(tryInfo.getTenor().substring(0, tryInfo.getTenor().length()-1));
				//计算砍头费   借款金额*借款天数*砍头费率/30(试算默认30天  生成订单后根据DDM计算费率)
				upfrontFeeVO.setOriginalUpfrontFee(new BigDecimal(tryInfo.getAmount()).multiply(new BigDecimal(tenorDay))
						.multiply(calFeeRateConfig.getUpfrontFeeRate()).divide(new BigDecimal(30),0,RoundingMode.HALF_UP));
				//如果有减免优惠码
				
			}else if(tryInfo.getSecondProductCode().contains("long")){
				upfrontFeeVO.setOriginalUpfrontFee(new BigDecimal(tryInfo.getAmount())
				.multiply(calFeeRateConfig.getUpfrontFeeRate()));
				if(tryInfo.getRebookOLdLoanNumber()!=null&&StringUtils.isNotEmpty(tryInfo.getRebookOLdLoanNumber())) {
					upfrontFeeVO.setOriginalUpfrontFee(new BigDecimal(rebookVo.getRealAmount())
							.multiply(calFeeRateConfig.getUpfrontFeeRate()));
				}
			}
			//如果是减免优惠码   再原有的砍头费上减免
			if(upfrontDuesVo.getPromotionCodeDetail()!=null &&upfrontDuesVo.getPromotionCodeDetail().getPreferentialType()==3) {
				upfrontFeeVO.setOriginalUpfrontFee(upfrontFeeVO.getOriginalUpfrontFee()
						.subtract(upfrontDuesVo.getPromotionCodeDetail().getUpfrontFee()));
			}
			//计算砍头费中  资金方利息
			calV2UpfrontFee(tryInfo, calFeeRateConfig, vatWht, upfrontDuesVo, tenorDay);
		}
		calRepayDues(tryInfo, calFeeRateConfig, vatWht, upfrontDuesVo);
		//优惠前的砍头费
		//优惠前的每月还款金额
		log.info("tryInfo.getPromotionCode():{}",tryInfo.getPromotionCode()==null);
		log.info("tryInfo.getPromotionCode():{}",tryInfo);

		if(tryInfo.getPromotionCode()!=null&&tryInfo.getTryFlag()!=1) {
			log.info("tryInfo.getPromotionCode():{}",tryInfo.getPromotionCode()==null);
			log.info("计算优惠前的砍头费  每月还款金额 总利息");;
			befDisCalUofrontDues(tryInfo, duesVo, upfrontFeeVO);
		}
		log.info("upfrontDuesVo{}", JSONObject.toJSONString(upfrontDuesVo));
		log.info("tryInfo{}", JSONObject.toJSONString(tryInfo));

		return upfrontDuesVo;
	}
	/**
	 * @param tryInfo
	 */
	private void getInsurancePremiumAmount(TryInfo tryInfo) {
		HashMap<String, Object> findinsuranceConfig = financeTradingMapper.findinsuranceConfig(tryInfo.getSecondProductCode());
		if(findinsuranceConfig==null){
			return;
		}
		int insuranceflg = (int)findinsuranceConfig.get("status");
		if(insuranceflg==1) {
			long insuranceAmount = (long)findinsuranceConfig.get("premium_amount");
			tryInfo.setInsurancePremiumAmount(insuranceAmount);
		}
	}
	/**
	 * 初始化rebook老订单相关费用
	 * @param tryInfo
	 * @param rebookVo
	 */
	public void initRebookOldOrder(TryInfo tryInfo, RebookVo rebookVo) {
		//查询dues
		QueryWrapper<Dues> queryWrapper = new QueryWrapper();
		queryWrapper.eq("loan_id", tryInfo.getRebookOLdLoanNumber());
		List<Dues> dueList = duesMapper.selectList(queryWrapper);
		long sumPrincipal = dueList.stream().filter(x->x.getDueType().equalsIgnoreCase("Principal")).mapToLong(Dues::getAmount).sum();
		rebookVo.setOldApplyAmount(sumPrincipal);
		//未结清duelist
		List<Dues> unCloseDueList = dueList.stream().filter(t->t.getIsClose()==false).collect(Collectors.toList());
		//未结清本金
		long sumUnClosePrincipal = unCloseDueList.stream().filter(x->x.getDueType().equalsIgnoreCase("Principal"))
				.mapToLong(Dues::getAmount).sum();
		rebookVo.setOldPrincipal(sumUnClosePrincipal);
		//未结清资金方利息
		long sumUnCloseFundInterest = unCloseDueList.stream().filter(x->x.getDueType().equalsIgnoreCase("Fund_Interest"))
				.mapToLong(Dues::getAmount).sum();
		rebookVo.setOldFundInterest(sumUnCloseFundInterest);
		//未结清平台服务费
		long sumUnCloseServiceFee = unCloseDueList.stream().filter(x->x.getDueType().equalsIgnoreCase("Service_Fee"))
				.mapToLong(Dues::getAmount).sum();
		long sumUnCloseServiceFeeVat = unCloseDueList.stream().filter(x->x.getDueType().equalsIgnoreCase("Service_Fee_VAT"))
				.mapToLong(Dues::getAmount).sum();
		rebookVo.setOldServiceFee(sumUnCloseServiceFee);
		rebookVo.setOldServiceFeeVat(sumUnCloseServiceFeeVat);
		//剩余未还金额
		rebookVo.setRemainingAmount(unCloseDueList.stream().mapToLong(Dues::getAmount).sum());
		long sumRemainingAmountNocarry = unCloseDueList.stream().filter(x->!x.getDueType().equalsIgnoreCase("Carrying_Fee")&&!x.getDueType().equalsIgnoreCase("Carrying_Fee_VAT"))
					.mapToLong(Dues::getAmount).sum();
		//剩余未还总金额 不含进位费
		rebookVo.setRemainingAmountNotIncludeCarryFee(sumRemainingAmountNocarry);
		
		//新订单计算费用相关
		rebookVo.setRealAmount(tryInfo.getAmount()-rebookVo.getOldPrincipal());
		//新订单需转出的还款金额
		rebookVo.setRepayTransferToFunder(rebookVo.getOldPrincipal());
		//新订单借款金额
		rebookVo.setNewApplyAmount(tryInfo.getAmount());
	}
	/**
	 * 处理ddm
	 * @param tryInfo
	 * @param upfrontDuesVo
	 * @param duedateList
	 * @param loandetail
	 */
	public void handleDdm(TryInfo tryInfo, UpfrontDuesVo upfrontDuesVo, List<String> duedateList,
			LoanDetail loandetail) {
		LocalDateTime lendAt = null;
		if(loandetail!=null){
			 lendAt = loandetail.getLendAt();
		}
		if(lendAt==null) {
			lendAt = LocalDateTime.now();
		}
//		String time = "2022-08-23 19:01:11";
//		lendAt = LocalDateTime.parse(time,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		LocalDate lendAtLocalDate = LocalDate.of(lendAt.getYear(), lendAt.getMonth(),lendAt.getDayOfMonth());
		//查询发薪日
		QueryWrapper<LoanDetailExtend> loanExtendquery = new QueryWrapper<>();
		loanExtendquery.eq("loan_number", tryInfo.getLoanNumber());
		LoanDetailExtend loanDetailExt = loanDetailExtendMapper.selectOne(loanExtendquery);
		int salaryDay = Integer.parseInt(loanDetailExt.getSalaryDay());	
		LocalDate salaryLocalDate = LocalDate.now();
		//salaryLocalDate = LocalDate.of(salaryLocalDate.getYear(), salaryLocalDate.getMonth(), salaryDay);

		salaryLocalDate = LocalDate.of(lendAt.getYear(), lendAt.getMonth(), salaryDay);

//		salaryLocalDate = LocalDate.parse("2022-10-15",DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		int criticalValue = 0;
		if(tryInfo.getSecondProductCode().contains("short")) {
			criticalValue = 15;
		}else if(tryInfo.getSecondProductCode().contains("long")) {
			criticalValue = 10;
		}
		log.info("salaryDay:{},lendAt.getDayOfMonth():{}",salaryDay,lendAt.getDayOfMonth());
		if(salaryDay<lendAt.getDayOfMonth()) {
			salaryLocalDate = salaryLocalDate.plusMonths(1);
			if(getDiffDay(lendAtLocalDate,salaryLocalDate)<criticalValue) {
				upfrontDuesVo.getDdmRelation().setDateDiff(getDiffDay(lendAtLocalDate,salaryLocalDate));
				salaryLocalDate = salaryLocalDate.plusMonths(1);	
				if(tryInfo.getSecondProductCode().contains("long")) {
					upfrontDuesVo.getDdmRelation().setPlanType("A");
				}
			} else 	if(tryInfo.getSecondProductCode().contains("long")&&getDiffDay(lendAtLocalDate,salaryLocalDate)==criticalValue) {
				upfrontDuesVo.getDdmRelation().setDateDiff(getDiffDay(lendAtLocalDate,salaryLocalDate));
				salaryLocalDate = salaryLocalDate.plusMonths(1);	
			}else {
				upfrontDuesVo.getDdmRelation().setDateDiff(getDiffDay(lendAtLocalDate,salaryLocalDate));
				if(tryInfo.getSecondProductCode().contains("long")) {
					upfrontDuesVo.getDdmRelation().setPlanType("B");
				}
			}
		}else {
			log.info("getDiffDay:{}",getDiffDay(lendAtLocalDate,salaryLocalDate));
			if(getDiffDay(lendAtLocalDate,salaryLocalDate)<criticalValue) {
				upfrontDuesVo.getDdmRelation().setDateDiff(getDiffDay(lendAtLocalDate,salaryLocalDate));
				salaryLocalDate = salaryLocalDate.plusMonths(1);	
				if(tryInfo.getSecondProductCode().contains("long")) {
					upfrontDuesVo.getDdmRelation().setPlanType("A");
				} 
			}else if(tryInfo.getSecondProductCode().contains("long")&&getDiffDay(lendAtLocalDate,salaryLocalDate)==criticalValue) {
				upfrontDuesVo.getDdmRelation().setDateDiff(getDiffDay(lendAtLocalDate,salaryLocalDate));
				salaryLocalDate = salaryLocalDate.plusMonths(1);	
					upfrontDuesVo.getDdmRelation().setPlanType("A");
			}else {
				if(tryInfo.getSecondProductCode().contains("long")) {
					upfrontDuesVo.getDdmRelation().setDateDiff(getDiffDay(lendAtLocalDate,salaryLocalDate));
					upfrontDuesVo.getDdmRelation().setPlanType("B");
				}
			}
		}
		//下一期时间
		DateTimeFormatter ofPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String format = salaryLocalDate.format(ofPattern);
		upfrontDuesVo.getDdmRelation().setNextDueDate(format);
		//最后一期还款时间
		if(tryInfo.getSecondProductCode().contains("long")) {
			Integer subTener = Integer.valueOf(tryInfo.getTenor().substring(0,tryInfo.getTenor().length()-1));
//				loanVo.setFinallyDueDateDDM(dateFormat.format( calendar2.getTime()));
			LocalDate lastDueDate = lendAtLocalDate.plusMonths(subTener);
			upfrontDuesVo.getDdmRelation().setLastDueDate(lastDueDate.format(ofPattern));
			LocalDate lastDueDateDdm = salaryLocalDate.plusMonths(subTener-1);
			upfrontDuesVo.getDdmRelation().setLastDueDateDdm(lastDueDateDdm.format(ofPattern));
			upfrontDuesVo.getDdmRelation().setDdmDays(Math.abs(getDiffDay(lastDueDate,lastDueDateDdm)));
//				calendar.add(Calendar.MONTH, subTener-1);
//				loanVo.setFinallyDueDate(dateFormat.format(calendar.getTime()));
//				loanVo.setDdmDay(Math.abs(getDiffDay(calendar,calendar2)));
			for(int i=0;i<subTener;i++) {
				LocalDate plusMonths = salaryLocalDate.plusMonths(i);
				String format2 = plusMonths.format(ofPattern);
				duedateList.add(format2);
			}
		}else if(tryInfo.getSecondProductCode().contains("short")) {
			log.info("lendAtLocalDate{},salaryLocalDate{}", lendAtLocalDate.format(ofPattern),salaryLocalDate.format(ofPattern));
			log.info("==={}",getDiffDay(lendAtLocalDate,salaryLocalDate));
			tryInfo.setTenor(getDiffDay(lendAtLocalDate,salaryLocalDate)+"D");
			duedateList.add(salaryLocalDate.format(ofPattern));
		}
	}
	/**
	 * 使用优惠码  更新产品费率
	 * @param tryInfo
	 * @param calFeeRateConfig
	 */
	public void buildPromotionRate(TryInfo tryInfo, CalFeeRateConfig calFeeRateConfig,UpfrontDuesVo upfrontDuesVo) {
		PromotionCodeDetail promotionCodeDetail = new PromotionCodeDetail();
		//查询优惠码信息
		HashMap<String, Object> promotionRateConfig = upfrontDuesMapper.getPromotionRateConfig(tryInfo.getPromotionCode());
		promotionCodeDetail.setPreferentialType((int)promotionRateConfig.get("preferential_type"));
		promotionCodeDetail.setUpfrontFee(new BigDecimal(promotionRateConfig.get("upfront_fee").toString()));
		promotionCodeDetail.setInterest(new BigDecimal(promotionRateConfig.get("interest").toString()));
		promotionCodeDetail.setOverdue(new BigDecimal(promotionRateConfig.get("overdue").toString()));
		upfrontDuesVo.setPromotionCodeDetail(promotionCodeDetail);
		if(promotionRateConfig.get("admin_fee")!=null) {
			promotionCodeDetail.setAdminFee(new BigDecimal(promotionRateConfig.get("admin_fee").toString()));
		}
		promotionCodeDetail.setPrincipal(new BigDecimal(promotionRateConfig.get("principal").toString()));
		tryInfo.setPromotionCodeDetail(promotionCodeDetail);
		// 还款计划中  资金方利率+平台服务费率
		BigDecimal inAndServiceDues = calFeeRateConfig.getDuesFundInterestRate().add(calFeeRateConfig.getDuesServiceFeeRate());
		
		// 砍头费中  资金方利率+平台服务费率
		BigDecimal inAndServiceUpfront = null;
		if(tryInfo.getSecondProductCode().contains("v2")) {
			 inAndServiceUpfront = calFeeRateConfig.getExtendUpfrontRate().getFundInterestRate().
					add(calFeeRateConfig.getExtendUpfrontRate().getServiceFeeRate());
		}
	
		//打折
		if(promotionCodeDetail.getPreferentialType()==1) {
			//处理砍头费砍头费率
			calFeeRateConfig.setUpfrontFeeRate(calFeeRateConfig.getUpfrontFeeRate()
					.multiply(promotionCodeDetail.getUpfrontFee()));
			//处理逾期费率
			calFeeRateConfig.setLateRate(calFeeRateConfig.getLateRate().multiply(promotionCodeDetail.getOverdue()));
			//处理还款计划中费率   平台服务费率
			calFeeRateConfig.setDuesServiceFeeRate(inAndServiceDues.multiply(promotionCodeDetail.getInterest())
					.subtract(calFeeRateConfig.getDuesFundInterestRate()));
		}else if(promotionCodeDetail.getPreferentialType()==2) {
			//处理砍头费砍头费率
			calFeeRateConfig.setUpfrontFeeRate(promotionCodeDetail.getUpfrontFee());
			//处理逾期费率
			calFeeRateConfig.setLateRate(promotionCodeDetail.getOverdue());
			//处理还款计划中费率   平台服务费率   替换后的比替换前的大就不替换
			if(inAndServiceDues.compareTo(promotionCodeDetail.getInterest())>=0) {		
				calFeeRateConfig.setDuesServiceFeeRate(promotionCodeDetail.getInterest()
						.subtract(calFeeRateConfig.getDuesFundInterestRate()));
			}
			//处理砍头费中费率   平台服务费率   替换后的比替换前的大就不替换
			if(inAndServiceUpfront.compareTo(promotionCodeDetail.getInterest())>=0&&inAndServiceUpfront!=null) {
				calFeeRateConfig.getExtendUpfrontRate().setServiceFeeRate(promotionCodeDetail.getInterest()
						.subtract(calFeeRateConfig.getExtendUpfrontRate().getFundInterestRate()));
			}
		}
	}
	public void befDisCalUofrontDues(TryInfo tryInfo, DuesVo duesVo, UpfrontFeeVO upfrontFeeVO) {
		TryInfo info = tryInfo;
		//info.setPromotionCode(null);
		info.setTryFlag(1);
		log.info("tryInfo:{},info:{}",tryInfo,info);
		log.info("tryInfo.getPromotionCode():{}",tryInfo.getPromotionCode()==null);
		log.info("Info.getPromotionCode():{}",info.getPromotionCode()==null);
		UpfrontDuesVo calculateUpfrontDues = CalculateUpfrontDues(info);
		upfrontFeeVO.setBeforeDisUpfrontFee(calculateUpfrontDues.getUpfrontFeeVO().getUpfrontFee());
		duesVo.setBefDisTotaServiceFee(calculateUpfrontDues.getDuesVo().getTotaServiceFee());
		duesVo.setBefDisRepaymentAmount(calculateUpfrontDues.getDuesVo().getRepaymentAmount());
		upfrontFeeVO.setDisUpfrontFee(upfrontFeeVO.getBeforeDisUpfrontFee().subtract(upfrontFeeVO.getUpfrontFee()));
		duesVo.setDisRepaymentAmount(duesVo.getBefDisRepaymentAmount().subtract(duesVo.getRepaymentAmount()));
		duesVo.setBefDisTotaServiceFee(duesVo.getBefDisTotaServiceFee().subtract(duesVo.getTotaServiceFee()));
		if(info.getSecondProductCode().contains("long")) {
			duesVo.setDisRepaymentAmount((duesVo.getBefDisRepaymentAmount().subtract(duesVo.getRepaymentAmount()))
					.multiply(new BigDecimal(info.getTenor().substring(0, info.getTenor().length()-1))));

		}
	}
	public void calRepayDues(TryInfo tryInfo, CalFeeRateConfig calFeeRateConfig, VatWht vatWht, UpfrontDuesVo upfrontDuesVo) {
		//如果是rebook替换计算砍头费的金额
		if(tryInfo.getRebookOLdLoanNumber()!=null&&StringUtils.isNotEmpty(tryInfo.getRebookOLdLoanNumber())) {
			//计算费用相关
			tryInfo.setAmount(upfrontDuesVo.getRebookVo().getRealAmount());
		}
		DuesVo duesVo = upfrontDuesVo.getDuesVo();
		DdmRelation ddmRelation = upfrontDuesVo.getDdmRelation();
		//每天的资金方利率  平台服务费率
		BigDecimal dailyInterestRate = calFeeRateConfig.getDuesFundInterestRate()
				.divide(new BigDecimal(30), 6, RoundingMode.HALF_UP);
		BigDecimal dailyServiceFeeRate = calFeeRateConfig.getDuesServiceFeeRate()
		.divide(new BigDecimal(30), 6, RoundingMode.HALF_UP);
		BigDecimal dailyProvisionRate =null;
		BigDecimal provisionDdm = null;
		if(calFeeRateConfig.getDuesProvisionRate()!=null) {
			dailyProvisionRate = calFeeRateConfig.getDuesProvisionRate()
					.divide(new BigDecimal(30), 6, RoundingMode.HALF_UP);
			provisionDdm = new BigDecimal(tryInfo.getAmount()).multiply(new BigDecimal(ddmRelation.getDdmDays())).multiply(dailyProvisionRate).setScale(0, RoundingMode.DOWN);
		}
		//计算额外的资金方利息   借款金额*额外的天数*每天的费率
		BigDecimal interestDdm = new BigDecimal(tryInfo.getAmount()).multiply(new BigDecimal(ddmRelation.getDdmDays())).multiply(dailyInterestRate).setScale(0, RoundingMode.DOWN);
		//计算额外的平台服务费   借款金额*额外的天数*每天的费率
		BigDecimal serviceFeeDdm = new BigDecimal(tryInfo.getAmount()).multiply(new BigDecimal(ddmRelation.getDdmDays())).multiply(dailyServiceFeeRate).setScale(0, RoundingMode.DOWN);

		int tenorDay = Integer.valueOf(tryInfo.getTenor().substring(0, tryInfo.getTenor().length()-1));
		int tenorM = Integer.valueOf(tryInfo.getTenor().substring(0, tryInfo.getTenor().length()-1));
		//计算还款计划中费用
		RepayDues normalDues = new RepayDues();
			//短期本金
		normalDues.setPrincipal(new BigDecimal(tryInfo.getAmount()));
		if(tryInfo.getSecondProductCode().contains("short")&&tryInfo.getLoanNumber()==null) {
			tenorDay = 30;
		}else if(tryInfo.getSecondProductCode().contains("long")) {
			//长期本金
			normalDues.setPrincipal(new BigDecimal(tryInfo.getAmount())
					.divide(new BigDecimal(tenorDay),0, RoundingMode.UP));
			//如果是rebook本金需要用新订单借款金额计算  ，因为前面的tryInfo.getAmount()被替换
			if(tryInfo.getRebookOLdLoanNumber()!=null&&StringUtils.isNotEmpty(tryInfo.getRebookOLdLoanNumber())) {
				//计算费用相关
				normalDues.setPrincipal(new BigDecimal(upfrontDuesVo.getRebookVo().getNewApplyAmount())
						.divide(new BigDecimal(tenorDay),0, RoundingMode.UP));
			}
			if(upfrontDuesVo.getEPlanOldOrderUnpayPrincipal()>0) {
				//计算费用相关
				normalDues.setPrincipal(new BigDecimal(upfrontDuesVo.getEPlanOldOrderUnpayPrincipal())
						.divide(new BigDecimal(tenorDay),0, RoundingMode.UP));
			}
			tenorDay = 1 ;
		}
		//资金方利息
		normalDues.setFundInterest(new BigDecimal(tryInfo.getAmount())
				.multiply(calFeeRateConfig.getDuesFundInterestRate()).multiply(new BigDecimal(tenorDay)).setScale(0, RoundingMode.UP));
		//保证金
		if(calFeeRateConfig.getDuesProvisionRate()!=null) {
			normalDues.setProvisionFee(new BigDecimal(tryInfo.getAmount())
					.multiply(calFeeRateConfig.getDuesProvisionRate()).multiply(new BigDecimal(tenorDay)).setScale(0, RoundingMode.UP));
		}

		//平台服务费
		normalDues.setServiceFee(new BigDecimal(tryInfo.getAmount())
				.multiply(calFeeRateConfig.getDuesServiceFeeRate()).multiply(new BigDecimal(tenorDay)).setScale(0, RoundingMode.UP));
		//如果是rebook   新订单的资金方利息  平台服务费 需要加上老订单的资金方利息和本金
		if(tryInfo.getRebookOLdLoanNumber()!=null&&StringUtils.isNotEmpty(tryInfo.getRebookOLdLoanNumber())) {
			//更新资金方利息
			normalDues.setFundInterest(normalDues.getFundInterest().add(new BigDecimal
					(upfrontDuesVo.getRebookVo().getOldFundInterest()).divide(new BigDecimal(tenorM),0, RoundingMode.UP)));
			//更新平台服务费
			normalDues.setServiceFee(normalDues.getServiceFee().add
					(new BigDecimal(upfrontDuesVo.getRebookVo().getOldServiceFee()).divide(new BigDecimal(tenorM),0, RoundingMode.UP)));
		}
		//处理优惠码
		if(upfrontDuesVo.getPromotionCodeDetail()!=null &&upfrontDuesVo.getPromotionCodeDetail().getPreferentialType()==3) {
			if(tryInfo.getSecondProductCode().contains("short")) {
				BigDecimal service = normalDues.getServiceFee().subtract(upfrontDuesVo.getPromotionCodeDetail().getInterest());
				normalDues.setServiceFee(service.compareTo(new BigDecimal(0))>=0?service:new BigDecimal(0));
			}else if(tryInfo.getSecondProductCode().contains("long")) {
				//每期平台服务费减免金额
				BigDecimal divide = upfrontDuesVo.getPromotionCodeDetail().getInterest().divide(new BigDecimal(tenorM), 0,RoundingMode.HALF_UP);
				BigDecimal service = normalDues.getServiceFee().subtract(divide);
				normalDues.setServiceFee(service.compareTo(new BigDecimal(0))>=0?service:new BigDecimal(0));

			}
		}
		//平台服务费税费
		normalDues.setServiceFeeVAT(normalDues.getServiceFee().multiply(vatWht.getVAT()).setScale(0, RoundingMode.HALF_UP));
		//资金方利息+保证金+平台服务费+平台服务费税费+本金
		BigDecimal repayAmountNoCarryfee = null;
		 repayAmountNoCarryfee = normalDues.getFundInterest().add(normalDues.getServiceFee())
				 .add(normalDues.getServiceFeeVAT()).add(normalDues.getPrincipal());
		if(calFeeRateConfig.getDuesProvisionRate()!=null) {
			 repayAmountNoCarryfee = normalDues.getFundInterest().add(normalDues.getProvisionFee())
					.add(normalDues.getServiceFee()).add(normalDues.getServiceFeeVAT())
							.add(normalDues.getPrincipal());
		}
		duesVo.setRepaymentAmount(repayAmountNoCarryfee.setScale(-3, RoundingMode.UP));
		//进位费 CarryingFeeIncludeVAT
		normalDues.setCarryingFeeIncludeVAT(duesVo.getRepaymentAmount().subtract(repayAmountNoCarryfee));
		normalDues.setCarryingFee(normalDues.getCarryingFeeIncludeVAT()
				.divide((new BigDecimal(1).add(vatWht.getVAT())),0, RoundingMode.HALF_UP));
		
		normalDues.setCarryingFeeVAT(normalDues.getCarryingFeeIncludeVAT().subtract(normalDues.getCarryingFee()));
		normalDues.setDueDate(upfrontDuesVo.getDdmRelation().getNextDueDate());
		System.out.println(upfrontDuesVo.getDdmRelation());
		if(tryInfo.getSecondProductCode().contains("long")&&tryInfo.getSecondProductCode().contains("v1")
				&&upfrontDuesVo.getDuesVo().getDuedateList()!=null&&upfrontDuesVo.getDuesVo().getDuedateList().size()>0) {
			normalDues.setDueDate(upfrontDuesVo.getDuesVo().getDuedateList().get(1));
		}
		normalDues.setAdminFee(new BigDecimal(0));
		normalDues.setAdminFeeVAT(new BigDecimal(0));
		//短期 V1 服务费
		duesVo.setTotaServiceFee(repayAmountNoCarryfee.subtract(normalDues.getPrincipal()));
		if(tryInfo.getSecondProductCode().contains("long")) {
			BigDecimal lastPrincipal = new BigDecimal(tryInfo.getAmount()).subtract((normalDues.getPrincipal().multiply(new BigDecimal(tenorM-1))));
//			duesVo.setTotaServiceFee((repayAmountNoCarryfee.subtract(normalDues.getPrincipal()))
//					.multiply(new BigDecimal(tenorM)).add((normalDues.getPrincipal().subtract(lastPrincipal))));
			//平台服务费费税费
			BigDecimal FundInterest = normalDues.getFundInterest().multiply(new BigDecimal(tenorM));
			BigDecimal totalPlatfee = normalDues.getServiceFee().multiply(new BigDecimal(tenorM));
			BigDecimal totalPlatfeeVat = totalPlatfee.multiply(vatWht.getVAT()).setScale(0, RoundingMode.HALF_UP);
			duesVo.setTotaServiceFee(FundInterest.add(totalPlatfee).add(totalPlatfeeVat));
			if(tryInfo.getLoanNumber()!=null&&StringUtils.isNotEmpty(tryInfo.getLoanNumber())) {
				//DDM
				RepayDues dDMDues = new RepayDues();
				if("A".equalsIgnoreCase(ddmRelation.getPlanType())) {
					dDMDues.setDueDate(ddmRelation.getNextDueDate());
					dDMDues.setPrincipal(normalDues.getPrincipal());
					dDMDues.setFundInterest(normalDues.getFundInterest().add(interestDdm));
					dDMDues.setServiceFee(normalDues.getServiceFee().add(serviceFeeDdm));
					dDMDues.setAdminFee(new BigDecimal(0));
					dDMDues.setAdminFeeVAT(new BigDecimal(0));
					dDMDues.setDueDate(upfrontDuesVo.getDdmRelation().getNextDueDate());
					normalDues.setDueDate(upfrontDuesVo.getDuesVo().getDuedateList().get(1));

					//保证金
					if(calFeeRateConfig.getDuesProvisionRate()!=null) {
						dDMDues.setProvisionFee(normalDues.getProvisionFee().add(provisionDdm));
					}
					
				}else if("B".equalsIgnoreCase(ddmRelation.getPlanType())&&tryInfo.getSecondProductCode().contains("v2")) {
					
					// 总的借款金额除以期数，除不尽  ，本金向上取整，最后一期=借款金额-非最后一期的本金只和
					dDMDues.setPrincipal(new BigDecimal(tryInfo.getAmount()).subtract((normalDues.getPrincipal()
							.multiply(new BigDecimal(tenorM).subtract(new BigDecimal(1))))));
					//如果是rebook替换计算砍头费的金额
					if(tryInfo.getRebookOLdLoanNumber()!=null&&StringUtils.isNotEmpty(tryInfo.getRebookOLdLoanNumber())) {
						//计算费用相关
						dDMDues.setPrincipal(new BigDecimal(upfrontDuesVo.getRebookVo().getNewApplyAmount()).subtract((normalDues.getPrincipal()
								.multiply(new BigDecimal(tenorM).subtract(new BigDecimal(1))))));
					}
					if(upfrontDuesVo.getEPlanOldOrderUnpayPrincipal()>0) {
						//计算费用相关
						normalDues.setPrincipal(new BigDecimal(upfrontDuesVo.getEPlanOldOrderUnpayPrincipal())
								.divide(new BigDecimal(tenorDay),0, RoundingMode.UP));
					}
					// 为保证每期借款金额一致  因为最后一期本金可能<非最后一期本金  少的部分需要加到服务费上
					
					BigDecimal principalDiff = normalDues.getPrincipal().subtract(dDMDues.getPrincipal());
					dDMDues.setDueDate(ddmRelation.getLastDueDateDdm());
					//因最后一期本金少 为了保证每一期还款金额一致 所以平台服务费需要增加响应的值  
					dDMDues.setFundInterest(normalDues.getFundInterest().subtract(interestDdm));
					dDMDues.setServiceFee(normalDues.getServiceFee().add(principalDiff).subtract(serviceFeeDdm));
					dDMDues.setAdminFee(new BigDecimal(0));
					dDMDues.setAdminFeeVAT(new BigDecimal(0));
					dDMDues.setDueDate(upfrontDuesVo.getDdmRelation().getLastDueDateDdm());

					//保证金
					if(calFeeRateConfig.getDuesProvisionRate()!=null) {
						dDMDues.setProvisionFee(normalDues.getProvisionFee().subtract(provisionDdm));
					}
				}else if("B".equalsIgnoreCase(ddmRelation.getPlanType())&&
						(tryInfo.getSecondProductCode().contains("v1")||tryInfo.getSecondProductCode().contains("rs"))) {
					
					// 总的借款金额除以期数，除不尽  ，本金向上取整，最后一期=借款金额-非最后一期的本金只和
					dDMDues.setPrincipal(normalDues.getPrincipal());
					// 为保证每期借款金额一致  因为最后一期本金可能<非最后一期本金  少的部分需要加到服务费上
//					BigDecimal principalDiff = normalDues.getPrincipal().subtract(dDMDues.getPrincipal());
					dDMDues.setDueDate(ddmRelation.getLastDueDateDdm());
					//因最后一期本金少 为了保证每一期还款金额一致 所以平台服务费需要增加响应的值  
					dDMDues.setFundInterest(normalDues.getFundInterest().subtract(interestDdm));
					dDMDues.setServiceFee(normalDues.getServiceFee().subtract(serviceFeeDdm));
					dDMDues.setAdminFee(new BigDecimal(0));
					dDMDues.setAdminFeeVAT(new BigDecimal(0));
					//第一个duedate即为DDM的duedate
					dDMDues.setDueDate(upfrontDuesVo.getDdmRelation().getNextDueDate());

					//保证金
					if(calFeeRateConfig.getDuesProvisionRate()!=null) {
						dDMDues.setProvisionFee(normalDues.getProvisionFee().subtract(provisionDdm));
					}
				}
				//平台服务费税费
				dDMDues.setServiceFeeVAT(dDMDues.getServiceFee().multiply(vatWht.getVAT()).setScale(0, RoundingMode.HALF_UP));
				//资金方利息+保证金+平台服务费+平台服务费税费+本金
				BigDecimal repayAmountNoCarryfeeDDM = null;
				repayAmountNoCarryfeeDDM = dDMDues.getFundInterest().add(dDMDues.getServiceFee())
						 .add(dDMDues.getServiceFeeVAT()).add(dDMDues.getPrincipal());
				
				if(calFeeRateConfig.getDuesProvisionRate()!=null) {
					repayAmountNoCarryfeeDDM = dDMDues.getFundInterest().add(dDMDues.getProvisionFee())
							.add(dDMDues.getServiceFee()).add(dDMDues.getServiceFeeVAT())
									.add(dDMDues.getPrincipal());
				}
				duesVo.setRepaymentAmountDdm(repayAmountNoCarryfeeDDM.setScale(-3, RoundingMode.UP));
				//进位费 CarryingFeeIncludeVAT
				dDMDues.setCarryingFeeIncludeVAT(duesVo.getRepaymentAmountDdm().subtract(repayAmountNoCarryfeeDDM));
				dDMDues.setCarryingFee(dDMDues.getCarryingFeeIncludeVAT()
						.divide((new BigDecimal(1).add(vatWht.getVAT())),0, RoundingMode.HALF_UP));
				
				dDMDues.setCarryingFeeVAT(dDMDues.getCarryingFeeIncludeVAT().subtract(dDMDues.getCarryingFee()));
				//短期 V1 服务费
				duesVo.setTotaServiceFee(repayAmountNoCarryfee.subtract(normalDues.getPrincipal()));
				duesVo.setDDMDues(dDMDues);
			}

		}
		duesVo.setNormalDues(normalDues);
	}
	/**
	 * 计算V2产品费率
	 * @param tryInfo   借款信息 金额期数
	 * @param calFeeRateConfig 二级产品费率
	 * @param vatWht   税费
	 * @param upfrontFeeVO   砍头费
	 * @param upEtendFee  砍头费中扩展字段
	 * @param tenorDay  借款期数  
	 */
	public void calV2UpfrontFee(TryInfo tryInfo, CalFeeRateConfig calFeeRateConfig, VatWht vatWht,
			UpfrontDuesVo upfrontDuesVo, int tenorDay) {
		UpfrontFeeVO upfrontFeeVO = upfrontDuesVo.getUpfrontFeeVO();
		DdmRelation ddmRelation = upfrontDuesVo.getDdmRelation();
		UpfrontExtendFee upEtendFee = 	new UpfrontExtendFee();
		//资金方对应的  wht
		BigDecimal wht = null;
		if(upfrontDuesVo.getUpfrontFeeVO().getFundId()>0) {
			List<Wht> whtList = vatWht.getWHT();
			//找到资金方对应的wht
			List<Wht> collect = whtList.stream().filter(x->x.getFundId()==upfrontDuesVo.getUpfrontFeeVO().getFundId()).collect(Collectors.toList());
			wht = collect.get(0).getRate();
		}else {
			wht = new BigDecimal(0.15);
		}
		//如果是rebook替换计算砍头费的金额
		if(tryInfo.getRebookOLdLoanNumber()!=null&&StringUtils.isNotEmpty(tryInfo.getRebookOLdLoanNumber())) {
			//计算费用相关
			tryInfo.setAmount(upfrontDuesVo.getRebookVo().getRealAmount());
		}
		//每天的资金方利率  平台服务费率
		//每天的资金方利率  平台服务费率
//		BigDecimal dailyInterestRate = calFeeRateConfig.getExtendUpfrontRate().getFundInterestRate()
//				.divide(new BigDecimal(30), 6, RoundingMode.HALF_UP);
		BigDecimal dailyInterestRate = calFeeRateConfig.getExtendUpfrontRate().getFundInterestRate()
				.divide(new BigDecimal(30), 6, RoundingMode.HALF_UP);
		BigDecimal dailyServiceFeeRate = calFeeRateConfig.getExtendUpfrontRate().getServiceFeeRate()
		.divide(new BigDecimal(30), 6, RoundingMode.HALF_UP);
		//计算额外的资金方利息   借款金额*额外的天数*每天的费率
		BigDecimal interestDdm = new BigDecimal(tryInfo.getAmount()).multiply(new BigDecimal(ddmRelation.getDdmDays())).multiply(dailyInterestRate).setScale(0, RoundingMode.DOWN);
		//计算额外的平台服务费   借款金额*额外的天数*每天的费率
		BigDecimal serviceFeeDdm = new BigDecimal(tryInfo.getAmount()).multiply(new BigDecimal(ddmRelation.getDdmDays())).multiply(dailyServiceFeeRate).setScale(0, RoundingMode.DOWN);

		//计算资金方利息
		upEtendFee.setUpFundInterestFeeIncludeWHT(new BigDecimal(tryInfo.getAmount()).multiply(new BigDecimal(tenorDay))
				.multiply(calFeeRateConfig.getExtendUpfrontRate().getFundInterestRate()));
		//计算砍头费中 平台服务费
		upEtendFee.setUpfrontServiceFee(new BigDecimal(tryInfo.getAmount()).multiply(new BigDecimal(tenorDay))
				.multiply(calFeeRateConfig.getExtendUpfrontRate().getServiceFeeRate()));
		if(tryInfo.getLoanNumber()!=null&&tryInfo.getSecondProductCode().contains("long")) {
			if("A".equalsIgnoreCase(ddmRelation.getPlanType())) {
				upEtendFee.setUpFundInterestFeeIncludeWHT(upEtendFee.getUpFundInterestFeeIncludeWHT().add(interestDdm));
				upEtendFee.setUpfrontServiceFee(upEtendFee.getUpfrontServiceFee().add(serviceFeeDdm));
			}else if("B".equalsIgnoreCase(ddmRelation.getPlanType())) {
				upEtendFee.setUpFundInterestFeeIncludeWHT(upEtendFee.getUpFundInterestFeeIncludeWHT().subtract(interestDdm));
				upEtendFee.setUpfrontServiceFee(upEtendFee.getUpfrontServiceFee().subtract(serviceFeeDdm));
			}
		}
		//如果是减免优惠码   再原有的服务费上减免
		if(upfrontDuesVo.getPromotionCodeDetail()!=null &&upfrontDuesVo.getPromotionCodeDetail().getPreferentialType()==3) {
			upEtendFee.setUpfrontServiceFee(upEtendFee.getUpfrontServiceFee()
					.subtract(upfrontDuesVo.getPromotionCodeDetail().getInterest()));
			if(upEtendFee.getUpfrontServiceFee().intValue()<0) {
				upEtendFee.setUpfrontServiceFee(new BigDecimal(0));
			}
		}
		// 资金方税费
		upEtendFee.setUpFundInterestFeeWHT(upEtendFee.getUpFundInterestFeeIncludeWHT().multiply(wht).setScale(0, RoundingMode.HALF_UP));
		//不含税资金方利息
		upEtendFee.setUpfrontFundInterestFee(upEtendFee.getUpFundInterestFeeIncludeWHT().subtract(upEtendFee.getUpFundInterestFeeWHT()));
		//计算砍头费中 保证金
		upEtendFee.setUpfrontProvisionFee(new BigDecimal(tryInfo.getAmount())
				.multiply(calFeeRateConfig.getExtendUpfrontRate().getProvisionRate()));
		//平台服务费非税费
		upEtendFee.setUpfrontServiceFeeVAT(upEtendFee.getUpfrontServiceFee().multiply(vatWht.getVAT()).setScale(0, RoundingMode.HALF_UP));
		//砍头费中不含税费的管理费
		upEtendFee.setUpfrontAdminFee(upfrontFeeVO.getOriginalUpfrontFee().subtract(upEtendFee.getUpFundInterestFeeIncludeWHT())
				.subtract(upEtendFee.getUpfrontProvisionFee()).subtract(upEtendFee.getUpfrontServiceFee()));
		if(upEtendFee.getUpfrontAdminFee().intValue()<0) {
			//adminfee小于0   则是因为总的砍头费校验，需要替换原先的管理费
			if(upfrontFeeVO.getOriginalUpfrontFee().compareTo(upEtendFee.getUpfrontFundInterestFee().add(upEtendFee.getUpfrontProvisionFee()))<0) {
				upEtendFee.setUpfrontProvisionFee(upfrontFeeVO.getOriginalUpfrontFee().subtract(upEtendFee.getUpFundInterestFeeIncludeWHT()));
			}
			upEtendFee.setUpfrontAdminFee(new BigDecimal(0));
		}
		upEtendFee.setUpfrontAdminFeeVAT(upEtendFee.getUpfrontAdminFee().multiply(vatWht.getVAT()).setScale(0, RoundingMode.HALF_UP));
		upfrontFeeVO.setUpfrontFee(upfrontFeeVO.getOriginalUpfrontFee().add(upEtendFee.getUpfrontAdminFeeVAT())
				.add(upEtendFee.getUpfrontServiceFeeVAT()).setScale(-3, RoundingMode.UP));

		upEtendFee.setUpfrontCarryFeeIncVat(upfrontFeeVO.getUpfrontFee().subtract(upfrontFeeVO.getOriginalUpfrontFee())
				.subtract(upEtendFee.getUpfrontAdminFeeVAT()).subtract(upEtendFee.getUpfrontServiceFeeVAT()));
		//砍头费比较小，保证金比用费率计算出来的小    保证金要重新分摊
		if(upfrontFeeVO.getOriginalUpfrontFee().compareTo(upEtendFee.getUpFundInterestFeeIncludeWHT().add(upEtendFee.getUpfrontProvisionFee()))<0){
			upEtendFee.setUpfrontProvisionFee(upfrontFeeVO.getOriginalUpfrontFee().subtract(upEtendFee.getUpFundInterestFeeIncludeWHT()));
		}
		//有保费  加上保费计算砍头费
		if(tryInfo.isInsuranceSelect()==true) {
			upfrontFeeVO.setUpfrontFee(upfrontFeeVO.getOriginalUpfrontFee().add(upEtendFee.getUpfrontAdminFeeVAT())
					.add(upEtendFee.getUpfrontServiceFeeVAT()).add(new BigDecimal(tryInfo.getInsurancePremiumAmount())).setScale(-3, RoundingMode.UP));
			upEtendFee.setUpfrontCarryFeeIncVat(upfrontFeeVO.getUpfrontFee().subtract(upfrontFeeVO.getOriginalUpfrontFee())
					.subtract(upEtendFee.getUpfrontAdminFeeVAT()).subtract(upEtendFee.getUpfrontServiceFeeVAT()).subtract(new BigDecimal(tryInfo.getInsurancePremiumAmount())));
//			upEtendFee.setUpfrontCarryFee(upEtendFee.getUpfrontCarryFeeIncVat()
//					.divide((new BigDecimal(1).add(vatWht.getVAT())),RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP));
//			upEtendFee.setUpfrontCarryFeeVAT(upEtendFee.getUpfrontCarryFeeIncVat().subtract(upEtendFee.getUpfrontCarryFee()));
		}
		upEtendFee.setUpfrontCarryFee(upEtendFee.getUpfrontCarryFeeIncVat()
				.divide((new BigDecimal(1).add(vatWht.getVAT())),RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP));
		upEtendFee.setUpfrontCarryFeeVAT(upEtendFee.getUpfrontCarryFeeIncVat().subtract(upEtendFee.getUpfrontCarryFee()));
//			upEtendFee.setUpfrontCarryFeeVAT(upEtendFee.getUpfrontCarryFeeIncVat().subtract(upEtendFee.getUpfrontCarryFee()));
		upfrontFeeVO.setExtendFee(upEtendFee);
		upfrontFeeVO.setReceivedMoney(new BigDecimal(tryInfo.getAmount()).subtract(upfrontFeeVO.getUpfrontFee()));
		//如果是rebook更新新订单
		if(tryInfo.getRebookOLdLoanNumber()!=null&&StringUtils.isNotEmpty(tryInfo.getRebookOLdLoanNumber())) {
			//更新新订单的砍头费     到手金额
			upfrontDuesVo.getRebookVo().setUpfrontFee(upfrontFeeVO.getUpfrontFee().longValue());
			upfrontDuesVo.getRebookVo().setReceivedAmount(upfrontFeeVO.getReceivedMoney().longValue());
		}
		if(tryInfo.getLoanNumber()!=null) {
			TransferAmount transfer = new TransferAmount();
			transfer.setTransferAdminFee(upEtendFee.getUpfrontAdminFee().add(upEtendFee.getUpfrontCarryFee()));
			transfer.setTransferFundInterestFee(upEtendFee.getUpfrontFundInterestFee());
			transfer.setTransferServiceFee(upEtendFee.getUpfrontServiceFee());
			transfer.setTransferProvisionFee(upEtendFee.getUpfrontProvisionFee());
			transfer.setTransferVatWht(upEtendFee.getUpfrontAdminFeeVAT().add(upEtendFee.getUpfrontServiceFeeVAT()
					.add(upEtendFee.getUpfrontCarryFeeVAT()).add(upEtendFee.getUpFundInterestFeeWHT())));
			transfer.setTransferToUserAmount(upfrontFeeVO.getReceivedMoney());
			transfer.setTransferRechargeAmount(new BigDecimal(tryInfo.getAmount()));
			transfer.setRepayTransferToFunder(new BigDecimal(upfrontDuesVo.getRebookVo().getRepayTransferToFunder()));
			upfrontDuesVo.setTransfer(transfer);
		}
	}
	/**
	 * 计算V1砍头费用
	 * @param tryInfo
	 * @param rateConfig
	 * @param calFeeRateConfig
	 * @param vatWht
	 * @param upfrontFeeVO
	 */
	public void calV1UpfrontFee(TryInfo tryInfo,  CalFeeRateConfig calFeeRateConfig,
			VatWht vatWht, UpfrontDuesVo upfrontDuesVo) {
		//如果是rebook替换计算砍头费的金额
		if(tryInfo.getRebookOLdLoanNumber()!=null&&StringUtils.isNotEmpty(tryInfo.getRebookOLdLoanNumber())) {
			//计算费用相关
			tryInfo.setAmount(upfrontDuesVo.getRebookVo().getRealAmount());
		}
		UpfrontFeeVO upfrontFeeVO = upfrontDuesVo.getUpfrontFeeVO();
		//原先的砍头费   借款金额*砍头费率
		upfrontFeeVO.setOriginalUpfrontFee(new BigDecimal(tryInfo.getAmount()).multiply(calFeeRateConfig.getUpfrontFeeRate()));
		System.out.println("v1产品");
		//如果是减免优惠码   再原有的砍头费上减免
		if(upfrontDuesVo.getPromotionCodeDetail()!=null &&upfrontDuesVo.getPromotionCodeDetail().getPreferentialType()==3) {
			upfrontFeeVO.setOriginalUpfrontFee(upfrontFeeVO.getOriginalUpfrontFee()
					.subtract(upfrontDuesVo.getPromotionCodeDetail().getUpfrontFee()));
		}
		String[] split = calFeeRateConfig.getProvisionRate().split(":");
		String upAdminfeeProportion = split[0];
		String upProvisionFeeProportion = split[1];
		BigDecimal proportion = new BigDecimal(upAdminfeeProportion).
				add(new BigDecimal(upProvisionFeeProportion));
		upfrontFeeVO.setUpfrontAdminfee(calV1Upfrontfee(upfrontFeeVO.getOriginalUpfrontFee(), upAdminfeeProportion, proportion).setScale(0, RoundingMode.HALF_UP));
//		upfrontFeeVO.setUpfrontProvisionFee(calV1Upfrontfee(upfrontFeeVO.getOriginalUpfrontFee(), upProvisionFeeProportion, proportion));
		upfrontFeeVO.setUpfrontProvisionFee(upfrontFeeVO.getOriginalUpfrontFee().subtract(upfrontFeeVO.getUpfrontAdminfee()));

		upfrontFeeVO.setUpfrontAdminfeeVat(upfrontFeeVO.getUpfrontAdminfee().multiply(vatWht.getVAT()).setScale(0, RoundingMode.HALF_UP));
		BigDecimal setScale = upfrontFeeVO.getUpfrontAdminfee().add(upfrontFeeVO.getUpfrontProvisionFee()).add(upfrontFeeVO.getUpfrontAdminfeeVat())
				.setScale(-3, RoundingMode.UP);
		//有保费  加上保费计算砍头费
		if(tryInfo.isInsuranceSelect()==true) {
			setScale = upfrontFeeVO.getUpfrontAdminfee().add(upfrontFeeVO.getUpfrontProvisionFee()).add(upfrontFeeVO.getUpfrontAdminfeeVat())
					.add(new BigDecimal(tryInfo.getInsurancePremiumAmount())).setScale(-3, RoundingMode.UP);
			upfrontFeeVO.setPremiumFee(new BigDecimal(tryInfo.getInsurancePremiumAmount()));
		}
		upfrontFeeVO.setUpfrontFee(setScale);
		upfrontFeeVO.setUpfrontCarryFeeIncludeVat(setScale.subtract(upfrontFeeVO.getUpfrontAdminfee())
				.subtract(upfrontFeeVO.getUpfrontProvisionFee()).subtract(upfrontFeeVO.getUpfrontAdminfeeVat()));
		if(tryInfo.isInsuranceSelect()==true) {
			upfrontFeeVO.setUpfrontCarryFeeIncludeVat(setScale.subtract(upfrontFeeVO.getUpfrontAdminfee())
					.subtract(upfrontFeeVO.getUpfrontProvisionFee()).subtract(upfrontFeeVO.getUpfrontAdminfeeVat()).subtract(new BigDecimal(tryInfo.getInsurancePremiumAmount())));
		}
		upfrontFeeVO.setUpfrontCarryFee(upfrontFeeVO.getUpfrontCarryFeeIncludeVat()
				.divide((new BigDecimal(1).add(vatWht.getVAT())),RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP));
		upfrontFeeVO.setUpfrontCarryFeeVat(upfrontFeeVO.getUpfrontCarryFeeIncludeVat().subtract(upfrontFeeVO.getUpfrontCarryFee()));
		upfrontFeeVO.setReceivedMoney(new BigDecimal(tryInfo.getAmount()).subtract(upfrontFeeVO.getUpfrontFee()));
		
		//如果是rebook更新新订单
		if(tryInfo.getRebookOLdLoanNumber()!=null&&StringUtils.isNotEmpty(tryInfo.getRebookOLdLoanNumber())) {
			//更新新订单的砍头费     到手金额
			upfrontDuesVo.getRebookVo().setUpfrontFee(upfrontFeeVO.getUpfrontFee().longValue());
			upfrontDuesVo.getRebookVo().setReceivedAmount(upfrontFeeVO.getReceivedMoney().longValue());
		}
		//转账 充值金额
		if(tryInfo.getLoanNumber()!=null) {
			TransferAmount transfer = new TransferAmount();
			transfer.setTransferAdminFee(upfrontFeeVO.getUpfrontAdminfee()
					.add(upfrontFeeVO.getUpfrontProvisionFee()).add(upfrontFeeVO.getUpfrontCarryFee()));
			transfer.setTransferVatWht(upfrontFeeVO.getUpfrontAdminfeeVat().add(upfrontFeeVO.getUpfrontCarryFeeVat()));
			transfer.setTransferToUserAmount(upfrontFeeVO.getReceivedMoney());
			transfer.setTransferRechargeAmount(new BigDecimal(tryInfo.getAmount()));
			transfer.setRepayTransferToFunder(new BigDecimal(upfrontDuesVo.getRebookVo().getRepayTransferToFunder()));
			transfer.setPremiumFee(new BigDecimal(tryInfo.getInsurancePremiumAmount()));
			upfrontDuesVo.setTransfer(transfer);
		}
	}
	/**
	 * 把费率保存到calFeeRateConfig
	 * @param rateConfig
	 * @param calFeeRateConfig
	 */
	public void buildCalFeeRateConfig(String  secondProductCode,CalFeeRateConfig calFeeRateConfig,int fundId) {
		
//		ExtendUpfrontRate extendRate = new ExtendUpfrontRate();
		//查询费率
		HashMap<String, Object> rateConfig = upfrontDuesMapper.getRateConfig(fundId,secondProductCode);
		calFeeRateConfig.setUpfrontFeeRate(new BigDecimal(rateConfig.get("admin_rate").toString()));
		calFeeRateConfig.setDuesFundInterestRate(new BigDecimal(rateConfig.get("fund_interest_rate").toString()));
		if(rateConfig.get("fund_provision_rate")!=null) {
			calFeeRateConfig.setDuesProvisionRate(new BigDecimal(rateConfig.get("fund_provision_rate").toString()));
		}
		calFeeRateConfig.setDuesServiceFeeRate(new BigDecimal(rateConfig.get("service_fee_rate").toString()));
		if(rateConfig.get("advance_rate")!=null) {
			calFeeRateConfig.setAdvanceRate(new BigDecimal(rateConfig.get("advance_rate").toString()));
		}
		calFeeRateConfig.setLateRate(new BigDecimal(rateConfig.get("late_rate").toString()));
		if(rateConfig.get("provision_rate")!=null) {
			calFeeRateConfig.setProvisionRate(rateConfig.get("provision_rate").toString());
		}
		log.info("rateConfig.get(\"extend\")!=null:{}", rateConfig.get("extend")!=null);
		if(rateConfig.get("extend")!=null&&rateConfig.get("extend").toString().length()>2) {
			String extendString = rateConfig.get("extend").toString();
			ExtendUpfrontRate extend = JSON.parseObject(extendString, ExtendUpfrontRate.class);
			log.info("extend:{},{}", extend,rateConfig.get("extend").toString());
			calFeeRateConfig.setExtendUpfrontRate(extend);
		}
		
	}
	public BigDecimal calV1Upfrontfee(BigDecimal originalUpfrontFee, String upAdminfeeProportion, BigDecimal proportion) {
		return originalUpfrontFee
		.multiply(new BigDecimal(upAdminfeeProportion)).divide(proportion,6,RoundingMode.HALF_UP);
	}
	public int getDiffDay(LocalDate startTime,LocalDate endTime) {

		return (int) (endTime.toEpochDay()-startTime.toEpochDay());
	}

}
