package com.xiaozhan.security;

import com.xiaozhan.common.ResultCode;
import com.xiaozhan.exception.BizException;
import com.xiaozhan.security.annotation.RequireRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * 角色权限拦截器
 */
@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RequireRole annotation = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), RequireRole.class);
        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), RequireRole.class);
        }
        if (annotation == null) {
            return true;
        }
        String role = UserContext.getRole();
        if (role == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        boolean allowed = Arrays.asList(annotation.value()).contains(role);
        if (!allowed) {
            throw new BizException(ResultCode.FORBIDDEN,
                    "当前角色无权访问该接口，需要角色：" + String.join("/", annotation.value()));
        }
        return true;
    }
}
