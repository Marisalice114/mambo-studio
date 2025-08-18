<template>
  <div class="workflow-page">
    <a-card title="🚀 AI 工作流执行" class="workflow-card">
      <!-- 输入区域 -->
      <div class="input-section">
        <a-form layout="vertical">
          <a-form-item label="项目描述">
            <a-textarea 
              v-model:value="prompt" 
              placeholder="请描述您想要创建的项目，例如：创建一个在线教育平台，包含课程展示和学习进度跟踪功能"
              :rows="4"
              :max-length="1000"
              show-count
            />
          </a-form-item>
          
          <a-form-item>
            <a-space>
              <a-button 
                type="primary" 
                size="large"
                :loading="isExecuting"
                @click="startWorkflow"
                :disabled="!prompt.trim()"
              >
                <template #icon>
                  <PlayCircleOutlined v-if="!isExecuting" />
                  <LoadingOutlined v-else />
                </template>
                {{ isExecuting ? '执行中...' : '开始执行工作流' }}
              </a-button>
              
              <a-button 
                @click="clearLogs"
                :disabled="isExecuting"
              >
                <template #icon>
                  <ClearOutlined />
                </template>
                清空日志
              </a-button>
              
              <a-button 
                v-if="isExecuting"
                danger
                @click="stopWorkflow"
              >
                <template #icon>
                  <StopOutlined />
                </template>
                停止执行
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </div>

      <!-- 状态显示 -->
      <div class="status-section">
        <a-tag 
          :color="getStatusColor()" 
          class="status-tag"
        >
          {{ getStatusText() }}
        </a-tag>
        <span v-if="currentStep" class="current-step">
          当前步骤: {{ currentStep }}
        </span>
      </div>

      <!-- 工作流步骤进度 -->
      <div class="progress-section" v-if="workflowSteps.length > 0">
        <a-steps 
          :current="currentStepIndex" 
          :status="stepStatus"
          size="small"
          class="workflow-steps"
        >
          <a-step 
            v-for="(step, index) in workflowSteps" 
            :key="index"
            :title="step.title"
            :description="step.description"
          />
        </a-steps>
      </div>

      <!-- 日志输出区域 -->
      <div class="logs-section">
        <div class="logs-header">
          <h3>执行日志</h3>
          <a-switch 
            v-model:checked="autoScroll" 
            checked-children="自动滚动" 
            un-checked-children="自动滚动"
            size="small"
          />
        </div>
        
        <div class="logs-container" ref="logsContainer">
          <div 
            v-for="(log, index) in logs" 
            :key="index"
            :class="['log-item', `log-${log.type}`]"
          >
            <div class="log-header">
              <span class="log-type">{{ getLogIcon(log.type) }} {{ log.type }}</span>
              <span class="log-time">{{ log.time }}</span>
            </div>
            <div class="log-content">{{ log.message }}</div>
            <div v-if="log.data" class="log-data">
              <pre>{{ JSON.stringify(log.data, null, 2) }}</pre>
            </div>
          </div>
          
          <div v-if="logs.length === 0" class="empty-logs">
            <a-empty description="暂无执行日志" />
          </div>
        </div>
      </div>

      <!-- 结果展示区域 -->
      <div v-if="workflowResult" class="result-section">
        <a-card title="🎉 执行结果" size="small">
          <div class="result-info">
            <a-descriptions :column="2" bordered size="small">
              <a-descriptions-item label="生成类型">
                {{ workflowResult.generationType }}
              </a-descriptions-item>
              <a-descriptions-item label="生成目录">
                {{ workflowResult.generatedCodeDir }}
              </a-descriptions-item>
              <a-descriptions-item label="构建目录" v-if="workflowResult.buildResultDir">
                {{ workflowResult.buildResultDir }}
              </a-descriptions-item>
              <a-descriptions-item label="执行时间">
                {{ workflowResult.executionTime }}
              </a-descriptions-item>
            </a-descriptions>
          </div>
          
          <div class="result-actions" style="margin-top: 16px;">
            <a-space>
              <a-button type="primary" @click="viewResult">
                <template #icon>
                  <EyeOutlined />
                </template>
                查看结果
              </a-button>
              
              <a-button @click="downloadResult">
                <template #icon>
                  <DownloadOutlined />
                </template>
                下载项目
              </a-button>
            </a-space>
          </div>
        </a-card>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, nextTick, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import { 
  PlayCircleOutlined, 
  LoadingOutlined, 
  ClearOutlined, 
  StopOutlined,
  EyeOutlined,
  DownloadOutlined
} from '@ant-design/icons-vue'

// 响应式数据
const prompt = ref('创建一个在线教育平台，包含课程展示和学习进度跟踪功能')
const isExecuting = ref(false)
const logs = ref<LogItem[]>([])
const currentStep = ref('')
const currentStepIndex = ref(-1)
const stepStatus = ref<'wait' | 'process' | 'finish' | 'error'>('wait')
const autoScroll = ref(true)
const workflowResult = ref<any>(null)
const logsContainer = ref<HTMLElement>()

// 工作流步骤定义
const workflowSteps = reactive([
  { title: '图片收集', description: '收集相关图片资源' },
  { title: '提示词增强', description: '优化和增强用户输入' },
  { title: '智能路由', description: '选择合适的生成策略' },
  { title: '代码生成', description: '生成项目代码' },
  { title: '质量检查', description: '检查代码质量' },
  { title: '项目构建', description: '构建完整项目' }
])

// SSE 连接
let eventSource: EventSource | null = null

// 类型定义
interface LogItem {
  type: 'system' | 'workflow_start' | 'step_completed' | 'workflow_completed' | 'workflow_error' | 'connection'
  message: string
  time: string
  data?: any
}

// 获取状态颜色
const getStatusColor = () => {
  if (isExecuting.value) return 'processing'
  if (workflowResult.value) return 'success'
  if (logs.value.some(log => log.type === 'workflow_error')) return 'error'
  return 'default'
}

// 获取状态文本
const getStatusText = () => {
  if (isExecuting.value) return '执行中'
  if (workflowResult.value) return '执行完成'
  if (logs.value.some(log => log.type === 'workflow_error')) return '执行失败'
  return '待执行'
}

// 获取日志图标
const getLogIcon = (type: string) => {
  const icons: Record<string, string> = {
    'system': '🔧',
    'workflow_start': '🚀',
    'step_completed': '✅',
    'workflow_completed': '🎉',
    'workflow_error': '❌',
    'connection': '🔗'
  }
  return icons[type] || '📝'
}

// 添加日志
const addLog = (type: LogItem['type'], message: string, data?: any) => {
  logs.value.push({
    type,
    message,
    time: new Date().toLocaleTimeString(),
    data
  })
  
  if (autoScroll.value) {
    nextTick(() => {
      if (logsContainer.value) {
        logsContainer.value.scrollTop = logsContainer.value.scrollHeight
      }
    })
  }
}

// 开始工作流
const startWorkflow = async () => {
  if (!prompt.value.trim()) {
    message.warning('请输入项目描述')
    return
  }

  isExecuting.value = true
  currentStep.value = ''
  currentStepIndex.value = -1
  stepStatus.value = 'process'
  workflowResult.value = null

  addLog('system', `开始执行工作流: ${prompt.value}`)

  try {
    // 建立 SSE 连接
    const url = `/api/workflow/execute-sse?prompt=${encodeURIComponent(prompt.value)}`
    eventSource = new EventSource(url)

    eventSource.onopen = () => {
      addLog('connection', 'SSE 连接已建立')
    }

    eventSource.addEventListener('workflow_start', (event) => {
      const data = JSON.parse(event.data)
      addLog('workflow_start', '工作流开始执行', data)
      currentStepIndex.value = 0
    })

    eventSource.addEventListener('step_completed', (event) => {
      const data = JSON.parse(event.data)
      addLog('step_completed', `步骤 ${data.stepNumber} 完成: ${data.currentStep}`, data)
      currentStep.value = data.currentStep
      currentStepIndex.value = data.stepNumber - 1
    })

    eventSource.addEventListener('workflow_completed', (event) => {
      const data = JSON.parse(event.data)
      addLog('workflow_completed', '工作流执行完成', data)
      workflowResult.value = data
      currentStepIndex.value = workflowSteps.length - 1
      stepStatus.value = 'finish'
      isExecuting.value = false
      eventSource?.close()
      eventSource = null
      message.success('工作流执行完成！')
    })

    eventSource.addEventListener('workflow_error', (event) => {
      const data = JSON.parse(event.data)
      addLog('workflow_error', `工作流执行失败: ${data.error}`, data)
      stepStatus.value = 'error'
      isExecuting.value = false
      eventSource?.close()
      eventSource = null
      message.error('工作流执行失败')
    })

    eventSource.onerror = () => {
      addLog('workflow_error', '连接错误或中断')
      isExecuting.value = false
      stepStatus.value = 'error'
      eventSource?.close()
      eventSource = null
      message.error('连接错误')
    }

  } catch (error) {
    console.error('启动工作流失败:', error)
    addLog('workflow_error', `启动失败: ${error}`)
    isExecuting.value = false
    stepStatus.value = 'error'
    message.error('启动工作流失败')
  }
}

// 停止工作流
const stopWorkflow = () => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
  isExecuting.value = false
  stepStatus.value = 'error'
  addLog('system', '用户手动停止工作流执行')
  message.info('已停止工作流执行')
}

// 清空日志
const clearLogs = () => {
  logs.value = []
  workflowResult.value = null
  currentStep.value = ''
  currentStepIndex.value = -1
  stepStatus.value = 'wait'
  addLog('system', '日志已清空')
}

// 查看结果
const viewResult = () => {
  if (workflowResult.value?.generatedCodeDir) {
    // 这里可以添加预览逻辑
    message.info('预览功能开发中...')
  }
}

// 下载结果
const downloadResult = () => {
  if (workflowResult.value?.buildResultDir) {
    // 这里可以添加下载逻辑
    message.info('下载功能开发中...')
  }
}

// 组件卸载时清理
onUnmounted(() => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
})
</script>

<style scoped>
.workflow-page {
  padding: 24px;
  background: #f5f5f5;
  min-height: 100vh;
}

.workflow-card {
  max-width: 1200px;
  margin: 0 auto;
}

.input-section {
  margin-bottom: 24px;
}

.status-section {
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-tag {
  font-size: 14px;
  padding: 4px 12px;
}

.current-step {
  font-weight: 500;
  color: #1890ff;
}

.progress-section {
  margin-bottom: 24px;
}

.workflow-steps {
  margin: 16px 0;
}

.logs-section {
  margin-bottom: 24px;
}

.logs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.logs-header h3 {
  margin: 0;
}

.logs-container {
  background: #fafafa;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  padding: 16px;
  max-height: 400px;
  overflow-y: auto;
}

.log-item {
  margin-bottom: 12px;
  padding: 12px;
  background: white;
  border-radius: 4px;
  border-left: 4px solid #d9d9d9;
}

.log-item.log-workflow_start {
  border-left-color: #1890ff;
}

.log-item.log-step_completed {
  border-left-color: #52c41a;
}

.log-item.log-workflow_completed {
  border-left-color: #722ed1;
}

.log-item.log-workflow_error {
  border-left-color: #ff4d4f;
}

.log-item.log-connection {
  border-left-color: #faad14;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.log-type {
  font-weight: 500;
  color: #1890ff;
}

.log-time {
  font-size: 12px;
  color: #8c8c8c;
}

.log-content {
  margin-bottom: 8px;
  line-height: 1.5;
}

.log-data {
  background: #f6f6f6;
  padding: 8px;
  border-radius: 4px;
  font-size: 12px;
}

.log-data pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

.empty-logs {
  text-align: center;
  padding: 40px 0;
}

.result-section {
  margin-top: 24px;
}

.result-info {
  margin-bottom: 16px;
}

.result-actions {
  text-align: center;
}
</style>
