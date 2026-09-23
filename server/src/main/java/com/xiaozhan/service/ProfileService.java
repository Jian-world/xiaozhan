package com.xiaozhan.service;

import com.xiaozhan.dto.CompanyProfileDTO;
import com.xiaozhan.dto.ExpertProfileDTO;
import com.xiaozhan.dto.StudentProfileDTO;
import com.xiaozhan.dto.UserBaseDTO;
import com.xiaozhan.vo.UserInfoVO;

/**
 * 个人档案服务
 */
public interface ProfileService {

    /**
     * 更新基础资料（昵称、头像、联系方式）
     */
    UserInfoVO updateBase(UserBaseDTO dto);

    /**
     * 更新学生档案（含技能标签）
     */
    UserInfoVO updateStudentProfile(StudentProfileDTO dto);

    /**
     * 更新专家档案
     */
    UserInfoVO updateExpertProfile(ExpertProfileDTO dto);

    /**
     * 更新企业档案
     */
    UserInfoVO updateCompanyProfile(CompanyProfileDTO dto);
}
