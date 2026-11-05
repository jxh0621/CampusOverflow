# Sprint 2 Backlog 任务分解

## Sprint Goal
问答行为能够异步、幂等地转化为声誉、徽章、悬赏与排行榜；采纳后回答者声誉+15，悬赏可以发起、结算与到期退款。

## 任务列表

| 任务 | 内容 | PBI | 模块 | 负责人 | 工时 |
|---|---|---|---|---|---|
| T2-01 | ReputationRulebook.voteEffect/voteDelta/applyDailyCap/initialPoints | PB-20/22 | rep-domain | jxh0621 | 2h |
| T2-02 | ReputationAccount 7个方法（open/applyReputation/freezePoints/spendFrozen/refundFrozen/ensureFrozen/requirePositive） | PB-20/24/25 | rep-domain | jxh0621 | 3h |
| T2-03 | Bounty.open/award/close/isDue/ensureOpen | PB-24/25 | rep-domain | YUYUzi-0221 | 3h |
| T2-04 | BadgePolicy 2个方法 + AcademicTerm 3个方法 | PB-23/26 | rep-domain | YUYUzi-0221 | 3h |
| T2-05 | BadgeService.grant + ReputationEventHandlers.on(UserRegistered/AnswerSubmitted/AnswerEndorsed) | PB-20/23 | rep-application | jxh0621 | 4h |
| T2-06 | ReputationEventHandlers.on(AnswerAccepted/VoteCast)（含每日上限与采纳数徽章） | PB-21/22/23 | rep-application | YUYUzi-0221 | 5h |
| T2-07 | BountyService.open/latest/settleOnAccept/expireDue/closeAndRefund | PB-24/25 | rep-application | jxh0621 | 9h |
| T2-08 | LeaderboardService.on/top（Redis ZSET） | PB-26 | rep-application | YUYUzi-0221 | 4h |
| T2-09 | ReputationQueryService.profile/ledger + ReputationApiImpl | PB-27 | rep-application | jxh0621 | 3h |
| T2-10 | ReconciliationService.reconcile（对账） | PB-28 | rep-application | YUYUzi-0221 | 2h |
| T2-11 | Reputation应用层测试（StubRepo、固定Clock、幂等重复事件） | PB-30 | rep-application test | 全员 | 10h |
| T2-12 | 移除Reputation行走骨架；CI覆盖率加入Reputation模块 | PB-18/04 | CI/pom | jxh0621 | 1h |
| T2-13 | 前端：悬赏对话框、个人声誉主页（徽章墙、流水分页、排行榜周期切换） | PB-29 | frontend | YUYUzi-0221 | 8h |
| T2-14 | 验证demo profile可启动（Discovery监听器仍为空实现） | PB-38 | bootstrap | jxh0621 | 1h |

## 分工总览

| 成员 | 主责 | 工时合计 |
|---|---|---|
| jxh0621 | Reputation领域层(Rulebook/Account)、BountyService、查询、CI | 25h |
| YUYUzi-0221 | Reputation领域层(Bounty/Badge)、事件处理、排行榜、前端、测试 | 35h |

## 幂等测试重点
- on(AnswerAccepted) 调用两次：声誉只加一次，流水不重复记账
- on(VoteCast) 每日正向投票超200：超出部分不计
- 自己回答自己提问被采纳：声誉不变
- Bounty settleOnAccept 重复调用：积分不重复扣
- expireDue 到期退款：frozenPoints正确退回

## DoD 检查清单
- [ ] 代码经PR合入develop，至少1人Approve
- [ ] Reputation领域测试+应用层测试全部通过
- [ ] 幂等测试覆盖每种事件处理器
- [ ] ArchUnit架构测试通过
- [ ] Reputation模块行覆盖率≥70%
- [ ] CI流水线绿灯
