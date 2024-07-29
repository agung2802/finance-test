package com.test.api.cases;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import com.test.api.util.DataUtil;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
public class Register extends BaseCase {

	@DataProvider
	public Object[][] datas(ITestContext context){
		System.out.println("我是register@DataProvider");

		Object[][] obj = DataUtil.getProviderData(context,"1");
		
		return obj;
		
	}

	
}
