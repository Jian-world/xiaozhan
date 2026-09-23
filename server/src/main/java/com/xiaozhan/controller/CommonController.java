package com.xiaozhan.controller;

import com.xiaozhan.common.Result;
import com.xiaozhan.entity.Project;
import com.xiaozhan.entity.SysUser;
import com.xiaozhan.mapper.ExpertReviewMapper;
import com.xiaozhan.mapper.ProjectMapper;
import com.xiaozhan.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiaozhan.security.annotation.IgnoreAuth;
import com.xiaozhan.service.PublicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 公共接口：首页数据、平台概览、公开档案
 */
@Tag(name = "00-公共", description = "首页统计、公开档案、健康检查")
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonController {

    private final SysUserMapper sysUserMapper;

    private final ProjectMapper projectMapper;

    private final ExpertReviewMapper expertReviewMapper;

    private final PublicService publicService;

    @IgnoreAuth
    @Operation(summary = "平台公开统计", description = "用于首页展示，无需登录")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("studentCount", sysUserMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getRole, "STUDENT")));
        result.put("expertCount", sysUserMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getRole, "EXPERT")));
        result.put("companyCount", sysUserMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getRole, "COMPANY")));
        result.put("projectCount", projectMapper.selectCount(
                Wrappers.<Project>lambdaQuery().eq(Project::getPublishStatus, Project.PUBLISH_DONE)));
        result.put("reviewCount", expertReviewMapper.selectCount(null));
        return Result.success(result);
    }

    @IgnoreAuth
    @Operation(summary = "学生公开档案", description = "作品集页头部展示，未开放检索时隐藏真实姓名")
    @GetMapping("/student/{studentId}")
    public Result<Map<String, Object>> studentProfile(@PathVariable Long studentId) {
        return Result.success(publicService.studentPublicProfile(studentId));
    }

    @IgnoreAuth
    @Operation(summary = "专家广场", description = "已通过认证的导师与企业工程师列表")
    @GetMapping("/experts")
    public Result<Map<String, Object>> expertHall(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize,
            @RequestParam(required = false) String expertType) {
        return Result.success(publicService.expertHall(pageNum, pageSize, expertType));
    }

    @IgnoreAuth
    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("ok");
    }
}
