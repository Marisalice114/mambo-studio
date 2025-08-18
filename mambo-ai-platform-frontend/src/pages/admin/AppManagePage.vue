<template>
  <div id="appManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="应用名称">
        <a-input v-model:value="searchParams.appName" placeholder="输入应用名称" />
      </a-form-item>
      <a-form-item label="创建者">
        <a-input v-model:value="searchParams.userId" placeholder="输入用户ID" />
      </a-form-item>
      <a-form-item label="生成类型">
        <a-select
          v-model:value="searchParams.codeGenType"
          placeholder="选择生成类型"
          style="width: 150px"
        >
          <a-select-option value="">全部</a-select-option>
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
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <a-divider />

    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      @change="doTableChange"
      :scroll="{ x: 1200 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'cover'">
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
          <a-tag v-if="record.priority === 99" color="gold">精选</a-tag>
          <span v-else>{{ record.priority || 0 }}</span>
        </template>
        <template v-else-if="column.dataIndex === 'deployedTime'">
          <span v-if="record.deployedTime">
            {{ formatTime(record.deployedTime) }}
          </span>
          <span v-else class="text-gray">未部署</span>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ formatTime(record.createTime) }}
        </template>
        <template v-else-if="column.dataIndex === 'user'">
          <UserInfo :user="record.user" size="small" />
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
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref, h } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { listAppVoByPageByAdmin, deleteAppByAdmin, updateAppByAdmin } from '@/api/appController'
import { CODE_GEN_TYPE_OPTIONS, formatCodeGenType } from '@/utils/codeGenTypes'
import { formatTime } from '@/utils/time'
import UserInfo from '@/components/UserInfo.vue'
import dayjs from 'dayjs'

const router = useRouter()

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

const columns = [
  {
    title: () => createSortableTitle('ID', 'id'),
    dataIndex: 'id',
    width: 80,
  },
  {
    title: () => createSortableTitle('应用名称', 'appName'),
    dataIndex: 'appName',
    width: 150,
  },
  {
    title: '封面',
    dataIndex: 'cover',
    width: 100,
  },
  {
    title: () => createSortableTitle('初始提示词'),
    dataIndex: 'initPrompt',
    width: 200,
  },
  {
    title: () => createSortableTitle('生成类型'),
    dataIndex: 'codeGenType',
    width: 100,
  },
  {
    title: () => createSortableTitle('优先级', 'priority'),
    dataIndex: 'priority',
    width: 80,
  },
  {
    title: () => createSortableTitle('部署时间', 'deployedTime'),
    dataIndex: 'deployedTime',
    width: 160,
  },
  {
    title: () => createSortableTitle('创建者'),
    dataIndex: 'user',
    width: 120,
  },
  {
    title: () => createSortableTitle('创建时间', 'createTime'),
    dataIndex: 'createTime',
    width: 160,
    customRender: ({ record }) => {
      return dayjs(record.createTime).format('YYYY-MM-DD HH:mm')
    }
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right',
  },
]

// 数据
const data = ref<API.AppVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 获取数据
const fetchData = async () => {
  try {
    const res = await listAppVoByPageByAdmin({
      ...searchParams,
    })
    if (res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch (error) {
    console.error('获取数据失败：', error)
    message.error('获取数据失败')
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 表格变化处理
const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索
const doSearch = () => {
  // 重置页码
  searchParams.pageNum = 1
  fetchData()
}

// 编辑应用
const editApp = (app: API.AppVO) => {
  router.push(`/app/edit/${app.id}`)
}

// 切换精选状态
const toggleFeatured = async (app: API.AppVO) => {
  if (!app.id) return

  const newPriority = app.priority === 99 ? 0 : 99

  try {
    const res = await updateAppByAdmin({
      id: app.id,
      priority: newPriority,
    })

    if (res.data.code === 0) {
      message.success(newPriority === 99 ? '已设为精选' : '已取消精选')
      // 刷新数据
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
const deleteApp = async (id: number | undefined) => {
  if (!id) return

  try {
    const res = await deleteAppByAdmin({ id })
    if (res.data.code === 0) {
      message.success('删除成功')
      // 刷新数据
      fetchData()
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error) {
    console.error('删除失败：', error)
    message.error('删除失败')
  }
}

// 添加排序处理函数
const handleSort = (field: string, order: 'asc' | 'desc') => {
  searchParams.sortField = field
  searchParams.sortOrder = order === 'asc' ? 'ascend' : 'descend'
  fetchData() // 修正函数名
}
</script>

<style scoped>
#appManagePage {
  padding: 0;
  background: var(--background-main, #FFFFFF);
  min-height: 100vh;
}

/* 页面头部样式 */
.page-header {
  background: linear-gradient(135deg, #FFFFFF 0%, #FFF5F8 100%);
  padding: 32px 24px;
  border-bottom: 1px solid var(--border-color, #FFE4E1);
  box-shadow: 0 2px 8px rgba(255, 105, 180, 0.08);
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

/* 搜索表单样式 */
:deep(.ant-form-inline) {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(10px);
  padding: 24px;
  border-bottom: 1px solid var(--border-color, #FFE4E1);
}

:deep(.ant-input) {
  border-radius: 12px !important;
  border-color: rgba(255, 182, 193, 0.3) !important;
  transition: all 0.3s ease !important;
}

:deep(.ant-input:focus) {
  border-color: #FF69B4 !important;
  box-shadow: 0 0 0 2px rgba(255, 105, 180, 0.1) !important;
}

:deep(.ant-select) {
  border-radius: 12px !important;
}

:deep(.ant-select .ant-select-selector) {
  border-radius: 12px !important;
  border-color: rgba(255, 182, 193, 0.3) !important;
  transition: all 0.3s ease !important;
}

:deep(.ant-select:focus .ant-select-selector),
:deep(.ant-select-focused .ant-select-selector) {
  border-color: #FF69B4 !important;
  box-shadow: 0 0 0 2px rgba(255, 105, 180, 0.1) !important;
}

:deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #FF69B4, #FF1493) !important;
  border: none !important;
  border-radius: 12px !important;
  box-shadow: 0 4px 15px rgba(255, 105, 180, 0.3) !important;
  transition: all 0.3s ease !important;
}

:deep(.ant-btn-primary:hover) {
  transform: translateY(-2px) !important;
  box-shadow: 0 6px 20px rgba(255, 105, 180, 0.4) !important;
}

/* 表格样式 */
:deep(.ant-table-wrapper) {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

:deep(.ant-table) {
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
  vertical-align: middle;
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

.prompt-text {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-primary, #333);
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

:deep(.ant-btn-dangerous) {
  border-color: rgba(255, 77, 79, 0.3) !important;
}

:deep(.ant-btn-dangerous:hover) {
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3) !important;
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

/* 排序按钮样式 */
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

/* 响应式设计 */
@media (max-width: 768px) {
  :deep(.ant-form-inline) {
    padding: 16px;
  }
  
  :deep(.ant-table-wrapper) {
    padding: 16px;
  }
  
  .page-header {
    padding: 20px 16px;
  }
  
  .page-title {
    font-size: 24px;
  }
}
</style>
