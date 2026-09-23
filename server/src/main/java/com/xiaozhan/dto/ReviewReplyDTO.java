package com.xiaozhan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 点评回应
 */
@Data
@Schema(description = "点评回应")
public class ReviewReplyDTO {

    @NotNull(message = "缺少点评 ID")
    @Schema(description = "点评 ID")
    private Long reviewId;

    @NotBlank(message = "请填写回应内容")
    @Schema(description = "回应内容")
    private String content;
}
