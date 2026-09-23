# 校栈 · 应届生项目作品集与能力背书平台

> 学生上传项目源码 / 文档 / 演示视频形成在线作品集 → 导师、企业工程师在线点评 → 企业直接查看项目作品替代简历筛选。

本仓库为**完整可运行**的前后端分离项目：SpringBoot 3 + Vue 3 + MySQL 8。

---

## 一、目录结构

```
xiaozhan/
├── sql/
│   └── schema.sql               # 建表 + 演示数据（一键导入）
│   └── fix_update_time          # 建表 
├── server/                      # 后端 SpringBoot 服务
│   ├── pom.xml
│   ├── uploads/                 # 本地文件存储根目录（运行时自动创建）
│   └── src/main/
│       ├── java/com/xiaozhan/
│       │   ├── XiaozhanApplication.java
│       │   ├── common/          # Result 统一响应、ResultCode 业务码
│       │   ├── config/          # WebMvc / MyBatisPlus / Knife4j / 业务参数
│       │   ├── controller/      # 12 个 Controller
│       │   ├── dto/             # 入参对象
│       │   ├── entity/          # 19 张表的实体 + BaseEntity
│       │   ├── exception/       # BizException + 全局异常处理器
│       │   ├── mapper/          # MyBatis Plus Mapper
│       │   ├── security/        # JWT 拦截器、@IgnoreAuth、@RequireRole、UserContext
│       │   ├── service/         # 业务接口 + impl
│       │   ├── util/            # JwtUtil 等
│       │   └── vo/              # 出参对象
│       └── resources/
│           └── application.yml
└── web/                         # 前端 Vue 3 + Vite
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── api/                 # axios 实例 + 全部接口定义
        ├── components/          # BrandLogo / ProjectCard / ScoreBlock
        ├── layouts/             # PublicLayout / ConsoleLayout
        ├── router/              # 4 套角色路由 + 登录守卫
        ├── stores/              # Pinia user store
        ├── styles/              # 设计变量与全局样式
        ├── utils/               # 字典映射
        └── views/               # 31 个页面（见下表）
```

---

## 二、技术栈

| 层 | 技术 | 版本 |
|---|---|---|
| 后端框架 | SpringBoot | 3.2.5 |
| 语言 | Java | 17 |
| ORM | MyBatis Plus | 3.5.5 |
| 数据库 | MySQL | 8.0 |
| 数据库连接池 | HikariCP | 随 Boot |
| 鉴权 | JWT（jjwt） | 0.12.5 |
| 密码加密 | spring-security-crypto（BCrypt） | 6.x |
| 接口文档 | Knife4j（OpenAPI 3） | 4.5.0 |
| 工具库 | Hutool | 5.8.27 |
| 简化代码 | Lombok | 1.18.30 |
| 前端框架 | Vue 3 | 3.4 |
| 构建工具 | Vite | 5.2 |
| 路由 | Vue Router | 4.3 |
| UI 组件 | Element Plus | 2.8 |
| 状态管理 | Pinia | 2.1 |
| HTTP | Axios | 1.7 |
| 图表 | ECharts | 5.5 |
| 富文本 | wangEditor | 5.1 |
| 样式 | Sass | 1.77 |

> Redis 为可选依赖：默认配置下项目**不依赖 Redis 即可运行**（JWT 无状态鉴权）。如需开启验证码 / 限流，可在 `application.yml` 中补 `spring.data.redis` 配置。

---

## 三、环境要求

| 软件 | 版本要求 | 说明 |
|---|---|---|
| JDK | 17+ | 需配置 `JAVA_HOME` |
| Maven | 3.8+ | 用于后端构建 |
| MySQL | 8.0+ | 需可连接 |
| Node.js | 18+ | 建议 20 / 22 |

---

## 四、启动步骤

### 1. 初始化数据库

```bash
mysql -u root -p < sql/schema.sql
```

或使用 Navicat / DataGrip 直接执行 `sql/schema.sql`。脚本会自动：

- 创建数据库 `xiaozhan`（若不存在）
- 创建 19 张业务表
- 写入演示数据（用户、项目、点评、认证记录等）

> 如果你导入的是**旧版** `schema.sql`（或数据库在此之前已建好），请再执行一次补丁脚本，
> 为 `candidate_favorite`、`company_member`、`content_audit`、`project_asset`、`project_member`、
> `review_appeal`、`review_reply`、`sys_dict`、`verify_record` 这 9 张表补齐 `update_time` 字段：
>
> ```bash
> mysql -u root -p xiaozhan < sql/fix_update_time.sql
> ```
>
> 该脚本幂等，可安全重复执行。新版 `schema.sql` 已包含此字段，无需再单独执行。

### 2. 启动后端

修改 `server/src/main/resources/application.yml` 中的数据库账号密码：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xiaozhan?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
    username: root
    password: 你的密码
```

然后启动：

```bash
cd server
mvn clean spring-boot:run
```

或打包后运行：

```bash
cd server
mvn clean package -DskipTests
java -jar target/xiaozhan-server-1.0.0.jar
```

启动成功后：

- 后端服务：<http://localhost:8080>
- 接口文档：<http://localhost:8080/doc.html>
- 健康检查：<http://localhost:8080/api/common/health>

### 3. 启动前端

```bash
cd web
npm install
npm run dev
```

访问 <http://localhost:5173>。

生产构建：

```bash
npm run build      # 产物在 web/dist
```

> Vite 已配置代理：`/api` 与 `/files` 自动转发到 `http://localhost:8080`，无需处理跨域。

---

## 五、演示账号

所有账号密码统一为 **`123456`**

| 角色 | 账号 | 说明 |
|---|---|---|
| 平台管理员 | `admin` | 拥有平台运营后台全部权限 |
| 学生 | `student01` | 学籍已认证，有 3 个公开项目 |
| 学生 | `student02` | 学籍已认证，有 2 个公开项目 |
| 学生 | `student03` | 学籍待认证 |
| 专家 | `expert01` | 高级点评人（GOLD），历史点评丰富 |
| 专家 | `expert02` | 普通点评人（SILVER） |
| 企业 | `company01` | 已认证企业，有查验额度 |

---

## 六、功能模块与页面清单

### 公共端（无需登录）

| 页面 | 路由 | 说明 |
|---|---|---|
| 首页 | `/` | 平台介绍、核心数据、精选项目、入口引导 |
| 项目广场 | `/square` | 全平台公开项目检索（按类型 / 技术栈 / 关键词） |
| 项目详情 | `/project/:id` | 项目全貌：素材、成员、专家点评、评分 |
| 学生作品集 | `/portfolio/:studentId` | 学生在线作品集主页 |
| 专家大厅 | `/experts` | 点评人展示 |

### 学生端（Console 布局）

| 页面 | 路由 | 说明 |
|---|---|---|
| 工作台 | `/student` | 数据概览、待办、作品集健康度 |
| 我的项目 | `/student/projects` | 项目列表、上下架、求点评开关 |
| 项目编辑 | `/student/project/new`、`/student/project/:id` | 项目录入 + 素材上传（源码/文档/视频）+ wangEditor 富文本 |
| 收到的点评 | `/student/reviews` | 四维评分、致谢、回应、申诉 |
| 面试邀约 | `/student/invitations` | 企业邀约同意 / 婉拒 |
| 个人档案 | `/student/profile` | 基础资料、在校信息、学籍认证、档案完整度 |

### 专家端（Console 布局）

| 页面 | 路由 | 说明 |
|---|---|---|
| 工作台 | `/expert` | 等级进度、今日额度、求点评池、最近点评 |
| 求点评池 | `/expert/pool` | 认领待点评项目 |
| 撰写点评 | `/expert/review/:projectId` | 四维评分 + 亮点 / 不足 / 建议，含敷衍预警 |
| 点评历史 | `/expert/history` | 历史点评、回应、公开状态 |
| 专家档案 | `/expert/profile` | 专业信息、领域标签、专家认证 |

### 企业端（Console 布局）

| 页面 | 路由 | 说明 |
|---|---|---|
| 工作台 | `/company` | 套餐额度、收藏 / 邀约统计、值得关注的候选人 |
| 人才检索 | `/company/talent` | 多维筛选（专业 / 学历 / 技能 / 仅看已认证） |
| 候选人档案 | `/company/candidate/:studentId` | 完整档案（消耗 1 次查验额度） |
| 收藏夹 | `/company/favorites` | 候选人收藏管理 |
| 发出的邀约 | `/company/invitations` | 邀约状态跟踪、重新发起 |
| 查验记录 | `/company/logs` | 合规留档 |
| 企业档案 | `/company/profile` | 企业信息、企业认证、套餐额度 |

### 管理端（Console 布局）

| 页面 | 路由 | 说明 |
|---|---|---|
| 平台总览 | `/admin` | 核心指标、专业 / 项目类型分布、认证通过率、供需结构 |
| 用户管理 | `/admin/users` | 全量账号查询、启用禁用、重置密码 |
| 认证审核 | `/admin/verifies` | 学籍 / 专家 / 企业认证审核 |
| 内容审核 | `/admin/audits` | 疑似敷衍点评、违规内容复核 |
| 申诉处理 | `/admin/appeals` | 点评 / 认证结果申诉 |
| 项目管理 | `/admin/projects` | 全平台项目查看与治理 |

---

## 七、接口约定

### 统一响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

`code` 为 200 表示成功；401 表示未登录 / token 失效；403 表示无权限；其余为业务错误码（见 `ResultCode`）。

### 认证方式

登录后返回 `token`，前端存入 localStorage，后续请求自动携带请求头：

```
Authorization: Bearer <token>
```

后端 `AuthInterceptor` 拦截 `/api/**`：

- 标 `@IgnoreAuth` 的接口放行
- 其余接口校验 JWT 并把用户信息写入 `UserContext`（ThreadLocal）
- 标 `@RequireRole(RequireRole.Role.XXX)` 的接口额外校验角色

### 接口编号体系

| 前缀 | 模块 |
|---|---|
| `/api/auth/**` | 01 认证与注册 |
| `/api/file/**` | 02 文件上传 |
| `/api/verify/**` | 03 认证审核 |
| `/api/profile/**` | 04 个人 / 企业档案 |
| `/api/project/**` | 05 项目作品集 |
| `/api/review/**` | 06 专家点评 |
| `/api/company/**` | 07 企业端 |
| `/api/invitation/**` | 08 面试邀约 |
| `/api/admin/**` | 09 平台运营 |
| `/api/common/**` | 00 公共数据 |

---

## 八、文件存储说明

采用**本地磁盘存储**，无外部对象存储依赖。

- 上传根目录：`server/uploads/`（可通过 `xiaozhan.upload.base-path` 修改）
- 落盘规则：`{basePath}/{bizType}/{yyyy}/{MM}/{uuid}.{ext}`
- 业务类型 `bizType`：`AVATAR` 头像、`COVER` 项目封面、`ASSET` 项目素材、`VERIFY` 认证材料、`EVIDENCE` 申诉证据
- 访问方式：静态资源映射 `/files/**` → 上传目录

**视频处理**：不做转码，原文件直接存储与回放。请留意单文件大小限制（见 `application.yml` 中 `spring.servlet.multipart.max-file-size`，默认 200MB）。

---

## 九、核心业务规则

| 规则 | 说明 |
|---|---|
| 学籍认证 | 未认证学生对外仅显示昵称，认证后才展示真实姓名 |
| 专家点评门槛 | 需专家认证通过；评语最短 50 字 |
| 重复点评限制 | 同一专家对同一项目 30 天内不可重复点评 |
| 每日点评额度 | 按专家等级授予（BRONZE 20 / SILVER 30 / GOLD 50），超出后次日恢复 |
| 敷衍点评识别 | 四项评分全同 + 未填写优缺点 + 评语 < 80 字 → 标记 `QUALITY_SUSPECT` 进入人工复核 |
| 点评回写 | 提交点评后自动回写项目均分、专家业绩分（+10）、并把项目移出求点评池 |
| 企业查验额度 | 每次查看候选人完整档案消耗 1 次月度额度 |
| 合规留痕 | 企业查验记录对候选人与平台均可见 |

---

## 十、常见问题

**Q：启动报 `Invalid value type for attribute 'factoryBeanObjectType': java.lang.String`？**
A：这是 MyBatis-Plus 3.5.5 传递依赖的 `mybatis-spring 3.0.2` 与 Spring Boot 3.2（Spring Framework 6.1）不兼容导致的。
`pom.xml` 已显式升级 `mybatis-spring` 到 `3.0.3` 修复。如果你的依赖被本地缓存污染，执行一次
`mvn -U clean compile` 强制刷新即可。

**Q：调用接口报 `Unknown column 'update_time' in 'field list'`？**
A：早期 `schema.sql` 漏建了部分表的 `update_time` 字段。执行补丁：
```bash
mysql -u root -p xiaozhan < sql/fix_update_time.sql
```

**Q：后端启动报数据库连接失败？**
A：确认 MySQL 已启动、`xiaozhan` 库已导入、`application.yml` 账号密码正确。

**Q：端口不是 8080 而是别的端口？**
A：检查环境变量里是否有 `SERVER__PORT`（双下划线）覆盖了配置。IDEA 等工具会注入该变量，
可用 `--server.port=8080` 显式指定。

**Q：前端页面接口 404 / 跨域？**
A：确认后端已在 8080 端口运行。前端所有请求走 Vite 代理，无需额外配置。

**Q：上传的文件访问 404？**
A：确认 `server/uploads/` 目录已生成且应用有写权限；静态映射前缀为 `/files/**`。

**Q：视频上传失败？**
A：默认单文件上限 200MB，可在 `application.yml` 调整 `max-file-size` / `max-request-size`。

**Q：npm install 很慢？**
A：可切换镜像：
```bash
npm install --registry=https://registry.npmmirror.com
```

**Q：登录后点击左侧菜单，页面空白没内容？**
A：这是「模板引用了 `<script setup>` 里不存在的变量」导致的组件渲染崩溃。此类问题**构建期完全静默**
（`vite build` 会成功），只在运行时求值那一刻抛 `Cannot read properties of undefined`，整棵子树渲染失败。
本项目已修复 3 处：
- `views/student/Profile.vue` — `profile.allowCompanySearch` → 改为既有的 `allowSearch`
- `views/student/ProjectEdit.vue` — `project.inReviewPool` 未定义 → 改为固定文案「保存并发布」
- `views/admin/Users.vue` — `adminApi.setUserStatus` 不存在 → 改为 `adminApi.toggleUserStatus`

如果后续改动又出现白屏，用两步快速定位：
1. 浏览器 F12 看 Console 是否有 `Cannot read properties of undefined (reading 'xxx')`，直接 grep 那个 `xxx`；
2. 用 `document.getElementById('app').innerHTML.length` 判断范围——返回 `0` 是根组件挂了，
   返回几千但内容空是 `router-view` 里的子组件挂了。

**Q：`curl http://localhost:5173` 返回 502，是前端没起来吗？**
A：不一定。若环境里存在 `HTTP_PROXY` / `HTTPS_PROXY`（如 IDE 注入的本地代理），
curl 会被代理劫持而返回 502，**不是** connection refused。加 `--noproxy '*'` 重试即可确认：
```bash
curl --noproxy '*' -o /dev/null -w "%{http_code}\n" http://127.0.0.1:5173/
```

---

## 十一、构建验证

```bash
# 后端编译
cd server && mvn clean package -DskipTests

# 前端构建
cd web && npm run build
```

两项均通过即代表工程完整可用。本项目已在本地完成**运行时端到端验证**：
19 张表建库导入 → 后端在 8080 启动 → 四类角色（学生 / 专家 / 企业 / 管理员）登录成功
→ 项目创建与素材提交 → 专家点评 → 学生回复与申诉 → 企业收藏与邀约 → 认证提交，
全链路接口返回 `code=200`，鉴权（401）与角色越权（403）拦截均生效。

前端还完成了**全站 27 个页面的浏览器实测巡检**（四角色逐页登录跳转 + SPA 内导航），
每页 DOM 渲染非空、控制台零运行时错误，`vite build` EXIT=0。

