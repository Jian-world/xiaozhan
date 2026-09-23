package com.xiaozhan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaozhan.vo.ProjectVO;

import java.util.List;
import java.util.Map;

/**
 * 平台运营服务（管理员）
 */
public interface AdminService {

    /**
     * 平台总览统计
     */
    Map<String, Object> overview();

    /**
     * 用户列表
     */
    IPage<Map<String, Object>> users(Integer pageNum, Integer pageSize, String role, Integer status, String keyword);

    /**
     * 启用 / 禁用用户
     */
    void toggleUserStatus(Long userId, boolean enable);

    /**
     * 重置用户密码
     */
    String resetPassword(Long userId);

    /**
     * 内容审核列表
     */
    IPage<Map<String, Object>> contentAudits(Integer pageNum, Integer pageSize, Integer status);

    /**
     * 内容审核处理
     */
    void handleAudit(Long auditId, boolean pass, String remark);

    /**
     * 项目列表（管理员视角，全量）
     */
    IPage<ProjectVO> projects(Integer pageNum, Integer pageSize, Integer reviewStatus);

    /**
     * 字典项
     */
    List<Map<String, Object>> dict(String dictType);

    /**
     * 埋点事件列表
     */
    IPage<Map<String, Object>> trackEvents(Integer pageNum, Integer pageSize, String eventCode);
}
