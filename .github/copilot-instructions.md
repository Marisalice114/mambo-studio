                   # Mambo AI Code Generation Platform - AI Agent Instructions

**MamboStudio** is an enterprise-grade AI code generation platform built with **Spring Boot 3 + LangChain4j + Vue 3**. This is a **backend-driven architecture** where AI logic, workflows, and business features are implemented in Java, with the frontend serving as an adaptive interface layer.

## 🏗️ Architecture Overview

### Backend (Spring Boot 3 + Java 21) - **PRIMARY FOCUS**

**Core Technologies:**

- **AI Framework**: LangChain4j with multi-model support (ModelScope API for local dev, production models for deployment)
- **Workflow Engine**: LangGraph4j for stateful AI workflows with node-based processing
- **Data Persistence**: MyBatis-Flex (code generation via `MyBatisCodeGenerator.main()`)
- **Caching/Sessions**: Redis (dual-purpose: sessions + LangChain4j chat memory + Redisson rate limiting)
- **File Storage**: Aliyun OSS for generated code and assets, local filesystem at `tmp/code_output/`
- **Monitoring**: Prometheus + Grafana for AI model metrics and business KPIs

**Key Design Patterns:**

- **Factory Pattern**: `AiCodeGeneratorServiceFactory` with Caffeine caching (30min TTL, per-app isolation)
- **Facade Pattern**: `AiCodeGeneratorFacade` as unified entry point for code generation
- **Tool System**: `ToolManager` auto-registers Spring beans extending `BaseTool` for LangChain4j function calling
- **Prototype Scope**: AI model beans use prototype scope to prevent concurrency issues

### Frontend (Vue 3 + TypeScript) - **ADAPTATION LAYER**

**Core Technologies:**

- **Framework**: Vue 3 + Vite + TypeScript with Ant Design Vue (requires heavy customization)
- **API Client**: Auto-generated from OpenAPI spec via `npm run openapi2ts`
- **Real-time**: EventSource/SSE for streaming AI responses in `AppChatPage.vue`
- **State Management**: Pinia stores for user sessions and application state
- **Visual Editing**: Custom `VisualEditor` class for iframe-based interactive element selection

**Visual Editing System (Unique Feature):**

- **Cross-frame Communication**: PostMessage API bridges parent window and iframe preview
- **Dynamic Script Injection**: Runtime injection of editing scripts into generated HTML
- **Element Detection**: CSS hover/click handlers with intelligent selector generation
- **AI Integration**: Selected elements feed context to LangChain4j tools for precise modifications

## 🔧 Critical Development Patterns

### 1. LangChain4j Service Factory Pattern

Each app gets isolated AI services with independent chat memory:

```java
// Factory creates cached instances per {appId}_{codeGenType}
AiCodeGeneratorService service = aiCodeGeneratorServiceFactory
    .getAiCodeGeneratorService(appId, CodeGenTypeEnum.VUE_PROJECT);
```

**Key Implementation Details:**

- **Caffeine Cache**: 30min write expiration, 10min access expiration, max 1000 instances
- **Redis Chat Memory**: `MessageWindowChatMemory` backed by `RedisChatMemoryStore` (100 message window)
- **Chat History Loading**: `chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 50)` preloads context
- **Prototype Scope**: AI model beans **must** use `@Scope("prototype")` to avoid threading conflicts
- **Model Selection**: Different generation types use different models (reasoning/streaming/routing)

### 2. LangGraph4j Workflow Architecture

Stateful workflows with serializable context passing between nodes:

```java
// WorkflowContext serializes into MessagesState for node communication
WorkflowContext context = WorkflowContext.getContext(state);
context.setEnhancedPrompt("improved prompt...");
return WorkflowContext.saveContext(context);
```

**Workflow Capabilities:**

- **Nodes**: ImageCollector → PromptEnhancer → Router → CodeGenerator → QualityCheck → ProjectBuilder
- **Conditional Edges**: `edge_async()` for quality checks and retry logic with `routeAfterQualityCheck()`
- **Subgraphs**: Parallel image collection (4 concurrent subgraphs for content/illustrations/diagrams/logos)
- **SSE Streaming**: `Flux<String>` support for real-time workflow progress updates
- **Graph Visualization**: `workflow.getGraph(GraphRepresentation.Type.MERMAID)` for debugging

### 3. LangChain4j Tool System

Auto-registered tools for AI function calling via Spring's component scanning:

```java
@Component
public class FileWriteTool extends BaseTool {
    @Tool("写入文件到指定路径")
    public String writeFile(
        @P("文件的相对路径") String relativeFilePath,
        @P("要写入文件的内容") String content,
        @ToolMemoryId Long appId
    ) {
        // Resolves relative paths to vue_project_{appId}/ directory
        // Returns relative path to prevent exposing absolute paths to AI
    }
}
```

**Available Tools:**

- **File Operations**: `FileWriteTool`, `FileReadTool`, `FileModifyTool`, `FileDeleteTool`, `FileDirReadTool`
- **Image Generation**: `LogoGeneratorTool` (DashScope API), `UndrawIllustrationTool` (free SVG library)
- **Diagram Generation**: `MermaidDiagramTool` (converts Mermaid syntax to images via CLI)
- **Tool Registration**: `ToolManager` uses `@Resource private BaseTool[] tools` to auto-inject all tool beans

### 4. Visual Editor Cross-Frame Communication

Frontend-only feature enabling interactive element selection in generated code:

```typescript
// VisualEditor injects script into iframe for click-to-select functionality
visualEditor.init(iframe)
visualEditor.toggleEditMode() // Injects hover/click handlers

// PostMessage API bridges iframe and parent window
window.addEventListener('message', (event) => {
  if (event.data.type === 'ELEMENT_SELECTED') {
    // Pass element info (selector, tagName, textContent) to AI
  }
})
```

**Implementation Details:**

- **Dynamic Injection**: `generateEditScript()` creates self-contained script with CSS and event handlers
- **Element Selection**: Generates unique CSS selectors (ID > classes > nth-child)
- **Visual Feedback**: `.edit-hover` (blue dashed outline) and `.edit-selected` (green solid outline)
- **Context Enrichment**: Selected element info appends to user message for precise AI modifications

### 5. Monitoring Context (ThreadLocal Issue & Solution)

**Critical Issue**: `MonitorContextHolder` uses ThreadLocal, causing NullPointerException in async operations.

**Solution**:

```java
// Set context before AI service calls
MonitorContextHolder.setContext(MonitorContext.builder()
    .userId(userId).appId(appId).build());

// For async operations, use attributes instead of ThreadLocal
Map<String, Object> attributes = new HashMap<>();
attributes.put(MONITOR_CONTEXT_KEY, MonitorContext.builder()...build());

// In listeners, retrieve from attributes
MonitorContext context = (MonitorContext) attributes.get(MONITOR_CONTEXT_KEY);
```

## 🛠️ Essential Development Commands

### Backend Development

```bash
# Start with local profile (uses ModelScope API)
mvn spring-boot:run -Dspring.profiles.active=local

# Generate MyBatis-Flex code (run in IDE)
# Execute: MyBatisCodeGenerator.main() in src/test/java

# Test workflows (run in IDE)
# Execute: WorkflowApp.main() for LangGraph4j workflow testing

# Build project
mvn clean package -DskipTests
```

### Frontend Development

```bash
cd mambo-ai-platform-frontend
npm install                # Install dependencies
npm run dev                # Development server (Vite) at http://localhost:5173
npm run openapi2ts         # Generate API clients from OpenAPI spec
npm run build              # Production build
npm run type-check         # TypeScript compilation check
```

### Database & Redis Setup

```bash
# MySQL (required schema in sql/ directory)
mysql -u root -p < src/main/resources/sql/create_table.sql

# Redis (default port 6379, no password for local dev)
redis-server
```

### Critical Integration Points

- **SSE Endpoint**: `/app/chat/gen/code` returns `Flux<ServerSentEvent<String>>`
- **EventSource Frontend**: Real-time streaming in `AppChatPage.vue`
- **API Documentation**: Swagger UI at `http://localhost:8234/api/doc.html`
- **OpenAPI Spec**: JSON schema at `http://localhost:8234/api/v3/api-docs`
- **Prometheus Metrics**: `http://localhost:8234/api/actuator/prometheus`

## 📁 Critical File Locations

### Backend Core Files

**AI Service Layer:**

- `AiCodeGeneratorServiceFactory.java` - Multi-instance AI service management with Caffeine caching
- `AiCodeGeneratorFacade.java` - Unified entry point for code generation
- `ai/tools/ToolManager.java` - Auto-registers all `BaseTool` beans via `@Resource private BaseTool[] tools`
- `ai/tools/FileWriteTool.java`, `FileReadTool.java`, `FileModifyTool.java`, `FileDeleteTool.java`, `FileDirReadTool.java`

**Workflow Layer:**

- `langgraph4j/CodeGenWorkflow.java` - Main LangGraph4j workflow with SSE support
- `langgraph4j/state/WorkflowContext.java` - State management (serializable in MessagesState)
- `langgraph4j/node/` - Individual workflow nodes (ImageCollectorNode, PromptEnhancerNode, RouterNode, etc.)
- `langgraph4j/tools/` - Image generation tools (LogoGeneratorTool, UndrawIllustrationTool, MermaidDiagramTool)

**Configuration:**

- `application.yml` - Base config (server, datasource, Redis, session)
- `application-local.yml` - Development config with ModelScope API keys
- `application-prod.yml` - Production config with live API endpoints
- `RedisChatMemoryStoreConfig.java` - Redis-backed LangChain4j chat memory
- `RoutingAiModelConfig.java` - Model routing for different AI tasks
- `StreamingChatModelConfig.java` - Streaming model with prototype scope

**Resource Files:**

- `resources/prompt/` - System prompts for different generation types
  - `codegen-html-system-prompt.txt`
  - `codegen-multi-file-system-prompt.txt`
  - `codegen-vue-system-prompt.txt`
  - `codegen-routing-system-prompt.txt`

### Frontend Core Files

**Pages & Components:**

- `src/pages/app/AppChatPage.vue` - Real-time chat interface with EventSource SSE handling
- `src/utils/visualEditor.ts` - VisualEditor class for iframe-based element selection
- `src/pages/admin/` - Admin dashboard pages (UserAppsPage, UsersPage, ChatsPage)

**API & State:**

- `src/api/` - Auto-generated API clients (regenerate with `npm run openapi2ts`)
- `src/stores/loginUser.ts` - User session management with Pinia
- `src/request.ts` - Axios configuration with base URL and credentials

**Configuration:**

- `openapi2ts.config.ts` - API client generation configuration
- `vite.config.ts` - Vite build and dev server configuration
- `tsconfig.json` - TypeScript compiler options

### Key Directories

- `tmp/code_output/` - Generated code storage (HTML, multi-file, Vue projects)
- `tmp/logos/` - Generated logo images
- `src/test/java/` - Test classes including MyBatisCodeGenerator
- `target/classes/` - Compiled classes and resources

## 🎯 Development Guidelines

### Working with AI Services

1. **Always specify generation type** when calling `AiCodeGeneratorFacade`
2. **Use factory pattern** for AI service creation (don't inject directly)
3. **Handle streaming responses** with proper error handling and timeouts
4. **Test memory isolation** between different apps/conversations

### LangGraph4j Workflow Development

1. **Define state first** in `WorkflowContext` before implementing nodes
2. **Use `WorkflowContext.getContext()` and `saveContext()`** for state flow
3. **Implement conditional edges** for quality checks and retries
4. **Test workflow graphs** with `GraphRepresentation.Type.MERMAID`

### Database Operations

1. **Use MyBatis-Flex** with generated mappers and entities
2. **Follow naming convention**: Entity -> Mapper -> Service -> Controller
3. **Leverage code generation** for consistent CRUD operations

### Frontend API Integration

1. **Regenerate API clients** after backend changes: `npm run openapi2ts`
2. **Adapt UI components** to support new backend features and data structures
3. **Handle SSE streams** for real-time AI responses
4. **Customize layouts** to match backend capabilities rather than generic templates

### Enterprise Development Practices

1. **Error Handling**: Implement comprehensive exception handling with proper HTTP status codes
2. **Logging**: Use structured logging with appropriate levels (INFO, WARN, ERROR)
3. **Testing**: Write unit tests for services and integration tests for APIs
4. **Documentation**: Maintain OpenAPI documentation and code comments
5. **Performance**: Monitor Redis cache hit rates and optimize database queries
6. **Security**: Implement rate limiting and input validation

### Frontend Customization Strategy

1. **Component Redesign**: Replace Ant Design defaults with custom styled components
2. **Layout Overhaul**: Create unique page layouts that reflect backend functionality
3. **Responsive Adaptation**: Ensure new UI elements work across devices
4. **Brand Identity**: Develop distinctive visual design separate from template origins

## 🔍 Common Debugging Approaches

### AI Service Issues

- Check `langchain4j.open-ai.*.log-requests/responses: true` in config
- Verify model availability and API keys in `application-local.yml`
- Monitor Redis for chat memory persistence issues
- Examine Caffeine cache hit rates in service factories

### Workflow Debugging

- Enable workflow graph output: `workflow.getGraph(GraphRepresentation.Type.MERMAID)`
- Add logging in node implementations to track state changes
- Use `WorkflowApp.main()` for isolated workflow testing
- Monitor SSE event streams for real-time workflow progress

### Frontend-Backend Integration

- Check browser Network tab for API call failures and SSE connections
- Verify CORS settings and context-path (`/api`) in URLs
- Monitor EventSource connection status in `AppChatPage.vue`
- Test API client regeneration after backend changes: `npm run openapi2ts`

### Configuration Issues

- Verify prototype scope for AI model beans (prevents concurrency issues)
- Check Redis connectivity for both session storage and chat memory
- Validate ModelScope API keys and endpoints in profile-specific configs
- Monitor MyBatis-Flex connection pool settings in `application.yml`

### AI Model Monitoring Issues

- **MonitorContext NullPointerException**: Common issue in `AiModelMonitorListener`
  - Root cause: `MonitorContextHolder.getContext()` returns null
  - Solution: Set context before AI service calls: `MonitorContextHolder.setContext(MonitorContext.builder().userId("...").appId("...").build())`
  - Threading issue: Use attributes to pass context across threads instead of ThreadLocal
  - Fix: Replace `MonitorContextHolder.getContext()` with `(MonitorContext) attributes.get(MONITOR_CONTEXT_KEY)`

## 🚀 Deployment Considerations

- **Redis is critical**: Used for sessions, chat memory, and caching
- **File storage**: Generated code saved to local filesystem (`tmp/code_output/`)
- **Model timeouts**: LangChain4j configured with 10-minute timeouts for long generations
- **Database**: MySQL with UTF-8 encoding and proper timezone settings
- **Rate limiting**: Redisson-based distributed rate limiting for API protection
- **Session management**: Redis-backed session storage for scalability
- **Error handling**: Comprehensive exception handling and logging
- **Monitoring**: Performance metrics and health checks for production readiness

## 🎨 Frontend Customization Guidelines

### When Backend Logic Changes

1. **Analyze backend changes**: Review new APIs, data structures, and business logic
2. **Update API integration**: Run `npm run openapi2ts` to regenerate clients
3. **Adapt components**: Modify existing Vue components to handle new data/features
4. **Test integration**: Ensure frontend properly displays backend functionality

### Creating Original UI Design

1. **Replace template elements**: Move away from standard Ant Design appearance
2. **Custom styling**: Create unique CSS/SCSS for distinctive visual identity
3. **Layout restructuring**: Reorganize page layouts to better serve backend features
4. **Component library**: Build custom reusable components matching project needs
5. **Responsive design**: Ensure custom elements work across all device sizes

### Development Workflow for Frontend Adaptation

```bash
# 1. After backend changes, regenerate API clients
npm run openapi2ts

# 2. Review changes in src/api/ directory
# 3. Update affected Vue components and stores
# 4. Test integration with backend
npm run dev

# 5. Build and verify custom styling
npm run build
```
