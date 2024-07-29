package com.test.api.entity.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;

import lombok.Data;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
@Data
public class LoanVo {
	/**
	 * 借款单号
	 */
	private String loanNumber;
	/**
	 * 借款期数
	 */
	private String tenor;
	/**
	 * 借款金额
	 */
	private long amount;
	/**
	 * 二级产品
	 */
	private String secondProductCode;
	/**
	 * 总的砍头费
	 */
	private long upfrontFee;
	/**
	 * 给用户的转账金额
	 */
	private int transferToUser;
	/**
	 * 管理费
	 */
	private long adminfee;
	
	/**
	 * 保证金费用
	 */
	private long provisionFee;
	/**
	 * 管理费和保证金比例
	 */
	private String adPvProportion;

	/**
	 * 资金方利息
	 */
	private BigDecimal fundInterestFee;
	/**
	 * 平台服务费
	 */
	private BigDecimal serviceFee;
	/**
	 * 保证金
	 */
	private BigDecimal repayProvisionFee;
	/**
	 * 总的砍头费率
	 */
	private BigDecimal upfrontFeeRate;
	/**
	 * 管理费率
	 */
	private BigDecimal adminfeeRate;
	
	/**
	 * 保证金费率
	 */
	private BigDecimal provisionFeeRate;

	/**
	 * 资金方利率
	 */
	private BigDecimal fundInterestRate;
	/**
	 * 平台服务费率
	 */
	private BigDecimal serviceFeeRate;
	
	
	/**
	 * 砍头费中的保证金费率
	 */
	private BigDecimal upfrontProvisionFeeRate;

	/**
	 * 砍头费中资金方利率
	 */
	private BigDecimal upfrontFundInterestRate;
	/**
	 * 砍头费中平台服务费率
	 */
	private BigDecimal upfrontServiceFeeRate;
	
	/**
	 * 砍头费中的保证金
	 */
	private BigDecimal upfrontProvisionFee;

	/**
	 * 砍头费中资金方
	 */
	private BigDecimal upfrontFundInterest;
	/**
	 * 砍头费中平台服务费
	 */
	private BigDecimal upfrontServiceFee;
	/**
	 * 砍头费中管理费
	 */
	private BigDecimal upfrontAdmin;	
	
	/**
	 * 砍头费中管理费税费
	 */
	private BigDecimal upfrontAdminVat;	

	/**
	 * 砍头费中服务费税费
	 */
	private BigDecimal upfrontServiceFeeVat;

	/**
	 * 砍头费中资金方税费
	 */
	private BigDecimal upfrontFundInterestWht;

	/**
	 * 砍头费中进位费不含税
	 */
	private BigDecimal upfronCarryFeeNoVat;
	/**
	 * 砍头费中进位费税费
	 */
	private BigDecimal upfronCarryFeeVat;
	/**
	 * 发薪日
	 */
	private String salaryDay;
	/**
	 * DDM差值
	 */
	private int ddmDay;
	/**
	 * A,B计划
	 */
	private String ddmPlan;
	/**
	 * 下一个还款时间
	 */
	private String nextDueDate;
	/**
	 * 最后一期的还款时间
	 */
	private String finallyDueDate;
	/**
	 * DDM下一个还款时间
	 */
	private String nextDueDateDDM;
	/**
	 * DDM最后一期的还款时间
	 */
	private String finallyDueDateDDM;
	/**
	 * 放款时间
	 */
	private LocalDateTime disbursedAt;
	/**
	 * 资金方
	 */
	private long fundId;
	/**
	 * 用户卡号
	 */
	private String bankAccountNumber;
	/**
	 * 卡号所属银行
	 */
	private String bankCode;
    /**
     * 放款方式:0-fifbranch,1-online,2-alfamart
     */
    private Integer lendType;
	/**
	 * 费率以及断言
	 */
	private LoanVoExt loanVoExt;
	/**
	 * userId
	 */
	private Long userId;
	/**
	 * 转账以及断言
	 */
	private TransferVo transferVo;
	/**
	 * loandetail中放款状态
	 */
	private Integer lendStatus;
	/**
	 * loandetail中还款状态
	 */
    private Integer repayStatus;
    /**
     * finance_lend中放款状态
     */
    private Integer status;
    	/**
    	 * komodo中数据状态
    	 */
    /**
     * 状态
     */
    private String state;
    /**
     * 保存状态校验结果
     */
    private CheckStatus checkStatus;
    /**
     * 查看配置表中的费率
     */
    private RateConfig rateConfig;
    /**
     * 保存后置处理结果
     */
    private HashMap<String,Boolean> assertionMap;
    /**
     * 逾期费
     */
    private BigDecimal lateRate;
    private BigDecimal advanceRate;
    /**
     	* 砍头费各费用
     */
    private UpfrontFeeVO upfrontFeeVO;
	/**
	 * 还款计划
	 */
	private DuesVo duesVo;
	/**
	 * 试算出来的费用
	 */
	private UpfrontDuesVo calUpfrontDuesVo;
	/**
	 * 提前结清试算返回
	 */
	private RepayByadvanceVo repayByadvanceVo;

}
