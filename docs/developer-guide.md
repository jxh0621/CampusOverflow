# CampusOverflow 开发者须知

## 环境要求
- JDK 21
- Maven 3.9+
- Node.js 20+
- Docker Desktop（MySQL 8 + Redis 7）

## 快速启动

```bash
# 1. 启动依赖中间件
docker compose -f deploy/docker-compose.yml up -d mysql redis

# 2. 后端编译测试
cd backend
mvn clean test

# 3. 启动后端
mvn spring-boot:run -pl co-bootstrap -Dspring-boot.run.profiles=dev

# 4. 前端
cd frontend
npm install
npm run dev
```

## 常用地址
- 后端 API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- 前端: http://localhost:5173
- Prometheus: http://localhost:9090

## 教师/管理员测试账号
```sql
-- 进入容器
docker exec -it co-mysql mysql -ucampus_dev -pcampus_dev_pwd campus_overflow
-- 提升为教师
UPDATE co_identity..id_user SET role = 'TEACHER', verified = TRUE WHERE username = 'T2001';
-- 提升为管理员
UPDATE co_identity..id_user SET role = 'ADMIN', verified = TRUE WHERE username = 'admin01';
```

## 分支策略
- main：保护分支，只接受PR合并
- develop：集成分支
- feature/<姓名>-<任务>：个人开发

## 提交规范
`feat: / fix: / docs: / refactor: / test: / chore:`
