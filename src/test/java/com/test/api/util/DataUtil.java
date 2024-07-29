package com.test.api.util;

import java.util.List;
import java.util.stream.Collectors;

import org.testng.ITestContext;

import com.test.api.pojo.Api;
import com.test.api.pojo.Case;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
public class DataUtil {

	
	public static Object[][] getProviderData(ITestContext context,String apiId) {
		List<Api> apiList = (List<Api>) context.getAttribute("api");
		List<Case> caseList = (List<Case>) context.getAttribute("case");

		List<Api> tarApi = apiList.stream().filter(x->x.getApiId().equals(apiId)).collect(Collectors.toList());

		List<Case> targetCase = caseList.stream().filter(x->x.getApiId().equals(apiId)).collect(Collectors.toList());
		tarApi.forEach(x->System.out.println(tarApi.size()+"       api    "+x));
		targetCase.forEach(x->System.out.println(targetCase.size()+"    case   "+x));
		Object[][] obj = new Object[targetCase.size()][2];
		System.out.println("obj.length"+obj.length);
		System.out.println("obj[0].length"+obj[0].length);

		for (int i = 0; i < targetCase.size(); i++) {
			System.out.println("lalalla");
			System.out.println(targetCase.get(i));
			System.out.println(tarApi.get(0));

				obj[i][0] = targetCase.get(i);
				obj[i][1] = tarApi.get(0);
				System.out.println("lalalla2");

		}
		return obj;
	}
}
