## 变更说明

<!-- 简述这次改动做了什么、为什么改 -->

## 关联事项

- 关联 Issue / 需求：#
- 影响的限界上下文：<!-- Identity / Q&A / Reputation / Discovery / 共享内核 / 启动装配 / 前端 -->

## 变更类型

- [ ] 新功能（feature）
- [ ] 缺陷修复（fix）
- [ ] 重构（refactor，不改变外部行为）
- [ ] 文档（docs）
- [ ] 构建 / CI / 配置（chore）

## 自测情况

- [ ] `mvn -q verify`（后端单元测试 + ArchUnit + 覆盖率门禁）通过
- [ ] `mvn -q verify -Dsurefire.skip=true`（Testcontainers 集成测试）通过
- [ ] 前端 `npm run typecheck` 与 `npm run build` 通过
- [ ] 涉及数据库变更时已补 Flyway 迁移脚本
- [ ] 涉及接口变更时已同步更新 `docs/`

## 评审关注点

<!-- 希望评审人重点看哪里，例如事务边界、事件幂等、权限校验 -->

## 截图 / 日志（可选）

<!-- 前端改动请附图，接口改动请附请求响应示例 -->
