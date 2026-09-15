<template>
  <div class="page-container">
    <div class="page-header">
      <h2>会议室预约申请</h2>
    </div>

    <el-card class="search-card" shadow="hover">
  <template #header>
    <span style="font-weight: bold;">🔍 查询空闲会议室</span>
  </template>
  <el-form :inline="true" class="search-form">
    <el-form-item label="会议室名称">
      <el-input 
        v-model="searchForm.room_name" 
        placeholder="输入会议室名称搜索" 
        clearable
        style="width: 200px;"
      />
    </el-form-item>
    <el-form-item label="日期">
      <el-date-picker 
        v-model="searchForm.date" 
        type="date" 
        placeholder="选择日期"
        :disabled-date="disabledDate"
        value-format="YYYY-MM-DD"
        style="width: 150px;"
        @change="searchAvailableRooms"
      />
    </el-form-item>
   <el-form-item label="开始时间">
  <el-time-select
    v-model="searchForm.start_time"
    :start="'08:00'"
    :step="'00:30'"
    :end="'20:00'"
    placeholder="开始时间"
    style="width: 130px;"
    @change="searchAvailableRooms"
  />
</el-form-item>
<el-form-item label="结束时间">
  <el-time-select
    v-model="searchForm.end_time"
    :start="'08:00'"
    :step="'00:30'"
    :end="'20:00'"
    :min-time="searchForm.start_time"
    placeholder="结束时间"
    style="width: 130px;"
    @change="searchAvailableRooms"
  />
</el-form-item>
    <el-form-item>
      <el-button type="primary" @click="searchAvailableRooms">🔍 查询空闲会议室</el-button>
      <el-button @click="resetSearchForm">重置</el-button>
    </el-form-item>
  </el-form>
</el-card>

    <el-card class="room-list-card" shadow="hover" style="margin-top: 20px;">
      <template #header>
        <span style="font-weight: bold;">📋 空闲会议室列表</span>
        <span style="margin-left: 20px; color: #909399; font-size: 14px;">
          共 {{ availableRooms.length }} 间空闲
        </span>
      </template>
      <el-table :data="availableRooms" border stripe v-loading="loading">
      
        <el-table-column prop="room_name" label="会议室名称" width="150" />
        <el-table-column prop="capacity" label="容纳人数" width="100" />
        <el-table-column prop="room_status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.room_status === '可用' ? 'success' : 'danger'">
              {{ row.room_status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="当前时间段状态" width="150">
          <template #default="{ row }">
            <el-tag type="success" v-if="isRoomAvailable(row)">🟢 空闲</el-tag>
            <el-tag type="warning" v-else-if="isRoomPending(row)">🟡 待审核</el-tag>
            <el-tag type="danger" v-else>🔴 已占用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="openApplyDialog(row)"
              :disabled="!isRoomAvailable(row) || row.room_status !== '可用'"
            >
              申请
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && availableRooms.length === 0" description="暂无空闲会议室，请调整搜索条件" />
    </el-card>

    <el-card class="apply-list-card" shadow="hover" style="margin-top: 20px;">
      <template #header>
        <span style="font-weight: bold;">📝 我的申请记录</span>
        <el-button type="primary" size="small" style="margin-left: 20px;" @click="fetchMyApplies">刷新</el-button>
        <el-select 
          v-model="applyFilter" 
          size="small" 
          style="width: 120px; margin-left: 10px;" 
          @change="filterApplies"
          placeholder="全部状态"
          clearable
        >
          <el-option label="全部" value="" />
          <el-option label="待审核" value="待审核" />
          <el-option label="已通过" value="已通过" />
          <el-option label="已驳回" value="已驳回" />
        </el-select>
      </template>
      <el-table :data="filteredApplies" border stripe v-loading="loading">
        <el-table-column prop="apply_id" label="申请编号" width="100" />
        <el-table-column prop="room_name" label="会议室" width="150" />
        <el-table-column prop="apply_time" label="申请时间" width="160" />
        <el-table-column prop="start_time" label="开始时间" width="160" />
        <el-table-column prop="end_time" label="结束时间" width="160" />
        <el-table-column prop="purpose" label="申请用途" min-width="180" />
        <el-table-column prop="apply_status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.apply_status)">{{ row.apply_status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button 
              link 
              type="danger" 
              v-if="row.apply_status === '待审核'" 
              @click="cancelApply(row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && filteredApplies.length === 0" description="暂无申请记录" />
    </el-card>

    <el-dialog v-model="applyDialogVisible" title="📝 提交预约申请" width="450px">
      <el-form :model="applyForm" label-width="100px">
        <el-form-item label="会议室">
          <el-input :value="applyForm.room_name" disabled />
        </el-form-item>
        <el-form-item label="会议室编号">
          <el-input :value="applyForm.room_id" disabled />
        </el-form-item>
        <el-form-item label="预约日期">
          <el-input :value="applyForm.date" disabled />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-input :value="applyForm.start_time" disabled />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-input :value="applyForm.end_time" disabled />
        </el-form-item>
        <el-form-item label="申请说明">
          <el-input v-model="applyForm.remark" type="textarea" rows="3" placeholder="可填写申请事由（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">确认申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useApplyStore } from '../stores/applyStore'

// 操作权限码
const actionCodes = JSON.parse(localStorage.getItem('userInfo') || '{}').actionCodes || []
const hasPermission = (code) => actionCodes.includes(code)

const applyStore = useApplyStore()

// ✅ 只保留这一个
const currentUser = computed(() => {
  const info = localStorage.getItem('userInfo')
  return info ? JSON.parse(info) : {}
})

const loading = ref(false)

const searchForm = ref({
  room_name: '',
  date: '',
  start_time: '',
  end_time: ''
})

const availableRooms = ref([])

const myApplies = ref([])
const applyFilter = ref('')

const applyDialogVisible = ref(false)
const applyForm = ref({
  room_id: '',
  room_name: '',
  date: '',
  start_time: '',
  end_time: '',
  remark: '',
  teacher_name: '当前用户'
})

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

const extractTime = (datetime) => {
  if (!datetime) return ''
  return datetime.split(' ')[1] || datetime
}

const extractDate = (datetime) => {
  if (!datetime) return ''
  return datetime.split(' ')[0] || datetime
}

const isRoomAvailable = (room) => {
  if (!searchForm.value.date || !searchForm.value.start_time || !searchForm.value.end_time) {
    return true
  }
  
  const dateStr = searchForm.value.date
  const start = searchForm.value.start_time
  const end = searchForm.value.end_time
  
  return !applyStore.isRoomOccupied(room.room_id, dateStr, start, end)
}

const isRoomPending = (room) => {
  if (!searchForm.value.date || !searchForm.value.start_time || !searchForm.value.end_time) {
    return false
  }
  
  const dateStr = searchForm.value.date
  const start = searchForm.value.start_time
  const end = searchForm.value.end_time
  
  return applyStore.getApplies().some(apply => {
    if (apply.room_id !== room.room_id) return false
    if (extractDate(apply.start_time) !== dateStr) return false
    if (apply.apply_status !== '待审核') return false
    
    const applyStart = extractTime(apply.start_time)
    const applyEnd = extractTime(apply.end_time)
    
    return (start < applyEnd && end > applyStart)
  })
}

const searchAvailableRooms = async () => {
  loading.value = true
  try {
    await applyStore.loadRooms()
    await applyStore.loadApplies()
    
    let rooms = [...applyStore.getRooms()]
    
    if (searchForm.value.room_name) {
      rooms = rooms.filter(r => 
        r.room_name.includes(searchForm.value.room_name)
      )
    }
    
    if (searchForm.value.date && searchForm.value.start_time && searchForm.value.end_time) {
      rooms = rooms.filter(r => {
        if (r.room_status !== '可用') return false
        return isRoomAvailable(r)
      })
    } else {
      rooms = rooms.filter(r => r.room_status === '可用')
    }
    
    availableRooms.value = rooms
    
    if (rooms.length === 0) {
      ElMessage.info('未找到符合条件的空闲会议室')
    } else {
      ElMessage.success(`找到 ${rooms.length} 间空闲会议室`)
    }
  } catch (error) {
    ElMessage.error('查询会议室失败')
    console.error('查询会议室失败:', error)
  } finally {
    loading.value = false
  }
}

const resetSearchForm = () => {
  searchForm.value = {
    room_name: '',
    date: '',
    start_time: '',
    end_time: ''
  }
  availableRooms.value = applyStore.getRooms().filter(r => r.room_status === '可用')
}

const openApplyDialog = (room) => {
  if (!searchForm.value.date || !searchForm.value.start_time || !searchForm.value.end_time) {
    ElMessage.warning('请先选择日期和时间段')
    return
  }
  
  const userInfo = currentUser.value
  const isAdmin = userInfo.userType === 'admin'
  
  applyForm.value = {
    room_id: room.room_id,
    room_name: room.room_name,
    date: searchForm.value.date,
    start_time: searchForm.value.start_time,
    end_time: searchForm.value.end_time,
    remark: '',
    // 会议室负责人姓名（用于预约成功后提示通知谁审核）
    manager_name: room.manager_name || '',
    // 管理员申请时，teacher_name 显示为管理员用户名
    teacher_name: isAdmin ? (userInfo.username || '管理员') : (userInfo.name || userInfo.username || '当前用户'),
    // 标记管理员申请
    isAdminApply: isAdmin
  }
  
  applyDialogVisible.value = true
}


const submitApply = async () => {
  loading.value = true
  try {
    const userInfo = currentUser.value
    await applyStore.addApply({
      room_id: applyForm.value.room_id,
      room_name: applyForm.value.room_name,
      teacher_name: userInfo.name || userInfo.username || '当前用户',
      date: applyForm.value.date,
      start_time: applyForm.value.start_time,
      end_time: applyForm.value.end_time,
      remark: applyForm.value.remark
    })
    
    // 弹出提示框：提示通知该会议室负责人进行审核
    const managerName = applyForm.value.manager_name
    const tipMsg = managerName
      ? `已预约，请微信通知${managerName}主任进行审核`
      : '已预约，请微信通知审核老师进行审核'
    ElMessageBox.alert(tipMsg, '提示', {
      confirmButtonText: '知道了',
      type: 'success',
      callback: () => {
        applyDialogVisible.value = false
        fetchMyApplies()
        searchAvailableRooms()
      }
    })
  } catch (error) {
    ElMessage.error('提交申请失败')
    console.error('提交申请失败:', error)
  } finally {
    loading.value = false
  }
}

const cancelApply = async (row) => {
  ElMessageBox.confirm('确定取消该申请吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      const success = await applyStore.cancelApply(row.apply_id)
      if (success) {
        const myIndex = myApplies.value.findIndex(a => a.apply_id === row.apply_id)
        if (myIndex !== -1) {
          myApplies.value.splice(myIndex, 1)
        }
        ElMessage.success('已取消申请')
        await searchAvailableRooms()
      }
    } catch (error) {
      ElMessage.error('取消申请失败')
      console.error('取消申请失败:', error)
    }
  })
}

const statusTagType = (status) => {
  if (status === '待审核') return 'warning'
  if (status === '已通过') return 'success'
  return 'danger'
}

const fetchMyApplies = async () => {
  loading.value = true
  try {
    await applyStore.loadApplies()
    const userInfo = currentUser.value
    const userId = userInfo.userId
    
    console.log('=== 调试我的申请 ===')
    console.log('当前用户 userId:', userId)
    console.log('所有申请数据:', applyStore.getApplies())
    
    myApplies.value = applyStore.getApplies().filter(a => {
      console.log(`申请 ${a.apply_id} 的 teacher_id:`, a.teacher_id, '匹配 userId:', userId)
      return a.teacher_id === userId || a.teacher_id === String(userId)
    })
    
    console.log('过滤后的申请:', myApplies.value)
  } catch (error) {
    console.error('加载我的申请失败:', error)
    myApplies.value = []
  } finally {
    loading.value = false
  }
}

const filteredApplies = computed(() => {
  if (!applyFilter.value) return myApplies.value
  return myApplies.value.filter(a => a.apply_status === applyFilter.value)
})

const filterApplies = () => {
}

onMounted(async () => {
  await applyStore.loadRooms()
  await applyStore.loadApplies()
  availableRooms.value = applyStore.getRooms().filter(r => r.room_status === '可用')
  fetchMyApplies()
})
</script>
<style scoped>
.page-container {
  padding: 20px;
  background-color: #ffffff;
  min-height: calc(100vh - 60px);
}

.page-header { 
  margin-bottom: 20px; 
}

.page-header h2 {
  color: #303133;
  font-weight: 600;
}

.search-card {
  background-color: #ffffff;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.search-form .el-form-item {
  margin-bottom: 10px;
}

.room-list-card, .apply-list-card {
  background-color: #ffffff;
}

:deep(.el-table .cell) {
  padding: 8px 0;
}

:deep(.el-card__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 15px 20px;
  font-weight: 500;
}
</style>
