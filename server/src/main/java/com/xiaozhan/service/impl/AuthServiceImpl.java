package com.xiaozhan.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiaozhan.common.ResultCode;
import com.xiaozhan.config.XiaozhanProperties;
import com.xiaozhan.dto.ChangePasswordDTO;
import com.xiaozhan.dto.LoginDTO;
import com.xiaozhan.dto.RegisterDTO;
import com.xiaozhan.entity.*;
import com.xiaozhan.exception.BizException;
import com.xiaozhan.mapper.*;
import com.xiaozhan.security.UserContext;
import com.xiaozhan.service.AuthService;
import com.xiaozhan.util.JwtUtil;
import com.xiaozhan.vo.LoginVO;
import com.xiaozhan.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;

    private final StudentProfileMapper studentProfileMapper;

    private final ExpertProfileMapper expertProfileMapper;

    private final CompanyProfileMapper companyProfileMapper;

    private final XiaozhanProperties properties;

    private final JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /* ==================== 注册 ==================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterDTO dto) {
        String role = dto.getRole().toUpperCase();

        Long exists = sysUserMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, dto.getUsername()));
        if (exists != null && exists > 0) {
            throw new BizException(ResultCode.USERNAME_EXISTS);
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(role);
        user.setNickname(StrUtil.blankToDefault(dto.getNickname(), dto.getUsername()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(1);
        sysUserMapper.insert(user);

        // 按角色补建档案
        switch (role) {
            case Role.STUDENT -> createStudentProfile(user.getId(), dto);
            case Role.EXPERT -> createExpertProfile(user.getId(), dto);
            case Role.COMPANY -> createCompanyProfile(user.getId(), dto);
            default -> throw new BizException(ResultCode.PARAM_ERROR.getCode(), "不支持注册该角色");
        }

        log.info("新用户注册成功：id={}, username={}, role={}", user.getId(), user.getUsername(), role);
        return buildLoginVO(user);
    }

    private void createStudentProfile(Long userId, RegisterDTO dto) {
        StudentProfile profile = new StudentProfile();
        profile.setUserId(userId);
        profile.setSchool(dto.getSchool());
        profile.setMajor(dto.getMajor());
        profile.setMajorCategory(StrUtil.blankToDefault(dto.getMajorCategory(), "OTHER"));
        profile.setDegree(dto.getDegree());
        profile.setGraduateYear(dto.getGraduateYear());
        profile.setEduVerified(StudentProfile.VERIFY_NONE);
        profile.setSkillTags("");
        profile.setAllowCompanySearch(1);
        profile.setPortfolioViews(0);
        studentProfileMapper.insert(profile);
    }

    private void createExpertProfile(Long userId, RegisterDTO dto) {
        ExpertProfile profile = new ExpertProfile();
        profile.setUserId(userId);
        profile.setOrgName(dto.getOrgName());
        profile.setPosition(dto.getPosition());
        profile.setExpertType("ENGINEER");
        profile.setDomain("");
        profile.setVerifyStatus(ExpertProfile.STATUS_NONE);
        profile.setShowOrg(1);
        profile.setDailyQuota(properties.getReview().getExpertDailyLimit());
        profile.setTotalReviews(0);
        profile.setThanksCount(0);
        profile.setLevel("BRONZE");
        profile.setPoints(0);
        expertProfileMapper.insert(profile);
    }

    private void createCompanyProfile(Long userId, RegisterDTO dto) {
        CompanyProfile profile = new CompanyProfile();
        profile.setUserId(userId);
        profile.setCompanyName(dto.getCompanyName());
        profile.setIndustry(dto.getIndustry());
        profile.setVerifyStatus(CompanyProfile.STATUS_NONE);
        profile.setPackageType(CompanyProfile.PKG_FREE);
        profile.setMonthQuota(properties.getCompany().getFreeMonthQuota());
        profile.setMonthUsed(0);
        profile.setQuotaResetAt(LocalDate.now().plusMonths(1).withDayOfMonth(1));
        companyProfileMapper.insert(profile);
    }

    /* ==================== 登录 ==================== */

    @Override
    public LoginVO login(LoginDTO dto) {
        SysUser user = sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BizException(ResultCode.PASSWORD_ERROR);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException(ResultCode.PASSWORD_ERROR);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(ResultCode.USER_DISABLED);
        }
        // 身份校验：避免用学生身份登录企业账号
        if (!user.getRole().equalsIgnoreCase(dto.getRole())) {
            throw new BizException(ResultCode.PASSWORD_ERROR);
        }

        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setLastLoginAt(LocalDateTime.now());
        sysUserMapper.updateById(update);
        user.setLastLoginAt(update.getLastLoginAt());

        log.info("用户登录：id={}, username={}, role={}", user.getId(), user.getUsername(), user.getRole());
        return buildLoginVO(user);
    }

    private LoginVO buildLoginVO(SysUser user) {
        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole()));
        vo.setExpire(properties.getJwt().getExpire());
        vo.setUser(toUserInfo(user));
        return vo;
    }

    /* ==================== 当前用户 ==================== */

    @Override
    public UserInfoVO currentUser() {
        Long userId = UserContext.getUserId();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        return toUserInfo(user);
    }

    private UserInfoVO toUserInfo(SysUser user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        vo.setNickname(user.getNickname());
        vo.setRealName(user.getRealName());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setLastLoginAt(user.getLastLoginAt());

        switch (user.getRole()) {
            case Role.STUDENT -> {
                StudentProfile p = studentProfileMapper.selectOne(
                        Wrappers.<StudentProfile>lambdaQuery().eq(StudentProfile::getUserId, user.getId()));
                if (p != null) {
                    vo.setStudentProfile(toStudentVO(p));
                }
            }
            case Role.EXPERT -> {
                ExpertProfile p = expertProfileMapper.selectOne(
                        Wrappers.<ExpertProfile>lambdaQuery().eq(ExpertProfile::getUserId, user.getId()));
                if (p != null) {
                    vo.setExpertProfile(toExpertVO(p));
                }
            }
            case Role.COMPANY -> {
                CompanyProfile p = companyProfileMapper.selectOne(
                        Wrappers.<CompanyProfile>lambdaQuery().eq(CompanyProfile::getUserId, user.getId()));
                if (p != null) {
                    vo.setCompanyProfile(toCompanyVO(p));
                }
            }
            default -> {
                // ADMIN 无扩展档案
            }
        }
        return vo;
    }

    private UserInfoVO.StudentProfileVO toStudentVO(StudentProfile p) {
        UserInfoVO.StudentProfileVO vo = new UserInfoVO.StudentProfileVO();
        vo.setSchool(p.getSchool());
        vo.setMajor(p.getMajor());
        vo.setMajorCategory(p.getMajorCategory());
        vo.setDegree(p.getDegree());
        vo.setGraduateYear(p.getGraduateYear());
        vo.setEduVerified(p.getEduVerified());
        vo.setEduVerifyRemark(p.getEduVerifyRemark());
        vo.setSkillTags(splitTags(p.getSkillTags()));
        vo.setBio(p.getBio());
        vo.setAllowCompanySearch(p.getAllowCompanySearch());
        vo.setPortfolioViews(p.getPortfolioViews());
        return vo;
    }

    private UserInfoVO.ExpertProfileVO toExpertVO(ExpertProfile p) {
        UserInfoVO.ExpertProfileVO vo = new UserInfoVO.ExpertProfileVO();
        vo.setOrgName(p.getOrgName());
        vo.setPosition(p.getPosition());
        vo.setExpertType(p.getExpertType());
        vo.setDomainTags(splitTags(p.getDomain()));
        vo.setShowOrg(p.getShowOrg());
        vo.setVerifyStatus(p.getVerifyStatus());
        vo.setVerifyRemark(p.getVerifyRemark());
        vo.setDailyQuota(p.getDailyQuota());
        vo.setTotalReviews(p.getTotalReviews());
        vo.setThanksCount(p.getThanksCount());
        vo.setQualityScore(p.getQualityScore() == null ? null : p.getQualityScore().doubleValue());
        vo.setLevel(p.getLevel());
        vo.setPoints(p.getPoints());
        return vo;
    }

    private UserInfoVO.CompanyProfileVO toCompanyVO(CompanyProfile p) {
        UserInfoVO.CompanyProfileVO vo = new UserInfoVO.CompanyProfileVO();
        vo.setCompanyName(p.getCompanyName());
        vo.setIndustry(p.getIndustry());
        vo.setScale(p.getScale());
        vo.setVerifyStatus(p.getVerifyStatus());
        vo.setVerifyRemark(p.getVerifyRemark());
        vo.setPackageType(p.getPackageType());
        vo.setRemainQuota(p.remainQuota());
        vo.setPackageExpire(p.getPackageExpire());
        return vo;
    }

    private List<String> splitTags(String tags) {
        if (StrUtil.isBlank(tags)) {
            return Collections.emptyList();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .toList();
    }

    /* ==================== 修改密码 ==================== */

    @Override
    public void changePassword(ChangePasswordDTO dto) {
        Long userId = UserContext.getUserId();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BizException(ResultCode.OLD_PASSWORD_ERROR);
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        sysUserMapper.updateById(update);
        log.info("用户修改密码：id={}", userId);
    }

    @Override
    public void logout() {
        // JWT 无状态，前端清除本地 token 即可；此处预留接入 Redis 黑名单
        log.info("用户退出登录：id={}", UserContext.getUserId());
    }

    /**
     * 角色常量
     */
    public interface Role {
        String STUDENT = "STUDENT";
        String EXPERT = "EXPERT";
        String COMPANY = "COMPANY";
        String ADMIN = "ADMIN";
    }
}
