<template>
  <div id="userRegisterPage">
    <div class="register-container">
      <div class="register-card">
        <!-- Logo和标题 -->
        <div class="register-header">
          <div class="logo-section">
            <img src="@/assets/mambo-logo.svg" alt="Mambo Logo" class="register-logo" />
          </div>
          <h1 class="register-title">Mambo AI 应用生成</h1>
          <p class="register-subtitle">用户注册</p>
          <div class="register-desc">不写一行代码，生成完整应用</div>
        </div>

        <!-- 注册表单 -->
        <div class="register-form-section">
          <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
            <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
              <a-input 
                v-model:value="formState.userAccount" 
                placeholder="请输入账号" 
                size="large"
                class="register-input"
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
                { min: 8, message: '密码不能小于 8 位' },
              ]"
            >
              <a-input-password 
                v-model:value="formState.userPassword" 
                placeholder="请输入密码" 
                size="large"
                class="register-input"
              >
                <template #prefix>
                  <LockOutlined class="input-icon" />
                </template>
              </a-input-password>
            </a-form-item>
            <a-form-item
              name="checkPassword"
              :rules="[
                { required: true, message: '请确认密码' },
                { min: 8, message: '密码不能小于 8 位' },
                { validator: validateCheckPassword },
              ]"
            >
              <a-input-password 
                v-model:value="formState.checkPassword" 
                placeholder="请确认密码" 
                size="large"
                class="register-input"
              >
                <template #prefix>
                  <SafetyCertificateOutlined class="input-icon" />
                </template>
              </a-input-password>
            </a-form-item>
            
            <div class="register-tips">
              已有账号？
              <RouterLink to="/user/login" class="login-link">立即登录</RouterLink>
            </div>
            
            <a-form-item>
              <a-button type="primary" html-type="submit" size="large" class="register-btn">
                注册
              </a-button>
            </a-form-item>
          </a-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { userRegister } from '@/api/userController'
import { message } from 'ant-design-vue'
import { reactive } from 'vue'
import { UserOutlined, LockOutlined, SafetyCertificateOutlined } from '@ant-design/icons-vue'

const router = useRouter()

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

/**
 * 验证确认密码
 * @param rule
 * @param value
 * @param callback
 */
const validateCheckPassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value && value !== formState.userPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: API.UserRegisterRequest) => {
  const res = await userRegister(values)
  // 注册成功，跳转到登录页面
  if (res.data.code === 0) {
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败，' + res.data.message)
  }
}
</script>

<style scoped>
#userRegisterPage {
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
#userRegisterPage::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(circle at 80% 20%, rgba(255, 105, 180, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 20% 80%, rgba(255, 182, 193, 0.12) 0%, transparent 50%),
    radial-gradient(circle at 60% 40%, rgba(255, 20, 147, 0.08) 0%, transparent 50%);
  animation: float 15s ease-in-out infinite reverse;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

.register-container {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 420px;
}

.register-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(255, 105, 180, 0.15);
  border: 1px solid rgba(255, 182, 193, 0.2);
  transition: all 0.3s ease;
}

.register-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 30px 80px rgba(255, 105, 180, 0.2);
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-section {
  margin-bottom: 20px;
}

.register-logo {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  box-shadow: 0 8px 25px rgba(255, 105, 180, 0.2);
  transition: all 0.3s ease;
}

.register-logo:hover {
  transform: scale(1.1) rotate(-5deg);
  box-shadow: 0 12px 35px rgba(255, 105, 180, 0.3);
}

.register-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.register-subtitle {
  font-size: 18px;
  font-weight: 500;
  margin: 0 0 8px 0;
  color: var(--text-primary, #333);
}

.register-desc {
  color: var(--text-secondary, #666);
  font-size: 14px;
  margin: 0;
}

.register-form-section {
  margin-top: 32px;
}

.register-input {
  border-radius: 16px !important;
  border: 2px solid rgba(255, 182, 193, 0.2) !important;
  height: 50px !important;
  transition: all 0.3s ease !important;
}

.register-input:focus,
.register-input:focus-within {
  border-color: #FF69B4 !important;
  box-shadow: 0 0 0 3px rgba(255, 105, 180, 0.1) !important;
}

.input-icon {
  color: var(--text-secondary, #666);
  transition: color 0.3s ease;
}

.register-input:focus .input-icon,
.register-input:focus-within .input-icon {
  color: #FF69B4;
}

.register-tips {
  text-align: center;
  color: var(--text-secondary, #666);
  font-size: 14px;
  margin: 16px 0 24px 0;
}

.login-link {
  color: #FF69B4 !important;
  text-decoration: none !important;
  font-weight: 500 !important;
  transition: all 0.3s ease !important;
}

.login-link:hover {
  color: #FF1493 !important;
  text-decoration: underline !important;
}

.register-btn {
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

.register-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 12px 35px rgba(255, 105, 180, 0.4) !important;
}

.register-btn:active {
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
  .register-card {
    padding: 24px;
    margin: 16px;
  }
  
  .register-title {
    font-size: 24px;
  }
  
  .register-subtitle {
    font-size: 16px;
  }
}
</style>
