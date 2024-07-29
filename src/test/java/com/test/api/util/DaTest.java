/**
 * 
 */
package com.test.api.util;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.mchange.v2.sql.filter.SynchronizedFilterDataSource;

/**  
 * @ClassName: DaTest
 * @Description: TODO(描述)
 * @author Faisal Mulya Santosa
 * @date 2024-07-29 
*/
public class DaTest {
	public int getDiffDay(Calendar cal1,Calendar cal2) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.format(cal1.getTime());
		System.out.println(""+dateFormat.format(cal1.getTime())+"        "+dateFormat.format(cal2.getTime()));
		System.out.println(""+cal2.getTime().getTime()+"        "+cal1.getTime().getTime());

//		long diff = (cal2.getTime().getTime()-cal1.getTime().getTime())/(24*60*60*1000);
		BigDecimal multiply = new BigDecimal(24).multiply(new BigDecimal(60)).multiply(new BigDecimal(60))
		.multiply(new BigDecimal(1000));
		BigDecimal  diff =new BigDecimal(cal2.getTime().getTime()).subtract(new BigDecimal(cal1.getTime().getTime()))
		.divide(multiply, 0, BigDecimal.ROUND_HALF_UP);
		System.out.println("diff:"+diff);
		return diff.intValue();
	}
	public static void main(String[] args) throws ParseException {
		// TODO Auto-generated method stub
		DaTest datest = new DaTest();
		String finaDdm="2021-03-25";
		

		SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");

		Date date =sdf.parse(finaDdm);

		Calendar calendar1 = Calendar.getInstance();

		calendar1.setTime(date);
		
		String finallyDueDate="2022-03-20";
		

		SimpleDateFormat sdfs= new SimpleDateFormat("yyyy-MM-dd");

		Date date2 =sdfs.parse(finallyDueDate);

		Calendar calendar2 = Calendar.getInstance();

		calendar2.setTime(date2);
		datest.getDiffDay(calendar1, calendar2);
	}

}
