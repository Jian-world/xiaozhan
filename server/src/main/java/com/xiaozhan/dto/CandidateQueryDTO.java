package com.xiaozhan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 候选人检索条件
 */
@Data
@Schema(description = "候选人检索条件")
public class CandidateQueryDTO {

    @Schema(description = "关键词：姓名 / 学校 / 技能 / 项目名")
    private String keyword;

    @Schema(description = "专业大类：CS/DESIGN/ECON/OTHER")
    private String majorCategory;

    @Schema(description = "学历")
    private String degree;

    @Schema(description = "毕业年份")
    private Integer graduateYear;

    @Schema(description = "技能标签（任一命中），逗号分隔")
    private String skillTags;

    @Schema(description = "仅看已通过学籍认证：0-否 1-是")
    private Integer onlyVerified;

    @Schema(description = "排序：latest / score / views")
    private String sortBy;
}
