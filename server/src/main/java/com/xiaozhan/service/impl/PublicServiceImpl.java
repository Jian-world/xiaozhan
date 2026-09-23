package com.xiaozhan.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaozhan.common.ResultCode;
import com.xiaozhan.entity.ExpertProfile;
import com.xiaozhan.entity.Project;
import com.xiaozhan.entity.StudentProfile;
import com.xiaozhan.entity.SysUser;
import com.xiaozhan.exception.BizException;
import com.xiaozhan.mapper.ExpertProfileMapper;
import com.xiaozhan.mapper.ProjectMapper;
import com.xiaozhan.mapper.StudentProfileMapper;
import com.xiaozhan.mapper.SysUserMapper;
import com.xiaozhan.service.PublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 公开信息查询实现
 */
@Service
@RequiredArgsConstructor
public class PublicServiceImpl implements PublicService {

    private final StudentProfileMapper studentProfileMapper;

    private final SysUserMapper sysUserMapper;

    private final ExpertProfileMapper expertProfileMapper;

    private final ProjectMapper projectMapper;

    @Override
    public Map<String, Object> studentPublicProfile(Long studentId) {
        SysUser user = sysUserMapper.selectById(studentId);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        StudentProfile profile = studentProfileMapper.selectOne(
                Wrappers.<StudentProfile>lambdaQuery().eq(StudentProfile::getUserId, studentId));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("studentId", studentId);
        // 未开放检索时只暴露昵称，保护隐私
        boolean open = profile != null && profile.getAllowCompanySearch() != null
                && profile.getAllowCompanySearch() == 1;
        result.put("nickname", StrUtil.blankToDefault(user.getNickname(), user.getUsername()));
        result.put("realName", open ? user.getRealName() : null);
        result.put("avatar", user.getAvatar());

        if (profile != null) {
            result.put("school", profile.getSchool());
            result.put("major", profile.getMajor());
            result.put("majorCategory", profile.getMajorCategory());
            result.put("degree", profile.getDegree());
            result.put("graduateYear", profile.getGraduateYear());
            result.put("eduVerified", profile.getEduVerified());
            result.put("skillTags", splitTags(profile.getSkillTags()));
            result.put("bio", profile.getBio());
            result.put("portfolioViews", profile.getPortfolioViews());
        } else {
            result.put("eduVerified", 0);
            result.put("skillTags", Collections.emptyList());
            result.put("portfolioViews", 0);
        }

        // 项目统计
        List<Project> projects = projectMapper.selectList(Wrappers.<Project>lambdaQuery()
                .eq(Project::getStudentId, studentId)
                .eq(Project::getPublishStatus, Project.PUBLISH_DONE)
                .ne(Project::getVisibility, Project.VIS_PRIVATE));
        result.put("projectCount", projects.size());

        int totalReviews = projects.stream()
                .mapToInt(p -> p.getExpertReviewCount() == null ? 0 : p.getExpertReviewCount()).sum();
        result.put("totalReviews", totalReviews);

        List<BigDecimal> scores = projects.stream()
                .map(Project::getExpertAvgScore)
                .filter(Objects::nonNull)
                .filter(s -> s.compareTo(BigDecimal.ZERO) > 0)
                .toList();
        result.put("avgScore", scores.isEmpty() ? null
                : scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP));

        return result;
    }

    @Override
    public Map<String, Object> expertHall(Integer pageNum, Integer pageSize, String expertType) {
        Page<ExpertProfile> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 12 : pageSize);
        var result = expertProfileMapper.selectPage(page,
                Wrappers.<ExpertProfile>lambdaQuery()
                        .eq(ExpertProfile::getVerifyStatus, ExpertProfile.STATUS_PASSED)
                        .eq(StrUtil.isNotBlank(expertType), ExpertProfile::getExpertType, expertType)
                        .orderByDesc(ExpertProfile::getTotalReviews)
                        .orderByDesc(ExpertProfile::getThanksCount));

        List<Map<String, Object>> records = new ArrayList<>();
        for (ExpertProfile p : result.getRecords()) {
            SysUser user = sysUserMapper.selectById(p.getUserId());
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("expertUserId", p.getUserId());
            map.put("expertId", p.getId());
            map.put("name", user == null ? null
                    : StrUtil.blankToDefault(user.getRealName(), user.getNickname()));
            map.put("avatar", user == null ? null : user.getAvatar());
            boolean showOrg = p.getShowOrg() == null || p.getShowOrg() == 1;
            map.put("orgName", showOrg ? p.getOrgName() : null);
            map.put("position", showOrg ? p.getPosition() : null);
            map.put("expertType", p.getExpertType());
            map.put("domainTags", splitTags(p.getDomain()));
            map.put("totalReviews", p.getTotalReviews());
            map.put("thanksCount", p.getThanksCount());
            map.put("qualityScore", p.getQualityScore());
            map.put("level", p.getLevel());
            records.add(map);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("records", records);
        response.put("total", result.getTotal());
        response.put("current", result.getCurrent());
        response.put("size", result.getSize());
        response.put("pages", result.getPages());
        return response;
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
}
