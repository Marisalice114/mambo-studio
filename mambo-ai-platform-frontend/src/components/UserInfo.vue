<template>
  <div class="user-info">
    <a-avatar 
      :src="userAvatarUrl" 
      :size="size"
      :class="{ 'vip-avatar': isVipUser }"
    >
      {{ user?.userName?.charAt(0) || 'U' }}
    </a-avatar>
    <span 
      v-if="showName" 
      class="user-name"
      :class="{ 'vip-user-name': isVipUser }"
    >
      {{ user?.userName || '未知用户' }}
    </span>
    <!-- VIP标识 -->
    <a-tag v-if="isVipUser && showVipBadge" color="gold" class="mini-vip-badge">
      <CrownOutlined />
    </a-tag>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { CrownOutlined } from '@ant-design/icons-vue'
import { getUserAvatarUrl } from '@/utils/avatar'

interface Props {
  user?: any // 临时使用any，等API类型定义完善后修改
  size?: number | 'small' | 'default' | 'large'
  showName?: boolean
  showVipBadge?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 'default',
  showName: true,
  showVipBadge: false,
})

// 判断是否为VIP用户（暂时模拟，等后端API支持）
const isVipUser = computed(() => {
  const user = props.user
  // 暂时通过用户角色判断，实际应该通过isVip字段和vipExpireTime
  return user && (user.userRole === 'admin' || 
                  (user.userName && user.userName.toLowerCase().includes('vip')))
})

// 用户头像URL
const userAvatarUrl = computed(() => {
  const sizeMap = {
    'small': 32,
    'default': 40,
    'large': 64
  }
  const avatarSize = typeof props.size === 'number' ? props.size : sizeMap[props.size]
  return getUserAvatarUrl(props.user, avatarSize)
})
</script>

<style scoped>
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-size: 14px;
  color: #1a1a1a;
  transition: all 0.3s ease;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* VIP用户名样式 */
.vip-user-name {
  color: #d4af37;
  font-weight: 600;
  background: linear-gradient(45deg, #d4af37, #ffd700);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* VIP头像样式 */
.vip-avatar {
  border: 2px solid #ffd700;
  box-shadow: 0 0 6px rgba(255, 215, 0, 0.4);
  position: relative;
}

.vip-avatar::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, #ffd700, #ffed4e, #ffa500, #ffd700);
  border-radius: 50%;
  z-index: -1;
  animation: vip-glow-small 3s linear infinite;
}

@keyframes vip-glow-small {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 迷你VIP标识 */
.mini-vip-badge {
  font-size: 10px;
  padding: 0 4px;
  height: 16px;
  line-height: 14px;
  border-radius: 2px;
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  border: 1px solid #ffd700;
  color: #8b4513;
}
</style>
