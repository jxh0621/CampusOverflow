# Sprint 3 Backlog 任务分解

## Sprint Goal
用户能用中文关键词搜到问题、看到相关推荐并收到通知；移除全部行走骨架，端到端测试全绿。

## 任务列表

| 任务 | 内容 | PBI | 模块 | 负责人 | 工时 |
|---|---|---|---|---|---|
| T3-01 | SearchQuery.terms() 与 terms(String)（清洗、分词、去重） | PB-31 | dis-domain | jxh0621 | 1h |
| T3-02 | RelatedQuestionPolicy.jaccard/rank（标签相似度Jaccard） | PB-33 | dis-domain | jxh0621 | 2h |
| T3-03 | MentionParser.extract + Notification.create/markRead | PB-34/35/36 | dis-domain | YUYUzi-0221 | 2h |
| T3-04 | SearchIndexProjector 10个方法（事件→投影更新） | PB-32 | dis-application | YUYUzi-0221 | 6h |
| T3-05 | DiscoveryQueryService.search/related | PB-31/33 | dis-application | jxh0621 | 4h |
| T3-06 | NotificationProjector 8个方法 | PB-34/35 | dis-application | YUYUzi-0221 | 6h |
| T3-07 | NotificationService 5个方法 | PB-36 | dis-application | jxh0621 | 3h |
| T3-08 | Discovery应用层测试（幂等、自己回答不通知、@不存在被忽略） | PB-39 | dis-application test | 全员 | 6h |
| T3-09 | 移除全部行走骨架；CampusOverflowFlowIT端到端测试全绿 | PB-37 | bootstrap/CI | jxh0621 | 5h |
| T3-10 | 根POM domain覆盖率minimum改0.70并保证通过 | PB-37 | pom | jxh0621 | 2h |
| T3-11 | 前端：搜索页（筛选/排序/标签/悬赏）、相关问题栏、通知未读高亮 | PB-31/33/36 | frontend | YUYUzi-0221 | 8h |
| T3-12 | 用demo profile完整走演示数据；全员回归测试与缺陷修复 | PB-38 | 全员 | 全员 | 4h |

## 分工总览

| 成员 | 主责 | 工时合计 |
|---|---|---|
| jxh0621 | Discovery领域(SearchQuery/RelatedPolicy)、查询服务、NotificationService、CI门禁 | 23h |
| YUYUzi-0221 | Discovery领域(Mention/Notification)、投影器、通知服务、前端 | 26h |

## 端到端验收链路（CampusOverflowFlowIT）
1. 注册学生A → 学生B
2. A发问题 → B回答 → A采纳
3. 验证：B声誉+15、B收到通知、搜索能搜到该问题
4. A给B的回答点赞 → B声誉+10
5. 评论@B → B收到通知
6. 管理员对账接口返回空

## DoD 检查清单
- [ ] mvn verify 全绿（含Testcontainers端到端）
- [ ] 行走骨架全部移除
- [ ] Discovery模块覆盖率≥70%
- [ ] Swagger UI可访问搜索/通知接口
- [ ] 前端搜索→详情→推荐→通知全链路可演示
