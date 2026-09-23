package com.xiaozhan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 项目摘要（列表卡片用）
 */
@Data
@Schema(description = "项目摘要")
public class ProjectBriefVO {

    @Schema(description = "项目 ID")
    private Long id;

    @Schema(description = "项目名称")
    private String name;

    @Schema(description = "方向")
    private String category;

    @Schema(description = "项目类型")
    private String projectType;

    @Schema(description = "一句话简介")
    private String summary;

    @Schema(description = "封面图")
    private String coverUrl;

    @Schema(description = "技术栈")
    private List<String> techStack;

    @Schema(description = "专家平均分")
    private BigDecimal expertAvgScore;

    @Schema(description = "专家点评数")
    private Integer expertReviewCount;

    @Schema(description = "浏览量")
    private Integer viewCount;
}
