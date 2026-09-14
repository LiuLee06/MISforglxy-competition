<template>
  <div class="page-container">
    <div class="page-header">
      <h2>预约审核</h2>
    </div>

    <el-card class="filter-card" shadow="hover">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="状态">
          <el-select v-model="filterStatus" placeholder="全部" clearable @change="fetchList" style="width: 130px">
            <el-option label="全部" value="" />
            <el-option label="待审核" value="待审核" />
            <el-option label="已通过" value="已通过" />
            <el-option label="已驳回" value="已驳回" />
          </el-select>
        </el-form-item>
        <el-form-item label="会议室">
          <el-select v-model="filterRoom" placeholder="全部" clearable @change="fetchList" style="width: 130px">
            <el-option v-for="r in roomOptions" :key="r.room_id" :label="r.room_name" :value="r.room_id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
          <el-button type="success" @click="fetchList" :loading="loading">🔄 刷新</el-button>
        </el-form-item>
        <el-form-item>
          <span style="color: #909399; font-size: 14px;">
            共 {{ filteredList.length }} 条记录
          </span>
        </el-form-item>
        <el-form-item v-if="isAdmin">
          <el-tag type="info" size="small">管理员模式：可查看全部申请</el-tag>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="hover" style="margin-top: 20px;">
      <el-table :data="filteredList" border stripe v-loading="loading">
        <el-table-column prop="apply_id" label="申请编号" width="120" />
        <el-table-column prop="room_name" label="会议室" width="150" />
        <el-table-column label="申请人" width="120">
          <template #default="{ row }">
            {{ row.teacher_name || row.applicant_name || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="apply_time" label="申请时间" width="180" />
        <el-table-column prop="start_time" label="开始时间" width="180" />
        <el-table-column prop="end_time" label="结束时间" width="180" />
        <el-table-column prop="apply_status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.apply_status)">{{ row.apply_status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120">
          <template #default="{ row }">
            <span v-if="row.remark">{{ row.remark }}</span>
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" v-if="showActions">
          <template #default="{ row }">
            <!-- 待审核：通过 / 驳回 -->
            <template v-if="row.apply_status === '待审核'">
              <el-button link type="success" @click="audit(row, '已通过')">通过</el-button>
              <el-button link type="danger" @click="audit(row, '已驳回')">驳回</el-button>
            </template>
            
            <!-- 已通过：有权限的人都可以撤销 -->
            <template v-else-if="row.apply_status === '已通过'">
              <el-button
                link
                type="warning"
                @click="revoke(row)"
              >
                撤销
              </el-button>
            </template>
            
            <!-- 已驳回：无操作 -->
            <template v-else-if="row.apply_status === '已驳回'">
              <span style="color: #c0c4cc;">-</span>
            </template>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="!loading && filteredList.length === 0" description="暂无申请记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useApplyStore } from '../stores/applyStore'

const route = useRoute()
// 铃铛跳转的数字状态码 → 页面筛选框使用的中文状态
const STATUS_TEXT_MAP = { 0: '待审核', 1: '已通过', 2: '已驳回' }

// 操作权限码
const actionCodes = JSON.parse(localStorage.getItem('userInfo') || '{}').actionCodes || []
const hasPermission = (code) => actionCodes.includes(code)

// 当前用户信息
const userInfo = computed(() => {
  const info = localStorage.getItem('userInfo')
  return info ? JSON.parse(info) : {}
})

// 是否为管理员（用于控制"查看全部申请"的数据范围）
const isAdmin = computed(() => userInfo.value.userType === 'admin')

const applyStore = useApplyStore()

const loading = ref(false)
const filterStatus = ref('')
const filterRoom = ref('')

const roomOptions = ref([])

const filteredList = computed(() => {
  let list = applyStore.getApplies()
  
  if (filterStatus.value) {
    list = list.filter(item => item.apply_status === filterStatus.value)
  }
  
  if (filterRoom.value) {
    list = list.filter(item => item.room_id === filterRoom.value)
  }
  
  return list
})

// 有菜单访问权限的人显示操作列
const showActions = computed(() => true)

const statusTagType = (status) => {
  if (status === '待审核') return 'warning'
  if (status === '已通过') return 'success'
  return 'danger'
}

const fetchList = async () => {
  loading.value = true
  try {
    await applyStore.loadApplies()
    await applyStore.loadRooms()
    roomOptions.value = applyStore.getRooms()
    const count = filteredList.value.length
    ElMessage.success(`已刷新，当前 ${count} 条记录`)
  } catch (error) {
    ElMessage.error('加载申请列表失败')
    console.error('加载申请列表失败:', error)
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  filterStatus.value = ''
  filterRoom.value = ''
  fetchList()
}

/**
 * 审核申请（通过/驳回）
 */
const audit = async (row, newStatus) => {
  const action = newStatus === '已通过' ? '通过' : '驳回'
  const confirmMsg = newStatus === '已通过' 
    ? `确定${action}该申请吗？` 
    : `确定${action}该申请吗？驳回后申请人将收到通知。`
  
  ElMessageBox.confirm(confirmMsg, '提示', { type: 'warning' }).then(async () => {
    try {
      const result = await applyStore.auditApply(row.apply_id, newStatus)
      if (result) {
        ElMessage.success(`已${action}申请`)
        await fetchList()
      } else {
        ElMessage.error(`${action}申请失败`)
      }
    } catch (error) {
      ElMessage.error(`${action}申请失败`)
      console.error(`${action}申请失败:`, error)
    }
  })
}

/**
 * 撤销已通过的申请（有 room:audit 权限的人均可操作）
 */
const revoke = async (row) => {
  ElMessageBox.confirm(
    `确定撤销该申请吗？撤销后状态将变为「已驳回」，申请人将收到通知。`,
    '撤销确认',
    { 
      type: 'warning',
      confirmButtonText: '确认撤销',
      cancelButtonText: '取消'
    }
  ).then(async () => {
    try {
      const result = await applyStore.revokeApply(row.apply_id)
      if (result) {
        ElMessage.success('已撤销申请')
        await fetchList()
      } else {
        ElMessage.error('撤销失败，请确认您有权限撤销该会议室的申请')
      }
    } catch (error) {
      ElMessage.error('撤销失败')
      console.error('撤销失败:', error)
    }
  })
}

// 应用铃铛下拉跳转携带的状态参数（?status=0 → 待审核），无参数则显示全部
const applyRouteStatus = () => {
  const status = route.query.status
  filterStatus.value = STATUS_TEXT_MAP[Number(status)] || ''
}

onMounted(async () => {
  applyRouteStatus()
  await applyStore.loadRooms()
  roomOptions.value = applyStore.getRooms()
  await fetchList()
})

// 已在审核页时再次从铃铛点击，query 变化需同步筛选条件
watch(() => route.query.status, () => {
  applyRouteStatus()
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

.filter-card, .table-card {
  background-color: #ffffff;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.filter-form .el-form-item {
  margin-bottom: 0;
}

:deep(.el-card__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 15px 20px;
  font-weight: 500;
}

:deep(.el-table .cell) {
  padding: 8px 0;
}
</style>