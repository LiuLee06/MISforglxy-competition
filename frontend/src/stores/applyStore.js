import { ref } from 'vue'
import { bookingApi, roomApi } from '../api/index.js'

const applies = ref([])
const rooms = ref([])

// 获取当前登录用户信息
const getCurrentUser = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  return userInfoStr ? JSON.parse(userInfoStr) : {}
}

// 获取当前用户的权限码列表
const getActionCodes = () => {
  const user = getCurrentUser()
  return user.actionCodes || []
}

// 判断是否有预约审核权限（有 /meeting-audit 菜单访问权限即可审核）
const hasAuditPermission = () => {
  const user = getCurrentUser()
  if (user.userType === 'admin') return true
  const menus = JSON.parse(localStorage.getItem('menus') || '[]')
  return menus.includes('/meeting-audit')
}

const loadApplies = async () => {
  const user = getCurrentUser()
  const userId = user.userId || 0
  const userType = user.userType || 'teacher'
  const hasAudit = hasAuditPermission()
  
  try {
    // 传入是否有审核权限，后端根据权限返回不同数据
    const res = await bookingApi.getAll(userId, userType, hasAudit)
    if (res.code === '200' && res.data) {
      applies.value = res.data.map(a => ({
        ...a,
        apply_id: a.applyId,
        room_id: a.room ? (a.room.roomId || a.room.room_id) : '',
        room_name: a.room ? (a.room.roomName || a.room.room_name) : '',
        teacher_name: a.teacher ? (a.teacher.name || a.teacher.teacherName) : '',
        teacher_id: a.teacher ? (a.teacher.teacherId || a.teacher.teacher_id) : '',
        applicant_name: a.applicantName || a.applicant_name || '',
        apply_time: a.applyTime ? formatDateTime(a.applyTime) : '',
        start_time: a.startTime ? formatDateTime(a.startTime) : '',
        end_time: a.endTime ? formatDateTime(a.endTime) : '',
        apply_status: getStatusText(a.applyStatus),
        rawStatus: a.applyStatus
      }))
    } else {
      applies.value = []
    }
  } catch (error) {
    console.error('加载申请数据失败:', error)
    applies.value = []
  }
}

const loadRooms = async () => {
  try {
    const res = await roomApi.getAll()
    if (res.code === '200' && res.data) {
      rooms.value = res.data.map(r => ({
        room_id: r.roomId,
        room_name: r.roomName,
        capacity: r.capacity,
        room_status: r.roomStatus == 1 ? '可用' : '维修中',
        manager_id: r.managerId,
        manager_name: r.managerName || ''
      }))
    } else {
      rooms.value = []
    }
  } catch (error) {
    console.error('加载会议室数据失败:', error)
    rooms.value = []
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  if (typeof dateTime === 'string') {
    return dateTime.replace('T', ' ').substring(0, 19)
  }
  return dateTime
}

const getStatusText = (status) => {
  if (status === 0 || status === '0') return '待审核'
  if (status === 1 || status === '1') return '已通过'
  if (status === 2 || status === '2') return '已驳回'
  return '未知(' + status + ')'
}

const getApplies = () => {
  return applies.value
}

const getPendingApplies = () => {
  return applies.value.filter(a => a.apply_status === '待审核')
}

const getApprovedApplies = () => {
  return applies.value.filter(a => a.apply_status === '已通过')
}

const getRejectedApplies = () => {
  return applies.value.filter(a => a.apply_status === '已驳回')
}

const addApply = async (apply) => {
  try {
    const dateStr = apply.date || apply.start_time.split(' ')[0]
    
    const userInfo = getCurrentUser()
    const isAdmin = userInfo.userType === 'admin'
    const teacherId = isAdmin ? -1 : (userInfo.userId || 1)
    const teacherName = isAdmin ? (userInfo.username || '管理员') : apply.teacher_name
    
    const payload = {
      room: { roomId: parseInt(apply.room_id.replace('R', '')) },
      teacher: { teacherId: teacherId },
      applicantName: isAdmin ? (userInfo.username || '管理员') : null,
      startTime: `${dateStr} ${apply.start_time}:00`,
      endTime: `${dateStr} ${apply.end_time}:00`,
      applyStatus: 0
    }
    const res = await bookingApi.create(payload)
    if (res.code === '200') {
      await loadApplies()
      const newApply = {
        apply_id: Date.now().toString(),
        room_id: apply.room_id,
        room_name: apply.room_name,
        teacher_name: teacherName,
        teacher_id: isAdmin ? '管理员' : teacherId,
        apply_time: new Date().toLocaleString(),
        start_time: `${dateStr} ${apply.start_time}`,
        end_time: `${dateStr} ${apply.end_time}`,
        apply_status: '待审核',
        remark: apply.remark
      }
      applies.value.unshift(newApply)
      return newApply
    }
    throw new Error('提交失败')
  } catch (error) {
    console.error('添加申请失败:', error)
    throw error
  }
}

const auditApply = async (applyId, newStatus) => {
  try {
    const statusMap = { '已通过': 1, '已驳回': 2 }
    const statusCode = statusMap[newStatus]
    const id = parseInt(applyId) || applyId

    const userInfo = getCurrentUser()
    const auditorId = userInfo.userType === 'admin' ? 0 : (userInfo.userId || 0)

    const res = await bookingApi.audit(id, statusCode, auditorId)
    if (res.code === '200') {
      await loadApplies()
      return true
    }
    return false
  } catch (error) {
    console.error('审核申请失败:', error)
    throw error
  }
}

/**
 * 撤销已通过的申请（有 room:audit 权限的人均可操作）
 */
const revokeApply = async (applyId) => {
  try {
    const id = parseInt(applyId) || applyId
    const userInfo = getCurrentUser()
    // 管理员身份：auditorId = 0，普通审核员传自己的 userId
    const auditorId = userInfo.userType === 'admin' ? 0 : (userInfo.userId || 0)
    
    const res = await bookingApi.audit(id, 2, auditorId)
    if (res.code === '200') {
      await loadApplies()
      return true
    }
    return false
  } catch (error) {
    console.error('撤销申请失败:', error)
    throw error
  }
}

const cancelApply = async (applyId) => {
  try {
    const id = parseInt(applyId) || applyId
    const res = await bookingApi.delete(id)
    if (res.code === '200') {
      await loadApplies()
      const index = applies.value.findIndex(a => a.apply_id === applyId)
      if (index !== -1) {
        applies.value.splice(index, 1)
      }
      return true
    }
    return false
  } catch (error) {
    console.error('取消申请失败:', error)
    throw error
  }
}

const getTeacherApplies = (teacherName) => {
  return applies.value.filter(a => {
    if (a.teacher_name === teacherName) return true
    const userInfo = getCurrentUser()
    if (a.teacher_id && userInfo.userId) {
      return a.teacher_id === userInfo.userId
    }
    return false
  })
}

const isRoomOccupied = (roomId, date, startTime, endTime) => {
  return applies.value.some(apply => {
    if (apply.room_id !== roomId) return false
    if (apply.start_time.split(' ')[0] !== date) return false
    if (apply.apply_status === '已驳回') return false
    
    const applyStart = apply.start_time.split(' ')[1]
    const applyEnd = apply.end_time.split(' ')[1]
    
    return (startTime < applyEnd && endTime > applyStart)
  })
}

const getRooms = () => {
  return rooms.value
}

export const useApplyStore = () => {
  return {
    applies,
    rooms,
    loadApplies,
    loadRooms,
    getRooms,
    getApplies,
    getPendingApplies,
    getApprovedApplies,
    getRejectedApplies,
    addApply,
    auditApply,
    revokeApply,
    cancelApply,
    getTeacherApplies,
    isRoomOccupied,
    hasAuditPermission
  }
}