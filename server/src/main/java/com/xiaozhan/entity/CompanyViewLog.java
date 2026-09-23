package com.xiaozhan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业查验记录（只有创建时间，无需逻辑删除）
 */
@Data
@TableName("company_view_log")
public class CompanyViewLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long companyId;

    private Long viewerUserId;

    private Long studentId;

    private Long projectId;

    /** PORTFOLIO / PROJECT / SEARCH */
    private String viewType;

    private Integer quotaCost;

    private LocalDateTime createTime;

    public static final String TYPE_PORTFOLIO = "PORTFOLIO";
    public static final String TYPE_PROJECT = "PROJECT";
    public static final String TYPE_SEARCH = "SEARCH";
}
