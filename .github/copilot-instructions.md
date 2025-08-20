# Mambo AI Code Generation Platform - AI Agent Instructions

This is a **learning-oriented enterprise-grade AI code generation platform** built with **Spring Boot 3 + LangChain4j + Vue 3**. The project combines **practical learning experience** with **production-ready architecture patterns**, emphasizing **backend-driven development** where the core AI logic, workflows, and business features are implemented in Java, while the frontend serves as an adaptable interface that should be modified to match backend capabilities and requirements.

## 🏗️ Architecture Overview

### Backend Architecture (Spring Boot 3 + Java 21) - **PRIMARY FOCUS**

- **AI Service Layer**: LangChain4j-powered code generation with multiple strategies (HTML, Multi-file, Vue projects)
- **Workflow Engine**: LangGraph4j for complex AI workflows with node-based processing
- **Data Layer**: MyBatis-Flex for database operations, Redis for caching/sessions
- **External Services**: Aliyun OSS for file storage, Selenium for screenshots
- **Enterprise Features**: Rate limiting (Redisson), session management, caching strategies, error handling
- **Custom Business Logic**: Extended backend features requiring frontend adaptation

### Frontend Architecture (Vue 3 + TypeScript) - **ADAPTATION LAYER**

- **Base Framework**: Vue 3 + Ant Design Vue foundation requiring extensive customization
- **Real-time Communication**: EventSource/SSE for streaming AI responses
- **API Integration**: Auto-generated clients from OpenAPI (`npm run openapi2ts`)
- **State Management**: Pinia stores for user session and application state
- **Development Workflow**: Vite dev server with hot reload, TypeScript compilation
- **Production Readiness**: Component optimization, responsive design, error boundaries

## 🔧 Key Development Patterns

### LangChain4j Service Factory Pattern

The platform uses a sophisticated factory pattern for AI services with Caffeine caching:

```java
// Multiple AI services per app/generation type with isolated chat memory
AiCodeGeneratorService service = aiCodeGeneratorServiceFactory
    .getAiCodeGeneratorService(appId, codeGenType);
```

- Each app gets isolated `MessageWindowChatMemory` with Redis persistence
- Different generation types (HTML/MULTI_FILE/VUE_PROJECT) use different models
- Factory manages caching with keys: `{appId}_{codeGenType}` (30min TTL)
- **Prototype scope** for AI model beans to solve concurrency issues

### LangGraph4j Workflow Architecture

Complex AI workflows use node-based processing with state serialization:

```java
// WorkflowContext flows through MessagesState between nodes
WorkflowContext context = WorkflowContext.getContext(state);
// Nodes: ImageCollector -> PromptEnhancer -> Router -> CodeGenerator -> ProjectBuilder
```

- State management via `WorkflowContext` serialized in `MessagesState`
- Conditional edges for quality checks and retries (`edge_async()`)
- Subgraph support for parallel processing (image collection with 4 concurrent subgraphs)
- SSE streaming support for real-time workflow progress updates

### Configuration Management

- **Multi-profile setup**: `application.yml` + `application-local.yml` (ModelScope API for development)
- **LangChain4j config**: Separate beans for different model types (reasoning, streaming, routing)
- **Redis dual-purpose**: Session storage + LangChain4j chat memory + Redisson rate limiting
- **Prototype beans**: Critical for AI models to avoid concurrency issues
- **MyBatis-Flex**: Code generation via `MyBatisCodeGenerator.main()` in test package

## 🛠️ Essential Development Commands

### Backend Development

```bash
# Start with local profile (uses ModelScope API)
mvn spring-boot:run -Dspring.profiles.active=local

# Generate MyBatis-Flex code (run in IDE)
# Execute: MyBatisCodeGenerator.main() in src/test/java

# Test workflows (run in IDE)
# Execute: WorkflowApp.main() for LangGraph4j workflow testing
```

### Frontend Development

```bash
cd mambo-ai-platform-frontend
npm run dev              # Development server (Vite)
npm run openapi2ts       # Generate API clients from OpenAPI spec
npm run build           # Production build
npm run type-check      # TypeScript compilation check
```

### Critical Integration Points

- **SSE Endpoint**: `/app/chat/gen/code` returns `Flux<ServerSentEvent<String>>`
- **EventSource Frontend**: Real-time streaming in `AppChatPage.vue`
- **API Generation**: OpenAPI schema at `localhost:8234/api/v3/api-docs`

## 📁 Critical File Locations

### Backend Core Files

- `AiCodeGeneratorServiceFactory.java` - Multi-instance AI service management with Caffeine caching
- `CodeGenWorkflow.java` - Main LangGraph4j workflow implementation with SSE support
- `WorkflowContext.java` - State management for workflows (serializable in MessagesState)
- `resources/prompt/` - System prompts for different generation types
- `AiCodeGeneratorFacade.java` - Unified entry point for code generation
- `RedisChatMemoryStoreConfig.java` - Redis-backed LangChain4j chat memory configuration

### Frontend Core Files

- `src/pages/app/AppChatPage.vue` - Real-time chat interface with EventSource SSE handling
- `src/api/` - Auto-generated API clients (regenerate with `npm run openapi2ts`)
- `src/stores/loginUser.ts` - User session management with Pinia
- `openapi2ts.config.ts` - API client generation configuration
- `src/request.ts` - Axios configuration with base URL and credentials

### Configuration Files

- `application-local.yml` - Development config with ModelScope API keys
- `application-prod.yml` - Production config with live API endpoints
- `RedisChatMemoryStoreConfig.java` - Chat memory persistence setup
- `RoutingAiModelConfig.java` - Model routing for different AI tasks
- `StreamingChatModelConfig.java` - Streaming model configuration with prototype scope

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
