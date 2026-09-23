package com.xiaozhan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 登录请求
 */
@Data
@Schema(description = "登录请求")
public class LoginDTO {

    @NotBlank(message = "请填写登录账号")
    @Schema(description = "登录账号", example = "student01")
    private String username;

    @NotBlank(message = "请填写密码")
    @Schema(description = "密码", example = "123456")
    private String password;

    @NotBlank(message = "请选择登录身份")
    @Pattern(regexp = "^(STUDENT|EXPERT|COMPANY|ADMIN)$", message = "登录身份不合法")
    @Schema(description = "登录身份：STUDENT/EXPERT/COMPANY/ADMIN", example = "STUDENT")
    private String role;
}
