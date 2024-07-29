package com.test.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 用户银行卡信息
 * </p>
 *
 * @author Faisal Mulya Santosa
 * @since 2024-07-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FinanceUserBankcard implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 1->maucash
     */
    private Long orgId;

    private LocalDateTime createAt;

    /**
     * 更新时间
     */
    private LocalDateTime updateAt;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 银行卡号码
     */
    private String bankAccountNumber;

    /**
     * 银行编码
     */
    private String bankCode;

    /**
     * 银行卡账户持有者
     */
    private String bankAccountHolderName;

    /**
     * 是否使用(0-不可用,1-待验证,2-可使用,3-等待中)
     */
    private Integer isUsed;

    /**
     * 银行名称
     */
    private String bankName;


}
