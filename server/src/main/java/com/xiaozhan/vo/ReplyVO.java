package com.xiaozhan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点评回应
 */
@Data
@Schema(description = "点评回应")
public class ReplyVO {

    @Schema(description = "回应 ID")
    private Long id;

    @Schema(description = "回应人用户 ID")
    private Long userId;

    @Schema(description = "回应人显示名")
    private String userName;

    @Schema(description = "回应人头像")
    private String avatar;

    @Schema(description = "回应人角色")
    private String role;

    @Schema(description = "回应内容")
    private String content;

    @Schema(description = "回应时间")
    private LocalDateTime createTime;
}
