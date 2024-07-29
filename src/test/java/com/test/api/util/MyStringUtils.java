package com.test.api.util;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.test.api.pojo.Api;
import com.test.api.pojo.ApiConfig;
import com.test.api.pojo.Case;
import com.test.api.util.function.FunctionInterface;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
public class MyStringUtils {

	public static String pattern = "\\$\\{(.*?)\\}";
	public static Pattern pt = Pattern.compile(pattern);
	public static Pattern pttn = Pattern.compile("__(\\w*?)\\((([\\w\\\\\\/:\\.\\$]*,?)*)\\)");
	public static void convertString(Case cas,Api api,ApiConfig apiConfig) {
		Map<String, String> map = apiConfig.getMap();
		//处理头信息
		if(api.getHeader().trim().length()>0) {
			Matcher matcher = pt.matcher(api.getHeader());
			while(matcher.find()) {
				String key = matcher.group(1);
				System.out.println(" api  matcher.group(0)"+ matcher.group(0));

				String value = map.get(key);
				if(null!=value) {
					api.setHeader(api.getHeader().replace(matcher.group(0), value));
				}else {
					api.setHeader(api.getHeader().replace(matcher.group(0), "null"));
				}
			}
		}
		//处理参数
		if (cas.getParams().trim().length()>0) {
			Matcher matcher = pt.matcher(cas.getParams());
			while (matcher.find()) {
				String key = matcher.group(1);
				System.out.println(" cas  matcher.group(0)"+ matcher.group(1));
				String value = map.get(key);
				if (null!=value) {
					cas.setParams(cas.getParams().replace(matcher.group(0), value));
				}else {
					cas.setParams(cas.getParams().replace(matcher.group(0), "null"));
				}
			}
		}
		
		System.out.println("api.getHeader"+api.getHeader());
		System.out.println("cas.getParams"+cas.getParams());

	}
	public static void replaceKeyFromMap(Case cas,Map<String,Class<?>> map,ApiConfig apiConfig) {
		
		Matcher matcher = pttn.matcher(cas.getParams());
		while (matcher.find()) {
			matcher.group(1);
			System.out.println("matcher.group(1)"+matcher.group(1));
			System.out.println("matcher.group(2)"+matcher.group(2));
			try {
				Class<?> class1 = map.get(matcher.group(1));
				FunctionInterface fi = (FunctionInterface) class1.newInstance();
				if (null==matcher.group(2)) {
					String value = fi.excute(null);
					cas.setParams(cas.getParams().replace(matcher.group(),value ));
					apiConfig.getMap().put(matcher.group(1), value);
				}else {
					String value =fi.excute(matcher.group(2).split(","));
					cas.setParams(cas.getParams().replace(matcher.group(), value));
					apiConfig.getMap().put(matcher.group(1), value);

				}
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		}
	}
	public static void main(String[] args) {
		String str = "mobile=__mobile()&channelType=0&mobile=__mobile()";
		Matcher matcher = pttn.matcher(str);
		while (matcher.find()) {
			matcher.group(1);
			matcher.group(2);
			System.out.println(""+matcher.group(1)+"              "+matcher.group(2));
		}
		String string = UUID.randomUUID().toString();
		System.out.println("UUID.randomUUID()             "+string);
		Api api = new Api();
		api.setApiId("1");
	}
}


