package com.test.api.entity;

import java.math.BigDecimal;

import lombok.Data;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29
* 类说明
*/
@Data
public class Wht {
	private BigDecimal rate;
	private String fundCode;
	private int fundId;


}
