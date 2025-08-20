import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '@/pages/HomePage.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserProfilePage from '@/pages/user/UserProfilePage.vue'
import UserSettingsPage from '@/pages/user/UserSettingsPage.vue'
import UserAppManagePage from '@/pages/user/UserAppManagePage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'
import AppManagePage from '@/pages/admin/AppManagePage.vue'
import AppChatPage from '@/pages/app/AppChatPage.vue'
import AppEditPage from '@/pages/app/AppEditPage.vue'
import ChatManagePage from "@/pages/admin/ChatManagePage.vue";
import WorkflowPage from "@/pages/workflow/WorkflowPage.vue";
import VipCenterPage from "@/pages/VipCenterPage.vue";
import StyleDemoPage from "@/pages/StyleDemoPage.vue";
import AvatarShowcasePage from "@/pages/AvatarShowcasePage.vue";

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
      component: UserLoginPage,
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: UserRegisterPage,
    },
    {
      path: '/user/profile',
      name: '个人信息',
      component: UserProfilePage,
    },
    {
      path: '/user/settings',
      name: '账户设置',
      component: UserSettingsPage,
    },
    {
      path: '/user/apps',
      name: '我的应用',
      component: UserAppManagePage,
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: UserManagePage,
    },
    {
      path: '/admin/appManage',
      name: '应用管理',
      component: AppManagePage,
    },
    {
      path: '/admin/userApps/:userId',
      name: '用户应用管理',
      component: () => import('@/pages/admin/UserAppsPage.vue'),
    },
    {
      path: '/admin/chatManage',
      name: '对话管理',
      component: ChatManagePage,
    },
    {
      path: '/app/chat/:id',
      name: '应用对话',
      component: AppChatPage,
    },
    {
      path: '/app/edit/:id',
      name: '编辑应用',
      component: AppEditPage,
    },
    {
      path: '/workflow',
      name: 'AI工作流',
      component: WorkflowPage,
    },
    {
      path: '/vip',
      name: 'VIP中心',
      component: VipCenterPage,
    },
    {
      path: '/demo',
      name: '样式演示',
      component: StyleDemoPage,
    },
    {
      path: '/avatar',
      name: '头像展示',
      component: AvatarShowcasePage,
    },
  ],
})

export default router
