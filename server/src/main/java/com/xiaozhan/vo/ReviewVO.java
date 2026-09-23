package com.xiaozhan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 专家点评
 */
@Data
@Schema(description = "专家点评")
public class ReviewVO {

    @Schema(description = "点评 ID")
    private Long id;

    @Schema(description = "项目 ID")
    private Long projectId;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "专家用户 ID")
    private Long expertId;

    @Schema(description = "专家显示名")
    private String expertName;

    @Schema(description = "专家头像")
    private String expertAvatar;

    @Schema(description = "专家所在单位")
    private String expertOrg;

    @Schema(description = "专家职位")
    private String expertPosition;

    @Schema(description = "专家类型：TEACHER/ENGINEER")
    private String expertType;

    @Schema(description = "专家等级：BRONZE/SILVER/GOLD")
    private String expertLevel;

    @Schema(description = "学生 ID")
    private Long studentId;

    @Schema(description = "学生昵称")
    private String studentName;

    @Schema(description = "完成度评分")
    private Integer scoreCompletion;

    @Schema(description = "规范性评分")
    private Integer scoreNormative;

    @Schema(description = "创新性评分")
    private Integer scoreInnovation;

    @Schema(description = "专业质量评分")
    private Integer scoreTechnical;

    @Schema(description = "平均分")
    private BigDecimal avgScore;

    @Schema(description = "总评")
    private String comment;

    @Schema(description = "亮点")
    private String strength;

    @Schema(description = "不足")
    private String weakness;

    @Schema(description = "改进建议")
    private String suggestion;

    @Schema(description = "来源：POOL-求点评池 INVITE-定向邀请")
    private String inviteType;

    @Schema(description = "质量校验：0-待校验 1-有效 2-嫌疑敷衍")
    private Integer qualityStatus;

    @Schema(description = "是否公开")
    private Integer isPublic;

    @Schema(description = "学生是否已致谢")
    private Integer thanksFlag;

    @Schema(description = "申诉状态：0-无 1-申诉中 2-成立 3-驳回")
    private Integer appealStatus;

    @Schema(description = "回应列表")
    private List<ReplyVO> replies;

    @Schema(description = "点评时间")
    private LocalDateTime createTime;
}
