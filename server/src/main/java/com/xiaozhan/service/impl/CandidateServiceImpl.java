package com.xiaozhan.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaozhan.common.ResultCode;
import com.xiaozhan.dto.CandidateQueryDTO;
import com.xiaozhan.dto.FavoriteDTO;
import com.xiaozhan.dto.InvitationDTO;
import com.xiaozhan.entity.*;
import com.xiaozhan.exception.BizException;
import com.xiaozhan.mapper.*;
import com.xiaozhan.security.UserContext;
import com.xiaozhan.service.CandidateService;
import com.xiaozhan.vo.CandidateVO;
import com.xiaozhan.vo.ProjectBriefVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 企业端服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateService {

    private final CandidateFavoriteMapper candidateFavoriteMapper;

    private final InvitationMapper invitationMapper;

    private final CompanyViewLogMapper companyViewLogMapper;

    private final CompanyProfileMapper companyProfileMapper;

    private final StudentProfileMapper studentProfileMapper;

    private final ProjectMapper projectMapper;

    private final SysUserMapper sysUserMapper;

    private final ExpertReviewMapper expertReviewMapper;

    /* ==================== 检索 ==================== */

    @Override
    public IPage<CandidateVO> search(CandidateQueryDTO query, Integer pageNum, Integer pageSize) {
        requireCompanyVerified();
        Long companyId = UserContext.getUserId();

        Page<StudentProfile> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 12 : pageSize);
        IPage<StudentProfile> result = studentProfileMapper.selectPage(page,
                Wrappers.<StudentProfile>lambdaQuery()
                        .eq(StudentProfile::getAllowCompanySearch, 1)
                        .eq(query.getOnlyVerified() != null && query.getOnlyVerified() == 1,
                                StudentProfile::getEduVerified, StudentProfile.VERIFY_PASSED)
                        .eq(StrUtil.isNotBlank(query.getMajorCategory()),
                                StudentProfile::getMajorCategory, query.getMajorCategory())
                        .eq(StrUtil.isNotBlank(query.getDegree()), StudentProfile::getDegree, query.getDegree())
                        .eq(query.getGraduateYear() != null,
                                StudentProfile::getGraduateYear, query.getGraduateYear())
                        .and(StrUtil.isNotBlank(query.getSkillTags()), w -> {
                            for (String tag : query.getSkillTags().split(",")) {
                                if (StrUtil.isNotBlank(tag)) {
                                    w.or().like(StudentProfile::getSkillTags, tag.trim());
                                }
                            }
                        })
                        .orderByDesc(StudentProfile::getPortfolioViews));

        // 关键词需联查用户表与学生作品，故先按档案过滤，再在内存中补充关键词条件
        List<CandidateVO> vos = result.getRecords().stream()
                .map(p -> toCandidateVO(p, companyId))
                .filter(vo -> matchKeyword(vo, query.getKeyword()))
                .toList();

        // 有项目作品的学生优先
        vos = vos.stream()
                .sorted(Comparator.comparingInt((CandidateVO c) ->
                        c.getProjectCount() == null ? 0 : c.getProjectCount()).reversed())
                .toList();

        Page<CandidateVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(vos);
        return voPage;
    }

    private boolean matchKeyword(CandidateVO vo, String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return true;
        }
        String k = keyword.trim().toLowerCase();
        return contains(vo.getNickname(), k)
                || contains(vo.getRealName(), k)
                || contains(vo.getSchool(), k)
                || contains(vo.getMajor(), k)
                || contains(vo.getBio(), k)
                || (vo.getSkillTags() != null && vo.getSkillTags().stream()
                        .anyMatch(t -> t.toLowerCase().contains(k)))
                || (vo.getTopProjects() != null && vo.getTopProjects().stream()
                        .anyMatch(p -> contains(p.getName(), k)));
    }

    private boolean contains(String source, String lowerKeyword) {
        return source != null && source.toLowerCase().contains(lowerKeyword);
    }

    @Override
    public Map<String, Object> candidateDetail(Long studentId) {
        requireCompanyVerified();
        Long companyId = UserContext.getUserId();

        StudentProfile profile = studentProfileMapper.selectOne(
                Wrappers.<StudentProfile>lambdaQuery().eq(StudentProfile::getUserId, studentId));
        if (profile == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND.getCode(), "学生不存在");
        }
        if (profile.getAllowCompanySearch() == null || profile.getAllowCompanySearch() != 1) {
            throw new BizException(ResultCode.FORBIDDEN.getCode(), "该学生未开放企业检索");
        }

        // 扣减查验额度
        consumeQuota(companyId);

        CompanyViewLog logRecord = new CompanyViewLog();
        logRecord.setCompanyId(companyId);
        logRecord.setViewerUserId(companyId);
        logRecord.setStudentId(studentId);
        logRecord.setViewType(CompanyViewLog.TYPE_PORTFOLIO);
        logRecord.setQuotaCost(1);
        logRecord.setCreateTime(LocalDateTime.now());
        companyViewLogMapper.insert(logRecord);

        StudentProfile viewUpdate = new StudentProfile();
        viewUpdate.setId(profile.getId());
        viewUpdate.setPortfolioViews((profile.getPortfolioViews() == null ? 0 : profile.getPortfolioViews()) + 1);
        studentProfileMapper.updateById(viewUpdate);

        CandidateVO candidate = toCandidateVO(profile, companyId);

        List<Project> projects = projectMapper.selectList(Wrappers.<Project>lambdaQuery()
                .eq(Project::getStudentId, studentId)
                .eq(Project::getPublishStatus, Project.PUBLISH_DONE)
                .ne(Project::getVisibility, Project.VIS_PRIVATE)
                .orderByDesc(Project::getUpdateTime));

        List<ProjectBriefVO> briefs = projects.stream().map(this::toBriefVO).toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("candidate", candidate);
        result.put("projects", briefs);

        // 点评摘要（公开的）
        List<ExpertReview> reviews = expertReviewMapper.selectList(Wrappers.<ExpertReview>lambdaQuery()
                .eq(ExpertReview::getStudentId, studentId)
                .eq(ExpertReview::getIsPublic, 1)
                .orderByDesc(ExpertReview::getCreateTime)
                .last("limit 5"));
        result.put("recentReviews", reviews.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("projectId", r.getProjectId());
            m.put("avgScore", r.getAvgScore());
            m.put("comment", r.getComment());
            m.put("strength", r.getStrength());
            m.put("weakness", r.getWeakness());
            m.put("suggestion", r.getSuggestion());
            m.put("createTime", r.getCreateTime());
            SysUser expert = sysUserMapper.selectById(r.getExpertId());
            m.put("expertName", expert == null ? null
                    : StrUtil.blankToDefault(expert.getRealName(), expert.getNickname()));
            return m;
        }).toList());

        return result;
    }

    private void consumeQuota(Long companyId) {
        CompanyProfile profile = companyProfileMapper.selectOne(
                Wrappers.<CompanyProfile>lambdaQuery().eq(CompanyProfile::getUserId, companyId));
        if (profile == null) {
            throw new BizException(ResultCode.COMPANY_NOT_VERIFIED);
        }
        if (profile.getQuotaResetAt() != null && !LocalDate.now().isBefore(profile.getQuotaResetAt())) {
            CompanyProfile reset = new CompanyProfile();
            reset.setId(profile.getId());
            reset.setMonthUsed(0);
            reset.setQuotaResetAt(LocalDate.now().plusMonths(1).withDayOfMonth(1));
            companyProfileMapper.updateById(reset);
            profile.setMonthUsed(0);
        }
        if (profile.remainQuota() <= 0) {
            throw new BizException(ResultCode.QUOTA_EXHAUSTED);
        }
        CompanyProfile update = new CompanyProfile();
        update.setId(profile.getId());
        update.setMonthUsed((profile.getMonthUsed() == null ? 0 : profile.getMonthUsed()) + 1);
        companyProfileMapper.updateById(update);
    }

    private void requireCompanyVerified() {
        Long companyId = UserContext.getUserId();
        CompanyProfile profile = companyProfileMapper.selectOne(
                Wrappers.<CompanyProfile>lambdaQuery().eq(CompanyProfile::getUserId, companyId));
        if (profile == null) {
            throw new BizException(ResultCode.COMPANY_NOT_VERIFIED);
        }
        if (profile.getVerifyStatus() == null || profile.getVerifyStatus() != CompanyProfile.STATUS_PASSED) {
            throw new BizException(ResultCode.COMPANY_NOT_VERIFIED);
        }
    }

    private CandidateVO toCandidateVO(StudentProfile profile, Long companyId) {
        CandidateVO vo = new CandidateVO();
        Long studentId = profile.getUserId();
        vo.setStudentId(studentId);
        vo.setSchool(profile.getSchool());
        vo.setMajor(profile.getMajor());
        vo.setMajorCategory(profile.getMajorCategory());
        vo.setDegree(profile.getDegree());
        vo.setGraduateYear(profile.getGraduateYear());
        vo.setEduVerified(profile.getEduVerified());
        vo.setSkillTags(splitTags(profile.getSkillTags()));
        vo.setBio(profile.getBio());
        vo.setPortfolioViews(profile.getPortfolioViews());

        SysUser user = sysUserMapper.selectById(studentId);
        if (user != null) {
            vo.setNickname(StrUtil.blankToDefault(user.getNickname(), user.getUsername()));
            vo.setRealName(user.getRealName());
            vo.setAvatar(user.getAvatar());
        }

        List<Project> projects = projectMapper.selectList(Wrappers.<Project>lambdaQuery()
                .eq(Project::getStudentId, studentId)
                .eq(Project::getPublishStatus, Project.PUBLISH_DONE)
                .ne(Project::getVisibility, Project.VIS_PRIVATE)
                .orderByDesc(Project::getExpertAvgScore));
        vo.setProjectCount(projects.size());
        vo.setTopProjects(projects.stream().limit(3).map(this::toBriefVO).toList());

        List<BigDecimal> scores = projects.stream()
                .map(Project::getExpertAvgScore)
                .filter(Objects::nonNull)
                .filter(s -> s.compareTo(BigDecimal.ZERO) > 0)
                .toList();
        if (!scores.isEmpty()) {
            vo.setAvgScore(scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP));
        }

        Long fav = candidateFavoriteMapper.selectCount(Wrappers.<CandidateFavorite>lambdaQuery()
                .eq(CandidateFavorite::getCompanyId, companyId)
                .eq(CandidateFavorite::getStudentId, studentId));
        vo.setFavorited(fav != null && fav > 0);

        Long invited = invitationMapper.selectCount(Wrappers.<Invitation>lambdaQuery()
                .eq(Invitation::getCompanyId, companyId)
                .eq(Invitation::getStudentId, studentId));
        vo.setInvited(invited != null && invited > 0);
        return vo;
    }

    private ProjectBriefVO toBriefVO(Project project) {
        ProjectBriefVO vo = new ProjectBriefVO();
        vo.setId(project.getId());
        vo.setName(project.getName());
        vo.setCategory(project.getCategory());
        vo.setProjectType(project.getProjectType());
        vo.setSummary(project.getSummary());
        vo.setCoverUrl(project.getCoverUrl());
        vo.setTechStack(splitTags(project.getTechStack()));
        vo.setExpertAvgScore(project.getExpertAvgScore());
        vo.setExpertReviewCount(project.getExpertReviewCount());
        vo.setViewCount(project.getViewCount());
        return vo;
    }

    /* ==================== 收藏 ==================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void favorite(FavoriteDTO dto) {
        Long companyId = UserContext.getUserId();
        // 与表上的唯一键 uk_company_student_list(company_id, student_id, list_name) 保持一致，
        // 否则同一候选人放入不同清单时会绕过校验、直接撞唯一键抛 DuplicateKeyException（500）。
        String listName = StrUtil.blankToDefault(dto.getListName(), "默认收藏夹");
        Long exists = candidateFavoriteMapper.selectCount(Wrappers.<CandidateFavorite>lambdaQuery()
                .eq(CandidateFavorite::getCompanyId, companyId)
                .eq(CandidateFavorite::getStudentId, dto.getStudentId())
                .eq(CandidateFavorite::getListName, listName));
        if (exists != null && exists > 0) {
            throw new BizException(ResultCode.FAVORITE_EXISTS);
        }
        CandidateFavorite favorite = new CandidateFavorite();
        favorite.setCompanyId(companyId);
        favorite.setStudentId(dto.getStudentId());
        favorite.setListName(listName);
        favorite.setRemark(dto.getRemark());
        try {
            candidateFavoriteMapper.insert(favorite);
        } catch (DuplicateKeyException e) {
            // 并发下两个请求同时通过上面的存在性校验，兜底转成友好业务提示
            throw new BizException(ResultCode.FAVORITE_EXISTS);
        }
        log.info("企业收藏候选人：companyId={}, studentId={}", companyId, dto.getStudentId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfavorite(Long studentId) {
        Long companyId = UserContext.getUserId();
        candidateFavoriteMapper.delete(Wrappers.<CandidateFavorite>lambdaQuery()
                .eq(CandidateFavorite::getCompanyId, companyId)
                .eq(CandidateFavorite::getStudentId, studentId));
        log.info("企业取消收藏：companyId={}, studentId={}", companyId, studentId);
    }

    @Override
    public IPage<CandidateVO> favorites(Integer pageNum, Integer pageSize, String listName) {
        Long companyId = UserContext.getUserId();
        Page<CandidateFavorite> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 12 : pageSize);
        IPage<CandidateFavorite> result = candidateFavoriteMapper.selectPage(page,
                Wrappers.<CandidateFavorite>lambdaQuery()
                        .eq(CandidateFavorite::getCompanyId, companyId)
                        .eq(StrUtil.isNotBlank(listName), CandidateFavorite::getListName, listName)
                        .orderByDesc(CandidateFavorite::getCreateTime));

        return result.convert(fav -> {
            StudentProfile profile = studentProfileMapper.selectOne(
                    Wrappers.<StudentProfile>lambdaQuery().eq(StudentProfile::getUserId, fav.getStudentId()));
            if (profile == null) {
                CandidateVO empty = new CandidateVO();
                empty.setStudentId(fav.getStudentId());
                return empty;
            }
            return toCandidateVO(profile, companyId);
        });
    }

    /* ==================== 邀约 ==================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendInvitation(InvitationDTO dto) {
        requireCompanyVerified();
        Long companyId = UserContext.getUserId();

        Long exists = invitationMapper.selectCount(Wrappers.<Invitation>lambdaQuery()
                .eq(Invitation::getCompanyId, companyId)
                .eq(Invitation::getStudentId, dto.getStudentId())
                .eq(Invitation::getStatus, Invitation.STATUS_PENDING));
        if (exists != null && exists > 0) {
            throw new BizException(ResultCode.INVITATION_EXISTS);
        }

        Invitation invitation = new Invitation();
        invitation.setCompanyId(companyId);
        invitation.setStudentId(dto.getStudentId());
        invitation.setProjectId(dto.getProjectId());
        invitation.setJobTitle(dto.getJobTitle());
        invitation.setContent(dto.getContent());
        invitation.setContactInfo(dto.getContactInfo());
        invitation.setStatus(Invitation.STATUS_PENDING);
        invitationMapper.insert(invitation);
        log.info("企业发送邀约：companyId={}, studentId={}", companyId, dto.getStudentId());
        return invitation.getId();
    }

    @Override
    public IPage<Map<String, Object>> sentInvitations(Integer pageNum, Integer pageSize) {
        Long companyId = UserContext.getUserId();
        return invitationPage(pageNum, pageSize,
                Wrappers.<Invitation>lambdaQuery()
                        .eq(Invitation::getCompanyId, companyId)
                        .orderByDesc(Invitation::getCreateTime), true);
    }

    @Override
    public IPage<Map<String, Object>> receivedInvitations(Integer pageNum, Integer pageSize) {
        Long studentId = UserContext.getUserId();
        return invitationPage(pageNum, pageSize,
                Wrappers.<Invitation>lambdaQuery()
                        .eq(Invitation::getStudentId, studentId)
                        .orderByDesc(Invitation::getCreateTime), false);
    }

    private IPage<Map<String, Object>> invitationPage(Integer pageNum, Integer pageSize,
                                                      com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Invitation> wrapper,
                                                      boolean showStudent) {
        Page<Invitation> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        IPage<Invitation> result = invitationMapper.selectPage(page, wrapper);
        return result.convert(inv -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", inv.getId());
            map.put("projectId", inv.getProjectId());
            map.put("jobTitle", inv.getJobTitle());
            map.put("content", inv.getContent());
            map.put("status", inv.getStatus());
            map.put("contactInfo", inv.getContactInfo());
            map.put("replyTime", inv.getReplyTime());
            map.put("createTime", inv.getCreateTime());

            if (showStudent) {
                SysUser student = sysUserMapper.selectById(inv.getStudentId());
                map.put("studentId", inv.getStudentId());
                map.put("studentName", student == null ? null
                        : StrUtil.blankToDefault(student.getNickname(), student.getUsername()));
                map.put("studentAvatar", student == null ? null : student.getAvatar());
            } else {
                CompanyProfile company = companyProfileMapper.selectOne(
                        Wrappers.<CompanyProfile>lambdaQuery().eq(CompanyProfile::getUserId, inv.getCompanyId()));
                map.put("companyName", company == null ? null : company.getCompanyName());
                map.put("companyIndustry", company == null ? null : company.getIndustry());
            }
            return map;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyInvitation(Long invitationId, boolean accept, String contactInfo) {
        Invitation invitation = invitationMapper.selectById(invitationId);
        if (invitation == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "邀约不存在");
        }
        if (!Objects.equals(invitation.getStudentId(), UserContext.getUserId())) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        if (invitation.getStatus() != null && invitation.getStatus() != Invitation.STATUS_PENDING) {
            throw new BizException(ResultCode.FAIL.getCode(), "该邀约已回应");
        }

        Invitation update = new Invitation();
        update.setId(invitationId);
        update.setStatus(accept ? Invitation.STATUS_ACCEPTED : Invitation.STATUS_REJECTED);
        update.setReplyTime(LocalDateTime.now());
        if (accept && StrUtil.isNotBlank(contactInfo)) {
            update.setContactInfo(contactInfo);
        }
        invitationMapper.updateById(update);
        log.info("学生回应邀约：invitationId={}, accept={}", invitationId, accept);
    }

    /* ==================== 统计 ==================== */

    @Override
    public Map<String, Object> companyStatistics() {
        Long companyId = UserContext.getUserId();
        CompanyProfile profile = companyProfileMapper.selectOne(
                Wrappers.<CompanyProfile>lambdaQuery().eq(CompanyProfile::getUserId, companyId));

        Map<String, Object> result = new LinkedHashMap<>();
        long favorites = candidateFavoriteMapper.selectCount(Wrappers.<CandidateFavorite>lambdaQuery()
                .eq(CandidateFavorite::getCompanyId, companyId));
        long invitations = invitationMapper.selectCount(Wrappers.<Invitation>lambdaQuery()
                .eq(Invitation::getCompanyId, companyId));
        long pendingInvitations = invitationMapper.selectCount(Wrappers.<Invitation>lambdaQuery()
                .eq(Invitation::getCompanyId, companyId)
                .eq(Invitation::getStatus, Invitation.STATUS_PENDING));
        long acceptedInvitations = invitationMapper.selectCount(Wrappers.<Invitation>lambdaQuery()
                .eq(Invitation::getCompanyId, companyId)
                .eq(Invitation::getStatus, Invitation.STATUS_ACCEPTED));
        long views = companyViewLogMapper.selectCount(Wrappers.<CompanyViewLog>lambdaQuery()
                .eq(CompanyViewLog::getCompanyId, companyId));

        result.put("favoriteCount", favorites);
        result.put("invitationCount", invitations);
        result.put("pendingInvitationCount", pendingInvitations);
        result.put("acceptedInvitationCount", acceptedInvitations);
        result.put("viewCount", views);
        result.put("packageType", profile == null ? CompanyProfile.PKG_FREE : profile.getPackageType());
        result.put("monthQuota", profile == null ? 0 : profile.getMonthQuota());
        result.put("monthUsed", profile == null ? 0 : profile.getMonthUsed());
        result.put("remainQuota", profile == null ? 0 : profile.remainQuota());
        result.put("packageExpire", profile == null ? null : profile.getPackageExpire());
        result.put("verifyStatus", profile == null ? 0 : profile.getVerifyStatus());
        return result;
    }

    @Override
    public IPage<Map<String, Object>> viewLogs(Integer pageNum, Integer pageSize) {
        Long companyId = UserContext.getUserId();
        Page<CompanyViewLog> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 20 : pageSize);
        IPage<CompanyViewLog> result = companyViewLogMapper.selectPage(page,
                Wrappers.<CompanyViewLog>lambdaQuery()
                        .eq(CompanyViewLog::getCompanyId, companyId)
                        .orderByDesc(CompanyViewLog::getCreateTime));
        return result.convert(logRecord -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", logRecord.getId());
            map.put("studentId", logRecord.getStudentId());
            map.put("projectId", logRecord.getProjectId());
            map.put("viewType", logRecord.getViewType());
            map.put("createTime", logRecord.getCreateTime());
            SysUser student = sysUserMapper.selectById(logRecord.getStudentId());
            map.put("studentName", student == null ? null
                    : StrUtil.blankToDefault(student.getNickname(), student.getUsername()));
            return map;
        });
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
