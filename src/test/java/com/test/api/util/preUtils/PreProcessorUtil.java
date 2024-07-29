package com.test.api.util.preUtils;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.ARRAY_MISMATCH_TEMPLATE;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.google.gson.JsonObject;
import com.test.api.entity.Dues;
import com.test.api.entity.vo.RepayByadvanceVo;
import com.test.api.mapper.DuesMapper;
import com.test.api.mapper.LoansMapper;
import com.test.api.pojo.ApiConfig;
import com.test.api.service.RepaymentService;
import com.test.api.util.JsonPathUtil;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.parser.Feature;
import com.baomidou.mybatisplus.core.conditions.segments.MatchSegment;
import com.dangdang.ddframe.job.lite.lifecycle.api.JobAPIFactory;
import com.dangdang.ddframe.job.lite.lifecycle.api.JobOperateAPI;
import com.dangdang.ddframe.job.lite.lifecycle.api.JobSettingsAPI;
import com.dangdang.ddframe.job.lite.lifecycle.api.ServerStatisticsAPI;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobSettings;
import com.google.common.base.Optional;
import com.test.api.cases.BaseCase;
import com.test.api.entity.TryInfo;
import com.test.api.entity.vo.UpfrontDuesVo;
import com.test.api.mapper.FinanceTradingMapper;
import com.test.api.pojo.AvailableList;
import com.test.api.pojo.Element;
import com.test.api.pojo.Header;
import com.test.api.service.CostCalculationService;
import com.test.api.util.HttpUtil;
import com.test.api.util.SignUtil;
import com.test.api.util.XmlPropertiesUtil;
import com.test.api.util.function.Mobile;
import com.test.api.util.function.Nik;

import lombok.extern.slf4j.Slf4j;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
@Slf4j
@Component
//@SpringBootTest
public class PreProcessorUtil {
	@Autowired
	FinanceTradingMapper extMapper;
	@Autowired
	@Qualifier("fatRabbitTemplate")
	RabbitTemplate fatRabbitTemplate;

	@Autowired
	@Qualifier("devRabbitTemplate")
	RabbitTemplate devRabbitTemplate;
	@Autowired
	CostCalculationService costCalculationService;
	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	RepaymentService repaymentService;

	@Autowired
	LoansMapper loansMapper;

	@Autowired
	DuesMapper duesMapper;
	public static Map<String, Object> global_map = new HashMap<String, Object>();
	public static Map<String, Object> getGlobal_map() {
		return global_map;
	}
	public static void setGlobalMap(String key, String value) {
		global_map.put(key, value);
	}
	/**
	 * 申请额度
	 */
	
	public void initXmlData(String configXmlPath) {
		
		BaseCase.apiConfig = XmlPropertiesUtil.readXml(configXmlPath);
		
		System.out.println("@BeforeSuite"+BaseCase.apiConfig);
	}
	
	public void initProperties(String propertiesPath) {
		BaseCase.imageMap =  XmlPropertiesUtil.readProperties(propertiesPath);
	}
	/**
	 * 申请进件
	 * @param map
	 */
	public void applyLimit(HashMap<String,Object> map,ApiConfig apiConfigTest) {
		//清理进件token
		List<Header> applyHeaderList = BaseCase.apiConfig.getHeaderList();
		for(Header header:applyHeaderList) {
			List<Element> elementList = header.getElementList();
			if(header.getName().equalsIgnoreCase("applyHeader")) {
				ListIterator<Element> listIterator = elementList.listIterator();
				while(listIterator.hasNext()) {
					Element next = listIterator.next();
					if(next.getName().equalsIgnoreCase("x-user-token")) {
						listIterator.remove();
					}
				}
			}
		}
		BaseCase.apiConfig.setHeaderList(applyHeaderList);

		log.info("===BaseCase.apiConfig{}",BaseCase.apiConfig);
		//发送短信
		String mobile = new Mobile().excute(null);
		map.put("mobile", mobile);
		String nik = new Nik().excute(null);
		String sendSmsCodeParams = "mobile="+mobile+"&channelType=0";
		String sendSmsCodeResp = HttpUtil.execute(apiConfigTest, "get", "/awsom-komodo/api/v3/noauth/user/login/send-code", sendSmsCodeParams,"applyHeader");
		int smsCodeId = (int) JSONPath.read(sendSmsCodeResp, "$.result");

		loansMapper.insertWhitelist(mobile,nik);
		//登录
		/**
		 */
		UUID uuid = UUID.randomUUID();
		String  wd_device_id="7fc8c6c95922b09681f7f547ac386a72";
		  wd_device_id=uuid.toString().replace("-","");
		String loginParams ="{\"mobile\":\""+ mobile+"\",\"smsCode\":{\"smsCodeId\":"+smsCodeId+","
				+ "\"smsCodeValue\":\"8888\"},\"googleFcmToken\":null,\"googleAdvertisingId\""
				+ ":\"9ad0690e-3991-489f-99ac-b37d9844f891\",\"deviceInfo\":{\"app_name\":\"maucash\","
				+ "\"app_version\":\"2.4.10_test\",\"model\":\"OPPO A57\",\"sdk\":\"23\",\"brand\":\"OPPO\","
				+ "\"os_version\":\"6.0.1\",\"wd_device_id\":\""+wd_device_id+"\",\"platform\":"
				+ "\"android\",\"sim_serial_number\":null,\"sim_card_number\":null,\"device_id\":null,\"fcm_token\":null,\"channel\":null,"
				+ "\"advertising_id\":\"9ad0690e-3991-489f-99ac-b37d9844f891\"}}";
		String loginResp = HttpUtil.execute(apiConfigTest, "post", "/awsom-user/api/v3/noauth/user-login", loginParams,"applyHeader");
		String token = (String) JSONPath.read(loginResp, "$.result.token");
	//	long userId =  (long) JSONPath.read(loginResp, "$.result.userId");
		String ids =   JSONPath.read(loginResp, "$.result.userId").toString();
		BigInteger userId = new BigInteger(ids);
		map.put("userId", userId);
		log.info("token{}",token);
		List<Header> headerList = apiConfigTest.getHeaderList();
		headerList.forEach(x->{
			if(x.getName().equals("applyHeader")) {
				x.getElementList().add(new Element("x-user-token",token));
			}
		});

		log.info(Thread.currentThread().getName()+"----"+apiConfigTest.hashCode()+"apiConfigTest:{}"+apiConfigTest);

		String juxinliParams = "source=sea&abTestType=juxinli";
		String juxinliResp =  HttpUtil.execute(apiConfigTest, "get", "/awsom-komodo/api/v3/wedefend/tools/getwebsites", juxinliParams,"applyHeader");
		String tag = (String) map.get("tag");
		String[] taglist = {"B1-2","B2-3","B2-4","B2-13","B2-12"};
		List<Object> asList = Arrays.asList(taglist);
		if(asList.contains(tag)) {
			String email = "test"+System.currentTimeMillis()+"@fifgroup.astra.co.id";
			//认证astra
			String astraParam= "{\"email\":\""+email+"\",\"nameOfEmployer\":\"PT Federal International Finance (FIFGROUP)\",\"nik\":\""+nik+"\",\"staffId\":\"9990\"}";
			String checkInfoResp = HttpUtil.execute(apiConfigTest, "post", "/awsom-application/api/v1/cooperation/checkInfo", astraParam,"applyHeader");
		}
		//个人信息
		String email = "test"+System.currentTimeMillis()+"@welab-inc.com";
		String userProfileParams="{\"name\":\"test\",\"nik\":\""+nik+"\",\"email\":\""+email+"\",\"birthplace\":\"Hongkong\","
				+ "\"residencePhone\":\"0755-5558554554\",\"ktpAddress\":{\"postcode\":\"80235\",\"province\":\"Bali\",\"city\":\"Denpasar\",\"district\""
				+ ":\"Denpasar Timur\",\"village\":\"Sumerta\",\"streetLine\":\"alibaba\",\"provinceId\":\"10\",\"cityId\":\"1011\",\"area\":\"Denpasar Timur\""
				+ ",\"areaId\":\"1011102\",\"villageId\":\"1011102108\"},\"rt\":\"550\",\"rw\":\"528\",\"homeAddress\":{\"postcode\":\"80235\",\"province\":\"Bali\",\"city\":\"Denpasar\",\"district\":\"Denpasar Timur\",\"village\":\"Sumerta\",\"streetLine\":\"tencent1\",\"homeKtpAddrSame\":false,\"provinceId\":\"10\",\"cityId\":\"1011\",\"area\":\"Denpasar Timur\",\"areaId\":\"1011102\",\"villageId\":\"1011102108\"}" +
				",\"mobile\":\"830888894402\",\"homeKtpAddrSame\":false,\"lockAttrs\":[\"email\"],\"maritalStatus\":\"10\",\"educationLevel\":\"13\",\"residence\":\"10\",\"religion\":\"13\",\"momSurname\":\"testtesttest\"}";
		String ss ="";
		String userProfileResp = HttpUtil.execute(apiConfigTest, "post", "/awsom-komodo/api/v3/user-profile", userProfileParams,"applyHeader");
		//添加照片信息
		String content = BaseCase.imageMap.get("content");
		String documentParams = "[{\"mimeType\":\"company_id_card\",\"content\":\""+content+"\"}]";
		String documentResp = 	HttpUtil.execute(apiConfigTest, "post", "/awsom-komodo/api/v3/document", documentParams,"applyHeader");
		//工作信息
		String workProfileParams = "{\"nameOfEmployer\":\"PT Federal International Finance (FIFGROUP)\",\"department\":\"Bbbnn\",\"salaryDay\":\"25\",\"workPhone\":"
				+ "\"5754-8754554646\",\"workAddress\":{\"postcode\":\"80235\",\"province\":\"Bali\",\"city\":\"Denpasar\",\"district\":\"Denpasar Timur\",\"village\":"
				+ "\"Sumerta\",\"streetLine\":\"tencent\",\"provinceId\":\"10\",\"cityId\":\"1011\",\"area\":\"Denpasar Timur\",\"areaId\":\"1011102\",\"villageId\":\"1011102108\"},"
				+ "\"yearOfService\":51,\"monthlySalary\":999999999,\"dueDate\":25,\"occupation\":\"5\",\"employmentStatus\":\"13\",\"industry\":\"10\",\"position\":\"12\",\"salaryMethod\":\"19\",\"incomeSource\":\"10\"}";
		String workProfileResp = HttpUtil.execute(apiConfigTest, "post", "/awsom-komodo/api/v3/work-profile", workProfileParams,"applyHeader");
		//工资照片
		String bankDocumentParams = "[{\"mimeType\":\"bank_statement_3_months\",\"content\":\""+content+"\"}]";
		String bankDocumentResp = 	HttpUtil.execute(apiConfigTest, "post", "/awsom-komodo/api/v3/document", bankDocumentParams,"applyHeader");

		//紧急联系人
		String mobile1 = new Mobile().excute(null);
		String mobile2 = new Mobile().excute(null);

		String contactsParams = "[{\"name\":\"Amy\",\"mobile\":\""+mobile1+"\",\"relationship\":\"12\",\"remark\":\"different_address\"}"
				+ ",{\"name\":\"Mark\",\"mobile\":\""+mobile2+"\",\"relationship\":\"14\",\"remark\":\"same_address\"}]";
		
		String contactsRsp =HttpUtil.execute(apiConfigTest, "post","/awsom-komodo/api/v3/contacts",contactsParams,"applyHeader");
		HttpUtil.execute(apiConfigTest, "post","/awsom-komodo/api/v3/credit-limit/pre-apply","{}","applyHeader");
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		//添加ktp
		String ktpParams = "[{\"mimeType\":\"KTP\",\"content\":\""+BaseCase.imageMap.get("content")+"\"}]";
		String ktpResp =HttpUtil.execute(apiConfigTest, "post","/awsom-komodo/api/v3/document",ktpParams,"applyHeader");
		//人脸识别
		String faceidParams = "{\"delta\":\"${delta}\",\"imageBestBase64\":\"${BestBase64image}\"}";
		String faceidResp = HttpUtil.execute(apiConfigTest, "post","/awsom-komodo/api/v3/faceid/dev",faceidParams,"applyHeader");
		
		//授权
		String thirdPartyParams="{\"authSuccess\":\"yes\",\"userId\":\""+mobile+"\","+"\"orderId\":\"93d9ac85e0f4452f93a454731e217fcd\",\"taskId\":\"15f7cbd97b55464db14a1c6c051ae06e\",\"callbackExtendPatams\":\"\",\"authType\":\"bni\"}";
		
		String thirdPartyResp =  HttpUtil.execute(apiConfigTest, "post","/awsom-komodo/api/v3/wedefend/tools/thirdPartyjump",thirdPartyParams,"applyHeader");
		//提交信息 "wdDeviceId":"572bd09048d8f2688cd76a7aa58026d7
		String wdDeviceId = "572bd09048d8f2688cd76a7aa58026d7"+mobile;
		String confirmParams = "{\"wdDeviceId\":\""+wdDeviceId+"\",\"location\":{\"latitude\":22.53251,\"longitude\":113.899497,\"addressline\":\"China\"}}";
		String confirmResp = HttpUtil.execute(apiConfigTest, "post","/awsom-komodo/api/v3/credit-limit/confirm","{}","applyHeader");
		String code = JSONPath.read(confirmResp, "$.code").toString();
		int temp =0;
		while(code.equalsIgnoreCase("10930042")&&temp<60){
			try {
				Thread.sleep(1000);
				temp++;
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			 confirmResp = HttpUtil.execute(apiConfigTest, "post","/awsom-komodo/api/v3/credit-limit/confirm","{}","applyHeader");
			 code =  JSONPath.read(confirmResp, "$.code").toString();
		}
		String loanLimitNumber = (String) JSONPath.read(confirmResp, "$.result");
		map.put("loanLimitNumber", loanLimitNumber);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 给定额度
	 */
	public void approvaLimit(HashMap<String,Object> map, ApiConfig apiConfigTest) {
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Date date = new Date();
		Timestamp timeStamp = new Timestamp(date.getTime());
		log.info("(int)map.get(\"userId\")===={}",map.get("userId"));
		BigInteger userId = (BigInteger) map.get("userId");
		extMapper.insertMonthlySalary(userId, 10000000, timeStamp, timeStamp);
		String mqBody = "{\"approveStatusNoticBaseDTO\":{\"approvedAmount\":10000000,\"approvedPayLaterAmount\":12500000,\"tag\":\""+map.get("tag")+"\"},\"loanNumber\":\""+map.get("loanLimitNumber")+"\",\"loanStatus\":\"APPROVED\",\"updateAt\":1637130772565}";
		log.info("消息体mqBody{}", mqBody);
		log.info("apiConfigTest.getRootUrl():{}",apiConfigTest.getRootUrl());
		if(apiConfigTest.getRootUrl().contains("dev.maucash.id")){
			log.info("RabbitTemplate:dev");
			devRabbitTemplate.convertAndSend("welab.sea.application.loans.status.update", new String(mqBody));
		}else{
			log.info("RabbitTemplate:fat");
			fatRabbitTemplate.convertAndSend("welab.sea.application.loans.status.update", new String(mqBody));

		}
		log.info("消息体mqBody{}", mqBody);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 使用额度
	 * @param map
	 */
	public void useLimit(HashMap<String,Object> map, ApiConfig apiConfigTest) {
		TryInfo tryInfo = new TryInfo();
		//调用试算接口参数
		String repayCalculateParams = "amount="+map.get("amount")+"&secondProdCode="+map.get("secondProdCode")+"&tenor="+map.get("tenor");
		//提交订单接口
		String useParams = "{\"tenor\":\""+map.get("tenor")+"\",\"amount\":"+map.get("amount")+",\"loanReason\":\"25\",\"position\":{\"latitude\":22.5323268"
				+ ",\"longitude\":113.8996402},\"secondProdCode\":\""+map.get("secondProdCode")+"\",\"promotionCode\":null}";
		//判断是否配置了优惠码
		if(map.get("promotionCode")!=null&&map.get("promotionCode").toString().length()>0) {
			//优惠码不为空  试算借款传入  
			tryInfo.setPromotionCode(map.get("promotionCode").toString());
			int type = 1;
			//优惠码类型
			if(map.get("promotionType")!=null&&map.get("promotionType").toString().length()>0){
				String promotionType = map.get("promotionType").toString();
				if(promotionType=="discount") {
					type = 1;
				}else if(promotionType=="rate") {
					type = 2;
				}else if(promotionType=="decrease") {
					type = 3;
				}
			}
			String promotionparams = "{\r\n" + 
					"  \"type\": \"1\",\r\n" + 
					"  \"activityCode\": \""+map.get("promotionCode")+"\",\r\n" + 
					"  \"operateSource\": 1,\r\n" + 
					"  \"amount\": "+map.get("amount")+",\r\n" + 
					"  \"tenor\": \""+map.get("tenor")+"\",\r\n" + 
					"  \"secondProductCode\": \""+map.get("secondProdCode")+"\"\r\n" + 
					"}";
			 HttpUtil.execute(apiConfigTest, "post","//awsom-komodo/api/v3/app/newActivityCodeStatus",promotionparams,"applyHeader");

			repayCalculateParams = "amount="+map.get("amount")+"&secondProdCode="+map.get("secondProdCode")+"&promotionCode="+map.get("promotionCode")+"&tenor="+map.get("tenor");
			useParams = "{\"tenor\":\""+map.get("tenor")+"\",\"amount\":"+map.get("amount")+",\"loanReason\":\"25\",\"position\":{\"latitude\":22.5323268"
					+ ",\"longitude\":113.8996402},\"secondProdCode\":\""+map.get("secondProdCode")+"\",\"promotionCode\":\""+map.get("promotionCode")+"\"}";
		}

		String repayCalculateResp = HttpUtil.execute(apiConfigTest, "get", "/awsom-komodo/api/v3/finances/repayCreditCalculate", repayCalculateParams,"applyHeader");
		String CalculateResp =  JSONPath.read(repayCalculateResp, "$.result").toString();
		tryInfo.setAmount(Long.valueOf(map.get("amount").toString().toString()));
		tryInfo.setSecondProductCode(map.get("secondProdCode").toString());
		tryInfo.setTenor(map.get("tenor").toString());
		UpfrontDuesVo calculateUpfrontDues = costCalculationService.CalculateUpfrontDues(tryInfo);
		//校验试算返回的费用是否正确  砍头费 用户到手金额  还款金额 以及总服务费
		HashMap calMap = JSON.parseObject(CalculateResp, HashMap.class);
		Map<Object, Object> trASsertionMap = new HashMap();
		// 砍头费
		Boolean upfrontFeeAssertion = calculateUpfrontDues.getUpfrontFeeVO().getUpfrontFee().compareTo(new BigDecimal(calMap.get("adminFee").toString()))==0?true:false;
		// 用户收到的钱
		Boolean receivedAssertion = calculateUpfrontDues.getUpfrontFeeVO().getReceivedMoney().compareTo(new BigDecimal(calMap.get("receivedAmount").toString()))==0?true:false;
		//还款金额
		Boolean repaymentAssertion = calculateUpfrontDues.getDuesVo().getRepaymentAmount().compareTo(new BigDecimal(calMap.get("payable").toString()))==0?true:false;
		//总服务费
		Boolean interestFeeAssertion =null;
		if(tryInfo.getPromotionCode()==null||tryInfo.getPromotionCode().isEmpty()) {
			interestFeeAssertion = calculateUpfrontDues.getDuesVo().getTotaServiceFee().compareTo(new BigDecimal(calMap.get("orgInterestFee").toString()))==0?true:false;
			map.put("interestFeeTrResp", calMap.get("orgInterestFee").toString());
			map.put("interestFeeTrCal", calculateUpfrontDues.getDuesVo().getTotaServiceFee());

		}else {
			//优惠后的服务费
			interestFeeAssertion = calculateUpfrontDues.getDuesVo().getTotaServiceFee().compareTo(new BigDecimal(calMap.get("interestFee").toString()))==0?true:false;
			map.put("interestFeeTrResp", calMap.get("interestFee").toString());
			map.put("interestFeeTrCal", calculateUpfrontDues.getDuesVo().getTotaServiceFee());


			//优惠前的服务费
			 //优惠前的砍头费
			 //优惠后的砍头费
		}
		map.put("upfrontFeeTrResp", calMap.get("adminFee").toString());
		map.put("receivedMoneyTrResp", calMap.get("receivedAmount").toString());
		map.put("repaymentAmountTrResp", calMap.get("payable").toString());
		
		map.put("upfrontFeeTrCal", calculateUpfrontDues.getUpfrontFeeVO().getUpfrontFee());
		map.put("receivedMoneyTrCal", calculateUpfrontDues.getUpfrontFeeVO().getReceivedMoney());
		map.put("repaymentAmountTrCal", calculateUpfrontDues.getDuesVo().getRepaymentAmount());
		
		trASsertionMap.put("upfrontFeeTrAssertion", upfrontFeeAssertion);
		trASsertionMap.put("receivedTrAssertion", receivedAssertion);
		trASsertionMap.put("repaymentTrAssertion", repaymentAssertion);
		trASsertionMap.put("interestFeeTrAssertion", interestFeeAssertion);
		map.put("trASsertionMap", trASsertionMap);
		String saveBankParams ="";
		String disbursementMethodParams ="";
		if("online".equalsIgnoreCase((String) map.get("disburseType"))) {
			//绑卡信息
			extMapper.insertBankAccount((String) map.get("mobile"));
			 saveBankParams = "{\r\n" + 
					"  \"userName\": \"test\",\r\n" + 
					"  \"bankCode\": \"BCA\",\r\n" +
					"  \"bankName\": \"BANK CENTRAL ASIA\",\r\n" +
					"  \"bankAccountNumber\": \""+map.get("mobile")+"\"}";
			 HttpUtil.execute(apiConfigTest, "post","/awsom-komodo/api/v3/finances/saveBankCard",saveBankParams,"applyHeader");
			 disbursementMethodParams ="{\"disbursementMethod\": \"online\"}";
		}else if("fif".equalsIgnoreCase((String) map.get("disburseType"))) {
			disbursementMethodParams = "{\"disbursementMethod\":\"fif_branch\",\"branchId\":626}";
		}
		
		
		//保存放款方式
		HttpUtil.execute(apiConfigTest, "post","/awsom-komodo/api/v3/save-disbursement-method",disbursementMethodParams ,"applyHeader");

		//
		//查看放款方式
		// welab-application/api/v1/get-disbursement-method
		String getDisbursementResp =  HttpUtil.execute(apiConfigTest, "get","/awsom-komodo/api/v3/get-disbursement-method","","applyHeader");
		//银行卡号
		//借款原因
		String loansReasonSetResp = HttpUtil.execute(apiConfigTest, "post","/awsom-komodo/api/v3/loans-reason-set","{\"reason\":\"25\"}","applyHeader");
		//使用
//		String useParams = "{\"tenor\":\""+map.get("tenor")+"\",\"amount\":"+map.get("amount")+",\"loanReason\":\"25\",\"position\":{\"latitude\":22.5323268"
//				+ ",\"longitude\":113.8996402},\"secondProdCode\":\""+map.get("secondProdCode")+"\",\"promotionCode\":null}";
		String useResp = HttpUtil.execute(apiConfigTest, "post","/awsom-komodo/api/v3/credit-limit/use",useParams,"applyHeader");
		String loanNumber = (String) JSONPath.read(useResp, "$.result");
		map.put("loanNumber", loanNumber);
		//查看合同
		String loanSignResp = HttpUtil.execute(apiConfigTest, "post","/awsom-komodo/api/v3/loan-sign","{\"loanNumber\":\""+map.get("loanNumber")+"\"}","applyHeader");

		//application_agreement_sign
		String sendVCodeParms = "channelType=0&mobile="+map.get("mobile")+"&isSendMail=true&appTags=application_agreement_sign";
		String sendVCodeResp = HttpUtil.execute(apiConfigTest, "get","/awsom-komodo/api/v3/agreementSign/sendVCode",sendVCodeParms,"applyHeader");
		int resultSmsCode = (int) JSONPath.read(sendVCodeResp, "$.result");
		//签约agreement-sign
		String disbursementMethodType="";
		if("online".equalsIgnoreCase((String) map.get("disburseType"))) {
			disbursementMethodType = "online";
		}else if("fif".equalsIgnoreCase((String) map.get("disburseType"))) {
			disbursementMethodType = "fif_branch";
		}
		String agreementSignParams ="{\"loanNumber\":\""+map.get("loanNumber")+"\",\"smsCodeDTO\":{\"smsCodeId\":"+resultSmsCode+","
				+ "\"smsCodeValue\":\"8888\",\"mobile\":\""+map.get("mobile")+"\"},\"disbursementMethodType\":\""+disbursementMethodType+"\",\"onlineBankDTO\":{\"bankAccount\":\""+map.get("mobile")+"\",\"bankCode\":\"MANDIRI\",\"bankName\":\"MANDIRI\"}}";
		
		String agreementSignResp = HttpUtil.execute(apiConfigTest, "post","/awsom-komodo/api/v3/agreement-sign",agreementSignParams,"applyHeader");
		if("fif".equalsIgnoreCase((String) map.get("disburseType"))) {
			String disburseCode = (String) JSONPath.read(agreementSignResp, "$.result.disburseCode");
			takeOutMoney(disburseCode,apiConfigTest);
		}
	}
	/**
	 * 
	 */
	public void takeOutMoney(String disburseCode, ApiConfig apiConfigTest) {
		List<Header> headerList =apiConfigTest.getHeaderList();
		
		for(Header header:headerList) {
			if(header.getName().equalsIgnoreCase("branchesweb")) {
				List<Element> elementList = header.getElementList();
				Iterator<Element> iterator = elementList.iterator();
				while(iterator.hasNext()) {
					Element next = iterator.next();
					if(next.getName().equalsIgnoreCase("x-user-token")) {
						//取款
						String disburseResp = HttpUtil.execute(apiConfigTest, "post","/awsom-application/api/v1/disburse/confirm",
								"{\"disburseCode\": \""+disburseCode+"\"}","branchesweb");
						return;
					}
				}
			}
		}


		//登录线下门店
		String loginBranch = HttpUtil.execute(apiConfigTest, "post","/awsom-user/api/v1/user-login","{\r\n" +
				"  \"mobile\": \"8000000666\",\r\n" + 
				"  \"passwd\": \"Aa_123456\"\r\n" + 
				"}","branchesweb");	
		String token = (String) JSONPath.read(loginBranch, "$.result.token");
		headerList.forEach(x->{
			if(x.getName().equalsIgnoreCase("branchesweb")) {
				x.getElementList().add(new Element("x-user-token", token));
				x.getElementList().add(new Element("x-user-mobile", "8000000666"));
			}
			
		});
		//取款
		String disburseResp = HttpUtil.execute(apiConfigTest, "post","/awsom-application/api/v1/disburse/confirm",
				"{\"disburseCode\": \""+disburseCode+"\"}","branchesweb");

		
	}
	/**
	  * @Desc
	  * @Params
	  * @Return 
	  * @author Faisal Mulya Santosa
	  * @Date 2024-07-29
	  **/
	public void setOverDue(HashMap<String,Object>  paramtersMap){

		String loanNumber = paramtersMap.get("loanNumber").toString();
		int overDueDate = Integer.parseInt(paramtersMap.get("overDueday").toString());
		LocalDate now = LocalDate.now();
		LocalDate localDate = now.plusDays(-overDueDate);

		QueryWrapper<Dues> wrapper = new QueryWrapper();
		wrapper.eq("loan_id",loanNumber);
		wrapper.eq("is_close",0);
		List<Dues> dues = duesMapper.selectList(wrapper);
		Collections.sort(dues, new Comparator<Dues>() {
			@Override
			public int compare(Dues o1, Dues o2) {
				if(o1.getDueIndex().compareTo(o2.getDueIndex())==0){
					return o1.getRepayIndex().compareTo(o2.getRepayIndex());
				}
				return o1.getDueIndex().compareTo(o2.getDueIndex());
			}
		});
		Integer dueIndex = dues.get(0).getDueIndex();
		UpdateWrapper<Dues> wrapper2 = new UpdateWrapper();
		wrapper2.eq("loan_id",loanNumber);
		wrapper2.eq("due_index",dueIndex);
		wrapper2.set("due_date",localDate);
		int update = duesMapper.update(null, wrapper2);
		if(update>0){
			log.info("duedate更新成功，{}",update);
		}
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		HashMap<String, String> jobParameterMap = new HashMap<>();
		jobParameterMap.put("loanNumber",loanNumber);
		triggerJob("welab-loan-finance-job","calculatorOverdueJob",JSONObject.toJSONString(jobParameterMap));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 触发转账定时任务
	 */
	public void triggerScheduledTask() {
		
		JobOperateAPI createJobOperateAPI = JobAPIFactory.createJobOperateAPI("192.168.114.6:2181", 
				"welab-pay-gateway-job", Optional.of("welabsea:sea@123"));
		log.info("===createJobOperateAPI{}",createJobOperateAPI);
		createJobOperateAPI.trigger(Optional.of("paymentJob"),Optional.<String>absent());
	}

	public void triggerJob(String jobSpace,String jobName,String jobParameter){
		JobOperateAPI createJobOperateAPI = JobAPIFactory.createJobOperateAPI("192.168.114.6:2181",
				jobSpace, Optional.of("welabsea:sea@123"));
		if(StringUtils.isNotBlank(jobParameter)&&jobParameter.length()>2){
			JobSettingsAPI jobSettingsAPI = JobAPIFactory.createJobSettingsAPI("192.168.114.6:2181",
					jobSpace, Optional.of("welabsea:sea@123"));
			JobSettings jobSettings = jobSettingsAPI.getJobSettings(jobName);
			jobSettings.setJobParameter(jobParameter);
			jobSettingsAPI.updateJobSettings(jobSettings);
			log.info("jobParameter:{}",jobParameter);
		}
		createJobOperateAPI.trigger(Optional.of(jobName),Optional.<String>absent());
	}

	public void testJJ() {
		log.info("========createJobOperateAPI{}");
//		new JobAPIServiceImpl("192.168.114.6:2181", "welab-loan-finance-job") ;
//		ServerStatisticsAPI createServerStatisticsAPI = JobAPIFactory.createServerStatisticsAPI("192.168.114.6:2181", 
//				"welab-loan-finance-job", Optional.of("welabsea:sea@123"));
		JobSettingsAPI jobSettingsAPI = JobAPIFactory.createJobSettingsAPI("192.168.114.6:2181", 
				"welab-loan-finance-job", Optional.of("welabsea:sea@123"));
		JobSettings jobSettings = jobSettingsAPI.getJobSettings("creditUserTagRuleDelayTriggerJob");
		log.info("====jobClass{},JobParameter{}",jobSettings.getJobClass(),jobSettings.getJobParameter());
		jobSettings.setJobParameter("{'triggerTime':'2022-12-10'}");
		log.info("====jobClass{},JobParameter{}",jobSettings.getJobClass(),jobSettings.getJobParameter());
		jobSettingsAPI.updateJobSettings(jobSettings);
		
		JobOperateAPI createJobOperateAPI = JobAPIFactory.createJobOperateAPI("192.168.114.6:2181", 
				"welab-loan-finance-job", Optional.of("welabsea:sea@123"));
		log.info("===createJobOperateAPI{}",createJobOperateAPI);
		
		createJobOperateAPI.trigger(Optional.of("creditUserTagRuleDelayTriggerJob"),Optional.<String>absent());
	}
	public HashMap<String,Object> randomAmountTenor(HashMap<String,Object> map, ApiConfig apiConfigTest){
		String params = "{}";
		String availableListResp = HttpUtil.execute(apiConfigTest, "post","/awsom-komodo/api/v3/noauth/product/credit-limit/list",params,"applyHeader");

		List<AvailableList> list = JSON.parseArray(JSONPath.read(availableListResp, "$.result").toString(), AvailableList.class);
		String  secondProdCode = (String) map.get("secondProdCode");
		Collections.shuffle(list);
		AvailableList shuffleAvailableList = null;
		for(AvailableList availableList:list) {
			if(availableList.getSecondProdCode().contains(secondProdCode)&&availableList.getAmountMin()>0&&availableList.getAmountMin()>0) {
				shuffleAvailableList = availableList;
				break;
			}
		}
		if(shuffleAvailableList==null){
			log.info("标签{}，没有配置{}这个产品",map.get("tag"),secondProdCode);
		}
		//随机取出借款金额
		List<Integer> amountList = shuffleAvailableList.getAmountList();
		int size = amountList.size();
		//随机出来的金额
		Integer amount =0;
		if(size>1) {
			int random = (int) Math.random()*(size-1);
			 amount = amountList.get(random);
		}else if(size==1) {
			 amount = amountList.get(0);
		}
		map.put("secondProdCode", shuffleAvailableList.getSecondProdCode());
		map.put("tenor", shuffleAvailableList.getTenor());
		map.put("amount", amount.toString());
		return map;
	}
	public void useLimitByPL(HashMap<String,Object> map, ApiConfig apiConfigTest) {
		
		Random random = new Random();
		int nextInt = random.nextInt(1000000000);
		// String mobile ="8043943162";
		String orderNo = "test"+nextInt;
		// String merchantCode = "testvintFIFADA2";
		// String goodsCode = "testvintFIFADA2";
		// String secret = "vhniaDidS4OKxbLOvW19Pw";
		// String appId = "FIFADA";
		// String amount = "900000";
		// String tenor = "6M";
		String mobile  =(String) map.get("mobile");
		String merchantCode = (String) map.get("merchantCode");
		String goodsCode = (String) map.get("goodsCode");
		String secret = (String) map.get("secret");
		String appId = (String) map.get("appId");
		String amount = (String) map.get("amount");
		String tenor = (String) map.get("tenor");
		 
		long timestamp = System.currentTimeMillis()/1000;
		String createOrderParam ="{\r\n" + 
				"  \"appId\": \""+appId+"\",\r\n" + 
				"  \"timestamp\": "+timestamp+",\r\n" + 
				"  \"v\": \"1.0\",\r\n" + 
				"  \"signature\": \"49174AA4D0F5B6E16F32AC696359C5261\",\r\n" + 
				"  \"body\": {\r\n" + 
				"    \"merchantCode\": \""+merchantCode+"\",\r\n" + 
				"    \"goodsCode\": \""+goodsCode+"\",\r\n" + 
				"    \"mobile\": \""+mobile+"\",\r\n" + 
				"    \"amount\": "+amount+",\r\n" + 
				"    \"transType\": \"QR\",\r\n" + 
				"    \"orderNo\": \""+orderNo+"\",\r\n" + 
				"    \"tenor\": \""+tenor+"\",\r\n" + 
				"    \"promotionCode\": \"\",\r\n" + 
				"    \"qrisMechantPan\": \"11111111\",\r\n" + 
				"    \"qrisMerchantLocation\": \"11111111\",\r\n" + 
				"    \"qrisMerchantName\": \"11111111\",\r\n" + 
				"    \"qrisAcquirerName\": \"11111111\",\r\n" + 
				"    \"qrisMerchantCode\": \"11111111\",\r\n" + 
				"    \"qrisAcquirerCode\": \"11111111\"\r\n" + 
				"  }\r\n" + 
				"}";
		HashMap<String,Object> createOrderMap = JSON.parseObject(createOrderParam, HashMap.class);
		createOrderMap.remove("signature");
		//获取签名
		String signature = SignUtil.getSignature(JSON.toJSONString(createOrderMap), secret);
		System.out.println("签名前"+createOrderParam);
		System.out.println("signature="+signature);
		//替换签名
		createOrderMap.put("signature", signature);
		System.out.println("签名后"+JSON.toJSONString(createOrderMap));
		//生成订单
		 String createOrderResponse = HttpUtil.execute(apiConfigTest, "post","/awsom-sun/api/credit/createOrder",JSON.toJSONString(createOrderMap),"applyHeader");
		 System.out.println("createOrderResponse："+createOrderResponse);
		 String loanNumber = (String) JSONPath.read(createOrderResponse, "$.result");
			map.put("loanNumber", loanNumber);
//		 //获取签约地址
//		 String signPageParam = "{\r\n" + 
//		 		"  \"appId\": \""+appId+"\",\r\n" + 
//		 		"  \"timestamp\": "+timestamp+",\r\n" + 
//		 		"  \"v\": \"1.0\",\r\n" + 
//		 		"  \"signature\": \"${signaturebb}\",\r\n" + 
//		 		"  \"body\": {\r\n" + 
//		 		"    \"merchantCode\": \"gardahome\",\r\n" + 
//		 		"    \"orderNo\": \""+orderNo+"\",\r\n" + 
//		 		"    \"redirectUrl\": \"https://www.baidu.com\",\r\n" + 
//		 		"    \"mobile\": \""+mobile+"\"\r\n" + 
//		 		"  }\r\n" + 
//		 		"}";
//		 HashMap<String,Object> signPageMap = JSON.parseObject(signPageParam, HashMap.class);
//		 signPageMap.remove("signature");
//		 String signPageSignature = SignUtil.getSignature(JSON.toJSONString(signPageMap), secret);
//		 signPageMap.put("signature", signPageSignature);
//		 String signPageResponse = HttpUtil.execute(BaseCase.apiConfig, "post","/awsom-sun/api/credit/signPage",JSON.toJSONString(signPageMap),"applyHeader");	
//		 String url = (String) JSONPath.read(signPageResponse, "$.result");
//		//获取token
//		 String[] array = url.split("&");
//		 String tokens ="";
//		 for (int i = 0; i < array.length; i++) {
//			log.info(array[i]);
//			if(array[i].contains("token")) {
//				String[] tokenArr = array[i].split("=");
//				tokens = tokenArr[1];
////				log.info("token:	"+tokens);
//			}
//		}
//			log.info("token{}",tokens);
//			 String token = tokens;
//			List<Header> headerList = BaseCase.apiConfig.getHeaderList();
//			headerList.forEach(x->{
//				if(x.getName().equals("applyHeader")) {
//					x.getElementList().add(new Element("x-user-token",token));
//				}
//			});
		 //签约
		 String loanSignParam="{loanNumber: \""+loanNumber+"\"}";
		 String loanSignResponse = HttpUtil.execute(apiConfigTest, "post","/awsom-komodo/api/v3/loan-sign",loanSignParam,"applyHeader");
		 //发送短信
		 String sendVCodeParam = "channelType=0&mobile="+mobile+"&isSendMail=true&appTags=application_agreement_sign";
		 String sendVCodeResponse = HttpUtil.execute(apiConfigTest, "get","/awsom-komodo/api/v3/agreementSign/sendVCode",sendVCodeParam,"applyHeader");
		 int signcode = (int) JSONPath.read(sendVCodeResponse, "$.result");
		 
		 //签约
		 String agreementSignParam = "{\"loanNumber\": \""+loanNumber+"\" ,\"smsCodeDTO\":{\"smsCodeId\":"+signcode+",\"smsCodeValue\":\"8888\",\"mobile\":\""+mobile+"\"}}";
		 String agreementSignResponse = HttpUtil.execute(apiConfigTest, "post","/awsom-komodo/api/v3/agreement-sign",agreementSignParam,"applyHeader");
		 //确认放款
		 long confirmTimestamp = System.currentTimeMillis()/1000;
		 String confirmOrderParam = "{\r\n" + 
		 		"  \"appId\": \""+appId+"\",\r\n" + 
		 		"  \"timestamp\": "+confirmTimestamp+",\r\n" + 
		 		"  \"v\": \"1.0\",\r\n" + 
		 		"  \"signature\": \"${signaturecc}\",\r\n" + 
		 		"  \"body\": {\r\n" + 
		 		"    \"mobile\": \""+mobile+"\",\r\n" + 
		 		"    \"status\": \"confirm\",\r\n" + 
		 		"    \"orderNo\": \""+orderNo+"\"\r\n" + 
		 		"  }\r\n" + 
		 		"}";
		 HashMap<String,Object> confirmOrderMap = JSON.parseObject(confirmOrderParam, HashMap.class);
		 confirmOrderMap.remove("signature");
		 String confirmOrderSignature = SignUtil.getSignature(JSON.toJSONString(confirmOrderMap), secret);
		 log.info("confirmOrderSignature:"+confirmOrderSignature);
		 confirmOrderMap.put("signature", confirmOrderSignature);
		 String confirmOrderResponse = HttpUtil.execute(apiConfigTest, "post","/awsom-sun/api/credit/confirmOrder",JSON.toJSONString(confirmOrderMap),"applyHeader");

		 
	}
	
	public void test() {
		initXmlData("src\\\\main\\\\resources\\\\apiConfig.xml");
		initProperties("src\\\\test\\\\resources\\\\ktp.properties");
		HashMap<String, Object> parameterMap = new HashMap<>();
		parameterMap.put("tag", "B1-2");
		parameterMap.put("tenor", "3M");
		parameterMap.put("amount", "2600000");
		parameterMap.put("secondProdCode", "NS_astra_maucash_long_B1-2_v1");
		parameterMap.put("disburseType", "fif");

//		PreProcessorUtil preProcessorUtil = new PreProcessorUtil();
//		
//		preProcessorUtil.initXmlData("src\\\\main\\\\resources\\\\apiConfig.xml");
//		preProcessorUtil.initProperties("src\\\\test\\\\resources\\\\ktp.properties");
//		applyLimit(parameterMap);
//		approvaLimit(parameterMap);
//		useLimit(parameterMap);
		
	}

	public void testSalary() {
		Date date = new Date();
		Timestamp timeStamp = new Timestamp(date.getTime());
		extMapper.insertMonthlySalary(BigInteger.valueOf(220077), 10000000, timeStamp, timeStamp);
	}
	public static void main(String[] args) {
		HashMap<String, Object> parameterMap = new HashMap<>();
		parameterMap.put("tag", "B1-2");
		PreProcessorUtil preProcessorUtil = new PreProcessorUtil();
//		
		preProcessorUtil.initXmlData("src\\test\\resources\\apiConfig.xml");
		preProcessorUtil.initProperties("src\\\\test\\\\resources\\\\ktp.properties");
//		preProcessorUtil.applyLimit(parameterMap);
//		preProcessorUtil.approvaLimit(parameterMap);
//		preProcessorUtil.testJJ();

		
		//preProcessorUtil.useLimitByPL();
	}

	//登录后台管理获取验证吗
	public String getSmsCode() {
		//先获取验证码
		String mobile = "800000555";
		String urlSms = "https://japi-fat.maucash.id//awsom-user/api/v1/send-sms-code/by-user";
		HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("mobile", mobile);
		String body = JSONUtil.toJsonStr(paramMap);
		// 添加特定请求头信息
		Map<String, String> heads = new HashMap<>();
		heads.put("x-org-id", "2");
		heads.put("x-origin", "admin-web");
		//
		HttpResponse response = cn.hutool.http.HttpUtil.createPost(urlSms)
				.body(body)
				.headerMap(heads, true)
				.execute();
		System.out.println(response);
		cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(response.body());
		return jsonObject.getStr("result");
	}
	//登录后台管理拿到token
	public String loginWeb() {
		//再去登录
		String mobile = "800000555";
		String passwd = "Aa_123456";
		String urlogin = "https://japi-fat.maucash.id//awsom-user/api/v1/user-login";
		HashMap<String, Object> map = new HashMap<>();
		HashMap<String, Object> smsCode = new HashMap<>();
		map.put("mobile", mobile);
		map.put("passwd", passwd);
		smsCode.put("smsCodeId", getSmsCode());
		smsCode.put("smsCodeValue", "8888");
		map.put("smsCode", smsCode);
		String bodyParmeters = JSONUtil.toJsonStr(map);
		// 添加特定请求头信息
		Map<String, String> heads = new HashMap<>();
		heads.put("x-org-id", "2");
		heads.put("x-origin", "admin-web");
		heads.put("content-type", "application/json");
		heads.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36");
		HttpRequest httpRequest = cn.hutool.http.HttpUtil.createPost(urlogin)
				.body(bodyParmeters)
				.headerMap(heads, true);
		HttpResponse response = httpRequest.execute();
		System.out.println(response);
		cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(response.body());
		cn.hutool.json.JSONObject result = JSONUtil.parseObj(jsonObject.getStr("result"));
		log.info("登录{}",result);
		global_map.put("customerToken",result.getStr("token"));
		return result.getStr("token");
	}
	//调取提前结清试算接口
	public cn.hutool.json.JSONObject advance(HashMap<String,Object> map) {
		// 添加特定请求头信息
		Map<String, String> heads = new HashMap<>();
		heads.put("x-org-id", "2");
		heads.put("x-origin", "customer-web");
		heads.put("x-user-mobile", "800000555");
//		heads.put("x-user-token", loginWeb());
		heads.put("x-user-token", global_map.get("customerToken").toString());
		heads.put("x-operated-product", "YN-MAUCASH");
		heads.put("x-product-code", "YN-SYSTEM");
		heads.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36");
		heads.put("content-type", "application/json");
		String urlAdvance = "https://japi-fat.maucash.id//awsom-customer-service/api/v1/loan/advance/schedule?loanNumber=" + map.get("loanNumber").toString();
		HttpRequest httpRequest = cn.hutool.http.HttpUtil.createGet(urlAdvance)
				.headerMap(heads, true);
		System.out.println(httpRequest.toString());
		HttpResponse response = httpRequest.execute();
		System.out.println(response);
		cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(response.body());

		RepayByadvanceVo repayByadvanceVo = (RepayByadvanceVo) repaymentService.repayByadvance(map.get("loanNumber").toString());
		HashMap<String, BigDecimal> repayByadvance = repayByadvanceVo.getMap();
//		HashMap<String, Object> repayByadvance = (HashMap<String, Object>)repaymentService.repayByadvance(map.get("loanNumber").toString());
		HashMap<String, Boolean> assertionMap = checkEarlySettlementTrial(jsonObject, repayByadvance);
		map.put("trASsertionMap",assertionMap);
		map.put("trRepayByadvanceResult",repayByadvanceVo);
		return jsonObject;
	}
	//提交确认
	public cn.hutool.json.JSONObject conFirm(HashMap<String,Object> peramMap) {
		// 添加特定请求头信息
		Map<String, String> heads = new HashMap<>();
		heads.put("x-org-id", "2");
		heads.put("x-origin", "customer-web");
		heads.put("x-user-mobile", "800000555");
//		heads.put("x-user-token", loginWeb());
		heads.put("x-user-token", global_map.get("customerToken").toString());
		heads.put("x-operated-product", "YN-MAUCASH");
		heads.put("x-product-code", "YN-SYSTEM");
		heads.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36");
		heads.put("content-type", "application/json");
		//组装参数
		String urlConFirm = "https://japi-fat.maucash.id//awsom-customer-service/api/v1/loan/advance/confirm ";
		HashMap<String, Object> map = new HashMap<>();
		map.put("loanNumber", peramMap.get("loanNumber").toString());
		String bodyParmeters = JSONUtil.toJsonStr(map);
		HttpRequest httpRequest = cn.hutool.http.HttpUtil.createPost(urlConFirm)
				.body(bodyParmeters)
				.headerMap(heads, true);
		HttpResponse response = httpRequest.execute();
		cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(response.body());
		cn.hutool.json.JSONObject result = JSONUtil.parseObj(jsonObject.getStr("result"));
		return result;
	}
	//app这里先send-code
	public void sendCodeApp(HashMap<String,Object> map) {
		String mobile = map.get("mobile").toString();
		String urlSendCode = "https://japi-fat.maucash.id/awsom-komodo/api/v3/noauth/user/login/send-code?inputCode=&mobile=" + mobile + "&channelType=0";

		ExtractableResponse<Response> response = given()
				.header("x-org-id", 1)
				.header("x-origin", "H5")
				.header("x-product-code", "YN-MAUCASH")
				.header("x-source-id", "H5")
				.header("user-agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.63 Mobile Safari/537.36")
				.log().all()
				.when()
				.get(urlSendCode)
				.then()
				.log().all().extract();
		System.out.println(response.body().jsonPath().get("result").toString());
		String smsCodeId = response.body().jsonPath().get("result").toString();
		loginApp(smsCodeId,map);
	}
	//app登录拿到当前用户的token
	public String loginApp(String smsCodeId,HashMap<String,Object> map) {

		String url = "https://japi-fat.maucash.id/awsom-user/api/v3/noauth/user-login";
		Map<String, Object> params = new HashMap<>();
		params.put("mobile", map.get("mobile").toString());
		params.put("channelType", 0);
		Map<String, Object> sms_params = new HashMap<>();
		sms_params.put("smsCodeId", Integer.valueOf(smsCodeId));
		sms_params.put("smsCodeValue", "8888");
		params.put("smsCode", sms_params);
		ExtractableResponse<Response> responseExtractableResponse = given()
				.header("content-type", "application/json; charset=UTF-8")
				.header("x-org-id", 1)
				.header("x-origin", "H5")
				.header("x-product-code", "YN-MAUCASH")
				.header("x-source-id", "H5")
				.header("user-agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.63 Mobile Safari/537.36")
				.body(JSON.toJSONString(params))
				.log().all()
				.when()
				.post(url)
				.then()
				.log().all().extract();
		String token = responseExtractableResponse.body().jsonPath().get("result.token").toString();
		System.out.println(token);
		String userId = responseExtractableResponse.body().jsonPath().get("result.userId").toString();
		global_map.put("token", token);
		global_map.put("userId", userId);
		return token;
	}
	//先获取站内信的列表，拿到提前还清通知的ID
	public Integer appMessage() {
		// 添加特定请求头信息
		Map<String, String> heads = new HashMap<>();
		heads.put("x-org-id", "1");
		heads.put("x-origin", "H5");
		String token = global_map.get("token").toString();
		System.out.println(token);
		heads.put("x-user-token", token);
		heads.put("x-product-code", "YN-SYSTEM");
		heads.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36");
		heads.put("content-type", "application/json");
		//组装参数
		String urlAdvanceRepayment = "https://japi-fat.maucash.id/awsom-komodo/api/v3/app/page?msgType=1&pageNum=1&pageSize=1000000";
		HttpRequest httpRequest = cn.hutool.http.HttpUtil.createGet(urlAdvanceRepayment)
				.headerMap(heads, true);
		HttpResponse response = httpRequest.execute();
		cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(response.body());
		System.out.println(jsonObject.toString());
		String result = jsonObject.get("result").toString();
		System.out.println(result);
		Integer advanceMsgId = JsonPathUtil.extract(result, "$.list[0].advanceMsgId");
		return advanceMsgId;
	}
	//点击进入要还款的站内信
	public void getAdvanceRepaymentInfo(Integer id) {
		String token = global_map.get("token").toString();
		String url = "https://japi-fat.maucash.id/awsom-komodo/api/v3/advancerepayment/getAdvanceRepaymentInfo?advanceRepaymentId=" + id + "";
		ExtractableResponse<Response> responseExtractableResponse = given()
				.header("content-type", "application/json; charset=UTF-8")
				.header("x-org-id", 1)
				.header("x-origin", "H5")
				.header("x-product-code", "YN-MAUCASH")
				.header("x-source-id", "H5")
				.header("user-agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.63 Mobile Safari/537.36")
				.header("x-user-token", token)
				.log().all()
				.when()
				.get(url)
				.then()
				.log().all().extract();
		String result = responseExtractableResponse.body().jsonPath().get("result").toString();
		//组装参数
		System.out.println(result);
	}
	//站内信点击提前还款接口
	public void repaymentAmountInfo(Integer repayId) {
		String token = global_map.get("token").toString();
		String url = "https://japi-fat.maucash.id/awsom-komodo/api/v3/advancerepayment/enableAdvanceRepayment";
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("advanceRepaymentId", repayId);
		ExtractableResponse<Response> responseExtractableResponse = given()
				.header("content-type", "application/json; charset=UTF-8")
				.header("x-org-id", 1)
				.header("x-origin", "H5")
				.header("x-product-code", "YN-MAUCASH")
				.header("x-source-id", "h5")
				.header("user-agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.63 Mobile Safari/537.36")
				.header("x-user-token", token)
				.body(jsonObject.toString())
				.log().all()
				.when()
				.post(url)
				.then()
				.log().all().extract();
		String result = responseExtractableResponse.body().jsonPath().get("result").toString();
		System.out.println(result);
	}
	//查询对应的还款账户再去还款
	public List<Map<String, Object>> searchBank() {
		String sql = "select pggva.account_number,pggva.fund_id,pggva.bank_code,pggva.suggested_amount,pggva.*  from pay_gateway.gateway_virtual_account   pggva where pggva.user_id =? and pggva.is_closed =0";
		//查询返回的是一个map,
		List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql, global_map.get("userId"));
		if (maps.size() == 0) {
			log.error("返回的sql是空，无法执行下一步的还款");
			return null;
		}
		return maps;
	}
	//遍历sql
	public void getBanckInfo(String bankCode) {
		List<Map<String, Object>> maps = searchBank();
		for (Map<String, Object> map : maps) {
			if (map.get("bank_code").equals(bankCode)) {
				String bank_code = map.get("bank_code").toString();
				String account_number = map.get("account_number").toString();
				String fund_id = map.get("fund_id").toString();
				String suggested_amount = map.get("suggested_amount").toString();
				setGlobalMap("bank_code", bank_code);
				setGlobalMap("account_number", account_number);
				setGlobalMap("fund_id", fund_id);
				setGlobalMap("suggested_amount", suggested_amount);
				System.out.println("打印BCA账号信息"+bank_code + "-" + account_number + "-" + fund_id + '-' + suggested_amount);
			}
		}
	}
	//线上还款
	public void repaymentAmount(String bankCode) {
		getBanckInfo(bankCode);
		System.out.println("打印map信息"+global_map);
		String url = "https://japi-fat.maucash.id/awsom-pay-gateway/api/receipt?bankCode=" + global_map.get("bank_code") + "&accountNumber=" + global_map.get("account_number") + "&amount=" + global_map.get("suggested_amount") + "&fund=" + global_map.get("fund_id");
		ExtractableResponse<Response> responseExtractableResponse = given()
				.log().all()
				.when()
				.post(url)
				.then()
				.log().all().extract();
	}

	public void buildAdvance(HashMap<String,Object> paramtersMap){
		if(!global_map.containsKey("customerToken")){
			loginWeb();
		}
		advance(paramtersMap);
		conFirm(paramtersMap);
		sendCodeApp(paramtersMap);
		Integer id = appMessage();
		getAdvanceRepaymentInfo(id);
		repaymentAmountInfo(id);
		getBanckInfo("BCA");
		repaymentAmount("BCA");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
	public HashMap<String, Boolean> checkEarlySettlementTrial(cn.hutool.json.JSONObject jsonObject, HashMap<String, BigDecimal> repayByadvance) {
		////与试算接口做对比
		//String totalAmount = jsonObject.getJSONObject("amount").toString();
		//上面返回的是String类型，要转Integer进行对比
		Integer amount = JsonPathUtil.extract(jsonObject.toString(), "$.result.amount");
		String totalAmount1 = repayByadvance.get("totalAmount").toString();
		//String interestFee = jsonObject.getJSONObject("interestFee").toString();
		Integer interestFee = JsonPathUtil.extract(jsonObject.toString(), "$.result.interestFee");
		String interestFee1 = repayByadvance.get("interestFee").toString();
		//String principalFee = jsonObject.getJSONObject("principalFee").toString();
		Integer principalFee = JsonPathUtil.extract(jsonObject.toString(), "$.result.principalFee");
		String principalFee1 = repayByadvance.get("principalSum").toString();
		//String carryFee = jsonObject.getJSONObject("carryFee").toString();
		Integer carryFee = JsonPathUtil.extract(jsonObject.toString(), "$.result.carryFee");
		String carryFee1 = repayByadvance.get("carryFee").toString();
		//这个断言不用，用框架封装好的断言
		//SoftAssert softAssert = new SoftAssert();
		//softAssert.assertEquals(String.valueOf(amount),totalAmount1,"总金额断言失败");
		//softAssert.assertEquals(String.valueOf(interestFee),interestFee1,"优惠后的服务费断言失败");
		//softAssert.assertEquals(String.valueOf(principalFee),principalFee1,"本金断言失败");
		//softAssert.assertEquals(String.valueOf(carryFee),carryFee1,"进位费断言失败");
		//log.info("对比结束，进入断言:{}" ,softAssert );
		//softAssert.assertAll();
		HashMap<String,Boolean> assertionMap = new HashMap<String,Boolean>();
		assertionMap.put("试算totalAmount",checkAmount(String.valueOf(amount),totalAmount1));
		//因为计算出来的服务费会有差异相差进位费  因此如果差异5  就任务是相等
//		assertionMap.put("interestFee",checkAmount(String.valueOf(interestFee),interestFee1));
		assertionMap.put("试算interestFee", Math.abs(interestFee-Integer.valueOf(interestFee1))<=10? true: false);
		assertionMap.put("试算principalFee",checkAmount(String.valueOf(principalFee),principalFee1));
//		assertionMap.put("carryFee",checkAmount(String.valueOf(carryFee),carryFee1));
		assertionMap.put("试算carryFee",Math.abs(carryFee-Integer.valueOf(carryFee1))<=10? true: false);
		log.info("提前结清试算断言结果：{}",assertionMap);
		return assertionMap;
	}
	public boolean checkAmount(String resource,String target){
	    log.info("resource：{}，target:{},断言结果：{}",resource,target,resource.equals(target) ? true:false);
		return resource.equals(target) ? true:false;
	}
}
