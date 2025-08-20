<template>
  <div class="app-card" :class="{ 'app-card--featured': featured }">
    <div class="app-preview">
      <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
      <div v-else class="app-placeholder">
        <span>🤖</span>
      </div>
      <div class="app-overlay">
        <a-space>
          <a-button type="primary" @click="handleViewChat">查看对话</a-button>
          <a-button v-if="app.deployKey" type="default" @click="handleViewWork">查看作品</a-button>
        </a-space>
      </div>
    </div>
    <div class="app-info">
      <div class="app-info-left">
        <a-avatar :src="app.user?.userAvatar" :size="40">
          {{ app.user?.userName?.charAt(0) || 'U' }}
        </a-avatar>
      </div>
      <div class="app-info-right">
        <h3 class="app-title">{{ app.appName || '未命名应用' }}</h3>
        <p class="app-author">
          {{ app.user?.userName || (featured ? '官方' : '未知用户') }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  app: API.AppVO
  featured?: boolean
}

interface Emits {
  (e: 'view-chat', appId: string | number | undefined): void
  (e: 'view-work', app: API.AppVO): void
}

const props = withDefaults(defineProps<Props>(), {
  featured: false,
})

const emit = defineEmits<Emits>()

const handleViewChat = () => {
  emit('view-chat', props.app.id)
}

const handleViewWork = () => {
  emit('view-work', props.app)
}
</script>

<style scoped>
.app-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(255, 105, 180, 0.15);
  backdrop-filter: blur(15px);
  border: 2px solid rgba(255, 182, 193, 0.2);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
}

.app-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 105, 180, 0.05), rgba(255, 182, 193, 0.03));
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 20px;
}

.app-card:hover::before {
  opacity: 1;
}

.app-card:hover {
  transform: translateY(-12px) scale(1.02);
  box-shadow: 0 20px 60px rgba(255, 105, 180, 0.25);
  border-color: rgba(255, 105, 180, 0.4);
}

.app-card--featured {
  border-color: rgba(255, 20, 147, 0.3);
  box-shadow: 0 8px 32px rgba(255, 20, 147, 0.2);
}

.app-card--featured::before {
  background: linear-gradient(135deg, rgba(255, 20, 147, 0.08), rgba(255, 105, 180, 0.05));
}

.app-card--featured:hover {
  border-color: rgba(255, 20, 147, 0.5);
  box-shadow: 0 20px 60px rgba(255, 20, 147, 0.3);
}

.app-preview {
  height: 200px;
  background: linear-gradient(135deg, #FFF5F8, #FFE4E1);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.app-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.app-card:hover .app-preview img {
  transform: scale(1.1);
}

.app-placeholder {
  font-size: 56px;
  background: linear-gradient(135deg, #FF69B4, #FFB6C1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 2px 4px rgba(255, 105, 180, 0.2));
}

.app-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 105, 180, 0.85), rgba(255, 182, 193, 0.8));
  backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 20px;
}

.app-card:hover .app-overlay {
  opacity: 1;
}

.app-overlay .ant-space {
  width: 100%;
  justify-content: center;
}

.app-overlay .ant-btn {
  border-radius: 25px;
  font-weight: 600;
  font-size: 14px;
  height: 44px;
  padding: 0 24px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  border: none;
  min-width: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
}

.app-overlay .ant-btn-primary {
  background: linear-gradient(135deg, #FFFFFF, #FFF8DC) !important;
  color: #FF1493 !important;
  box-shadow: 0 6px 20px rgba(255, 255, 255, 0.4) !important;
  font-weight: 700 !important;
}

.app-overlay .ant-btn-primary:hover {
  background: linear-gradient(135deg, #FFFFFF, #FFFAF0) !important;
  transform: translateY(-3px) scale(1.05) !important;
  box-shadow: 0 8px 25px rgba(255, 255, 255, 0.6) !important;
}

.app-overlay .ant-btn-default {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.25), rgba(255, 255, 255, 0.15)) !important;
  color: white !important;
  border: 2px solid rgba(255, 255, 255, 0.4) !important;
  backdrop-filter: blur(15px) !important;
  font-weight: 600 !important;
}

.app-overlay .ant-btn-default:hover {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.35), rgba(255, 255, 255, 0.25)) !important;
  transform: translateY(-3px) scale(1.05) !important;
  border-color: rgba(255, 255, 255, 0.6) !important;
  box-shadow: 0 8px 25px rgba(255, 255, 255, 0.3) !important;
}

.app-info {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

.app-info-left {
  flex-shrink: 0;
}

.app-info-left .ant-avatar {
  border: 2px solid rgba(255, 105, 180, 0.2);
  transition: all 0.3s ease;
}

.app-card:hover .app-info-left .ant-avatar {
  border-color: rgba(255, 105, 180, 0.4);
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(255, 105, 180, 0.2);
}

.app-info-right {
  flex: 1;
  min-width: 0;
}

.app-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 6px;
  color: var(--text-primary, #333);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.3s ease;
}

.app-card:hover .app-title {
  color: #FF1493;
}

.app-author {
  font-size: 14px;
  color: var(--text-secondary, #666);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  opacity: 0.8;
}

/* 特色应用标识 */
.app-card--featured .app-title {
  background: linear-gradient(135deg, #FF1493, #FF69B4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 700;
}

.app-card--featured .app-author::after {
  content: '⭐';
  margin-left: 4px;
  color: #FFD700;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-overlay .ant-btn {
    height: 38px;
    font-size: 13px;
    padding: 0 18px;
    min-width: 85px;
  }
  
  .app-overlay {
    padding: 15px;
  }
  
  .app-overlay .ant-space {
    gap: 8px !important;
  }
}

@media (max-width: 480px) {
  .app-overlay .ant-btn {
    height: 36px;
    font-size: 12px;
    padding: 0 16px;
    min-width: 80px;
  }
  
  .app-overlay {
    padding: 12px;
  }
}
</style>
