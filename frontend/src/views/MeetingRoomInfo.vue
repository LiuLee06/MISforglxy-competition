<template>
  <div class="page-container">
    <div class="page-header">
      <h2>会议室信息管理</h2>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="hover">
      <el-form :inline="true" class="search-form">
        <el-form-item label="会议室名称">
          <el-input 
            v-model="searchKeyword" 
            placeholder="输入会议室名称搜索" 
            clearable
            @input="handleSearch"
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchStatus" placeholder="全部" clearable @change="handleSearch" style="width: 130px">
            <el-option label="可用" value="可用" />
            <el-option label="维修中" value="维修中" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="success" @click="openAddDialog" v-if="hasPermission('room:add')">+ 新增会议室</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 会议室列表 -->
    <el-card class="table-card" shadow="hover" style="margin-top: 20px;">
      <template #header>
        <span style="font-weight: bold;">会议室列表</span>
        <span style="margin-left: 20px; color: #909399; font-size: 14px;">
          共 {{ filteredRooms.length }} 间
        </span>
      </template>
      <el-table :data="filteredRooms" border stripe v-loading="loading">
        
        <el-table-column prop="roomName" label="会议室名称" width="150" />
        <el-table-column prop="capacity" label="容纳人数" width="100" />
        <el-table-column prop="roomStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.roomStatus === '可用' ? 'success' : 'danger'">
              {{ row.roomStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDialog(row)" v-if="hasPermission('room:edit')">编辑</el-button>
            <el-button link type="danger" @click="deleteRoom(row)" v-if="hasPermission('room:delete')">删除</el-button>
            <el-button link type="info" @click="viewRoomDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="450px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="会议室名称" prop="roomName">
          <el-input v-model="form.roomName" placeholder="请输入会议室名称" />
        </el-form-item>
        <el-form-item label="容纳人数" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" :max="500" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="状态" prop="roomStatus">
          <el-radio-group v-model="form.roomStatus">
            <el-radio label="可用">可用</el-radio>
            <el-radio label="维修中">维修中</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="设备信息" prop="equipment">
          <el-input 
            v-model="form.equipment" 
            type="textarea" 
            rows="2" 
            placeholder="如：投影仪、音响、白板等（选填）" 
          />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" placeholder="如：行政楼3楼（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRoom">保存</el-button>
      </template>
    </el-dialog>

    <!-- 会议室详情弹窗 -->
    <el-dialog v-model="detailVisible" title="会议室详情" width="450px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="会议室编号">{{ detailRoom.roomId }}</el-descriptions-item>
        <el-descriptions-item label="会议室名称">{{ detailRoom.roomName }}</el-descriptions-item>
        <el-descriptions-item label="容纳人数">{{ detailRoom.capacity }} 人</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailRoom.roomStatus === '可用' ? 'success' : 'danger'">
            {{ detailRoom.roomStatus }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="设备信息">{{ detailRoom.equipment || '无' }}</el-descriptions-item>
        <el-descriptions-item label="位置">{{ detailRoom.location || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { roomApi } from '../api/index'

// 从localStorage获取用户信息
const getUserInfo = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  return userInfoStr ? JSON.parse(userInfoStr) : {}
}

// 当前用户的操作权限列表
const actionCodes = computed(() => {
  const userInfo = getUserInfo()
  const codes = userInfo.actionCodes
  return Array.isArray(codes) ? codes : []
})

// 检查是否有指定权限
const hasPermission = (code) => {
  return actionCodes.value.includes(code)
}

const loading = ref(false)

const searchKeyword = ref('')
const searchStatus = ref('')

const roomList = ref([])

const filteredRooms = computed(() => {
  let list = [...roomList.value]
  
  if (searchKeyword.value) {
    list = list.filter(r => (r.roomName || '').includes(searchKeyword.value))
  }
  
  if (searchStatus.value) {
    list = list.filter(r => r.roomStatus === searchStatus.value)
  }
  
  return list
})

const getRoomData = async () => {
  loading.value = true
  try {
    const res = await roomApi.getAll()
    if (res.code === '200' && res.data) {
      roomList.value = res.data.map(room => ({
        ...room,
        roomStatus: room.roomStatus == 1 ? '可用' : '维修中'
      }))
    } else {
      roomList.value = []
    }
  } catch (err) {
    console.error('加载会议室数据失败:', err)
    ElMessage.error('加载会议室数据失败')
    roomList.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getRoomData()
})

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增会议室')
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  roomId: '',
  roomName: '',
  capacity: 30,
  roomStatus: '可用',
  equipment: '',
  location: ''
})

const rules = {
  roomName: [{ required: true, message: '请输入会议室名称', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容纳人数', trigger: 'blur' }]
}

const detailVisible = ref(false)
const detailRoom = ref({})

const handleSearch = () => {
}

const resetSearch = () => {
  searchKeyword.value = ''
  searchStatus.value = ''
}

const openAddDialog = () => {
  dialogTitle.value = '新增会议室'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  dialogTitle.value = '编辑会议室'
  isEdit.value = true
  Object.assign(form, {
    roomId: row.roomId || '',
    roomName: row.roomName || '',
    capacity: row.capacity || 30,
    roomStatus: row.roomStatus || '可用',
    equipment: row.equipment || '',
    location: row.location || ''
  })
  dialogVisible.value = true
}

const resetForm = () => {
  form.roomId = ''
  form.roomName = ''
  form.capacity = 30
  form.roomStatus = '可用'
  form.equipment = ''
  form.location = ''
  formRef.value?.clearValidate()
}

const saveRoom = async () => {
  await formRef.value.validate()
  
  try {
    const roomData = {
      roomId: form.roomId,
      roomName: form.roomName,
      capacity: form.capacity,
      roomStatus: form.roomStatus === '可用' ? 1 : 0,
      equipment: form.equipment,
      location: form.location
    }
    
    if (isEdit.value) {
      const res = await roomApi.update(roomData)
      if (res.code === '200') {
        ElMessage.success('修改成功')
        getRoomData()
      } else {
        ElMessage.error('修改失败')
      }
    } else {
      const res = await roomApi.create(roomData)
      if (res.code === '200') {
        ElMessage.success('新增成功')
        getRoomData()
      } else {
        ElMessage.error('新增失败')
      }
    }
    
    dialogVisible.value = false
    resetForm()
  } catch (err) {
    console.error('保存会议室失败:', err)
    ElMessage.error('保存会议室失败')
  }
}

const deleteRoom = (row) => {
  ElMessageBox.confirm(`确定要删除会议室 "${row.roomName}" 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        const res = await roomApi.delete(row.roomId)
        if (res.code === '200') {
          ElMessage.success('删除成功')
          getRoomData()
        } else {
          ElMessage.error('删除失败')
        }
      } catch (err) {
        console.error('删除会议室失败:', err)
        ElMessage.error('删除会议室失败')
      }
    })
    .catch(() => {})
}

const viewRoomDetail = (row) => {
  detailRoom.value = { ...row }
  detailVisible.value = true
}
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

.search-card, .table-card {
  background-color: #ffffff;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.search-form .el-form-item {
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
