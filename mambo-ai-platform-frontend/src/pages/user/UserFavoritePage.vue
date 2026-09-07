<template>
  <div id="userFavoritePage">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>我的收藏</h2>
      <p>管理您收藏的所有应用</p>
    </div>
    <a-divider />

    <!-- 收藏应用网格 -->
    <div class="favorite-grid" v-if="!loading">
      <AppCard
        v-for="app in data"
        :key="app.id"
        :app="app"
        @view-chat="viewChat"
        @view-work="viewWork"
        @toggle-favorite="toggleFavorite"
      />
    </div>
    <div class="favorite-grid" v-else>
      <a-skeleton v-for="n in 3" :key="n" class="card-skeleton" active />
    </div>

    <!-- 空状态 -->
    <a-empty v-if="!loading && data.length === 0" description="还没有收藏任何应用">
      <template #extra>
        <a-button type="primary" @click="goHome">去逛逛</a-button>
      </template>
    </a-empty>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <a-pagination
        v-model:current="searchParams.pageNum"
        v-model:page-size="searchParams.pageSize"
        :total="total"
        :show-size-changer="false"
        :show-total="(t: number) => `共 ${t} 个收藏`"
        @change="fetchData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { listMyFavoriteAppByPage, cancelFavorite } from '@/api/appController'
import { getDeployUrl } from '@/config/env'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()

const data = ref<API.AppVO[]>([])
const total = ref(0)
const loading = ref(true)

const searchParams = reactive<API.listMyFavoriteAppByPageParams>({
  pageNum: 1,
  pageSize: 6,
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listMyFavoriteAppByPage({
      pageNum: searchParams.pageNum,
      pageSize: searchParams.pageSize,
    })
    if (res.data.code === 0 && res.data.data) {
      data.value = res.data.data.records || []
      total.value = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载收藏失败：', error)
    message.error('加载收藏失败')
  } finally {
    loading.value = false
  }
}

const viewChat = (appId: string | number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}?view=1`)
  }
}

const viewWork = (app: API.AppVO) => {
  if (app.deployKey) {
    const url = getDeployUrl(app.deployKey)
    window.open(url, '_blank')
  }
}

const toggleFavorite = async (app: API.AppVO) => {
  if (!app.id) return
  try {
    const res = await cancelFavorite({ appId: app.id })
    if (res.data.code === 0) {
      message.success('已取消收藏')
      await fetchData()
    } else {
      message.error('取消收藏失败：' + res.data.message)
    }
  } catch (error) {
    console.error('取消收藏失败：', error)
    message.error('取消收藏失败')
  }
}

const goHome = () => {
  router.push('/')
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#userFavoritePage {
  padding: 20px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(255, 245, 248, 0.95));
  backdrop-filter: blur(10px);
  min-height: 100vh;
}

.page-header {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  margin-bottom: 8px;
  text-align: center;
  padding: 24px;
  border: 1px solid rgba(255, 182, 193, 0.2);
  backdrop-filter: blur(8px);
  box-shadow: 0 4px 12px rgba(255, 105, 180, 0.1);
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 28px;
  font-weight: 600;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 16px;
}

.favorite-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 32px;
  margin-bottom: 40px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

.card-skeleton {
  min-height: 200px;
  border-radius: 16px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.6);
}

@media (max-width: 768px) {
  .favorite-grid {
    grid-template-columns: 1fr;
    gap: 24px;
  }
}
</style>