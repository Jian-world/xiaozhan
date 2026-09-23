package com.xiaozhan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码请求
 */
@Data
@Schema(description = "修改密码请求")
public class ChangePasswordDTO {

    @NotBlank(message = "请填写原密码")
    @Schema(description = "原密码")
    private String oldPassword;

    @NotBlank(message = "请填写新密码")
    @Size(min = 6, max = 32, message = "密码长度需在 6-32 位之间")
    @Schema(description = "新密码")
    private String newPassword;
}
