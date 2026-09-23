package com.xiaozhan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 沟通邀约
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("invitation")
public class Invitation extends BaseEntity {

    private Long companyId;

    private Long studentId;

    private Long projectId;

    private String jobTitle;

    private String content;

    /** 1-待回应 2-已同意 3-已拒绝 */
    private Integer status;

    private String contactInfo;

    private LocalDateTime replyTime;

    public static final int STATUS_PENDING = 1;
    public static final int STATUS_ACCEPTED = 2;
    public static final int STATUS_REJECTED = 3;
}
