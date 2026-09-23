package com.xiaozhan.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaozhan.common.Result;
import com.xiaozhan.security.annotation.RequireRole;
import com.xiaozhan.service.AdminService;
import com.xiaozhan.vo.ProjectVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 平台运营接口（管理员）
 */
@Tag(name = "09-平台运营", description = "总览统计、用户管理、内容审核、字典、埋点")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@RequireRole(RequireRole.Role.ADMIN)
public class AdminController extends BaseController {

    private final AdminService adminService;

    @Operation(summary = "平台总览统计", description = "用户数、项目数、点评数、认证率、专业与项目类型分布")
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(adminService.overview());
    }

    /* ---- 用户管理 ---- */

    @Operation(summary = "用户列表")
    @GetMapping("/users")
    public Result<IPage<Map<String, Object>>> users(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.success(adminService.users(pageNum, pageSize, role, status, keyword));
    }

    @Operation(summary = "启用 / 禁用用户")
    @PostMapping("/users/{id}/status")
    public Result<Void> toggleUserStatus(@PathVariable Long id, @RequestParam boolean enable) {
        adminService.toggleUserStatus(id, enable);
        return Result.success();
    }

    @Operation(summary = "重置用户密码", description = "返回新的随机密码，请通知用户及时修改")
    @PostMapping("/users/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Long id) {
        return Result.success(adminService.resetPassword(id));
    }

    /* ---- 内容审核 ---- */

    @Operation(summary = "内容审核列表")
    @GetMapping("/content-audits")
    public Result<IPage<Map<String, Object>>> contentAudits(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.success(adminService.contentAudits(pageNum, pageSize, status));
    }

    @Operation(summary = "内容审核处理", description = "pass=true 通过；false 下架")
    @PostMapping("/content-audits/{id}/handle")
    public Result<Void> handleAudit(@PathVariable Long id,
                                    @RequestParam boolean pass,
                                    @RequestParam(required = false) String remark) {
        adminService.handleAudit(id, pass, remark);
        return Result.success();
    }

    /* ---- 项目 ---- */

    @Operation(summary = "项目列表（全量）")
    @GetMapping("/projects")
    public Result<IPage<ProjectVO>> projects(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer reviewStatus) {
        return Result.success(adminService.projects(pageNum, pageSize, reviewStatus));
    }

    /* ---- 字典 / 埋点 ---- */

    @Operation(summary = "字典项", description = "不传 dictType 返回全部")
    @GetMapping("/dict")
    public Result<List<Map<String, Object>>> dict(@RequestParam(required = false) String dictType) {
        return Result.success(adminService.dict(dictType));
    }

    @Operation(summary = "埋点事件列表")
    @GetMapping("/track-events")
    public Result<IPage<Map<String, Object>>> trackEvents(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String eventCode) {
        return Result.success(adminService.trackEvents(pageNum, pageSize, eventCode));
    }
}
