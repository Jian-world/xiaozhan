package com.xiaozhan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 邀请专家定向点评
 */
@Data
@Schema(description = "邀请专家定向点评")
public class ReviewInviteDTO {

    @Schema(description = "项目 ID")
    private Long projectId;

    @Schema(description = "被邀请的专家用户 ID（与 expertId 二选一）")
    private Long expertUserId;

    @Schema(description = "被邀请的专家档案 ID（与 expertUserId 二选一）")
    private Long expertId;

    @Schema(description = "邀请留言")
    private String message;

    @Schema(description = "希望重点点评的方面")
    private String focus;
}
