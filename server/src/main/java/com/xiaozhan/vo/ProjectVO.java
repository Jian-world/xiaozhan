package com.xiaozhan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目详情
 */
@Data
@Schema(description = "项目详情")
public class ProjectVO {

    @Schema(description = "项目 ID")
    private Long id;

    @Schema(description = "所属学生 ID")
    private Long studentId;

    @Schema(description = "学生昵称")
    private String studentName;

    @Schema(description = "学生头像")
    private String studentAvatar;

    @Schema(description = "学校")
    private String studentSchool;

    @Schema(description = "专业")
    private String studentMajor;

    @Schema(description = "学籍认证状态")
    private Integer eduVerified;

    @Schema(description = "项目名称")
    private String name;

    @Schema(description = "项目类型")
    private String projectType;

    @Schema(description = "方向")
    private String category;

    @Schema(description = "一句话简介")
    private String summary;

    @Schema(description = "角色说明")
    private String roleDesc;

    @Schema(description = "技术栈列表")
    private List<String> techStack;

    @Schema(description = "技术难点与亮点")
    private String highlight;

    @Schema(description = "仓库地址")
    private String repoUrl;

    @Schema(description = "仓库平台")
    private String repoPlatform;

    @Schema(description = "开始时间")
    private LocalDate startDate;

    @Schema(description = "结束时间")
    private LocalDate endDate;

    @Schema(description = "封面图")
    private String coverUrl;

    @Schema(description = "可见性")
    private String visibility;

    @Schema(description = "发布状态：0-草稿 1-已发布")
    private Integer publishStatus;

    @Schema(description = "审核状态：0-待审核 1-通过 2-驳回")
    private Integer reviewStatus;

    @Schema(description = "是否在求点评池")
    private Integer inReviewPool;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "专家平均分")
    private BigDecimal expertAvgScore;

    @Schema(description = "专家点评数")
    private Integer expertReviewCount;

    @Schema(description = "素材列表")
    private List<ProjectAssetVO> assets;

    @Schema(description = "协作者列表")
    private List<ProjectMemberVO> members;

    @Schema(description = "点评列表")
    private List<ReviewVO> reviews;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "当前登录者是否为项目所有者")
    private Boolean owner;
}
