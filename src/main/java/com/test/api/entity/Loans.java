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
 * 借款信息表
 * </p>
 *
 * @author Faisal Mulya Santosa
 * @since 2024-07-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Loans implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id自增长
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 借款编号
     */
    private String loanNumber;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 申请金额
     */
    private Long amount;

    /**
     * 期限
     */
    private String tenor;

    /**
     * 状态
     */
    private String state;

    /**
     * 申请时间
     */
    private LocalDateTime appliedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 审批时间
     */
    private LocalDateTime approvedAt;

    /**
     * 合同签订时间
     */
    private LocalDateTime agreementSignedAt;

    /**
     * 放款时间
     */
    private LocalDateTime disbursedAt;

    /**
     * 结束时间
     */
    private LocalDateTime closedAt;

    /**
     * 拒绝时间
     */
    private LocalDateTime rejectedAt;

    /**
     * 取消时间
     */
    private LocalDateTime cancelledAt;

    /**
     * 审批金额
     */
    private Long approvedAmount;

    /**
     * 审批期限
     */
    private String approvedTenor;

    /**
     * 利率
     */
    private BigDecimal flatRateInPercentage;

    /**
     * 资金方id
     */
    private Long lenderId;

    /**
     * 资金匹配时间
     */
    private LocalDateTime fundedAt;

    /**
     * 放款方式
     */
    private String disbursementMethodType;

    /**
     * 产品代码
     */
    private String productCode;

    /**
     * 进件来源
     */
    private String origin;

    /**
     * 进件类型: android, H5 ...
     */
    private String sourceId;

    /**
     * 未审批通过原因
     */
    private String notApprovedDesc;

    /**
     * wedefend sdk获取的id
     */
    private String wdDeviceId;

    /**
     * 通知第三方时间
     */
    private LocalDateTime informPartnerAt;

    /**
     * 线下放款码
     */
    private String disburseCode;

    /**
     * 二级产品code
     */
    private String secondProdCode;

    /**
     * 类型,0-普通订单;1-cash_loan;2-paylater
     */
    private Integer type;

    /**
     * 借款原因
     */
    private String loanReason;

    /**
     * 优惠码
     */
    private String promotionCode;


}
