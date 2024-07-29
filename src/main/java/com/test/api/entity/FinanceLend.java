package com.test.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 放款记录
 * </p>
 *
 * @author Faisal Mulya Santosa
 * @since 2024-07-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FinanceLend implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 借款单号
     */
    private String loanNumber;

    /**
     * MAUCASH-1,SPEKTRA-11,YN-TB-UVF-1001
     */
    private Long orgId;

    /**
     * 1->针对用户，2->针对商家
     */
    private Integer mode;

    /**
     * 1->已支付adminFee，0->未支付adminFee
     */
    private Boolean isPayAdminfee;

    /**
     * 是否支付渠道费用
     */
    private Boolean isPayChannelfee;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 放款金额
     */
    private Long amount;

    /**
     * 渠道费用
     */
    private Long channelFee;

    /**
     * 其他费用
     */
    private Long otherFee;

    /**
     * 准备金(adminFee拆开的一部分)
     */
    private Long provisionFee;

    /**
     * astra费用
     */
    private Long astraFee;

    /**
     * 剩余金额
     */
    private Long surplusAmount;

    /**
     * 用户实际收到的金额
     */
    private Long receivedAmount;

    /**
     * 放款账户
     */
    private String lendAccount;

    /**
     * 银行
     */
    private String bankCode;

    /**
     * 收款账户
     */
    private String receiptAccount;

    /**
     * 账户名称
     */
    private String accountName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 修改时间
     */
    private LocalDateTime updatedAt;

    /**
     * 交易流水号
     */
    private String transId;

    /**
     * 放款编号
     */
    @TableField("lendId")
    private String lendId;

    /**
     * 放款状态:0-预支付,1-支付中,2-支付异常,3-支付成功,4-支付失败,5-E计划新单支付成功
     */
    private Integer status;

    /**
     * 资金方Id
     */
    private Long fundId;

    /**
     * 放款方式:0-fifbranch,1-online,2-alfamart
     */
    private Integer lendType;

    /**
     * 门店Id
     */
    private Long storeId;

    /**
     * 放款渠道（）
     */
    private Integer fundType;

    /**
     * 订单id
     */
    private String orderNo;

    /**
     * 资金方利息
     */
    private Long fundInterestFee;

    /**
     * 平台服务费
     */
    private Long serviceFee;

    /**
     * 进位费各项明细
     */
    private String carryExt;


}
