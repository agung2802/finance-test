package com.test.api.cases;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import com.test.api.util.DataUtil;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
public class UserProfile extends BaseCase {
	@DataProvider
	public Object[][] datas(ITestContext context){
		System.out.println("我是register@DataProvider");

		Object[][] obj = DataUtil.getProviderData(context,"3");
		
		return obj;
		
	}
	public void test() {
		int fail = 0 ;
		UserProfile profile = null;
		
			profile.datas(null);

	}
	public static void main(String[] args) {
		UserProfile profile = new UserProfile();
		int sum =0;
		try {
			profile.test();
		} catch (Exception e) {
			e.printStackTrace();
			sum++;
			// TODO: handle exception
			System.out.println("sum:"+sum);
		}
		System.out.println("sum:"+sum);
		profile.test();

	}
}
