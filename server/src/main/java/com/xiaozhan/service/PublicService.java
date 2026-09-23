package com.xiaozhan.service;

import java.util.Map;

/**
 * 公开信息查询（无需登录）
 */
public interface PublicService {

    /**
     * 学生公开档案（作品集页头部使用）
     *
     * @param studentId 学生用户 ID
     */
    Map<String, Object> studentPublicProfile(Long studentId);

    /**
     * 专家广场列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param expertType 专家类型筛选
     */
    Map<String, Object> expertHall(Integer pageNum, Integer pageSize, String expertType);
}
