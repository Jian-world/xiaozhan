package com.xiaozhan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 专家提交点评
 */
@Data
@Schema(description = "专家提交点评")
public class ReviewSubmitDTO {

    @NotNull(message = "缺少项目 ID")
    @Schema(description = "项目 ID")
    private Long projectId;

    @NotNull(message = "请给出完成度评分")
    @Min(value = 1, message = "评分最低 1 分")
    @Max(value = 5, message = "评分最高 5 分")
    @Schema(description = "完成度 1-5")
    private Integer scoreCompletion;

    @NotNull(message = "请给出规范性评分")
    @Min(value = 1, message = "评分最低 1 分")
    @Max(value = 5, message = "评分最高 5 分")
    @Schema(description = "规范性 1-5")
    private Integer scoreNormative;

    @NotNull(message = "请给出创新性评分")
    @Min(value = 1, message = "评分最低 1 分")
    @Max(value = 5, message = "评分最高 5 分")
    @Schema(description = "创新性 1-5")
    private Integer scoreInnovation;

    @NotNull(message = "请给出专业质量评分")
    @Min(value = 1, message = "评分最低 1 分")
    @Max(value = 5, message = "评分最高 5 分")
    @Schema(description = "工程 / 专业质量 1-5")
    private Integer scoreTechnical;

    @NotBlank(message = "请填写总评")
    @Schema(description = "总评", example = "整体完成度较高，架构分层清晰……")
    private String comment;

    @Schema(description = "亮点")
    private String strength;

    @Schema(description = "不足")
    private String weakness;

    @Schema(description = "改进建议")
    private String suggestion;

    @Schema(description = "是否公开：0-否 1-是")
    private Integer isPublic;
}
