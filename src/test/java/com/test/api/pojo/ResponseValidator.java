package com.test.api.pojo;
/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
public class ResponseValidator {

	private String jsonPath;
	private String excepted;
	public ResponseValidator() {
		super();
	}
	public ResponseValidator(String jsonPath, String excepted) {
		super();
		this.jsonPath = jsonPath;
		this.excepted = excepted;
	}
	public String getJsonPath() {
		return jsonPath;
	}
	public void setJsonPath(String jsonPath) {
		this.jsonPath = jsonPath;
	}
	public String getExcepted() {
		return excepted;
	}
	public void setExcepted(String excepted) {
		this.excepted = excepted;
	}
	
}
