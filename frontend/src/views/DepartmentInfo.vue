<template>
  <div class="page-container">
    <div class="page-header">
      <h2>部门信息</h2>
    </div>

    <el-card class="search-card" shadow="hover">
      <div style="display: flex; gap: 10px; align-items: center; flex-wrap: wrap;">
      <label>搜索部门信息</label>
      <el-input
        v-model="searchText"
        placeholder="请输入部门名称进行搜索"
        @input="onSearchInput"
        style="width: 220px;"
      />
      <el-button v-if="hasPermission('dept:add')" type="primary" @click="handleAdd">新增部门</el-button>
      </div>
    </el-card>

    <el-card class="table-card" shadow="hover" style="margin-top: 20px;">
    <el-table
      :data="tableData"
      style="width: 100%"
      border
    >
      <el-table-column prop="deptId" label="部门ID" width="100" />
      <el-table-column prop="deptName" label="部门名称" />
      <el-table-column prop="deptDesc" label="部门描述" />
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button v-if="hasPermission('dept:edit')" type="success" size="small" @click="handleEdit(scope.row)">修改</el-button>
          <el-button v-if="hasPermission('dept:delete')" type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="title" width="500px" draggable>
      <el-form :model="form" label-width="auto" style="max-width: 600px" :rules="rules">
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" @input="onFormInput" />
        </el-form-item>
        <el-form-item label="部门描述" prop="deptDesc">
          <el-input v-model="form.deptDesc" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitHandle()">
            提交
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deptApi } from '../api/index.js'

const tableData = ref([])
const searchText = ref('')
const dialogVisible = ref(false)
const form = ref({})
const title = ref('')

// 按钮权限控制
const actionCodes = JSON.parse(localStorage.getItem('userInfo') || '{}').actionCodes || []
const hasPermission = (code) => actionCodes.includes(code)

const rules = reactive({
  deptName: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度为 2-50 个字符', trigger: 'blur' },
  ],
  deptDesc: [
    { max: 200, message: '描述不能超过200个字符', trigger: 'blur' },
  ],
})

const getDepartmentData = async () => {
  try {
    const res = await deptApi.getAll()
    if (res.code === '200' && res.data) {
      tableData.value = res.data
    } else {
      tableData.value = []
    }
  } catch (err) {
    console.error('加载部门数据失败:', err)
    ElMessage.error('加载部门数据失败')
    tableData.value = []
  }
}

const handleSearch = async () => {
  try {
    const res = await deptApi.getAll()
    if (res.code === '200' && res.data) {
      let result = res.data
      if (searchText.value) {
        result = result.filter(item =>
          item.deptName?.includes(searchText.value) ||
          item.deptDesc?.includes(searchText.value)
        )
      }
      tableData.value = result
    } else {
      tableData.value = []
    }
  } catch (err) {
    console.error('搜索部门失败:', err)
    ElMessage.error('搜索部门失败')
  }
}

const onSearchInput = (val) => {
  if (val && val.length > 20) {
    ElMessage.warning('部门名称不能超过20个字符')
  }
  handleSearch()
}

const onFormInput = (val) => {
  if (val && val.length > 20) {
    ElMessage.warning('部门名称不能超过20个字符')
  }
}

const handleAdd = () => {
  dialogVisible.value = true
  title.value = '新增部门'
  form.value = {}
}

const handleEdit = (row) => {
  form.value = { ...row }
  dialogVisible.value = true
  title.value = '修改部门'
}

const submitHandle = async () => {
  try {
    let res
    if (title.value === '新增部门') {
      // 校验部门名称字数（不得超过20个字符）
      if (form.value.deptName && form.value.deptName.length > 20) {
        ElMessage.warning('部门名称不能超过20个字符')
        return
      }
      // 新增前检查部门名称是否已存在
      const allRes = await deptApi.getAll()
      if (allRes.code === '200' && allRes.data) {
        const exists = allRes.data.some(item => item.deptName === form.value.deptName)
        if (exists) {
          ElMessage.warning('该部门已存在，请勿重复添加')
          return
        }
      }
      res = await deptApi.create(form.value)
    } else {
      // 校验部门名称字数（不得超过20个字符）
      if (form.value.deptName && form.value.deptName.length > 20) {
        ElMessage.warning('部门名称不能超过20个字符')
        return
      }
      // 修改时检查部门名称是否与其他部门重名（排除自身）
      const allRes = await deptApi.getAll()
      if (allRes.code === '200' && allRes.data) {
        const exists = allRes.data.some(item =>
          item.deptName === form.value.deptName && item.deptId !== form.value.deptId
        )
        if (exists) {
          ElMessage.warning('该部门名称已被使用，请更换')
          return
        }
      }
      res = await deptApi.update(form.value)
    }

    if (res.code === '200') {
      ElMessage.success(title.value === '新增部门' ? '添加成功' : '修改成功')
      dialogVisible.value = false
      form.value = {}
      getDepartmentData()
    } else {
      ElMessage.error(title.value === '新增部门' ? '添加失败' : '修改失败')
    }
  } catch (err) {
    console.error('提交失败:', err)
    ElMessage.error('操作失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除部门「${row.deptName}」吗？删除后不可恢复`,
    '删除确认',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deptApi.delete(row.deptId)
      if (res.code === '200') {
        ElMessage.success('删除成功')
        getDepartmentData()
      } else {
        ElMessage.error('删除失败')
      }
    } catch (err) {
      console.error('删除失败:', err)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  getDepartmentData()
})
</script>

<style scoped>
.department-info {
  padding: 20px;
}

.search-container {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.search-container label {
  margin-right: 10px;
  font-size: 14px;
  color: #606266;
}

.search-container .el-input {
  width: 400px;
}

.el-table {
  margin-top: 10px;
}

.el-button {
  margin-right: 5px;
}

.dialog-footer {
  text-align: right;
}
</style>