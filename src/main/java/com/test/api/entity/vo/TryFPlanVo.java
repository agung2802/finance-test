package com.test.api.entity.vo;

import lombok.Data;

import java.util.List;



/**
 * @author Faisal Mulya Santosa
 * @create 2022-09-16 10:09
 * @description:
 */
@Data
public class TryFPlanVo {
    /**
     *总的还款金额
     */
    private long  totalAmount;
    /**
     *总的资金方利息
     */
    private long  totalFundInterest;
    /**
     *总的保证金金额
     */
    private long  totalProvisionFee;
    /**
     *总的平台服务费
     */
    private long  totalServiceFee;
    /**
     *总的本金
     */
    private long  totalPrincipal;
    /**
     *总的逾期费
     */
    private long  totalLateFee;
    /**
     *总的进位费
     */
    private long  totalCarryFee;
    /**
     *
     */
    private List<LoanFPlanVo> loanFPlanVos;
    /**
     * 本金减免比例
     */
    private String discountPrincipalRatio;
    /**
     * 逾期费减免比例
     */
    private String discountLFRatio;
    /**
     * 平台服务费减免比例
     */
    private String discountServiceFeeRatio;
}
