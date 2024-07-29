package com.test.api.entity;

import lombok.Data;

import java.util.List;

/**
 * @author Faisal Mulya Santosa
 * @create 2022-09-16 9:58
 * @description:
 */
@Data
public class LoanTryFPlan {
    /**
     * 参加F计划的订单
     */
    private List<String> loanNumbers;
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

