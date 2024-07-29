package com.test.api.entity.vo;

import java.math.BigDecimal;

import com.test.api.entity.DiscountUpfrontFeeVo;
import com.test.api.entity.UpfrontExtendFee;

import lombok.Data;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 
* 类说明
*/
@Data
public class UpfrontFeeVO {
	/**
	 * 资金方id
	 */
	private int fundId;
	/**
	 * 原先的砍头费
	 */
	private BigDecimal originalUpfrontFee;
	/**
	 * 含税的砍头费
	 */
	private BigDecimal upfrontFee;
	/**
	 * 使用优惠前的金额
	 */
	private BigDecimal beforeDisUpfrontFee;
	/**
	 * 使用优惠码减免的砍头费
	 */
	private BigDecimal disUpfrontFee;

	/**
	 * 用户收到的钱
	 */
	private BigDecimal receivedMoney;
	/**
	 * 管理费
	 */
	private BigDecimal upfrontAdminfee;
	/**
	 * 保证金
	 */
	private BigDecimal upfrontProvisionFee;
	/**
	 * 管理费税费
	 */
	private BigDecimal upfrontAdminfeeVat;
	/**
	 * 进位费
	 */
	private BigDecimal upfrontCarryFeeIncludeVat;
	/**
	 * 进位费(不含税)
	 */
	private BigDecimal upfrontCarryFee;
	/**
	 * 进位费(进位费税费)
	 */
	private BigDecimal upfrontCarryFeeVat;
	/**
	 * v2产品的砍头费
	 */
	private UpfrontExtendFee extendFee;
	/**
	 * 放款优惠码
	 */
	private DiscountUpfrontFeeVo discountUpfrontFeeVo;
	/**
	 * 砍头费中的保费
	 */
	private BigDecimal  premiumFee;

	@Override
	public String toString() {
		return "UpfrontFeeVO [originalUpfrontFee=" + originalUpfrontFee + ", upfrontFee=" + upfrontFee
				+ ", beforeDisUpfrontFee=" + beforeDisUpfrontFee + ", disUpfrontFee=" + disUpfrontFee
				+ ", receivedMoney=" + receivedMoney + ", upfrontAdminfee=" + upfrontAdminfee + ", upfrontProvisionFee="
				+ upfrontProvisionFee + ", upfrontAdminfeeVat=" + upfrontAdminfeeVat + ", upfrontCarryFeeIncludeVat="
				+ upfrontCarryFeeIncludeVat + ", upfrontCarryFee=" + upfrontCarryFee + ", upfrontCarryFeeVat="
				+ upfrontCarryFeeVat + ", extendFee=" + extendFee + ", discountUpfrontFeeVo=" + discountUpfrontFeeVo+",premiumFee"+premiumFee
				+ "]";
	}

}
