<template>
  <div class="vip-center">
    <!-- VIP状态卡片 -->
    <a-card class="vip-status-card">
      <template #title>
        <div class="vip-title">
          <CrownOutlined class="vip-icon" />
          <span>VIP会员中心</span>
        </div>
      </template>

      <div v-if="isVipUser" class="vip-active">
        <a-row :gutter="24">
          <a-col :span="12">
            <div class="vip-info">
              <h3>VIP等级</h3>
              <div class="vip-level">
                <CrownOutlined style="color: #ffd700; font-size: 24px;" />
                <span class="level-text">至尊VIP</span>
              </div>
            </div>
          </a-col>
          <a-col :span="12">
            <div class="vip-info">
              <h3>到期时间</h3>
              <div class="expire-time" :class="expireClass">
                <ClockCircleOutlined />
                <span>{{ formatVipExpireTime }}</span>
              </div>
              <div v-if="isExpiringSoon" class="expire-warning">
                <ExclamationCircleOutlined />
                <span>您的VIP即将到期，请及时续费</span>
              </div>
            </div>
          </a-col>
        </a-row>
      </div>

      <div v-else class="vip-inactive">
        <a-empty description="您还不是VIP用户">
          <a-button type="primary" size="large" @click="openUpgradeModal">
            <CrownOutlined />
            立即开通VIP
          </a-button>
        </a-empty>
      </div>
    </a-card>

    <!-- VIP特权介绍 -->
    <a-card title="VIP专属特权" class="vip-privileges">
      <a-row :gutter="24">
        <a-col :span="8" v-for="privilege in vipPrivileges" :key="privilege.id">
          <div class="privilege-item">
            <div class="privilege-icon">
              <component :is="privilege.icon" :style="{ color: privilege.color, fontSize: '32px' }" />
            </div>
            <h4>{{ privilege.title }}</h4>
            <p>{{ privilege.description }}</p>
            <a-tag v-if="isVipUser" color="success">已享受</a-tag>
            <a-tag v-else color="default">需要VIP</a-tag>
          </div>
        </a-col>
      </a-row>
    </a-card>

    <!-- VIP专属应用 -->
    <a-card title="VIP专属应用" class="vip-apps">
      <div v-if="isVipUser">
        <div v-if="vipApps.length > 0" class="apps-grid">
          <div
            v-for="app in vipApps"
            :key="app.id"
            class="app-card"
            @click="handleViewChat(app.id)"
          >
            <div class="app-cover">
              <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
              <div v-else class="no-cover">{{ app.appName?.charAt(0) || 'A' }}</div>
            </div>
            <div class="app-info">
              <h4>{{ app.appName }}</h4>
              <p>{{ app.appDesc }}</p>
              <a-tag color="gold">VIP专属</a-tag>
            </div>
          </div>
        </div>
        <a-empty v-else description="暂无VIP专属应用" />
      </div>
      <div v-else class="vip-locked">
        <a-empty description="开通VIP后可享受专属应用">
          <a-button type="primary" @click="openUpgradeModal">
            立即开通VIP
          </a-button>
        </a-empty>
      </div>
    </a-card>

    <!-- 升级VIP模态框 -->
    <a-modal
      v-model:open="upgradeModalVisible"
      title="升级VIP会员"
      :footer="null"
      width="600px"
    >
      <div class="upgrade-content">
        <div class="upgrade-header">
          <CrownOutlined class="upgrade-icon" />
          <h2>成为VIP会员，解锁更多特权</h2>
        </div>

        <div class="pricing-plans">
          <div class="plan-item" v-for="plan in pricingPlans" :key="plan.id">
            <div class="plan-header">
              <h3>{{ plan.name }}</h3>
              <div class="plan-price">
                <span class="currency">¥</span>
                <span class="amount">{{ plan.price }}</span>
                <span class="period">/{{ plan.period }}</span>
              </div>
            </div>
            <ul class="plan-features">
              <li v-for="feature in plan.features" :key="feature">
                <CheckOutlined style="color: #52c41a;" />
                {{ feature }}
              </li>
            </ul>
            <a-button type="primary" size="large" block @click="purchaseVip(plan)">
              选择此套餐
            </a-button>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  CrownOutlined,
  ClockCircleOutlined,
  ExclamationCircleOutlined,
  CheckOutlined,
  ThunderboltOutlined,
  StarOutlined,
  SafetyOutlined
} from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { listMyAppVoByPage } from '@/api/appController'
import dayjs from 'dayjs'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 响应式数据
const upgradeModalVisible = ref(false)
const vipApps = ref<any[]>([])

// VIP状态计算
const isVipUser = computed(() => {
  const user = loginUserStore.loginUser
  return user && user.isVip && user.vipExpireTime && dayjs(user.vipExpireTime).isAfter(dayjs())
})

const formatVipExpireTime = computed(() => {
  if (loginUserStore.loginUser.vipExpireTime) {
    return dayjs(loginUserStore.loginUser.vipExpireTime).format('YYYY年MM月DD日 HH:mm')
  }
  return ''
})

const isExpiringSoon = computed(() => {
  if (!loginUserStore.loginUser.vipExpireTime) return false
  const expire = dayjs(loginUserStore.loginUser.vipExpireTime)
  const now = dayjs()
  const diffDays = expire.diff(now, 'day')
  return diffDays <= 7 && diffDays >= 0
})

const expireClass = computed(() => {
  if (!loginUserStore.loginUser.vipExpireTime) return ''
  const expire = dayjs(loginUserStore.loginUser.vipExpireTime)
  const now = dayjs()

  if (expire.isBefore(now)) {
    return 'expired'
  } else if (isExpiringSoon.value) {
    return 'expiring-soon'
  } else {
    return 'valid'
  }
})

// VIP特权列表
const vipPrivileges = [
  {
    id: 1,
    icon: ThunderboltOutlined,
    color: '#ffd700',
    title: '无限次生成',
    description: '享受无限次AI代码生成服务，释放创作潜能'
  },
  {
    id: 2,
    icon: StarOutlined,
    color: '#ff4d4f',
    title: '专属应用',
    description: '访问VIP专属应用库，体验高级功能'
  },
  {
    id: 3,
    icon: SafetyOutlined,
    color: '#52c41a',
    title: '优先支持',
    description: '享受24/7优先客服支持，快速解决问题'
  }
]

// 价格套餐
const pricingPlans = [
  {
    id: 1,
    name: '月度VIP',
    price: 29,
    period: '月',
    features: ['无限次代码生成', 'VIP专属应用', '优先客服支持', '历史记录保存']
  },
  {
    id: 2,
    name: '年度VIP',
    price: 299,
    period: '年',
    features: ['无限次代码生成', 'VIP专属应用', '优先客服支持', '历史记录保存', '专属定制服务']
  }
]

// 方法
const openUpgradeModal = () => {
  upgradeModalVisible.value = true
}

const purchaseVip = (plan: any) => {
  message.info(`即将跳转到支付页面购买${plan.name}`)
  // 这里可以集成支付系统
  upgradeModalVisible.value = false
}

const handleViewChat = (appId: string | number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}`)
  }
}

// 获取VIP专属应用
const fetchVipApps = async () => {
  if (!isVipUser.value) return

  try {
    const res = await listMyAppVoByPage({
      pageNum: 1,
      pageSize: 20,
      // isVipOnly: true // 假设后端支持VIP专属应用过滤
    })
    if (res.data.code === 0 && res.data.data?.records) {
      vipApps.value = res.data.data.records
    }
  } catch (error) {
    console.error('获取VIP应用失败:', error)
  }
}

onMounted(() => {
  fetchVipApps()
})
</script>

<style scoped>
.vip-center {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.vip-status-card {
  margin-bottom: 24px;
  background: linear-gradient(135deg, #fff9e6, #fffbf0);
  border: 2px solid #ffd700;
}

.vip-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #8b4513;
  font-weight: bold;
}

.vip-icon {
  color: #ffd700;
  font-size: 24px;
}

.vip-active {
  padding: 20px 0;
}

.vip-info h3 {
  margin-bottom: 16px;
  color: #8b4513;
  font-size: 16px;
}

.vip-level {
  display: flex;
  align-items: center;
  gap: 8px;
}

.level-text {
  font-size: 18px;
  font-weight: bold;
  color: #8b4513;
}

.expire-time {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  margin-bottom: 8px;
}

.expire-time.valid {
  color: #52c41a;
}

.expire-time.expiring-soon {
  color: #fa8c16;
}

.expire-time.expired {
  color: #ff4d4f;
}

.expire-warning {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fa8c16;
  font-size: 14px;
}

.vip-inactive {
  padding: 40px 0;
  text-align: center;
}

.vip-privileges,
.vip-apps {
  margin-bottom: 24px;
}

.privilege-item {
  text-align: center;
  padding: 24px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  height: 100%;
}

.privilege-icon {
  margin-bottom: 16px;
}

.privilege-item h4 {
  margin-bottom: 12px;
  color: #1a1a1a;
  font-size: 16px;
}

.privilege-item p {
  color: #666;
  margin-bottom: 16px;
  line-height: 1.5;
}

.apps-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.app-card {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.app-card:hover {
  border-color: #ffd700;
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.3);
  transform: translateY(-2px);
}

.app-cover {
  width: 100%;
  height: 150px;
  margin-bottom: 12px;
  border-radius: 6px;
  overflow: hidden;
}

.app-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-cover {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: bold;
  color: #8b4513;
}

.app-info h4 {
  margin-bottom: 8px;
  color: #1a1a1a;
  font-size: 16px;
}

.app-info p {
  color: #666;
  margin-bottom: 12px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.vip-locked {
  padding: 40px 0;
  text-align: center;
}

.upgrade-content {
  text-align: center;
}

.upgrade-header {
  margin-bottom: 32px;
}

.upgrade-icon {
  color: #ffd700;
  font-size: 48px;
  margin-bottom: 16px;
}

.upgrade-header h2 {
  color: #8b4513;
  margin: 0;
  font-size: 24px;
}

.pricing-plans {
  display: flex;
  gap: 24px;
  justify-content: center;
}

.plan-item {
  flex: 1;
  border: 2px solid #f0f0f0;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  transition: all 0.3s;
}

.plan-item:hover {
  border-color: #ffd700;
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(255, 215, 0, 0.3);
}

.plan-header h3 {
  margin-bottom: 16px;
  color: #1a1a1a;
  font-size: 20px;
}

.plan-price {
  margin-bottom: 24px;
}

.currency {
  font-size: 16px;
  color: #666;
}

.amount {
  font-size: 32px;
  font-weight: bold;
  color: #ffd700;
  margin: 0 4px;
}

.period {
  font-size: 14px;
  color: #666;
}

.plan-features {
  list-style: none;
  padding: 0;
  margin: 0 0 24px 0;
  text-align: left;
}

.plan-features li {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: #666;
}
</style>
