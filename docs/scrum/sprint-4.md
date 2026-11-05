# Sprint 4 记录（第8周）

## Sprint Goal
系统稳定化、体验打磨、补齐可观测性，完成 v1.0.0 发布与最终答辩。

## 计划：纳入的 PBI
- PB-40：管理员禁用账号
- PB-41：个人资料页
- PB-42：前端体验优化（分页/骨架屏/Markdown预览）
- PB-43：通知实时刷新
- PB-44：Grafana 面板
- PB-45：压测脚本
- PB-46：docker compose 一键部署
- PB-47：管理页
- PB-48：选课审批（Won't 本期做）

## 完成情况
- ✅ 前端：个人资料页、管理页、Markdown预览、悬赏对话框
- ✅ docker-compose 一键部署
- ✅ Prometheus 指标监控
- ⚠️ Grafana 面板：仅有 prometheus.yml，未配置 dashboard
- ⚠️ 禁用账号服务：后端接口已有，前端管理页可操作
- ⚠️ 压测脚本：未做

## 数据
- 完成点数：26 SP（Must/Should 12个，Could 按容量选做）
- 版本标签：v1.0.0

## 回顾
- 做得好：核心功能完整，文档齐全
- 待改进：可观测性（Grafana）和压测留到后续迭代
