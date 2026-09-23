package com.xiaozhan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 专家档案
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("expert_profile")
public class ExpertProfile extends BaseEntity {

    private Long userId;

    private String orgName;

    private String position;

    /** TEACHER-高校导师 ENGINEER-企业工程师 */
    private String expertType;

    /** 擅长领域，逗号分隔 */
    private String domain;

    private String verifyFile;

    /** 0-待提交 1-待审核 2-已通过 3-已驳回 */
    private Integer verifyStatus;

    private String verifyRemark;

    /** 是否公开单位信息 */
    private Integer showOrg;

    private Integer dailyQuota;

    private Integer totalReviews;

    private Integer thanksCount;

    private BigDecimal qualityScore;

    /** BRONZE / SILVER / GOLD */
    private String level;

    private Integer points;

    public static final int STATUS_NONE = 0;
    public static final int STATUS_PENDING = 1;
    public static final int STATUS_PASSED = 2;
    public static final int STATUS_REJECTED = 3;
}
