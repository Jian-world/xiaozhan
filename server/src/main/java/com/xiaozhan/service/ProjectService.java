package com.xiaozhan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaozhan.dto.ProjectSaveDTO;
import com.xiaozhan.entity.ProjectAsset;
import com.xiaozhan.vo.ProjectVO;

import java.util.List;
import java.util.Map;

/**
 * 项目作品集服务
 */
public interface ProjectService {

    /**
     * 新建项目（草稿）
     */
    Long create(ProjectSaveDTO dto);

    /**
     * 编辑项目
     */
    void update(ProjectSaveDTO dto);

    /**
     * 删除项目（逻辑删除，级联素材与协作者）
     */
    void delete(Long projectId);

    /**
     * 项目详情（含素材、协作者、点评）
     *
     * @param projectId 项目 ID
     * @param forCompany 是否企业视角（会记录查验埋点）
     */
    ProjectVO detail(Long projectId, boolean forCompany);

    /**
     * 发布项目（草稿 -> 已发布，进入内容审核队列）
     */
    void publish(Long projectId);

    /**
     * 下架项目（已发布 -> 草稿）
     */
    void unpublish(Long projectId);

    /**
     * 我的项目列表
     */
    IPage<ProjectVO> myProjects(Integer pageNum, Integer pageSize, Integer publishStatus);

    /**
     * 公开作品集（按学生 ID）
     */
    List<ProjectVO> portfolioOf(Long studentId);

    /**
     * 项目广场 / 企业检索
     *
     * @param keyword  关键词
     * @param category 方向
     * @param sortBy   latest / score / views
     */
    IPage<ProjectVO> search(Integer pageNum, Integer pageSize, String keyword, String category, String sortBy);

    /**
     * 求点评池（专家视角）
     */
    IPage<ProjectVO> reviewPool(Integer pageNum, Integer pageSize, String category);

    /**
     * 加入 / 退出求点评池
     */
    void toggleReviewPool(Long projectId, boolean join);

    /**
     * 上传 / 登记项目素材
     */
    List<ProjectAsset> saveAsset(Long projectId, ProjectAsset asset);

    /**
     * 删除素材
     */
    void deleteAsset(Long assetId);

    /**
     * 统计数据（学生工作台）
     */
    Map<String, Object> statistics(Long studentId);
}
