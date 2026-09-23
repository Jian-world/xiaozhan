package com.xiaozhan.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiaozhan.common.ResultCode;
import com.xiaozhan.dto.CompanyProfileDTO;
import com.xiaozhan.dto.ExpertProfileDTO;
import com.xiaozhan.dto.StudentProfileDTO;
import com.xiaozhan.dto.UserBaseDTO;
import com.xiaozhan.entity.CompanyProfile;
import com.xiaozhan.entity.ExpertProfile;
import com.xiaozhan.entity.StudentProfile;
import com.xiaozhan.entity.SysUser;
import com.xiaozhan.exception.BizException;
import com.xiaozhan.mapper.CompanyProfileMapper;
import com.xiaozhan.mapper.ExpertProfileMapper;
import com.xiaozhan.mapper.StudentProfileMapper;
import com.xiaozhan.mapper.SysUserMapper;
import com.xiaozhan.security.UserContext;
import com.xiaozhan.service.AuthService;
import com.xiaozhan.service.ProfileService;
import com.xiaozhan.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 个人档案服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final SysUserMapper sysUserMapper;

    private final StudentProfileMapper studentProfileMapper;

    private final ExpertProfileMapper expertProfileMapper;

    private final CompanyProfileMapper companyProfileMapper;

    private final AuthService authService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoVO updateBase(UserBaseDTO dto) {
        Long userId = UserContext.getUserId();
        SysUser update = new SysUser();
        update.setId(userId);
        update.setNickname(dto.getNickname());
        update.setRealName(dto.getRealName());
        update.setAvatar(dto.getAvatar());
        update.setPhone(dto.getPhone());
        update.setEmail(dto.getEmail());
        sysUserMapper.updateById(update);
        return authService.currentUser();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoVO updateStudentProfile(StudentProfileDTO dto) {
        Long userId = UserContext.getUserId();
        StudentProfile profile = studentProfileMapper.selectOne(
                Wrappers.<StudentProfile>lambdaQuery().eq(StudentProfile::getUserId, userId));
        if (profile == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND.getCode(), "学生档案不存在");
        }
        if (StrUtil.isNotBlank(dto.getSchool())) {
            profile.setSchool(dto.getSchool());
        }
        if (StrUtil.isNotBlank(dto.getMajor())) {
            profile.setMajor(dto.getMajor());
        }
        if (StrUtil.isNotBlank(dto.getMajorCategory())) {
            profile.setMajorCategory(dto.getMajorCategory());
        }
        if (StrUtil.isNotBlank(dto.getDegree())) {
            profile.setDegree(dto.getDegree());
        }
        if (dto.getGraduateYear() != null) {
            profile.setGraduateYear(dto.getGraduateYear());
        }
        if (dto.getSkillTags() != null) {
            profile.setSkillTags(String.join(",", dto.getSkillTags()));
        }
        if (dto.getBio() != null) {
            profile.setBio(dto.getBio());
        }
        if (dto.getAllowCompanySearch() != null) {
            profile.setAllowCompanySearch(dto.getAllowCompanySearch());
        }
        studentProfileMapper.updateById(profile);
        return authService.currentUser();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoVO updateExpertProfile(ExpertProfileDTO dto) {
        Long userId = UserContext.getUserId();
        ExpertProfile profile = expertProfileMapper.selectOne(
                Wrappers.<ExpertProfile>lambdaQuery().eq(ExpertProfile::getUserId, userId));
        if (profile == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND.getCode(), "专家档案不存在");
        }
        if (dto.getDomainTags() != null) {
            profile.setDomain(String.join(",", dto.getDomainTags()));
        }
        if (dto.getShowOrg() != null) {
            profile.setShowOrg(dto.getShowOrg());
        }
        expertProfileMapper.updateById(profile);
        return authService.currentUser();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoVO updateCompanyProfile(CompanyProfileDTO dto) {
        Long userId = UserContext.getUserId();
        CompanyProfile profile = companyProfileMapper.selectOne(
                Wrappers.<CompanyProfile>lambdaQuery().eq(CompanyProfile::getUserId, userId));
        if (profile == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND.getCode(), "企业档案不存在");
        }
        if (StrUtil.isNotBlank(dto.getCompanyName())) {
            profile.setCompanyName(dto.getCompanyName());
        }
        if (StrUtil.isNotBlank(dto.getIndustry())) {
            profile.setIndustry(dto.getIndustry());
        }
        if (StrUtil.isNotBlank(dto.getScale())) {
            profile.setScale(dto.getScale());
        }
        companyProfileMapper.updateById(profile);
        return authService.currentUser();
    }
}
