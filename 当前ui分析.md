📋 当前前端技术栈分析
🛠️ 核心技术栈
框架: Vue 3 + TypeScript + Composition API
构建工具: Vite 7.0.0
UI 组件库: Ant Design Vue 4.2.6
状态管理: Pinia 3.0.3
路由管理: Vue Router 4.5.1
HTTP 客户端: Axios 1.11.0
Markdown 渲染: markdown-it 14.1.0 + highlight.js 11.11.1
🎨 已有视觉特效

1. 动画和过渡效果
   CSS 动画: 使用@keyframes 实现 VIP 用户头像的旋转光环效果
   过渡效果: 广泛使用 transition: all 0.3s ease 实现悬停状态变化
   变换效果: transform: translateY(-2px)实现卡片悬停上浮效果
2. 渐变和阴影
   线性渐变: 多处使用 linear-gradient，如 VIP 标识、背景等
   盒子阴影: box-shadow 实现深度感和悬停效果
   文字渐变: VIP 用户名使用渐变文字效果
3. VIP 用户特殊样式
   动态光环: VIP 头像有旋转的金色光环动画
   渐变文字: VIP 用户名使用金色渐变效果
   特殊标识: 金色 VIP 徽章和到期提醒图标
4. 背景特效
   网格背景: 首页使用点状网格背景
   鼠标跟随: 实现了鼠标移动的光效跟随效果
   多层背景: 结合渐变和网格的复合背景
   🏗️ 项目结构特点
   组件架构
   布局组件: BasicLayout.vue 作为主布局容器
   全局组件: GlobalHeader.vue、GlobalFooter.vue
   页面组件: 按功能模块划分（admin、app、user 等）
   通用组件: AppCard.vue、UserInfo.vue 等
   样式管理
   Scoped 样式: 每个组件使用<style scoped>避免样式冲突
   内联样式: 部分动态样式使用 Vue 的动态绑定
   CSS 变量: 使用 CSS 自定义属性实现主题色管理
   🎯 当前品牌元素
   品牌名: "鱼皮应用生成"
   Logo: logo.png (鱼皮相关图标)
   配色: 蓝色主题 (#1890ff) + 金色 VIP 元素
   字体: 系统默认字体栈
   🔧 开发工具和配置
   代码质量: ESLint + Prettier
   API 集成: OpenAPI 自动生成 TypeScript 客户端
   开发服务器: Vite 开发服务器 + 代理配置
   构建优化: TypeScript 类型检查 + 生产构建
