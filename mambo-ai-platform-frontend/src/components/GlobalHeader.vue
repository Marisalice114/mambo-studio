<template>
  <a-layout-header class="header">
    <a-row :wrap="false">
      <!-- 左侧：Logo和标题 -->
      <a-col flex="200px">
        <RouterLink to="/">
          <div class="header-left">
            <img class="logo" src="@/assets/logo.svg" alt="Mambo Studio Logo" />
            <h1 class="site-title">Mambo Studio</h1>
          </div>
        </RouterLink>
      </a-col>
      <!-- 中间：导航菜单 -->
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="menuItems"
          @click="handleMenuClick"
        />
      </a-col>
      <!-- 右侧：用户操作区域 -->
      <a-col>
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <!-- 用户头像 -->
                <a-avatar 
                  :src="userAvatarUrl" 
                  :class="{ 'vip-avatar': isVipUser }"
                >
                  {{ loginUserStore.loginUser.userName?.charAt(0) || 'U' }}
                </a-avatar>
                <!-- 用户名 -->
                <span 
                  class="user-name" 
                  :class="{ 'vip-user-name': isVipUser }"
                >
                  {{ loginUserStore.loginUser.userName ?? '无名' }}
                </span>
                <!-- VIP标识 -->
                <a-tag v-if="isVipUser" color="gold" class="vip-badge">
                  <CrownOutlined />
                  VIP
                </a-tag>
                <!-- VIP到期提醒 -->
                <a-tooltip v-if="isVipUser && vipExpireTime" :title="`VIP将于 ${formatVipExpireTime} 到期`">
                  <ClockCircleOutlined class="vip-expire-icon" />
                </a-tooltip>
              </a-space>
              <template #overlay>
                <a-menu>
                  <!-- VIP专属菜单项 -->
                  <a-menu-item v-if="isVipUser" @click="goToVipCenter" class="vip-menu-item">
                    <CrownOutlined style="color: #ffd700;" />
                    VIP中心
                  </a-menu-item>
                  <!-- 个人信息 -->
                  <a-menu-item @click="goToProfile">
                    <UserOutlined />
                    个人信息
                  </a-menu-item>
                  <!-- 我的应用 -->
                  <a-menu-item @click="goToMyApps">
                    <AppstoreOutlined />
                    我的应用
                  </a-menu-item>
                  <!-- 账户设置 -->
                  <a-menu-item @click="goToSettings">
                    <SettingOutlined />
                    账户设置
                  </a-menu-item>
                  <a-menu-divider />
                  <!-- 退出登录 -->
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { useRouter } from 'vue-router'
import { type MenuProps, message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { userLogout } from '@/api/userController'
import {
  LogoutOutlined,
  HomeOutlined,
  CrownOutlined,
  ClockCircleOutlined,
  UserOutlined,
  SettingOutlined,
  AppstoreOutlined
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { getUserAvatarUrl } from '@/utils/avatar'

const loginUserStore = useLoginUserStore()
const router = useRouter()
// 当前选中菜单
const selectedKeys = ref<string[]>(['/'])
// 监听路由变化，更新当前选中菜单
router.afterEach((to) => {
  selectedKeys.value = [to.path]
})

// VIP相关计算属性
const isVipUser = computed(() => {
  const user = loginUserStore.loginUser
  // 临时测试：可以通过用户角色或者用户名来判断VIP状态
  // 如果用户角色是admin或者用户名包含"vip"，则显示为VIP用户（仅用于测试）
  return user && (user.userRole === 'admin' || 
                  (user.userName && user.userName.toLowerCase().includes('vip')))
})

const vipExpireTime = computed(() => {
  // 临时测试VIP到期时间
  if (isVipUser.value) {
    return dayjs().add(30, 'day').format('YYYY-MM-DD HH:mm:ss')
  }
  return null
})

const formatVipExpireTime = computed(() => {
  if (vipExpireTime.value) {
    return dayjs(vipExpireTime.value).format('YYYY-MM-DD HH:mm')
  }
  return ''
})

// 用户头像URL
const userAvatarUrl = computed(() => {
  return getUserAvatarUrl(loginUserStore.loginUser, 40)
})

// 菜单配置项
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/workflow',
    label: 'AI工作流',
    title: 'AI工作流',
  },
  {
    key: '/user/apps',
    label: '我的应用',
    title: '我的应用',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/appManage',
    label: '应用管理',
    title: '应用管理',
  },
]

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    const loginUser = loginUserStore.loginUser
    
    // 管理员菜单只对admin用户显示
    if (menuKey?.startsWith('/admin')) {
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    
    // 用户菜单只对已登录用户显示
    if (menuKey?.startsWith('/user/apps')) {
      if (!loginUser || !loginUser.id) {
        return false
      }
    }
    
    return true
  })
}

// 展示在菜单的路由数组
const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  // 跳转到对应页面
  if (key.startsWith('/')) {
    router.push(key)
  }
}

// 退出登录
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}

// 跳转到VIP中心
const goToVipCenter = () => {
  router.push('/vip')
}

// 跳转到个人信息
const goToProfile = () => {
  router.push('/user/profile')
}

// 跳转到我的应用
const goToMyApps = () => {
  router.push('/user/apps')
}

// 跳转到账户设置
const goToSettings = () => {
  router.push('/user/settings')
}
</script>

<style scoped>
.header {
  background: rgba(255, 255, 255, 0.95) !important;
  backdrop-filter: blur(15px);
  box-shadow: 0 2px 20px rgba(255, 105, 180, 0.1);
  border-bottom: 1px solid rgba(255, 182, 193, 0.2);
  position: sticky;
  top: 0;
  z-index: 100;
  height: 64px;
  line-height: 64px;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s ease;
  padding: 0;
  position: relative;
  z-index: 101;
  height: 64px;
  max-width: 200px;
  overflow: hidden;
}

.header-left:hover {
  transform: scale(1.02);
}

.logo {
  height: 32px;
  width: 32px;
  border-radius: 50%;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  filter: drop-shadow(0 2px 4px rgba(255, 105, 180, 0.2));
  flex-shrink: 0;
  z-index: 102;
  position: relative;
}

.logo:hover {
  transform: rotate(5deg) scale(1.05);
  filter: drop-shadow(0 4px 8px rgba(255, 105, 180, 0.3));
}

.site-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  font-family: 'Fredoka One', 'Comic Neue', 'Bubblegum Sans', 'Chilanka', 'Comic Sans MS', 'Microsoft YaHei', '微软雅黑', 'PingFang SC', 'Hiragino Sans GB', sans-serif;
  background: linear-gradient(135deg, #FF69B4, #FF1493, #FFB6C1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  transition: all 0.3s ease;
  letter-spacing: 1px;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  height: auto;
  display: flex;
  align-items: center;
  text-shadow: 1px 1px 2px rgba(255, 105, 180, 0.2);
  transform: perspective(100px) rotateX(3deg);
}

.user-login-status {
  display: flex;
  align-items: center;
  height: 64px;
}

.user-name {
  font-weight: 500;
  color: var(--text-primary, #333);
  transition: all 0.3s ease;
}

/* VIP用户名样式 */
.vip-user-name {
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(255, 215, 0, 0.3);
  background: linear-gradient(45deg, #d4af37, #ffd700);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* VIP头像样式 */
.vip-avatar {
  border: 2px solid #ffd700;
  box-shadow: 0 0 12px rgba(255, 215, 0, 0.4);
  position: relative;
}

.vip-avatar::before {
  content: '';
  position: absolute;
  top: -3px;
  left: -3px;
  right: -3px;
  bottom: -3px;
  background: linear-gradient(45deg, #ffd700, #ffed4e, #ffa500, #ffd700);
  border-radius: 50%;
  z-index: -1;
  animation: vip-glow 3s linear infinite;
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
  box-shadow: 0 3px 6px rgba(255, 215, 0, 0.4);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.vip-badge:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.6);
}

.vip-expire-icon {
  color: #ffa500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.vip-expire-icon:hover {
  color: #ff8c00;
  transform: scale(1.1);
}

.ant-menu-horizontal {
  border-bottom: none !important;
  background: transparent !important;
}

/* VIP菜单项样式 */
:deep(.vip-menu-item) {
  background: linear-gradient(135deg, #fff9e6, #fffbf0);
  border-left: 3px solid #ffd700;
  border-radius: 4px;
}

:deep(.vip-menu-item:hover) {
  background: linear-gradient(135deg, #fff3cd, #fff8e1);
  transform: translateX(2px);
}

/* 菜单项hover效果 */
:deep(.ant-menu-item) {
  transition: all 0.3s ease !important;
  border-radius: 6px !important;
  margin: 0 4px !important;
}

:deep(.ant-menu-item:hover) {
  background: linear-gradient(135deg, #FFF5F8, #FFE4E1) !important;
  transform: translateY(-1px) !important;
}

:deep(.ant-menu-item-selected) {
  background: linear-gradient(135deg, #FF69B4, #FFB6C1) !important;
  color: white !important;
}

/* 登录按钮样式 */
:deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #FF69B4, #FF1493) !important;
  border: none !important;
  box-shadow: 0 4px 15px rgba(255, 105, 180, 0.3) !important;
  border-radius: 20px !important;
  font-weight: 500 !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

:deep(.ant-btn-primary:hover) {
  transform: translateY(-2px) scale(1.02) !important;
  box-shadow: 0 6px 20px rgba(255, 105, 180, 0.4) !important;
}
</style>
