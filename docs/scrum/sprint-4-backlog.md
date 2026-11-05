# Sprint 4 Backlog 任务分解

## Sprint Goal
系统稳定化、体验打磨、补齐可观测性，完成 v1.0.0 发布与最终答辩。

## 任务列表

| 任务 | 内容 | PBI | 模块 | 负责人 | 工时 |
|---|---|---|---|---|---|
| T4-01 | 用户禁用/启用服务（已有服务层，补接口和前端管理页） | PB-40 | identity/interfaces | jxh0621 | 5h |
| T4-02 | 个人资料页（学院/班级/昵称展示编辑） | PB-41 | frontend | YUYUzi-0221 | 3h |
| T4-03 | 前端体验优化：分页/骨架屏/错误重试/Markdown实时预览 | PB-42 | frontend | YUYUzi-0221 | 5h |
| T4-04 | 通知红点轮询（SSE替代，30s轮询） | PB-43 | frontend+interfaces | jxh0621 | 3h |
| T4-05 | Grafana Dashboard配置（QPS/延迟/错误率/发件箱积压） | PB-44 | deploy/ | jxh0621 | 2h |
| T4-06 | 压测脚本（wrk/locust 压测问题列表） | PB-45 | scripts | YUYUzi-0221 | 3h |
| T4-07 | docker-compose一键部署验证 + Release Notes | PB-46 | deploy/ | 全员 | 2h |
| T4-08 | 管理员页面（认证学生/创建教职工） | PB-47 | frontend | YUYUzi-0221 | 3h |

## 分工总览

| 成员 | 主责 | 工时合计 |
|---|---|---|
| jxh0621 | 后端管理接口、通知轮询、Grafana、部署 | 12h |
| YUYUzi-0221 | 前端全部页面、压测脚本 | 14h |

## 发布检查
- [ ] main 分支打标签 v1.0.0
- [ ] docker compose up 一键启动全部服务
- [ ] Prometheus 指标可访问
- [ ] Swagger UI 可访问
- [ ] 端到端演示脚本走通
- [ ] Release Notes 编写完成
