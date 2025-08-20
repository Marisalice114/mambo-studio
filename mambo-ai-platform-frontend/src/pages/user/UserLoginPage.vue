<template>
  <div id="userLoginPage">
    <div class="login-container">
      <div class="login-card">
        <!-- Logo和标题 -->
        <div class="login-header">
          <div class="logo-section">
            <img src="@/assets/mambo-logo.svg" alt="Mambo Logo" class="login-logo" />
          </div>
          <h1 class="login-title">Mambo Studio</h1>
          <p class="login-subtitle">用户登录</p>
          <div class="login-desc">不写一行代码，生成完整应用</div>
        </div>

        <!-- 登录表单 -->
        <div class="login-form-section">
          <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
            <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
              <a-input 
                v-model:value="formState.userAccount" 
                placeholder="请输入账号" 
                size="large"
                class="login-input"
              >
                <template #prefix>
                  <UserOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>
            <a-form-item
              name="userPassword"
              :rules="[
                { required: true, message: '请输入密码' },
                { min: 8, message: '密码长度不能小于 8 位' },
              ]"
            >
              <a-input-password 
                v-model:value="formState.userPassword" 
                placeholder="请输入密码" 
                size="large"
                class="login-input"
              >
                <template #prefix>
                  <LockOutlined class="input-icon" />
                </template>
              </a-input-password>
            </a-form-item>
            
            <div class="login-tips">
              没有账号？
              <RouterLink to="/user/register" class="register-link">立即注册</RouterLink>
            </div>
            
            <a-form-item>
              <a-button type="primary" html-type="submit" size="large" class="login-btn">
                登录
              </a-button>
            </a-form-item>
          </a-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { reactive } from 'vue'
import { userLogin } from '@/api/userController'
import { useLoginUserStore } from '@/stores/loginUser'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const router = useRouter()
const loginUserStore = useLoginUserStore()

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: Record<string, any>) => {
  const res = await userLogin(values)
  // 登录成功，把登录态保存到全局状态中
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    router.push({
      path: '/',
      replace: true,
    })
  } else {
    message.error('登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
#userLoginPage {
  min-height: 100vh;
  background: linear-gradient(135deg, #FFFFFF 0%, #FFF5F8 30%, #FFE4E1 100%);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

/* 背景装饰 */
#userLoginPage::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(circle at 20% 20%, rgba(255, 105, 180, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(255, 182, 193, 0.12) 0%, transparent 50%),
    radial-gradient(circle at 40% 60%, rgba(255, 20, 147, 0.08) 0%, transparent 50%);
  animation: float 15s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

.login-container {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 420px;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(255, 105, 180, 0.15);
  border: 1px solid rgba(255, 182, 193, 0.2);
  transition: all 0.3s ease;
}

.login-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 30px 80px rgba(255, 105, 180, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-section {
  margin-bottom: 20px;
}

.login-logo {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  box-shadow: 0 8px 25px rgba(255, 105, 180, 0.2);
  transition: all 0.3s ease;
}

.login-logo:hover {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 12px 35px rgba(255, 105, 180, 0.3);
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-subtitle {
  font-size: 18px;
  font-weight: 500;
  margin: 0 0 8px 0;
  color: var(--text-primary, #333);
}

.login-desc {
  color: var(--text-secondary, #666);
  font-size: 14px;
  margin: 0;
}

.login-form-section {
  margin-top: 32px;
}

.login-input {
  border-radius: 16px !important;
  border: 2px solid rgba(255, 182, 193, 0.2) !important;
  height: 50px !important;
  transition: all 0.3s ease !important;
}

.login-input:focus,
.login-input:focus-within {
  border-color: #FF69B4 !important;
  box-shadow: 0 0 0 3px rgba(255, 105, 180, 0.1) !important;
}

.input-icon {
  color: var(--text-secondary, #666);
  transition: color 0.3s ease;
}

.login-input:focus .input-icon,
.login-input:focus-within .input-icon {
  color: #FF69B4;
}

.login-tips {
  text-align: center;
  color: var(--text-secondary, #666);
  font-size: 14px;
  margin: 16px 0 24px 0;
}

.register-link {
  color: #FF69B4 !important;
  text-decoration: none !important;
  font-weight: 500 !important;
  transition: all 0.3s ease !important;
}

.register-link:hover {
  color: #FF1493 !important;
  text-decoration: underline !important;
}

.login-btn {
  width: 100% !important;
  height: 50px !important;
  border-radius: 16px !important;
  background: linear-gradient(135deg, #FF69B4, #FF1493) !important;
  border: none !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  box-shadow: 0 8px 25px rgba(255, 105, 180, 0.3) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.login-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 12px 35px rgba(255, 105, 180, 0.4) !important;
}

.login-btn:active {
  transform: translateY(0) !important;
}

/* 表单项样式 */
:deep(.ant-form-item) {
  margin-bottom: 20px;
}

:deep(.ant-form-item-label) {
  font-weight: 500;
  color: var(--text-primary, #333);
}

:deep(.ant-input-affix-wrapper) {
  border-radius: 16px !important;
  border: 2px solid rgba(255, 182, 193, 0.2) !important;
  height: 50px !important;
  transition: all 0.3s ease !important;
}

:deep(.ant-input-affix-wrapper:focus),
:deep(.ant-input-affix-wrapper-focused) {
  border-color: #FF69B4 !important;
  box-shadow: 0 0 0 3px rgba(255, 105, 180, 0.1) !important;
}

:deep(.ant-input-affix-wrapper .ant-input) {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-card {
    padding: 24px;
    margin: 16px;
  }
  
  .login-title {
    font-size: 24px;
  }
  
  .login-subtitle {
    font-size: 16px;
  }
}
</style>
