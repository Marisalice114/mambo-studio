<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { addApp, listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import { getDeployUrl } from '@/config/env'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 用户提示词
const userPrompt = ref('')
const creating = ref(false)

// 我的应用数据
const myApps = ref<API.AppVO[]>([])
const myAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 精选应用数据
const featuredApps = ref<API.AppVO[]>([])
const featuredAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 设置提示词
const setPrompt = (prompt: string) => {
  userPrompt.value = prompt
}

// 优化提示词功能已移除

// 创建应用
const createApp = async () => {
  if (!userPrompt.value.trim()) {
    message.warning('请输入应用描述')
    return
  }

  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }

  creating.value = true
  try {
    const res = await addApp({
      initPrompt: userPrompt.value.trim(),
    })

    if (res.data.code === 0 && res.data.data) {
      message.success('应用创建成功')
      // 跳转到对话页面，确保ID是字符串类型
      const appId = String(res.data.data)
      await router.push(`/app/chat/${appId}`)
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch (error) {
    console.error('创建应用失败：', error)
    message.error('创建失败，请重试')
  } finally {
    creating.value = false
  }
}

// 跳转到工作流页面
const goToWorkflow = () => {
  router.push('/workflow')
}

// 加载我的应用
const loadMyApps = async () => {
  if (!loginUserStore.loginUser.id) {
    return
  }

  try {
    const res = await listMyAppVoByPage({
      pageNum: myAppsPage.current,
      pageSize: myAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载我的应用失败：', error)
  }
}

// 加载精选应用
const loadFeaturedApps = async () => {
  try {
    const res = await listGoodAppVoByPage({
      pageNum: featuredAppsPage.current,
      pageSize: featuredAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载精选应用失败：', error)
  }
}

// 查看对话
const viewChat = (appId: string | number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}?view=1`)
  }
}

// 查看作品
const viewWork = (app: API.AppVO) => {
  if (app.deployKey) {
    const url = getDeployUrl(app.deployKey)
    window.open(url, '_blank')
  }
}

// 格式化时间函数已移除，不再需要显示创建时间

// 页面加载时获取数据
onMounted(() => {
  loadMyApps()
  loadFeaturedApps()

  // 鼠标跟随光效
  const handleMouseMove = (e: MouseEvent) => {
    const { clientX, clientY } = e
    const { innerWidth, innerHeight } = window
    const x = (clientX / innerWidth) * 100
    const y = (clientY / innerHeight) * 100

    document.documentElement.style.setProperty('--mouse-x', `${x}%`)
    document.documentElement.style.setProperty('--mouse-y', `${y}%`)
  }

  document.addEventListener('mousemove', handleMouseMove)

  // 清理事件监听器
  return () => {
    document.removeEventListener('mousemove', handleMouseMove)
  }
})
</script>

<template>
  <div id="homePage">
    <div class="container">
      <!-- 网站标题和描述 -->
      <div class="hero-section">
        <h1 class="hero-title">AI 应用生成平台</h1>
        <p class="hero-description">一句话轻松创建网站应用</p>
      </div>

      <!-- 用户提示词输入框 -->
      <div class="input-section">
        <a-textarea
          v-model:value="userPrompt"
          placeholder="帮我创建个人博客网站"
          :rows="4"
          :maxlength="1000"
          class="prompt-input"
        />
        <div class="input-actions">
          <a-button type="primary" size="large" @click="createApp" :loading="creating">
            <template #icon>
              <span>↑</span>
            </template>
          </a-button>
          <a-button type="default" size="large" @click="goToWorkflow" class="workflow-btn">
            <template #icon>
              <span>🚀</span>
            </template>
            AI工作流
          </a-button>
        </div>
      </div>

      <!-- 快捷按钮 -->
      <div class="quick-actions">
        <a-button
          type="default"
          @click="
            setPrompt(
              '创建一个现代化的个人博客网站，包含文章列表、详情页、分类标签、搜索功能、评论系统和个人简介页面。采用简洁的设计风格，支持响应式布局，文章支持Markdown格式，首页展示最新文章和热门推荐。',
            )
          "
          >个人博客网站</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '设计一个专业的企业官网，包含公司介绍、产品服务展示、新闻资讯、联系我们等页面。采用商务风格的设计，包含轮播图、产品展示卡片、团队介绍、客户案例展示，支持多语言切换和在线客服功能。',
            )
          "
          >企业官网</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '构建一个功能完整的在线商城，包含商品展示、购物车、用户注册登录、订单管理、支付结算等功能。设计现代化的商品卡片布局，支持商品搜索筛选、用户评价、优惠券系统和会员积分功能。',
            )
          "
          >在线商城</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '制作一个精美的作品展示网站，适合设计师、摄影师、艺术家等创作者。包含作品画廊、项目详情页、个人简历、联系方式等模块。采用瀑布流或网格布局展示作品，支持图片放大预览和作品分类筛选。',
            )
          "
          >作品展示网站</a-button
        >
      </div>

      <!-- 我的作品 -->
      <div class="section">
        <h2 class="section-title">我的作品</h2>
        <div class="app-grid">
          <AppCard
            v-for="app in myApps"
            :key="app.id"
            :app="app"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="myAppsPage.current"
            v-model:page-size="myAppsPage.pageSize"
            :total="myAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个应用`"
            @change="loadMyApps"
          />
        </div>
      </div>

      <!-- 精选案例 -->
      <div class="section">
        <h2 class="section-title">精选案例</h2>
        <div class="featured-grid">
          <AppCard
            v-for="app in featuredApps"
            :key="app.id"
            :app="app"
            :featured="true"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="featuredAppsPage.current"
            v-model:page-size="featuredAppsPage.pageSize"
            :total="featuredAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个案例`"
            @change="loadFeaturedApps"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#homePage {
  width: 100%;
  margin: 0;
  padding: 0;
  min-height: 100vh;
  background:
    linear-gradient(135deg, #FFFFFF 0%, #FFF5F8 30%, #FFE4E1 80%, #FFB6C1 100%),
    radial-gradient(circle at 20% 80%, rgba(255, 105, 180, 0.12) 0%, transparent 60%),
    radial-gradient(circle at 80% 20%, rgba(255, 182, 193, 0.15) 0%, transparent 60%),
    radial-gradient(circle at 40% 40%, rgba(255, 20, 147, 0.08) 0%, transparent 50%);
  position: relative;
  overflow: hidden;
}

/* 优雅的网格背景 */
#homePage::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    linear-gradient(rgba(255, 105, 180, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 105, 180, 0.08) 1px, transparent 1px),
    linear-gradient(rgba(255, 182, 193, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 182, 193, 0.06) 1px, transparent 1px);
  background-size:
    120px 120px,
    120px 120px,
    24px 24px,
    24px 24px;
  pointer-events: none;
  animation: gridFloat 25s ease-in-out infinite;
}

/* 鼠标跟随粉色光效 */
#homePage::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(
      700px circle at var(--mouse-x, 50%) var(--mouse-y, 50%),
      rgba(255, 105, 180, 0.15) 0%,
      rgba(255, 182, 193, 0.12) 40%,
      transparent 80%
    ),
    linear-gradient(45deg, transparent 30%, rgba(255, 105, 180, 0.05) 50%, transparent 70%),
    linear-gradient(-45deg, transparent 30%, rgba(255, 182, 193, 0.04) 50%, transparent 70%);
  pointer-events: none;
  animation: lightPulse 12s ease-in-out infinite alternate;
}

@keyframes gridFloat {
  0%, 100% {
    transform: translate(0, 0);
  }
  33% {
    transform: translate(8px, 4px);
  }
  66% {
    transform: translate(-4px, 8px);
  }
}

@keyframes lightPulse {
  0% {
    opacity: 0.4;
  }
  100% {
    opacity: 0.8;
  }
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  position: relative;
  z-index: 2;
  width: 100%;
  box-sizing: border-box;
}

/* 英雄区域 */
.hero-section {
  text-align: center;
  padding: 100px 0 80px;
  margin-bottom: 40px;
  color: var(--text-primary, #333);
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(ellipse 900px 450px at center, rgba(255, 105, 180, 0.15) 0%, transparent 70%),
    linear-gradient(45deg, transparent 30%, rgba(255, 182, 193, 0.08) 50%, transparent 70%),
    linear-gradient(-45deg, transparent 30%, rgba(255, 20, 147, 0.06) 50%, transparent 70%);
  animation: heroGlow 15s ease-in-out infinite alternate;
}

@keyframes heroGlow {
  0% {
    opacity: 0.8;
    transform: scale(1);
  }
  100% {
    opacity: 1;
    transform: scale(1.05);
  }
}

.hero-title {
  font-size: 64px;
  font-weight: 700;
  margin: 0 0 24px;
  line-height: 1.1;
  background: linear-gradient(135deg, #FF69B4 0%, #FF1493 30%, #FFB6C1 70%, #FF69B4 100%);
  background-size: 300% 300%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -2px;
  position: relative;
  z-index: 2;
  animation: titleShimmer 4s ease-in-out infinite;
  text-shadow: 0 2px 4px rgba(255, 105, 180, 0.2);
}

@keyframes titleShimmer {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.hero-description {
  font-size: 22px;
  margin: 0;
  color: var(--text-secondary, #666);
  position: relative;
  z-index: 2;
  font-weight: 400;
}

/* 输入区域 */
.input-section {
  position: relative;
  margin: 0 auto 40px;
  max-width: 800px;
}

.prompt-input {
  border-radius: 20px;
  border: 2px solid rgba(255, 105, 180, 0.2);
  font-size: 16px;
  padding: 24px 80px 24px 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(25px);
  box-shadow: 0 12px 48px rgba(255, 105, 180, 0.15);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  color: var(--text-primary, #333);
}

.prompt-input:focus {
  background: rgba(255, 255, 255, 1);
  border-color: rgba(255, 105, 180, 0.4);
  box-shadow: 0 16px 60px rgba(255, 105, 180, 0.25);
  transform: translateY(-4px) scale(1.01);
}

.prompt-input::placeholder {
  color: rgba(102, 102, 102, 0.6);
}

.input-actions {
  position: absolute;
  bottom: 16px;
  right: 16px;
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 主按钮样式 */
:deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #FF69B4, #FF1493) !important;
  border: none !important;
  border-radius: 50% !important;
  width: 48px !important;
  height: 48px !important;
  box-shadow: 0 6px 20px rgba(255, 105, 180, 0.4) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

:deep(.ant-btn-primary:hover) {
  transform: translateY(-3px) scale(1.05) !important;
  box-shadow: 0 8px 25px rgba(255, 105, 180, 0.5) !important;
}

.workflow-btn {
  background: linear-gradient(135deg, #FFB6C1, #FF69B4) !important;
  border: none !important;
  color: white !important;
  border-radius: 25px !important;
  padding: 8px 20px !important;
  height: auto !important;
  font-weight: 500 !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  box-shadow: 0 4px 15px rgba(255, 182, 193, 0.3) !important;
}

.workflow-btn:hover {
  background: linear-gradient(135deg, #FF69B4, #FF1493) !important;
  transform: translateY(-2px) scale(1.02) !important;
  box-shadow: 0 6px 20px rgba(255, 105, 180, 0.4) !important;
}

/* 快捷按钮 */
.quick-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 80px;
  flex-wrap: wrap;
}

.quick-actions .ant-btn {
  border-radius: 30px;
  padding: 12px 24px;
  height: auto;
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid rgba(255, 182, 193, 0.3);
  color: var(--text-primary, #333);
  backdrop-filter: blur(20px);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  font-weight: 500;
  box-shadow: 0 4px 15px rgba(255, 105, 180, 0.1);
}

.quick-actions .ant-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 105, 180, 0.15), transparent);
  transition: left 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.quick-actions .ant-btn:hover::before {
  left: 100%;
}

.quick-actions .ant-btn:hover {
  background: rgba(255, 255, 255, 1);
  border-color: rgba(255, 105, 180, 0.5);
  color: #FF1493;
  transform: translateY(-4px) scale(1.03);
  box-shadow: 0 12px 30px rgba(255, 105, 180, 0.25);
}

/* 区域标题 */
.section {
  margin-bottom: 80px;
}

.section-title {
  font-size: 36px;
  font-weight: 600;
  margin-bottom: 40px;
  color: var(--text-primary, #333);
  text-align: center;
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, #FF69B4, #FFB6C1);
  border-radius: 2px;
}

/* 我的作品网格 */
.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 32px;
  margin-bottom: 40px;
}

/* 精选案例网格 */
.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 32px;
  margin-bottom: 40px;
}

/* 分页样式增强 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

:deep(.ant-pagination-item) {
  border-radius: 8px !important;
  border-color: rgba(255, 182, 193, 0.3) !important;
  transition: all 0.3s ease !important;
}

:deep(.ant-pagination-item:hover) {
  border-color: #FF69B4 !important;
  transform: translateY(-1px) !important;
}

:deep(.ant-pagination-item-active) {
  background: linear-gradient(135deg, #FF69B4, #FFB6C1) !important;
  border-color: #FF69B4 !important;
}

:deep(.ant-pagination-item-active a) {
  color: white !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-title {
    font-size: 40px;
  }

  .hero-description {
    font-size: 18px;
  }

  .app-grid,
  .featured-grid {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .quick-actions {
    justify-content: center;
  }

  .input-actions {
    flex-direction: column;
    gap: 8px;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 32px;
  }

  .quick-actions .ant-btn {
    padding: 10px 18px;
    font-size: 14px;
  }
}
</style>
