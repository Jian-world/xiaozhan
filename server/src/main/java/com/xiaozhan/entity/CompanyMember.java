package com.xiaozhan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 企业子账号
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("company_member")
public class CompanyMember extends BaseEntity {

    private Long companyId;

    private Long userId;

    /** HR / INTERVIEWER / ADMIN */
    private String memberRole;
}
