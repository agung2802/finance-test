package com.test.api.entity.test;
/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
public class Api {

	 private String apiId;
	 private String apiName;
	 private String apiMethod;
	 private String apiType;
	 private String url;
	 private String header;
	public String getApiId() {
		return apiId;
	}
	public void setApiId(String apiId) {
		this.apiId = apiId;
	}
	public String getApiName() {
		return apiName;
	}
	public void setApiName(String apiName) {
		this.apiName = apiName;
	}
	public String getApiMethod() {
		return apiMethod;
	}
	public void setApiMethod(String apiMethod) {
		this.apiMethod = apiMethod;
	}
	public String getApiType() {
		return apiType;
	}
	public void setApiType(String apiType) {
		this.apiType = apiType;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getHeader() {
		return header;
	}
	public void setHeader(String header) {
		this.header = header;
	}
	public Api() {
		super();
	}
	public Api(String apiId, String apiName, String apiMethod, String apiType, String url, String header) {
		super();
		this.apiId = apiId;
		this.apiName = apiName;
		this.apiMethod = apiMethod;
		this.apiType = apiType;
		this.url = url;
		this.header = header;
	}
	@Override
	public String toString() {
		return "Api [apiId=" + apiId + ", apiName=" + apiName + ", apiMethod=" + apiMethod + ", apiType=" + apiType
				+ ", url=" + url + ", header=" + header + "]";
	}
	 

}
