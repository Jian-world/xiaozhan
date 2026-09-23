package com.xiaozhan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录返回
 */
@Data
@Schema(description = "登录返回")
public class LoginVO {

    @Schema(description = "访问令牌")
    private String token;

    @Schema(description = "令牌有效期（毫秒）")
    private Long expire;

    @Schema(description = "当前登录用户信息")
    private UserInfoVO user;
}
