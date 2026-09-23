package com.xiaozhan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 校栈 —— 应届生项目作品集与能力背书平台 启动类
 *
 * @author xiaozhan
 */
@SpringBootApplication
@MapperScan("com.xiaozhan.mapper")
@EnableAsync
@EnableScheduling
public class XiaozhanApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaozhanApplication.class, args);
        System.out.println("""

                ============================================================
                  校栈 Xiaozhan 服务启动成功
                  接口文档: http://localhost:8080/doc.html
                ============================================================
                """);
    }
}
