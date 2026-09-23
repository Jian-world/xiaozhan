package com.xiaozhan.controller;

import com.xiaozhan.common.Result;
import com.xiaozhan.dto.ChangePasswordDTO;
import com.xiaozhan.dto.LoginDTO;
import com.xiaozhan.dto.RegisterDTO;
import com.xiaozhan.security.annotation.IgnoreAuth;
import com.xiaozhan.service.AuthService;
import com.xiaozhan.vo.LoginVO;
import com.xiaozhan.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口
 */
@Tag(name = "01-认证", description = "注册、登录、当前用户、修改密码")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final AuthService authService;

    @IgnoreAuth
    @Operation(summary = "注册", description = "支持学生 / 专家 / 企业三种角色，注册即登录")
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.success(authService.register(dto));
    }

    @IgnoreAuth
    @Operation(summary = "登录", description = "需传入登录身份 role")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @Operation(summary = "获取当前登录用户", description = "含角色对应的扩展档案")
    @GetMapping("/me")
    public Result<UserInfoVO> currentUser() {
        return Result.success(authService.currentUser());
    }

    @Operation(summary = "修改密码")
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        authService.changePassword(dto);
        return Result.success();
    }

    @Operation(summary = "退出登录", description = "JWT 无状态，前端清除 token 即可")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
}
