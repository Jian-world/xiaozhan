package com.xiaozhan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 企业档案更新
 */
@Data
@Schema(description = "企业档案更新")
public class CompanyProfileDTO {

    @Schema(description = "企业名称")
    private String companyName;

    @Schema(description = "所属行业")
    private String industry;

    @Schema(description = "企业规模")
    private String scale;
}
