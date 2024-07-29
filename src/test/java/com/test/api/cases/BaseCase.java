package com.test.api.cases;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.test.api.Application;
import com.test.api.entity.vo.LoanVo;
import com.test.api.entity.vo.RepayByadvanceVo;
import com.test.api.pojo.*;
import com.test.api.util.*;
import com.test.api.util.function.FunctionInterface;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@SpringBootTest(classes=Application.class)
//@Component

public class BaseCase extends AbstractTestNGSpringContextTests  {
	public static Logger log = Logger.getLogger(BaseCase.class);
	public static ApiConfig apiConfig;
	public static Map<String,Class<?>> map = new HashMap<>();
	public static Map<String,String> imageMap = new HashMap<>();
	public static int totalCaseNum;
	public static int successCaseNum;
	public static int failCaseNum;
	public static int abnormalNum;
	private ExecutorService executor =Executors.newFixedThreadPool(5);
	static {
		log.info("log.info静态");
		List<Class<?>> classes = FunctionUtil.getClasses(FunctionInterface.class);
		classes.forEach(x->{
			try {
				log.info("遍历集合");
				FunctionInterface fi = (FunctionInterface) x.newInstance();
				System.out.println(fi.excute(null));
				if (fi.getReferenceKey().length()>0) {
					map.put(fi.getReferenceKey(), x);
				}
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
	@Autowired
	PostProcessor postProcessor;
	@Autowired
	PreProcessor preProcessor;
	@Autowired
	CostCalculation costCalculation;
	@Autowired
	RapayByAdvancePostProcessor rapayByAdvancePostProcessor;
	/**
	 * 读取xml
	 */
	@Parameters("configXmlPath")
	@BeforeSuite
	public void initXmlData(String configXmlPath) {
	
		apiConfig = XmlPropertiesUtil.readXml(configXmlPath);
		List<Header> headerList = apiConfig.getHeaderList();
		//登录线下门店
		String loginBranch = HttpUtil.execute(BaseCase.apiConfig, "post","/awsom-user/api/v1/user-login","{\r\n" +
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
	}
	@Parameters("propertiesPath")
	@BeforeSuite
	public void initProperties(String propertiesPath) {
		imageMap = XmlPropertiesUtil.readProperties(propertiesPath);
	}
	/*
	 * 读取excel
	 * 
	 * */
	@Parameters("excelPath")
	@BeforeTest
	public void readExecl(String excelPath,ITestContext context) {
		System.out.println("@BeforeTest");
		 List<Api> apiList = ExcelUtil.readExcel(Api.class, excelPath, 0);
		List<Case> caseList = ExcelUtil.readExcel(Case.class, excelPath, 1);
		System.out.println("apiList"+apiList.size());
		System.out.println("caseList"+caseList.size());
		context.setAttribute("api", apiList);
		context.setAttribute("case", caseList);
	}
	

	@Test(dataProvider="datas")
	public void test(Case cas,Api api) {
		HashMap<String,Object>  paramtersMap = new HashMap<>();
		StringBuilder excuteMethod =new StringBuilder();
		LoanVo loanVo = new LoanVo();
		ApiConfig apiConfigTest =JSON.parseObject(JSON.toJSONString(BaseCase.apiConfig),ApiConfig.class);
		//BeanUtils.copyProperties(BaseCase.apiConfig,apiConfigTest);

		log.info(Thread.currentThread().getName()+"----"+apiConfigTest.hashCode()+"apiConfigTest:{}"+apiConfigTest);
		//前置处理  处理单接口参数
		beforeExcute(cas,api,paramtersMap,excuteMethod,apiConfigTest);
		paramtersMap.entrySet().forEach(x->log.info("paramtersMap"+x.getKey()+":"+x.getValue()));
		//单接口的调用，流程用例费用计算
		excute(cas, api, paramtersMap, excuteMethod.toString(),loanVo);
		//后置处理
		afterExcute(cas, paramtersMap, loanVo);
	}

	@Parameters("excelPath")
	@AfterSuite
	public void write(String excelPath) {
		log.info("writeCellsList::{}"+ExcelUtil.writeCellsList);
		log.info("总用例数{}"+totalCaseNum+"，成功用例数{}"+successCaseNum+"，失败用例数{}"+failCaseNum+",环境异常用例数{}"+abnormalNum);
		ExcelUtil.writeToExcel(ExcelUtil.writeCellsList, excelPath);
		ExcelUtil excelUtil = new ExcelUtil();
		log.info("总用例数{}"+totalCaseNum+"，成功用例数{}"+successCaseNum+"，失败用例数{}"+failCaseNum+",环境异常用例数{}"+abnormalNum);

		excelUtil.sendWeChat(totalCaseNum,successCaseNum,failCaseNum,abnormalNum);
		
	}
	@org.junit.jupiter.api.Test
	public void test() {
		LoanVo loanVo = new LoanVo();
		loanVo.setLoanNumber("CL21120614162663713200232");
		loanVo.getLoanNumber().startsWith("CL");
		System.out.println(loanVo.getLoanNumber().startsWith("CL"));
//		costCalculation.buildLoanVo(loanVo);
		costCalculation.test();

	}
	public void beforeExcute(Case cas,Api api,HashMap<String,Object>  paramtersMap,StringBuilder excuteMethod,ApiConfig apiConfigTest){
		totalCaseNum = totalCaseNum+1;
		System.out.println("@Test");
		System.out.println("cas"+cas+"api"+api);
		log.info("前置cas"+cas);

		//前置处理
		String beforeExcute = cas.getBeforeExcute();
		log.info("beforeExcute{}"+beforeExcute.length());
		JSONObject paramMap = JSON.parseObject(beforeExcute);
		// HashMap<String,Object>  paramtersMap = null;
//		 String excuteMethod ="";
		if(paramMap.get("parameter")!=null) {
			System.out.println("paramMap.get(\"parameter\")"+paramMap.get("parameter"));
			HashMap parameter = JSON.parseObject(paramMap.get("parameter").toString(), HashMap.class);
			paramtersMap.putAll(parameter);
			String execution = (String) paramMap.get("execution");
			excuteMethod.append(execution);
			log.info("====parameter{}"+paramMap.get("parameter")+",disburse{}"+paramMap.get("execution"));
			for(Entry<String, Object> entry: paramtersMap.entrySet()) {
				log.info("=====entr"+entry.getKey()+","+entry.getValue());
			}
		}
		Class<? extends PreProcessor> class2 = preProcessor.getClass();
		Method[] methods2 = class2.getDeclaredMethods();
		List<String> methodList =  new ArrayList<>();
		for (Method method : methods2) {
			String name = method.getName();
			methodList.add(name);
		}
		if(methodList.contains(excuteMethod.toString())) {
			try {
				Method method = class2.getMethod(excuteMethod.toString(), HashMap.class,ApiConfig.class);
				method.invoke(preProcessor, paramtersMap, apiConfigTest);
			} catch (NoSuchMethodException e) {
				log.error(e);
			} catch (SecurityException e) {
				log.error(e);
			} catch (IllegalAccessException e) {
				log.error(e);
			} catch (IllegalArgumentException e) {
				log.error(e);
			} catch (InvocationTargetException e) {
				log.error(e);
			}
		}
		//替换参数
		MyStringUtils.replaceKeyFromMap(cas, map,apiConfig);
		MyStringUtils.convertString(cas, api, apiConfig);
		paramtersMap.entrySet().forEach(x->log.info("paramtersMap"+x.getKey()+":"+x.getValue()));
	}
	public LoanVo excute(Case cas, Api api, HashMap<String, Object> paramtersMap, String excuteMethod,LoanVo loanVo) {
		if(cas.getParams().length()>0) {
			String response = HttpUtil.execute(apiConfig, cas, api,"applyHeader");
			ExcelUtil.addWritecell(cas.getNo(), "ActualResponse", response);
			log.info("接口入参"+ cas);
			log.info("接口返回"+response);
			//接口断言
			if(cas.getResponseValidators().trim().length()>0) {
				String verifyResponse = HandleResponse.verifyResponse(cas,  response);
				ExcelUtil.addWritecell(cas.getNo(), "ResponseValidationResult", verifyResponse);
			}
			//参数提取
			if (cas.getSave().trim().length()>0) {
				HandleResponse.saveResponse(apiConfig, cas, response);
			}
		}
		loanVo.setLoanNumber(paramtersMap.get("loanNumber").toString());
		if("disburse".equalsIgnoreCase(excuteMethod)){
			try {
				costCalculation.buildLoanVo(loanVo);
			} catch (Exception e) {
				BaseCase.abnormalNum++;
				throw new RuntimeException(e);
			}
		}else if("rapayByAdvance".equalsIgnoreCase(excuteMethod)){
			loanVo.setRepayByadvanceVo((RepayByadvanceVo) paramtersMap.get("trRepayByadvanceResult"));
		}
		return loanVo;
	}
	public void afterExcute(Case cas, HashMap<String, Object> paramtersMap, LoanVo loanVo) {
		log.info("开始清理token");
		//清理进件token
		List<Header> headerList = BaseCase.apiConfig.getHeaderList();
		for(Header header:headerList) {
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

		BaseCase.apiConfig.setHeaderList(headerList);
		log.info("结束清理token");

		//后置处理
		log.info("后置cas"+ cas);

		log.info("后置处理"+ cas.getAfterExcute());

		if(cas.getAfterExcute().trim().length()>0) {
			log.info("====后置处理=====s"+ cas.getAfterExcute());
			List<String> asList = null;
//			LoanVo loanVo = new LoanVo();
			//取出要执行的类
			String[] split = cas.getAfterExcute().split(":");
			String classToExecuteStr = split[0];
			Object classToExecute =null;
			if("PostProcessor".equalsIgnoreCase(classToExecuteStr)){
				classToExecute = postProcessor;
			}else if("RapayByAdvancePostProcessor".equalsIgnoreCase(classToExecuteStr)){
				classToExecute = rapayByAdvancePostProcessor;
			}
			//取出执行的方法
			String afterExcute = split[1];
			cas.setAfterExcute(afterExcute);
			if(cas.getAfterExcute().contains(",")) {
				log.info("含，");
				String[] afterExcuteArray = cas.getAfterExcute().split(",");
				System.out.println("=========");
				log.info("afterExcuteArray.toString()"+afterExcuteArray.toString());
				asList = Arrays.asList(afterExcuteArray);
			}else {
				asList = Arrays.asList(cas.getAfterExcute());
			}

			Class<? extends Object> class1 = classToExecute.getClass();

//			CostCalculation cost = new CostCalculation();
//			cost.calculateNextDueDate(loanVo);
			List<String> list = new ArrayList<>();
			Method[] methods = class1.getDeclaredMethods();
			for (Method method : methods) {
				list.add(method.getName());
			}
			//校验试算返回的值是否正确
			if(paramtersMap.containsKey("trASsertionMap")) {
				if(loanVo.getAssertionMap()==null){
					HashMap<String,Boolean> assertionMap = new HashMap<>();
					loanVo.setAssertionMap(assertionMap);
				}
				loanVo.getAssertionMap().putAll((Map) paramtersMap.get("trASsertionMap"));
			}
			for (int i = 0; i < asList.size(); i++) {
				if(list.contains(asList.get(i)) ){
					Method declaredMethod;
					try {
						declaredMethod = class1.getDeclaredMethod(asList.get(i), LoanVo.class);
						declaredMethod.invoke(classToExecute, loanVo);
//							Assert.assertEquals(false, true);
					} catch (NoSuchMethodException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
			log.info("loanVo:{}"+ loanVo);
//			if(paramtersMap.containsKey("trASsertionMap")) {
//				if(loanVo.getAssertionMap()==null){
//					HashMap<String,Boolean> assertionMap = new HashMap<>();
//					loanVo.setAssertionMap(assertionMap);
//				}
//				loanVo.getAssertionMap().putAll((Map)paramtersMap.get("trASsertionMap"));
//			}
			boolean checkAssertionMap = costCalculation.checkAssertionMap(loanVo);
			if(checkAssertionMap) {
				ExcelUtil.addWritecell(cas.getNo(), "AfterExcuteAssertionResult", "通过");
				successCaseNum = successCaseNum+1;
			}else {
				ExcelUtil.addWritecell(cas.getNo(), "AfterExcuteAssertionResult", "不通过");
				failCaseNum++;

			}
			String beforeExcuteResult = JSON.toJSONString(paramtersMap);
			ExcelUtil.addWritecell(cas.getNo(), "BeforeExcuteResult", beforeExcuteResult);
			ExcelUtil.addWritecell(cas.getNo(), "AfterExcuteResult", JSON.toJSONString(loanVo));
			Assert.assertEquals(checkAssertionMap, true);

		}
	}
}
