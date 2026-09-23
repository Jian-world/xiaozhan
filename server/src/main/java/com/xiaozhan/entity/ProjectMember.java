package com.xiaozhan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 项目协作者
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("project_member")
public class ProjectMember extends BaseEntity {

    private Long projectId;

    private Long studentId;

    /** 本人在项目中的分工 */
    private String memberRole;

    private Integer isOwner;
}
