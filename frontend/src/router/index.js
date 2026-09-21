import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/ai-assistant',
    name: 'AiAssistant',
    component: () => import('../views/AiAssistant.vue')
  },
  {
    path: '/ai-knowledge',
    name: 'AiKnowledge',
    component: () => import('../views/AiKnowledge.vue')
  },
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue')
  },
  {
    path: '/teacher-info',
    name: 'TeacherInfo',
    component: () => import('../views/TeacherInfo.vue')
  },
  {
    path: '/teacher-structure',
    name: 'TeacherStructure',
    component: () => import('../views/TeacherStructure.vue')
  },
  {
    path: '/meeting-room-info',
    name: 'MeetingRoomInfo',
    component: () => import('../views/MeetingRoomInfo.vue')
  },
  {
    path: '/meeting-reserve',
    name: 'MeetingReserve',
    component: () => import('../views/MeetingReserve.vue')
  },
  {
    path: '/meeting-audit',
    name: 'MeetingAudit',
    component: () => import('../views/MeetingAudit.vue')
  },
  {
    path: '/exam-arrange',
    name: 'ExamArrange',
    component: () => import('../views/ExamArrange.vue')
  },
  {
    path: '/exam-progress',
    name: 'ProgressManage',
    component: () => import('../views/ProgressManage.vue')
  },
  {
    path: '/exam-supervise',
    name: 'ExamSupervise',
    component: () => import('../views/ExamSupervise.vue')
  },
  {
    path: '/teaching-plan',
    name: 'TeachingPlan',
    component: () => import('../views/TeachingPlan.vue')
  },
  {
    path: '/teacher-teaching-plan',
    name: 'TeacherTeachingPlan',
    component: () => import('../views/TeacherTeachingPlan.vue')
  },
  {
    path: '/workload',
    name: 'Workload',
    component: () => import('../views/Workload.vue')
  },
  {
    path: '/teacher-workload',
    name: 'TeacherWorkload',
    component: () => import('../views/TeacherWorkload.vue')
  },
  {
    path: '/workload-result',
    name: 'WorkloadResult',
    component: () => import('../views/WorkloadResult.vue')
  },
  {
    path: '/teacher-workload-result',
    name: 'TeacherWorkloadResult',
    component: () => import('../views/TeacherWorkloadResult.vue')
  },
  {
    path: '/department-info',
    name: 'DepartmentInfo',
    component: () => import('../views/DepartmentInfo.vue')
  },
  {
    path: '/role-manage',
    name: 'RoleManage',
    component: () => import('../views/RoleManage.vue')
  },
  {
    path: '/role-assignment',
    name: 'RoleAssignment',
    component: () => import('../views/RoleAssignment.vue')
  },
  {
    path: '/notice',
    name: 'Notice',
    component: () => import('../views/Notice.vue')
  },
  {
    path: '/notice-list',
    name: 'NoticeList',
    component: () => import('../views/NoticeList.vue')
  },
  {
    path: '/notice-publish',
    name: 'NoticePublish',
    component: () => import('../views/NoticePublish.vue')
  },
  {
    path: '/notice-maintain',
    name: 'NoticeMaintain',
    component: () => import('../views/NoticeMaintain.vue')
  },
  {
    path: '/achievement-collect',
    name: 'AchievementCollect',
    component: () => import('../views/achievement/AchievementCollect.vue')
  },
  {
    path: '/achievement-verify',
    name: 'AchievementVerify',
    component: () => import('../views/achievement/AchievementVerify.vue')
  },
  {
    path: '/achievement-display',
    name: 'AchievementDisplay',
    component: () => import('../views/achievement/AchievementDisplay.vue')
  },
  {
    path: '/no-permission',
    name: 'NoPermission',
    component: () => import('../views/NoPermission.vue')
  }
]

const router = createRouter({
  // 跟随 vite 的 base：开发为 '/'，生产构建为 '/mis/'
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.path === '/login') {
    next()
    return
  }

  const isLoggedIn = localStorage.getItem('isLoggedIn')
  const menusStr = localStorage.getItem('menus')
  const menus = menusStr ? JSON.parse(menusStr) : []
  const userInfoStr = localStorage.getItem('userInfo')
  const userInfo = userInfoStr ? JSON.parse(userInfoStr) : {}

  if (!isLoggedIn) {
    next('/login')
    return
  }

  // 会话过期检查：登录超过 24 小时则强制重新登录
  const SESSION_DURATION = 24 * 60 * 60 * 1000 // 24 小时（毫秒）
  const loginTime = localStorage.getItem('loginTime')
  if (loginTime) {
    const elapsed = Date.now() - Number(loginTime)
    if (elapsed > SESSION_DURATION) {
      // 清除已过期的登录状态
      localStorage.removeItem('isLoggedIn')
      localStorage.removeItem('loginTime')
      ElMessage.warning('登录已过期，请重新登录')
      next('/login')
      return
    }
  } else {
    // 没有 loginTime 视为异常状态，要求重新登录
    next('/login')
    return
  }

  const publicPaths = ['/profile', '/no-permission', '/ai-assistant']
  if (publicPaths.includes(to.path)) {
    next()
    return
  }

  // 超级管理员判断（三重兜底，与 App.vue 的 isAdmin 保持一致）
  const isAdmin = userInfo.userType === 'admin' ||
    (Array.isArray(userInfo.roleId) ? userInfo.roleId.includes(1) : userInfo.roleId === 1 || userInfo.roleId === '1')

  // 统一权限判断：只要是管理员，或者菜单列表里包含该路径，就允许访问
  const hasPermission = isAdmin || menus.includes(to.path)
  if (hasPermission) {
    next()
  } else {
    next('/no-permission')
  }
})

export default router
