package com.xiaozhan.security.annotation;

import com.xiaozhan.common.ResultCode;
import com.xiaozhan.exception.BizException;
import com.xiaozhan.security.UserContext;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

/**
 * 角色校验注解。
 * <p>
 * 用法：{@code @RequireRole(Role.STUDENT)}，可传多个角色表示"满足其一即可"。
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    String[] value();

    /**
     * 常用角色常量
     */
    interface Role {
        String STUDENT = "STUDENT";
        String EXPERT = "EXPERT";
        String COMPANY = "COMPANY";
        String ADMIN = "ADMIN";
    }
}
