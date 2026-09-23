package com.xiaozhan.common;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    /* 客户端错误 */
    PARAM_ERROR(400, "参数校验失败"),
    UNAUTHORIZED(401, "登录状态已失效，请重新登录"),
    FORBIDDEN(403, "没有操作权限"),
    NOT_FOUND(404, "请求的资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),

    /* 业务错误 */
    USERNAME_EXISTS(1001, "该账号已被注册"),
    USER_NOT_FOUND(1002, "用户不存在"),
    PASSWORD_ERROR(1003, "账号或密码错误"),
    USER_DISABLED(1004, "账号已被禁用，请联系平台运营"),
    OLD_PASSWORD_ERROR(1005, "原密码不正确"),

    EDU_NOT_VERIFIED(1101, "请先完成学籍认证"),
    EXPERT_NOT_VERIFIED(1102, "专家认证通过后才可使用该功能"),
    COMPANY_NOT_VERIFIED(1103, "企业认证通过后才可使用该功能"),
    VERIFY_PENDING(1104, "认证申请审核中，请耐心等待"),
    VERIFY_REJECTED(1105, "认证申请已被驳回，请查看审核意见后重新提交"),

    PROJECT_NOT_FOUND(1201, "项目不存在或已被删除"),
    PROJECT_NO_PERMISSION(1202, "无权操作该项目"),
    PROJECT_NOT_PUBLISHED(1203, "项目尚未发布"),
    ASSET_UPLOAD_FAILED(1204, "文件上传失败"),
    ASSET_TYPE_LIMIT(1205, "该类素材数量已达上限"),
    FILE_TOO_LARGE(1206, "文件体积超出限制"),
    FILE_TYPE_NOT_ALLOWED(1207, "不支持的文件类型"),

    REVIEW_NOT_FOUND(1301, "点评不存在"),
    REVIEW_CONTENT_TOO_SHORT(1302, "评语过短，至少需要 50 字"),
    REVIEW_QUOTA_EXHAUSTED(1303, "今日点评额度已用完，请明天再来"),
    REVIEW_DUPLICATE(1304, "30 天内已点评过该学生的项目"),
    REVIEW_SELF_FORBIDDEN(1305, "不能点评自己提交的项目"),

    QUOTA_EXHAUSTED(1401, "本月免费查验额度已用完，请升级套餐"),
    INVITATION_EXISTS(1402, "已向该同学发送过邀约，请等待回应"),
    FAVORITE_EXISTS(1403, "该候选人已在收藏清单中"),

    APPEAL_PROCESSING(1501, "该点评有申诉正在处理中");

    private final Integer code;

    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
