package com.xiaozhan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 点评回应
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("review_reply")
public class ReviewReply extends BaseEntity {

    private Long reviewId;

    private Long userId;

    private String role;

    private String content;
}
