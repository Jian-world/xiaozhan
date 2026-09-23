package com.xiaozhan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 专家点评
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("expert_review")
public class ExpertReview extends BaseEntity {

    private Long projectId;

    private Long expertId;

    private Long studentId;

    /** 完成度 1-5 */
    private Integer scoreCompletion;

    /** 规范性 1-5 */
    private Integer scoreNormative;

    /** 创新性 1-5 */
    private Integer scoreInnovation;

    /** 工程/专业质量 1-5 */
    private Integer scoreTechnical;

    private BigDecimal avgScore;

    private String comment;

    private String strength;

    private String weakness;

    private String suggestion;

    /** POOL-求点评池 INVITE-定向邀请 */
    private String inviteType;

    /** 0-待校验 1-有效 2-嫌疑敷衍 */
    private Integer qualityStatus;

    private Integer isPublic;

    private Integer thanksFlag;

    /** 0-无 1-申诉中 2-申诉成立 3-申诉驳回 */
    private Integer appealStatus;

    public static final String INVITE_POOL = "POOL";
    public static final String INVITE_DIRECT = "INVITE";

    public static final int QUALITY_PENDING = 0;
    public static final int QUALITY_VALID = 1;
    public static final int QUALITY_SUSPECT = 2;

    /**
     * 计算平均分（保留两位小数）
     */
    public BigDecimal calcAvgScore() {
        int total = scoreCompletion + scoreNormative + scoreInnovation + scoreTechnical;
        return BigDecimal.valueOf(total).divide(BigDecimal.valueOf(4), 2, java.math.RoundingMode.HALF_UP);
    }
}
