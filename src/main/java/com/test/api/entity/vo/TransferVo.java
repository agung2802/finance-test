package com.test.api.entity.vo;

import java.util.List;

import lombok.Data;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
@Data
public class TransferVo {
	/**
	 * 转账类型
	 */
	private List<Integer> transferTypeList;
	/**
	 * 转账类型是否正确
	 */
	private Boolean transferTypeAssertion;
	/**
	 * 类型为4的转账账号断言
	 */
	private Boolean transferAccount4Assersion;
	/**
	 * 类型为4的转账金额断言
	 */
	private Boolean transferAmount4Assersion;
	/**
	 * 类型为1的转账账号断言
	 */
	private Boolean transferAccount1Assersion;
	/**
	 * 类型为1的转账金额断言
	 */
	private Boolean transferAmount1Assersion;
	
	/**
	 * 类型为9的转账账号断言
	 */
	private Boolean transferAccount9Assersion;
	/**
	 * 类型为9的转账金额断言
	 */
	private Boolean transferAmount9Assersion;
	
	
	/**
	 * 类型为10的转账账号断言
	 */
	private Boolean transferAccount10Assersion;
	/**
	 * 类型为10的转账金额断言
	 */
	private Boolean transferAmount10Assersion;
	
	/**
	 * 类型为13的转账账号断言
	 */
	private Boolean transferAccount13Assersion;
	/**
	 * 类型为13的转账金额断言
	 */
	private Boolean transferAmount13Assersion;
	
	/**
	 * 类型为4的实际转账转账账号断言
	 */
	private Boolean actTransferAccount4Assersion;
	/**
	 * 类型为4的实际转账金额断言
	 */
	private Boolean actTransferAmount4Assersion;
	/**
	 * 类型为1的实际转账账号断言
	 */
	private Boolean actTransferAccount1Assersion;
	/**
	 * 类型为1的实际转账金额断言
	 */
	private Boolean actTransferAmount1Assersion;
	
	/**
	 * 类型为9的实际转账账号断言
	 */
	private Boolean actTransferAccount9Assersion;
	/**
	 * 类型为9的实际转账金额断言
	 */
	private Boolean actTransferAmount9Assersion;
	
	
	/**
	 * 类型为10的实际转账账号断言
	 */
	private Boolean actTransferAccount10Assersion;
	/**
	 * 类型为10的实际转账金额断言
	 */
	private Boolean actTransferAmount10Assersion;
	
	/**
	 * 类型为13的实际转账账号断言
	 */
	private Boolean actTransferAccount13Assersion;
	/**
	 * 类型为13的实际转账金额断言
	 */
	private Boolean actTransferAmount13Assersion;
	
	/**
	 * 给用户的转账账户
	 */
	private Boolean actTransferAccountToUser;
	/**
	 * 给用户的转账金额
	 */
	private Boolean actTransferAmountToUser;

}
