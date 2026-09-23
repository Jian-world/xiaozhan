package com.xiaozhan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 项目素材
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("project_asset")
public class ProjectAsset extends BaseEntity {

    private Long projectId;

    /** SOURCE/DOC/VIDEO/IMAGE */
    private String assetType;

    private String fileName;

    private String fileUrl;

    private Long fileSize;

    private String fileExt;

    private Integer sortOrder;

    /** 扩展信息 JSON */
    private String extJson;

    public static final String TYPE_SOURCE = "SOURCE";
    public static final String TYPE_DOC = "DOC";
    public static final String TYPE_VIDEO = "VIDEO";
    public static final String TYPE_IMAGE = "IMAGE";
}
