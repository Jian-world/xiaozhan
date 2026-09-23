package com.xiaozhan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaozhan.dto.CompanyVerifyDTO;
import com.xiaozhan.dto.EduVerifyDTO;
import com.xiaozhan.dto.ExpertVerifyDTO;
import com.xiaozhan.dto.VerifyAuditDTO;
import com.xiaozhan.vo.VerifyRecordVO;

/**
 * 认证服务：学籍认证 / 专家认证 / 企业认证
 */
public interface VerifyService {

    /**
     * 学生提交学籍认证
     */
    void submitEduVerify(EduVerifyDTO dto);

    /**
     * 专家提交认证
     */
    void submitExpertVerify(ExpertVerifyDTO dto);

    /**
     * 企业提交认证
     */
    void submitCompanyVerify(CompanyVerifyDTO dto);

    /**
     * 我的认证记录
     */
    java.util.List<VerifyRecordVO> myRecords();

    /**
     * 管理员：认证审核列表
     */
    IPage<VerifyRecordVO> pageRecords(Integer pageNum, Integer pageSize, String bizType, Integer status);

    /**
     * 管理员：审核
     */
    void audit(VerifyAuditDTO dto);
}
