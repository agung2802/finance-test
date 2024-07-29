package com.test.api.pojo;
/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
public class Case {
	private String caseId;
	private String caseName;
	private String apiId;
	private String params;
	private String isPositive;
	private String responseValidators;
	private String actualResponse;
	private String responseValidationResult;
	private String save;
	private String beforeExcute;
	private String beforeExcuteResult;
	
	private String afterExcute;
	private String afterExcuteResult;
	private String afterExcuteAssertionResult;

	public String getBeforeExcuteResult() {
		return beforeExcuteResult;
	}
	public void setBeforeExcuteResult(String beforeExcuteResult) {
		this.beforeExcuteResult = beforeExcuteResult;
	}
	public String getAfterExcuteAssertionResult() {
		return afterExcuteAssertionResult;
	}
	public void setAfterExcuteAssertionResult(String afterExcuteAssertionResult) {
		this.afterExcuteAssertionResult = afterExcuteAssertionResult;
	}
	private String no;
	public String getNo() {
		return no;
	}
	public String getAfterExcuteResult() {
		return afterExcuteResult;
	}
	public void setAfterExcuteResult(String afterExcuteResult) {
		this.afterExcuteResult = afterExcuteResult;
	}
	public void setNo(String no) {
		this.no = no;
	}
	public String getCaseId() {
		return caseId;
	}
	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}
	public String getCaseName() {
		return caseName;
	}
	public void setCaseName(String caseName) {
		this.caseName = caseName;
	}
	public String getApiId() {
		return apiId;
	}
	public void setApiId(String apiId) {
		this.apiId = apiId;
	}
	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}
	public String getIsPositive() {
		return isPositive;
	}
	public void setIsPositive(String isPositive) {
		this.isPositive = isPositive;
	}
	public Case(String caseId, String caseName, String apiId, String params, String isPositive,
			String responseValidators, String actualResponse, String responseValidationResult, String save,
			String beforeExcute, String afterExcute, String afterExcuteResult, String no) {
		super();
		this.caseId = caseId;
		this.caseName = caseName;
		this.apiId = apiId;
		this.params = params;
		this.isPositive = isPositive;
		this.responseValidators = responseValidators;
		this.actualResponse = actualResponse;
		this.responseValidationResult = responseValidationResult;
		this.save = save;
		this.beforeExcute = beforeExcute;
		this.afterExcute = afterExcute;
		this.afterExcuteResult = afterExcuteResult;
		this.no = no;
	}
	public String getResponseValidators() {
		return responseValidators;
	}
	public void setResponseValidators(String responseValidators) {
		this.responseValidators = responseValidators;
	}
	public String getActualResponse() {
		return actualResponse;
	}
	public void setActualResponse(String actualResponse) {
		this.actualResponse = actualResponse;
	}
	public String getResponseValidationResult() {
		return responseValidationResult;
	}
	public void setResponseValidationResult(String responseValidationResult) {
		this.responseValidationResult = responseValidationResult;
	}
	public String getSave() {
		return save;
	}
	public void setSave(String save) {
		this.save = save;
	}
	public Case() {
		super();
	}

	public Case(String caseId, String caseName, String apiId, String params, String isPositive,
			String responseValidators, String actualResponse, String responseValidationResult, String save,
			String beforeExcute, String beforeExcuteResult, String afterExcute, String afterExcuteResult,
			String afterExcuteAssertionResult, String no) {
		super();
		this.caseId = caseId;
		this.caseName = caseName;
		this.apiId = apiId;
		this.params = params;
		this.isPositive = isPositive;
		this.responseValidators = responseValidators;
		this.actualResponse = actualResponse;
		this.responseValidationResult = responseValidationResult;
		this.save = save;
		this.beforeExcute = beforeExcute;
		this.beforeExcuteResult = beforeExcuteResult;
		this.afterExcute = afterExcute;
		this.afterExcuteResult = afterExcuteResult;
		this.afterExcuteAssertionResult = afterExcuteAssertionResult;
		this.no = no;
	}
	@Override
	public String toString() {
		return "Case [caseId=" + caseId + ", caseName=" + caseName + ", apiId=" + apiId + ", params=" + params
				+ ", isPositive=" + isPositive + ", responseValidators=" + responseValidators + ", actualResponse="
				+ actualResponse + ", responseValidationResult=" + responseValidationResult + ", save=" + save
				+ ", beforeExcute=" + beforeExcute + ", beforeExcuteResult=" + beforeExcuteResult + ", afterExcute="
				+ afterExcute + ", afterExcuteResult=" + afterExcuteResult + ", afterExcuteAssertionResult="
				+ afterExcuteAssertionResult + ", no=" + no + ", getBeforeExcuteResult()=" + getBeforeExcuteResult()
				+ ", getAfterExcuteAssertionResult()=" + getAfterExcuteAssertionResult() + ", getNo()=" + getNo()
				+ ", getAfterExcuteResult()=" + getAfterExcuteResult() + ", getCaseId()=" + getCaseId()
				+ ", getCaseName()=" + getCaseName() + ", getApiId()=" + getApiId() + ", getParams()=" + getParams()
				+ ", getIsPositive()=" + getIsPositive() + ", getResponseValidators()=" + getResponseValidators()
				+ ", getActualResponse()=" + getActualResponse() + ", getResponseValidationResult()="
				+ getResponseValidationResult() + ", getSave()=" + getSave() + ", getBeforeExcute()="
				+ getBeforeExcute() + ", getAfterExcute()=" + getAfterExcute() + ", getClass()=" + getClass()
				+ ", hashCode()=" + hashCode() + ", toString()=" + super.toString() + "]";
	}
	public String getBeforeExcute() {
		return beforeExcute;
	}
	public void setBeforeExcute(String beforeExcute) {
		this.beforeExcute = beforeExcute;
	}
	public String getAfterExcute() {
		return afterExcute;
	}
	public void setAfterExcute(String afterExcute) {
		this.afterExcute = afterExcute;
	}
	
}
