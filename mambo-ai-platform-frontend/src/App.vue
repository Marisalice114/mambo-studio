<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
</script>

<template>
  <BasicLayout />
</template>

<style>
/* 全局样式优化 */
:root {
  /* Mambo主题色彩 */
  --primary-color: #FF69B4;
  --primary-light: #FFB6C1;
  --primary-lighter: #FFC0CB;
  --secondary-color: #FFFFFF;
  --accent-color: #FF1493;
  --text-primary: #333333;
  --text-secondary: #666666;
  --background-main: #FFFFFF;
  --background-soft: #FFF5F8;
  --border-color: #FFE4E1;
  --shadow-color: rgba(255, 105, 180, 0.1);
  --gradient-primary: linear-gradient(135deg, #FF69B4, #FFB6C1);
  --gradient-soft: linear-gradient(135deg, #FFF5F8, #FFFFFF);
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell',
    'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  overflow-x: hidden;
  background: var(--background-main);
  color: var(--text-primary);
}

html {
  overflow-x: hidden;
}

/* 全局动画基类 */
.mambo-transition {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.mambo-hover-scale:hover {
  transform: scale(1.02);
}

.mambo-hover-lift:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px var(--shadow-color);
}

/* 全局按钮样式增强 */
.ant-btn-primary {
  background: var(--gradient-primary) !important;
  border: none !important;
  box-shadow: 0 4px 15px var(--shadow-color) !important;
  transition: all 0.3s ease !important;
}

.ant-btn-primary:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 6px 20px var(--shadow-color) !important;
}

/* 卡片样式增强 */
.ant-card {
  border: 1px solid var(--border-color) !important;
  box-shadow: 0 2px 8px var(--shadow-color) !important;
  transition: all 0.3s ease !important;
}

.ant-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px var(--shadow-color) !important;
}
</style>
