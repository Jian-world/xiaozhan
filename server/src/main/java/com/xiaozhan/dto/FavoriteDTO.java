package com.xiaozhan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 企业收藏候选人
 */
@Data
@Schema(description = "收藏候选人")
public class FavoriteDTO {

    @NotNull(message = "缺少学生 ID")
    @Schema(description = "学生用户 ID")
    private Long studentId;

    @Schema(description = "收藏夹名称，默认「默认收藏夹」")
    private String listName;

    @Schema(description = "备注")
    private String remark;
}
