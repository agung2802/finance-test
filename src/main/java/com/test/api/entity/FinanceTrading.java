package com.test.api.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 交易流水表
 * </p>
 *
 * @author Faisal Mulya Santosa
 * @since 2024-07-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FinanceTrading implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 贷款编号
     */
    private String loanNumber;

    /**
     * MAUCASH-1,SPEKTRA-11,YN-TB-UVF-1001
     */
    private Long orgId;

    /**
     * 出账账号
     */
    private String outAccountNo;

    /**
     * 入账账号
     */
    private String inAccountNo;

    /**
     * 转账类型:1-管理费,2-渠道费,3-放款费,4-充值,5-afmart充值,6-批量放款,7-批量渠道费,8-手续费,9-资金方利息,10-保证金,11-绑卡失败退款,12-延迟退款,13-平台服务费,14-motor批量充值,15-motor批量充值放款
     */
    private Integer type;

    /**
     * 交易金额
     */
    private Long amount;

    /**
     * 交易状态(0->初始化，1->成功,2->失败)
     */
    private Integer status;

    /**
     * 资金方ID
     */
    private Long fundId;

    /**
     * 交易网关类型:0-SIMULATE,2-BNI,4-FASPAY,5-PERMATA
     */
    private Integer fundType;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 网关交易号
     */
    private String orderId;

    /**
     * 备注
     */
    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 商户编码
     */
    private String merchantCode;


}
