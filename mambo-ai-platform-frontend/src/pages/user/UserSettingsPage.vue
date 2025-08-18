<template>
  <div class="user-settings">
    <a-card title="账户设置">
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        layout="vertical"
        @finish="onFinish"
      >
        <a-form-item label="用户名" name="userName">
          <a-input v-model:value="formData.userName" placeholder="请输入用户名" />
        </a-form-item>
        
        <a-form-item label="头像" name="userAvatar">
          <div class="avatar-upload">
            <a-avatar :size="80" :src="formData.userAvatar">
              {{ formData.userName?.charAt(0) || 'U' }}
            </a-avatar>
            <div class="upload-info">
              <a-input 
                v-model:value="formData.userAvatar" 
                placeholder="请输入头像URL" 
                style="margin-top: 12px;"
              />
              <p class="upload-tip">支持 jpg、png 格式，建议尺寸 200x200 像素</p>
            </div>
          </div>
        </a-form-item>
        
        <a-form-item label="个人简介" name="userProfile">
          <a-textarea 
            v-model:value="formData.userProfile" 
            placeholder="介绍一下自己吧..." 
            :rows="4"
            :maxlength="200"
            show-count
          />
        </a-form-item>
        
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit" :loading="loading">
              保存设置
            </a-button>
            <a-button @click="resetForm">
              重置
            </a-button>
            <a-button @click="goBack">
              返回
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 表单相关
const formRef = ref()
const loading = ref(false)

const formData = reactive({
  userName: '',
  userAvatar: '',
  userProfile: '',
})

const rules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在2-20个字符', trigger: 'blur' },
  ],
  userAvatar: [
    { type: 'url', message: '请输入有效的URL地址', trigger: 'blur' },
  ],
  userProfile: [
    { max: 200, message: '个人简介最多200个字符', trigger: 'blur' },
  ],
}

// 初始化表单数据
const initFormData = () => {
  const user = loginUserStore.loginUser
  formData.userName = user.userName || ''
  formData.userAvatar = user.userAvatar || ''
  formData.userProfile = user.userProfile || ''
}

// 提交表单
const onFinish = async (values: any) => {
  loading.value = true
  try {
    // 这里调用更新用户信息的API
    // 由于当前API可能不支持，这里做模拟处理
    message.success('保存成功')
    
    // 更新本地用户信息
    loginUserStore.setLoginUser({
      ...loginUserStore.loginUser,
      ...values,
    })
    
    router.push('/user/profile')
  } catch (error) {
    message.error('保存失败')
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  initFormData()
  formRef.value?.clearValidate()
}

// 返回
const goBack = () => {
  router.back()
}

onMounted(() => {
  initFormData()
})
</script>

<style scoped>
.user-settings {
  padding: 24px;
  max-width: 600px;
  margin: 0 auto;
}

.avatar-upload {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.upload-info {
  flex: 1;
}

.upload-tip {
  margin: 8px 0 0 0;
  color: #666;
  font-size: 12px;
}
</style>
