# CampusOverflow v1.0.0 发布说明

## 新功能

### Q&A 问答核心
- 发布/编辑/删除问题（标签、课程关联）
- 提交/编辑/删除回答
- 投票（赞/踩/撤销），幂等处理
- 采纳最佳答案
- 教师认证优质回答
- 评论与@提及
- 热门标签

### 声誉与激励
- 声誉账户（开户、计分、每日上限200）
- 徽章系统（首次回答、首次采纳、教师认证）
- 悬赏系统（发起/结算/到期退款）
- 排行榜（周/月/学期/总榜）
- 对账功能

### 内容发现
- 中文全文搜索（ngram分词）
- 相关问题推荐
- 通知中心（回答/采纳/认证/悬赏/徽章/@提及）

### 前端
- 9个页面：登录/注册、问题列表、提问、问题详情、课程、通知、排行榜、个人资料、管理后台
- Markdown 实时预览
- 悬赏对话框
- 响应式布局

## 技术架构
- 模块化单体 × 领域驱动设计
- 6个限界上下文，27个Maven模块
- 事务性发件箱模式（事件最终一致）
- 事件幂等处理（账本唯一键）
- Docker Compose 一键部署

## 部署说明

```bash
# 1. 启动中间件
docker-compose up -d mysql redis

# 2. 启动后端
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 3. 启动前端
cd ../frontend
npm install && npm run dev

# 4. 访问
# 前端: http://localhost:5173
# 后端 API: http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
```

## 演示账号
- 管理员：admin01 / Campus2026
- 教师：T2001 / Campus2026
- 学生：2023001 / Campus2026（小明）

## 已知问题
- Grafana 面板未配置（仅有 Prometheus 指标）
- 通知实时刷新为30秒轮询（未做SSE）
- 压测脚本未提供
