package com.xiaozhan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 点评申诉 / 回应
 */
@Data
@Schema(description = "点评申诉")
public class ReviewAppealDTO {

    @NotNull(message = "缺少点评 ID")
    @Schema(description = "点评 ID")
    private Long reviewId;

    @NotBlank(message = "请填写申诉理由")
    @Schema(description = "申诉理由", example = "该点评未阅读源码包中的 README……")
    private String reason;

    @Schema(description = "证据材料地址")
    private String evidenceUrl;
}
