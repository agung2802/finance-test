package com.test.api.util;

import java.util.List;

import org.testng.Assert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import com.test.api.pojo.ApiConfig;
import com.test.api.pojo.Case;
import com.test.api.pojo.ResponseValidator;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
public class HandleResponse {

	public static String verifyResponse(Case cas, String result) {
		String assertionResult[] = new String[1];
		assertionResult[0] = "通过";
		String validators = cas.getResponseValidators();
		System.out.println("校验信息"+validators);
		  List<ResponseValidator> validatotList = JSON.parseArray(validators, ResponseValidator.class);
		  validatotList.forEach(x->{
			 String actrul = JSONPath.read(result, x.getJsonPath()).toString();
//				 if(!actrue.equalsIgnoreCase(x.getExcepted())) {
//					 assertion[0]= "不通过";
//				 }
			 try {
				Assert.assertEquals(actrul, x.getExcepted());
			} catch (Exception e) {
				assertionResult[0] = "不通过";
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  });
		  
//		  System.out.println("断言结果"+cas.getResponseValidationResult());
		return assertionResult[0];
	}
	public static void saveResponse(ApiConfig apiConfig, Case cas, String result) {
		String save = cas.getSave();
		 List<ResponseValidator> saveList = JSON.parseArray(save, ResponseValidator.class);
		 saveList.forEach(x->{
			String save1 = JSONPath.read(result, x.getJsonPath()).toString();
			apiConfig.getMap().put(x.getExcepted(), save1);
			System.out.println("save1:"+save1);	
		 });
	}
}
