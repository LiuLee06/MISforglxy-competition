<template>
  <div class="page-container">
    <div class="page-header">
      <h2>教师角色分配</h2>
    </div>

    <el-card class="search-card" shadow="hover">
      <template #header>
        <span style="font-weight: bold;">查询条件</span>
      </template>
      <el-form :inline="true" class="search-form">
        <el-form-item label="角色名称">
          <el-select v-model="searchForm.roleId" placeholder="全部角色" clearable style="width: 150px;">
            <el-option v-for="r in roleList" :key="r.roleId" :label="r.roleName" :value="r.roleId" />
          </el-select>
        </el-form-item>
        <el-form-item label="教师姓名">
          <el-input v-model="searchForm.teacherName" placeholder="输入教师姓名搜索" clearable style="width: 200px;" @input="onTeacherNameInput" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchTeacherRoles">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="hover" style="margin-top: 20px;">
      <template #header>
        <span style="font-weight: bold;">教师角色列表</span>
        <el-button type="success" size="small" style="margin-left: 20px;" @click="openAssignDialog">分配角色</el-button>
      </template>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="teacherId" label="教师ID" width="100" />
        <el-table-column prop="teacherName" label="教师姓名" width="120" />
        <el-table-column prop="teacherTitle" label="职称" width="100" />
        <el-table-column prop="teacherDept" label="所属部门" width="150" />
        <el-table-column prop="roleId" label="角色ID" width="100" />
        <el-table-column prop="roleName" label="角色名称" width="120" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="openChangeRoleDialog(row)">更换角色</el-button>
            <el-button link type="danger" @click="removeRole(row)">取消分配</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination style="margin-top: 20px; text-align: right;" :current-page="pagination.pageNum" :page-size="pagination.pageSize" :total="pagination.total" layout="total, prev, pager, next" @current-change="handlePageChange" @size-change="handleSizeChange" />
      <el-empty v-if="!loading && tableData.length === 0" description="暂无角色分配数据" />
    </el-card>

    <el-dialog v-model="assignDialogVisible" :title="isChangeRole ? '更换角色' : '分配角色'" width="500px">
      <el-form :model="assignForm" label-width="100px">
        <el-form-item label="选择教师" required>
          <el-select v-model="assignForm.teacherId" placeholder="请选择教师" style="width: 100%;" @change="onTeacherSelect">
            <el-option v-for="t in teacherList" :key="t.teacherId" :label="t.name || t.teacherName" :value="t.teacherId" />
          </el-select>
        </el-form-item>
        <el-form-item label="教师信息">
          <div style="color: #606266;">
            <div>姓名：{{ selectedTeacher.name || selectedTeacher.teacherName || '-' }}</div>
            <div>职称：{{ selectedTeacher.title || selectedTeacher.professionalTitle || '-' }}</div>
            <div>部门：{{ selectedTeacher.dept || '-' }}</div>
          </div>
        </el-form-item>
        <el-form-item label="选择角色" required>
          <el-select v-model="assignForm.roleId" placeholder="请选择角色" style="width: 100%;">
            <el-option v-for="r in roleList" :key="r.roleId" :label="r.roleName" :value="r.roleId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAssignment">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { teacherRoleApi, roleApi, teacherApi } from '../api/index.js'

const loading = ref(false)
const assignDialogVisible = ref(false)
const isChangeRole = ref(false)

const searchForm = reactive({
  roleId: '',
  teacherName: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])
const roleList = ref([])
const teacherList = ref([])

const assignForm = reactive({
  teacherId: null,
  roleId: null,
  oldRoleId: null
})

const selectedTeacher = ref({})

const fetchTeacherRoles = async () => {
  loading.value = true
  try {
    const res = await teacherRoleApi.getAll()
    if (res.code === '200' && res.data) {
      let data = res.data
      if (data.records) {
        data = data.records
      }
      if (Array.isArray(data)) {
        let filtered = data.map(item => ({
          ...item,
          teacherId: parseInt(item.teacherId) || item.teacherId,
          teacherName: item.teacher ? (item.teacher.name || item.teacher.teacherName) : '',
          teacherTitle: item.teacher ? (item.teacher.title || item.teacher.professionalTitle) : '',
          teacherDept: item.teacher ? (item.teacher.dept || '') : '',
          roleName: item.role ? (item.role.roleName || '') : '',
          createTime: item.createTime ? formatDateTime(item.createTime) : ''
        }))
        
        if (searchForm.roleId) {
          const roleId = parseInt(searchForm.roleId)
          filtered = filtered.filter(item => parseInt(item.roleId) === roleId)
        }
        
        if (searchForm.teacherName) {
          filtered = filtered.filter(item => 
            item.teacherName && item.teacherName.includes(searchForm.teacherName)
          )
        }
        
        pagination.total = filtered.length
        const start = (pagination.pageNum - 1) * pagination.pageSize
        tableData.value = filtered.slice(start, start + pagination.pageSize)
      } else {
        tableData.value = []
        pagination.total = 0
      }
    } else {
      tableData.value = []
      pagination.total = 0
    }
  } catch (error) {
    ElMessage.error('加载教师角色数据失败')
    console.error('加载教师角色数据失败:', error)
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const fetchRoles = async () => {
  try {
    const res = await roleApi.getAll()
    if (res.code === '200' && res.data) {
      roleList.value = res.data
    }
  } catch (error) {
    console.error('加载角色列表失败:', error)
  }
}

const fetchTeachers = async () => {
  try {
    const res = await teacherApi.getAll()
    if (res.code === '200' && res.data) {
      teacherList.value = res.data
    }
  } catch (error) {
    console.error('加载教师列表失败:', error)
  }
}

const resetSearch = () => {
  searchForm.roleId = ''
  searchForm.teacherName = ''
  pagination.pageNum = 1
  fetchTeacherRoles()
}

const onTeacherNameInput = (val) => {
  if (val && val.length > 10) {
    ElMessage.warning('教师姓名不能超过10个字符')
  }
}

const handlePageChange = (page) => {
  pagination.pageNum = page
  fetchTeacherRoles()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchTeacherRoles()
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  if (typeof dateTime === 'string') {
    return dateTime.replace('T', ' ').substring(0, 19)
  }
  return dateTime
}

const onTeacherSelect = (teacherId) => {
  selectedTeacher.value = teacherList.value.find(t => t.teacherId === teacherId) || {}
}

const openAssignDialog = () => {
  isChangeRole.value = false
  assignForm.teacherId = null
  assignForm.roleId = null
  assignForm.oldRoleId = null
  selectedTeacher.value = {}
  assignDialogVisible.value = true
}

const openChangeRoleDialog = (row) => {
  isChangeRole.value = true
  assignForm.teacherId = row.teacherId
  assignForm.roleId = null
  assignForm.oldRoleId = row.roleId
  selectedTeacher.value = {
    name: row.teacherName,
    title: row.teacherTitle,
    dept: row.teacherDept
  }
  assignDialogVisible.value = true
}

const saveAssignment = async () => {
  if (!assignForm.teacherId) {
    ElMessage.warning('请选择教师')
    return
  }
  if (!assignForm.roleId) {
    ElMessage.warning('请选择角色')
    return
  }

  // 分配或更换角色前，检查该教师是否已拥有目标角色
  try {
    const allRes = await teacherRoleApi.getAll()
    if (allRes.code === '200' && allRes.data) {
      let data = allRes.data
      if (data.records) data = data.records
      if (Array.isArray(data)) {
        const exists = data.some(item =>
          parseInt(item.teacherId) === parseInt(assignForm.teacherId) &&
          parseInt(item.roleId) === parseInt(assignForm.roleId)
        )
        if (exists) {
          if (isChangeRole.value) {
            ElMessage.warning('该教师已拥有此角色，请勿重复更换')
          } else {
            ElMessage.warning('该教师已分配此角色，请勿重复分配')
          }
          return
        }
      }
    }
  } catch (err) {
    console.error('检查角色分配失败:', err)
  }

  try {
    let res
    if (isChangeRole.value) {
      // 更换角色前检查目标角色是否与当前角色相同
      if (parseInt(assignForm.roleId) === parseInt(assignForm.oldRoleId)) {
        ElMessage.warning('新角色与当前角色相同，无需更换')
        return
      }
      const data = {
        teacherId: assignForm.teacherId,
        roleId: assignForm.roleId,
        oldRoleId: assignForm.oldRoleId
      }
      res = await teacherRoleApi.updateRole(data)
    } else {
      const data = {
        teacherId: assignForm.teacherId,
        roleId: assignForm.roleId
      }
      res = await teacherRoleApi.create(data)
    }
    
    if (res.code === '200') {
      ElMessage.success(isChangeRole.value ? '角色更换成功' : '角色分配成功')
      assignDialogVisible.value = false
      fetchTeacherRoles()
    } else {
      ElMessage.error(isChangeRole.value ? '角色更换失败' : '角色分配失败')
    }
  } catch (error) {
    ElMessage.error(isChangeRole.value ? '角色更换失败' : '角色分配失败')
    console.error('保存角色分配失败:', error)
  }
}

const removeRole = (row) => {
  ElMessageBox.confirm('确定取消该教师的角色分配吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      const res = await teacherRoleApi.delete(row.teacherId, row.roleId)
      if (res.code === '200') {
        ElMessage.success('角色分配已取消')
        fetchTeacherRoles()
      } else {
        ElMessage.error('取消角色分配失败')
      }
    } catch (error) {
      ElMessage.error('取消角色分配失败')
      console.error('取消角色分配失败:', error)
    }
  })
}

onMounted(async () => {
  await fetchRoles()
  await fetchTeachers()
  await fetchTeacherRoles()
})
</script>

<style scoped>
.page-container { padding: 20px; background-color: #ffffff; min-height: calc(100vh - 60px); }
.page-header { margin-bottom: 20px; }
.page-header h2 { color: #303133; font-weight: 600; }
.search-card { background-color: #ffffff; }
.search-form { display: flex; flex-wrap: wrap; align-items: center; gap: 10px; }
.search-form .el-form-item { margin-bottom: 10px; }
.table-card { background-color: #ffffff; }
:deep(.el-table .cell) { padding: 8px 0; }
:deep(.el-card__header) { border-bottom: 1px solid #ebeef5; padding: 15px 20px; font-weight: 500; }
</style>
