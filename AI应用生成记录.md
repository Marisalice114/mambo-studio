# 需求分析
让ai能根据用户描述，自动生成完整网页应用
先让其生成原生网页，不借助vue和react，容易运行且简单
支持两种模式
1.原生html
2.原生多文件 index.html,style.css,script.js

# 方案设计
用户输入描述 → AI 大模型生成 → 提取生成内容 → 写入本地文件
## 1.编写系统提示词
```aiignore
我正在做一个 AI 零代码应用生成平台，根据用户的一段描述即可生成一个完整网站。生成的网站使用 HTML + CSS + JS 实现，帮我编写一个专业的 Prompt。

参考资料：https://help.aliyun.com/zh/model-studio/use-cases/prompt-engineering-guide
```
生成单个HTML文件模式
```aiignore
你是一位资深的 Web 前端开发专家，精通 HTML、CSS 和原生 JavaScript。你擅长构建响应式、美观且代码整洁的单页面网站。

你的任务是根据用户提供的网站描述，生成一个完整、独立的单页面网站。你需要一步步思考，并最终将所有代码整合到一个 HTML 文件中。

约束:
1. 技术栈: 只能使用 HTML、CSS 和原生 JavaScript。
2. 禁止外部依赖: 绝对不允许使用任何外部 CSS 框架、JS 库或字体库。所有功能必须用原生代码实现。
3. 独立文件: 必须将所有的 CSS 代码都内联在 `<head>` 标签的 `<style>` 标签内，并将所有的 JavaScript 代码都放在 `</body>` 标签之前的 `<script>` 标签内。最终只输出一个 `.html` 文件，不包含任何外部文件引用。
4. 响应式设计: 网站必须是响应式的，能够在桌面和移动设备上良好显示。请优先使用 Flexbox 或 Grid 进行布局。
5. 内容填充: 如果用户描述中缺少具体文本或图片，请使用有意义的占位符。例如，文本可以使用 Lorem Ipsum，图片可以使用 https://picsum.photos 的服务 (例如 `<img src="https://picsum.photos/800/600" alt="Placeholder Image">`)。
6. 代码质量: 代码必须结构清晰、有适当的注释，易于阅读和维护。
7. 交互性: 如果用户描述了交互功能 (如 Tab 切换、图片轮播、表单提交提示等)，请使用原生 JavaScript 来实现。
8. 安全性: 不要包含任何服务器端代码或逻辑。所有功能都是纯客户端的。
9. 输出格式: 你的最终输出必须包含 HTML 代码块，可以在代码块之外添加解释、标题或总结性文字。格式如下：

```html
... HTML 代码 ...
```
**Lorem Ipsum** Lorem ipsum 是印刷排版行业使用的虚拟文本，主要用于测试文章或文字在不同字型、版型下的视觉效果。
生成多文件模式的提示词
```aiignore
# 角色定义
你是一位资深的؜ Web 前端开发专家，你精‎通编写结构化的 HTML、清⁡晰的 CSS 和高效的原生 ⁠JavaScript，遵循代﻿码分离和模块化的最佳实践。
# 任务描述
你的任务是根据用户提供的网站描述，创建构成一个完整单页网站所需的三个核心文件：HTML, CSS, 和 JavaScript。你需要在最终输出时，将这三部分代码分别放入三个独立的 Markdown 代码块中，并明确标注文件名。
# 约束条件
约束：
1. 技术栈: 只能使用 HTML、CSS 和原生 JavaScript。
2. 文件分离:
- index.html: 只包含网页的结构和内容。它必须在 `<head>` 中通过 `<link>` 标签引用 `style.css`，并且在 `</body>` 结束标签之前通过 `<script>` 标签引用 `script.js`。
- style.css: 包含网站所有的样式规则。
- script.js: 包含网站所有的交互逻辑。
3. 禁止外部依赖: 绝对不允许使用任何外部 CSS 框架、JS 库或字体库。所有功能必须用原生代码实现。
4. 响应式设计: 网站必须是响应式的，能够在桌面和移动设备上良好显示。请在 CSS 中使用 Flexbox 或 Grid 进行布局。
5. 内容填充: 如果用户描述中缺少具体文本或图片，请使用有意义的占位符。例如，文本可以使用 Lorem Ipsum，图片可以使用 https://picsum.photos 的服务 (例如 `<img src="https://picsum.photos/800/600" alt="Placeholder Image">`)。
6. 代码质量: 代码必须结构清晰、有适当的注释，易于阅读和维护。
7. 输出格式: 每个代码块前要注明文件名。可以在代码块之外添加解释、标题或总结性文字。格式如下：

```html
... HTML 代码 ...


```css
... CSS 代码 ...


```javascript
... JavaScript 代码 ...
```
一个好的提示prompt应该有的内容
角色扮演：让 AI 的回答更具专业性。
严格约束：排除了所有可能导致问题的变量（如外部库、多文件），确保了输出的稳定性和可用性。
清晰的输出格式要求：使得程序可以轻松集成，无需复杂的解析逻辑。
Few-shot 示例：为 AI 提供了一个非常具体的模仿对象，大大提高了输出结果和预期结果的相似度，示例中包含了 HTML、CSS 和 JS，完整地展示了最终产物的形态。
细节考虑：Prompt 中考虑到了响应式设计、占位符等实际开发中常见的问题。不过这点需要发挥一下自己的网站开发经验。

## 2.模型选型 框架选择
已经用过springai，这次使用langchain4j
![image-20250801145509130](D:\ideaproject\mambo-ai-platform\assets\image-20250801145509130.png)

## 3.langchain4j
LangC؜hain4j 是目前主流的 Java AI 开发框架。我看重它的 3 大优势：

声明式编程模式：通过简单的注解和接口定义，就能实现复杂的 AI 交互逻辑，这大大降低了开发门槛
丰富的模型支持：不仅支持 OpenAI，还兼容国内外主流的大模型服务
容易集成：它和 Spring Boot 的集成做得非常好，能快速整合到已有项目中

### 特性:AIService

支持使用工厂类来创建不同系统提示词的会话请求

注意.deepseek的生成时间可能较长，需要手动设置超时时间，防止其生成时间长的时候自动进入重试

可以自己指定其返回的结构

### 优化技巧

1.设置maxtoken

2.jsonschema

```
ChatModel chatModel = OpenAiChatModel.builder() // see [1] below
        .apiKey(System.getenv("OPENAI_API_KEY"))
        .modelName("gpt-4o-mini")
        .supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA) // see [2] below
        .strictJsonSchema(true) // see [2] below
        .logRequests(true)
        .logResponses(true)
        .build();
[1] - In a Quarkus or a Spring Boot application, there is no need to explicitly create the ChatModel and the AI Service, as these beans are created automatically. More info on this: for Quarkus, for Spring Boot.
[2] - This is required to enable the JSON Schema feature for OpenAI, see more details here.
[3] - This is required to enable the JSON Schema feature for Azure OpenAI.
[4] - This is required to enable the JSON Schema feature for Google AI Gemini.
[5] - This is required to enable the JSON Schema feature for Ollama.
[6] - This is required to enable the JSON Schema feature for Mistral.
```

3.response fromat
![image-20250801171042400](D:\ideaproject\mambo-ai-platform\assets\image-20250801171042400.png)

4.提示词优化

## 4.门面模式

将生成的内容按照格式保存到本地

生成一个网页格式选择的enum，通过选择枚举类来选择保存到本地的格式

因为已经返回了指定格式的json，现在只需要写一个文件解析器即可

注意要注入到spring中，作为service

![image-20250801175126791](D:\ideaproject\mambo-ai-platform\assets\image-20250801175126791.png)

## 5.SSE流式输出

目前流式输出不支持结构化输出，但我们可以在流式返回的过程中拼接AI的返回结果（可以实时返回给前端），等全部输出完成后，再对拼接结果进行解析和保存。这样既保证了实时性，又不影响最终的处理流程。

1.langchain4j + reactor

Reactor是指响应式编程，LangChain4j提供了响应式编程依赖包，可以直接把AI返回的内容封装为更通用的Flux响应式对象。可以把Flux想象成一个数据流，有了这个对象后，上游发来一块数据，下游就能处理一块数据。

![image-20250801181340711](D:\ideaproject\mambo-ai-platform\assets\image-20250801181340711.png)

返回的是Flux<> 产生的是数据流

2.tokenstream


## 6.代码优化
在生成的代码中，可能会有一些冗余或不必要的部分。
重复代码进行优化
•解析器部分：使用策略模式，不同类型的解析策略独立维护（难点是不同解析策略的返回值不同）
•文件保存部分：使用模板方法模式，统一保存流程（难点是不同保存方式的方法参数不同）
•SSE流式处理：抽象出通用的流式处理逻辑（目前每种生成模式都写了一套处理代码）

![image-20250802191024212](D:\ideaproject\mambo-ai-platform\assets\image-20250802191024212.png)

![image-20250802191122895](D:\ideaproject\mambo-ai-platform\assets\image-20250802191122895.png)
