package com.test.api.entity.vo;

import lombok.Data;

/**
 * @author Faisal Mulya Santosa
 * @create 2022-09-16 13:54
 * @description:
 */
@Data
public class LoanFPlanVo {
    /**
     *应还资金方利息
     */
    private long fundInterest;
    /**
     *应还保证金
     */
    private long provisionFee;
    /**
     *应还平台服务费包含服务费
     */
    private long serviceFee;
    /**
     *应还本金
     */
    private long principal;
    /**
     *应还逾期费
     */
    private long lateFee;
    /**
     *应还进位费
     */
    private long carryFee;
    /**
     *订单号
     */
    private String loanNumber;
    /**
     *减免前的应还款金额
     */
    private long orgTotalAmount;
    /**
     *减免后的应还款金额
     */
    private long totalAmount;


}
