package com.xiaozhan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 候选人收藏
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("candidate_favorite")
public class CandidateFavorite extends BaseEntity {

    private Long companyId;

    private Long studentId;

    private String listName;

    private String remark;
}
