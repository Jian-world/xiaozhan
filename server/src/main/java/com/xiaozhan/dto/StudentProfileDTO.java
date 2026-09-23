package com.xiaozhan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 学生档案更新
 */
@Data
@Schema(description = "学生档案更新")
public class StudentProfileDTO {

    @Schema(description = "学校")
    private String school;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "专业大类：CS/DESIGN/ECON/OTHER")
    private String majorCategory;

    @Schema(description = "学历")
    private String degree;

    @Schema(description = "毕业年份")
    private Integer graduateYear;

    @Schema(description = "技能标签列表")
    private List<String> skillTags;

    @Schema(description = "一句话自述")
    private String bio;

    @Schema(description = "是否允许企业检索：0-否 1-是")
    private Integer allowCompanySearch;
}
