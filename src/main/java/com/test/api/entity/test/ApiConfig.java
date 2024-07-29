package com.test.api.entity.test;

import java.util.List;
import java.util.Map;

public class ApiConfig {
	private  String rootUrl;
	private  List<Header> headerList;
	private  Map<String,String> map;
	
	public String getRootUrl() {
		return rootUrl;
	}
	public void setRootUrl(String rootUrl) {
		this.rootUrl = rootUrl;
	}
	public List<Header> getHeaderList() {
		return headerList;
	}
	public void setHeaderList(List<Header> headerList) {
		this.headerList = headerList;
	}
	public Map<String, String> getMap() {
		return map;
	}
	public void setMap(Map<String, String> map) {
		this.map = map;
	}
	public ApiConfig(String name, List<Header> headerList, Map<String, String> map) {
		super();
		this.rootUrl = name;
		this.headerList = headerList;
		this.map = map;
	}
	public ApiConfig() {
		super();
	}
	@Override
	public String toString() {
		return "ApiConfig [rootUrl=" + rootUrl + ", headerList=" + headerList + ", map=" + map + "]";
	}
	
}
