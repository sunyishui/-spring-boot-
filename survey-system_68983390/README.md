# Survey System - 电子问卷管理系统

基于 **Spring Boot 3 + Vue 3 + Element Plus + MySQL** 的全栈电子问卷管理系统，支持问卷创建、发布、填写、数据统计及 AI 智能生成。

## 功能概览

**管理员端**
- 问卷全生命周期管理（创建 → 编辑 → 发布 → 结束 → 删除）
- 题目管理：单选题、多选题、填空题，支持拖拽排序
- 数据统计可视化：柱状图、饼图、词云图
- 答卷数据查看与 Excel 导出
- 用户管理（编辑、删除、禁用/启用）

**AI 智能功能**
- AI 智能生成问卷：输入描述自动生成完整问卷（标题 + 题目 + 选项）
- AI 数据分析报告：一键生成问卷数据分析报告
- AI 填空题摘要：对大量文本回答智能归纳总结

**用户端**
- 注册 / 登录
- 浏览已发布问卷、填写提交（同一问卷不可重复提交）
- 查看个人答卷记录

## 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| 后端框架 | Spring Boot 3.2.5 | Java 17 |
| ORM | MyBatis-Plus 3.5.5 | 分页、逻辑删除、自动填充 |
| 安全认证 | Spring Security + JWT | 无状态鉴权 |
| 数据库 | MySQL 8.0 | utf8mb4 字符集 |
| AI 接口 | 通义千问 (DashScope) | OpenAI 兼容协议 |
| HTTP 客户端 | OkHttp 4 | 调用 AI API |
| 数据导出 | Apache POI | Excel 导出 |
| API 文档 | Knife4j | Swagger 增强 |
| 前端框架 | Vue 3 | Composition API + `<script setup>` |
| UI 组件 | Element Plus | — |
| 构建工具 | Vite 5 | — |
| 路由 | Vue Router 4 | 导航守卫 + 角色鉴权 |
| 状态管理 | Pinia | — |
| 可视化 | ECharts 5 + echarts-wordcloud | 柱状图、饼图、词云 |

## 项目结构

```
survey-system/
├── backend/                              # Spring Boot 后端
│   ├── pom.xml
│   ├── sql/
│   │   └── init.sql                      # 数据库初始化（建表 + 测试数据）
│   └── src/main/java/com/survey/
│       ├── SurveyApplication.java        # 启动类
│       ├── config/                       # 安全、CORS、MyBatis、异常处理
│       ├── controller/                   # REST 接口（6个控制器）
│       ├── dto/                          # 请求/响应数据对象
│       ├── entity/                       # 数据库实体（6张表）
│       ├── mapper/                       # MyBatis Mapper 接口
│       ├── security/                     # JWT 工具 + 认证过滤器
│       ├── service/                      # 业务逻辑层
│       └── util/                         # 通用工具类
│
└── frontend/                             # Vue 3 前端
    ├── package.json
    ├── vite.config.js                    # 开发代理配置
    └── src/
        ├── api/                          # Axios 封装 + 接口定义
        ├── router/                       # 路由 + 权限守卫
        ├── store/                        # Pinia 用户状态
        └── views/
            ├── auth/                     # 登录、注册
            ├── admin/                    # 管理后台（6个页面）
            └── user/                     # 用户端（3个页面）
```

## 数据库设计

| 表名 | 说明 |
|------|------|
| `sys_user` | 用户表（管理员/普通用户） |
| `survey` | 问卷表（草稿/已发布/已结束） |
| `question` | 题目表（RADIO/CHECKBOX/INPUT） |
| `question_option` | 选项表 |
| `answer_sheet` | 答卷表（用户+问卷唯一约束） |
| `answer` | 答案表（选项ID/文本内容） |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 16+
- MySQL 8.0+

### 1. 初始化数据库

```bash
mysql -u root -p < backend/sql/init.sql
```

脚本会自动创建 `survey_system` 数据库、6 张表，并插入测试数据（管理员账号、示例问卷、答卷数据）。

### 2. 启动后端

```bash
cd backend

# 按需修改数据库配置（默认 root/123456）
# vim src/main/resources/application.yml

mvn spring-boot:run
```

看到 `Started SurveyApplication` 即启动成功，后端运行在 `http://localhost:8080`。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 `http://localhost:5173`，开发模式下自动代理 API 请求到后端。

### 4. 登录系统

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 测试用户 | zhangsan / lisi / wangwu / zhaoliu | 123456 |

## AI 功能配置

AI 功能依赖阿里云通义千问 API，需要配置 API Key：

1. 前往 [阿里云 DashScope 控制台](https://dashscope.console.aliyun.com/) 注册并获取 API Key
2. 修改 `backend/src/main/resources/application.yml`：

```yaml
ai:
  dashscope:
    api-key: sk-your-api-key-here    # 替换为你的 API Key
    model: qwen-turbo
```

未配置 API Key 时，AI 相关功能（智能生成问卷、分析报告、填空题摘要）将不可用，其他功能正常使用。

## API 文档

启动后端后访问 http://localhost:8080/doc.html 查看 Knife4j API 文档。

主要接口：

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 认证 | POST | `/api/auth/login` | 登录 |
| 认证 | POST | `/api/auth/register` | 注册 |
| 问卷 | GET/POST/PUT/DELETE | `/api/surveys` | 问卷 CRUD |
| 题目 | GET/POST/PUT/DELETE | `/api/questions` | 题目 CRUD |
| 答卷 | POST | `/api/answers/submit` | 提交答卷 |
| 统计 | GET | `/api/answers/statistics/{id}` | 统计数据 |
| 导出 | GET | `/api/answers/export/{id}` | Excel 导出 |
| AI | POST | `/api/ai/generate-survey` | AI 生成问卷 |
| AI | GET | `/api/ai/analyze/{id}` | AI 分析报告 |
| AI | GET | `/api/ai/summarize/{id}` | AI 文本摘要 |

## 预置测试数据

init.sql 包含以下测试数据，方便开箱即用：

- **6 个用户**：1 个管理员 + 5 个普通用户（含 1 个已禁用）
- **3 份问卷**：
  - 「大学生网购消费习惯调查」— 已发布，5 道题，4 份答卷
  - 「员工工作满意度调研」— 已结束，5 道题，3 份答卷
  - 「校园食堂满意度调查」— 草稿，3 道题
- **13 道题目**：覆盖单选、多选、填空三种类型
- **7 份答卷**：含完整作答数据，可直接查看统计图表

## License

MIT
