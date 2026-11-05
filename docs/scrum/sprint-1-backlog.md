# Sprint 1 Backlog 任务分解

## Sprint Goal
学生可以完成"提问→回答→投票→评论→采纳"的完整问答闭环；教师可以认证与删除内容。

## 任务列表

| 任务 | 内容 | PBI | 模块 | 负责人 | 工时 |
|---|---|---|---|---|---|
| T1-01 | MarkdownBody.ofQuestion/ofAnswer、Tags.names（工厂方法+校验） | PB-06 | qa-domain | jxh0621 | 2h |
| T1-02 | Question.post/edit/ensureNotDeleted/ensureOpenForAnswers/softDelete | PB-06/12/13 | qa-domain | jxh0621 | 4h |
| T1-03 | Question.accept（验收不变式：权限、未采纳、答案属于本题） | PB-10 | qa-domain | YUYUzi-0221 | 3h |
| T1-04 | Answer.submit/edit/endorse/softDelete/ensureNotDeleted | PB-09/12/13/14 | qa-domain | YUYUzi-0221 | 4h |
| T1-05 | Vote.cast/change/checkValue、Comment.post | PB-11/15 | qa-domain | jxh0621 | 4h |
| T1-06 | 补充领域边界测试：标题/正文长度、6标签、重复采纳、已删除编辑、二次认证 | PB-06~15 | qa-domain test | 全员 | 4h |
| T1-07 | QuestionCommandService.post/edit/delete/load + 新增QuestionCommandServiceTest | PB-06/12/13 | qa-application | jxh0621 | 8h |
| T1-08 | AnswerCommandService 7个方法（submit/edit/endorse/delete/accept等） | PB-09/10/12/13/14 | qa-application | YUYUzi-0221 | 7h |
| T1-09 | VoteService.cast/resolve、CommentService.post | PB-11/15 | qa-application | jxh0621 | 7h |
| T1-10 | QuestionQueryService.list/detail/popularTags + 基于假设模型测试 | PB-07/08/16 | qa-application | YUYUzi-0221 | 10h |
| T1-11 | QaQueryApiImpl.findQuestion（供Reputation/Discovery调用） | PB-17 | qa-application | jxh0621 | 1h |
| T1-12 | 行走骨架：Reputation/Discovery的@EventListener方法暂为空实现 | PB-18 | rep/dis-application | YUYUzi-0221 | 1h |
| T1-13 | 前端：编辑/删除问题与回答、评论回复、错误提示 | PB-19 | frontend | YUYUzi-0221 | 6h |
| T1-14 | CI门禁：Q&A模块行覆盖率≥0.70 | PB-04 | CI/pom | jxh0621 | 2h |
| T1-15 | http冒烟脚本 + Sprint评审演示脚本 | - | http/ | 全员 | 2h |

## 分工总览

| 成员 | 主责 | 工时合计 |
|---|---|---|
| jxh0621 | Q&A领域层(Question/Vote/Comment)、QuestionCommandService、VoteService、CI | 31h |
| YUYUzi-0221 | Q&A领域层(Answer/accept)、AnswerCommandService、QueryService、前端、行走骨架 | 31h |

## DoD 检查清单
- [ ] 代码经PR合入develop，至少1人Approve
- [ ] 领域测试+应用层测试全部通过
- [ ] ArchUnit架构测试通过
- [ ] Q&A模块行覆盖率≥70%
- [ ] CI流水线绿灯
- [ ] Swagger UI可访问
