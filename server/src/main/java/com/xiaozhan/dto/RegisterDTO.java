package com.xiaozhan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求
 */
@Data
@Schema(description = "注册请求")
public class RegisterDTO {

    @NotBlank(message = "请填写登录账号")
    @Size(min = 4, max = 20, message = "账号长度需在 4-20 位之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "账号只能包含字母、数字和下划线")
    @Schema(description = "登录账号", example = "student2026")
    private String username;

    @NotBlank(message = "请填写密码")
    @Size(min = 6, max = 32, message = "密码长度需在 6-32 位之间")
    @Schema(description = "密码", example = "123456")
    private String password;

    @NotBlank(message = "请选择角色")
    @Pattern(regexp = "^(STUDENT|EXPERT|COMPANY)$", message = "角色不合法")
    @Schema(description = "角色：STUDENT/EXPERT/COMPANY", example = "STUDENT")
    private String role;

    @Schema(description = "昵称", example = "李思远")
    private String nickname;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    /* ---- 学生专属 ---- */
    @Schema(description = "学校（学生注册可填）")
    private String school;

    @Schema(description = "专业（学生注册可填）")
    private String major;

    @Schema(description = "专业大类：CS/DESIGN/ECON/OTHER")
    private String majorCategory;

    @Schema(description = "学历")
    private String degree;

    @Schema(description = "毕业年份")
    private Integer graduateYear;

    /* ---- 专家专属 ---- */
    @Schema(description = "所在单位（专家注册可填）")
    private String orgName;

    @Schema(description = "职位（专家注册可填）")
    private String position;

    /* ---- 企业专属 ---- */
    @Schema(description = "企业名称（企业注册可填）")
    private String companyName;

    @Schema(description = "行业")
    private String industry;
}
