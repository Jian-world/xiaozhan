package com.xiaozhan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件上传返回
 */
@Data
@Schema(description = "文件上传返回")
public class UploadVO {

    @Schema(description = "可访问的相对地址，如 /files/project/2026/09/uuid.png")
    private String url;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "文件体积（字节）")
    private Long size;

    @Schema(description = "扩展名，如 png / zip")
    private String ext;

    @Schema(description = "业务类型")
    private String bizType;
}
