package com.test.api.entity.vo;

import lombok.Data;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 
* 类说明
*/
@Data
public class CheckStatus {

	private Boolean checkLendStatusAssertion;
	
	private Boolean checkRepayStatusAssertion;
	
	private Boolean checkStatusAssertion;
	
	private Boolean checkLoansStateAssertion;
}
