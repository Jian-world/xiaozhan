package com.xiaozhan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 项目协作者
 */
@Data
@Schema(description = "项目协作者")
public class ProjectMemberVO {

    @Schema(description = "学生 ID")
    private Long studentId;

    @Schema(description = "学生昵称")
    private String studentName;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "学校")
    private String school;

    @Schema(description = "角色分工")
    private String memberRole;

    @Schema(description = "是否项目主理人：0-否 1-是")
    private Integer isOwner;
}
