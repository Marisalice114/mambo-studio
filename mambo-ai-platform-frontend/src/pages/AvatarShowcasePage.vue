<template>
  <div class="avatar-showcase">
    <a-card title="用户头像效果展示">
      <a-row :gutter="24">
        <a-col :span="8">
          <h3>普通用户头像</h3>
          <div class="avatar-demo-grid">
            <div v-for="user in normalUsers" :key="user.id" class="avatar-item">
              <a-avatar :src="getUserAvatarUrl(user, 64)" size="large">
                {{ user.userName.charAt(0) }}
              </a-avatar>
              <p>{{ user.userName }}</p>
            </div>
          </div>
        </a-col>
        
        <a-col :span="8">
          <h3>VIP用户头像</h3>
          <div class="avatar-demo-grid">
            <div v-for="user in vipUsers" :key="user.id" class="avatar-item">
              <a-avatar 
                :src="getUserAvatarUrl(user, 64)" 
                size="large"
                class="vip-avatar"
              >
                {{ user.userName.charAt(0) }}
              </a-avatar>
              <p class="vip-user-name">{{ user.userName }}</p>
            </div>
          </div>
        </a-col>
        
        <a-col :span="8">
          <h3>不同尺寸展示</h3>
          <div class="size-demo">
            <a-space direction="vertical" align="center">
              <div class="size-item">
                <a-avatar :src="getUserAvatarUrl(demoUser, 32)" size="small" />
                <span>小 (32px)</span>
              </div>
              <div class="size-item">
                <a-avatar :src="getUserAvatarUrl(demoUser, 40)" size="default" />
                <span>默认 (40px)</span>
              </div>
              <div class="size-item">
                <a-avatar :src="getUserAvatarUrl(demoUser, 64)" size="large" />
                <span>大 (64px)</span>
              </div>
              <div class="size-item">
                <a-avatar :src="getUserAvatarUrl(demoUser, 80)" :size="80" />
                <span>特大 (80px)</span>
              </div>
            </a-space>
          </div>
        </a-col>
      </a-row>
      
      <!-- 头像说明 -->
      <a-divider />
      <div class="avatar-description">
        <h4>头像特色说明</h4>
        <ul>
          <li><strong>颜色一致性：</strong>同一用户名总是生成相同颜色的头像</li>
          <li><strong>美观设计：</strong>渐变背景、装饰图案、文字阴影等精美效果</li>
          <li><strong>VIP特殊效果：</strong>VIP用户头像带有金色主题和皇冠装饰</li>
          <li><strong>响应式设计：</strong>支持不同尺寸的自适应显示</li>
          <li><strong>自动回退：</strong>当用户没有上传头像时自动使用生成的默认头像</li>
        </ul>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { getUserAvatarUrl } from '@/utils/avatar'

// 测试用户数据
const normalUsers = [
  { id: 1, userName: '张三', userAvatar: '', userRole: 'user' },
  { id: 2, userName: '李四', userAvatar: '', userRole: 'user' },
  { id: 3, userName: '王五', userAvatar: '', userRole: 'user' },
  { id: 4, userName: '赵六', userAvatar: '', userRole: 'user' },
  { id: 5, userName: 'Alice', userAvatar: '', userRole: 'user' },
  { id: 6, userName: 'Bob', userAvatar: '', userRole: 'user' },
]

const vipUsers = [
  { id: 7, userName: 'VIP会员', userAvatar: '', userRole: 'admin' },
  { id: 8, userName: '金牌用户', userAvatar: '', userRole: 'admin' },
  { id: 9, userName: '至尊VIP', userAvatar: '', userRole: 'admin' },
  { id: 10, userName: 'Diamond', userAvatar: '', userRole: 'admin' },
  { id: 11, userName: '白金会员', userAvatar: '', userRole: 'admin' },
  { id: 12, userName: 'Premium', userAvatar: '', userRole: 'admin' },
]

const demoUser = { id: 0, userName: '演示用户', userAvatar: '', userRole: 'user' }
</script>

<style scoped>
.avatar-showcase {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.avatar-demo-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.avatar-item {
  text-align: center;
  padding: 12px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background: #fafafa;
}

.avatar-item p {
  margin: 8px 0 0 0;
  font-size: 12px;
  color: #666;
}

.size-demo {
  text-align: center;
}

.size-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.avatar-description {
  background: #f9f9f9;
  padding: 16px;
  border-radius: 8px;
}

.avatar-description h4 {
  margin-bottom: 12px;
  color: #1890ff;
}

.avatar-description ul {
  margin: 0;
  padding-left: 20px;
}

.avatar-description li {
  margin-bottom: 8px;
  line-height: 1.5;
}

/* VIP样式复用 */
.vip-user-name {
  color: #d4af37;
  font-weight: 600;
  background: linear-gradient(45deg, #d4af37, #ffd700);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

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
</style>
