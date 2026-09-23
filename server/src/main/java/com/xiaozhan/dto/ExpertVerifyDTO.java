package com.xiaozhan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 专家认证提交
 */
@Data
@Schema(description = "专家认证提交")
public class ExpertVerifyDTO {

    @NotBlank(message = "请填写所在单位")
    @Schema(description = "所在单位")
    private String orgName;

    @NotBlank(message = "请填写职位")
    @Schema(description = "职位")
    private String position;

    @NotBlank(message = "请选择专家类型")
    @Schema(description = "专家类型：TEACHER-高校导师 ENGINEER-企业工程师")
    private String expertType;

    @Schema(description = "擅长领域，逗号分隔")
    private String domain;

    @NotBlank(message = "请上传资质证明")
    @Schema(description = "资质证明地址（先调上传接口）")
    private String verifyFile;
}
