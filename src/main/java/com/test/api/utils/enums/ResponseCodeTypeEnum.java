package com.test.api.utils.enums;
/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29
* 类说明
*/
public enum ResponseCodeTypeEnum {
	SUCCESS("0","请求成功"),
	PARAMETER_ERROR("400","参数错误");
	private String code;
	private String message;
	private ResponseCodeTypeEnum(String code, String message) {
		this.code = code;
		this.message = message;
	}
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
	
}
