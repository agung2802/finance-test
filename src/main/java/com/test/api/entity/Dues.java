package com.test.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 还款计划表
 * </p>
 *
 * @author Faisal Mulya Santosa
 * @since 2024-07-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Dues implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 借款单号
     */
    private String loanId;

    /**
     * MAUCASH-1,SPEKTRA-11,YN-TB-UVF-1001
     */
    private Long orgId;

    /**
     * 还款期数
     */
    private Integer dueIndex;

    /**
     * 还款日期
     */
    private LocalDate dueDate;

    /**
     * 还款类型
     */
    private String dueType;

    /**
     * 还款金额
     */
    private Long amount;

    /**
     * 还款顺序
     */
    private Integer repayIndex;

    /**
     * 已还金额
     */
    private Long settledAmount;

    /**
     * 未还金额
     */
    private Long remainingAmount;

    /**
     * 支付渠道编码
     */
    private String capitalCode;

    /**
     * 是否风险垫付(true:垫付; false:不垫付)
     */
    private String isAdvanced;

    /**
     * 是否是砍头费(true:是; false:否)
     */
    private String isKantouFee;

    /**
     * 优惠的逾期费（修改过差值）
     */
    private Long discountLateFee;

    /**
     * 已优惠金额
     */
    private Long couponFee;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 是否结清
     */
    private Boolean isClose;

    /**
     * 0-正常的,1->C1计划
     */
    private Integer planType;

    /**
     * 逾期本金
     */
    @TableField("Late_fee_amount")
    private Long lateFeeAmount;

    /**
     * 进位后的多处部分
     */
    private Long overAmount;

    /**
     * 0-解除锁定逾期，1-跳过计算逾期
     */
    private Boolean isLock;

    /**
     * 展期是否重新计算(true:是; false:否)
     */
    private Boolean isRecalculate;

    /**
     * 结清当前用户标签
     */
    private String creditTag;


}
