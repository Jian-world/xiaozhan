package com.xiaozhan.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaozhan.common.Result;
import com.xiaozhan.dto.CandidateQueryDTO;
import com.xiaozhan.dto.FavoriteDTO;
import com.xiaozhan.dto.InvitationDTO;
import com.xiaozhan.security.annotation.RequireRole;
import com.xiaozhan.service.CandidateService;
import com.xiaozhan.vo.CandidateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 企业端接口：人才检索、收藏、邀约
 */
@Tag(name = "07-企业端", description = "候选人检索、作品集查验、收藏、邀约、额度")
@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyController extends BaseController {

    private final CandidateService candidateService;

    /* ---- 人才检索 ---- */

    @RequireRole(RequireRole.Role.COMPANY)
    @Operation(summary = "候选人检索",
            description = "需企业认证通过；支持关键词、专业大类、学历、毕业年份、技能标签筛选")
    @PostMapping("/candidates/search")
    public Result<IPage<CandidateVO>> search(@RequestBody CandidateQueryDTO query,
                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "12") Integer pageSize) {
        return Result.success(candidateService.search(query, pageNum, pageSize));
    }

    @RequireRole(RequireRole.Role.COMPANY)
    @Operation(summary = "候选人详情",
            description = "含完整作品集与公开点评摘要；每次调用消耗 1 次查验额度")
    @GetMapping("/candidates/{studentId}")
    public Result<Map<String, Object>> candidateDetail(@PathVariable Long studentId) {
        return Result.success(candidateService.candidateDetail(studentId));
    }

    /* ---- 收藏 ---- */

    @RequireRole(RequireRole.Role.COMPANY)
    @Operation(summary = "收藏候选人")
    @PostMapping("/favorites")
    public Result<Void> favorite(@Valid @RequestBody FavoriteDTO dto) {
        candidateService.favorite(dto);
        return Result.success();
    }

    @RequireRole(RequireRole.Role.COMPANY)
    @Operation(summary = "取消收藏")
    @DeleteMapping("/favorites/{studentId}")
    public Result<Void> unfavorite(@PathVariable Long studentId) {
        candidateService.unfavorite(studentId);
        return Result.success();
    }

    @RequireRole(RequireRole.Role.COMPANY)
    @Operation(summary = "我的收藏列表")
    @GetMapping("/favorites")
    public Result<IPage<CandidateVO>> favorites(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize,
            @RequestParam(required = false) String listName) {
        return Result.success(candidateService.favorites(pageNum, pageSize, listName));
    }

    /* ---- 邀约 ---- */

    @RequireRole(RequireRole.Role.COMPANY)
    @Operation(summary = "发送邀约", description = "需企业认证通过")
    @PostMapping("/invitations")
    public Result<Long> sendInvitation(@Valid @RequestBody InvitationDTO dto) {
        return Result.success(candidateService.sendInvitation(dto));
    }

    @RequireRole(RequireRole.Role.COMPANY)
    @Operation(summary = "我发出的邀约")
    @GetMapping("/invitations")
    public Result<IPage<Map<String, Object>>> sentInvitations(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(candidateService.sentInvitations(pageNum, pageSize));
    }

    /* ---- 工作台 ---- */

    @RequireRole(RequireRole.Role.COMPANY)
    @Operation(summary = "企业工作台统计", description = "收藏数、邀约数、查验次数、套餐与剩余额度")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        return Result.success(candidateService.companyStatistics());
    }

    @RequireRole(RequireRole.Role.COMPANY)
    @Operation(summary = "查验记录")
    @GetMapping("/view-logs")
    public Result<IPage<Map<String, Object>>> viewLogs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(candidateService.viewLogs(pageNum, pageSize));
    }
}
