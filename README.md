# CampusOverflow · 校园问答平台

[![CI](https://github.com/jxh0621/CampusOverflow/actions/workflows/ci.yml/badge.svg)](https://github.com/jxh0621/CampusOverflow/actions/workflows/ci.yml)
[![version](https://img.shields.io/badge/version-v1.0.0-brightgreen.svg)](https://github.com/jxh0621/CampusOverflow/releases/latest)
[![tests](https://img.shields.io/badge/unit%20tests-61%20passing-blue.svg)]()
[![java](https://img.shields.io/badge/Java-21-orange.svg)]()
[![react](https://img.shields.io/badge/React-18-61dafb.svg)]()

> 《软件工程》课程贯穿项目 · 模块化单体 × 领域驱动设计 × Scrum

## 项目状态：v1.0.0

✅ 4 个业务限界上下文（Identity / Q&A / Reputation / Discovery）全部实现
✅ 61 个单元测试全绿，ArchUnit 架构约束通过
✅ 前端 9 个页面全链路闭环
✅ DDD 设计文档 + Scrum 记录 + ADR 001–008

## 功能概览

### Q&A 问答核心
- 发布 / 编辑 / 删除问题（标签、课程关联）
- 提交 / 编辑 / 删除回答
- 投票（赞 / 踩 / 撤销），幂等处理
- 采纳最佳答案、教师认证优质回答
- 评论与 @ 提及、热门标签

### 声誉与激励
- 声誉账户（开户、计分、每日正向投票上限 200）
- 徽章系统（首次回答、首次采纳、教师认证）
- 悬赏系统（发起 / 结算 / 到期退款）
- 排行榜（周 / 月 / 学期 / 总榜）与对账功能

### 内容发现
- 中文全文搜索（ngram 分词）
- 相关问题推荐
- 通知中心（回答 / 采纳 / 认证 / 悬赏 / 徽章 / @ 提及）

### 前端
- 9 个页面：登录 / 注册、问题列表、提问、问题详情、课程、通知、排行榜、个人资料、管理后台
- Markdown 实时预览、悬赏对话框、响应式布局

## 技术架构

- 模块化单体 × 领域驱动设计：4 个业务限界上下文 + 共享内核 `co-shared-kernel` + 启动装配 `co-bootstrap`，共 **27 个 Maven 模块**
- 事务性发件箱模式（事件最终一致），消费端事件幂等（账本唯一键）
- 服务端会话存于 Redis；表结构由 Flyway 迁移管理
- Docker Compose 一键部署 MySQL 8 + Redis 7；GitHub Actions 流水线（后端构建 / 测试 / 覆盖率 / 集成测试，前端类型检查与构建，镜像构建）

## 五分钟跑起来

### 环境要求
JDK 21、Maven 3.9+、Node.js 20+、Docker Desktop（MySQL 8 + Redis 7）。

```bash
# 1) 启动中间件
docker compose -f deploy/docker-compose.yml up -d mysql redis

# 2) 启动后端
#    dev  —— 本地开发，空库，自行注册账号
#    demo —— 自动播种管理员 / 教师 / 助教 / 3 名学生与示例问答，供课堂演示
cd backend
mvn spring-boot:run -pl co-bootstrap -Dspring-boot.run.profiles=demo

# 3) 启动前端
cd ../frontend
npm install && npm run dev      # http://localhost:5173
```

接口文档（Swagger UI）：http://localhost:8080/swagger-ui.html
后端 API：http://localhost:8080

## 演示账号

以下账号由 `demo` profile 的 `DemoDataSeeder` 自动播种，密码统一为 `Campus2026`：

| 角色 | 用户名 | 显示名 |
|---|---|---|
| 管理员 | admin01 | 系统管理员 |
| 教师 | T2001 | 王老师 |
| 助教 | TA3001 | 李助教 |
| 学生 | 2023001 | 小明 |
| 学生 | 2023002 | 小红 |
| 学生 | 2023003 | 小刚 |

## 质量保障

| 项目 | 要求 | 自检命令 |
| --- | --- | --- |
| 单元测试 | 全部通过 | `cd backend && mvn test` |
| 领域层覆盖率 | ≥ 70%，门禁通过 | `mvn verify` |
| 架构约束 | ArchUnit 全部通过（领域层零框架依赖、跨上下文只经 api、无环、实体表名前缀） | `mvn test -pl co-bootstrap -Dtest=ArchitectureTest` |
| 集成测试 | 端到端链路通过（提问 → 回答 → 采纳 → 声誉 → 搜索 → 通知） | `mvn verify`（需要 Docker） |
| 前端 | 类型检查与构建通过 | `cd frontend && npm run typecheck && npm run build` |
| 流水线 | GitHub Actions 全绿 | 推送后查看 Actions 页 |

## 文档索引

- [docs/adr/](docs/adr/)：8 条架构决策记录（模块化单体、发件箱、Redis 会话、声誉账本、中文检索、软删除、契约优先、领域与持久化分离）
- [docs/ddd/](docs/ddd/)：上下文映射、聚合识别、事件目录、统一语言词汇表
- [docs/业务错误码.md](docs/业务错误码.md)：错误码分段与 HTTP 状态映射
- [docs/scrum/](docs/scrum/)：Sprint 计划、Backlog 分解与回顾
- [docs/developer-guide.md](docs/developer-guide.md)：开发者须知、环境与分支规范

## 已知问题

- Grafana 面板未配置（仅有 Prometheus 指标）
- 通知实时刷新为 30 秒轮询（尚未改 SSE）
- 压测脚本未提供

## 团队成员

- jxh0621（组长）：Q&A 领域与应用服务、权限及事件编排
- YUYUzi-0221：声誉与发现模块、前端联调
