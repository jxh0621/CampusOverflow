# 团队工作约定

## 分支策略

```
main（保护，打标签）
  ↑ merge from develop
develop（保护，集成分支）
  ↑ merge from feature/*
feature/<姓名拼音>-<任务简述>（个人开发）
```

- main 不允许直推，PR 必须经评审
- develop 不允许直推，PR 必须经评审
- feature 分支命名：`feature/jxh-question-post` / `feature/yuyu-reputation`
- 修复 hotfix：`hotfix/<描述>`，从 main 拉出，修复后合回 main + develop

## 提交规范（Conventional Commits）

| 前缀 | 用途 |
|---|---|
| `feat:` | 新功能 |
| `fix:` | 修 bug |
| `docs:` | 文档变更 |
| `refactor:` | 重构（不改功能） |
| `test:` | 测试相关 |
| `chore:` | 构建/工具/依赖 |

示例：`feat: 实现Question.post工厂方法及标签规范化`

## PR 流程

1. 从 develop 切 feature 分支
2. 完成开发 + 本地测试通过
3. Push 到远端，发 PR 到 develop
4. PR 模板填写：关联PBI、测试情况、截图/录屏
5. 队友 Review，提出修改意见或 LGTM
6. 合并后删除 feature 分支

## 看板看板列定义

| 列 | 含义 |
|---|---|
| To Do | 待开始 |
| In Progress | 进行中（每人最多同时1个） |
| In Review | 待Review |
| Done | 已合并到develop |

## 三周增量计划

| Sprint | 目标 | 核心交付 |
|---|---|---|
| S0（第1周） | 基础设施就绪 | 仓库、CI、Backlog、文档框架 |
| S1（第2-3周） | Q&A闭环 | 提问→回答→投票→评论→采纳 |
| S2（第4-5周） | 声誉激励 | 声誉记账、徽章、悬赏、排行榜 |
