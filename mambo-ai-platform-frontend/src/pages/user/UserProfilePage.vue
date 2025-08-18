<template>
  <div class="user-profile">
    <a-card title="个人信息">
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="用户ID">
          {{ loginUserStore.loginUser.id || '未知' }}
        </a-descriptions-item>
        <a-descriptions-item label="用户名">
          {{ loginUserStore.loginUser.userName || '未知' }}
        </a-descriptions-item>
        <a-descriptions-item label="用户账号">
          {{ loginUserStore.loginUser.userAccount || '未知' }}
        </a-descriptions-item>
        <a-descriptions-item label="用户角色">
          <a-tag :color="roleColor">{{ roleText }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="注册时间">
          {{ formatTime(loginUserStore.loginUser.createTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="最后更新">
          {{ formatTime(loginUserStore.loginUser.updateTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="用户头像" :span="2">
          <a-avatar :size="64" :src="loginUserStore.loginUser.userAvatar">
            {{ loginUserStore.loginUser.userName?.charAt(0) || 'U' }}
          </a-avatar>
        </a-descriptions-item>
        <a-descriptions-item label="个人简介" :span="2">
          {{ loginUserStore.loginUser.userProfile || '暂无个人简介' }}
        </a-descriptions-item>
      </a-descriptions>
      
      <div class="action-buttons">
        <a-button type="primary" @click="goToSettings">
          编辑个人信息
        </a-button>
        <a-button @click="goToVip">
          VIP中心
        </a-button>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser'
import { formatTime } from '@/utils/time'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 角色相关计算属性
const roleColor = computed(() => {
  const role = loginUserStore.loginUser.userRole
  switch (role) {
    case 'admin':
      return 'red'
    case 'vip':
      return 'gold'
    default:
      return 'blue'
  }
})

const roleText = computed(() => {
  const role = loginUserStore.loginUser.userRole
  switch (role) {
    case 'admin':
      return '管理员'
    case 'vip':
      return 'VIP用户'
    default:
      return '普通用户'
  }
})

// 方法
const goToSettings = () => {
  router.push('/user/settings')
}

const goToVip = () => {
  router.push('/vip')
}
</script>

<style scoped>
.user-profile {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.action-buttons {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}
</style>
