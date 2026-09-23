-- =====================================================================
-- 校栈 Xiaozhan — 应届生项目作品集与能力背书平台
-- 数据库初始化脚本 (MySQL 8.0+)
-- 字符集: utf8mb4 / 排序规则: utf8mb4_general_ci
-- =====================================================================

DROP DATABASE IF EXISTS `xiaozhan`;
CREATE DATABASE `xiaozhan` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `xiaozhan`;

-- ---------------------------------------------------------------------
-- 1. 用户表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`      VARCHAR(50)  NOT NULL COMMENT '登录账号',
  `password`      VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码',
  `role`          VARCHAR(20)  NOT NULL COMMENT '角色: STUDENT/EXPERT/COMPANY/ADMIN',
  `nickname`      VARCHAR(50)           DEFAULT NULL COMMENT '昵称',
  `real_name`     VARCHAR(50)           DEFAULT NULL COMMENT '真实姓名',
  `avatar`        VARCHAR(255)          DEFAULT NULL COMMENT '头像地址',
  `phone`         VARCHAR(20)           DEFAULT NULL COMMENT '手机号',
  `email`         VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
  `last_login_at` DATETIME              DEFAULT NULL COMMENT '最后登录时间',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ---------------------------------------------------------------------
-- 2. 学生档案表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `student_profile`;
CREATE TABLE `student_profile` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`           BIGINT       NOT NULL COMMENT '用户ID',
  `school`            VARCHAR(100)          DEFAULT NULL COMMENT '学校',
  `major`             VARCHAR(100)          DEFAULT NULL COMMENT '专业',
  `major_category`    VARCHAR(30)           DEFAULT NULL COMMENT '专业大类: CS/DESIGN/ECON/OTHER',
  `degree`            VARCHAR(20)           DEFAULT NULL COMMENT '学历: 专科/本科/硕士/博士',
  `graduate_year`     INT                   DEFAULT NULL COMMENT '毕业年份',
  `edu_verified`      TINYINT      NOT NULL DEFAULT 0 COMMENT '学籍认证: 0-未认证 1-待审核 2-已认证 3-已驳回',
  `edu_verify_file`   VARCHAR(255)          DEFAULT NULL COMMENT '学籍验证材料（学信网截图/edu邮箱截图）',
  `edu_verify_remark` VARCHAR(255)          DEFAULT NULL COMMENT '学籍审核意见',
  `skill_tags`        VARCHAR(500)          DEFAULT NULL COMMENT '技能标签，逗号分隔',
  `bio`               VARCHAR(500)          DEFAULT NULL COMMENT '个人简介',
  `allow_company_search` TINYINT   NOT NULL DEFAULT 0 COMMENT '是否允许企业检索: 0-否 1-是',
  `portfolio_views`   INT          NOT NULL DEFAULT 0 COMMENT '作品集浏览量',
  `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_major_category` (`major_category`),
  KEY `idx_school` (`school`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生档案表';

-- ---------------------------------------------------------------------
-- 3. 专家档案表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `expert_profile`;
CREATE TABLE `expert_profile` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`         BIGINT       NOT NULL COMMENT '用户ID',
  `org_name`        VARCHAR(100)          DEFAULT NULL COMMENT '所在单位（学校/企业）',
  `position`        VARCHAR(100)          DEFAULT NULL COMMENT '职位',
  `expert_type`     VARCHAR(20)           DEFAULT NULL COMMENT '专家类型: TEACHER-高校导师 ENGINEER-企业工程师',
  `domain`          VARCHAR(200)          DEFAULT NULL COMMENT '擅长领域，逗号分隔',
  `verify_file`     VARCHAR(255)          DEFAULT NULL COMMENT '证明材料（工牌/聘书/企业邮箱截图）',
  `verify_status`   TINYINT      NOT NULL DEFAULT 0 COMMENT '认证状态: 0-待提交 1-待审核 2-已通过 3-已驳回',
  `verify_remark`   VARCHAR(255)          DEFAULT NULL COMMENT '审核意见',
  `show_org`        TINYINT      NOT NULL DEFAULT 1 COMMENT '是否公开单位信息: 0-否 1-是',
  `daily_quota`     INT          NOT NULL DEFAULT 20 COMMENT '每日点评额度',
  `total_reviews`   INT          NOT NULL DEFAULT 0 COMMENT '累计点评数',
  `thanks_count`    INT          NOT NULL DEFAULT 0 COMMENT '被感谢数',
  `quality_score`   DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '点评质量分',
  `level`           VARCHAR(20)  NOT NULL DEFAULT 'BRONZE' COMMENT '认证等级: BRONZE/SILVER/GOLD',
  `points`          INT          NOT NULL DEFAULT 0 COMMENT '激励积分',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`         TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_verify_status` (`verify_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专家档案表';

-- ---------------------------------------------------------------------
-- 4. 企业档案表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `company_profile`;
CREATE TABLE `company_profile` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`         BIGINT       NOT NULL COMMENT '用户ID',
  `company_name`    VARCHAR(100) NOT NULL COMMENT '企业名称',
  `industry`        VARCHAR(50)           DEFAULT NULL COMMENT '行业',
  `scale`           VARCHAR(30)           DEFAULT NULL COMMENT '规模',
  `license_file`    VARCHAR(255)          DEFAULT NULL COMMENT '营业执照文件',
  `verify_status`   TINYINT      NOT NULL DEFAULT 0 COMMENT '认证状态: 0-待提交 1-待审核 2-已通过 3-已驳回',
  `verify_remark`   VARCHAR(255)          DEFAULT NULL COMMENT '审核意见',
  `package_type`    VARCHAR(20)  NOT NULL DEFAULT 'FREE' COMMENT '套餐: FREE/BASIC/PRO',
  `package_expire`  DATETIME              DEFAULT NULL COMMENT '套餐到期时间',
  `month_quota`     INT          NOT NULL DEFAULT 20 COMMENT '每月查验额度',
  `month_used`      INT          NOT NULL DEFAULT 0 COMMENT '本月已用额度',
  `quota_reset_at`  DATE                  DEFAULT NULL COMMENT '额度重置日期',
  `verified_at`     DATETIME              DEFAULT NULL COMMENT '认证通过时间',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`         TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_verify_status` (`verify_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业档案表';

-- ---------------------------------------------------------------------
-- 5. 企业子账号表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `company_member`;
CREATE TABLE `company_member` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `company_id`  BIGINT      NOT NULL COMMENT '企业档案ID',
  `user_id`     BIGINT      NOT NULL COMMENT '子账号用户ID',
  `member_role` VARCHAR(20) NOT NULL DEFAULT 'HR' COMMENT '角色: HR/INTERVIEWER/ADMIN',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted`     TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_company_id` (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业子账号表';

-- ---------------------------------------------------------------------
-- 6. 项目表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `project`;
CREATE TABLE `project` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id`      BIGINT       NOT NULL COMMENT '学生用户ID',
  `name`            VARCHAR(120) NOT NULL COMMENT '项目名称',
  `project_type`    VARCHAR(30)  NOT NULL COMMENT '项目类型: COURSE/COMPETITION/INTERNSHIP/PERSONAL/GRADUATION',
  `category`        VARCHAR(30)  NOT NULL COMMENT '项目大类: CS/DESIGN/ECON/OTHER',
  `summary`         VARCHAR(500)          DEFAULT NULL COMMENT '项目简介',
  `role_desc`       VARCHAR(255)          DEFAULT NULL COMMENT '个人角色与分工',
  `tech_stack`      VARCHAR(500)          DEFAULT NULL COMMENT '技术栈/工具，逗号分隔',
  `highlight`       TEXT                  DEFAULT NULL COMMENT '技术难点与亮点（富文本）',
  `repo_url`        VARCHAR(255)          DEFAULT NULL COMMENT '代码仓库地址',
  `repo_platform`   VARCHAR(30)           DEFAULT NULL COMMENT '仓库平台: GITHUB/GITEE/OTHER',
  `start_date`      DATE                  DEFAULT NULL COMMENT '开始日期',
  `end_date`        DATE                  DEFAULT NULL COMMENT '结束日期',
  `cover_url`       VARCHAR(255)          DEFAULT NULL COMMENT '封面图',
  `visibility`      VARCHAR(20)  NOT NULL DEFAULT 'PUBLIC' COMMENT '可见性: PUBLIC/LINK/PRIVATE',
  `publish_status`  TINYINT      NOT NULL DEFAULT 0 COMMENT '发布状态: 0-草稿 1-已发布',
  `review_status`   TINYINT      NOT NULL DEFAULT 1 COMMENT '内容审核: 0-待审核 1-通过 2-驳回',
  `in_review_pool`  TINYINT      NOT NULL DEFAULT 0 COMMENT '是否在求点评池: 0-否 1-是',
  `pool_join_time`  DATETIME              DEFAULT NULL COMMENT '进入求点评池时间',
  `pool_boost`      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已享受曝光加权',
  `view_count`      INT          NOT NULL DEFAULT 0 COMMENT '浏览数',
  `expert_avg_score` DECIMAL(3,2)         DEFAULT NULL COMMENT '专家平均分',
  `expert_review_count` INT       NOT NULL DEFAULT 0 COMMENT '专家点评数',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`         TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_category` (`category`),
  KEY `idx_pool` (`in_review_pool`, `review_status`),
  KEY `idx_visibility` (`visibility`, `publish_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- ---------------------------------------------------------------------
-- 7. 项目素材表（源码包 / 文档 / 视频 / 截图）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `project_asset`;
CREATE TABLE `project_asset` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `project_id`    BIGINT       NOT NULL COMMENT '项目ID',
  `asset_type`    VARCHAR(20)  NOT NULL COMMENT '素材类型: SOURCE/DOC/VIDEO/IMAGE',
  `file_name`     VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `file_url`      VARCHAR(500) NOT NULL COMMENT '访问地址',
  `file_size`     BIGINT                DEFAULT 0 COMMENT '文件大小（字节）',
  `file_ext`      VARCHAR(20)           DEFAULT NULL COMMENT '扩展名',
  `sort_order`    INT          NOT NULL DEFAULT 0 COMMENT '排序',
  `ext_json`      VARCHAR(1000)         DEFAULT NULL COMMENT '扩展信息JSON（视频时长/外链地址等）',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted`       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`, `asset_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目素材表';

-- ---------------------------------------------------------------------
-- 8. 项目协作者表（团队项目）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `project_member`;
CREATE TABLE `project_member` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `project_id`    BIGINT       NOT NULL COMMENT '项目ID',
  `student_id`    BIGINT       NOT NULL COMMENT '协作者学生用户ID',
  `member_role`   VARCHAR(100)          DEFAULT NULL COMMENT '本人在项目中的分工',
  `is_owner`      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否项目创建者',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted`       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目协作者表';

-- ---------------------------------------------------------------------
-- 9. 点评表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `expert_review`;
CREATE TABLE `expert_review` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT,
  `project_id`     BIGINT       NOT NULL COMMENT '项目ID',
  `expert_id`      BIGINT       NOT NULL COMMENT '专家用户ID',
  `student_id`     BIGINT       NOT NULL COMMENT '学生用户ID',
  `score_completion` TINYINT    NOT NULL COMMENT '完成度 1-5',
  `score_normative`  TINYINT    NOT NULL COMMENT '规范性 1-5',
  `score_innovation` TINYINT    NOT NULL COMMENT '创新性 1-5',
  `score_technical`  TINYINT    NOT NULL COMMENT '工程/专业质量 1-5',
  `avg_score`      DECIMAL(3,2) NOT NULL COMMENT '平均分',
  `comment`        TEXT         NOT NULL COMMENT '评语（>=50字）',
  `strength`       VARCHAR(500)          DEFAULT NULL COMMENT '优点',
  `weakness`       VARCHAR(500)          DEFAULT NULL COMMENT '不足',
  `suggestion`     VARCHAR(500)          DEFAULT NULL COMMENT '改进建议',
  `invite_type`    VARCHAR(20)  NOT NULL DEFAULT 'POOL' COMMENT '来源: POOL-求点评池 INVITE-定向邀请',
  `quality_status` TINYINT      NOT NULL DEFAULT 1 COMMENT '质量校验: 0-待校验 1-有效 2-嫌疑敷衍',
  `is_public`      TINYINT      NOT NULL DEFAULT 1 COMMENT '是否公开',
  `thanks_flag`    TINYINT      NOT NULL DEFAULT 0 COMMENT '学生是否感谢',
  `appeal_status`  TINYINT      NOT NULL DEFAULT 0 COMMENT '申诉状态: 0-无 1-申诉中 2-申诉成立 3-申诉驳回',
  `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`        TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_expert_id` (`expert_id`),
  KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专家点评表';

-- ---------------------------------------------------------------------
-- 10. 点评回应表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `review_reply`;
CREATE TABLE `review_reply` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `review_id`   BIGINT      NOT NULL COMMENT '点评ID',
  `user_id`     BIGINT      NOT NULL COMMENT '回复人',
  `role`        VARCHAR(20) NOT NULL COMMENT '回复人角色',
  `content`     VARCHAR(1000) NOT NULL COMMENT '回复内容',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted`     TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_review_id` (`review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点评回应表';

-- ---------------------------------------------------------------------
-- 11. 点评申诉表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `review_appeal`;
CREATE TABLE `review_appeal` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `review_id`     BIGINT       NOT NULL COMMENT '点评ID',
  `student_id`    BIGINT       NOT NULL COMMENT '申诉学生',
  `reason`        VARCHAR(1000) NOT NULL COMMENT '申诉理由',
  `evidence_url`  VARCHAR(500)          DEFAULT NULL COMMENT '佐证材料',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1-待处理 2-成立 3-驳回',
  `handle_remark` VARCHAR(500)          DEFAULT NULL COMMENT '处理意见',
  `handler_id`    BIGINT                DEFAULT NULL COMMENT '处理人',
  `handle_time`   DATETIME              DEFAULT NULL COMMENT '处理时间',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted`       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点评申诉表';

-- ---------------------------------------------------------------------
-- 12. 企业查验记录表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `company_view_log`;
CREATE TABLE `company_view_log` (
  `id`             BIGINT      NOT NULL AUTO_INCREMENT,
  `company_id`     BIGINT      NOT NULL COMMENT '企业档案ID',
  `viewer_user_id` BIGINT      NOT NULL COMMENT '操作人用户ID',
  `student_id`     BIGINT      NOT NULL COMMENT '被查看学生用户ID',
  `project_id`     BIGINT               DEFAULT NULL COMMENT '被查看项目ID（为空表示作品集主页）',
  `view_type`      VARCHAR(20) NOT NULL DEFAULT 'PORTFOLIO' COMMENT '类型: PORTFOLIO-作品集 PROJECT-项目详情 SEARCH-人才检索',
  `quota_cost`     TINYINT     NOT NULL DEFAULT 0 COMMENT '是否消耗额度',
  `create_time`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业查验记录表';

-- ---------------------------------------------------------------------
-- 13. 候选人收藏 / 人才清单表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `candidate_favorite`;
CREATE TABLE `candidate_favorite` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `company_id`  BIGINT      NOT NULL COMMENT '企业档案ID',
  `student_id`  BIGINT      NOT NULL COMMENT '学生用户ID',
  `list_name`   VARCHAR(50) NOT NULL DEFAULT '默认清单' COMMENT '人才清单名称',
  `remark`      VARCHAR(255)         DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted`     TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_student_list` (`company_id`, `student_id`, `list_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='候选人收藏表';

-- ---------------------------------------------------------------------
-- 14. 沟通邀约表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `invitation`;
CREATE TABLE `invitation` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `company_id`    BIGINT       NOT NULL COMMENT '企业档案ID',
  `student_id`    BIGINT       NOT NULL COMMENT '学生用户ID',
  `project_id`    BIGINT                DEFAULT NULL COMMENT '关联项目ID',
  `job_title`     VARCHAR(100)          DEFAULT NULL COMMENT '意向岗位',
  `content`       VARCHAR(1000)         DEFAULT NULL COMMENT '邀约内容',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1-待回应 2-已同意 3-已拒绝',
  `contact_info`  VARCHAR(255)          DEFAULT NULL COMMENT '学生同意后交换的联系方式',
  `reply_time`    DATETIME              DEFAULT NULL COMMENT '回应时间',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='沟通邀约表';

-- ---------------------------------------------------------------------
-- 15. 认证审核记录表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `verify_record`;
CREATE TABLE `verify_record` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `biz_type`      VARCHAR(20)  NOT NULL COMMENT '业务类型: STUDENT/EXPERT/COMPANY',
  `biz_id`        BIGINT       NOT NULL COMMENT '业务主键（档案ID）',
  `applicant_id`  BIGINT       NOT NULL COMMENT '申请人用户ID',
  `submit_file`   VARCHAR(255)          DEFAULT NULL COMMENT '提交材料',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1-待审核 2-通过 3-驳回',
  `remark`        VARCHAR(500)          DEFAULT NULL COMMENT '审核意见',
  `handler_id`    BIGINT                DEFAULT NULL COMMENT '审核人',
  `handle_time`   DATETIME              DEFAULT NULL COMMENT '审核时间',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted`       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_biz` (`biz_type`, `status`),
  KEY `idx_applicant` (`applicant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='认证审核记录表';

-- ---------------------------------------------------------------------
-- 16. 内容审核记录表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `content_audit`;
CREATE TABLE `content_audit` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `target_type`   VARCHAR(20)  NOT NULL COMMENT '目标类型: PROJECT/DOC/VIDEO/REVIEW',
  `target_id`     BIGINT       NOT NULL COMMENT '目标ID',
  `snapshot`      VARCHAR(500)          DEFAULT NULL COMMENT '内容摘要',
  `auto_result`   VARCHAR(20)           DEFAULT NULL COMMENT '自动检测结果: PASS/SUSPECT',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1-待审核 2-通过 3-下架',
  `remark`        VARCHAR(500)          DEFAULT NULL COMMENT '审核意见',
  `handler_id`    BIGINT                DEFAULT NULL COMMENT '审核人',
  `handle_time`   DATETIME              DEFAULT NULL COMMENT '审核时间',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted`       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容审核记录表';

-- ---------------------------------------------------------------------
-- 17. 企业订单表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `company_order`;
CREATE TABLE `company_order` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `company_id`    BIGINT        NOT NULL COMMENT '企业档案ID',
  `order_no`      VARCHAR(50)   NOT NULL COMMENT '订单号',
  `package_type`  VARCHAR(20)   NOT NULL COMMENT '套餐类型: BASIC/PRO',
  `amount`        DECIMAL(10,2) NOT NULL COMMENT '金额（元）',
  `pay_status`    TINYINT       NOT NULL DEFAULT 1 COMMENT '状态: 1-待支付 2-已支付 3-已取消',
  `pay_time`      DATETIME               DEFAULT NULL COMMENT '支付时间',
  `invoice_title` VARCHAR(100)           DEFAULT NULL COMMENT '发票抬头',
  `invoice_tax_no` VARCHAR(50)           DEFAULT NULL COMMENT '税号',
  `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_company_id` (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业订单表';

-- ---------------------------------------------------------------------
-- 18. 埋点事件表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `track_event`;
CREATE TABLE `track_event` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `event_code`  VARCHAR(50)  NOT NULL COMMENT '事件编码',
  `user_id`     BIGINT                DEFAULT NULL COMMENT '用户ID',
  `role`        VARCHAR(20)           DEFAULT NULL COMMENT '角色',
  `major`       VARCHAR(30)           DEFAULT NULL COMMENT '专业大类',
  `project_type` VARCHAR(30)          DEFAULT NULL COMMENT '项目类型',
  `extra`       VARCHAR(500)          DEFAULT NULL COMMENT '扩展信息',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_event_code` (`event_code`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='埋点事件表';

-- ---------------------------------------------------------------------
-- 19. 系统字典表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `dict_type`   VARCHAR(50)  NOT NULL COMMENT '字典类型',
  `dict_key`    VARCHAR(50)  NOT NULL COMMENT '字典键',
  `dict_label`  VARCHAR(100) NOT NULL COMMENT '字典标签',
  `sort_order`  INT          NOT NULL DEFAULT 0,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted`     TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统字典表';

-- =====================================================================
-- 初始化数据
-- =====================================================================

-- 密码统一为 123456 的 BCrypt 密文（已用 spring-security-crypto BCryptPasswordEncoder 实际校验通过）
-- $2a$10$vupdzEYg.nJHrPGNNj.qp.FtGnb4ZACxJOaYCodUrvROiMZCL.SGa
SET @pwd = '$2a$10$vupdzEYg.nJHrPGNNj.qp.FtGnb4ZACxJOaYCodUrvROiMZCL.SGa';

-- 管理员
INSERT INTO `sys_user` (`username`, `password`, `role`, `nickname`, `real_name`, `status`) VALUES
('admin', @pwd, 'ADMIN', '平台管理员', '王运营', 1);

-- 学生：李思远
INSERT INTO `sys_user` (`id`, `username`, `password`, `role`, `nickname`, `real_name`, `phone`, `email`, `status`) VALUES
(2, 'student01', @pwd, 'STUDENT', '李思远', '李思远', '13800000001', 'lisy@stu.edu.cn', 1),
(3, 'student02', @pwd, 'STUDENT', '陈知微', '陈知微', '13800000002', 'chenzw@stu.edu.cn', 1),
(4, 'student03', @pwd, 'STUDENT', '赵晓萌', '赵晓萌', '13800000003', 'zhaoxm@stu.edu.cn', 1);

INSERT INTO `student_profile` (`user_id`, `school`, `major`, `major_category`, `degree`, `graduate_year`, `edu_verified`, `skill_tags`, `bio`, `allow_company_search`, `portfolio_views`) VALUES
(2, '华中科技大学', '计算机科学与技术', 'CS', '本科', 2026, 2, 'Java,SpringBoot,MySQL,Vue', '专注后端开发，主导过多个课程与竞赛项目，喜欢把想法做成能跑的东西。', 1, 128),
(3, '中国美术学院', '视觉传达设计', 'DESIGN', '本科', 2026, 2, 'Figma,UI设计,品牌视觉', '视觉传达方向，关注信息设计与交互体验。', 1, 86),
(4, '上海财经大学', '金融学', 'ECON', '硕士', 2026, 2, '财务建模,数据分析,Python', '金融数据分析方向，擅长用数据讲清业务问题。', 1, 54);

-- 专家：王工（企业工程师）、张教授（高校导师）
INSERT INTO `sys_user` (`id`, `username`, `password`, `role`, `nickname`, `real_name`, `email`, `status`) VALUES
(5, 'expert01', @pwd, 'EXPERT', '王工', '王浩然', 'wanghr@techcorp.com', 1),
(6, 'expert02', @pwd, 'EXPERT', '张教授', '张明德', 'zhangmd@univ.edu.cn', 1);

INSERT INTO `expert_profile` (`user_id`, `org_name`, `position`, `expert_type`, `domain`, `verify_status`, `show_org`, `total_reviews`, `thanks_count`, `quality_score`, `level`, `points`) VALUES
(5, '星澜科技', '后端技术专家', 'ENGINEER', 'Java,分布式架构,代码规范', 2, 1, 46, 41, 92.50, 'GOLD', 460),
(6, '华中科技大学计算机学院', '副教授', 'TEACHER', '软件工程,项目管理,学术写作', 2, 1, 38, 35, 89.00, 'SILVER', 380);

-- 企业：云图智能
INSERT INTO `sys_user` (`id`, `username`, `password`, `role`, `nickname`, `real_name`, `email`, `status`) VALUES
(7, 'company01', @pwd, 'COMPANY', '云图智能HR', '刘敏', 'hr@yuntu.com', 1);

INSERT INTO `company_profile` (`user_id`, `company_name`, `industry`, `scale`, `verify_status`, `package_type`, `month_quota`, `month_used`, `verified_at`) VALUES
(7, '云图智能科技有限公司', '互联网/人工智能', '500-2000人', 2, 'PRO', 500, 37, NOW());

INSERT INTO `company_member` (`company_id`, `user_id`, `member_role`) VALUES (1, 7, 'ADMIN');

-- 项目数据
INSERT INTO `project` (`id`, `student_id`, `name`, `project_type`, `category`, `summary`, `role_desc`, `tech_stack`, `highlight`, `repo_url`, `repo_platform`, `start_date`, `end_date`, `visibility`, `publish_status`, `review_status`, `view_count`, `expert_avg_score`, `expert_review_count`) VALUES
(1, 2, '校园二手交易平台', 'COURSE', 'CS', '面向校内学生的闲置物品交易系统，支持发布、搜索、下单、站内消息与信用评价，解决校园闲置流转效率低的问题。', '后端主程：负责整体架构设计、数据库建模、订单与支付模块开发，并完成部署上线。', 'Java,SpringBoot,MyBatis Plus,MySQL,Redis,Vue', '采用 Redis 缓存热点商品列表，QPS 从 120 提升到 900；通过乐观锁解决并发下单超卖问题；设计了基于状态机的订单流转模型避免状态错乱。', 'https://github.com/demo/campus-trade', 'GITHUB', '2025-09-01', '2026-01-15', 'PUBLIC', 1, 1, 1, 342, 4.25, 2),
(2, 2, '分布式短链接服务', 'PERSONAL', 'CS', '高并发场景下的短链接生成与跳转服务，支持自定义短码、点击统计与过期策略。', '独立开发：从需求分析、方案设计到编码测试全部独立完成。', 'Java,SpringBoot,Redis,Guava,MySQL', '使用布隆过滤器拦截无效短码请求，降低 60% 的无效数据库查询；基于雪花算法与 Base62 生成无冲突短码；实现了多级缓存与异步埋点统计。', 'https://github.com/demo/shortlink', 'GITHUB', '2025-06-01', '2025-08-20', 'PUBLIC', 1, 1, 1, 187, 4.50, 1),
(3, 3, '城市记忆·老街区导视系统设计', 'GRADUATION', 'DESIGN', '为老城区设计的一套街区导视与信息可视化系统，包含导视牌、地图交互与线上导览小程序视觉规范。', '主创设计师：负责整体视觉概念、导视系统规范制定、主视觉设计与落地物料输出。', 'Figma,Illustrator,Photoshop,Principle', '以"时间褶皱"为概念，将街区历史分层可视化为导视图形语言；建立了包含 42 个图标、6 级信息层级的完整导视规范；方案在学院毕业设计展中获评优秀。', '', 'OTHER', '2025-09-10', '2026-05-20', 'PUBLIC', 1, 1, 1, 265, 4.75, 1),
(4, 4, 'A股新能源板块基本面量化分析', 'COMPETITION', 'ECON', '基于财务因子与行业数据的量化分析项目，构建新能源板块选股打分模型并完成回测验证。', '负责人：数据采集与清洗、因子构建、模型回测与结论撰写。', 'Python,Pandas,MySQL,Tableau', '构建了包含 5 大类 18 个财务因子的评分模型；通过 2019—2025 年数据回测，模型年化超额收益 8.3%；使用 Tableau 输出可交互分析看板。', 'https://github.com/demo/ne-energy-quant', 'GITHUB', '2025-10-01', '2025-12-30', 'PUBLIC', 1, 1, 1, 143, 4.00, 1),
(5, 2, 'AI 简历解析与岗位匹配助手', 'INTERNSHIP', 'CS', '实习期间参与的企业项目，通过 NLP 技术解析简历并与岗位 JD 做语义匹配打分。', '后端开发实习生：负责简历解析服务与匹配打分接口开发，参与模型效果调优。', 'Java,SpringBoot,Elasticsearch,Python', '设计了两段式解析流程（规则优先 + 模型兜底），解析准确率从 76% 提升到 91%；使用 ES 完成简历与 JD 的向量化召回。', '', 'OTHER', '2026-03-01', '2026-06-30', 'PUBLIC', 1, 1, 1, 98, 4.00, 1),
(6, 3, '非遗手作电商品牌视觉升级', 'INTERNSHIP', 'DESIGN', '为一家非遗手作电商品牌完成视觉升级，覆盖品牌标识、包装、详情页与社交媒体视觉体系。', '视觉设计实习生：负责品牌视觉提案、包装设计与详情页视觉输出。', 'Figma,Photoshop,Illustrator', '以"手工温度"为核心完成品牌视觉重塑；设计了可延展的图形系统，覆盖 20+ 应用场景；详情页改版后转化率提升 23%。', '', 'OTHER', '2026-04-01', '2026-05-30', 'PUBLIC', 1, 1, 1, 76, NULL, 0);

-- 项目素材
INSERT INTO `project_asset` (`project_id`, `asset_type`, `file_name`, `file_url`, `file_size`, `file_ext`, `sort_order`, `ext_json`) VALUES
(1, 'SOURCE', 'campus-trade-source.zip', '/files/demo/campus-trade-source.zip', 18432000, 'zip', 1, NULL),
(1, 'DOC', '校园二手交易平台-需求与设计文档.pdf', '/files/demo/campus-trade-doc.pdf', 2458000, 'pdf', 1, NULL),
(1, 'VIDEO', '校园二手交易平台-功能演示.mp4', '/files/demo/campus-trade-demo.mp4', 52428800, 'mp4', 1, '{"duration":"3:12"}'),
(1, 'IMAGE', '商品列表页.png', '/files/demo/campus-trade-1.png', 356000, 'png', 1, NULL),
(1, 'IMAGE', '订单详情页.png', '/files/demo/campus-trade-2.png', 298000, 'png', 2, NULL),
(2, 'SOURCE', 'shortlink-source.zip', '/files/demo/shortlink-source.zip', 5242880, 'zip', 1, NULL),
(2, 'DOC', '短链接服务-技术方案.md', '/files/demo/shortlink-doc.md', 86000, 'md', 1, NULL),
(2, 'VIDEO', '短链接服务-压测演示.mp4', '/files/demo/shortlink-demo.mp4', 31457280, 'mp4', 1, '{"duration":"2:05"}'),
(3, 'DOC', '导视系统设计说明书.pdf', '/files/demo/wayfinding-doc.pdf', 12560000, 'pdf', 1, NULL),
(3, 'VIDEO', '导视系统-方案讲解.mp4', '/files/demo/wayfinding-demo.mp4', 47360000, 'mp4', 1, '{"duration":"4:30"}'),
(3, 'IMAGE', '主视觉.png', '/files/demo/wayfinding-1.png', 512000, 'png', 1, NULL),
(3, 'IMAGE', '导视牌应用.png', '/files/demo/wayfinding-2.png', 468000, 'png', 2, NULL),
(4, 'DOC', '新能源板块量化分析报告.pdf', '/files/demo/ne-energy-report.pdf', 3456000, 'pdf', 1, NULL),
(4, 'IMAGE', '模型回测曲线.png', '/files/demo/ne-energy-1.png', 287000, 'png', 1, NULL),
(5, 'SOURCE', 'resume-parser.zip', '/files/demo/resume-parser.zip', 9437184, 'zip', 1, NULL),
(6, 'DOC', '品牌视觉升级提案.pdf', '/files/demo/brand-doc.pdf', 8912000, 'pdf', 1, NULL),
(6, 'IMAGE', '品牌主视觉.png', '/files/demo/brand-1.png', 421000, 'png', 1, NULL);

-- 项目协作者
INSERT INTO `project_member` (`project_id`, `student_id`, `member_role`, `is_owner`) VALUES
(1, 2, '后端主程 / 架构设计', 1),
(1, 3, '前端开发 / 界面设计', 0),
(3, 3, '主创设计师', 1);

-- 点评数据
INSERT INTO `expert_review` (`id`, `project_id`, `expert_id`, `student_id`, `score_completion`, `score_normative`, `score_innovation`, `score_technical`, `avg_score`, `comment`, `strength`, `weakness`, `suggestion`, `invite_type`, `quality_status`, `is_public`, `thanks_flag`, `create_time`) VALUES
(1, 1, 5, 2, 5, 4, 4, 4, 4.25,
 '整体完成度很高，是一个能跑通完整业务闭环的项目。订单状态机的设计是亮点，说明作者对业务边界有思考而不是简单堆 CRUD。代码分层清晰，Controller/Service/Mapper 职责分明，异常处理也有统一封装。不足之处在于缓存一致性处理偏简单，商品列表更新后依赖过期策略被动失效，在数据更新频繁时可能读到较旧的数据；另外缺少接口层面的限流与幂等设计，下单接口在重复提交场景下存在隐患。建议补充缓存更新策略的说明与压测数据。',
 '订单状态机设计合理，代码分层清晰，有统一异常封装', '缓存一致性处理偏简单，缺少限流与幂等设计', '补充缓存更新策略与压测数据，增加接口幂等校验', 'POOL', 1, 1, 1, '2026-06-20 10:24:00'),
(2, 1, 6, 2, 4, 5, 3, 4, 4.00,
 '作为课程设计项目，文档规范性突出：需求文档、数据库设计文档、接口文档齐全，这一点在本科生中并不常见。技术实现上基本功扎实，但对系统边界的界定还可以更清楚——例如站内消息模块是否属于核心链路、失败如何处理，文档中未展开。创新性方面偏常规，建议在选题或技术方案上寻找差异化角度。总体上这是一个可以拿去面试讲清楚的项目。',
 '文档规范齐全，基本功扎实，具备可讲性', '系统边界界定不清，创新性偏常规', '补充关键模块的失败处理设计，尝试在方案上做差异化', 'POOL', 1, 1, 1, '2026-06-22 15:10:00'),
(3, 2, 5, 2, 4, 4, 5, 4, 4.25,
 '短链接服务是一个很适合体现技术深度的选题，作者抓住了高并发场景下的三个关键点：短码生成的唯一性、无效请求的拦截、统计数据的异步化。布隆过滤器的引入是主动思考的结果，不是照搬教程。需要提升的是对"为什么这样设计"的量化说明——比如 60% 的无效查询降低是怎么测出来的，多级缓存各层的命中率是多少。面试官通常会顺着这些数字追问，能把数据讲清楚会大幅加分。',
 '选题有技术深度，关键点抓得准，布隆过滤器有主动思考', '量化数据缺失，设计权衡的说明不够', '补充压测数据与缓存命中率，说明设计权衡过程', 'POOL', 1, 1, 0, '2026-06-25 09:40:00'),
(4, 3, 6, 3, 5, 5, 5, 4, 4.75,
 '这套导视系统完成度与专业度都很高。最打动人的是从概念到落地规范形成了完整链路："时间褶皱"这个抽象概念被成功翻译成了可复用的图形语言与信息层级，而不是停留在视觉稿层面。42 个图标、6 级信息层级的规范说明作者具备系统化设计能力，这正是企业设计岗最看重的能力之一。建议补充真实场景的落地照片或用户测试反馈，让方案的说服力从"设计完成"延伸到"设计有效"。',
 '概念到规范形成完整链路，系统化设计能力强', '缺少落地验证与用户反馈', '补充真实场景落地照片或用户测试结论', 'POOL', 1, 1, 1, '2026-06-26 14:20:00'),
(5, 4, 5, 4, 4, 3, 4, 4, 3.75,
 '从数据采集到因子建模再到回测，流程完整，因子体系的构建有一定思考，不是简单堆指标。但需要特别注意样本外验证的问题：如果模型是在全样本上调参后回测，年化超额 8.3% 这个结果可能会有前视偏差。建议做一次严格的样本外测试（如滚动窗口），并把交易成本、滑点纳入回测，这样结论才站得住。这类项目在面试中很容易被追问方法论，要提前准备好。',
 '流程完整，因子体系有思考', '存在前视偏差风险，未考虑交易成本与滑点', '做滚动窗口样本外测试，将交易成本纳入回测', 'POOL', 1, 1, 0, '2026-06-28 11:05:00'),
(6, 5, 5, 2, 4, 4, 4, 4, 4.00,
 '实习项目的加分点在于接触了真实业务场景，两段式解析流程（规则优先 + 模型兜底）是工程上很务实的做法。但作为简历项目要注意：需要清晰说明哪些是你独立完成的、哪些是团队成果，否则面试时容易被问穿。技术上建议补充解析失败的兜底与人工介入流程，以及模型效果评估的具体指标定义。',
 '接触真实业务，两段式方案务实', '个人贡献边界不清晰，缺少失败兜底设计', '明确个人贡献范围，补充失败兜底与效果评估指标', 'INVITE', 1, 1, 0, '2026-07-02 16:30:00');

-- 点评回应
INSERT INTO `review_reply` (`review_id`, `user_id`, `role`, `content`, `create_time`) VALUES
(1, 3, 'STUDENT', '谢谢老师的点评！关于 API 设计部分，我在后续版本里补充了接口文档和错误码规范，也重写了分页逻辑。', '2026-06-21 09:12:00'),
(2, 2, 'STUDENT', '感谢王工，您提到的并发下单超卖问题我已经用乐观锁 + 库存预扣解决了，压测 500 并发下没有出现超卖。', '2026-06-23 10:05:00'),
(4, 3, 'STUDENT', '非常感谢！我会按建议补充落地场景的照片和用户测试反馈。', '2026-06-27 08:45:00');

-- 求点评池中的项目（项目 5、6 待点评）
UPDATE `project` SET `in_review_pool` = 1, `pool_join_time` = '2026-07-01 09:00:00' WHERE `id` = 5;
UPDATE `project` SET `in_review_pool` = 1, `pool_join_time` = '2026-07-03 10:30:00' WHERE `id` = 6;

-- 企业查验记录
INSERT INTO `company_view_log` (`company_id`, `viewer_user_id`, `student_id`, `project_id`, `view_type`, `quota_cost`, `create_time`) VALUES
(1, 7, 2, NULL, 'PORTFOLIO', 1, '2026-07-05 10:12:00'),
(1, 7, 2, 1, 'PROJECT', 0, '2026-07-05 10:14:00'),
(1, 7, 3, NULL, 'PORTFOLIO', 1, '2026-07-05 10:20:00'),
(1, 7, 4, NULL, 'PORTFOLIO', 1, '2026-07-06 14:35:00');

-- 候选人收藏
INSERT INTO `candidate_favorite` (`company_id`, `student_id`, `list_name`, `remark`) VALUES
(1, 2, '后端岗候选', '项目闭环完整，订单状态机设计是亮点'),
(1, 3, '设计岗候选', '导视系统规范完整，系统化能力强');

-- 邀约
INSERT INTO `invitation` (`company_id`, `student_id`, `project_id`, `job_title`, `content`, `status`, `create_time`) VALUES
(1, 2, 1, 'Java 后端开发工程师（校招）', '您好，我们在校栈看到您的校园二手交易平台项目，对您在订单状态机与并发处理上的设计很感兴趣，希望邀请您参加我们的校招面试。', 2, '2026-07-06 11:00:00'),
(1, 3, 3, '视觉设计师（校招）', '您好，您的老街区导视系统设计给我们留下了深刻印象，希望进一步沟通设计岗机会。', 1, '2026-07-07 09:30:00');

-- 认证审核记录
INSERT INTO `verify_record` (`biz_type`, `biz_id`, `applicant_id`, `submit_file`, `status`, `remark`, `handler_id`, `handle_time`) VALUES
('STUDENT', 1, 2, '/files/demo/edu-verify-2.png', 2, '学信网在线验证报告核验通过', 1, '2026-06-10 09:30:00'),
('STUDENT', 2, 3, '/files/demo/edu-verify-3.png', 2, '学信网在线验证报告核验通过', 1, '2026-06-10 09:35:00'),
('STUDENT', 3, 4, '/files/demo/edu-verify-4.png', 2, '学信网在线验证报告核验通过', 1, '2026-06-11 10:00:00'),
('EXPERT', 1, 5, '/files/demo/expert-verify-5.png', 2, '企业邮箱与在职信息核验通过', 1, '2026-06-08 15:20:00'),
('EXPERT', 2, 6, '/files/demo/expert-verify-6.png', 2, '高校教师聘书核验通过', 1, '2026-06-09 11:40:00'),
('COMPANY', 1, 7, '/files/demo/company-license.png', 2, '营业执照与对公账户核验通过', 1, '2026-06-05 16:10:00');

-- 待审核认证（用于演示管理端审核工作台）
INSERT INTO `verify_record` (`biz_type`, `biz_id`, `applicant_id`, `submit_file`, `status`) VALUES
('EXPERT', 3, 5, '/files/demo/expert-pending.png', 1);

-- 内容审核（待处理）
INSERT INTO `content_audit` (`target_type`, `target_id`, `snapshot`, `auto_result`, `status`) VALUES
('PROJECT', 6, '非遗手作电商品牌视觉升级 - 项目简介', 'SUSPECT', 1);

-- 埋点事件（用于数据看板）
INSERT INTO `track_event` (`event_code`, `user_id`, `role`, `major`, `project_type`, `create_time`) VALUES
('REGISTER', 2, 'STUDENT', 'CS', NULL, '2026-06-01 10:00:00'),
('REGISTER', 3, 'STUDENT', 'DESIGN', NULL, '2026-06-02 11:00:00'),
('REGISTER', 4, 'STUDENT', 'ECON', NULL, '2026-06-03 14:00:00'),
('PROJECT_CREATE', 2, 'STUDENT', 'CS', 'COURSE', '2026-06-05 09:20:00'),
('ASSET_UPLOAD', 2, 'STUDENT', 'CS', 'COURSE', '2026-06-05 09:40:00'),
('PROJECT_PUBLISH', 2, 'STUDENT', 'CS', 'COURSE', '2026-06-05 10:00:00'),
('PROJECT_CREATE', 3, 'STUDENT', 'DESIGN', 'GRADUATION', '2026-06-06 13:00:00'),
('PROJECT_PUBLISH', 3, 'STUDENT', 'DESIGN', 'GRADUATION', '2026-06-06 15:30:00'),
('REVIEW_SUBMIT', 5, 'EXPERT', NULL, NULL, '2026-06-20 10:24:00'),
('REVIEW_SUBMIT', 6, 'EXPERT', NULL, NULL, '2026-06-22 15:10:00'),
('PORTFOLIO_SHARE', 2, 'STUDENT', 'CS', NULL, '2026-06-24 08:30:00'),
('COMPANY_VIEW', 7, 'COMPANY', NULL, NULL, '2026-07-05 10:12:00'),
('INVITATION_SEND', 7, 'COMPANY', NULL, NULL, '2026-07-06 11:00:00'),
('INVITATION_ACCEPT', 2, 'STUDENT', 'CS', NULL, '2026-07-06 12:30:00');

-- 字典数据
INSERT INTO `sys_dict` (`dict_type`, `dict_key`, `dict_label`, `sort_order`) VALUES
('projectType', 'COURSE', '课程设计', 1),
('projectType', 'COMPETITION', '竞赛作品', 2),
('projectType', 'INTERNSHIP', '实习项目', 3),
('projectType', 'PERSONAL', '个人项目', 4),
('projectType', 'GRADUATION', '毕业设计', 5),
('majorCategory', 'CS', '计算机类', 1),
('majorCategory', 'DESIGN', '设计类', 2),
('majorCategory', 'ECON', '经管类', 3),
('majorCategory', 'OTHER', '其他', 4),
('visibility', 'PUBLIC', '公开', 1),
('visibility', 'LINK', '仅链接可见', 2),
('visibility', 'PRIVATE', '私密', 3);
