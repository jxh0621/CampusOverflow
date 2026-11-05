# Sprint 2 记录（第4-5周）

## Sprint Goal
问答行为能够异步、幂等地转化为声誉、徽章、悬赏与排行榜；采纳后回答者声誉+15，悬赏可以发起、结算与到期退款。

## 计划：纳入的 PBI
- PB-20：开户
- PB-21：采纳计分
- PB-22：投票计分
- PB-23：徽章
- PB-24：发起悬赏
- PB-25：悬赏结算
- PB-26：排行榜
- PB-27：声誉档案
- PB-28：对账
- PB-29：前端悬赏对话框
- PB-30：Reputation 应用层单元测试

## 完成情况
- ✅ Reputation 领域层完成（ReputationAccount/ReputationRulebook/Bounty/BadgePolicy/AcademicTerm）
- ✅ Reputation 应用层完成（ReputationEventHandlers/BountyService/BadgeService/LeaderboardService/ReconciliationService）
- ✅ 事件幂等处理（账本唯一键）
- ✅ 前端：悬赏对话框、排行榜页
- ✅ 测试：BountyAndAccountTest/ReputationRulebookTest/ReputationEventHandlersTest（5个幂等测试）

## 数据
- 完成点数：42 SP
- CI 红灯次数：2次
- 团队速度：42 SP/2周

## 回顾
- 做得好：幂等设计到位，事件重放测试覆盖
- 待改进：应用层测试起步晚，应该在开发时同步写
