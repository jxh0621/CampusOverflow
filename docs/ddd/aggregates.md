# 聚合识别清单

## Q&A 限界上下文

### 聚合根：Question（问题）
- **实体/值对象**：Question（聚合根）、MarkdownBody（值对象）、Tags（值对象）、Tag（值对象）
- **关键不变式**：
  - 一个问题最多一个被采纳答案，采纳后不可更改（QA-2002）
  - 只有提问者或课程管理者可以采纳（QA-3001）
  - 被采纳的回答必须属于本题且未删除（QA-1010/QA-1011）
  - 已删除的问题不能再回答/编辑/采纳（QA-1020）
  - 提问者只能删除尚无回答的问题，课程管理者可删除任意问题（QA-1013/QA-3003）
- **仓储接口**：QuestionRepository（domain层）

### 聚合根：Answer（回答）
- **实体/值对象**：Answer（聚合根）、MarkdownBody（值对象）
- **关键不变式**：
  - 被采纳的回答不能由作者删除（QA-1012）
  - 作者可删自己的回答；课程管理者可删；路人403（QA-3003）
  - 教师认证每个回答只能一次（QA-2003）
- **仓储接口**：AnswerRepository（domain层）

### 聚合根：Vote（投票）
- **值对象**：Vote（独立聚合，voterId+targetType+targetId唯一）
- **关键不变式**：
  - 不能给自己的内容投票（QA-1015）
  - 值只能为{-1, 0, +1}（QA-1014）

### 实体：Comment（评论，独立聚合）
- **关键不变式**：
  - 正文2-600字符（QA-1003）
  - 回复只允许一层，必须挂在同一目标上（QA-1016/QA-1017）

## Reputation 限界上下文

### 聚合根：ReputationAccount（声誉账户）
- **值对象**：LedgerEntry（只追加流水）
- **关键不变式**：
  - reputation = 流水之和（FF-8）
  - 任何声誉变化都先写rep_ledger，唯一键(eventId, userId, reason)

### 聚合根：Bounty（悬赏）
- **状态机**：OPEN → AWARDED（采纳）/ EXPIRED（到期），终态不可变
- **关键不变式**：
  - 悬赏积分10-500，期限1-14天（REP-1003/REP-1004）
  - 只有课程管理者可发起（REP-3001）

## Discovery 限界上下文

### 聚合根：Notification（通知）
- **关键不变式**：
  - 唯一键(sourceEventId, recipientId)
  - 只有接收者能标记已读（DIS-3001）

### 值对象：SearchDocument / SearchHit
- 只读投影，由事件处理器写入，不包含业务行为

## Identity 限界上下文（参考实现）

### 聚合根：User
### 聚合根：Course / CourseMember
