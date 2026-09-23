package com.xiaozhan.security;

import cn.hutool.core.util.StrUtil;
import com.xiaozhan.common.ResultCode;
import com.xiaozhan.config.XiaozhanProperties;
import com.xiaozhan.exception.BizException;
import com.xiaozhan.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 认证拦截器
 * <p>
 * 未标注 {@link com.xiaozhan.security.annotation.IgnoreAuth} 的接口均需登录。
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final XiaozhanProperties properties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 非控制器方法（静态资源等）直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        // 放行 OPTIONS 预检
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // 标注 @IgnoreAuth 的接口放行
        if (handlerMethod.hasMethodAnnotation(com.xiaozhan.security.annotation.IgnoreAuth.class)
                || handlerMethod.getBeanType().isAnnotationPresent(com.xiaozhan.security.annotation.IgnoreAuth.class)) {
            return true;
        }

        String header = request.getHeader(properties.getJwt().getHeader());
        String token = jwtUtil.resolveToken(header);
        if (StrUtil.isBlank(token)) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        Claims claims = jwtUtil.parseToken(token);
        if (claims == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }

        Long userId = Long.valueOf(String.valueOf(claims.get("userId")));
        String username = String.valueOf(claims.get("username"));
        String role = String.valueOf(claims.get("role"));
        UserContext.set(new UserContext.LoginUser(userId, username, role));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
