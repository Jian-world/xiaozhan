package com.xiaozhan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 专家档案更新
 */
@Data
@Schema(description = "专家档案更新")
public class ExpertProfileDTO {

    @Schema(description = "所在单位")
    private String orgName;

    @Schema(description = "职位")
    private String position;

    @Schema(description = "擅长领域列表")
    private List<String> domainTags;

    @Schema(description = "是否公开单位信息")
    private Integer showOrg;
}
