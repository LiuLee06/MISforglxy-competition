import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/mis-api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器:自动附加 JWT(Authorization: Bearer <token>)
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, error => Promise.reject(error))

// 响应拦截器:统一处理业务 code 与 401/403
api.interceptors.response.use(response => {
  const res = response.data
  if (res && res.code !== undefined) {
    res.code = String(res.code)
  }
  // 业务层 403(权限不足)
  if (res && res.code === '403') {
    ElMessage.error(res.msg || '无权限执行该操作')
  }
  return res
}, error => {
  const status = error.response?.status
  const res = error.response?.data
  if (status === 401) {
    // token 过期/非法,清登录态跳登录页
    localStorage.removeItem('isLoggedIn')
    localStorage.removeItem('token')
    localStorage.removeItem('loginTime')
    ElMessage.warning(res?.msg || '登录已过期,请重新登录')
    // 跳登录页(避免在登录页循环跳转)
    if (!window.location.pathname.endsWith('/login')) {
      window.location.href = '/login'
    }
  } else if (status === 403) {
    ElMessage.error(res?.msg || '无权限执行该操作')
  } else {
    console.error('API Error:', error)
  }
  return Promise.reject(error)
})

export const deptApi = {
  getAll: () => api.get('/dept'),
  getById: (id) => api.get(`/dept/${id}`),
  create: (data) => api.post('/dept', data),
  update: (data) => api.put('/dept', data),
  delete: (id) => api.delete(`/dept/${id}`)
}

export const teacherApi = {
  getAll: () => api.get('/teacher'),
  getById: (id) => api.get(`/teacher/${id}`),
  getByUsername: (username) => api.get(`/teacher/username/${username}`),
  getByPhone: (phone) => api.get(`/teacher/phone/${phone}`),
  getPage: (params) => api.get('/teacher/page', { params }),
  create: (data) => api.post('/teacher', data),
  update: (data) => {
    // TeacherController.update 接口是 PUT /teacher，body 中传递 teacherId
    return api.put('/teacher', data)
  },
  delete: (id) => api.delete(`/teacher/${id}`)
}

export const roleApi = {
  getAll: () => api.get('/role'),
  getPage: (params) => api.get('/role/page', { params }),
  getById: (id) => api.get(`/role/${id}`),
  create: (data) => api.post('/role', data),
  update: (data) => api.put('/role', data),
  delete: (id) => api.delete(`/role/${id}`)
}

export const menuActionApi = {
  getAll: () => api.get('/menu-action'),
  getById: (id) => api.get(`/menu-action/${id}`),
  getByMenuId: (menuId) => api.get(`/menu-action/menu/${menuId}`),
  getByCode: (code) => api.get(`/menu-action/code/${code}`),
  create: (data) => api.post('/menu-action', data),
  update: (data) => api.put('/menu-action', data),
  delete: (id) => api.delete(`/menu-action/${id}`)
}

export const roleMenuActionApi = {
  getAll: () => api.get('/role-menu-action'),
  getByRoleId: (roleId) => api.get(`/role-menu-action/role/${roleId}`),
  getActionCodesByRoleId: (roleId) => api.get(`/role-menu-action/action-codes/${roleId}`),
  getActionCodesByRoleIds: (roleIds) => api.post('/role-menu-action/action-codes', { roleIds }),
  create: (data) => api.post('/role-menu-action', data),
  update: (data) => api.put('/role-menu-action', data),
  delete: (id) => api.delete(`/role-menu-action/${id}`),
  deleteByRoleId: (roleId) => api.delete(`/role-menu-action/role/${roleId}`),
  batchInsert: (list) => api.post('/role-menu-action/batch', list)
}

export const menuApi = {
  getAll: () => api.get('/menu'),
  getById: (id) => api.get(`/menu/${id}`)
}

export const roleMenuApi = {
  getAll: () => api.get('/role-menu'),
  getByRoleIdAndMenuId: (roleId, menuId) => api.get(`/role-menu/role/${roleId}/menu/${menuId}`),
  getByRoleId: (roleId) => api.get(`/role-menu/role/${roleId}`),
  getMenuIdsByRoleId: (roleId) => api.get(`/role-menu/role-ids/${roleId}`),
  create: (data) => api.post('/role-menu', data),
  update: (data) => api.put('/role-menu', data),
  deleteByRoleIdAndMenuId: (roleId, menuId) => api.delete(`/role-menu/role/${roleId}/menu/${menuId}`),
  deleteByRoleId: (roleId) => api.delete(`/role-menu/role/${roleId}`),
  batchInsert: (list) => api.post('/role-menu/batch', list)
}

export const teacherRoleApi = {
  getAll: () => api.get('/teacher-role'),
  getPage: (params) => api.get('/teacher-role/page', { params }),
  getByTeacherId: (teacherId) => api.get(`/teacher-role/teacher/${teacherId}`),
  getByRoleId: (roleId) => api.get(`/teacher-role/role/${roleId}`),
  create: (data) => api.post('/teacher-role', data),
  updateRole: (data) => api.put('/teacher-role', data),
  delete: (teacherId, roleId) => api.delete(`/teacher-role?teacherId=${teacherId}&roleId=${roleId}`)
}

export const originalExamApi = {
  getAll: () => api.get('/original-exam'),
  getById: (id) => api.get(`/original-exam/${id}`),
  getBySemesterId: (semesterId) => api.get(`/original-exam/semester/${semesterId}`),
  pageQuery: (params) => api.get('/original-exam/page', { params }),
  create: (data) => api.post('/original-exam', data),
  batchImport: (data) => api.post('/original-exam/batch', data),
  update: (data) => api.put('/original-exam', data),
  updateReason: (data) => api.put('/original-exam/reason', data),
  generateFinal: () => api.post('/original-exam/generate-final'),
  getConfirmStats: (params) => api.get('/original-exam/confirm-stats', { params }),
  getConfirmStatsFlat: (params) => api.get('/original-exam/confirm-stats-flat', { params }),
  pageQueryFlat: (params) => api.get('/original-exam/page-flat', { params }),
  updateTeacherReason: (data) => api.put('/original-exam/teacher-reason', data),
  getStatsByTeacher: (params) => api.get('/original-exam/stats-by-teacher', { params }),
  getDistinctDept: (params) => api.get('/original-exam/distinct-dept', { params }),
  exportWithReason: () => api.get('/original-exam/export-with-reason'),
  delete: (id) => api.delete(`/original-exam/${id}`),
  batchDelete: (ids) => api.post('/original-exam/batch-delete', ids),
  deleteAll: () => api.post('/original-exam/delete-all', {})
}

export const finalExamApi = {
  getAll: () => api.get('/final-exam'),
  getById: (id) => api.get(`/final-exam/${id}`),
  getBySemesterId: (semesterId) => api.get(`/final-exam/semester/${semesterId}`),
  pageQuery: (params) => api.get('/final-exam/page', { params }),
  create: (data) => api.post('/final-exam', data),
  batchImport: (data) => api.post('/final-exam/batch', data),
  update: (data) => api.put('/final-exam', data),
  delete: (id) => api.delete(`/final-exam/${id}`),
  batchDelete: (ids) => api.post('/final-exam/batch-delete', ids)
}

export const originalExamTeacherApi = {
  getAll: () => api.get('/original-exam-teacher'),
  getById: (id) => api.get(`/original-exam-teacher/${id}`),
  getByExamId: (examId) => api.get(`/original-exam-teacher/exam/${examId}`),
  pageQuery: (params) => api.get('/original-exam-teacher/page', { params }),
  create: (data) => api.post('/original-exam-teacher', data),
  batchImport: (data) => api.post('/original-exam-teacher/batch', data),
  update: (data) => api.put('/original-exam-teacher', data),
  updateReason: (data) => api.put('/original-exam-teacher/reason', data),
  generateFinal: () => api.post('/original-exam-teacher/generate-final'),
  delete: (id) => api.delete(`/original-exam-teacher/${id}`),
  deleteAll: () => api.post('/original-exam-teacher/delete-all', {}),
  exportData: (params) => api.get('/original-exam-teacher/export', { params, responseType: 'blob' })
}

export const finalExamTeacherApi = {
  getAll: () => api.get('/final-exam-teacher'),
  getById: (id) => api.get(`/final-exam-teacher/${id}`),
  getByExamId: (examId) => api.get(`/final-exam-teacher/exam/${examId}`),
  pageQuery: (params) => api.get('/final-exam-teacher/page', { params }),
  create: (data) => api.post('/final-exam-teacher', data),
  batchImport: (data) => api.post('/final-exam-teacher/batch', data),
  update: (data) => api.put('/final-exam-teacher', data),
  delete: (id) => api.delete(`/final-exam-teacher/${id}`),
  generateFinal: () => api.post('/final-exam-teacher/generate'),
  deleteAll: () => api.delete('/final-exam-teacher/all')
}

export const semesterApi = {
  getAll: (params) => api.get('/semester', { params: params || {} }),
  getById: (id) => api.get(`/semester/${id}`),
  create: (data) => api.post('/semester', data),
  update: (data) => api.put(`/semester/${data.semesterId}`, data),
  delete: (id) => api.delete(`/semester/${id}`)
}

export const teachingScheduleApi = {
  getAll: () => api.get('/teaching-schedule'),
  getByNoticeId: (noticeId) => api.get(`/teaching-schedule/${noticeId}`),
  getByConditions: (params) => api.get('/teaching-schedule', { params: params || {} }),
  create: (data) => api.post('/teaching-schedule', data),
  batchImport: (data) => api.post('/teaching-schedule/imports', data),
  update: (data) => api.put(`/teaching-schedule/${data.noticeId}`, data),
  updateEditable: (data) => api.put(`/teaching-schedule/${data.noticeId}/editable-fields`, data),
  delete: (noticeId) => api.delete(`/teaching-schedule/${noticeId}`),
  deleteBySemester: (semesterId) => api.delete('/teaching-schedule', { params: { semesterId } }),
  confirm: (noticeId) => api.put(`/teaching-schedule/${noticeId}/confirmation`)
}

export const roomApi = {
  getAll: () => api.get('/meeting-rooms'),
  getById: (id) => api.get(`/meeting-rooms/${id}`),
  create: (data) => api.post('/meeting-rooms', data),
  update: (data) => api.put(`/meeting-rooms/${data.roomId}`, data),
  delete: (id) => api.delete(`/meeting-rooms/${id}`),
  getAvailable: (params) => {
    const query = Object.keys(params)
      .filter(key => params[key] && params[key] !== '')
      .map(key => `${key}=${encodeURIComponent(params[key])}`)
      .join('&')
    return api.get(`/room-applies/available?${query}`)
  }
}

// 替换原有的 bookingApi
export const bookingApi = {
  getAll: (userId, userType, hasAuditPermission) => {
    const params = new URLSearchParams()
    if (userId !== undefined && userId !== null) {
      params.append('userId', userId)
    }
    if (userType) {
      params.append('userType', userType)
    }
    if (hasAuditPermission !== undefined && hasAuditPermission !== null) {
      params.append('hasAuditPermission', hasAuditPermission)
    }
    const queryString = params.toString()
    return api.get(`/room-applies${queryString ? '?' + queryString : ''}`)
  },
  getById: (id) => api.get(`/room-applies/${id}`),
  create: (data) => api.post('/room-applies', data),
  update: (data) => api.put(`/room-applies/${data.bookingId}`, data),
  delete: (id) => api.delete(`/room-applies/${id}`),
  audit: (id, status, auditorId) => api.put(`/room-applies/${id}/audit`, { status, auditorId })
}
export const noticeApi = {
  getAll: () => api.get('/notice'),
  getById: (id) => api.get(`/notice/${id}`),
  getByTeacherId: (teacherId) => api.get(`/notice/teacher/${teacherId}`),
  getByPublisherId: (publisherId, userType) => api.get(`/notice/publisher/${publisherId}?userType=${userType || 'teacher'}`),
  create: (data) => api.post('/notice', data),
  publish: (data) => api.post('/notice/publish', data),
  editPublish: (data) => api.post('/notice/edit-publish', data),
  update: (data) => api.put('/notice', data),
  delete: (id, userId, userType) => api.delete(`/notice/${id}?userId=${userId}&userType=${userType || 'teacher'}`)
}

export const noticeReceiveApi = {
  markAsRead: (noticeId, teacherId) => api.put(`/notice-receive/mark-read?noticeId=${noticeId}&teacherId=${teacherId}`),
  markAsTodo: (noticeId, teacherId) => api.put(`/notice-receive/mark-todo?noticeId=${noticeId}&teacherId=${teacherId}`),
  unmarkTodo: (noticeId, teacherId) => api.put(`/notice-receive/unmark-todo?noticeId=${noticeId}&teacherId=${teacherId}`),
  getReadStats: (noticeId) => api.get(`/notice-receive/stats/${noticeId}`),
  getReadList: (noticeId) => api.get(`/notice-receive/list/${noticeId}`),
  remind: (noticeId) => api.put(`/notice-receive/remind/${noticeId}`),
  getCounts: (teacherId, hasAuditPermission) => api.get(`/notice-receive/counts?teacherId=${teacherId}&hasAuditPermission=${hasAuditPermission ? 'true' : 'false'}`)
}

// 教师工作量计算结果（管理端计算落库，教师端只读）
export const workloadResultApi = {
  getList: (params) => api.get('/workload-result', { params: params || {} }),
  getMyList: (params) => api.get('/workload-result/my', { params: params || {} }),
  calculate: (data) => api.post('/workload-result/calculate', data)
}

export const workloadApi = {
  getList: (params) => api.get('/workload', { params: params || {} }),
  getMyRelated: (params) => api.get('/workload/related', { params: params || {} }),
  // 后端 GET /workload/department 已实现，但当前无页面调用，暂不启用
  // getDepartmentWorkloads: (params) => api.get('/workload/department', { params: params || {} }),
  getCompletion: (params) => api.get('/workload/completion', { params: params || {} }),
  getDepartmentCompletion: (params) => api.get('/workload/department-completion', { params: params || {} }),
  // 后端 GET /workload/{id} 已实现，但编辑回填直接用表格行数据，无页面调用，暂不启用
  // getById: (id) => api.get(`/workload/${id}`),
  batchImport: (data) => api.post('/workload/imports', data),
  deleteBySemester: (semesterId) => api.delete('/workload', { params: { semesterId } }),
  updateActualHours: (id, data) => api.put(`/workload/${id}/actual-hours`, data),
  confirm: (id, data) => api.put(`/workload/${id}/confirmation`, data)
}

export default api
export const agentApi = {
  chat: (data) => api.post('/agent/chat', data, { timeout: 90000 }),
  getConversations: () => api.get('/agent/conversations'),
  getMessages: (conversationId) => api.get('/agent/conversations/' + conversationId + '/messages'),
  deleteConversation: (conversationId) => api.delete('/agent/conversations/' + conversationId),
  confirmAction: (actionId) => api.post('/agent/actions/' + actionId + '/confirm'),
  cancelAction: (actionId) => api.post('/agent/actions/' + actionId + '/cancel')
}

// 成果管理：成果收集 / 人工验证 / 成果展示
export const achievementApi = {
  getOptions: () => api.get('/achievement/options'),
  getList: (params) => api.get('/achievement', { params: params || {} }),
  getStats: (params) => api.get('/achievement/stats', { params: params || {} }),
  create: (data) => api.post('/achievement', data),
  update: (data) => api.put('/achievement', data),
  remove: (id) => api.delete(`/achievement/${id}`),
  verify: (id, status) => api.post(`/achievement/${id}/verify`, null, { params: { status } }),
  batchPass: (ids) => api.post('/achievement/batch-pass', { ids }),
  batchDelete: (ids) => api.post('/achievement/batch-delete', { ids }),
  upload: (formData) => api.post('/achievement/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000
  }),
  getOcrStatus: () => api.get('/achievement/ocr/status'),
  ocrRecognize: (fileUrl) => api.post('/achievement/ocr', { fileUrl }, { timeout: 90000 })
}
