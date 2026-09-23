package com.xiaozhan.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaozhan.common.ResultCode;
import com.xiaozhan.config.XiaozhanProperties;
import com.xiaozhan.dto.ProjectSaveDTO;
import com.xiaozhan.entity.*;
import com.xiaozhan.exception.BizException;
import com.xiaozhan.mapper.*;
import com.xiaozhan.security.UserContext;
import com.xiaozhan.service.FileService;
import com.xiaozhan.service.ProjectService;
import com.xiaozhan.vo.ProjectAssetVO;
import com.xiaozhan.vo.ProjectMemberVO;
import com.xiaozhan.vo.ProjectVO;
import com.xiaozhan.vo.ReplyVO;
import com.xiaozhan.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 项目作品集服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;

    private final ProjectAssetMapper projectAssetMapper;

    private final ProjectMemberMapper projectMemberMapper;

    private final ExpertReviewMapper expertReviewMapper;

    private final ReviewReplyMapper reviewReplyMapper;

    private final StudentProfileMapper studentProfileMapper;

    private final SysUserMapper sysUserMapper;

    private final ExpertProfileMapper expertProfileMapper;

    private final CompanyViewLogMapper companyViewLogMapper;

    private final ContentAuditMapper contentAuditMapper;

    private final FileService fileService;

    private final XiaozhanProperties properties;

    /* ==================== 增删改 ==================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ProjectSaveDTO dto) {
        Long studentId = UserContext.getUserId();
        ensureStudentProfile(studentId);

        Project project = new Project();
        fillProject(project, dto);
        project.setStudentId(studentId);
        project.setPublishStatus(Project.PUBLISH_DRAFT);
        project.setReviewStatus(Project.AUDIT_PENDING);
        project.setInReviewPool(0);
        project.setPoolBoost(0);
        project.setViewCount(0);
        project.setExpertReviewCount(0);
        projectMapper.insert(project);

        syncMembers(project.getId(), studentId, dto.getMemberIds());
        log.info("学生创建项目：studentId={}, projectId={}, name={}", studentId, project.getId(), project.getName());
        return project.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ProjectSaveDTO dto) {
        if (dto.getId() == null) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "缺少项目 ID");
        }
        Project existing = mustOwn(dto.getId());

        fillProject(existing, dto);
        // 已发布项目修改后重新进入审核
        if (existing.getPublishStatus() != null && existing.getPublishStatus() == Project.PUBLISH_DONE) {
            existing.setReviewStatus(Project.AUDIT_PENDING);
        }
        projectMapper.updateById(existing);
        syncMembers(existing.getId(), existing.getStudentId(), dto.getMemberIds());
        log.info("更新项目：projectId={}", existing.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long projectId) {
        Project project = mustOwn(projectId);

        projectAssetMapper.delete(Wrappers.<ProjectAsset>lambdaQuery()
                .eq(ProjectAsset::getProjectId, projectId));
        projectMemberMapper.delete(Wrappers.<ProjectMember>lambdaQuery()
                .eq(ProjectMember::getProjectId, projectId));
        projectMapper.deleteById(project.getId());
        log.info("删除项目：projectId={}", projectId);
    }

    private void fillProject(Project project, ProjectSaveDTO dto) {
        project.setName(dto.getName());
        project.setProjectType(dto.getProjectType());
        project.setCategory(dto.getCategory());
        project.setSummary(dto.getSummary());
        project.setRoleDesc(dto.getRoleDesc());
        project.setTechStack(dto.getTechStack());
        project.setHighlight(dto.getHighlight());
        project.setRepoUrl(dto.getRepoUrl());
        project.setRepoPlatform(dto.getRepoPlatform());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setCoverUrl(dto.getCoverUrl());
        project.setVisibility(StrUtil.blankToDefault(dto.getVisibility(), Project.VIS_PUBLIC));
        if (dto.getInReviewPool() != null) {
            project.setInReviewPool(dto.getInReviewPool());
        }
    }

    private Project mustOwn(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException(ResultCode.PROJECT_NOT_FOUND);
        }
        Long currentUserId = UserContext.getUserId();
        if (!Objects.equals(project.getStudentId(), currentUserId) && !isAdmin()) {
            throw new BizException(ResultCode.PROJECT_NO_PERMISSION);
        }
        return project;
    }

    private boolean isAdmin() {
        return "ADMIN".equals(UserContext.getRole());
    }

    private void ensureStudentProfile(Long userId) {
        StudentProfile profile = studentProfileMapper.selectOne(
                Wrappers.<StudentProfile>lambdaQuery().eq(StudentProfile::getUserId, userId));
        if (profile == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND.getCode(), "请先完善学生档案");
        }
    }

    private void syncMembers(Long projectId, Long ownerId, List<Long> memberIds) {
        projectMemberMapper.delete(Wrappers.<ProjectMember>lambdaQuery()
                .eq(ProjectMember::getProjectId, projectId));

        ProjectMember owner = new ProjectMember();
        owner.setProjectId(projectId);
        owner.setStudentId(ownerId);
        owner.setMemberRole("项目主理人");
        owner.setIsOwner(1);
        projectMemberMapper.insert(owner);

        if (memberIds == null || memberIds.isEmpty()) {
            return;
        }
        for (Long memberId : memberIds) {
            if (memberId == null || memberId.equals(ownerId)) {
                continue;
            }
            ProjectMember member = new ProjectMember();
            member.setProjectId(projectId);
            member.setStudentId(memberId);
            member.setMemberRole("协作成员");
            member.setIsOwner(0);
            projectMemberMapper.insert(member);
        }
    }

    /* ==================== 查询 ==================== */

    @Override
    public ProjectVO detail(Long projectId, boolean forCompany) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException(ResultCode.PROJECT_NOT_FOUND);
        }

        Long currentUserId = UserContext.getUserId();
        boolean owner = Objects.equals(project.getStudentId(), currentUserId);
        boolean companyView = forCompany || "COMPANY".equals(UserContext.getRole());

        // 可见性校验
        if (!owner && !isAdmin()) {
            if (Project.VIS_PRIVATE.equals(project.getVisibility())) {
                throw new BizException(ResultCode.PROJECT_NO_PERMISSION.getCode(), "该项目未公开");
            }
            if (project.getPublishStatus() == null || project.getPublishStatus() != Project.PUBLISH_DONE) {
                throw new BizException(ResultCode.PROJECT_NOT_PUBLISHED);
            }
        }

        // 浏览量 +1
        Project countUpdate = new Project();
        countUpdate.setId(projectId);
        countUpdate.setViewCount((project.getViewCount() == null ? 0 : project.getViewCount()) + 1);
        projectMapper.updateById(countUpdate);
        project.setViewCount(countUpdate.getViewCount());

        // 企业查验记录 + 学生作品集访问数
        if (companyView && !owner) {
            recordCompanyView(currentUserId, project);
        }

        ProjectVO vo = toVO(project, owner);
        vo.setAssets(listAssets(projectId));
        vo.setMembers(listMembers(projectId));

        // 点评：公开的 + 本人的
        List<ExpertReview> reviews = expertReviewMapper.selectList(
                Wrappers.<ExpertReview>lambdaQuery()
                        .eq(ExpertReview::getProjectId, projectId)
                        .orderByDesc(ExpertReview::getCreateTime));
        vo.setReviews(buildReviewVOs(reviews, owner));

        return vo;
    }

    private void recordCompanyView(Long companyUserId, Project project) {
        CompanyViewLog logRecord = new CompanyViewLog();
        logRecord.setCompanyId(companyUserId);
        logRecord.setViewerUserId(companyUserId);
        logRecord.setStudentId(project.getStudentId());
        logRecord.setProjectId(project.getId());
        logRecord.setViewType(CompanyViewLog.TYPE_PROJECT);
        logRecord.setQuotaCost(1);
        logRecord.setCreateTime(LocalDateTime.now());
        companyViewLogMapper.insert(logRecord);

        StudentProfile profile = studentProfileMapper.selectOne(
                Wrappers.<StudentProfile>lambdaQuery().eq(StudentProfile::getUserId, project.getStudentId()));
        if (profile != null) {
            StudentProfile update = new StudentProfile();
            update.setId(profile.getId());
            update.setPortfolioViews((profile.getPortfolioViews() == null ? 0 : profile.getPortfolioViews()) + 1);
            studentProfileMapper.updateById(update);
        }
    }

    @Override
    public IPage<ProjectVO> myProjects(Integer pageNum, Integer pageSize, Integer publishStatus) {
        Long studentId = UserContext.getUserId();
        Page<Project> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        IPage<Project> result = projectMapper.selectPage(page,
                Wrappers.<Project>lambdaQuery()
                        .eq(Project::getStudentId, studentId)
                        .eq(publishStatus != null, Project::getPublishStatus, publishStatus)
                        .orderByDesc(Project::getUpdateTime));
        return result.convert(p -> toVO(p, true));
    }

    @Override
    public List<ProjectVO> portfolioOf(Long studentId) {
        List<Project> projects = projectMapper.selectList(
                Wrappers.<Project>lambdaQuery()
                        .eq(Project::getStudentId, studentId)
                        .eq(Project::getPublishStatus, Project.PUBLISH_DONE)
                        .ne(Project::getVisibility, Project.VIS_PRIVATE)
                        .orderByDesc(Project::getUpdateTime));
        Long currentUserId = UserContext.getUserId();
        return projects.stream()
                .map(p -> {
                    ProjectVO vo = toVO(p, Objects.equals(p.getStudentId(), currentUserId));
                    vo.setAssets(listAssets(p.getId()));
                    return vo;
                })
                .toList();
    }

    @Override
    public IPage<ProjectVO> search(Integer pageNum, Integer pageSize, String keyword, String category, String sortBy) {
        Page<Project> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 12 : pageSize);
        IPage<Project> result = projectMapper.selectPage(page,
                Wrappers.<Project>lambdaQuery()
                        .eq(Project::getPublishStatus, Project.PUBLISH_DONE)
                        .eq(Project::getReviewStatus, Project.AUDIT_PASSED)
                        .ne(Project::getVisibility, Project.VIS_PRIVATE)
                        .eq(StrUtil.isNotBlank(category), Project::getCategory, category)
                        .and(StrUtil.isNotBlank(keyword), w -> w
                                .like(Project::getName, keyword)
                                .or().like(Project::getSummary, keyword)
                                .or().like(Project::getTechStack, keyword))
                        .orderByDesc("score".equals(sortBy), Project::getExpertAvgScore)
                        .orderByDesc("views".equals(sortBy), Project::getViewCount)
                        .orderByDesc("latest".equals(sortBy) || StrUtil.isBlank(sortBy), Project::getUpdateTime));

        return result.convert(p -> {
            ProjectVO vo = toVO(p, Objects.equals(p.getStudentId(), UserContext.getUserId()));
            vo.setAssets(listAssets(p.getId()));
            return vo;
        });
    }

    @Override
    public IPage<ProjectVO> reviewPool(Integer pageNum, Integer pageSize, String category) {
        Page<Project> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        IPage<Project> result = projectMapper.selectPage(page,
                Wrappers.<Project>lambdaQuery()
                        .eq(Project::getInReviewPool, 1)
                        .eq(Project::getPublishStatus, Project.PUBLISH_DONE)
                        .eq(StrUtil.isNotBlank(category), Project::getCategory, category)
                        // 未被点评过的项目优先，其次按加推权重
                        .orderByAsc(Project::getExpertReviewCount)
                        .orderByDesc(Project::getPoolBoost)
                        .orderByDesc(Project::getPoolJoinTime));

        return result.convert(p -> {
            ProjectVO vo = toVO(p, false);
            vo.setAssets(listAssets(p.getId()));
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleReviewPool(Long projectId, boolean join) {
        Project project = mustOwn(projectId);
        Project update = new Project();
        update.setId(project.getId());
        update.setInReviewPool(join ? 1 : 0);
        update.setPoolJoinTime(join ? LocalDateTime.now() : null);
        projectMapper.updateById(update);
        log.info("求点评池状态变更：projectId={}, join={}", projectId, join);
    }

    /* ==================== 发布 ==================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long projectId) {
        Project project = mustOwn(projectId);
        long assetCount = projectAssetMapper.selectCount(Wrappers.<ProjectAsset>lambdaQuery()
                .eq(ProjectAsset::getProjectId, projectId));
        if (assetCount == 0) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(),
                    "发布前请至少上传一份源码包、文档或演示视频");
        }

        Project update = new Project();
        update.setId(projectId);
        update.setPublishStatus(Project.PUBLISH_DONE);
        update.setReviewStatus(Project.AUDIT_PENDING);
        projectMapper.updateById(update);

        // 写入内容审核队列
        ContentAudit audit = new ContentAudit();
        audit.setTargetType("PROJECT");
        audit.setTargetId(projectId);
        audit.setSnapshot(project.getName());
        audit.setAutoResult("PASS");
        audit.setStatus(ContentAudit.STATUS_PENDING);
        contentAuditMapper.insert(audit);

        log.info("项目发布：projectId={}", projectId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpublish(Long projectId) {
        Project project = mustOwn(projectId);
        Project update = new Project();
        update.setId(project.getId());
        update.setPublishStatus(Project.PUBLISH_DRAFT);
        projectMapper.updateById(update);
        log.info("项目下架：projectId={}", projectId);
    }

    /* ==================== 素材 ==================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ProjectAsset> saveAsset(Long projectId, ProjectAsset asset) {
        Project project = mustOwn(projectId);

        String type = asset.getAssetType();
        long current = projectAssetMapper.selectCount(Wrappers.<ProjectAsset>lambdaQuery()
                .eq(ProjectAsset::getProjectId, projectId)
                .eq(ProjectAsset::getAssetType, type));
        int limit = switch (type == null ? "" : type) {
            case ProjectAsset.TYPE_SOURCE -> 3;
            case ProjectAsset.TYPE_DOC -> 10;
            case ProjectAsset.TYPE_VIDEO -> 3;
            case ProjectAsset.TYPE_IMAGE -> 20;
            default -> 10;
        };
        if (current >= limit) {
            throw new BizException(ResultCode.ASSET_TYPE_LIMIT.getCode(),
                    "该类素材最多上传 " + limit + " 份");
        }

        asset.setId(null);
        asset.setProjectId(project.getId());
        asset.setSortOrder((int) current);
        projectAssetMapper.insert(asset);
        log.info("项目素材已登记：projectId={}, type={}, url={}", projectId, type, asset.getFileUrl());
        return listAssets(projectId).stream()
                .filter(a -> a.getId().equals(asset.getId()))
                .map(this::toAssetEntityStub)
                .toList();
    }

    private ProjectAsset toAssetEntityStub(ProjectAssetVO vo) {
        ProjectAsset entity = new ProjectAsset();
        entity.setId(vo.getId());
        entity.setProjectId(null);
        entity.setAssetType(vo.getAssetType());
        entity.setFileName(vo.getFileName());
        entity.setFileUrl(vo.getFileUrl());
        entity.setFileSize(vo.getFileSize());
        entity.setFileExt(vo.getFileExt());
        entity.setSortOrder(vo.getSortOrder());
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAsset(Long assetId) {
        ProjectAsset asset = projectAssetMapper.selectById(assetId);
        if (asset == null) {
            return;
        }
        mustOwn(asset.getProjectId());
        projectAssetMapper.deleteById(assetId);
        fileService.delete(asset.getFileUrl());
        log.info("删除项目素材：assetId={}", assetId);
    }

    private List<ProjectAssetVO> listAssets(Long projectId) {
        List<ProjectAsset> assets = projectAssetMapper.selectList(
                Wrappers.<ProjectAsset>lambdaQuery()
                        .eq(ProjectAsset::getProjectId, projectId)
                        .orderByAsc(ProjectAsset::getAssetType)
                        .orderByAsc(ProjectAsset::getSortOrder));
        return assets.stream().map(this::toAssetVO).toList();
    }

    private ProjectAssetVO toAssetVO(ProjectAsset asset) {
        ProjectAssetVO vo = new ProjectAssetVO();
        vo.setId(asset.getId());
        vo.setAssetType(asset.getAssetType());
        vo.setFileName(asset.getFileName());
        vo.setFileUrl(asset.getFileUrl());
        vo.setFileSize(asset.getFileSize());
        vo.setFileExt(asset.getFileExt());
        vo.setSortOrder(asset.getSortOrder());
        vo.setReadableSize(asset.getFileSize() == null ? null : FileUtil.readableFileSize(asset.getFileSize()));
        return vo;
    }

    private List<ProjectMemberVO> listMembers(Long projectId) {
        List<ProjectMember> members = projectMemberMapper.selectList(
                Wrappers.<ProjectMember>lambdaQuery()
                        .eq(ProjectMember::getProjectId, projectId)
                        .orderByDesc(ProjectMember::getIsOwner));
        return members.stream().map(m -> {
            ProjectMemberVO vo = new ProjectMemberVO();
            vo.setStudentId(m.getStudentId());
            vo.setMemberRole(m.getMemberRole());
            vo.setIsOwner(m.getIsOwner());
            SysUser user = sysUserMapper.selectById(m.getStudentId());
            if (user != null) {
                vo.setStudentName(StrUtil.blankToDefault(user.getNickname(), user.getUsername()));
                vo.setAvatar(user.getAvatar());
            }
            StudentProfile profile = studentProfileMapper.selectOne(
                    Wrappers.<StudentProfile>lambdaQuery().eq(StudentProfile::getUserId, m.getStudentId()));
            if (profile != null) {
                vo.setSchool(profile.getSchool());
            }
            return vo;
        }).toList();
    }

    /* ==================== 转换 ==================== */

    private ProjectVO toVO(Project project, boolean owner) {
        ProjectVO vo = new ProjectVO();
        vo.setId(project.getId());
        vo.setStudentId(project.getStudentId());
        vo.setName(project.getName());
        vo.setProjectType(project.getProjectType());
        vo.setCategory(project.getCategory());
        vo.setSummary(project.getSummary());
        vo.setRoleDesc(project.getRoleDesc());
        vo.setTechStack(splitTags(project.getTechStack()));
        vo.setHighlight(project.getHighlight());
        vo.setRepoUrl(project.getRepoUrl());
        vo.setRepoPlatform(project.getRepoPlatform());
        vo.setStartDate(project.getStartDate());
        vo.setEndDate(project.getEndDate());
        vo.setCoverUrl(project.getCoverUrl());
        vo.setVisibility(project.getVisibility());
        vo.setPublishStatus(project.getPublishStatus());
        vo.setReviewStatus(project.getReviewStatus());
        vo.setInReviewPool(project.getInReviewPool());
        vo.setViewCount(project.getViewCount());
        vo.setExpertAvgScore(project.getExpertAvgScore());
        vo.setExpertReviewCount(project.getExpertReviewCount());
        vo.setCreateTime(project.getCreateTime());
        vo.setUpdateTime(project.getUpdateTime());
        vo.setOwner(owner);

        SysUser user = sysUserMapper.selectById(project.getStudentId());
        if (user != null) {
            vo.setStudentName(StrUtil.blankToDefault(user.getNickname(), user.getUsername()));
            vo.setStudentAvatar(user.getAvatar());
        }
        StudentProfile profile = studentProfileMapper.selectOne(
                Wrappers.<StudentProfile>lambdaQuery().eq(StudentProfile::getUserId, project.getStudentId()));
        if (profile != null) {
            vo.setStudentSchool(profile.getSchool());
            vo.setStudentMajor(profile.getMajor());
            vo.setEduVerified(profile.getEduVerified());
        }
        return vo;
    }

    private List<ReviewVO> buildReviewVOs(List<ExpertReview> reviews, boolean includeAll) {
        List<ReviewVO> list = new ArrayList<>(reviews.size());
        for (ExpertReview review : reviews) {
            boolean visible = review.getIsPublic() != null && review.getIsPublic() == 1;
            if (!visible && !includeAll) {
                continue;
            }
            list.add(toReviewVO(review));
        }
        return list;
    }

    private ReviewVO toReviewVO(ExpertReview review) {
        ReviewVO vo = new ReviewVO();
        vo.setId(review.getId());
        vo.setProjectId(review.getProjectId());
        vo.setExpertId(review.getExpertId());
        vo.setStudentId(review.getStudentId());
        vo.setScoreCompletion(review.getScoreCompletion());
        vo.setScoreNormative(review.getScoreNormative());
        vo.setScoreInnovation(review.getScoreInnovation());
        vo.setScoreTechnical(review.getScoreTechnical());
        vo.setAvgScore(review.getAvgScore());
        vo.setComment(review.getComment());
        vo.setStrength(review.getStrength());
        vo.setWeakness(review.getWeakness());
        vo.setSuggestion(review.getSuggestion());
        vo.setInviteType(review.getInviteType());
        vo.setQualityStatus(review.getQualityStatus());
        vo.setIsPublic(review.getIsPublic());
        vo.setThanksFlag(review.getThanksFlag());
        vo.setAppealStatus(review.getAppealStatus());
        vo.setCreateTime(review.getCreateTime());

        SysUser expertUser = sysUserMapper.selectById(review.getExpertId());
        if (expertUser != null) {
            vo.setExpertName(StrUtil.blankToDefault(expertUser.getRealName(), expertUser.getNickname()));
            vo.setExpertAvatar(expertUser.getAvatar());
        }
        ExpertProfile expertProfile = expertProfileMapper.selectOne(
                Wrappers.<ExpertProfile>lambdaQuery().eq(ExpertProfile::getUserId, review.getExpertId()));
        if (expertProfile != null) {
            boolean showOrg = expertProfile.getShowOrg() == null || expertProfile.getShowOrg() == 1;
            if (showOrg) {
                vo.setExpertOrg(expertProfile.getOrgName());
                vo.setExpertPosition(expertProfile.getPosition());
            }
            vo.setExpertType(expertProfile.getExpertType());
            vo.setExpertLevel(expertProfile.getLevel());
        }

        List<ReviewReply> replies = reviewReplyMapper.selectList(
                Wrappers.<ReviewReply>lambdaQuery()
                        .eq(ReviewReply::getReviewId, review.getId())
                        .orderByAsc(ReviewReply::getCreateTime));
        vo.setReplies(replies.stream().map(this::toReplyVO).toList());
        return vo;
    }

    private ReplyVO toReplyVO(ReviewReply reply) {
        ReplyVO vo = new ReplyVO();
        vo.setId(reply.getId());
        vo.setUserId(reply.getUserId());
        vo.setRole(reply.getRole());
        vo.setContent(reply.getContent());
        vo.setCreateTime(reply.getCreateTime());
        SysUser user = sysUserMapper.selectById(reply.getUserId());
        if (user != null) {
            vo.setUserName(StrUtil.blankToDefault(user.getNickname(), user.getUsername()));
            vo.setAvatar(user.getAvatar());
        }
        return vo;
    }

    /* ==================== 统计 ==================== */

    @Override
    public Map<String, Object> statistics(Long studentId) {
        Long targetId = studentId == null ? UserContext.getUserId() : studentId;

        Map<String, Object> result = new LinkedHashMap<>();
        long total = projectMapper.selectCount(Wrappers.<Project>lambdaQuery()
                .eq(Project::getStudentId, targetId));
        long published = projectMapper.selectCount(Wrappers.<Project>lambdaQuery()
                .eq(Project::getStudentId, targetId)
                .eq(Project::getPublishStatus, Project.PUBLISH_DONE));
        long draft = total - published;

        List<Project> projects = projectMapper.selectList(Wrappers.<Project>lambdaQuery()
                .eq(Project::getStudentId, targetId));
        int views = projects.stream().mapToInt(p -> p.getViewCount() == null ? 0 : p.getViewCount()).sum();
        int reviews = projects.stream().mapToInt(p -> p.getExpertReviewCount() == null ? 0 : p.getExpertReviewCount()).sum();
        int poolCount = (int) projects.stream().filter(p -> p.getInReviewPool() != null && p.getInReviewPool() == 1).count();

        result.put("totalProjects", total);
        result.put("publishedProjects", published);
        result.put("draftProjects", draft);
        result.put("totalViews", views);
        result.put("totalReviews", reviews);
        result.put("inPoolCount", poolCount);

        StudentProfile profile = studentProfileMapper.selectOne(
                Wrappers.<StudentProfile>lambdaQuery().eq(StudentProfile::getUserId, targetId));
        result.put("portfolioViews", profile == null ? 0 : profile.getPortfolioViews());
        result.put("eduVerified", profile == null ? 0 : profile.getEduVerified());
        result.put("skillTags", profile == null ? List.of() : splitTags(profile.getSkillTags()));
        return result;
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
