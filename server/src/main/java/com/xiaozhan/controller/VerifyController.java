package com.xiaozhan.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaozhan.common.Result;
import com.xiaozhan.dto.CompanyVerifyDTO;
import com.xiaozhan.dto.EduVerifyDTO;
import com.xiaozhan.dto.ExpertVerifyDTO;
import com.xiaozhan.dto.VerifyAuditDTO;
import com.xiaozhan.security.annotation.RequireRole;
import com.xiaozhan.service.VerifyService;
import com.xiaozhan.vo.VerifyRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证接口
 */
@Tag(name = "03-认证审核", description = "学籍认证、专家认证、企业认证、管理员审核")
@RestController
@RequestMapping("/api/verify")
@RequiredArgsConstructor
public class VerifyController extends BaseController {

    private final VerifyService verifyService;

    @RequireRole(RequireRole.Role.STUDENT)
    @Operation(summary = "学生提交学籍认证")
    @PostMapping("/student")
    public Result<Void> submitEduVerify(@Valid @RequestBody EduVerifyDTO dto) {
        verifyService.submitEduVerify(dto);
        return Result.success();
    }

    @RequireRole(RequireRole.Role.EXPERT)
    @Operation(summary = "专家提交认证")
    @PostMapping("/expert")
    public Result<Void> submitExpertVerify(@Valid @RequestBody ExpertVerifyDTO dto) {
        verifyService.submitExpertVerify(dto);
        return Result.success();
    }

    @RequireRole(RequireRole.Role.COMPANY)
    @Operation(summary = "企业提交认证")
    @PostMapping("/company")
    public Result<Void> submitCompanyVerify(@Valid @RequestBody CompanyVerifyDTO dto) {
        verifyService.submitCompanyVerify(dto);
        return Result.success();
    }

    @Operation(summary = "我的认证记录")
    @GetMapping("/my")
    public Result<List<VerifyRecordVO>> myRecords() {
        return Result.success(verifyService.myRecords());
    }

    @RequireRole(RequireRole.Role.ADMIN)
    @Operation(summary = "管理员：认证审核列表")
    @GetMapping("/page")
    public Result<IPage<VerifyRecordVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String bizType,
            @RequestParam(required = false) Integer status) {
        return Result.success(verifyService.pageRecords(pageNum, pageSize, bizType, status));
    }

    @RequireRole(RequireRole.Role.ADMIN)
    @Operation(summary = "管理员：审核认证")
    @PostMapping("/audit")
    public Result<Void> audit(@Valid @RequestBody VerifyAuditDTO dto) {
        verifyService.audit(dto);
        return Result.success();
    }
}
