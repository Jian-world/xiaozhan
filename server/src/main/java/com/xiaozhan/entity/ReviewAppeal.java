package com.xiaozhan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 点评申诉
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("review_appeal")
public class ReviewAppeal extends BaseEntity {

    private Long reviewId;

    private Long studentId;

    private String reason;

    private String evidenceUrl;

    /** 1-待处理 2-成立 3-驳回 */
    private Integer status;

    private String handleRemark;

    private Long handlerId;

    private LocalDateTime handleTime;

    public static final int STATUS_PENDING = 1;
    public static final int STATUS_ACCEPTED = 2;
    public static final int STATUS_REJECTED = 3;
}
