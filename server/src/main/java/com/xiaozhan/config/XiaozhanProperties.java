package com.xiaozhan.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 校栈业务配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "xiaozhan")
public class XiaozhanProperties {

    private Jwt jwt = new Jwt();

    private Upload upload = new Upload();

    private Review review = new Review();

    private Company company = new Company();

    @Data
    public static class Jwt {
        private String secret;
        private Long expire = 604800000L;
        private String header = "Authorization";
        private String prefix = "Bearer ";
    }

    @Data
    public static class Upload {
        private String basePath = "./files";
        private String urlPrefix = "/files";
        private Long maxVideoSize = 1073741824L;
        private Long maxDocSize = 104857600L;
        private Long maxSourceSize = 524288000L;
    }

    @Data
    public static class Review {
        private Integer minCommentLength = 50;
        private Integer expertDailyLimit = 20;
        private Integer duplicateDays = 30;
        private Integer poolBoostHours = 72;
        private Integer poolExpireDays = 7;
    }

    @Data
    public static class Company {
        private Integer freeMonthQuota = 20;
    }
}
