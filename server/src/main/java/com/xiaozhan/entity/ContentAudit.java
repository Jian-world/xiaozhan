package com.xiaozhan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 内容审核记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("content_audit")
public class ContentAudit extends BaseEntity {

    /** PROJECT / DOC / VIDEO / REVIEW */
    private String targetType;

    private Long targetId;

    private String snapshot;

    /** PASS / SUSPECT */
    private String autoResult;

    /** 1-待审核 2-通过 3-下架 */
    private Integer status;

    private String remark;

    private Long handlerId;

    private LocalDateTime handleTime;

    public static final int STATUS_PENDING = 1;
    public static final int STATUS_PASSED = 2;
    public static final int STATUS_OFFLINE = 3;
}
