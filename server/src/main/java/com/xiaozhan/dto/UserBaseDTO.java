package com.xiaozhan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 通用基础资料更新（昵称、头像、联系方式）
 */
@Data
@Schema(description = "基础资料更新")
public class UserBaseDTO {

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;
}
