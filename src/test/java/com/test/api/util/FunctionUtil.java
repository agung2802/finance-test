package com.test.api.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.test.api.util.function.FunctionInterface;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
public class FunctionUtil {
	public static Map<String,Class<?>> map = new HashMap<>();
	static {
		System.out.println("静态");
		List<Class<?>> classes = getClasses(FunctionInterface.class);
		classes.forEach(x->{
			try {
				System.out.println("遍历集合");
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
		for (Entry<String, Class<?>> entry : map.entrySet()) {
			System.out.println("遍历map");
			System.out.println(entry.getKey()+"  "+entry.getValue());
		}
	}
	public static List<Class<?>> getClasses(Class<?> cl) {
		List<Class<?>> list = new ArrayList<Class<?>>();
		String path = cl.getResource("").getPath();
		String pack = cl.getPackage().getName();
		System.out.println(path);
		System.out.println(pack);

		 File file = new File(path); 
		 for (Class<?> class1 : getClassesByFile(pack, file)) {
			if (cl.isAssignableFrom(class1)&&!cl.equals(class1)) {
				list.add(class1);
			}
		}

		return list;
		

	}

	private static List<Class<?>> getClassesByFile(String pack, File file) {
		List<Class<?>> list = new ArrayList<Class<?>>();

		for (File fl : file.listFiles()) {
			System.out.println("fl.isDirectory()  "+fl.isDirectory());
			if (fl.isDirectory()) {
				getClassesByFile(pack, file);
			}
			System.out.println(fl.getName());
			if (fl.getName().contains("class")) {
				try {
					Class<?> clazz = Class.forName(pack+"."+fl.getName().substring(0, fl.getName().length()-6));
					 list.add(clazz);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		 return list;
	}
	
	public static void main(String[] args) {
		FunctionUtil func = new FunctionUtil();
		List<Class<?>> classes = FunctionUtil.getClasses(FunctionInterface.class);
		classes.forEach(x->{
			System.out.println(x);
		});
		
	}

}
