package com.xiaozhan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 埋点事件
 */
@Data
@TableName("track_event")
public class TrackEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String eventCode;

    private Long userId;

    private String role;

    private String major;

    private String projectType;

    private String extra;

    private LocalDateTime createTime;
}
