package com.test.api.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

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
public class GatewayTask implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 1-创建虚拟账号  3-支付  6-转账 7-创建收款单
     */
    private Integer type;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 机构id
     */
    private Integer fundId;

    /**
     * 机构名称
     */
    private String fundName;

    /**
     * 任务状态 	1-等待验证（调用第三方网关验证转账是否成功）	2-成功	3-失败	4-过期  无效数据	5-等待重试（重新请求第三方网关）
     */
    private Integer status;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 扩展id
     */
    private String extendId;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 第三方唯一id
     */
    private String thirdpartyId;

    /**
     * 借款单号
     */
    private String loanNum;

    /**
     * 请求参数
     */
    private String params;

    /**
     * 调用平台
     */
    private String platform;

    /**
     * 交易金额
     */
    private Long amount;

    /**
     * 业务线交易类型
     */
    private String tradingType;


}
