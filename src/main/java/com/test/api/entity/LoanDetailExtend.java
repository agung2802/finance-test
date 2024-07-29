package com.test.api.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author Faisal Mulya Santosa
 * @since 2024-07-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LoanDetailExtend implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 借款单号
     */
    private String loanNumber;

    /**
     * 0-maucash 1-Cash loan 2-Paylater
     */
    private Integer productType;

    /**
     * 1-按比例,2-按固定,5-extend扩展
     */
    private Integer adminType;

    /**
     * 是否砍头费
     */
    private Boolean isHeadFee;

    /**
     * 是否查询成功
     */
    private Boolean isQuery;

    /**
     * 固定admin_fee
     */
    private Integer fixAdminFee;

    /**
     * 0
     */
    private String fixFundInterest;

    /**
     * 承若金比例
     */
    private String provisionRate;

    /**
     * 保证金费率
     */
    private BigDecimal fundProvisionRate;

    /**
     * 1-比例，3-列举
     */
    private Integer fundInterestType;

    /**
     * astra费率
     */
    private BigDecimal astraRate;

    /**
     * 商户折扣
     */
    private BigDecimal mdrRate;

    /**
     * 税费
     */
    private BigDecimal taxRate;

    /**
     * 1->指定折扣率,2->指定利率,3->减免
     */
    private Integer preferentialType;

    /**
     * 减免管理费
     */
    private String disInterestFee;

    /**
     * 减免平台服务费
     */
    private String disAdminFee;

    /**
     * 减免资金方利息费
     */
    private String disLateFee;

    /**
     * 优惠的砍头费
     */
    private String disUpfrontFee;

    /**
     * 发薪日
     */
    private String salaryDay;

    /**
     * 发薪日期(不用于DDM)
     */
    private String payDay;

    /**
     * 发薪方式
     */
    private String payCycle;

    /**
     * 交易类型
     */
    private String transType;

    /**
     * 商户产品编号
     */
    private String merchantProductCode;

    /**
     * 商户标识
     */
    private String merchantCode;

    /**
     * 合作方单号
     */
    private String merchantOrderNo;

    /**
     * 卖个第三方
     */
    private String buyerName;

    /**
     * 扩展信息
     */
    private String extend;

    /**
     * 扩展
     */
    private String ext;

    /**
     * MC500 Funder Interest
     */
    private Long fundInterest;

    /**
     * 砍头费的进位费
     */
    private Long upfrontCarryFee;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 减免保证金
     */
    private String disProvisionFee;

    /**
     * 减免资金方利息
     */
    private String disFundInterestFee;

    /**
     * 订单标记 1:Cancel，2：Fraud Activity & Cancel，3：Timeout
     */
    private Integer flag;

    /**
     * 订单标记原因
     */
    private String flagReason;

    /**
     * 费率扩展字段，基本格式:[{"collectionType":"upfrontFee/repay","feeType":"provisionRate","value":0.00479}]
     */
    private String rateExtend;

    /**
     * 最小期数
     */
    private String tenorMin;

    /**
     * 最大期数
     */
    private String tenorMax;

    /**
     * 是否有保费；枚举LoanDetailExtendEnum.IsPremium
     */
    private Integer isPremium;


}
