package com.xiaozhan.service;

import com.xiaozhan.dto.ChangePasswordDTO;
import com.xiaozhan.dto.LoginDTO;
import com.xiaozhan.dto.RegisterDTO;
import com.xiaozhan.vo.LoginVO;
import com.xiaozhan.vo.UserInfoVO;

/**
 * 认证服务
 */
public interface AuthService {

    /**
     * 注册（学生 / 专家 / 企业）
     *
     * @return 注册后的登录态
     */
    LoginVO register(RegisterDTO dto);

    /**
     * 登录
     */
    LoginVO login(LoginDTO dto);

    /**
     * 获取当前登录用户完整信息
     */
    UserInfoVO currentUser();

    /**
     * 修改密码
     */
    void changePassword(ChangePasswordDTO dto);

    /**
     * 退出登录（前端清除 token 即可，服务端预留用于扩展黑名单）
     */
    void logout();
}
