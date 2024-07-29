package com.test.api.service;

import com.test.api.entity.TryInfo;
import com.test.api.entity.vo.UpfrontDuesVo;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
public interface CostCalculationService {

	UpfrontDuesVo CalculateUpfrontDues(TryInfo tryInfo);
}
