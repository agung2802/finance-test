package com.test.api.util.function;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
public class Nik implements FunctionInterface {

	@Override
	public String excute(String[] args) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		//生成6位数
		Random rm = new Random();
		for (int i = 0; i < 6; i++) {
			String str = String.valueOf(rm.nextInt(10));
			sb.append(str);
		}
		Date date = new Date();
		//生成的时间所在范围
		
		SimpleDateFormat format = new SimpleDateFormat("ddMMyy");
		try {
			Date startTime = format.parse("010172");
			Date endTime = format.parse("010100");
			//随机时间
			long time = startTime.getTime()+(long)((Math.random()*(endTime.getTime()-startTime.getTime())));
			String format2 = format.format(time);
//			System.out.println("     format2  "+format2);
			sb.append(format2);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		for (int i = 0; i < 4; i++) {
			String str = String.valueOf(rm.nextInt(10));
			sb.append(str);
		}
//		String format2 = format.format(date);
		return sb.toString();
	}

	@Override
	public String getReferenceKey() {
		// TODO Auto-generated method stub
		return "nik";
	}
	public static void main(String[] agrs) {
		// TODO Auto-generated method stub
			Nik nik = new Nik();
			String[] str = new String[1];
			nik.excute(str);
			System.out.println(nik.excute(str));
	}
}
