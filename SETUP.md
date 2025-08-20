# 🚀 Mambo AI Platform - 环境配置指南

本文档将指导您如何配置 Mambo AI Platform 的运行环境。

## 📋 配置清单

### 1. 数据库配置

#### MySQL

- **版本要求**: MySQL 8.0+
- **数据库名**: `mambo_code_platform`
- **字符集**: UTF-8
- **时区**: UTC

```sql

```

#### Redis

- **版本要求**: Redis 6.0+
- **数据库**: 1（用于 Session 和缓存）
- **密码**: 可选，建议生产环境配置

### 2. AI 服务配置

#### ModelScope API

> **免费额度**: ModelScope 提供一定的免费 API 调用额度

1. 访问 [ModelScope](https://www.modelscope.cn/)
2. 注册账号并实名认证
3. 进入控制台 → API-KEY 管理
4. 创建新的 API Key
5. 将 Key 配置到 `langchain4j.open-ai.chat-model.api-key`

**推荐模型**:

- **对话模型**: `Qwen/Qwen3-235B-A22B-Instruct-2507`
- **代码模型**: `Qwen/Qwen3-Coder-480B-A35B-Instruct`

#### 阿里云 DashScope（可选）

> **用途**: 路由模型和图片生成

1. 访问 [阿里云 DashScope](https://dashscope.aliyun.com/)
2. 开通服务并获取 API Key
3. 配置到 `dashscope.api-key`

### 3. 文件存储配置

#### 阿里云 OSS

> **用途**: 存储生成的应用封面和文件

1. 访问 [阿里云 OSS 控制台](https://oss.console.aliyun.com/)
2. 创建 Bucket
3. 获取 AccessKey ID 和 AccessKey Secret
4. 配置存储区域和 Bucket 名称

**建议配置**:

- **区域**: 选择距离用户最近的区域
- **读写权限**: 公共读，私有写
- **跨域配置**: 允许前端访问

### 4. 第三方服务配置

#### Pexels API（可选）

> **用途**: 获取高质量图片素材

1. 访问 [Pexels API](https://www.pexels.com/api/)
2. 注册并获取免费 API Key
3. 配置到 `pexels.api-key`

## 🛠️ 快速开始

### 1. 克隆项目

```bash
git clone <your-repo-url>
cd mambo-ai-platform
```

### 2. 复制配置文件

```bash
# 复制示例配置文件
cp application-example.yml src/main/resources/application-local.yml
```

### 3. 编辑配置文件

编辑 `src/main/resources/application-local.yml`，填入您的实际配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mambo_code_platform?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
    username: your_db_username
    password: your_db_password
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password # 如果Redis有密码

langchain4j:
  open-ai:
    chat-model:
      api-key: ms-xxxxxxxxxx # 您的ModelScope API Key
```

### 4. 创建数据库

运行 SQL 脚本创建表结构：

```bash
# 表结构文件位于
src/main/resources/sql/
```

### 5. 启动应用

```bash
# 使用Maven启动
mvn spring-boot:run -Dspring.profiles.active=local

# 或者使用IDE直接运行MainApplication
```

### 6. 启动前端

```bash
cd mambo-ai-platform-frontend
npm install
npm run dev
```

## 🔧 环境变量配置（推荐）

为了更好的安全性，建议使用环境变量配置敏感信息：

```bash
# Linux/macOS
export OSS_ACCESS_KEY_ID="your_oss_access_key_id"
export OSS_ACCESS_KEY_SECRET="your_oss_access_key_secret"
export MODELSCOPE_API_KEY="your_modelscope_api_key"

# Windows
set OSS_ACCESS_KEY_ID=your_oss_access_key_id
set OSS_ACCESS_KEY_SECRET=your_oss_access_key_secret
set MODELSCOPE_API_KEY=your_modelscope_api_key
```

然后在配置文件中引用：

```yaml
langchain4j:
  open-ai:
    chat-model:
      api-key: ${MODELSCOPE_API_KEY:default_value}
```

## 🚀 部署配置

### 生产环境配置文件

创建 `application-prod.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://your_prod_host:3306/mambo_code_platform
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}

langchain4j:
  open-ai:
    chat-model:
      api-key: ${MODELSCOPE_API_KEY}
      log-requests: false # 生产环境关闭日志
      log-responses: false
```

### Docker 部署（可选）

```dockerfile
FROM openjdk:21-jdk-slim
COPY target/mambo-ai-platform-*.jar app.jar
EXPOSE 8234
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.profiles.active=prod"]
```

## 📝 注意事项

1. **安全性**

   - 不要将 API 密钥提交到 Git 仓库
   - 生产环境使用环境变量配置敏感信息
   - 定期轮换 API 密钥

2. **性能优化**

   - Redis 建议配置持久化
   - MySQL 建议调整连接池大小
   - OSS 建议配置 CDN 加速

3. **监控**
   - 启用 Prometheus 监控
   - 配置日志收集
   - 设置告警规则

## 🆘 常见问题

### Q: ModelScope API 配额不足怎么办？

A: 可以申请增加配额，或者配置多个 API Key 轮换使用。

### Q: 数据库连接失败？

A: 检查数据库服务是否启动，用户名密码是否正确，防火墙是否开放端口。

### Q: Redis 连接失败？

A: 检查 Redis 服务状态，确认配置的 host、port、password 是否正确。

### Q: 前端无法访问后端 API？

A: 检查跨域配置，确认后端端口 8234 是否正常启动。

## 📧 技术支持

如有问题，欢迎提交 Issue 或联系维护者。

---

**Mambo AI Platform** - 让 AI 代码生成更简单 🚀
