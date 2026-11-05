# 事件风暴记录

## Big Picture 事件流（按时间顺序）

```
用户注册 → UserRegisteredEvent
  ↓
发布问题 → QuestionPostedEvent
  ↓
编辑问题 → QuestionEditedEvent
  ↓
提交回答 → AnswerSubmittedEvent
  ↓
教师认证回答 → AnswerEndorsedEvent
  ↓
投票/改投/撤销 → VoteCastEvent
  ↓
发表评论 → CommentPostedEvent
  ↓
采纳最佳答案 → AnswerAcceptedEvent
  ↓
软删除内容 → ContentDeletedEvent
  ↓
（声誉域）记账 → ReputationChangedEvent
  ↓
（声誉域）授予徽章 → BadgeAwardedEvent
  ↓
（声誉域）悬赏发起/结算/关闭 → BountyOpenedEvent / BountyAwardedEvent / BountyClosedEvent
```

## Process Modeling（命令与角色）

| 命令 | 触发角色 | 聚合 |
|---|---|---|
| register / createStaff | 学生自助 / 管理员 | User |
| createCourse / addMember | 管理员 / 教师 | Course |
| postQuestion | 学生 | Question |
| editQuestion / deleteQuestion | 作者 / 课程管理者 | Question |
| submitAnswer | 学生 | Answer |
| editAnswer / deleteAnswer / endorseAnswer | 作者/管理者 / 教师 | Answer |
| castVote | 登录用户 | Vote |
| postComment | 登录用户 | Comment |
| acceptAnswer | 提问者 / 课程管理者 | Question |
| openBounty | 课程管理者 | Bounty |
| settleBounty / expireBounty | 系统定时任务 | Bounty |

## 外部系统

- 百度翻译API（翻译功能）
- Redis（会话、缓存、排行榜ZSET）
- MySQL（业务数据、全文索引）

## 热点事件

1. **AnswerAcceptedEvent**：触发链最长——Q&A→Reputation(+15、徽章、悬赏结算)→Discovery(标记已采纳、通知)
2. **VoteCastEvent**：Reputation计分 + Discovery问题得分同步
3. **ContentDeletedEvent**：Discovery标记删除 + Reputation排行榜更新
