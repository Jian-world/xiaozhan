package com.xiaozhan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 企业发送邀约
 */
@Data
@Schema(description = "企业发送邀约")
public class InvitationDTO {

    @NotNull(message = "缺少学生 ID")
    @Schema(description = "学生用户 ID")
    private Long studentId;

    @Schema(description = "关联项目 ID")
    private Long projectId;

    @NotBlank(message = "请填写职位名称")
    @Schema(description = "职位名称", example = "后端开发工程师（校招）")
    private String jobTitle;

    @NotBlank(message = "请填写邀约内容")
    @Schema(description = "邀约内容")
    private String content;

    @Schema(description = "联系方式")
    private String contactInfo;
}
