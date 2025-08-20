# 🚀 MamboStudio - 环境配置指南

本文档将指导您如何完整配置 MamboStudio AI 代码生成平台的运行环境。

## 📋 环境要求

- **Java**: JDK 21+
- **Node.js**: 20.0+
- **MySQL**: 8.0+
- **Redis**: 6.0+

## 🗄️ 数据库配置

### 1. MySQL 数据库设置

#### 步骤 1: 创建数据库

```sql
-- 连接到 MySQL 服务器后执行以下命令：
CREATE DATABASE mambo_code_platform 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- 或者使用更完整的配置：
CREATE DATABASE IF NOT EXISTS mambo_code_platform
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci
    DEFAULT ENCRYPTION='N';
```

#### 步骤 2: 执行初始化脚本

数据库创建完成后，执行项目中的初始化脚本：

```bash
# 方法1: 使用 MySQL 命令行
mysql -u root -p mambo_code_platform < src/main/resources/sql/init.sql

# 方法2: 使用 MySQL Workbench 或其他客户端工具
# 直接执行 src/main/resources/sql/init.sql 文件内容
```

**数据库结构说明**:
- `user` 表：用户信息管理，支持 VIP 会员功能
- `app` 表：AI 生成的应用管理，支持部署和版本控制

#### 步骤 3: 验证数据库

```sql
-- 切换到目标数据库
USE mambo_code_platform;

-- 查看创建的表
SHOW TABLES;

-- 验证表结构
DESCRIBE user;
DESCRIBE app;
```

### 2. Redis 配置

Redis 用于会话管理、缓存和 LangChain4j 聊天记忆存储：

```bash
# 安装 Redis (Ubuntu/Debian)
sudo apt update
sudo apt install redis-server

# 安装 Redis (CentOS/RHEL)
sudo yum install redis

# 安装 Redis (macOS)
brew install redis

# 启动 Redis 服务
redis-server

# 验证 Redis 运行
redis-cli ping
# 应该返回: PONG
```

## 🤖 AI 服务配置

### 1. ModelScope API (推荐免费方案)

MamboStudio 使用 ModelScope 作为主要的 AI 服务提供商：

#### 步骤 1: 注册 ModelScope

1. 访问 [ModelScope](https://www.modelscope.cn/)
2. 注册账号并完成实名认证
3. 进入 **控制台** → **API-KEY 管理**
4. 创建新的 API Key

#### 步骤 2: 模型说明

- **对话生成模型**: `Qwen/Qwen3-235B-A22B-Instruct-2507`
- **代码生成模型**: `Qwen/Qwen3-Coder-480B-A35B-Instruct`
- **推理模型**: `Qwen/Qwen3-Coder-480B-A35B-Instruct`

### 2. 阿里云 DashScope (可选)

用于路由判断和辅助功能：

1. 访问 [阿里云 DashScope](https://dashscope.aliyun.com/)
2. 开通服务并获取 API Key
3. 主要用于 `qwen-turbo` 模型

## ☁️ 云服务配置 (可选)

### 阿里云 OSS 文件存储

用于存储生成的应用封面和静态文件：

1. 访问 [阿里云 OSS 控制台](https://oss.console.aliyun.com/)
2. 创建 Bucket (建议选择离用户最近的区域)
3. 获取 AccessKey ID 和 AccessKey Secret
4. 配置读写权限：**公共读，私有写**

### Pexels API (可选)

用于获取高质量图片素材：

1. 访问 [Pexels API](https://www.pexels.com/api/)
2. 注册并获取免费 API Key

## 🔧 项目配置

### 1. 克隆项目

```bash
git clone https://github.com/Marisalice114/mambo-studio.git
cd mambo-studio
```

### 2. 后端配置

#### 步骤 1: 复制配置文件

```bash
# 复制配置示例文件
cp application-example.yml src/main/resources/application-local.yml
```

#### 步骤 2: 编辑配置文件

编辑 `src/main/resources/application-local.yml`，填入您的实际配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mambo_code_platform?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
    username: your_mysql_username
    password: your_mysql_password
  
  data:
    redis:
      host: localhost
      port: 6379
      password: # 如果设置了密码
      database: 1

langchain4j:
  open-ai:
    chat-model:
      api-key: your_modelscope_api_key
    streaming-chat-model:
      api-key: your_modelscope_api_key
    reasoning-stream-model:
      api-key: your_modelscope_api_key

# 可选配置
dashscope:
  api-key: your_dashscope_api_key

aliyun:
  oss:
    access-key-id: your_oss_access_key_id
    access-key-secret: your_oss_access_key_secret

pexels:
  api-key: your_pexels_api_key
```

### 3. 前端配置

```bash
cd mambo-ai-platform-frontend
npm install
```

## 🚀 启动应用

### 1. 启动后端服务

```bash
# 方法1: 使用 Maven
mvn spring-boot:run -Dspring.profiles.active=local

# 方法2: 使用 IDE
# 直接运行 MainApplication.java，并设置 VM options: -Dspring.profiles.active=local
```

### 2. 启动前端服务

```bash
cd mambo-ai-platform-frontend
npm run dev
```

## 🌐 访问应用

启动成功后，您可以通过以下地址访问应用：

- **前端应用**: http://localhost:5173
- **后端 API**: http://localhost:8234/api
- **API 文档**: http://localhost:8234/api/doc.html

## 🔍 验证配置

### 1. 数据库连接验证

查看后端启动日志，确认以下信息：

```
Successfully acquired change log lock
Running Changeset: ...
```

### 2. Redis 连接验证

```
Lettuce version: ...
Redis connection established
```

### 3. AI 服务验证

访问前端应用，尝试创建一个简单的 HTML 项目，观察是否能正常生成代码。

## 🛠️ 故障排除

### 常见问题

1. **数据库连接失败**
   - 检查 MySQL 服务是否启动
   - 验证数据库名称、用户名、密码是否正确
   - 确认 MySQL 端口 (默认 3306) 是否开放

2. **Redis 连接失败**
   - 检查 Redis 服务是否启动: `redis-cli ping`
   - 验证 Redis 配置中的主机、端口、密码

3. **AI 服务调用失败**
   - 检查 ModelScope API Key 是否有效
   - 确认网络连接是否正常
   - 查看后端日志中的详细错误信息

4. **前端无法访问后端**
   - 确认后端服务已启动 (端口 8234)
   - 检查防火墙设置
   - 验证前端代理配置

### 日志位置

- **后端日志**: 控制台输出 + `logs/` 目录
- **前端日志**: 浏览器开发者工具 Console
- **API 调用日志**: 后端启用了 `log-requests: true` 和 `log-responses: true`

## 📚 更多资源

- **项目文档**: [README.md](README.md)
- **API 文档**: http://localhost:8234/api/doc.html (启动后访问)
- **技术支持**: [GitHub Issues](https://github.com/Marisalice114/mambo-studio/issues)

---

配置完成后，您就可以开始使用 MamboStudio 进行 AI 代码生成了！🎉
