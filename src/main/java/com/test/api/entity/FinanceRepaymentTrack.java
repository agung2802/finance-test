package com.test.api.entity;

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
public class FinanceRepaymentTrack implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 单号
     */
    private String loanNumber;

    /**
     * MAUCASH-1,SPEKTRA-11,YN-TB-UVF-1001
     */
    private Long orgId;

    /**
     * 产品二级编码
     */
    private String secondProdCode;

    /**
     * 期数
     */
    private Integer dueIndex;

    /**
     * 还款时间
     */
    private LocalDateTime repaymentAt;

    /**
     * 实际还款时间
     */
    private LocalDateTime actualRepaymentAt;

    /**
     * 还款方式:0-fifbranch,1-online,2-alfamart
     */
    private Integer type;

    /**
     * 交易流水号
     */
    private String transId;

    private Long userId;

    /**
     * 还款金额
     */
    private Long repaymentAmount;

    /**
     * 操作人id
     */
    private Long operatorId;

    private LocalDateTime createdAt;

    /**
     * 修改时间
     */
    private LocalDateTime updateAt;

    /**
     * 是否提前结清
     */
    private Boolean isAdvance;

    /**
     * 0-默认,1-被卖
     */
    private Integer markType;

    /**
     * 卖个第三方
     */
    private String buyerName;

    /**
     * 资金方利息
     */
    private Long fundInterestFee;

    /**
     * 平台服务费
     */
    private Long serviceFee;

    /**
     * 保证金费用
     */
    private Long provisionFee;

    /**
     * 利息
     */
    private Long interestFee;

    /**
     * 逾期费
     */
    private Long lateFee;

    /**
     * 本金
     */
    private Long principalFee;

    /**
     * 管理费
     */
    private Long adminFee;

    /**
     * 门店Id
     */
    private Long storeId;

    /**
     * 线上支付地址
     */
    private String onlineAddress;

    /**
     * 进位后的多处部分
     */
    private Long overAmount;

    /**
     * 资方Id
     */
    private Long fundId;

    /**
     * 放款渠道
     */
    private Integer fundType;

    /**
     * 是否自定义还款
     */
    private Boolean isCustom;

    /**
     * 0->未逾期,1-已逾期
     */
    private Boolean isOverdue;

    /**
     * 线下还款码
     */
    private String repayCode;

    /**
     * 收款账户
     */
    private String receiptAccount;

    private String bankCode;

    /**
     * 订单id
     */
    private String orderNo;

    /**
     * 0->正常,1->Sigap还款,2-催收还款,4-uvf还款
     */
    private Integer origin;

    /**
     * 0-正常,1->C1计划,2->F计划
     */
    private Integer planType;

    /**
     * 逾期费抵扣金额
     */
    private Long lateFeeDiscountAmount;

    /**
     * 优惠的逾期费(通过修改逾期费)
     */
    private Long discountLateFee;

    /**
     * 优惠资金方利息费
     */
    private Long couponLateFee;

    /**
     * 优惠平台服务费
     */
    private Long couponAdminFee;

    /**
     * 优惠管理费
     */
    private Long couponInterestFee;

    /**
     * 优惠本金
     */
    private Long couponPrincipal;

    /**
     * 计划减掉平台服务费
     */
    private Long subServiceFee;

    /**
     * 计划减掉资金方利息
     */
    private Long subFundInterestFee;

    /**
     * 优惠码
     */
    private String promotionCode;

    private Long fastPayFee;

    /**
     * 总vat税费
     */
    private Long vatFee;

    /**
     * 当前用户标签
     */
    private String creditTag;

    /**
     * 平台费税费
     */
    private Long pfVatFee;

    /**
     * 管理费税费
     */
    private Long afVatFee;

    /**
     * 进位费税费
     */
    private Long cfVatFee;

    /**
     * 逾期费税费
     */
    private Long lfVatFee;

    /**
     * 优惠资金方利息
     */
    private Long couponFundFee;

    /**
     * 优惠保证金
     */
    private Long couponProvisionFee;


}
