package com.xiaozhan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 安全相关 Bean
 */
@Configuration
public class SecurityBeanConfig {

    /**
     * BCrypt 密码编码器（与演示数据中的 $2a$ 哈希格式一致）
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
