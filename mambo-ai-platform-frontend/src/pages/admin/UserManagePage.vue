<template>
  <div id="userManagePage">
    <!-- 页面标题和统计信息 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">用户管理</h1>
        <div class="stats-row">
          <div class="stat-card">
            <div class="stat-value">{{ total }}</div>
            <div class="stat-label">总用户数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ adminCount }}</div>
            <div class="stat-label">管理员</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ vipCount }}</div>
            <div class="stat-label">VIP用户</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ normalCount }}</div>
            <div class="stat-label">普通用户</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索表单 -->
    <div class="search-section">
      <a-form layout="inline" :model="searchParams" @finish="doSearch" class="search-form">
        <a-form-item label="账号">
          <a-input 
            v-model:value="searchParams.userAccount" 
            placeholder="输入账号" 
            class="search-input"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="用户名">
          <a-input 
            v-model:value="searchParams.userName" 
            placeholder="输入用户名" 
            class="search-input"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="用户角色">
          <a-select 
            v-model:value="searchParams.userRole" 
            placeholder="选择角色" 
            class="search-select"
            allow-clear
          >
            <a-select-option value="admin">管理员</a-select-option>
            <a-select-option value="user">普通用户</a-select-option>
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
        class="user-table"
        :scroll="{ x: 1200 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'userAvatar'">
            <a-avatar :src="record.userAvatar" :size="48">
              {{ record.userName?.charAt(0) || 'U' }}
            </a-avatar>
          </template>
          <template v-else-if="column.dataIndex === 'userName'">
            <a-button type="link" @click="goToUserApps(record)" class="user-name-link">
              <a-tooltip :title="record.userName || '未设置用户名'">
                <div class="truncated-text">{{ record.userName || '未设置用户名' }}</div>
              </a-tooltip>
            </a-button>
          </template>
          <template v-else-if="column.dataIndex === 'userRole'">
            <a-tag v-if="record.userRole === 'admin'" color="gold" class="role-tag">
              <CrownOutlined />
              管理员
            </a-tag>
            <a-tag v-else-if="isVipUser(record)" color="purple" class="role-tag vip-tag">
              <CrownOutlined />
              VIP用户
            </a-tag>
            <a-tag v-else color="blue" class="role-tag">
              <UserOutlined />
              普通用户
            </a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'vipExpireTime'">
            <span v-if="record.vipExpireTime" class="time-text vip-time">
              {{ dayjs(record.vipExpireTime).format('YYYY-MM-DD HH:mm:ss') }}
            </span>
            <span v-else class="no-vip-text">未开通VIP</span>
          </template>
          <template v-else-if="column.dataIndex === 'createTime'">
            <span class="time-text">
              {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
            </span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button danger @click="doDelete(record.id)" class="delete-btn" size="small">
              <template #icon>
                <DeleteOutlined />
              </template>
              删除
            </a-button>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref, h } from 'vue'
import { deleteUser, listUserVoByPage } from '@/api/userController'
import { message } from 'ant-design-vue'
import { SearchOutlined, CrownOutlined, UserOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'

// 排序状态
const sortInfo = ref<{ field?: string; order?: string }>({})

// 创建通用的带排序按钮的标题组件
const createSortableTitle = (title: string, sortField?: string) => {
  if (!sortField) {
    return title
  }
  return h('div', { style: 'display: flex; align-items: center; justify-content: space-between;' }, [
    h('span', title),
    h('div', { class: 'sort-buttons' }, [
      h('button', {
        class: 'sort-button',
        onClick: () => handleSort(sortField, 'asc'),
        title: `${title}升序`
      }, '▲'),
      h('button', {
        class: 'sort-button',
        onClick: () => handleSort(sortField, 'desc'),
        title: `${title}降序`
      }, '▼')
    ])
  ])
}

// 排序处理函数
const handleSort = (field: string, order: 'asc' | 'desc') => {
  // 执行排序
  data.value.sort((a: any, b: any) => {
    let aValue = a[field]
    let bValue = b[field]
    
    // 处理时间字段
    if (field.includes('Time')) {
      aValue = new Date(aValue || 0).getTime()
      bValue = new Date(bValue || 0).getTime()
    }
    
    // 处理数字字段
    if (typeof aValue === 'string' && !isNaN(Number(aValue))) {
      aValue = Number(aValue)
      bValue = Number(bValue)
    }
    
    if (order === 'asc') {
      return aValue > bValue ? 1 : -1
    } else {
      return aValue < bValue ? 1 : -1
    }
  })
  
  message.success(`已按${field}${order === 'asc' ? '升序' : '降序'}排序`)
}

const columns = [
  {
    title: () => createSortableTitle('ID', 'id'),
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
    width: 80,
  },
  {
    title: () => createSortableTitle('账号', 'userAccount'),
    dataIndex: 'userAccount',
    width: 140,
  },
  {
    title: () => createSortableTitle('用户名', 'userName'),
    dataIndex: 'userName',
    width: 150,
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
    ellipsis: true,
    width: 220,
  },
  {
    title: () => createSortableTitle('用户角色', 'userRole'),
    dataIndex: 'userRole',
    width: 140,
  },
  {
    title: () => createSortableTitle('VIP到期时间', 'vipExpireTime'),
    dataIndex: 'vipExpireTime',
    width: 180,
  },
  {
    title: () => createSortableTitle('创建时间', 'createTime'),
    dataIndex: 'createTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 100,
    fixed: 'right',
  },
]

// 展示的数据
const data = ref<API.UserVO[]>([])
const total = ref(0)
const loading = ref(false)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 统计数据
const adminCount = computed(() => 
  data.value.filter(user => user.userRole === 'admin').length
)

const normalCount = computed(() => 
  data.value.filter(user => user.userRole === 'user').length
)

// VIP统计数据
const vipCount = computed(() => 
  data.value.filter(user => isVipUser(user)).length
)

// 判断用户是否为VIP
const isVipUser = (user: any) => {
  // 如果有VIP到期时间且还未到期，则为VIP用户
  if (user.vipExpireTime) {
    return dayjs(user.vipExpireTime).isAfter(dayjs())
  }
  // 如果有isVip字段，则根据该字段判断
  if (user.isVip !== undefined) {
    return user.isVip
  }
  // 临时逻辑：管理员默认为VIP，或者用户名包含vip的为VIP
  return user.userRole === 'admin' || (user.userName && user.userName.toLowerCase().includes('vip'))
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await listUserVoByPage({
      ...searchParams,
      sortField: sortInfo.value.field,
      sortOrder: sortInfo.value.order === 'ascend' ? 'asc' : 'desc',
    })
    if (res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch {
    message.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total: number) => `共 ${total} 条记录`,
    pageSizeOptions: ['10', '20', '50', '100'],
  }
})

// 表格分页和排序变化时的操作
const doTableChange = (page: { current: number; pageSize: number }, filters: Record<string, any>, sorter: Record<string, any>) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  
  // 处理排序
  if (sorter && sorter.field) {
    sortInfo.value = {
      field: sorter.field,
      order: sorter.order
    }
  } else {
    sortInfo.value = {}
  }
  
  fetchData()
}

// 搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.pageNum = 1
  fetchData()
}

// 重置搜索
const doReset = () => {
  searchParams.userAccount = undefined
  searchParams.userName = undefined
  searchParams.userRole = undefined
  searchParams.pageNum = 1
  sortInfo.value = {}
  fetchData()
}

// 删除数据
const doDelete = async (id: number) => {
  if (!id) {
    return
  }
  const res = await deleteUser({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}

// 查看用户的应用 - 跳转到该用户的应用管理页面
const goToUserApps = (user: any) => {
  if (user && user.id) {
    console.log('跳转到用户应用页面，用户ID:', user.id, '用户ID类型:', typeof user.id, '用户信息:', user)
    // 确保ID作为字符串传递，避免JavaScript数字精度问题
    const userId = String(user.id)
    console.log('转换后的userId:', userId)
    // 跳转到专门显示该用户应用的页面
    window.open(`/admin/userApps/${userId}`, '_blank')
  } else {
    console.error('用户信息不完整:', user)
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#userManagePage {
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
  max-width: 1200px;
  margin: 0 auto;
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

.stats-row {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.stat-card {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  padding: 20px 24px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 182, 193, 0.2);
  transition: all 0.3s ease;
  min-width: 120px;
  text-align: center;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(255, 105, 180, 0.15);
  border-color: rgba(255, 105, 180, 0.3);
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #FF1493;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary, #666);
}

/* 搜索区域 */
.search-section {
  padding: 24px 40px;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border-color, #FFE4E1);
}

.search-form {
  max-width: 100%;
  margin: 0 auto;
}

.search-input,
.search-select {
  border-radius: 12px !important;
  border-color: rgba(255, 182, 193, 0.3) !important;
  transition: all 0.3s ease !important;
}

.search-input:focus,
.search-select:focus {
  border-color: #FF69B4 !important;
  box-shadow: 0 0 0 2px rgba(255, 105, 180, 0.1) !important;
}

.search-btn {
  background: linear-gradient(135deg, #FF69B4, #FF1493) !important;
  border: none !important;
  border-radius: 12px !important;
  box-shadow: 0 4px 15px rgba(255, 105, 180, 0.3) !important;
  transition: all 0.3s ease !important;
}

.search-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 6px 20px rgba(255, 105, 180, 0.4) !important;
}

.reset-btn {
  border-radius: 12px !important;
  border-color: rgba(255, 182, 193, 0.3) !important;
  color: var(--text-secondary, #666) !important;
  margin-left: 12px !important;
  transition: all 0.3s ease !important;
}

.reset-btn:hover {
  border-color: #FF69B4 !important;
  color: #FF1493 !important;
}

/* 表格区域 */
.table-section {
  padding: 24px 40px;
  max-width: 95%;
  margin: 0 auto;
}

.user-table {
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
  font-size: 14px !important;
}

:deep(.ant-table-tbody > tr) {
  transition: all 0.3s ease !important;
}

:deep(.ant-table-tbody > tr:hover) {
  background: rgba(255, 245, 248, 0.5) !important;
  transform: scale(1.01) !important;
}

:deep(.ant-table-tbody > tr > td) {
  border-bottom: 1px solid rgba(255, 182, 193, 0.1) !important;
  padding: 16px 12px !important;
}

.role-tag {
  border-radius: 12px !important;
  font-weight: 500 !important;
  display: inline-flex !important;
  align-items: center !important;
  gap: 4px !important;
  padding: 4px 12px !important;
}

.vip-tag {
  border-radius: 12px !important;
  font-weight: 500 !important;
  display: inline-flex !important;
  align-items: center !important;
  gap: 4px !important;
  padding: 4px 12px !important;
  background: linear-gradient(135deg, #9F7AEA, #805AD5) !important;
  border-color: #9F7AEA !important;
  color: white !important;
  box-shadow: 0 2px 8px rgba(159, 122, 234, 0.3) !important;
}

.normal-tag {
  border-radius: 12px !important;
  font-weight: 500 !important;
  display: inline-flex !important;
  align-items: center !important;
  gap: 4px !important;
  padding: 4px 12px !important;
}

.time-text {
  color: var(--text-secondary, #666);
  font-size: 13px;
}

.vip-time {
  color: #9F7AEA;
  font-weight: 500;
}

.no-vip-text {
  color: var(--text-secondary, #999);
  font-size: 12px;
  font-style: italic;
}

.delete-btn {
  border-radius: 8px !important;
  transition: all 0.3s ease !important;
}

.delete-btn:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3) !important;
}

/* 分页样式 */
:deep(.ant-pagination) {
  margin-top: 24px !important;
  text-align: center !important;
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

:deep(.ant-pagination-prev),
:deep(.ant-pagination-next) {
  border-radius: 8px !important;
  border-color: rgba(255, 182, 193, 0.3) !important;
  transition: all 0.3s ease !important;
}

:deep(.ant-pagination-prev:hover),
:deep(.ant-pagination-next:hover) {
  border-color: #FF69B4 !important;
  color: #FF1493 !important;
}

/* 排序按钮样式 - 与应用管理页面一致 */
:deep(.sort-buttons) {
  display: flex;
  flex-direction: column;
  margin-left: 8px;
}

:deep(.sort-button) {
  border: none;
  background: none;
  cursor: pointer;
  color: #FF69B4;
  font-size: 10px;
  line-height: 1;
  transition: all 0.2s ease;
  padding: 1px 2px;
}

:deep(.sort-button:hover) {
  color: #FF1493;
  transform: scale(1.2);
}

/* 用户名链接样式 */
.user-name-link {
  font-weight: 600;
  color: #FF69B4 !important;
  padding: 0 !important;
  text-decoration: none;
  border: none;
  background: none;
  box-shadow: none !important;
}

.user-name-link:hover {
  color: #FF1493 !important;
  text-decoration: underline;
  background: none !important;
  border: none !important;
  box-shadow: none !important;
}

.user-name-link:focus {
  color: #FF1493 !important;
  background: none !important;
  border: none !important;
  box-shadow: none !important;
}

/* 文本溢出处理 */
.truncated-text {
  max-width: 130px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
}

.user-name-link .truncated-text {
  max-width: 130px;
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
  
  .search-form {
    flex-direction: column;
  }
  
  .search-form .ant-form-item {
    margin-bottom: 16px;
  }
}
</style>
