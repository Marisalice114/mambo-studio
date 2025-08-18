<template>
  <div class="style-demo">
    <a-card title="VIP用户样式演示">
      <a-row :gutter="32">
        <a-col :span="12">
          <h3>普通用户</h3>
          <div class="user-demo">
            <UserInfo 
              :user="normalUser" 
              :showName="true" 
              :showVipBadge="false"
              size="large"
            />
          </div>
          <div class="header-demo">
            <a-avatar :src="normalUserAvatarUrl" size="large">
              {{ normalUser.userName.charAt(0) }}
            </a-avatar>
            <span class="user-name">{{ normalUser.userName }}</span>
          </div>
        </a-col>
        
        <a-col :span="12">
          <h3>VIP用户</h3>
          <div class="user-demo">
            <UserInfo 
              :user="vipUser" 
              :showName="true" 
              :showVipBadge="true"
              size="large"
            />
          </div>
          <div class="header-demo">
            <a-avatar 
              :src="vipUserAvatarUrl" 
              size="large"
              class="vip-avatar"
            >
              {{ vipUser.userName.charAt(0) }}
            </a-avatar>
            <span class="user-name vip-user-name">{{ vipUser.userName }}</span>
            <a-tag color="gold" class="vip-badge">
              <CrownOutlined />
              VIP
            </a-tag>
          </div>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { CrownOutlined } from '@ant-design/icons-vue'
import UserInfo from '@/components/UserInfo.vue'
import { getUserAvatarUrl } from '@/utils/avatar'

// 模拟用户数据
const normalUser = {
  id: 1,
  userName: '普通用户',
  userAvatar: '', // 空字符串，将使用生成的默认头像
  userRole: 'user'
}

const vipUser = {
  id: 2,
  userName: 'VIP用户',
  userAvatar: '', // 空字符串，将使用生成的VIP头像
  userRole: 'admin' // 通过admin角色触发VIP样式
}

// 生成头像URL
const normalUserAvatarUrl = computed(() => getUserAvatarUrl(normalUser, 64))
const vipUserAvatarUrl = computed(() => getUserAvatarUrl(vipUser, 64))
</script>

<style scoped>
.style-demo {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.user-demo {
  padding: 16px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 16px;
  background: #fafafa;
}

.header-demo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background: #fafafa;
}

/* 复制GlobalHeader中的VIP样式 */
.user-name {
  font-weight: 500;
  color: #333;
  transition: all 0.3s ease;
}

.vip-user-name {
  color: #d4af37;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(212, 175, 55, 0.3);
  background: linear-gradient(45deg, #d4af37, #ffd700);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.vip-avatar {
  border: 2px solid #ffd700;
  box-shadow: 0 0 8px rgba(255, 215, 0, 0.4);
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
  animation: vip-glow 2s linear infinite;
}

@keyframes vip-glow {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.vip-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: bold;
  border: 1px solid #ffd700;
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  color: #8b4513;
  box-shadow: 0 2px 4px rgba(255, 215, 0, 0.3);
}
</style>
