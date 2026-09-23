package com.xiaozhan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 项目素材
 */
@Data
@Schema(description = "项目素材")
public class ProjectAssetVO {

    @Schema(description = "素材 ID")
    private Long id;

    @Schema(description = "类型：SOURCE/DOC/VIDEO/IMAGE")
    private String assetType;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "访问地址")
    private String fileUrl;

    @Schema(description = "文件体积（字节）")
    private Long fileSize;

    @Schema(description = "扩展名")
    private String fileExt;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "可读体积，如 12.4 MB")
    private String readableSize;
}
