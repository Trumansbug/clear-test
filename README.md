# 智力测试问卷系统

这是一个基于 Vue + Spring Boot 的智力测试问卷系统。

## 项目结构

```
.
├── backend/          # 后端项目
│   ├── src/         # 源代码
│   └── pom.xml      # Maven 配置文件
└── frontend/        # 前端项目
    ├── src/         # 源代码
    └── package.json # npm 配置文件
```

## 技术栈

### 前端
- Vue 3
- Element Plus
- Vue Router
- Vuex
- Axios

### 后端
- Spring Boot
- MyBatis Plus
- MySQL
- Spring Security
- JWT

## 功能特性

1. 用户权限管理
   - 基于 JWT 的身份认证
   - 用户管理
   - 角色管理

2. 试卷管理
   - 试卷的创建、编辑、删除
   - 支持单选题、多选题、填空题
   - 试卷状态管理（未发布/已发布）
   - 自动评分功能

3. 系统功能
   - 全局导航栏
   - 响应式布局
   - 权限控制

## 开发环境要求

- JDK 1.8+
- Node.js 14+
- MySQL 5.7+

## 运行说明

### 后端
1. 创建数据库：
```sql
CREATE DATABASE iq_test;
```

2. 修改数据库配置：
编辑 `backend/src/main/resources/application.yml` 文件中的数据库连接信息。

3. 运行后端服务：
```bash
cd backend
mvn spring-boot:run
```

### 前端
1. 安装依赖：
```bash
cd frontend
npm install
```

2. 运行开发服务器：
```bash
npm run serve
```

## 访问地址

- 前端：http://localhost:8080
- 后端：http://localhost:8081 