package com.xiaozhan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 候选人（学生）摘要——企业检索结果
 */
@Data
@Schema(description = "候选人摘要")
public class CandidateVO {

    @Schema(description = "学生用户 ID")
    private Long studentId;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "真实姓名（仅企业认证后可见）")
    private String realName;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "学校")
    private String school;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "专业大类")
    private String majorCategory;

    @Schema(description = "学历")
    private String degree;

    @Schema(description = "毕业年份")
    private Integer graduateYear;

    @Schema(description = "学籍认证状态：0-未认证 1-待审核 2-已认证 3-已驳回")
    private Integer eduVerified;

    @Schema(description = "技能标签")
    private List<String> skillTags;

    @Schema(description = "一句话自述")
    private String bio;

    @Schema(description = "项目数量")
    private Integer projectCount;

    @Schema(description = "作品集被查看次数")
    private Integer portfolioViews;

    @Schema(description = "平均专家评分")
    private java.math.BigDecimal avgScore;

    @Schema(description = "代表项目（最多 3 个）")
    private List<ProjectBriefVO> topProjects;

    @Schema(description = "是否已收藏")
    private Boolean favorited;

    @Schema(description = "是否已发送邀约")
    private Boolean invited;
}
