package com.xiaozhan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 学生档案
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("student_profile")
public class StudentProfile extends BaseEntity {

    private Long userId;

    private String school;

    private String major;

    /** CS / DESIGN / ECON / OTHER */
    private String majorCategory;

    private String degree;

    private Integer graduateYear;

    /** 0-未认证 1-待审核 2-已认证 3-已驳回 */
    private Integer eduVerified;

    private String eduVerifyFile;

    private String eduVerifyRemark;

    /** 技能标签，逗号分隔 */
    private String skillTags;

    private String bio;

    /** 是否允许企业检索：0-否 1-是 */
    private Integer allowCompanySearch;

    private Integer portfolioViews;

    public static final int VERIFY_NONE = 0;
    public static final int VERIFY_PENDING = 1;
    public static final int VERIFY_PASSED = 2;
    public static final int VERIFY_REJECTED = 3;
}
