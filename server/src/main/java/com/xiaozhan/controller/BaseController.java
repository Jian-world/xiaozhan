package com.xiaozhan.controller;

import com.xiaozhan.security.UserContext;

/**
 * 控制器基类，提供当前登录用户快捷方法
 */
public abstract class BaseController {

    protected Long currentUserId() {
        return UserContext.getUserId();
    }

    protected String currentRole() {
        return UserContext.getRole();
    }

    protected boolean isStudent() {
        return "STUDENT".equals(currentRole());
    }

    protected boolean isExpert() {
        return "EXPERT".equals(currentRole());
    }

    protected boolean isCompany() {
        return "COMPANY".equals(currentRole());
    }

    protected boolean isAdmin() {
        return "ADMIN".equals(currentRole());
    }
}
