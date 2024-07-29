package com.test.api.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.test.api.pojo.ApiConfig;
import com.test.api.pojo.Header;

import lombok.extern.slf4j.Slf4j;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
@Slf4j
public class XmlPropertiesUtil {

	public static ApiConfig readXml(String configXmlPath){
		ApiConfig apiConfig = new ApiConfig();
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(configXmlPath);
			Element rootElement = document.getRootElement();
			apiConfig.setRootUrl(rootElement.element("rootUrl").getTextTrim());
			System.out.println("RootUrl()"+apiConfig.getRootUrl());
			//取出headers中信息
			List<Element> headerElement = rootElement.elements("headers");
			List<Header> headerList = new ArrayList<>();
			Map<String,String> map =new HashMap();
			headerElement.forEach(el->{
				String attributeValue = el.attributeValue("name");
				Header header = new Header();
				List<com.test.api.pojo.Element> list = new ArrayList<>();
				header.setName(attributeValue);
				System.out.println("attributeValue:"+attributeValue);
				List<Element> elements = el.elements();
				elements.forEach(ele->{
					list.add(new com.test.api.pojo.Element(ele.attributeValue("name"),ele.attributeValue("value")));
				});
				header.setElementList(list);	
				headerList.add(header);
			});
			apiConfig.setHeaderList(headerList);
			//取出params中参数
			List<Element> paramsElements = rootElement.elements("params");
			paramsElements.forEach(params->{
				List<Element> param = params.elements();
				param.forEach(par->{
					map.put(par.attributeValue("name"), par.attributeValue("value"));
				});
			});
			apiConfig.setMap(map);
			return apiConfig;
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			System.out.println("找不到文件");
			e.printStackTrace();
		}
		return apiConfig;
		
	}
	
	public static Map<String,String> readProperties(String propertiesPath){
		Map<String,String> map = new HashMap<>();
		Properties properties = new Properties();
		FileInputStream inStream = null;
		try {
			inStream = new FileInputStream(propertiesPath);
			properties.load(inStream);
			String content = properties.getProperty("content");
			String imageBestBase = properties.getProperty("imageBestBase");
			String delta = properties.getProperty("delta");
			log.info("contentcontent::{}",content);
			log.info("imageBestBaseimageBestBase::{}",imageBestBase);
			log.info("deltadelta::{}",delta);

			map.put("content", content);
			map.put("imageBestBase", imageBestBase);
			map.put("delta", delta);
			
			for(Entry<String,String> entry: map.entrySet()) {
				log.info("key::{},value::{}",entry.getKey(),entry.getValue());
			}
			return map;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				if(inStream!=null) {
					inStream.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	public static void main(String[] args) {
		XmlPropertiesUtil xmlPropertiesUtil = new XmlPropertiesUtil();
//		xmlPropertiesUtil.readXml("src\\\\test\\\\resources\\\\apiConfig.xml");
		Map<String,String> map =XmlPropertiesUtil.readProperties("src\\test\\resources\\ktp.properties");
		log.info("======content{}",map.get("content"));
		
		log.info("imageBestBase======content{}",map.get("imageBestBase"));

		log.info("delta======content{}",map.get("delta"));

		
	}
}
