package com.test.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 资金方电子账户
 * </p>
 *
 * @author Faisal Mulya Santosa
 * @since 2024-07-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FundAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 资金方信息ID
     */
    private Long fundBaseId;

    /**
     * 资金方账号（分放款账户，还款账户）
     */
    private String accountNo;

    /**
     * 交易网关类型:2-BNI,4-FASPAY,5-PERMATA
     */
    private Integer gatewayType;

    /**
     * 资金方CODE
     */
    private String fundCode;

    /**
     * 账户余额
     */
    private Long amount;

    /**
     * 0：放款总账户，1：还款总账户，2：放款分账账户，3：还款分账账户,4：其他用户指定管理账户
     */
    private Integer accountType;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    /**
     * 账号备注
     */
    private String remark;


}
