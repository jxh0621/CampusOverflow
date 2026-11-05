# Sprint 3 记录（第6-7周）

## Sprint Goal
用户能搜到问题、看到相关推荐并收到通知；移除全部行走骨架；端到端链路全绿。

## 计划：纳入的 PBI
- PB-31：搜索
- PB-32：搜索投影
- PB-33：相关推荐
- PB-34：通知
- PB-35：@提及
- PB-36：通知中心
- PB-37：端到端贯通
- PB-38：demo 数据
- PB-39：Discovery 应用层测试

## 完成情况
- ✅ Discovery 领域层完成（SearchQuery/RelatedQuestionPolicy/MentionParser/Notification）
- ✅ Discovery 应用层完成（SearchIndexProjector/NotificationProjector/DiscoveryQueryService/NotificationService）
- ✅ 行走骨架全部移除
- ✅ 通知中心前端
- ✅ DemoDataSeeder 完成（6账号/1课程/3问题/全链路演示）
- ✅ 测试：DiscoveryDomainTest/NotificationProjectorTest（2个幂等测试）

## 数据
- 完成点数：31 SP
- CI 红灯次数：1次
- 团队速度：31 SP/2周

## 回顾
- 做得好：从空壳到完整实现，端到端链路打通
- 待改进：搜索性能优化留到后续
