package com.xiaozhan.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaozhan.common.Result;
import com.xiaozhan.security.annotation.RequireRole;
import com.xiaozhan.service.CandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 学生端：收到的企业邀约
 */
@Tag(name = "08-邀约（学生端）", description = "学生查看与回应企业邀约")
@RestController
@RequestMapping("/api/invitation")
@RequiredArgsConstructor
public class InvitationController extends BaseController {

    private final CandidateService candidateService;

    @RequireRole(RequireRole.Role.STUDENT)
    @Operation(summary = "我收到的邀约")
    @GetMapping("/received")
    public Result<IPage<Map<String, Object>>> received(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(candidateService.receivedInvitations(pageNum, pageSize));
    }

    @RequireRole(RequireRole.Role.STUDENT)
    @Operation(summary = "回应邀约", description = "accept=true 同意，false 拒绝；同意时可回传联系方式")
    @PostMapping("/{id}/reply")
    public Result<Void> reply(@PathVariable Long id,
                              @RequestParam boolean accept,
                              @RequestParam(required = false) String contactInfo) {
        candidateService.replyInvitation(id, accept, contactInfo);
        return Result.success();
    }
}
