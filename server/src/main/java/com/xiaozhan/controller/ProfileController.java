package com.xiaozhan.controller;

import com.xiaozhan.common.Result;
import com.xiaozhan.dto.CompanyProfileDTO;
import com.xiaozhan.dto.ExpertProfileDTO;
import com.xiaozhan.dto.StudentProfileDTO;
import com.xiaozhan.dto.UserBaseDTO;
import com.xiaozhan.security.annotation.RequireRole;
import com.xiaozhan.service.ProfileService;
import com.xiaozhan.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 个人档案接口
 */
@Tag(name = "04-档案", description = "基础资料、学生档案、技能标签、专家档案、企业档案")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController extends BaseController {

    private final ProfileService profileService;

    @Operation(summary = "更新基础资料", description = "昵称、头像、真实姓名、联系方式")
    @PutMapping("/base")
    public Result<UserInfoVO> updateBase(@RequestBody UserBaseDTO dto) {
        return Result.success(profileService.updateBase(dto));
    }

    @RequireRole(RequireRole.Role.STUDENT)
    @Operation(summary = "更新学生档案", description = "含技能标签、一句话自述、是否允许企业检索")
    @PutMapping("/student")
    public Result<UserInfoVO> updateStudent(@RequestBody StudentProfileDTO dto) {
        return Result.success(profileService.updateStudentProfile(dto));
    }

    @RequireRole(RequireRole.Role.EXPERT)
    @Operation(summary = "更新专家档案", description = "擅长领域、是否公开单位信息")
    @PutMapping("/expert")
    public Result<UserInfoVO> updateExpert(@RequestBody ExpertProfileDTO dto) {
        return Result.success(profileService.updateExpertProfile(dto));
    }

    @RequireRole(RequireRole.Role.COMPANY)
    @Operation(summary = "更新企业档案", description = "企业名称、所属行业、企业规模")
    @PutMapping("/company")
    public Result<UserInfoVO> updateCompany(@RequestBody CompanyProfileDTO dto) {
        return Result.success(profileService.updateCompanyProfile(dto));
    }
}
