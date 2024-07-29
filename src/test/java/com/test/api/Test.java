package com.test.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.test.api.entity.*;
import com.test.api.entity.tob.ApplyTobParam;
import com.test.api.entity.vo.RepayByadvanceVo;
import com.test.api.mapper.DuesMapper;
import com.test.api.mapper.FinanceTradingMapper;
import com.test.api.mapper.UpfrontDuesMapper;
import com.test.api.pojo.ApiConfig;
import com.test.api.service.CostCalculationService;
import com.test.api.service.RepaymentService;
import com.test.api.service.TobService;
import com.test.api.util.ExcelUtil;
import com.test.api.util.HttpUtil;
import com.test.api.util.XmlPropertiesUtil;
import com.test.api.util.enty.DayaModel;
import com.test.api.util.preUtils.PreProcessorUtil;
import com.test.api.util.preUtils.RepaymentUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 
* 类说明
*/
@Slf4j
@SpringBootTest
public class Test {
	@Autowired
	UpfrontDuesMapper duesMapper;
	@Autowired
	FinanceTradingMapper  financeTradingMapper;
	
	@Autowired
	CostCalculationService costCalculationService;
	
	@Autowired
	RepaymentService repaymentService;
	@Autowired
	RepaymentUtils repaymentUtils;

	@Autowired
	DuesMapper Mapper;
	private TryCPlanInfo tryCPlanInfo;
	@Autowired
	PreProcessorUtil preProcessorUtil;
	@Autowired
	TobService tobService;
	//测试放款以及试算
	@org.junit.jupiter.api.Test
	public void ConfigTest() {
//		Map<String, Object> rateConfig = duesMapper.getRateConfig(0, "NS_maucash_short_B1-1_v1");
//		Map<String, Object> promotionRateConfig = duesMapper.getPromotionRateConfig("v1rate");
//		for (Entry<String, Object> entry: rateConfig.entrySet()) {
//			System.out.println("entry.getKey():"+entry.getKey()+"entry.getKey():"+entry.getValue());
//		}
//		for (Entry<String, Object> entry: promotionRateConfig.entrySet()) {
//			System.out.println("entry.getKey():"+entry.getKey()+"entry.getKey():"+entry.getValue());
//		}
		TryInfo tryInfo = new TryInfo();
//		 tryInfo = new TryInfo();
//		 tryInfo.setAmount(2400000);
//		 tryInfo.setTenor("3M");
//		 tryInfo.setSecondProductCode("NS_astra_maucash_short_B3-2_v2");
//		 tryInfo.setPromotionCode("NS_astra_maucash_short_B3-2_v2");
		 tryInfo.setLoanNumber("CL22082316390781056196149");
		costCalculationService.CalculateUpfrontDues(tryInfo);
	
	}
	@org.junit.jupiter.api.Test
	public void testTryCPlan() {
		System.out.println();
		 TryCPlanInfo tryCPlanInfo = new TryCPlanInfo();
		 tryCPlanInfo.setAmount("");
		 List<Map<String,Object>> list = new ArrayList<>();
		 Map<String,Object> map = new HashMap<>();
		 Map<String,Object> map2 = new HashMap<>();
		 Map<String,Object> map3 = new HashMap<>();
		 map.put("loanNumber", "PL22110409330040533279334");
		 map2.put("loanNumber", "CL22110320315009559181914");
//		 map3.put("loanNumber", "CL22051220342618685741859");
		 map.put("discountLFratio", "0.5");
		 map2.put("discountLFratio", "0.5");
//		 map3.put("discountLFratio", "0.5");
		 list.add(map);
		 list.add(map2);
//		 list.add(map3);
		 
//		 Map<String,Object> map4 = new HashMap<>();
//		 map4.put("loanNumber", "CL22041920353545030997651");
//		 list.add(map4);
		
		 tryCPlanInfo.setTryCList(list);
		repaymentService.repayByCplan(tryCPlanInfo);
	}

	@org.junit.jupiter.api.Test
	public void testEplan(){
		ArrayList<LoanTryEPlan> ePlanlist = new ArrayList<>();
		LoanTryEPlan loanTryE = new LoanTryEPlan();
		loanTryE.setDiscountLFratio("0.5");
		loanTryE.setDisDownPaymentRatio("0.5");
		loanTryE.setLoanNumber("CL22123014193559239772589");
		loanTryE.setTenor("2M");
		ePlanlist.add(loanTryE);

		LoanTryEPlan loanTryE2 = new LoanTryEPlan();
		loanTryE2.setDiscountLFratio("0.5");
		loanTryE2.setDisDownPaymentRatio("0.5");
		loanTryE2.setLoanNumber("PL22123015562843782119271");
		loanTryE2.setTenor("3M");
		ePlanlist.add(loanTryE2);
		log.info("ePlanlist{}", JSONObject.toJSONString(ePlanlist));
		repaymentService.repayByEplan(ePlanlist );
	}
	//8567093924     679835
	@org.junit.jupiter.api.Test
	public void testFplan(){
		LoanTryFPlan loanTryFPlan = new LoanTryFPlan();
		loanTryFPlan.setDiscountLFRatio("1");
		loanTryFPlan.setDiscountPrincipalRatio("0.3");
		loanTryFPlan.setDiscountServiceFeeRatio("1");
		List<String> loanNumbers = new ArrayList<>();
		loanNumbers.add("CL22091516100837759367240");
		loanNumbers.add("PL22091516100856272498766");
		loanTryFPlan.setLoanNumbers(loanNumbers);
		repaymentService.repayByFplan(loanTryFPlan);
	}
	
	@org.junit.jupiter.api.Test
	public void testrepaymentUtils() {
		repaymentUtils.repaymentByRebook();
	}
	@org.junit.jupiter.api.Test
	public void testAdvance(){
		//                CL22091620332591198753536
//		HashMap<String,BigDecimal> resultMap = (HashMap)repaymentService.repayByadvance("PL22091620400583746834675");
		RepayByadvanceVo repayByadvanceVo = (RepayByadvanceVo) repaymentService.repayByadvance("CL22110220343911157718153");

		log.info("repayByadvanceVo:{}", JSON.toJSONString(repayByadvanceVo));
		List<Dues> dues = repayByadvanceVo.getDues();
		//总的资金方利息+ 平台服务费
		long sumInAndSev= 0;
		long sumPrincipal = 0;
		for(Dues   due: dues){
			if("Fund_Interest".equalsIgnoreCase(due.getDueType())||"Service_Fee".equalsIgnoreCase(due.getDueType())
					||"Service_Fee_VAT".equalsIgnoreCase(due.getDueType())){
				sumInAndSev = sumInAndSev+ due.getRemainingAmount();
			}
			if("Principal".equalsIgnoreCase(due.getDueType())){
				sumPrincipal = sumPrincipal+ due.getRemainingAmount();
			}
		}
		long sumUnpay = new BigDecimal(sumInAndSev).add(new BigDecimal(sumPrincipal)).setScale(-3, RoundingMode.UP).longValue();
		log.info("资金方利息平台服务费以及税费{}，总的本金：{}，进位后总的应还款金额{}，进位费：{}"
				,sumInAndSev,sumPrincipal,sumUnpay,sumUnpay-sumInAndSev-sumPrincipal);
	}

	/**
	  * @Desc
	  * @Params 
	  * @Return 
	  * @author Faisal Mulya Santosa
	  * @Date 2024-07-29
	  **/
	@org.junit.jupiter.api.Test
	public void testSort(){
		QueryWrapper<Dues> queryWrapper = new QueryWrapper<Dues>();
		queryWrapper.eq("loan_id","CL22101414371378160788577");
		queryWrapper.eq("is_close",1);
		queryWrapper.orderByDesc("id");
		List<Dues> dues = Mapper.selectList(queryWrapper);
		log.info("排序前:{}",dues);
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
		log.info("排序前:{}",dues);

	}
	@org.junit.jupiter.api.Test
	public void testTriggerJob(){
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("loanNumber","CL22110220343911157718153");
		map.put("overDueday","5");
		//preProcessorUtil.setOverDue(map);

	}
	@org.junit.jupiter.api.Test
	public void testclendar(){
		//String转Date
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String createTime = "2023-04-01 23:59:59";

		try {
			Date date = simpleDateFormat.parse(createTime);
			Calendar instance = Calendar.getInstance();
			instance.setTime(date);
			//instance.add(instance.day);
			instance.set(Calendar.DAY_OF_MONTH, 1);
			instance.add(Calendar.DAY_OF_MONTH,-1);
			String format = simpleDateFormat.format(instance.getTime());
			System.out.println("format:"+format);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}


	}
	@org.junit.jupiter.api.Test
	public void testSalary() {
		Date date = new Date();
		Timestamp timeStamp = new Timestamp(date.getTime());
		financeTradingMapper.insertMonthlySalary(BigInteger.valueOf(6220077), 10000000, timeStamp, timeStamp);
	}
	public  static void testDevice(){
		try {
			log.info("1/0:{}",1/0);
		} catch (Exception e) {
			log.info("====={}",11);
			throw new RuntimeException(e);

		}
	}
	@org.junit.jupiter.api.Test
	public void testUpload()  {
		ApiConfig apiConfig1 = XmlPropertiesUtil.readXml("src/test/resources/apiConfig.xml");

		HttpUtil.doPostUpload(apiConfig1,"post","/awsom-moon/api/v3/loan/import/DAYA",""
				,"YNDAYA","src/test/resources/file/daya.xlsx");
	}
	@org.junit.jupiter.api.Test
	public void testEasyExcel(){
		List<DayaModel> dayaModels = ExcelUtil.easyReadExcel("src/test/resources/file/dayatemplate.xlsx", DayaModel.class);
		log.info("dayaModels:{}",dayaModels);
		ExcelUtil.easyWriteExcel("src/test/resources/file/daya1.xlsx",DayaModel.class,dayaModels);
		log.info("===========");

	}

	public static void main(String[] args) {
		log.info("最小:{}最大{}",Integer.MIN_VALUE,Integer.MAX_VALUE);
		log.info("最小:{}最大{}",Long.MIN_VALUE,Long.MAX_VALUE);

		String loginResp="{\n\t\"code\":0,\n\t\"message\":\"Permintaan berhasil\",\n\t\"result\":{\n\t\t\"enableABTest\":false,\n\t\t\"mobile\":\"833471680\",\n\t\t\"newUser\":true,\n\t\t\"passwordExpired\":false,\n\t\t\"productCode\":\"YN-MAUCASH\",\n\t\t\"temporaryLogin\":false,\n\t\t\"token\":\"uwkxglqueknka0895362875982352384\",\n\t\t\"userId\":716827,\n\t\t\"userProductType\":\"new\"\n\t}\n}";
		String s = JSONPath.read(loginResp, "$.result.userId").toString();
		BigInteger userId =  new BigInteger(s) ;
		log.info("s:{},userId:{}",s,userId);

		log.info("StringUtils.isNotBlank{}",StringUtils.isNotBlank(""));

		LocalDate now = LocalDate.now();
		long l = System.currentTimeMillis();
		log.info("currentTimeMillis:{}",l);
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String format = now.format(dateTimeFormatter);
		LocalDate parse = LocalDate.parse("2022-10-27", dateTimeFormatter);
		String format1 = parse.format(dateTimeFormatter);
		boolean b = now.compareTo(parse) > 0 ? true : false;
		log.info("now:{},format1:{},{}",format,format1,b);

		int a = -1;
		int abs = Math.abs(a);

		log.info("abs:{},a:{}",abs,a);

		LocalDate nownow = LocalDate.now();
		LocalDate localDate = now.plusDays(-5);
		System.out.println(localDate.format(dateTimeFormatter));
		log.info("========");

		try {
			testDevice();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		log.info("========");

		ReentrantLock reentrantLock = new ReentrantLock();
		reentrantLock.newCondition();
		reentrantLock.lock();
	}

}
