package com.xiaozhan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 当前登录用户信息
 */
@Data
@Schema(description = "当前登录用户信息")
public class UserInfoVO {

    @Schema(description = "用户 ID")
    private Long id;

    @Schema(description = "登录账号")
    private String username;

    @Schema(description = "角色：STUDENT/EXPERT/COMPANY/ADMIN")
    private String role;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "账号状态：0-禁用 1-正常")
    private Integer status;

    @Schema(description = "最近登录时间")
    private LocalDateTime lastLoginAt;

    @Schema(description = "学生档案（role=STUDENT 时返回）")
    private StudentProfileVO studentProfile;

    @Schema(description = "专家档案（role=EXPERT 时返回）")
    private ExpertProfileVO expertProfile;

    @Schema(description = "企业档案（role=COMPANY 时返回）")
    private CompanyProfileVO companyProfile;

    /**
     * 学生档案
     */
    @Data
    @Schema(description = "学生档案")
    public static class StudentProfileVO {

        @Schema(description = "学校")
        private String school;

        @Schema(description = "专业")
        private String major;

        @Schema(description = "专业大类：CS/DESIGN/ECON/OTHER")
        private String majorCategory;

        @Schema(description = "学历")
        private String degree;

        @Schema(description = "毕业年份")
        private Integer graduateYear;

        @Schema(description = "学籍认证状态：0-未认证 1-待审核 2-已认证 3-已驳回")
        private Integer eduVerified;

        @Schema(description = "认证驳回原因")
        private String eduVerifyRemark;

        @Schema(description = "技能标签")
        private List<String> skillTags;

        @Schema(description = "一句话自述")
        private String bio;

        @Schema(description = "是否允许企业检索")
        private Integer allowCompanySearch;

        @Schema(description = "作品集被查看次数")
        private Integer portfolioViews;
    }

    /**
     * 专家档案
     */
    @Data
    @Schema(description = "专家档案")
    public static class ExpertProfileVO {

        @Schema(description = "所在单位")
        private String orgName;

        @Schema(description = "职位")
        private String position;

        @Schema(description = "专家类型：TEACHER-高校导师 ENGINEER-企业工程师")
        private String expertType;

        @Schema(description = "擅长领域")
        private List<String> domainTags;

        @Schema(description = "是否公开单位信息")
        private Integer showOrg;

        @Schema(description = "专家认证状态：0-待提交 1-待审核 2-已通过 3-已驳回")
        private Integer verifyStatus;

        @Schema(description = "认证驳回原因")
        private String verifyRemark;

        @Schema(description = "今日剩余点评额度")
        private Integer dailyQuota;

        @Schema(description = "累计点评数")
        private Integer totalReviews;

        @Schema(description = "收到致谢数")
        private Integer thanksCount;

        @Schema(description = "点评质量分")
        private Double qualityScore;

        @Schema(description = "等级：BRONZE/SILVER/GOLD")
        private String level;

        @Schema(description = "积分")
        private Integer points;
    }

    /**
     * 企业档案
     */
    @Data
    @Schema(description = "企业档案")
    public static class CompanyProfileVO {

        @Schema(description = "企业名称")
        private String companyName;

        @Schema(description = "行业")
        private String industry;

        @Schema(description = "企业规模")
        private String scale;

        @Schema(description = "企业认证状态：0-待提交 1-待审核 2-已通过 3-已驳回")
        private Integer verifyStatus;

        @Schema(description = "认证驳回原因")
        private String verifyRemark;

        @Schema(description = "套餐：FREE/BASIC/PRO")
        private String packageType;

        @Schema(description = "本月剩余查验额度")
        private Integer remainQuota;

        @Schema(description = "套餐到期时间")
        private LocalDateTime packageExpire;
    }
}
