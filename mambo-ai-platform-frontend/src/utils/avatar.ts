/**
 * 生成用户默认头像SVG
 * @param name 用户名
 * @param size 头像尺寸，默认100
 * @returns SVG字符串的Data URL
 */
export function generateUserAvatar(name: string = '用户', size: number = 100): string {
  const firstChar = name.charAt(0).toUpperCase() || 'U'
  
  // 根据用户名生成颜色
  const colors = [
    { bg: '#FF6B6B', text: '#FFFFFF' }, // 红色系
    { bg: '#4ECDC4', text: '#FFFFFF' }, // 青色系
    { bg: '#45B7D1', text: '#FFFFFF' }, // 蓝色系
    { bg: '#96CEB4', text: '#FFFFFF' }, // 绿色系
    { bg: '#FECA57', text: '#FFFFFF' }, // 黄色系
    { bg: '#FF9FF3', text: '#FFFFFF' }, // 粉色系
    { bg: '#A8E6CF', text: '#2C3E50' }, // 浅绿色
    { bg: '#FFD93D', text: '#2C3E50' }, // 浅黄色
    { bg: '#6C5CE7', text: '#FFFFFF' }, // 紫色系
    { bg: '#FD79A8', text: '#FFFFFF' }, // 玫瑰色
  ]
  
  // 基于用户名生成一致的颜色索引
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  const colorIndex = Math.abs(hash) % colors.length
  const color = colors[colorIndex]
  
  const svg = `
    <svg width="${size}" height="${size}" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
      <!-- 背景圆形 -->
      <circle cx="50" cy="50" r="50" fill="${color.bg}"/>
      
      <!-- 渐变定义 -->
      <defs>
        <linearGradient id="gradient-${hash}" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color:${color.bg};stop-opacity:1" />
          <stop offset="100%" style="stop-color:${adjustColor(color.bg, -20)};stop-opacity:1" />
        </linearGradient>
        <filter id="shadow-${hash}" x="-50%" y="-50%" width="200%" height="200%">
          <feDropShadow dx="2" dy="2" stdDeviation="3" flood-color="rgba(0,0,0,0.3)"/>
        </filter>
      </defs>
      
      <!-- 背景渐变圆形 -->
      <circle cx="50" cy="50" r="48" fill="url(#gradient-${hash})" filter="url(#shadow-${hash})"/>
      
      <!-- 装饰图案 -->
      <circle cx="25" cy="25" r="8" fill="rgba(255,255,255,0.1)"/>
      <circle cx="75" cy="25" r="5" fill="rgba(255,255,255,0.08)"/>
      <circle cx="80" cy="70" r="6" fill="rgba(255,255,255,0.06)"/>
      
      <!-- 文字 -->
      <text x="50" y="50" 
            font-family="Arial, sans-serif" 
            font-size="36" 
            font-weight="bold" 
            text-anchor="middle" 
            dominant-baseline="central" 
            fill="${color.text}"
            style="text-shadow: 1px 1px 2px rgba(0,0,0,0.2)">
        ${firstChar}
      </text>
    </svg>
  `
  
  // 将SVG转换为Data URL
  return `data:image/svg+xml;base64,${btoa(unescape(encodeURIComponent(svg)))}`
}

/**
 * 生成VIP用户专属头像
 * @param name 用户名
 * @param size 头像尺寸，默认100
 * @returns SVG字符串的Data URL
 */
export function generateVipUserAvatar(name: string = '用户', size: number = 100): string {
  const firstChar = name.charAt(0).toUpperCase() || 'V'
  
  // 生成哈希用于一致性
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  
  const svg = `
    <svg width="${size}" height="${size}" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <!-- VIP金色渐变 -->
        <linearGradient id="vip-gradient-${hash}" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color:#FFD700;stop-opacity:1" />
          <stop offset="50%" style="stop-color:#FFA500;stop-opacity:1" />
          <stop offset="100%" style="stop-color:#FF8C00;stop-opacity:1" />
        </linearGradient>
        
        <!-- VIP边框渐变 -->
        <linearGradient id="vip-border-${hash}" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color:#FFED4E;stop-opacity:1" />
          <stop offset="50%" style="stop-color:#FFD700;stop-opacity:1" />
          <stop offset="100%" style="stop-color:#FFA500;stop-opacity:1" />
        </linearGradient>
        
        <!-- 发光效果 -->
        <filter id="vip-glow-${hash}" x="-50%" y="-50%" width="200%" height="200%">
          <feGaussianBlur stdDeviation="3" result="coloredBlur"/>
          <feMerge> 
            <feMergeNode in="coloredBlur"/>
            <feMergeNode in="SourceGraphic"/>
          </feMerge>
        </filter>
        
        <!-- 阴影效果 -->
        <filter id="vip-shadow-${hash}" x="-50%" y="-50%" width="200%" height="200%">
          <feDropShadow dx="2" dy="2" stdDeviation="4" flood-color="rgba(255,215,0,0.6)"/>
        </filter>
      </defs>
      
      <!-- 外层发光圆环 -->
      <circle cx="50" cy="50" r="49" fill="none" stroke="url(#vip-border-${hash})" stroke-width="2" filter="url(#vip-glow-${hash})"/>
      
      <!-- 主背景 -->
      <circle cx="50" cy="50" r="45" fill="url(#vip-gradient-${hash})" filter="url(#vip-shadow-${hash})"/>
      
      <!-- 装饰元素 -->
      <g opacity="0.3">
        <circle cx="30" cy="30" r="6" fill="#FFFFFF"/>
        <circle cx="70" cy="25" r="4" fill="#FFFFFF"/>
        <circle cx="75" cy="70" r="5" fill="#FFFFFF"/>
        <polygon points="20,75 25,65 30,75" fill="#FFFFFF"/>
      </g>
      
      <!-- 皇冠装饰 -->
      <g transform="translate(35, 15)" opacity="0.4">
        <polygon points="0,8 5,0 10,4 15,0 20,4 25,0 30,8 25,12 20,10 15,12 10,10 5,12" fill="#FFFFFF"/>
        <circle cx="5" cy="2" r="1" fill="#FFD700"/>
        <circle cx="15" cy="2" r="1" fill="#FFD700"/>
        <circle cx="25" cy="2" r="1" fill="#FFD700"/>
      </g>
      
      <!-- 用户名文字 -->
      <text x="50" y="55" 
            font-family="Arial, sans-serif" 
            font-size="32" 
            font-weight="bold" 
            text-anchor="middle" 
            dominant-baseline="central" 
            fill="#8B4513"
            style="text-shadow: 1px 1px 2px rgba(0,0,0,0.3)">
        ${firstChar}
      </text>
      
      <!-- VIP标识 -->
      <text x="50" y="80" 
            font-family="Arial, sans-serif" 
            font-size="8" 
            font-weight="bold" 
            text-anchor="middle" 
            dominant-baseline="central" 
            fill="#8B4513"
            opacity="0.8">
        VIP
      </text>
    </svg>
  `
  
  return `data:image/svg+xml;base64,${btoa(unescape(encodeURIComponent(svg)))}`
}

/**
 * 调整颜色亮度
 * @param hex 16进制颜色值
 * @param percent 调整百分比，正数变亮，负数变暗
 * @returns 调整后的颜色值
 */
function adjustColor(hex: string, percent: number): string {
  const num = parseInt(hex.replace('#', ''), 16)
  const amt = Math.round(2.55 * percent)
  const R = (num >> 16) + amt
  const G = (num >> 8 & 0x00FF) + amt
  const B = (num & 0x0000FF) + amt
  
  return '#' + (0x1000000 + (R < 255 ? R < 1 ? 0 : R : 255) * 0x10000 +
    (G < 255 ? G < 1 ? 0 : G : 255) * 0x100 +
    (B < 255 ? B < 1 ? 0 : B : 255)).toString(16).slice(1)
}

/**
 * 获取用户头像URL
 * @param user 用户信息
 * @param size 头像尺寸
 * @returns 头像URL
 */
export function getUserAvatarUrl(user: any, size: number = 100): string {
  // 如果用户有自定义头像，直接返回
  if (user?.userAvatar && user.userAvatar.trim() !== '') {
    return user.userAvatar
  }
  
  // 判断是否为VIP用户（这里使用临时判断逻辑）
  const isVip = user && (user.userRole === 'admin' || 
                        (user.userName && user.userName.toLowerCase().includes('vip')))
  
  const userName = user?.userName || '用户'
  
  // 根据VIP状态生成不同的默认头像
  return isVip ? generateVipUserAvatar(userName, size) : generateUserAvatar(userName, size)
}
