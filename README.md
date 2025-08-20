# 🚀 Mambo AI Platform

<div align="center">

![Mambo AI Platform](mambo.png)

**智能 AI 代码生成平台** - 让 AI 帮你生成完整的 Web 应用

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.x-4FC08D.svg)](https://vuejs.org/)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-Latest-blue.svg)](https://github.com/langchain4j/langchain4j)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

[快速开始](#快速开始) • [功能特性](#功能特性) • [技术架构](#技术架构) • [部署指南](#部署指南) • [贡献指南](#贡献指南)

</div>

## ✨ 项目简介

Mambo AI Platform 是一个基于 **Spring Boot 3 + LangChain4j + Vue 3** 构建的企业级 AI 代码生成平台。通过自然语言描述，AI 可以自动生成完整的 Web 应用，支持多种项目类型和部署方式。

### 🎯 核心亮点

- 🤖 **智能代码生成**: 支持 HTML、多文件项目、Vue 工程等多种生成模式
- 🔄 **实时流式响应**: 基于 SSE 的实时 AI 对话体验
- 📱 **响应式设计**: 完美适配桌面端和移动端
- 🛡️ **企业级架构**: 完整的用户管理、权限控制、监控体系
- 🚀 **一键部署**: 生成的应用可直接访问和部署
- 💎 **精选案例**: 展示高质量 AI 生成应用

## 🚀 快速开始

### 环境要求

- **Java**: JDK 21+
- **Node.js**: 16.0+
- **MySQL**: 5.7+ 或 8.0+
- **Redis**: 5.0+

### 1. 克隆项目

```bash
git clone https://github.com/your-username/mambo-ai-platform.git
cd mambo-ai-platform
```

### 2. 配置环境

```bash
# 复制配置示例文件
cp application-example.yml src/main/resources/application-local.yml

# 编辑配置文件，填入您的API密钥和数据库信息
vim src/main/resources/application-local.yml
```

### 3. 启动后端

```bash
# 使用Maven启动
mvn spring-boot:run -Dspring.profiles.active=local

# 或使用IDE直接运行 MainApplication.java
```

### 4. 启动前端

```bash
cd mambo-ai-platform-frontend
npm install
npm run dev
```

### 5. 访问应用

- **前端应用**: http://localhost:5173
- **后端 API**: http://localhost:8234/api
- **API 文档**: http://localhost:8234/api/doc.html

详细配置指南请参考 [SETUP.md](SETUP.md)

## ✨ 功能特性

### 🎨 AI 代码生成

- **多模式生成**: HTML 单文件、多文件项目、Vue 完整工程
- **智能路由**: 根据需求自动选择最适合的 AI 模型
- **实时预览**: 生成过程中实时查看代码和效果
- **一键部署**: 生成后自动部署，获得可访问链接

### 👥 用户体系

- **用户注册/登录**: 完整的用户认证体系
- **VIP 会员**: 高级功能和无限生成次数
- **个人中心**: 管理个人信息和应用
- **应用管理**: 查看、编辑、删除个人应用

### 🛡️ 管理后台

- **用户管理**: 用户信息查看、VIP 状态管理
- **应用管理**: 全局应用监控、精选设置
- **对话管理**: AI 对话历史追踪
- **数据统计**: 平台使用数据分析

### 🔧 企业特性

- **会话管理**: Redis 持久化的用户会话
- **缓存优化**: 多层缓存提升性能
- **限流保护**: 防止 API 滥用
- **监控告警**: Prometheus + Grafana 监控
- **安全防护**: XSS 防护、SQL 注入防护

## 🏗️ 技术架构

### 后端技术栈

- **框架**: Spring Boot 3.x
- **AI 引擎**: LangChain4j
- **数据库**: MySQL + MyBatis-Flex
- **缓存**: Redis + Caffeine
- **监控**: Prometheus + Grafana
- **文档**: Knife4j (Swagger)

### 前端技术栈

- **框架**: Vue 3 + TypeScript
- **UI 库**: Ant Design Vue
- **构建工具**: Vite
- **状态管理**: Pinia
- **HTTP 客户端**: Axios

### 核心架构模式

- **工厂模式**: AI 服务实例管理
- **门面模式**: 统一的 AI 服务入口
- **适配器模式**: 多种 AI 模型适配
- **观察者模式**: 实时流式响应

详细架构分析请参考 [设计模式分析文档.md](设计模式分析文档.md)

## 📚 API 文档

启动应用后，访问以下地址查看完整 API 文档：

- **Swagger UI**: http://localhost:8234/api/doc.html
- **OpenAPI 规范**: http://localhost:8234/api/v3/api-docs

### 核心 API

```bash
# 用户相关
POST /api/user/register     # 用户注册
POST /api/user/login        # 用户登录
GET  /api/user/get/login    # 获取当前用户

# 应用相关
POST /api/app/add           # 创建应用
GET  /api/app/list/page     # 获取应用列表
POST /api/app/update        # 更新应用

# AI生成
POST /api/chat/gen/code     # AI代码生成 (SSE流式响应)
```

## 🛠️ 部署指南

### Docker 部署

```bash
# 构建镜像
docker build -t mambo-ai-platform .

# 运行容器
docker run -d -p 8234:8234 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=your_db_host \
  -e REDIS_HOST=your_redis_host \
  mambo-ai-platform
```

### 传统部署

```bash
# 构建项目
mvn clean package -DskipTests

# 运行JAR文件
java -jar target/mambo-ai-platform-*.jar --spring.profiles.active=prod
```

详细部署指南请参考 [部署文档](docs/deployment.md)

## 📖 开发指南

### 项目结构

```
mambo-ai-platform/
├── src/main/java/
│   ├── controller/         # 控制器层
│   ├── service/           # 业务逻辑层
│   ├── model/             # 数据模型
│   ├── config/            # 配置类
│   └── utils/             # 工具类
├── src/main/resources/
│   ├── mapper/            # MyBatis映射文件
│   ├── prompt/            # AI提示词模板
│   └── sql/               # 数据库脚本
├── mambo-ai-platform-frontend/  # Vue前端项目
└── docs/                  # 项目文档
```

### 开发规范

- **代码规范**: 遵循阿里巴巴 Java 开发手册
- **Git 规范**: 使用 Conventional Commits
- **API 设计**: RESTful API 设计原则
- **测试覆盖**: 单元测试 + 集成测试

## 🤝 贡献指南

我们欢迎所有形式的贡献！

### 贡献方式

1. **Fork** 本项目
2. **创建** 功能分支 (`git checkout -b feature/amazing-feature`)
3. **提交** 更改 (`git commit -m 'Add some amazing feature'`)
4. **推送** 到分支 (`git push origin feature/amazing-feature`)
5. **开启** Pull Request

### 开发者

- [@Marisalice114](https://github.com/Marisalice114) - 项目维护者

## 📄 许可证

本项目基于 [MIT License](LICENSE) 开源协议。

## 🙏 致谢

- [LangChain4j](https://github.com/langchain4j/langchain4j) - Java AI 框架
- [Spring Boot](https://spring.io/projects/spring-boot) - Java Web 框架
- [Vue.js](https://vuejs.org/) - 前端框架
- [Ant Design Vue](https://antdv.com/) - UI 组件库

## 📞 联系我们

- **Issues**: [GitHub Issues](https://github.com/your-username/mambo-ai-platform/issues)
- **Discussions**: [GitHub Discussions](https://github.com/your-username/mambo-ai-platform/discussions)

---

<div align="center">
  <sub>Built with ❤️ by the Mambo AI Platform team</sub>
</div>
