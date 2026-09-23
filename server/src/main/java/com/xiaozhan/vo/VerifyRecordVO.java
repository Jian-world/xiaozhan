package com.xiaozhan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 认证记录
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "认证记录")
public class VerifyRecordVO {

    @Schema(description = "记录 ID")
    private Long id;

    @Schema(description = "业务类型：STUDENT/EXPERT/COMPANY")
    private String bizType;

    @Schema(description = "业务主体 ID")
    private Long bizId;

    @Schema(description = "申请人 ID")
    private Long applicantId;

    @Schema(description = "申请人昵称")
    private String applicantName;

    @Schema(description = "提交材料地址")
    private String submitFile;

    @Schema(description = "状态：1-待审核 2-通过 3-驳回")
    private Integer status;

    @Schema(description = "审核意见")
    private String remark;

    @Schema(description = "审核时间")
    private LocalDateTime handleTime;

    @Schema(description = "提交时间")
    private LocalDateTime createTime;

    @Schema(description = "业务摘要（学校/单位/企业名）")
    private String summary;
}
