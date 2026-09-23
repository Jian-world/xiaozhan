package com.xiaozhan.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaozhan.common.Result;
import com.xiaozhan.dto.ProjectSaveDTO;
import com.xiaozhan.entity.ProjectAsset;
import com.xiaozhan.security.annotation.IgnoreAuth;
import com.xiaozhan.security.annotation.RequireRole;
import com.xiaozhan.service.ProjectService;
import com.xiaozhan.vo.ProjectVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 项目作品集接口
 */
@Tag(name = "05-项目作品集", description = "项目增删改查、发布、素材、求点评池、作品集")
@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController extends BaseController {

    private final ProjectService projectService;

    /* ---- 增删改 ---- */

    @RequireRole(RequireRole.Role.STUDENT)
    @Operation(summary = "新建项目")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ProjectSaveDTO dto) {
        return Result.success(projectService.create(dto));
    }

    @RequireRole(RequireRole.Role.STUDENT)
    @Operation(summary = "编辑项目")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody ProjectSaveDTO dto) {
        projectService.update(dto);
        return Result.success();
    }

    @RequireRole(RequireRole.Role.STUDENT)
    @Operation(summary = "删除项目")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return Result.success();
    }

    /* ---- 查询 ---- */

    @Operation(summary = "项目详情", description = "企业身份访问会记录查验埋点")
    @GetMapping("/{id}")
    public Result<ProjectVO> detail(@PathVariable Long id,
                                    @Parameter(description = "是否企业查验视角")
                                    @RequestParam(defaultValue = "false") boolean forCompany) {
        return Result.success(projectService.detail(id, forCompany));
    }

    @RequireRole(RequireRole.Role.STUDENT)
    @Operation(summary = "我的项目列表")
    @GetMapping("/my")
    public Result<IPage<ProjectVO>> myProjects(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer publishStatus) {
        return Result.success(projectService.myProjects(pageNum, pageSize, publishStatus));
    }

    @Operation(summary = "学生作品集", description = "该学生所有已发布且未设为私密的项目")
    @GetMapping("/portfolio/{studentId}")
    public Result<List<ProjectVO>> portfolio(@PathVariable Long studentId) {
        return Result.success(projectService.portfolioOf(studentId));
    }

    @IgnoreAuth
    @Operation(summary = "项目广场 / 企业检索",
            description = "sortBy 可选：latest（默认）/ score / views")
    @GetMapping("/search")
    public Result<IPage<ProjectVO>> search(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sortBy) {
        return Result.success(projectService.search(pageNum, pageSize, keyword, category, sortBy));
    }

    @RequireRole(RequireRole.Role.EXPERT)
    @Operation(summary = "求点评池", description = "专家领取待点评项目")
    @GetMapping("/review-pool")
    public Result<IPage<ProjectVO>> reviewPool(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category) {
        return Result.success(projectService.reviewPool(pageNum, pageSize, category));
    }

    /* ---- 发布 ---- */

    @RequireRole(RequireRole.Role.STUDENT)
    @Operation(summary = "发布项目", description = "至少上传一份素材后才可发布")
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        projectService.publish(id);
        return Result.success();
    }

    @RequireRole(RequireRole.Role.STUDENT)
    @Operation(summary = "下架项目")
    @PostMapping("/{id}/unpublish")
    public Result<Void> unpublish(@PathVariable Long id) {
        projectService.unpublish(id);
        return Result.success();
    }

    @RequireRole(RequireRole.Role.STUDENT)
    @Operation(summary = "加入 / 退出求点评池")
    @PostMapping("/{id}/review-pool")
    public Result<Void> toggleReviewPool(@PathVariable Long id,
                                         @RequestParam(defaultValue = "true") boolean join) {
        projectService.toggleReviewPool(id, join);
        return Result.success();
    }

    /* ---- 素材 ---- */

    @RequireRole(RequireRole.Role.STUDENT)
    @Operation(summary = "登记项目素材",
            description = "先调用 /api/file/upload 上传文件，再把返回的 url 与元信息登记到项目")
    @PostMapping("/{id}/asset")
    public Result<List<ProjectAsset>> saveAsset(@PathVariable Long id,
                                                @RequestBody ProjectAsset asset) {
        return Result.success(projectService.saveAsset(id, asset));
    }

    @RequireRole(RequireRole.Role.STUDENT)
    @Operation(summary = "删除项目素材")
    @DeleteMapping("/asset/{assetId}")
    public Result<Void> deleteAsset(@PathVariable Long assetId) {
        projectService.deleteAsset(assetId);
        return Result.success();
    }

    /* ---- 统计 ---- */

    @RequireRole(RequireRole.Role.STUDENT)
    @Operation(summary = "学生作品集统计", description = "项目数、浏览数、点评数、求点评池数量等")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        return Result.success(projectService.statistics(currentUserId()));
    }
}
