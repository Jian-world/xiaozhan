package com.xiaozhan.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaozhan.common.ResultCode;
import com.xiaozhan.dto.CompanyVerifyDTO;
import com.xiaozhan.dto.EduVerifyDTO;
import com.xiaozhan.dto.ExpertVerifyDTO;
import com.xiaozhan.dto.VerifyAuditDTO;
import com.xiaozhan.entity.*;
import com.xiaozhan.exception.BizException;
import com.xiaozhan.mapper.*;
import com.xiaozhan.security.UserContext;
import com.xiaozhan.service.VerifyService;
import com.xiaozhan.vo.VerifyRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyServiceImpl implements VerifyService {

    private final VerifyRecordMapper verifyRecordMapper;

    private final StudentProfileMapper studentProfileMapper;

    private final ExpertProfileMapper expertProfileMapper;

    private final CompanyProfileMapper companyProfileMapper;

    private final SysUserMapper sysUserMapper;

    /* ==================== 提交认证 ==================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitEduVerify(EduVerifyDTO dto) {
        Long userId = UserContext.getUserId();
        StudentProfile profile = studentProfileMapper.selectOne(
                Wrappers.<StudentProfile>lambdaQuery().eq(StudentProfile::getUserId, userId));
        if (profile == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND.getCode(), "学生档案不存在");
        }
        if (profile.getEduVerified() != null && profile.getEduVerified() == StudentProfile.VERIFY_PENDING) {
            throw new BizException(ResultCode.VERIFY_PENDING);
        }

        profile.setSchool(dto.getSchool());
        profile.setMajor(dto.getMajor());
        if (StrUtil.isNotBlank(dto.getMajorCategory())) {
            profile.setMajorCategory(dto.getMajorCategory());
        }
        profile.setDegree(dto.getDegree());
        profile.setGraduateYear(dto.getGraduateYear());
        profile.setEduVerifyFile(dto.getEduVerifyFile());
        profile.setEduVerified(StudentProfile.VERIFY_PENDING);
        profile.setEduVerifyRemark(null);
        studentProfileMapper.updateById(profile);

        insertPendingRecord(VerifyRecord.BIZ_STUDENT, profile.getId(), userId, dto.getEduVerifyFile());
        log.info("学生提交学籍认证：userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitExpertVerify(ExpertVerifyDTO dto) {
        Long userId = UserContext.getUserId();
        ExpertProfile profile = expertProfileMapper.selectOne(
                Wrappers.<ExpertProfile>lambdaQuery().eq(ExpertProfile::getUserId, userId));
        if (profile == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND.getCode(), "专家档案不存在");
        }
        if (profile.getVerifyStatus() != null && profile.getVerifyStatus() == ExpertProfile.STATUS_PENDING) {
            throw new BizException(ResultCode.VERIFY_PENDING);
        }

        profile.setOrgName(dto.getOrgName());
        profile.setPosition(dto.getPosition());
        profile.setExpertType(dto.getExpertType());
        profile.setDomain(dto.getDomain());
        profile.setVerifyFile(dto.getVerifyFile());
        profile.setVerifyStatus(ExpertProfile.STATUS_PENDING);
        profile.setVerifyRemark(null);
        expertProfileMapper.updateById(profile);

        insertPendingRecord(VerifyRecord.BIZ_EXPERT, profile.getId(), userId, dto.getVerifyFile());
        log.info("专家提交认证：userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitCompanyVerify(CompanyVerifyDTO dto) {
        Long userId = UserContext.getUserId();
        CompanyProfile profile = companyProfileMapper.selectOne(
                Wrappers.<CompanyProfile>lambdaQuery().eq(CompanyProfile::getUserId, userId));
        if (profile == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND.getCode(), "企业档案不存在");
        }
        if (profile.getVerifyStatus() != null && profile.getVerifyStatus() == CompanyProfile.STATUS_PENDING) {
            throw new BizException(ResultCode.VERIFY_PENDING);
        }

        profile.setCompanyName(dto.getCompanyName());
        profile.setIndustry(dto.getIndustry());
        profile.setScale(dto.getScale());
        profile.setLicenseFile(dto.getLicenseFile());
        profile.setVerifyStatus(CompanyProfile.STATUS_PENDING);
        profile.setVerifyRemark(null);
        companyProfileMapper.updateById(profile);

        insertPendingRecord(VerifyRecord.BIZ_COMPANY, profile.getId(), userId, dto.getLicenseFile());
        log.info("企业提交认证：userId={}", userId);
    }

    private void insertPendingRecord(String bizType, Long bizId, Long applicantId, String file) {
        VerifyRecord record = new VerifyRecord();
        record.setBizType(bizType);
        record.setBizId(bizId);
        record.setApplicantId(applicantId);
        record.setSubmitFile(file);
        record.setStatus(VerifyRecord.STATUS_PENDING);
        verifyRecordMapper.insert(record);
    }

    /* ==================== 查询 ==================== */

    @Override
    public List<VerifyRecordVO> myRecords() {
        Long userId = UserContext.getUserId();
        List<VerifyRecord> records = verifyRecordMapper.selectList(
                Wrappers.<VerifyRecord>lambdaQuery()
                        .eq(VerifyRecord::getApplicantId, userId)
                        .orderByDesc(VerifyRecord::getCreateTime));
        return records.stream().map(this::toVO).toList();
    }

    @Override
    public IPage<VerifyRecordVO> pageRecords(Integer pageNum, Integer pageSize, String bizType, Integer status) {
        Page<VerifyRecord> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        IPage<VerifyRecord> result = verifyRecordMapper.selectPage(page,
                Wrappers.<VerifyRecord>lambdaQuery()
                        .eq(StrUtil.isNotBlank(bizType), VerifyRecord::getBizType, bizType)
                        .eq(status != null, VerifyRecord::getStatus, status)
                        .orderByAsc(VerifyRecord::getStatus)
                        .orderByDesc(VerifyRecord::getCreateTime));
        return result.convert(this::toVO);
    }

    private VerifyRecordVO toVO(VerifyRecord record) {
        VerifyRecordVO vo = new VerifyRecordVO();
        vo.setId(record.getId());
        vo.setBizType(record.getBizType());
        vo.setBizId(record.getBizId());
        vo.setApplicantId(record.getApplicantId());
        vo.setSubmitFile(record.getSubmitFile());
        vo.setStatus(record.getStatus());
        vo.setRemark(record.getRemark());
        vo.setHandleTime(record.getHandleTime());
        vo.setCreateTime(record.getCreateTime());

        SysUser user = sysUserMapper.selectById(record.getApplicantId());
        if (user != null) {
            vo.setApplicantName(StrUtil.blankToDefault(user.getRealName(), user.getNickname()));
        }
        vo.setSummary(resolveSummary(record));
        return vo;
    }

    private String resolveSummary(VerifyRecord record) {
        return switch (record.getBizType()) {
            case VerifyRecord.BIZ_STUDENT -> {
                StudentProfile p = studentProfileMapper.selectById(record.getBizId());
                yield p == null ? null : p.getSchool() + " · " + p.getMajor();
            }
            case VerifyRecord.BIZ_EXPERT -> {
                ExpertProfile p = expertProfileMapper.selectById(record.getBizId());
                yield p == null ? null : p.getOrgName() + " · " + p.getPosition();
            }
            case VerifyRecord.BIZ_COMPANY -> {
                CompanyProfile p = companyProfileMapper.selectById(record.getBizId());
                yield p == null ? null : p.getCompanyName() + " · " + p.getIndustry();
            }
            default -> null;
        };
    }

    /* ==================== 审核 ==================== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(VerifyAuditDTO dto) {
        VerifyRecord record = verifyRecordMapper.selectById(dto.getRecordId());
        if (record == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "认证记录不存在");
        }
        if (record.getStatus() != null && record.getStatus() != VerifyRecord.STATUS_PENDING) {
            throw new BizException(ResultCode.FAIL.getCode(), "该认证已处理，请勿重复操作");
        }

        boolean pass = "PASS".equalsIgnoreCase(dto.getStatus());
        if (!pass && StrUtil.isBlank(dto.getRemark())) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "驳回时必须填写审核意见");
        }

        record.setStatus(pass ? VerifyRecord.STATUS_PASSED : VerifyRecord.STATUS_REJECTED);
        record.setRemark(dto.getRemark());
        record.setHandlerId(UserContext.getUserId());
        record.setHandleTime(LocalDateTime.now());
        verifyRecordMapper.updateById(record);

        // 同步业务主体状态
        switch (record.getBizType()) {
            case VerifyRecord.BIZ_STUDENT -> {
                StudentProfile p = studentProfileMapper.selectById(record.getBizId());
                if (p != null) {
                    p.setEduVerified(pass ? StudentProfile.VERIFY_PASSED : StudentProfile.VERIFY_REJECTED);
                    p.setEduVerifyRemark(pass ? null : dto.getRemark());
                    studentProfileMapper.updateById(p);
                }
            }
            case VerifyRecord.BIZ_EXPERT -> {
                ExpertProfile p = expertProfileMapper.selectById(record.getBizId());
                if (p != null) {
                    p.setVerifyStatus(pass ? ExpertProfile.STATUS_PASSED : ExpertProfile.STATUS_REJECTED);
                    p.setVerifyRemark(pass ? null : dto.getRemark());
                    expertProfileMapper.updateById(p);
                }
            }
            case VerifyRecord.BIZ_COMPANY -> {
                CompanyProfile p = companyProfileMapper.selectById(record.getBizId());
                if (p != null) {
                    p.setVerifyStatus(pass ? CompanyProfile.STATUS_PASSED : CompanyProfile.STATUS_REJECTED);
                    p.setVerifyRemark(pass ? null : dto.getRemark());
                    if (pass) {
                        p.setVerifiedAt(LocalDateTime.now());
                    }
                    companyProfileMapper.updateById(p);
                }
            }
            default -> {
            }
        }
        log.info("认证审核完成：recordId={}, pass={}", record.getId(), pass);
    }

    /**
     * 认证状态文案映射
     */
    public static Map<Integer, String> statusText() {
        Map<Integer, String> map = new HashMap<>(4);
        map.put(VerifyRecord.STATUS_PENDING, "待审核");
        map.put(VerifyRecord.STATUS_PASSED, "已通过");
        map.put(VerifyRecord.STATUS_REJECTED, "已驳回");
        return map;
    }
}
