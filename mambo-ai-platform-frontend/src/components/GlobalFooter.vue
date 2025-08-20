<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      <div class="footer-decoration"></div>
      <p class="copyright">
        <span class="brand-text">Mambo Studio</span> AI 创作工作室 © 2025
      </p>
      <p class="subtitle">
        智能创作，无限可能，让每个想法都成为现实
      </p>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// 无需额外的响应式数据
</script>

<style scoped>
.footer {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(255, 245, 248, 0.9));
  backdrop-filter: blur(15px);
  text-align: center;
  padding: 40px 20px;
  margin-top: 60px;
  border-top: 1px solid var(--border-color, #FFE4E1);
  position: relative;
  overflow: hidden;
}

.footer-content {
  position: relative;
  z-index: 2;
}

.footer-decoration {
  position: absolute;
  top: -15px;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 4px;
  background: linear-gradient(90deg, transparent, #FF69B4, #FFB6C1, #FF69B4, transparent);
  border-radius: 2px;
}

.copyright {
  margin: 20px 0 8px 0;
  color: var(--text-primary, #333);
  font-size: 16px;
  font-weight: 500;
}

.brand-text {
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  color: var(--text-secondary, #666);
  font-size: 14px;
  opacity: 0.8;
}

/* 添加浮动粒子效果 */
.footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 20% 50%, rgba(255, 105, 180, 0.1) 0%, transparent 50%),
              radial-gradient(circle at 80% 50%, rgba(255, 182, 193, 0.1) 0%, transparent 50%),
              radial-gradient(circle at 40% 80%, rgba(255, 105, 180, 0.05) 0%, transparent 50%);
  animation: float-particles 10s ease-in-out infinite;
}

@keyframes float-particles {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}
</style>
