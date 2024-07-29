package com.test.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 用户信息表
 * </p>
 *
 * @author Faisal Mulya Santosa
 * @since 2024-07-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户表主键id。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 组织机构id
     */
    private Long orgId;

    /**
     * 用户名字
     */
    private String mobile;

    private String adminName;

    /**
     * 用户类型。1-普通用户,0-后台管理员
     */
    private Integer userType;

    /**
     * 加密后的登录密码
     */
    private String passwd;

    /**
     * 登录成功次数
     */
    private Integer loginSucessCount;

    /**
     * 密码错误次数。密码输入5次会锁定用户。
     */
    private Integer loginFailedCount;

    /**
     * 最后一次登录ip
     */
    private String lastLoginIp;

    /**
     * 最后一次登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 注册时的渠道号
     */
    private String origin;

    /**
     * 用户注册时的产品代号。指的是公司内部的产品分类或代号。
     */
    private String productCode;

    /**
     * 邀请人用户id。如果不是被邀请的或者根据手机号无法查询到邀请人，那就默认填0。
     */
    private Long invUserId;

    /**
     * 用户状态。0-停用；1-正常；2-锁定
     */
    private Integer state;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * google push token
     */
    private String googleFcmToken;

    /**
     * google 广告位ID
     */
    private String googleAdvertisingId;


}
