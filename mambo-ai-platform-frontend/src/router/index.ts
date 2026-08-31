import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '@/pages/HomePage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '主页',
      component: HomePage,
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: () => import('@/pages/user/UserLoginPage.vue'),
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: () => import('@/pages/user/UserRegisterPage.vue'),
    },
    {
      path: '/user/profile',
      name: '个人信息',
      component: () => import('@/pages/user/UserProfilePage.vue'),
    },
    {
      path: '/user/settings',
      name: '账户设置',
      component: () => import('@/pages/user/UserSettingsPage.vue'),
    },
    {
      path: '/user/apps',
      name: '我的应用',
      component: () => import('@/pages/user/UserAppManagePage.vue'),
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: () => import('@/pages/admin/UserManagePage.vue'),
    },
    {
      path: '/admin/appManage',
      name: '应用管理',
      component: () => import('@/pages/admin/AppManagePage.vue'),
    },
    {
      path: '/admin/userApps/:userId',
      name: '用户应用管理',
      component: () => import('@/pages/admin/UserAppsPage.vue'),
    },
    {
      path: '/admin/chatManage',
      name: '对话管理',
      component: () => import('@/pages/admin/ChatManagePage.vue'),
    },
    {
      path: '/app/chat/:id',
      name: '应用对话',
      component: () => import('@/pages/app/AppChatPage.vue'),
    },
    {
      path: '/app/edit/:id',
      name: '编辑应用',
      component: () => import('@/pages/app/AppEditPage.vue'),
    },
    {
      path: '/workflow',
      name: 'AI工作流',
      component: () => import('@/pages/workflow/WorkflowPage.vue'),
    },
    {
      path: '/vip',
      name: 'VIP中心',
      component: () => import('@/pages/VipCenterPage.vue'),
    },
    {
      path: '/demo',
      name: '样式演示',
      component: () => import('@/pages/StyleDemoPage.vue'),
    },
    {
      path: '/avatar',
      name: '头像展示',
      component: () => import('@/pages/AvatarShowcasePage.vue'),
    },
  ],
})

export default router
