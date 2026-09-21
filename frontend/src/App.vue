<template>
  <router-view v-if="$route.path === '/login'"></router-view>
  <div class="common-layout" v-else>
    <el-container>
      <el-header>
        <div class="header-left">
          <div v-if="isMobile" class="hamburger-btn" @click="sidebarOpen = !sidebarOpen">
            <el-icon :size="24"><Fold v-if="sidebarOpen" /><Expand v-else /></el-icon>
          </div>
          <div class="header-title">🎓 学院一体化管理平台</div>
        </div>
        <div class="header-right">
          <!-- 铃铛通知图标 -->
          <el-popover
            placement="bottom-end"
            :width="280"
            trigger="hover"
            v-if="userType !== 'admin'"
          >
            <template #reference>
              <div class="notification-bell" @click="handleBellClick">
                <el-badge :value="totalBadge" :hidden="totalBadge === 0" class="bell-badge">
                  <el-icon class="header-icon bell-icon"><Bell /></el-icon>
                </el-badge>
              </div>
            </template>
            <div class="notification-panel">
              <div class="panel-header">消息通知</div>
              <div class="panel-content">
                <div class="notification-item" @click="goToNoticeList('unread')">
                  <span class="item-icon unread-icon">📢</span>
                  <span class="item-text">未读通知</span>
                  <span class="item-count" v-if="counts.unreadCount > 0">{{ counts.unreadCount }}</span>
                  <span class="item-count zero" v-else>0</span>
                </div>
                <div class="notification-item" @click="goToNoticeList('todo')">
                  <span class="item-icon todo-icon">📋</span>
                  <span class="item-text">待办事项</span>
                  <span class="item-count" v-if="counts.todoCount > 0">{{ counts.todoCount }}</span>
                  <span class="item-count zero" v-else>0</span>
                </div>
                <!-- 待审批会议室：仅拥有"预约审核"菜单权限的教师可见，与左侧菜单权限一致 -->
                <div v-if="canAccess('/meeting-audit')" class="notification-item" @click="goToMeetingAudit">
                  <span class="item-icon audit-icon">📅</span>
                  <span class="item-text">待审批会议室</span>
                  <span class="item-count" v-if="counts.pendingAuditCount > 0">{{ counts.pendingAuditCount }}</span>
                  <span class="item-count zero" v-else>0</span>
                </div>
              </div>
            </div>
          </el-popover>

          <router-link to="/ai-assistant" class="header-link">
            <span>✨ AI 助手</span>
          </router-link>          <router-link to="/profile" class="header-link">
            <el-icon class="header-icon"><User /></el-icon>
            <span>个人中心</span>
          </router-link>
          <button class="logout-btn" @click="handleLogout">
            <el-icon class="header-icon"><SwitchButton /></el-icon>
            <span>退出登录</span>
          </button>
        </div>
      </el-header>
      
      <el-container>
        <!-- 移动端抽屉遮罩 -->
        <div v-if="isMobile && sidebarOpen" class="sidebar-mask" @click="sidebarOpen = false"></div>

        <el-aside width="220px" :class="['app-sidebar', { 'sidebar-mobile': isMobile, 'sidebar-open': isMobile && sidebarOpen }]">
          <el-menu
            :default-active="$route.path"
            router
            @item-click="onMenuItemClick"
          >
            <el-menu-item index="/ai-assistant">✨ AI 助手</el-menu-item>
            <el-menu-item v-if="isAdmin" index="/ai-knowledge">📚 AI 知识库</el-menu-item>
            <el-sub-menu v-if="showMenuGroup('teacher')" index="teacher">
              <template #title>
                <el-icon><UserFilled /></el-icon>
                <span>教师管理</span>
              </template>
              <el-menu-item v-if="canAccess('/teacher-info')" index="/teacher-info">教师信息</el-menu-item>
              <el-menu-item v-if="canAccess('/teacher-structure')" index="/teacher-structure">师资结构</el-menu-item>
            </el-sub-menu>
            
            <el-sub-menu v-if="showMenuGroup('meeting')" index="meeting">
              <template #title>
                <el-icon><OfficeBuilding /></el-icon>
                <span>会议室管理</span>
              </template>
              <el-menu-item v-if="canAccess('/meeting-room-info')" index="/meeting-room-info">会议室信息</el-menu-item>
              <el-menu-item v-if="canAccess('/meeting-reserve')" index="/meeting-reserve">预约申请</el-menu-item>
              <el-menu-item v-if="canAccess('/meeting-audit')" index="/meeting-audit">预约审核</el-menu-item>
            </el-sub-menu>
            
            <el-sub-menu v-if="showMenuGroup('exam')" index="exam">
              <template #title>
                <el-icon><Document /></el-icon>
                <span>考试管理</span>
              </template>
              <el-menu-item v-if="canAccess('/exam-arrange')" index="/exam-arrange">考试安排</el-menu-item>
              <el-menu-item v-if="canAccess('/exam-progress')" index="/exam-progress">进度管理</el-menu-item>
              <el-menu-item v-if="canAccess('/exam-supervise')" index="/exam-supervise">监考任务</el-menu-item>
            </el-sub-menu>
            
            <el-sub-menu v-if="showMenuGroup('teaching')" index="teaching">
              <template #title>
                <el-icon><Reading /></el-icon>
                <span>教学管理</span>
              </template>
              <el-menu-item v-if="canAccess('/teaching-plan')" index="/teaching-plan">教学计划安排</el-menu-item>
              <el-menu-item v-if="canAccess('/teacher-teaching-plan')" index="/teacher-teaching-plan">我的教学计划</el-menu-item>
              <el-menu-item v-if="canAccess('/workload')" index="/workload">教学课时管理</el-menu-item>
              <el-menu-item v-if="canAccess('/teacher-workload')" index="/teacher-workload">我的教学课时</el-menu-item>
              <el-menu-item v-if="canAccess('/workload-result')" index="/workload-result">工作量计算</el-menu-item>
              <el-menu-item v-if="canAccess('/teacher-workload-result')" index="/teacher-workload-result">我的工作量</el-menu-item>
            </el-sub-menu>
            
            <el-sub-menu v-if="showMenuGroup('org')" index="org">
              <template #title>
                <el-icon><Management /></el-icon>
                <span>组织管理</span>
              </template>
              <el-menu-item v-if="canAccess('/department-info')" index="/department-info">部门信息</el-menu-item>
              <el-menu-item v-if="canAccess('/role-manage')" index="/role-manage">角色管理</el-menu-item>
              <el-menu-item v-if="canAccess('/role-assignment')" index="/role-assignment">角色分配</el-menu-item>
            </el-sub-menu>
            
            <el-sub-menu v-if="showMenuGroup('notice')" index="notice">
              <template #title>
                <el-icon><Bell /></el-icon>
                <span>通知公告</span>
              </template>
              <el-menu-item v-if="canAccess('/notice-list')" index="/notice-list">公告列表</el-menu-item>
              <el-menu-item v-if="canAccess('/notice-publish')" index="/notice-publish">发布公告</el-menu-item>
              <el-menu-item v-if="canAccess('/notice-maintain')" index="/notice-maintain">我的发布</el-menu-item>
            </el-sub-menu>

            <el-sub-menu v-if="showMenuGroup('achievement')" index="achievement">
              <template #title>
                <el-icon><Trophy /></el-icon>
                <span>成果管理</span>
              </template>
              <el-menu-item v-if="canAccess('/achievement-collect')" index="/achievement-collect">成果收集</el-menu-item>
              <el-menu-item v-if="canAccess('/achievement-verify')" index="/achievement-verify">成果验证</el-menu-item>
              <el-menu-item v-if="canAccess('/achievement-display')" index="/achievement-display">成果展示</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-aside>

        <el-main :class="[{ 'main-mobile': isMobile }, { 'ai-main': $route.path === '/ai-assistant' }]">
          <router-view></router-view>
        </el-main>

      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { ref, computed, watch, onMounted, onUnmounted, provide } from 'vue'
import {
  UserFilled,
  OfficeBuilding,
  Document,
  Reading,
  Management,
  Bell,
  Trophy,
  User,
  SwitchButton,
  Expand,
  Fold
} from '@element-plus/icons-vue'
import { noticeReceiveApi } from './api/index.js'

const router = useRouter()
const route = useRoute()

// ===== 移动端适配：窄屏下侧边栏改为抽屉式 =====
const MOBILE_BREAKPOINT = 768
const isMobile = ref(window.innerWidth <= MOBILE_BREAKPOINT)
const sidebarOpen = ref(false)

const handleResize = () => {
  isMobile.value = window.innerWidth <= MOBILE_BREAKPOINT
  // 切回桌面宽度时,收起抽屉状态避免残留
  if (!isMobile.value) sidebarOpen.value = false
}

onMounted(() => window.addEventListener('resize', handleResize))
onUnmounted(() => window.removeEventListener('resize', handleResize))

// 移动端点击菜单项后自动收起抽屉
const onMenuItemClick = () => {
  if (isMobile.value) sidebarOpen.value = false
}

// 使用响应式 ref 存储用户信息和菜单，确保能被 Vue 正确追踪
const userInfo = ref({})
const menus = ref([])

// 从 localStorage 加载数据的方法
const loadAuthData = () => {
  const savedUserInfo = localStorage.getItem('userInfo')
  const savedMenus = localStorage.getItem('menus')

  if (savedUserInfo) {
    try {
      userInfo.value = JSON.parse(savedUserInfo)
    } catch (e) {
      userInfo.value = {}
    }
  }
  if (savedMenus) {
    try {
      menus.value = JSON.parse(savedMenus)
    } catch (e) {
      menus.value = []
    }
  }
}

// 首次加载
loadAuthData()

// 【关键修复】：App.vue 是根组件，setup 只在应用启动时执行一次。
// 登录成功后 Login.vue 写入 localStorage 并跳转，但 App.vue 不会重新挂载，
// 所以 menus ref 不会更新。通过 watch 路由变化来重新加载权限数据，
// 确保从 /login 跳转到其他页面时，菜单能基于最新的 localStorage 数据渲染。
watch(() => route.path, (newPath, oldPath) => {
  // 任何路由跳转后收起移动端抽屉
  sidebarOpen.value = false
  // 从 /login 跳出到其他页面，说明用户刚登录成功，需要重新加载
  if (oldPath === '/login' && newPath !== '/login') {
    loadAuthData()
    loadCounts()
  }
})

const userType = computed(() => {
  return userInfo.value.userType || ''
})

// 超级管理员判断（三重兜底，基于响应式的 userInfo）
const isAdmin = computed(() => {
  if (userType.value === 'admin') return true
  const roleId = userInfo.value.roleId
  if (Array.isArray(roleId)) return roleId.includes(1)
  return roleId === 1 || roleId === '1'
})

// 判断是否可以访问某个菜单路径
const canAccess = (path) => {
  if (isAdmin.value) return true
  return menus.value.includes(path)
}

// 定义菜单分组及其包含的子菜单路径
const menuGroups = {
  teacher: ['/teacher-info', '/teacher-structure'],
  meeting: ['/meeting-room-info', '/meeting-reserve', '/meeting-audit'],
  exam: ['/exam-arrange', '/exam-progress', '/exam-supervise'],
  teaching: ['/teaching-plan', '/teacher-teaching-plan', '/workload', '/teacher-workload', '/workload-result', '/teacher-workload-result'],
  org: ['/department-info', '/role-manage', '/role-assignment'],
  notice: ['/notice-list', '/notice-publish', '/notice-maintain'],
  achievement: ['/achievement-collect', '/achievement-verify', '/achievement-display']
}

// 判断某个菜单分组是否应该显示（至少一个子菜单有权限）
const showMenuGroup = (groupKey) => {
  const paths = menuGroups[groupKey] || []
  return paths.some(path => canAccess(path))
}

// 通知统计数据
const counts = ref({
  unreadCount: 0,
  todoCount: 0,
  pendingAuditCount: 0
})

// 徽章总数（用于铃铛角标）
const totalBadge = computed(() => {
  return Number(counts.value.unreadCount || 0) +
         Number(counts.value.todoCount || 0) +
         Number(counts.value.pendingAuditCount || 0)
})

// 加载通知统计
const loadCounts = async () => {
  // 未登录时不请求，避免登出后轮询仍带旧用户身份发请求
  if (!localStorage.getItem('isLoggedIn')) return
  if (userType.value === 'admin') return
  const teacherId = userInfo.value.userId || userInfo.value.user_id
  if (!teacherId) return
  try {
    // 与审核列表口径一致：拥有"预约审核"菜单权限才统计全部待审核数，否则为0
    const res = await noticeReceiveApi.getCounts(teacherId, canAccess('/meeting-audit'))
    if (res.code === '200' && res.data) {
      counts.value = {
        unreadCount: Number(res.data.unreadCount || 0),
        todoCount: Number(res.data.todoCount || 0),
        pendingAuditCount: Number(res.data.pendingAuditCount || 0)
      }
    }
  } catch (e) {
    console.error('加载通知统计失败:', e)
  }
}

// 点击铃铛（整体跳转或刷新）
const handleBellClick = () => {
  loadCounts()
}

// 跳转到通知列表，可携带状态筛选（未读/待办）
const goToNoticeList = (status) => {
  if (status) {
    router.push({ path: '/notice-list', query: { status } })
  } else {
    router.push('/notice-list')
  }
}

// 暴露给子页面：标记已读/待办等操作后立即刷新铃铛角标，无需等待轮询
provide('refreshBellCount', loadCounts)

// 跳转到会议室审核，默认只看待审核申请（status=0）
const goToMeetingAudit = () => {
  router.push({ path: '/meeting-audit', query: { status: 0 } })
}

// 生命周期：挂载后加载统计，并启动固定 10 秒轮询保持角标最新
// （loadCounts 内部会跳过未登录/admin，登出后轮询自动空转）
let countsTimer = null

onMounted(() => {
  if (userType.value !== 'admin') {
    loadCounts()
  }
  countsTimer = setInterval(() => {
    loadCounts()
  }, 10000)
})

onUnmounted(() => {
  if (countsTimer) {
    clearInterval(countsTimer)
    countsTimer = null
  }
})

const handleLogout = () => {
  localStorage.removeItem('isLoggedIn')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('menus')
  localStorage.removeItem('loginTime')
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style scoped>
.el-header {
  background-color: #90c2ff;
  height: 90px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-title {
  font-size: 32px;
  font-weight: 600;
  color: #f7ffee;
  letter-spacing: 2px;
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Segoe UI', sans-serif;
}

.header-right {
  display: flex;
  gap: 30px;
}

.header-link {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  text-decoration: none;
  font-size: 16px;
  padding: 8px 16px;
  border-radius: 20px;
  transition: all 0.3s;
}

.header-link:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.header-icon {
  font-size: 20px;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  font-size: 16px;
  padding: 8px 16px;
  border-radius: 20px;
  background-color: transparent;
  border: 1px solid rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s;
}

.logout-btn:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.ai-main {
  padding: 0 !important;
  overflow: hidden;
}

/* 通知铃铛样式 */
.notification-bell {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: all 0.3s;
}

.notification-bell:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.bell-icon {
  font-size: 22px !important;
  color: #fff;
}

.bell-badge {
  margin-right: 0;
}

.notification-panel {
  padding: 0;
}

.panel-header {
  padding: 12px 16px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
}

.panel-content {
  padding: 8px 0;
}

.notification-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.item-icon {
  font-size: 18px;
  margin-right: 12px;
}

.item-text {
  flex: 1;
  font-size: 14px;
  color: #606266;
}

.item-count {
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
}

.item-count.zero {
  color: #c0c4cc;
}

/* ===== 汉堡按钮 ===== */
.hamburger-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  margin-right: 8px;
  color: #fff;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.hamburger-btn:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

/* ===== 移动端抽屉遮罩 ===== */
.sidebar-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 998;
}

/* ===== 移动端适配(≤768px):抽屉式侧边栏 + 紧凑 header/内容区 ===== */
@media (max-width: 768px) {
  .el-header {
    height: 60px;
    padding: 0 12px;
  }

  .header-title {
    font-size: 18px;
    letter-spacing: 0;
  }

  .header-right {
    gap: 8px;
  }

  /* 个人中心/退出登录:窄屏只留图标,节省空间 */
  .header-link span,
  .logout-btn span {
    display: none;
  }

  .header-link,
  .logout-btn {
    padding: 8px;
  }

  /* 侧边栏:固定定位 + 滑入滑出 */
  .sidebar-mobile {
    position: fixed;
    top: 60px;
    bottom: 0;
    left: 0;
    width: 220px !important;
    z-index: 999;
    background: #fff;
    overflow-y: auto;
    box-shadow: 2px 0 12px rgba(0, 0, 0, 0.15);
    transform: translateX(-100%);
    transition: transform 0.25s ease;
  }

  .sidebar-mobile.sidebar-open {
    transform: translateX(0);
  }

  /* 内容区:全宽 + 紧凑内边距 */
  .main-mobile {
    padding: 10px !important;
    overflow-x: hidden;
  }
}
</style>

<style>
/* ===== 全局移动端修正(非 scoped,作用于所有页面的 Element 组件) ===== */
@media (max-width: 768px) {
  /* 弹窗宽度自适应,避免溢出屏幕 */
  .el-dialog {
    width: 92% !important;
    margin-top: 6vh !important;
  }

  .el-message-box {
    width: 90% !important;
    max-width: 420px;
  }

  /* 防止整页横向溢出 */
  body {
    overflow-x: hidden;
  }

  /* 消息提示不超出屏幕 */
  .el-message {
    max-width: 90vw;
  }
}
</style>
