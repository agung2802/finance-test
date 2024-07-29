package com.test.api.util.function;

import java.util.Random;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
public class Mobile implements FunctionInterface {

	@Override
	public String excute(String[] args) {
		Random rd = new Random();
		StringBuilder sb = new StringBuilder("8");
		int[] str = {7,8,9,10,11};
		int nextInt = rd.nextInt(4);
		for (int i = 1; i <=str[nextInt]; i++) {
			String nextString = String.valueOf(rd.nextInt(10));
			sb.append(nextString);
		}
		return sb.toString();
	}

	@Override
	public String getReferenceKey() {
		// TODO Auto-generated method stub
		return "mobile";
	}
	public static void main(String[] args) {
		Mobile mb = new Mobile();
		String[] str = new String[1];
		mb.excute(str);
		System.out.println(mb.excute(str)+"   "+mb.excute(str).length());
	}
}
