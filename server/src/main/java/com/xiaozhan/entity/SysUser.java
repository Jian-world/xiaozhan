package com.xiaozhan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String username;

    @JsonIgnore
    private String password;

    /** STUDENT / EXPERT / COMPANY / ADMIN */
    private String role;

    private String nickname;

    private String realName;

    private String avatar;

    private String phone;

    private String email;

    /** 0-禁用 1-正常 */
    private Integer status;

    private LocalDateTime lastLoginAt;
}
