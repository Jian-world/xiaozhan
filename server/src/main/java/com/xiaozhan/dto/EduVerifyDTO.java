package com.xiaozhan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 学籍认证提交
 */
@Data
@Schema(description = "学籍认证提交")
public class EduVerifyDTO {

    @NotBlank(message = "请填写学校名称")
    @Schema(description = "学校名称")
    private String school;

    @NotBlank(message = "请填写专业")
    @Schema(description = "专业")
    private String major;

    @Schema(description = "专业大类：CS/DESIGN/ECON/OTHER")
    private String majorCategory;

    @Schema(description = "学历：本科/硕士/博士")
    private String degree;

    @Schema(description = "毕业年份")
    private Integer graduateYear;

    @NotBlank(message = "请上传学生证或在读证明")
    @Schema(description = "证明材料地址（先调上传接口）")
    private String eduVerifyFile;
}
