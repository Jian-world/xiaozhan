package com.xiaozhan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("project")
public class Project extends BaseEntity {

    private Long studentId;

    private String name;

    /** COURSE/COMPETITION/INTERNSHIP/PERSONAL/GRADUATION */
    private String projectType;

    /** CS/DESIGN/ECON/OTHER */
    private String category;

    private String summary;

    private String roleDesc;

    private String techStack;

    /** 技术难点与亮点（富文本 HTML） */
    private String highlight;

    private String repoUrl;

    /** GITHUB/GITEE/OTHER */
    private String repoPlatform;

    private LocalDate startDate;

    private LocalDate endDate;

    private String coverUrl;

    /** PUBLIC/LINK/PRIVATE */
    private String visibility;

    /** 0-草稿 1-已发布 */
    private Integer publishStatus;

    /** 0-待审核 1-通过 2-驳回 */
    private Integer reviewStatus;

    /** 是否在求点评池 */
    private Integer inReviewPool;

    private LocalDateTime poolJoinTime;

    private Integer poolBoost;

    private Integer viewCount;

    private java.math.BigDecimal expertAvgScore;

    private Integer expertReviewCount;

    public static final int PUBLISH_DRAFT = 0;
    public static final int PUBLISH_DONE = 1;

    public static final int AUDIT_PENDING = 0;
    public static final int AUDIT_PASSED = 1;
    public static final int AUDIT_REJECTED = 2;

    public static final String VIS_PUBLIC = "PUBLIC";
    public static final String VIS_LINK = "LINK";
    public static final String VIS_PRIVATE = "PRIVATE";

    /**
     * 是否对企业可见（公开或仅链接可见）
     */
    public boolean visibleToCompany() {
        return VIS_PUBLIC.equals(visibility) || VIS_LINK.equals(visibility);
    }
}
