package com.xiaozhan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaozhan.dto.CandidateQueryDTO;
import com.xiaozhan.dto.FavoriteDTO;
import com.xiaozhan.dto.InvitationDTO;
import com.xiaozhan.vo.CandidateVO;
import com.xiaozhan.vo.ProjectBriefVO;

import java.util.List;
import java.util.Map;

/**
 * 企业端服务：候选人检索、收藏、邀约、查验额度
 */
public interface CandidateService {

    /**
     * 候选人检索（仅返回允许企业检索且已发布作品的学生）
     */
    IPage<CandidateVO> search(CandidateQueryDTO query, Integer pageNum, Integer pageSize);

    /**
     * 候选人详情（含完整作品集）
     */
    Map<String, Object> candidateDetail(Long studentId);

    /**
     * 收藏 / 取消收藏
     */
    void favorite(FavoriteDTO dto);

    /**
     * 取消收藏
     */
    void unfavorite(Long studentId);

    /**
     * 我的收藏列表
     */
    IPage<CandidateVO> favorites(Integer pageNum, Integer pageSize, String listName);

    /**
     * 发送邀约
     */
    Long sendInvitation(InvitationDTO dto);

    /**
     * 企业发出的邀约
     */
    IPage<Map<String, Object>> sentInvitations(Integer pageNum, Integer pageSize);

    /**
     * 学生收到的邀约
     */
    IPage<Map<String, Object>> receivedInvitations(Integer pageNum, Integer pageSize);

    /**
     * 学生回应邀约
     */
    void replyInvitation(Long invitationId, boolean accept, String contactInfo);

    /**
     * 企业工作台统计
     */
    Map<String, Object> companyStatistics();

    /**
     * 查验记录
     */
    IPage<Map<String, Object>> viewLogs(Integer pageNum, Integer pageSize);
}
