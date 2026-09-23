package com.xiaozhan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 认证审核（管理员）
 */
@Data
@Schema(description = "认证审核")
public class VerifyAuditDTO {

    @Schema(description = "认证记录 ID")
    private Long recordId;

    @NotBlank(message = "请给出审核结论")
    @Schema(description = "审核结论：PASS-通过 REJECT-驳回")
    private String status;

    @Schema(description = "审核意见 / 驳回原因")
    private String remark;
}
