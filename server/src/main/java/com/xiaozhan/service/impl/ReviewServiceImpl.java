package com.xiaozhan.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaozhan.common.ResultCode;
import com.xiaozhan.config.XiaozhanProperties;
import com.xiaozhan.dto.ReviewAppealDTO;
import com.xiaozhan.dto.ReviewInviteDTO;
import com.xiaozhan.dto.ReviewReplyDTO;
import com.xiaozhan.dto.ReviewSubmitDTO;
import com.xiaozhan.entity.*;
import com.xiaozhan.exception.BizException;
import com.xiaozhan.mapper.*;
import com.xiaozhan.security.UserContext;
import com.xiaozhan.service.ReviewService;
import com.xiaozhan.vo.ReplyVO;
import com.xiaozhan.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 点评服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ExpertReviewMapper expertReviewMapper;

    private final ReviewReplyMapper reviewReplyMapper;

    private final ReviewAppealMapper reviewAppealMapper;

    private final ProjectMapper projectMapper;

    private final ExpertProfileMapper expertProfileMapper;

    private final StudentProfileMapper studentProfileMapper;

    private final SysUserMapper sysUserMapper;

    private final XiaozhanProperties properties;

    /* ==================== 提交点评 ==================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submit(ReviewSubmitDTO dto) {
        Long expertUserId = UserContext.getUserId();

        ExpertProfile expert = expertProfileMapper.selectOne(
                Wrappers.<ExpertProfile>lambdaQuery().eq(ExpertProfile::getUserId, expertUserId));
        if (expert == null) {
            throw new BizException(ResultCode.EXPERT_NOT_VERIFIED);
        }
        if (expert.getVerifyStatus() == null || expert.getVerifyStatus() != ExpertProfile.STATUS_PASSED) {
            throw new BizException(ResultCode.EXPERT_NOT_VERIFIED);
        }

        Project project = projectMapper.selectById(dto.getProjectId());
        if (project == null) {
            throw new BizException(ResultCode.PROJECT_NOT_FOUND);
        }
        if (Objects.equals(project.getStudentId(), expertUserId)) {
            throw new BizException(ResultCode.REVIEW_SELF_FORBIDDEN);
        }

        // 评语长度
        int minLen = properties.getReview().getMinCommentLength();
        if (StrUtil.length(StrUtil.trim(dto.getComment())) < minLen) {
            throw new BizException(ResultCode.REVIEW_CONTENT_TOO_SHORT.getCode(),
                    "评语过短，至少需要 " + minLen + " 字");
        }

        // 30 天内重复点评校验
        int duplicateDays = properties.getReview().getDuplicateDays();
        Long dup = expertReviewMapper.selectCount(Wrappers.<ExpertReview>lambdaQuery()
                .eq(ExpertReview::getExpertId, expertUserId)
                .eq(ExpertReview::getProjectId, project.getId())
                .ge(ExpertReview::getCreateTime, LocalDateTime.now().minusDays(duplicateDays)));
        if (dup != null && dup > 0) {
            throw new BizException(ResultCode.REVIEW_DUPLICATE);
        }

        // 每日额度校验
        Long todayCount = expertReviewMapper.selectCount(Wrappers.<ExpertReview>lambdaQuery()
                .eq(ExpertReview::getExpertId, expertUserId)
                .ge(ExpertReview::getCreateTime, LocalDate.now().atStartOfDay()));
        int quota = expert.getDailyQuota() == null
                ? properties.getReview().getExpertDailyLimit() : expert.getDailyQuota();
        if (todayCount != null && todayCount >= quota) {
            throw new BizException(ResultCode.REVIEW_QUOTA_EXHAUSTED);
        }

        ExpertReview review = new ExpertReview();
        review.setProjectId(project.getId());
        review.setExpertId(expertUserId);
        review.setStudentId(project.getStudentId());
        review.setScoreCompletion(dto.getScoreCompletion());
        review.setScoreNormative(dto.getScoreNormative());
        review.setScoreInnovation(dto.getScoreInnovation());
        review.setScoreTechnical(dto.getScoreTechnical());
        review.setAvgScore(review.calcAvgScore());
        review.setComment(dto.getComment().trim());
        review.setStrength(dto.getStrength());
        review.setWeakness(dto.getWeakness());
        review.setSuggestion(dto.getSuggestion());
        review.setInviteType(project.getInReviewPool() != null && project.getInReviewPool() == 1
                ? ExpertReview.INVITE_POOL : ExpertReview.INVITE_DIRECT);
        review.setQualityStatus(autoQualityCheck(dto));
        review.setIsPublic(dto.getIsPublic() == null ? 1 : dto.getIsPublic());
        review.setThanksFlag(0);
        review.setAppealStatus(0);
        expertReviewMapper.insert(review);

        // 回写项目均分与点评数
        refreshProjectScore(project.getId());

        // 更新专家业绩
        expert.setTotalReviews((expert.getTotalReviews() == null ? 0 : expert.getTotalReviews()) + 1);
        expert.setPoints((expert.getPoints() == null ? 0 : expert.getPoints()) + 10);
        refreshExpertLevel(expert);
        expertProfileMapper.updateById(expert);

        // 点评后自动移出求点评池（已获点评）
        Project poolUpdate = new Project();
        poolUpdate.setId(project.getId());
        poolUpdate.setInReviewPool(0);
        projectMapper.updateById(poolUpdate);

        log.info("专家提交点评：expertId={}, projectId={}, avg={}", expertUserId, project.getId(), review.getAvgScore());
        return review.getId();
    }

    /**
     * 自动质量校验：评语过短或四项评分全部相同且无优缺点拆分 -> 嫌疑敷衍
     */
    private int autoQualityCheck(ReviewSubmitDTO dto) {
        boolean sameScores = Objects.equals(dto.getScoreCompletion(), dto.getScoreNormative())
                && Objects.equals(dto.getScoreNormative(), dto.getScoreInnovation())
                && Objects.equals(dto.getScoreInnovation(), dto.getScoreTechnical());
        boolean noDetail = StrUtil.isBlank(dto.getStrength()) && StrUtil.isBlank(dto.getWeakness());
        boolean shortComment = StrUtil.length(StrUtil.trim(dto.getComment())) < 80;
        if (sameScores && noDetail && shortComment) {
            return ExpertReview.QUALITY_SUSPECT;
        }
        return ExpertReview.QUALITY_VALID;
    }

    private void refreshProjectScore(Long projectId) {
        List<ExpertReview> reviews = expertReviewMapper.selectList(Wrappers.<ExpertReview>lambdaQuery()
                .eq(ExpertReview::getProjectId, projectId)
                .ne(ExpertReview::getQualityStatus, ExpertReview.QUALITY_SUSPECT));
        BigDecimal avg = BigDecimal.ZERO;
        if (!reviews.isEmpty()) {
            BigDecimal total = reviews.stream()
                    .map(ExpertReview::getAvgScore)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            avg = total.divide(BigDecimal.valueOf(reviews.size()), 2, RoundingMode.HALF_UP);
        }
        Project update = new Project();
        update.setId(projectId);
        update.setExpertAvgScore(avg);
        update.setExpertReviewCount(reviews.size());
        projectMapper.updateById(update);
    }

    private void refreshExpertLevel(ExpertProfile expert) {
        int reviews = expert.getTotalReviews() == null ? 0 : expert.getTotalReviews();
        String level = reviews >= 50 ? "GOLD" : reviews >= 20 ? "SILVER" : "BRONZE";
        expert.setLevel(level);
    }

    /* ==================== 查询 ==================== */

    @Override
    public ReviewVO detail(Long reviewId) {
        ExpertReview review = expertReviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BizException(ResultCode.REVIEW_NOT_FOUND);
        }
        return toVO(review, false);
    }

    @Override
    public List<ReviewVO> listByProject(Long projectId) {
        List<ExpertReview> reviews = expertReviewMapper.selectList(Wrappers.<ExpertReview>lambdaQuery()
                .eq(ExpertReview::getProjectId, projectId)
                .orderByDesc(ExpertReview::getCreateTime));
        boolean includeAll = Objects.equals(currentStudentIdOf(projectId), UserContext.getUserId());
        return reviews.stream()
                .filter(r -> includeAll || (r.getIsPublic() != null && r.getIsPublic() == 1))
                .map(r -> toVO(r, true))
                .toList();
    }

    private Long currentStudentIdOf(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        return project == null ? null : project.getStudentId();
    }

    @Override
    public IPage<ReviewVO> receivedByMe(Integer pageNum, Integer pageSize) {
        Long studentId = UserContext.getUserId();
        Page<ExpertReview> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        IPage<ExpertReview> result = expertReviewMapper.selectPage(page,
                Wrappers.<ExpertReview>lambdaQuery()
                        .eq(ExpertReview::getStudentId, studentId)
                        .orderByDesc(ExpertReview::getCreateTime));
        return result.convert(r -> toVO(r, true));
    }

    @Override
    public IPage<ReviewVO> myReviews(Integer pageNum, Integer pageSize) {
        Long expertId = UserContext.getUserId();
        Page<ExpertReview> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        IPage<ExpertReview> result = expertReviewMapper.selectPage(page,
                Wrappers.<ExpertReview>lambdaQuery()
                        .eq(ExpertReview::getExpertId, expertId)
                        .orderByDesc(ExpertReview::getCreateTime));
        return result.convert(r -> toVO(r, true));
    }

    @Override
    public Map<String, Object> expertStatistics() {
        Long expertId = UserContext.getUserId();
        ExpertProfile expert = expertProfileMapper.selectOne(
                Wrappers.<ExpertProfile>lambdaQuery().eq(ExpertProfile::getUserId, expertId));

        Map<String, Object> result = new LinkedHashMap<>();
        long today = expertReviewMapper.selectCount(Wrappers.<ExpertReview>lambdaQuery()
                .eq(ExpertReview::getExpertId, expertId)
                .ge(ExpertReview::getCreateTime, LocalDate.now().atStartOfDay()));
        long total = expertReviewMapper.selectCount(Wrappers.<ExpertReview>lambdaQuery()
                .eq(ExpertReview::getExpertId, expertId));
        List<ExpertReview> all = expertReviewMapper.selectList(Wrappers.<ExpertReview>lambdaQuery()
                .eq(ExpertReview::getExpertId, expertId));

        BigDecimal avg = all.isEmpty() ? BigDecimal.ZERO
                : all.stream().map(ExpertReview::getAvgScore).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(all.size()), 2, RoundingMode.HALF_UP);

        result.put("todayReviews", today);
        result.put("dailyQuota", expert == null ? 0 : expert.getDailyQuota());
        result.put("totalReviews", total);
        result.put("avgGivenScore", avg);
        result.put("thanksCount", expert == null ? 0 : expert.getThanksCount());
        result.put("level", expert == null ? "BRONZE" : expert.getLevel());
        result.put("points", expert == null ? 0 : expert.getPoints());
        return result;
    }

    /* ==================== 互动 ==================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void thanks(Long reviewId) {
        ExpertReview review = expertReviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BizException(ResultCode.REVIEW_NOT_FOUND);
        }
        if (!Objects.equals(review.getStudentId(), UserContext.getUserId())) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        if (review.getThanksFlag() != null && review.getThanksFlag() == 1) {
            throw new BizException(ResultCode.FAIL.getCode(), "已致谢过该点评");
        }

        ExpertReview update = new ExpertReview();
        update.setId(reviewId);
        update.setThanksFlag(1);
        expertReviewMapper.updateById(update);

        ExpertProfile expert = expertProfileMapper.selectOne(
                Wrappers.<ExpertProfile>lambdaQuery().eq(ExpertProfile::getUserId, review.getExpertId()));
        if (expert != null) {
            ExpertProfile expertUpdate = new ExpertProfile();
            expertUpdate.setId(expert.getId());
            expertUpdate.setThanksCount((expert.getThanksCount() == null ? 0 : expert.getThanksCount()) + 1);
            expertUpdate.setPoints((expert.getPoints() == null ? 0 : expert.getPoints()) + 5);
            expertProfileMapper.updateById(expertUpdate);
        }
        log.info("学生致谢点评：reviewId={}", reviewId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long reply(ReviewReplyDTO dto) {
        ExpertReview review = expertReviewMapper.selectById(dto.getReviewId());
        if (review == null) {
            throw new BizException(ResultCode.REVIEW_NOT_FOUND);
        }
        Long userId = UserContext.getUserId();

        ReviewReply reply = new ReviewReply();
        reply.setReviewId(review.getId());
        reply.setUserId(userId);
        reply.setRole(UserContext.getRole());
        reply.setContent(dto.getContent());
        reviewReplyMapper.insert(reply);
        log.info("点评回应：reviewId={}, userId={}", review.getId(), userId);
        return reply.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long appeal(ReviewAppealDTO dto) {
        ExpertReview review = expertReviewMapper.selectById(dto.getReviewId());
        if (review == null) {
            throw new BizException(ResultCode.REVIEW_NOT_FOUND);
        }
        if (!Objects.equals(review.getStudentId(), UserContext.getUserId())) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        if (review.getAppealStatus() != null && review.getAppealStatus() == 1) {
            throw new BizException(ResultCode.APPEAL_PROCESSING);
        }

        ReviewAppeal appeal = new ReviewAppeal();
        appeal.setReviewId(review.getId());
        appeal.setStudentId(UserContext.getUserId());
        appeal.setReason(dto.getReason());
        appeal.setEvidenceUrl(dto.getEvidenceUrl());
        appeal.setStatus(ReviewAppeal.STATUS_PENDING);
        reviewAppealMapper.insert(appeal);

        ExpertReview update = new ExpertReview();
        update.setId(review.getId());
        update.setAppealStatus(1);
        expertReviewMapper.updateById(update);

        log.info("学生发起点评申诉：reviewId={}, appealId={}", review.getId(), appeal.getId());
        return appeal.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invite(ReviewInviteDTO dto) {
        if (dto.getProjectId() == null) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "缺少项目 ID");
        }
        Project project = projectMapper.selectById(dto.getProjectId());
        if (project == null) {
            throw new BizException(ResultCode.PROJECT_NOT_FOUND);
        }
        if (!Objects.equals(project.getStudentId(), UserContext.getUserId())) {
            throw new BizException(ResultCode.PROJECT_NO_PERMISSION);
        }
        // 邀请记录复用 track_event 埋点，便于后续扩展站内信
        log.info("学生邀请专家点评：projectId={}, expertUserId={}, expertId={}",
                dto.getProjectId(), dto.getExpertUserId(), dto.getExpertId());
    }

    /* ==================== 申诉管理 ==================== */

    @Override
    public IPage<Map<String, Object>> appealPage(Integer pageNum, Integer pageSize, Integer status) {
        Page<ReviewAppeal> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        IPage<ReviewAppeal> result = reviewAppealMapper.selectPage(page,
                Wrappers.<ReviewAppeal>lambdaQuery()
                        .eq(status != null, ReviewAppeal::getStatus, status)
                        .orderByAsc(ReviewAppeal::getStatus)
                        .orderByDesc(ReviewAppeal::getCreateTime));

        return result.convert(appeal -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", appeal.getId());
            map.put("reviewId", appeal.getReviewId());
            map.put("studentId", appeal.getStudentId());
            map.put("reason", appeal.getReason());
            map.put("evidenceUrl", appeal.getEvidenceUrl());
            map.put("status", appeal.getStatus());
            map.put("handleRemark", appeal.getHandleRemark());
            map.put("createTime", appeal.getCreateTime());

            SysUser student = sysUserMapper.selectById(appeal.getStudentId());
            map.put("studentName", student == null ? null
                    : StrUtil.blankToDefault(student.getNickname(), student.getUsername()));

            ExpertReview review = expertReviewMapper.selectById(appeal.getReviewId());
            if (review != null) {
                map.put("projectId", review.getProjectId());
                map.put("expertId", review.getExpertId());
                map.put("avgScore", review.getAvgScore());
                map.put("comment", review.getComment());
                Project project = projectMapper.selectById(review.getProjectId());
                map.put("projectName", project == null ? null : project.getName());
            }
            return map;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAppeal(Long appealId, String status, String remark) {
        ReviewAppeal appeal = reviewAppealMapper.selectById(appealId);
        if (appeal == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "申诉记录不存在");
        }
        boolean accepted = "ACCEPT".equalsIgnoreCase(status);

        appeal.setStatus(accepted ? ReviewAppeal.STATUS_ACCEPTED : ReviewAppeal.STATUS_REJECTED);
        appeal.setHandleRemark(remark);
        appeal.setHandlerId(UserContext.getUserId());
        appeal.setHandleTime(LocalDateTime.now());
        reviewAppealMapper.updateById(appeal);

        ExpertReview reviewUpdate = new ExpertReview();
        reviewUpdate.setId(appeal.getReviewId());
        reviewUpdate.setAppealStatus(accepted ? 2 : 3);
        if (accepted) {
            // 申诉成立：该点评标记为嫌疑，不再计入项目均分
            reviewUpdate.setQualityStatus(ExpertReview.QUALITY_SUSPECT);
            reviewUpdate.setIsPublic(0);
        }
        expertReviewMapper.updateById(reviewUpdate);

        if (accepted) {
            Long projectId = projectIdOfReview(appeal.getReviewId());
            if (projectId != null) {
                refreshProjectScore(projectId);
            }
        }
        log.info("申诉处理完成：appealId={}, accepted={}", appealId, accepted);
    }

    private Long projectIdOfReview(Long reviewId) {
        ExpertReview review = expertReviewMapper.selectById(reviewId);
        return review == null ? null : review.getProjectId();
    }

    /* ==================== 转换 ==================== */

    private ReviewVO toVO(ExpertReview review, boolean withReplies) {
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

        SysUser student = sysUserMapper.selectById(review.getStudentId());
        if (student != null) {
            vo.setStudentName(StrUtil.blankToDefault(student.getNickname(), student.getUsername()));
        }
        Project project = projectMapper.selectById(review.getProjectId());
        if (project != null) {
            vo.setProjectName(project.getName());
        }

        if (withReplies) {
            List<ReviewReply> replies = reviewReplyMapper.selectList(Wrappers.<ReviewReply>lambdaQuery()
                    .eq(ReviewReply::getReviewId, review.getId())
                    .orderByAsc(ReviewReply::getCreateTime));
            vo.setReplies(replies.stream().map(this::toReplyVO).toList());
        } else {
            vo.setReplies(Collections.emptyList());
        }
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
}
