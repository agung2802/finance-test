package com.test.api.mapper;

import java.util.HashMap;

import org.apache.poi.ss.formula.functions.T;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29
* 类说明
*/
@DS("master")
public interface UpfrontDuesMapper extends BaseMapper<T> {
	/**
	 * 查询产品费率
	 * @param fundId
	 * @param secondProdCode
	 * @return
	 */
	HashMap<String,Object> getRateConfig(int fundId,String secondProdCode);
	/**
	 * 查询优惠码费率
	 * @param promotionCode
	 * @return
	 */
	HashMap<String,Object> getPromotionRateConfig(String promotionCode );
	/**
	 * 
	 * @return
	 */
	String getVatWht(String configKey);
}
