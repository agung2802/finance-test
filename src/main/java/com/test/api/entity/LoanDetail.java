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
 * 贷款详情
 * </p>
 *
 * @author Faisal Mulya Santosa
 * @since 2024-07-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LoanDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String loanNumber;

    /**
     * MAUCASH-1,SPEKTRA-11,YN-TB-UVF-1001
     */
    private Long orgId;

    /**
     * 期数
     */
    private String period;

    /**
     * 金额
     */
    private Long amount;

    /**
     * 申请时间
     */
    private LocalDateTime applyAt;

    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    private String mobile;

    /**
     * 产品编号
     */
    private String productCode;

    /**
     * 放款时间
     */
    private LocalDateTime lendAt;

    /**
     * 放款方式:0-fifbranch,1-online,2-alfamart
     */
    private Integer lendType;

    /**
     * 放款状态:1-申请成功,2-放款中,3-放款成功,4-放款失败,5-超时,6-拒绝,7-签约拒绝,8-E计划新单放款成功
     */
    private Integer lendStatus;

    /**
     * 放款失败重试次数
     */
    private Integer retryNum;

    /**
     * 还款状态：1-还款中,2-逾期,3-结清,4-E计划新单结清的重组
     */
    private Integer repayStatus;

    private LocalDateTime createAt;

    /**
     * 更新时间
     */
    private LocalDateTime updateAt;

    /**
     * 是否提前结清
     */
    private Boolean isAdvance;

    /**
     * 提前结清金额
     */
    private Long advanceAmount;

    /**
     * 逾期自定义金额
     */
    private Long customAmount;

    /**
     * 风险级别
     */
    private String riskLevel;

    /**
     * 评分
     */
    private String scoreClass;

    private BigDecimal interestRate;

    /**
     * 资金方利息费率
     */
    private BigDecimal fundInterestRate;

    /**
     * 平台服务费
     */
    private BigDecimal serviceFeeRate;

    private BigDecimal adminRate;

    private BigDecimal lateRate;

    private BigDecimal advanceRate;

    /**
     * 催收标签
     */
    private String label;

    /**
     * 提前申请时间
     */
    private LocalDateTime advanceTime;

    /**
     * 标签修改时间
     */
    private LocalDateTime labelUpdateAt;

    /**
     * 提前结清进位差值
     */
    private Long overAmount;

    /**
     * 审批通过时间
     */
    private LocalDateTime approvedAt;

    /**
     * 资方通过时间
     */
    private LocalDateTime fundAt;

    /**
     * 申请的二级code
     */
    private String applySecondCode;

    /**
     * 申请金额
     */
    private Long applyAmount;

    /**
     * 申请期数
     */
    private String applyPeriod;

    /**
     * 产品小编号
     */
    private String secondProdCode;

    /**
     * 0-maucash 1-Cash loan 2-Paylater 3-TAPP
     */
    private Integer productType;

    /**
     * admin fee
     */
    private Long adminFee;

    /**
     * 资金方签约时间
     */
    private LocalDateTime fundSignAt;

    /**
     * 签约时间
     */
    private LocalDateTime timeOut;

    /**
     * 资金方Id
     */
    private Long fundId;

    /**
     * 每期还款金额
     */
    private Long repayDueAmount;

    /**
     * 优惠码
     */
    private String promotionCode;

    /**
     * 标签过期时间
     */
    private LocalDateTime labelExpiredAt;

    /**
     * 是否签约
     */
    private Boolean isSign;

    /**
     * 是否跳过超时状态(1-跳过,0-不跳过)
     */
    private Boolean isSkip;

    /**
     * 来源
     */
    private String origin;

    /**
     * 进件渠道: android, h5 ...
     */
    private String sourceId;

    /**
     * 特殊审批通过的优惠标识
     */
    private String specialPromotion;

    /**
     * 新的订单号
     */
    private String loanTransfer;

    /**
     * 老的订单号
     */
    private String previousLoan;

    /**
     * 用户类型
     */
    private Integer userType;

    /**
     * 业务类型：1-rebook
     */
    private Integer bizType;

    /**
     * 批次号
     */
    private String batchNo;


}
