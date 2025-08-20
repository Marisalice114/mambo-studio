# VIP 用户样式功能说明

## 实现的功能

### 1. VIP 用户名显示效果

- **普通用户名**：黑色文字 `#333`
- **VIP 用户名**：金色渐变文字，带有文字阴影效果
  - 颜色：金色渐变 (`#d4af37` 到 `#ffd700`)
  - 字重：600（加粗）
  - 特效：文字阴影和渐变填充

### 2. VIP 用户头像框效果

- **普通用户头像**：无特殊边框
- **VIP 用户头像**：
  - 金色边框 `2px solid #ffd700`
  - 金色发光阴影效果
  - 旋转金色光圈动画效果
  - 渐变色光环：金黄色渐变旋转

### 3. VIP 标识和提醒

- VIP 标签：金色渐变背景
- VIP 到期时间提醒图标
- 特殊的 VIP 菜单项样式

## 代码实现位置

### 1. GlobalHeader.vue

- 主要的用户名和头像显示区域
- VIP 状态判断逻辑
- VIP 相关的 CSS 样式

### 2. UserInfo.vue

- 通用的用户信息组件
- 支持在其他页面复用 VIP 样式
- 可选的迷你 VIP 标识

## 临时测试方法

由于后端 API 暂时不支持 VIP 字段，当前使用以下逻辑进行测试：

```typescript
// 在GlobalHeader.vue和UserInfo.vue中
const isVipUser = computed(() => {
  const user = loginUserStore.loginUser
  // 如果用户角色是admin或者用户名包含"vip"，则显示为VIP用户
  return (
    user &&
    (user.userRole === 'admin' ||
      (user.userName && user.userName.toLowerCase().includes('vip')))
  )
})
```

### 测试方法：

1. **管理员用户**：登录管理员账号，会自动显示 VIP 样式
2. **包含 VIP 的用户名**：创建或修改用户名包含"vip"的用户（如"testVip"、"vipUser"等）

## CSS 样式详解

### VIP 用户名样式

```css
.vip-user-name {
  color: #d4af37;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(212, 175, 55, 0.3);
  background: linear-gradient(45deg, #d4af37, #ffd700);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
```

### VIP 头像样式

```css
.vip-avatar {
  border: 2px solid #ffd700;
  box-shadow: 0 0 8px rgba(255, 215, 0, 0.4);
  position: relative;
}

.vip-avatar::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, #ffd700, #ffed4e, #ffa500, #ffd700);
  border-radius: 50%;
  z-index: -1;
  animation: vip-glow 2s linear infinite;
}

@keyframes vip-glow {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
```

## 后端 API 集成准备

当后端 API 支持 VIP 功能时，需要修改以下部分：

### 1. 用户数据结构

```typescript
interface User {
  id: number
  userName: string
  userAvatar: string
  userRole: string
  isVip: boolean // 新增：是否为VIP用户
  vipExpireTime: string // 新增：VIP到期时间
  // ... 其他字段
}
```

### 2. VIP 状态判断逻辑

```typescript
const isVipUser = computed(() => {
  const user = loginUserStore.loginUser
  return (
    user &&
    user.isVip &&
    user.vipExpireTime &&
    dayjs(user.vipExpireTime).isAfter(dayjs())
  )
})
```

## 视觉效果对比

| 用户类型 | 用户名颜色 | 字体粗细 | 头像边框 | 特殊效果 |
| -------- | ---------- | -------- | -------- | -------- |
| 普通用户 | #333       | 500      | 无       | 无       |
| VIP 用户 | 金色渐变   | 600      | 金色发光 | 旋转光圈 |

## 组件使用示例

### GlobalHeader 中的使用

```vue
<a-avatar
  :src="loginUserStore.loginUser.userAvatar"
  :class="{ 'vip-avatar': isVipUser }"
/>
<span class="user-name" :class="{ 'vip-user-name': isVipUser }">
  {{ loginUserStore.loginUser.userName ?? '无名' }}
</span>
```

### UserInfo 组件中的使用

```vue
<UserInfo :user="userInfo" :showVipBadge="true" size="default" />
```

这样的设计确保了 VIP 用户在界面上有明显的视觉区分，提升了用户的尊贵感和平台的商业价值。
