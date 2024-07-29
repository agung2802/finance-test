package com.test.api.utils;


import com.test.api.entity.test.ApiConfig;
import com.test.api.entity.test.Header;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

//import javax.annotation.PostConstruct;
import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
@Slf4j
@Service
public class XmlPropertiesUtil {
	public static ApiConfig apiConfig;
	@PostConstruct
	public void initApiConfig(){
		log.info("apiConfig:{}",apiConfig);
		//apiConfig = readXml("src/main/resources/apiConfig.xml");
		apiConfig = readXml("apiConfig.xml");

		log.info("apiConfig:{}",apiConfig);
	}
	public ApiConfig readXml(String configXmlPath){
		ApiConfig apiConfig = new ApiConfig();
		SAXReader saxReader = new SAXReader();
		try {
			log.info("configXmlPath:{}"+configXmlPath);
			ClassPathResource classPathResource = new ClassPathResource(configXmlPath);
			String path = classPathResource.getPath();
			InputStream inputStream = classPathResource.getInputStream();
//			String path = this.getClass().getClassLoader().getResource(configXmlPath).getFile();
			log.info("path:{}",path);
//			Document document = saxReader.read(configXmlPath);
			Document document = saxReader.read(inputStream);
			Element rootElement = document.getRootElement();
			apiConfig.setRootUrl(rootElement.element("rootUrl").getTextTrim());
			log.info("RootUrl:{}"+apiConfig.getRootUrl());
			//取出headers中信息
			List<Element> headerElement = rootElement.elements("headers");
			List<Header> headerList = new ArrayList<>();
			Map<String,String> map =new HashMap();
			headerElement.forEach(el->{
				String attributeValue = el.attributeValue("name");
				Header header = new Header();
				List<com.test.api.entity.test.Element> list = new ArrayList<>();
				header.setName(attributeValue);
				log.info("attributeValue:{}"+attributeValue);
				List<Element> elements = el.elements();
				elements.forEach(ele->{
					list.add(new com.test.api.entity.test.Element(ele.attributeValue("name"),ele.attributeValue("value")));
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
			log.info("初始化完apiConfig:{}",apiConfig);
			return apiConfig;
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			log.info("找不到文件");
			e.printStackTrace();
		} catch (IOException e) {
			throw new RuntimeException(e);
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
