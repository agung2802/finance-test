package com.test.api.entity;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 
* 类说明
*/
@Data
public class VatWht {

	private BigDecimal VAT;
	private List<Wht>  WHT;
}
