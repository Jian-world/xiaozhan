package com.xiaozhan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 企业认证提交
 */
@Data
@Schema(description = "企业认证提交")
public class CompanyVerifyDTO {

    @NotBlank(message = "请填写企业名称")
    @Schema(description = "企业名称")
    private String companyName;

    @NotBlank(message = "请填写所属行业")
    @Schema(description = "所属行业")
    private String industry;

    @Schema(description = "企业规模：1-50/50-200/200-1000/1000+")
    private String scale;

    @NotBlank(message = "请上传营业执照")
    @Schema(description = "营业执照地址（先调上传接口）")
    private String licenseFile;
}
