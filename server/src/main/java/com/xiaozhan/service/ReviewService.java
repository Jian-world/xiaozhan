package com.xiaozhan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaozhan.dto.ReviewAppealDTO;
import com.xiaozhan.dto.ReviewInviteDTO;
import com.xiaozhan.dto.ReviewReplyDTO;
import com.xiaozhan.dto.ReviewSubmitDTO;
import com.xiaozhan.vo.ReviewVO;

import java.util.List;
import java.util.Map;

/**
 * 点评服务
 */
public interface ReviewService {

    /**
     * 专家提交点评（含质量校验、额度扣减、项目均分回写）
     */
    Long submit(ReviewSubmitDTO dto);

    /**
     * 点评详情
     */
    ReviewVO detail(Long reviewId);

    /**
     * 项目下的点评列表
     */
    List<ReviewVO> listByProject(Long projectId);

    /**
     * 我收到的点评（学生）
     */
    IPage<ReviewVO> receivedByMe(Integer pageNum, Integer pageSize);

    /**
     * 我发出的点评（专家）
     */
    IPage<ReviewVO> myReviews(Integer pageNum, Integer pageSize);

    /**
     * 专家工作台统计
     */
    Map<String, Object> expertStatistics();

    /**
     * 学生致谢点评
     */
    void thanks(Long reviewId);

    /**
     * 学生回应点评
     */
    Long reply(ReviewReplyDTO dto);

    /**
     * 学生对点评发起申诉
     */
    Long appeal(ReviewAppealDTO dto);

    /**
     * 邀请专家定向点评（学生发起，写入邀请记录并生成占位点评）
     */
    void invite(ReviewInviteDTO dto);

    /**
     * 管理员：申诉处理列表
     */
    IPage<Map<String, Object>> appealPage(Integer pageNum, Integer pageSize, Integer status);

    /**
     * 管理员：处理申诉
     */
    void handleAppeal(Long appealId, String status, String remark);
}
