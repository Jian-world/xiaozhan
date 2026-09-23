package com.xiaozhan.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaozhan.common.ResultCode;
import com.xiaozhan.entity.*;
import com.xiaozhan.exception.BizException;
import com.xiaozhan.mapper.*;
import com.xiaozhan.security.UserContext;
import com.xiaozhan.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 平台运营服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final SysUserMapper sysUserMapper;

    private final StudentProfileMapper studentProfileMapper;

    private final ExpertProfileMapper expertProfileMapper;

    private final CompanyProfileMapper companyProfileMapper;

    private final ProjectMapper projectMapper;

    private final ExpertReviewMapper expertReviewMapper;

    private final ContentAuditMapper contentAuditMapper;

    private final SysDictMapper sysDictMapper;

    private final TrackEventMapper trackEventMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Map<String, Object> overview() {
        Map<String, Object> result = new LinkedHashMap<>();

        long students = sysUserMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getRole, "STUDENT"));
        long experts = sysUserMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getRole, "EXPERT"));
        long companies = sysUserMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getRole, "COMPANY"));
        long totalProjects = projectMapper.selectCount(null);
        long publishedProjects = projectMapper.selectCount(Wrappers.<Project>lambdaQuery()
                .eq(Project::getPublishStatus, Project.PUBLISH_DONE));
        long reviews = expertReviewMapper.selectCount(null);
        long verifiedStudents = studentProfileMapper.selectCount(Wrappers.<StudentProfile>lambdaQuery()
                .eq(StudentProfile::getEduVerified, StudentProfile.VERIFY_PASSED));
        long pendingAudits = contentAuditMapper.selectCount(Wrappers.<ContentAudit>lambdaQuery()
                .eq(ContentAudit::getStatus, ContentAudit.STATUS_PENDING));

        long poolProjects = projectMapper.selectCount(Wrappers.<Project>lambdaQuery()
                .eq(Project::getInReviewPool, 1));

        result.put("studentCount", students);
        result.put("expertCount", experts);
        result.put("companyCount", companies);
        result.put("totalProjects", totalProjects);
        result.put("publishedProjects", publishedProjects);
        result.put("reviewCount", reviews);
        result.put("verifiedStudentCount", verifiedStudents);
        result.put("pendingAuditCount", pendingAudits);
        result.put("poolProjectCount", poolProjects);
        result.put("verifyRate", students == 0 ? "0%"
                : Math.round(verifiedStudents * 1000.0 / students) / 10.0 + "%");

        // 专业大类分布
        List<StudentProfile> profiles = studentProfileMapper.selectList(null);
        Map<String, Long> majorDist = new LinkedHashMap<>();
        for (StudentProfile p : profiles) {
            String key = StrUtil.blankToDefault(p.getMajorCategory(), "OTHER");
            majorDist.merge(key, 1L, Long::sum);
        }
        result.put("majorDistribution", majorDist);

        // 项目类型分布
        Map<String, Long> typeDist = new LinkedHashMap<>();
        for (Project p : projectMapper.selectList(null)) {
            String key = StrUtil.blankToDefault(p.getProjectType(), "OTHER");
            typeDist.merge(key, 1L, Long::sum);
        }
        result.put("projectTypeDistribution", typeDist);

        return result;
    }

    @Override
    public IPage<Map<String, Object>> users(Integer pageNum, Integer pageSize,
                                            String role, Integer status, String keyword) {
        Page<SysUser> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        IPage<SysUser> result = sysUserMapper.selectPage(page,
                Wrappers.<SysUser>lambdaQuery()
                        .eq(StrUtil.isNotBlank(role), SysUser::getRole, role)
                        .eq(status != null, SysUser::getStatus, status)
                        .and(StrUtil.isNotBlank(keyword), w -> w
                                .like(SysUser::getUsername, keyword)
                                .or().like(SysUser::getNickname, keyword)
                                .or().like(SysUser::getRealName, keyword))
                        .orderByDesc(SysUser::getCreateTime));
        return result.convert(this::toUserMap);
    }

    private Map<String, Object> toUserMap(SysUser user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("role", user.getRole());
        map.put("nickname", user.getNickname());
        map.put("realName", user.getRealName());
        map.put("avatar", user.getAvatar());
        map.put("phone", user.getPhone());
        map.put("email", user.getEmail());
        map.put("status", user.getStatus());
        map.put("lastLoginAt", user.getLastLoginAt());
        map.put("createTime", user.getCreateTime());

        switch (user.getRole()) {
            case "STUDENT" -> {
                StudentProfile p = studentProfileMapper.selectOne(Wrappers.<StudentProfile>lambdaQuery()
                        .eq(StudentProfile::getUserId, user.getId()));
                if (p != null) {
                    map.put("orgInfo", p.getSchool() + " · " + p.getMajor());
                    map.put("verifyStatus", p.getEduVerified());
                }
            }
            case "EXPERT" -> {
                ExpertProfile p = expertProfileMapper.selectOne(Wrappers.<ExpertProfile>lambdaQuery()
                        .eq(ExpertProfile::getUserId, user.getId()));
                if (p != null) {
                    map.put("orgInfo", p.getOrgName() + " · " + p.getPosition());
                    map.put("verifyStatus", p.getVerifyStatus());
                }
            }
            case "COMPANY" -> {
                CompanyProfile p = companyProfileMapper.selectOne(Wrappers.<CompanyProfile>lambdaQuery()
                        .eq(CompanyProfile::getUserId, user.getId()));
                if (p != null) {
                    map.put("orgInfo", p.getCompanyName() + " · " + p.getIndustry());
                    map.put("verifyStatus", p.getVerifyStatus());
                    map.put("packageType", p.getPackageType());
                }
            }
            default -> {
            }
        }
        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleUserStatus(Long userId, boolean enable) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setStatus(enable ? 1 : 0);
        sysUserMapper.updateById(update);
        log.info("管理员{}用户：userId={}", enable ? "启用" : "禁用", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String resetPassword(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        String newPassword = IdUtil.fastSimpleUUID().substring(0, 10);
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(newPassword));
        sysUserMapper.updateById(update);
        log.info("管理员重置用户密码：userId={}", userId);
        return newPassword;
    }

    @Override
    public IPage<Map<String, Object>> contentAudits(Integer pageNum, Integer pageSize, Integer status) {
        Page<ContentAudit> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        IPage<ContentAudit> result = contentAuditMapper.selectPage(page,
                Wrappers.<ContentAudit>lambdaQuery()
                        .eq(status != null, ContentAudit::getStatus, status)
                        .orderByAsc(ContentAudit::getStatus)
                        .orderByDesc(ContentAudit::getCreateTime));

        return result.convert(audit -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", audit.getId());
            map.put("targetType", audit.getTargetType());
            map.put("targetId", audit.getTargetId());
            map.put("snapshot", audit.getSnapshot());
            map.put("autoResult", audit.getAutoResult());
            map.put("status", audit.getStatus());
            map.put("remark", audit.getRemark());
            map.put("createTime", audit.getCreateTime());

            if ("PROJECT".equals(audit.getTargetType())) {
                Project project = projectMapper.selectById(audit.getTargetId());
                if (project != null) {
                    map.put("title", project.getName());
                    map.put("summary", project.getSummary());
                    map.put("coverUrl", project.getCoverUrl());
                    SysUser student = sysUserMapper.selectById(project.getStudentId());
                    map.put("authorName", student == null ? null
                            : StrUtil.blankToDefault(student.getNickname(), student.getUsername()));
                }
            }
            return map;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAudit(Long auditId, boolean pass, String remark) {
        ContentAudit audit = contentAuditMapper.selectById(auditId);
        if (audit == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "审核记录不存在");
        }
        ContentAudit update = new ContentAudit();
        update.setId(auditId);
        update.setStatus(pass ? ContentAudit.STATUS_PASSED : ContentAudit.STATUS_OFFLINE);
        update.setRemark(remark);
        update.setHandlerId(UserContext.getUserId());
        update.setHandleTime(LocalDateTime.now());
        contentAuditMapper.updateById(update);

        if ("PROJECT".equals(audit.getTargetType())) {
            Project projectUpdate = new Project();
            projectUpdate.setId(audit.getTargetId());
            projectUpdate.setReviewStatus(pass ? Project.AUDIT_PASSED : Project.AUDIT_REJECTED);
            if (!pass) {
                projectUpdate.setPublishStatus(Project.PUBLISH_DRAFT);
            }
            projectMapper.updateById(projectUpdate);
        }
        log.info("内容审核完成：auditId={}, pass={}", auditId, pass);
    }

    @Override
    public IPage<com.xiaozhan.vo.ProjectVO> projects(Integer pageNum, Integer pageSize, Integer reviewStatus) {
        Page<Project> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        IPage<Project> result = projectMapper.selectPage(page,
                Wrappers.<Project>lambdaQuery()
                        .eq(reviewStatus != null, Project::getReviewStatus, reviewStatus)
                        .orderByDesc(Project::getUpdateTime));
        return result.convert(p -> {
            com.xiaozhan.vo.ProjectVO vo = new com.xiaozhan.vo.ProjectVO();
            vo.setId(p.getId());
            vo.setName(p.getName());
            vo.setStudentId(p.getStudentId());
            vo.setCategory(p.getCategory());
            vo.setProjectType(p.getProjectType());
            vo.setSummary(p.getSummary());
            vo.setCoverUrl(p.getCoverUrl());
            vo.setPublishStatus(p.getPublishStatus());
            vo.setReviewStatus(p.getReviewStatus());
            vo.setExpertAvgScore(p.getExpertAvgScore());
            vo.setExpertReviewCount(p.getExpertReviewCount());
            vo.setViewCount(p.getViewCount());
            vo.setCreateTime(p.getCreateTime());
            vo.setUpdateTime(p.getUpdateTime());
            SysUser student = sysUserMapper.selectById(p.getStudentId());
            if (student != null) {
                vo.setStudentName(StrUtil.blankToDefault(student.getNickname(), student.getUsername()));
                vo.setStudentAvatar(student.getAvatar());
            }
            return vo;
        });
    }

    @Override
    public List<Map<String, Object>> dict(String dictType) {
        List<SysDict> dicts = sysDictMapper.selectList(Wrappers.<SysDict>lambdaQuery()
                .eq(StrUtil.isNotBlank(dictType), SysDict::getDictType, dictType)
                .orderByAsc(SysDict::getDictType)
                .orderByAsc(SysDict::getSortOrder));
        return dicts.stream().map(d -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", d.getId());
            map.put("dictType", d.getDictType());
            map.put("dictKey", d.getDictKey());
            map.put("dictLabel", d.getDictLabel());
            map.put("sortOrder", d.getSortOrder());
            return map;
        }).toList();
    }

    @Override
    public IPage<Map<String, Object>> trackEvents(Integer pageNum, Integer pageSize, String eventCode) {
        Page<TrackEvent> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 20 : pageSize);
        IPage<TrackEvent> result = trackEventMapper.selectPage(page,
                Wrappers.<TrackEvent>lambdaQuery()
                        .eq(StrUtil.isNotBlank(eventCode), TrackEvent::getEventCode, eventCode)
                        .orderByDesc(TrackEvent::getCreateTime));
        return result.convert(e -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", e.getId());
            map.put("eventCode", e.getEventCode());
            map.put("userId", e.getUserId());
            map.put("role", e.getRole());
            map.put("major", e.getMajor());
            map.put("projectType", e.getProjectType());
            map.put("extra", e.getExtra());
            map.put("createTime", e.getCreateTime());
            return map;
        });
    }
}
