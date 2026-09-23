package com.xiaozhan.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaozhan.common.Result;
import com.xiaozhan.dto.ReviewAppealDTO;
import com.xiaozhan.dto.ReviewInviteDTO;
import com.xiaozhan.dto.ReviewReplyDTO;
import com.xiaozhan.dto.ReviewSubmitDTO;
import com.xiaozhan.security.annotation.IgnoreAuth;
import com.xiaozhan.security.annotation.RequireRole;
import com.xiaozhan.service.ReviewService;
import com.xiaozhan.vo.ReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 点评接口
 */
@Tag(name = "06-专家点评", description = "提交点评、点评列表、致谢、回应、申诉")
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController extends BaseController {

    private final ReviewService reviewService;

    /* ---- 专家 ---- */

    @RequireRole(RequireRole.Role.EXPERT)
    @Operation(summary = "提交点评",
            description = "需完成专家认证；评语至少 50 字；同一项目 30 天内仅可点评一次")
    @PostMapping
    public Result<Long> submit(@Valid @RequestBody ReviewSubmitDTO dto) {
        return Result.success(reviewService.submit(dto));
    }

    @RequireRole(RequireRole.Role.EXPERT)
    @Operation(summary = "我发出的点评")
    @GetMapping("/my")
    public Result<IPage<ReviewVO>> myReviews(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(reviewService.myReviews(pageNum, pageSize));
    }

    @RequireRole(RequireRole.Role.EXPERT)
    @Operation(summary = "专家工作台统计", description = "今日点评数、额度、累计点评数、给予均分、等级、积分")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> expertStatistics() {
        return Result.success(reviewService.expertStatistics());
    }

    /* ---- 学生 ---- */

    @RequireRole(RequireRole.Role.STUDENT)
    @Operation(summary = "我收到的点评")
    @GetMapping("/received")
    public Result<IPage<ReviewVO>> received(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(reviewService.receivedByMe(pageNum, pageSize));
    }

    @RequireRole(RequireRole.Role.STUDENT)
    @Operation(summary = "致谢点评", description = "需要已收到该点评")
    @PostMapping("/{id}/thanks")
    public Result<Void> thanks(@PathVariable Long id) {
        reviewService.thanks(id);
        return Result.success();
    }

    @RequireRole(RequireRole.Role.STUDENT)
    @Operation(summary = "对点评发起申诉")
    @PostMapping("/appeal")
    public Result<Long> appeal(@Valid @RequestBody ReviewAppealDTO dto) {
        return Result.success(reviewService.appeal(dto));
    }

    @RequireRole(RequireRole.Role.STUDENT)
    @Operation(summary = "邀请专家定向点评")
    @PostMapping("/invite")
    public Result<Void> invite(@RequestBody ReviewInviteDTO dto) {
        reviewService.invite(dto);
        return Result.success();
    }

    /* ---- 通用 ---- */

    @IgnoreAuth
    @Operation(summary = "项目下的公开点评")
    @GetMapping("/project/{projectId}")
    public Result<List<ReviewVO>> listByProject(@PathVariable Long projectId) {
        return Result.success(reviewService.listByProject(projectId));
    }

    @IgnoreAuth
    @Operation(summary = "点评详情")
    @GetMapping("/{id}")
    public Result<ReviewVO> detail(@PathVariable Long id) {
        return Result.success(reviewService.detail(id));
    }

    @Operation(summary = "回应点评", description = "学生与专家均可回应")
    @PostMapping("/reply")
    public Result<Long> reply(@Valid @RequestBody ReviewReplyDTO dto) {
        return Result.success(reviewService.reply(dto));
    }

    /* ---- 管理员 ---- */

    @RequireRole(RequireRole.Role.ADMIN)
    @Operation(summary = "管理员：申诉列表")
    @GetMapping("/appeal/page")
    public Result<IPage<Map<String, Object>>> appealPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.success(reviewService.appealPage(pageNum, pageSize, status));
    }

    @RequireRole(RequireRole.Role.ADMIN)
    @Operation(summary = "管理员：处理申诉", description = "status 可选 ACCEPT / REJECT")
    @PostMapping("/appeal/{id}/handle")
    public Result<Void> handleAppeal(@PathVariable Long id,
                                     @RequestParam String status,
                                     @RequestParam(required = false) String remark) {
        reviewService.handleAppeal(id, status, remark);
        return Result.success();
    }
}
