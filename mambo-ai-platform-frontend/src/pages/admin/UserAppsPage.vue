<template>
  <div id="userAppsPage">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <a-button type="link" @click="goBack" class="back-btn">
          <template #icon>
            <ArrowLeftOutlined />
          </template>
          返回用户管理
        </a-button>
        <h1 class="page-title">
          用户应用管理
          <span v-if="currentUser" class="user-info">
            - {{ currentUser.userName || currentUser.userAccount }}
          </span>
        </h1>
        <div class="stats-row">
          <div class="stat-card">
            <div class="stat-value">{{ total }}</div>
            <div class="stat-label">总应用数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ deployedCount }}</div>
            <div class="stat-label">已部署应用</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ featuredCount }}</div>
            <div class="stat-label">精选应用</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索表单 -->
    <div class="search-section">
      <a-form layout="inline" :model="searchParams" @finish="doSearch" class="search-form">
        <a-form-item label="应用名称">
          <a-input 
            v-model:value="searchParams.appName" 
            placeholder="输入应用名称" 
            class="search-input"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="生成类型">
          <a-select 
            v-model:value="searchParams.codeGenType" 
            placeholder="选择生成类型" 
            class="search-select"
            allow-clear
          >
            <a-select-option
              v-for="option in CODE_GEN_TYPE_OPTIONS"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" class="search-btn">
            <template #icon>
              <SearchOutlined />
            </template>
            搜索
          </a-button>
          <a-button @click="doReset" class="reset-btn">重置</a-button>
        </a-form-item>
      </a-form>
    </div>

    <!-- 表格 -->
    <div class="table-section">
      <a-table
        :columns="columns"
        :data-source="data"
        :pagination="pagination"
        @change="doTableChange"
        :loading="loading"
        class="apps-table"
        :scroll="{ x: 1200 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'appName'">
            <a-button type="link" @click="viewChat(record)" class="app-name-link">
              <a-tooltip :title="record.appName || '未命名应用'">
                <div class="truncated-text">{{ record.appName || '未命名应用' }}</div>
              </a-tooltip>
            </a-button>
          </template>
          <template v-else-if="column.dataIndex === 'cover'">
            <a-image v-if="record.cover" :src="record.cover" :width="80" :height="60" />
            <div v-else class="no-cover">无封面</div>
          </template>
          <template v-else-if="column.dataIndex === 'initPrompt'">
            <a-tooltip :title="record.initPrompt">
              <div class="prompt-text">{{ record.initPrompt }}</div>
            </a-tooltip>
          </template>
          <template v-else-if="column.dataIndex === 'codeGenType'">
            {{ formatCodeGenType(record.codeGenType) }}
          </template>
          <template v-else-if="column.dataIndex === 'priority'">
            <div class="priority-display">
              <a-tag v-if="record.priority === 99" color="gold">精选 (99)</a-tag>
              <span v-else class="priority-number">{{ record.priority || 0 }}</span>
            </div>
          </template>
          <template v-else-if="column.dataIndex === 'deployedTime'">
            <span v-if="record.deployedTime" class="time-text">
              {{ formatTime(record.deployedTime) }}
            </span>
            <span v-else class="text-gray">未部署</span>
          </template>
          <template v-else-if="column.dataIndex === 'createTime'">
            <span class="time-text">
              {{ formatTime(record.createTime) }}
            </span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="primary" size="small" @click="editApp(record)"> 编辑 </a-button>
              <a-button
                type="default"
                size="small"
                @click="toggleFeatured(record)"
                :class="{ 'featured-btn': record.priority === 99 }"
              >
                {{ record.priority === 99 ? '取消精选' : '精选' }}
              </a-button>
              <a-popconfirm title="确定要删除这个应用吗？" @confirm="deleteApp(record.id)">
                <a-button danger size="small">删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined, SearchOutlined } from '@ant-design/icons-vue'
import { deleteAppByAdmin, updateAppByAdmin } from '@/api/appController'
import { CODE_GEN_TYPE_OPTIONS, formatCodeGenType } from '@/utils/codeGenTypes'
import { formatTime } from '@/utils/time'

const router = useRouter()
const route = useRoute()

// 当前用户信息
const currentUser = ref<any>(null)
const userId = route.params.userId as string

// 调试：检查数字转换
console.log('原始userId:', userId)
console.log('parseInt转换后:', parseInt(userId))
console.log('parseInt结果是否等于原值:', parseInt(userId).toString() === userId)

// 表格列定义
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '应用名称',
    dataIndex: 'appName',
    width: 150,
  },
  {
    title: '封面',
    dataIndex: 'cover',
    width: 100,
  },
  {
    title: '初始提示词',
    dataIndex: 'initPrompt',
    width: 200,
  },
  {
    title: '生成类型',
    dataIndex: 'codeGenType',
    width: 100,
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    width: 80,
  },
  {
    title: '部署时间',
    dataIndex: 'deployedTime',
    width: 160,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 160,
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right',
  },
]

// 数据
const data = ref<any[]>([])
const total = ref(0)
const loading = ref(false)

// 搜索参数
const searchParams = reactive({
  pageNum: 1,
  pageSize: 10,
  userId: userId, // 保持字符串格式，但这会导致类型错误
  appName: undefined as string | undefined,
  codeGenType: undefined as string | undefined,
})

// 统计数据
const deployedCount = computed(() => {
  return data.value.filter(app => app.deployedTime).length
})

const featuredCount = computed(() => {
  return data.value.filter(app => app.priority === 99).length
})

// 分页配置
const pagination = computed(() => ({
  current: searchParams.pageNum,
  pageSize: searchParams.pageSize,
  total: total.value,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 个应用`,
}))

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    console.log('正在获取用户信息，userId:', userId)
    
    // 直接使用request发送请求，绕过API客户端的类型检查
    // 这样可以确保ID作为字符串传递，避免精度丢失
    const url = `/api/user/get/vo?id=${userId}`
    console.log('请求URL:', url)
    
    const response = await fetch(url, {
      method: 'GET',
      credentials: 'include', // 包含cookies
      headers: {
        'Content-Type': 'application/json',
      },
    })
    
    console.log('Response status:', response.status)
    console.log('Response headers:', response.headers)
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const res = await response.json()
    console.log('API响应:', res)
    
    if (res.code === 0 && res.data) {
      currentUser.value = res.data
      console.log('用户信息获取成功:', res.data)
    } else {
      console.error('API返回错误:', res.message || '未知错误')
      message.error('获取用户信息失败：' + (res.message || '未知错误'))
    }
  } catch (error) {
    console.error('获取用户信息失败：', error)
    message.error('获取用户信息失败')
  }
}

// 获取应用数据
const fetchData = async () => {
  loading.value = true
  try {
    console.log('正在获取应用列表，userId:', userId)
    
    // 构建请求体，保持userId为字符串以避免精度丢失
    const requestBody = {
      pageNum: searchParams.pageNum,
      pageSize: searchParams.pageSize,
      userId: userId, // 直接使用字符串，避免parseInt精度丢失
      appName: searchParams.appName || undefined,
      codeGenType: searchParams.codeGenType || undefined,
    }
    
    console.log('应用列表请求体:', requestBody)
    
    const response = await fetch('/api/app/admin/list/page/vo', {
      method: 'POST',
      credentials: 'include', // 包含cookies
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(requestBody),
    })
    
    console.log('应用列表Response status:', response.status)
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const res = await response.json()
    console.log('应用列表API响应:', res)
    
    if (res.code === 0 && res.data) {
      data.value = res.data.records || []
      total.value = res.data.totalRow || 0
      console.log('应用列表获取成功，记录数:', data.value.length)
    } else {
      console.error('应用列表API返回错误:', res.message || '未知错误')
      message.error('获取应用列表失败：' + (res.message || '未知错误'))
    }
  } catch (error) {
    console.error('获取应用列表失败：', error)
    message.error('获取应用列表失败')
  } finally {
    loading.value = false
  }
}

// 表格变化处理
const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索
const doSearch = () => {
  searchParams.pageNum = 1
  fetchData()
}

// 重置搜索
const doReset = () => {
  searchParams.appName = undefined
  searchParams.codeGenType = undefined
  searchParams.pageNum = 1
  fetchData()
}

// 查看对话
const viewChat = (record: any) => {
  router.push(`/app/chat/${record.id}`)
}

// 编辑应用
const editApp = (record: any) => {
  router.push(`/app/edit/${record.id}`)
}

// 切换精选状态
const toggleFeatured = async (record: any) => {
  if (!record.id) return

  const newPriority = record.priority === 99 ? 0 : 99

  try {
    const res = await updateAppByAdmin({
      id: record.id,
      priority: newPriority,
    })

    if (res.data.code === 0) {
      message.success(newPriority === 99 ? '已设为精选' : '已取消精选')
      fetchData()
    } else {
      message.error('操作失败：' + res.data.message)
    }
  } catch (error) {
    console.error('操作失败：', error)
    message.error('操作失败')
  }
}

// 删除应用
const deleteApp = async (id: number) => {
  if (!id) return

  try {
    const res = await deleteAppByAdmin({ id })
    if (res.data.code === 0) {
      message.success('删除成功')
      fetchData()
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error) {
    console.error('删除失败：', error)
    message.error('删除失败')
  }
}

// 返回用户管理页面
const goBack = () => {
  router.push('/admin/userManage')
}

// 页面加载时获取数据
onMounted(() => {
  fetchUserInfo()
  fetchData()
})
</script>

<style scoped>
#userAppsPage {
  padding: 0;
  background: var(--background-main, #FFFFFF);
  min-height: 100vh;
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #FFFFFF 0%, #FFF5F8 100%);
  padding: 32px 24px;
  border-bottom: 1px solid var(--border-color, #FFE4E1);
  box-shadow: 0 2px 8px rgba(255, 105, 180, 0.08);
}

.header-content {
  max-width: calc(100vw - 48px);
  margin: 0 auto;
}

.back-btn {
  color: #FF69B4 !important;
  font-weight: 500;
  margin-bottom: 16px;
  padding: 0 !important;
}

.back-btn:hover {
  color: #FF1493 !important;
}

.page-title {
  font-size: 32px;
  font-weight: 600;
  margin: 0 0 24px 0;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.user-info {
  font-size: 18px;
  color: #666;
}

.stats-row {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.stat-card {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  padding: 20px;
  border-radius: 16px;
  border: 1px solid rgba(255, 182, 193, 0.2);
  box-shadow: 0 4px 16px rgba(255, 105, 180, 0.1);
  text-align: center;
  transition: all 0.3s ease;
  min-width: 120px;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 32px rgba(255, 105, 180, 0.2);
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary, #666);
  font-weight: 500;
}

/* 搜索区域 */
.search-section {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(10px);
  padding: 24px;
  border-bottom: 1px solid var(--border-color, #FFE4E1);
}

.search-form {
  max-width: calc(100vw - 48px);
  margin: 0 auto;
}

/* 表格区域 */
.table-section {
  padding: 16px;
  max-width: calc(100vw - 32px);
  margin: 0 auto;
}

:deep(.apps-table) {
  background: rgba(255, 255, 255, 0.95) !important;
  border-radius: 16px !important;
  overflow: hidden !important;
  box-shadow: 0 8px 32px rgba(255, 105, 180, 0.1) !important;
  border: 1px solid rgba(255, 182, 193, 0.2) !important;
}

:deep(.ant-table-thead > tr > th) {
  background: linear-gradient(135deg, #FFF5F8, #FFE4E1) !important;
  border-bottom: 2px solid rgba(255, 105, 180, 0.1) !important;
  color: var(--text-primary, #333) !important;
  font-weight: 600 !important;
}

:deep(.ant-table-tbody > tr:hover) {
  background: rgba(255, 245, 248, 0.5) !important;
}

/* 应用名称链接样式 */
.app-name-link {
  font-weight: 600;
  color: #FF69B4 !important;
  padding: 0 !important;
  text-decoration: none;
  border: none;
  background: none;
  box-shadow: none !important;
}

.app-name-link:hover {
  color: #FF1493 !important;
  text-decoration: underline;
  background: none !important;
  border: none !important;
  box-shadow: none !important;
}

/* 文本溢出处理 */
.truncated-text {
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.prompt-text {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-primary, #333);
}

.no-cover {
  width: 80px;
  height: 60px;
  background: linear-gradient(135deg, #FFF5F8, #FFE4E1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary, #666);
  font-size: 12px;
  border-radius: 8px;
  border: 1px solid rgba(255, 182, 193, 0.2);
}

.priority-display {
  display: flex;
  align-items: center;
  justify-content: center;
}

.priority-number {
  font-weight: 600;
  color: #666;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(255, 182, 193, 0.1);
}

.time-text {
  color: var(--text-primary, #333);
  font-size: 12px;
}

.text-gray {
  color: var(--text-secondary, #666);
}

.featured-btn {
  background: linear-gradient(135deg, #FFD700, #FFA500) !important;
  border: none !important;
  color: white !important;
  border-radius: 8px !important;
  transition: all 0.3s ease !important;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.3) !important;
}

.featured-btn:hover {
  background: linear-gradient(135deg, #FFA500, #FF8C00) !important;
  transform: translateY(-1px) !important;
  box-shadow: 0 4px 12px rgba(255, 140, 0, 0.4) !important;
}

/* 按钮样式增强 */
:deep(.ant-btn) {
  border-radius: 8px !important;
  transition: all 0.3s ease !important;
}

:deep(.ant-btn:hover) {
  transform: translateY(-1px) !important;
}

/* Tag样式增强 */
:deep(.ant-tag) {
  border-radius: 12px !important;
  font-weight: 500 !important;
  padding: 4px 12px !important;
}

:deep(.ant-tag-gold) {
  background: linear-gradient(135deg, #FFD700, #FFA500) !important;
  border-color: #FFD700 !important;
  color: white !important;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.3) !important;
}

/* 输入框和按钮样式 */
:deep(.ant-input) {
  border-radius: 12px !important;
  border-color: rgba(255, 182, 193, 0.3) !important;
}

:deep(.ant-input:focus) {
  border-color: #FF69B4 !important;
  box-shadow: 0 0 0 2px rgba(255, 105, 180, 0.1) !important;
}

:deep(.ant-select .ant-select-selector) {
  border-radius: 12px !important;
  border-color: rgba(255, 182, 193, 0.3) !important;
}

:deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #FF69B4, #FF1493) !important;
  border: none !important;
  border-radius: 12px !important;
  box-shadow: 0 4px 15px rgba(255, 105, 180, 0.3) !important;
}

:deep(.ant-btn-primary:hover) {
  transform: translateY(-2px) !important;
  box-shadow: 0 6px 20px rgba(255, 105, 180, 0.4) !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    padding: 20px 16px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .stats-row {
    justify-content: center;
  }
  
  .search-section,
  .table-section {
    padding: 16px;
  }
}
</style>
