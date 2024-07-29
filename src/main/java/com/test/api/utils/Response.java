package com.test.api.utils;

import java.io.Serializable;

import org.apache.poi.ss.formula.functions.T;

import com.test.api.utils.enums.ResponseCodeTypeEnum;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
public class Response  {
	/*
	 * 默认成功的返回码*/
	private String code = ResponseCodeTypeEnum.SUCCESS.getCode();
	
	/*
	 * 默认成功的返回信息*/
	private String message = ResponseCodeTypeEnum.SUCCESS.getMessage();
	/*
	 * 返回结果*/
	private Object result;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
	public Response(String code, String message, Object result) {
		super();
		this.code = code;
		this.message = message;
		this.result = result;
	}
	public Response() {
		super();
	}
	public Response(Object result) {
		super();
		this.result = result;
	}
	public Response(String code, String message) {
		super();
		this.code = code;
		this.message = message;
	}
	
	
}
