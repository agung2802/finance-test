package com.test.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 合作渠道用户信息
 * </p>
 *
 * @author Faisal Mulya Santosa
 * @since 2024-07-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TbUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 合作方名称
     */
    private String partner;

    private Long userId;

    private String name;

    /**
     * 真实名字
     */
    private String picName;

    /**
     * 电话号码
     */
    private String mobile;

    /**
     * nik
     */
    private String nik;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 其他相关信息（来自mongodb id）
     */
    private String docId;

    /**
     * 上级用户id
     */
    private Long parentUserId;

    /**
     * 第三方用户编号
     */
    private String partnerUserNo;

    /**
     * 用户类型
     */
    private Integer userType;

    private Boolean deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;


}
