package com.xiaozhan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 企业档案
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("company_profile")
public class CompanyProfile extends BaseEntity {

    private Long userId;

    private String companyName;

    private String industry;

    private String scale;

    private String licenseFile;

    /** 0-待提交 1-待审核 2-已通过 3-已驳回 */
    private Integer verifyStatus;

    private String verifyRemark;

    /** FREE / BASIC / PRO */
    private String packageType;

    private LocalDateTime packageExpire;

    private Integer monthQuota;

    private Integer monthUsed;

    private LocalDate quotaResetAt;

    private LocalDateTime verifiedAt;

    public static final int STATUS_NONE = 0;
    public static final int STATUS_PENDING = 1;
    public static final int STATUS_PASSED = 2;
    public static final int STATUS_REJECTED = 3;

    public static final String PKG_FREE = "FREE";
    public static final String PKG_BASIC = "BASIC";
    public static final String PKG_PRO = "PRO";

    public static final int FREE_QUOTA = 20;
    public static final int BASIC_QUOTA = 200;
    public static final int PRO_QUOTA = 9999;

    /**
     * 计算剩余额度
     */
    public int remainQuota() {
        int quota = monthQuota == null ? FREE_QUOTA : monthQuota;
        int used = monthUsed == null ? 0 : monthUsed;
        return Math.max(0, quota - used);
    }
}
