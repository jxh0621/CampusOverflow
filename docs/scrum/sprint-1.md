# Sprint 1 记录（第2-3周）

## Sprint Goal
学生可以完成"提问→回答→投票→评论→采纳"的完整问答闭环；教师可以认证与删除内容。

## 计划：纳入的 PBI
- PB-06：发布问题
- PB-07：问题列表
- PB-08：问题详情
- PB-09：提交回答
- PB-10：采纳最佳答案
- PB-11：投票
- PB-12：编辑问题/回答
- PB-13：软删除
- PB-14：教师认证
- PB-15：评论
- PB-16：热门标签
- PB-17：QaQueryApi
- PB-18：行走骨架
- PB-19：前端问答闭环

## 完成情况
- ✅ Q&A 领域层全部完成（Question/Answer/Vote/Comment/MarkdownBody/Tags）
- ✅ Q&A 应用层完成（QuestionCommandService/AnswerCommandService/VoteService/CommentService）
- ✅ Q&A 接口层完成
- ✅ 前端问答闭环完成（提问/列表/详情/回答/投票/采纳/评论）
- ✅ 行走骨架（Discovery 空实现）
- ✅ 测试：QuestionTest/AnswerTest/VoteAndCommentTest/AnswerAndVoteServiceTest

## 数据
- 完成点数：43 SP
- CI 红灯次数：3次（编译错误、BOM头、lambda变量）
- 团队速度：43 SP/2周

## 回顾
- 做得好：领域模型设计合理，边界清晰
- 待改进：标签计数 bug 后期才发现，单元测试覆盖不够
