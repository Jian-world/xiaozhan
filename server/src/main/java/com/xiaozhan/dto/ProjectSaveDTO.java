package com.xiaozhan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 项目保存请求（新建 / 编辑共用）
 */
@Data
@Schema(description = "项目保存请求")
public class ProjectSaveDTO {

    @Schema(description = "项目 ID，编辑时传")
    private Long id;

    @NotBlank(message = "请填写项目名称")
    @Size(max = 100, message = "项目名称不超过 100 字")
    @Schema(description = "项目名称", example = "校园二手交易平台")
    private String name;

    @NotBlank(message = "请选择项目类型")
    @Schema(description = "项目类型：COURSE/COMPETITION/INTERNSHIP/PERSONAL/GRADUATION")
    private String projectType;

    @NotBlank(message = "请选择项目方向")
    @Schema(description = "方向：CS/DESIGN/ECON/OTHER")
    private String category;

    @Size(max = 200, message = "一句话简介不超过 200 字")
    @Schema(description = "一句话简介")
    private String summary;

    @Schema(description = "担任角色 / 分工说明")
    private String roleDesc;

    @Schema(description = "技术栈，逗号分隔", example = "SpringBoot,MySQL,Vue3")
    private String techStack;

    @Schema(description = "技术难点与亮点（富文本 HTML）")
    private String highlight;

    @Schema(description = "仓库地址")
    private String repoUrl;

    @Schema(description = "仓库平台：GITHUB/GITEE/OTHER")
    private String repoPlatform;

    @Schema(description = "开始时间")
    private LocalDate startDate;

    @Schema(description = "结束时间")
    private LocalDate endDate;

    @Schema(description = "封面图地址")
    private String coverUrl;

    @Schema(description = "可见性：PUBLIC/LINK/PRIVATE")
    private String visibility;

    @Schema(description = "是否在求点评池中：0-否 1-是")
    private Integer inReviewPool;

    @Schema(description = "协作者学生 ID 列表")
    private List<Long> memberIds;
}
