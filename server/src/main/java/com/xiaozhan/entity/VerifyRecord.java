package com.xiaozhan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 认证审核记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("verify_record")
public class VerifyRecord extends BaseEntity {

    /** STUDENT / EXPERT / COMPANY */
    private String bizType;

    private Long bizId;

    private Long applicantId;

    private String submitFile;

    /** 1-待审核 2-通过 3-驳回 */
    private Integer status;

    private String remark;

    private Long handlerId;

    private LocalDateTime handleTime;

    public static final String BIZ_STUDENT = "STUDENT";
    public static final String BIZ_EXPERT = "EXPERT";
    public static final String BIZ_COMPANY = "COMPANY";

    public static final int STATUS_PENDING = 1;
    public static final int STATUS_PASSED = 2;
    public static final int STATUS_REJECTED = 3;
}
