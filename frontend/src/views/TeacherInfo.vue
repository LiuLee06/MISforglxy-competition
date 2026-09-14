<template>
  <div class="page-container">
    <div class="page-header">
      <h2>教师信息管理</h2>
    </div>

    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="hover">
      <div style="display: flex; gap: 10px; align-items: center; flex-wrap: wrap;">
      <el-input
        v-model="searchKey"
        placeholder="请输入工号/姓名/手机号搜索"
        clearable
        style="width: 320px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button type="success" @click="openDialog()" v-if="canAddTeacher">新增教师</el-button>
      <el-switch v-model="hideRetired" active-text="隐藏退休教师" inactive-text="显示退休教师" style="margin-left: 16px;" />
      </div>
    </el-card>

    <!-- 教师数据表格 -->
    <el-card class="table-card" shadow="hover" style="margin-top: 20px;">
    <el-table :data="teacherList" border style="width: 100%;" v-loading="loading">
      <el-table-column prop="staffNo" label="工号" width="120" />
      <el-table-column prop="name" label="教师姓名" width="120" />
      <el-table-column prop="professionalTitle" label="职称" width="120" />
      <el-table-column prop="position" label="职务" width="120" />
      <el-table-column prop="birth" label="出生日期" width="120" />
      <el-table-column prop="political" label="政治面貌" width="120" />
      <el-table-column prop="phone" label="联系电话" width="150" />
      <el-table-column prop="officePhone" label="办公电话" width="150" />
      <el-table-column label="所属部门" width="150">
        <template #default="{ row }">
          <span>{{ Array.isArray(row.dept) ? row.dept.join('、') : row.dept }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="education" label="学历" width="120" />
      <el-table-column prop="degree" label="学位" width="120" />
      <el-table-column prop="isRetired" label="是否退休" width="120">
        <template #default="{ row }">
          <span>{{ row.isRetired ? '是' : '否' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="isFullTime" label="是否专任" width="120">
        <template #default="{ row }">
          <span>{{ row.isFullTime ? '是' : '否' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
       <template #default="{ row }">
  <!-- 编辑按钮逻辑：
      1. 有teacher:edit权限：可以编辑所有人（院办主任、超级管理员、教学办等）
      2. 只有teacher:edit_self权限：只能编辑自己的信息
      3. 无任何编辑权限：不显示编辑按钮
  -->
  <el-button
    size="small"
    @click="openDialog(row)"
    v-if="
      canEditAllTeacher
      || (canEditSelf && row.teacherId === loginTeacherId)
    "
  >编辑</el-button>
  <el-button
    size="small"
    type="danger"
    @click="handleDelete(row)"
    v-if="canDeleteTeacher"
  >删除</el-button>
</template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <div class="pagination-container" style="display: flex; justify-content: center; margin-top: 20px;">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[5, 10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="教师信息编辑" width="650px">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="教师编号">
              <el-input v-model="form.teacherId" :disabled="isEdit"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="教师姓名">
              <el-input v-model="form.name" placeholder="请输入姓名"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="职称">
              <el-input v-model="form.professionalTitle" placeholder="请输入职称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职务">
              <el-input v-model="form.position" placeholder="请输入职务" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出生日期">
              <el-date-picker
                v-model="form.birth"
                type="month"
                value-format="YYYY.MM"
                placeholder="请选择日期"
                style="width: 100%;"
              ></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="政治面貌">
              <el-input v-model="form.political" placeholder="请输入政治面貌" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" placeholder="请输入联系电话"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="办公电话">
              <el-input v-model="form.officePhone" placeholder="请输入办公电话"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属部门">
              <el-select v-model="form.dept" multiple placeholder="请选择部门" style="width:100%" clearable collapse-tags>
                <el-option
                  v-for="dept in deptOptions"
                  :key="dept"
                  :label="dept"
                  :value="dept"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="!isSelfEditMode">
            <el-form-item label="登录密码">
              <el-input v-model="form.password" type="password" :placeholder="isEdit ? '不修改请留空' : '请输入密码'"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工号">
              <el-input v-model="form.staffNo" placeholder="请输入工号"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学历">
              <el-input v-model="form.education" placeholder="请输入学历"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学位">
              <el-input v-model="form.degree" placeholder="请输入学位"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否退休">
              <el-switch v-model="form.isRetired" active-text="已退休" inactive-text="未退休" :active-value="true" :inactive-value="false" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="是否专任">
              <el-switch v-model="form.isFullTime" active-text="专任" inactive-text="非专任" :active-value="true" :inactive-value="false" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTeacher">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { teacherApi, deptApi } from '../api/index'

// 从localStorage获取用户信息
const getUserInfo = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  return userInfoStr ? JSON.parse(userInfoStr) : {}
}

// 当前登录用户信息
const userInfo = computed(() => getUserInfo())

// 当前登录用户角色名称
const currentRole = computed(() => {
  const roleName = userInfo.value.roleName
  if (Array.isArray(roleName)) {
    return roleName[0] || '普通教师'
  }
  return roleName || '普通教师'
})

// 当前登录教师ID（普通教师用来判断只能编辑自己）
const loginTeacherId = computed(() => userInfo.value.userId || '')

// 当前登录教师所属部门/教研室（教研室角色过滤用）
const loginDeptName = computed(() => userInfo.value.dept || '')

// 当前用户的操作权限列表
const actionCodes = computed(() => {
  const codes = userInfo.value.actionCodes
  return Array.isArray(codes) ? codes : []
})

// 检查是否有指定权限
const hasPermission = (code) => {
  return actionCodes.value.includes(code)
}

// 权限计算 - 使用操作权限判断
const canAddTeacher = computed(() => hasPermission('teacher:add'))

const canDeleteTeacher = computed(() => hasPermission('teacher:delete'))

// 是否拥有全局编辑权限（可以编辑全部教师）
const canEditAllTeacher = computed(() => hasPermission('teacher:edit'))

// 个人信息修改：所有登录教师均可修改自己的信息
const canEditSelf = computed(() => true)

// 是否个人信息编辑模式（仅限：无全局编辑权限时编辑自己，此时隐藏密码框，改密码走个人中心）
const isSelfEditMode = computed(() =>
  isEdit.value &&
  !canEditAllTeacher.value &&
  String(form.teacherId) === String(loginTeacherId.value)
)
const loading = ref(false)

const teacherList = ref([])
const searchKey = ref('')
const hideRetired = ref(true)

const titleOptions = ref([])
const deptOptions = ref([])
const positionOptions = ref([])

const parseDept = (value) => {
  if (Array.isArray(value)) return value
  if (value === null || value === undefined) return []
  return String(value)
    .split(/[,、]/)
    .map(item => item.trim())
    .filter(Boolean)
}

const formatDept = (value) => {
  if (Array.isArray(value)) return value.join('、')
  return value || ''
}

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const getTeacherData = async () => {
  loading.value = true
  try {
    const keyword = searchKey.value.trim()
    let res

    if (keyword) {
      res = await teacherApi.getAll()
    } else {
      res = await teacherApi.getPage({
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        isRetired: hideRetired.value ? 'NO' : undefined
      })
    }

    if (res.code === '200') {
      const allRecords = (res.data?.records || res.data || []).map(r => ({
        ...r,
        staffNo: r.staffNo ?? r.staff_no ?? '',
        education: r.education ?? r.Education ?? '',
        degree: r.degree ?? r.Degree ?? '',
        professionalTitle: r.professionalTitle ?? r.professional_title ?? r.title ?? '',
        dept: parseDept(r.dept ?? r.deptName ?? r.dept_name ?? ''),
        position: r.position ?? '',
        isFullTime: r.isFullTime === 'YES' || r.isFullTime === 1 || r.isFullTime === '1' || r.is_full_time === 'YES' || r.is_full_time === 1 || r.is_full_time === '1',
        isRetired: r.isRetired === 'YES' || r.isRetired === 1 || r.isRetired === '1' || r.is_retired === 'YES' || r.is_retired === 1 || r.is_retired === '1'
      }))

      titleOptions.value = Array.from(new Set(allRecords.map(item => item.professionalTitle).filter(Boolean)))
      positionOptions.value = Array.from(new Set(allRecords.map(item => item.position).filter(Boolean)))

      const filteredByRetired = hideRetired.value ? allRecords.filter(item => !item.isRetired) : allRecords

      const keywordMatches = (item) => {
        if (!keyword) return true
        const staffNo = String(item.staffNo ?? '').toLowerCase()
        const name = String(item.name ?? '').toLowerCase()
        const phone = String(item.phone ?? '').toLowerCase()
        const lowerKeyword = keyword.toLowerCase()
        return staffNo.includes(lowerKeyword) || name.includes(lowerKeyword) || phone.includes(lowerKeyword)
      }

      const filteredRecords = filteredByRetired.filter(keywordMatches)

      if (keyword) {
        const start = (currentPage.value - 1) * pageSize.value
        const end = start + pageSize.value
        teacherList.value = filteredRecords.slice(start, end)
        total.value = filteredRecords.length
      } else {
        teacherList.value = filteredRecords.slice(0, pageSize.value)
        total.value = parseInt(res.data?.total) || filteredRecords.length
      }
    } else {
      teacherList.value = []
      total.value = 0
    }
  } catch (err) {
    console.error('加载教师数据失败:', err)
    ElMessage.error('加载教师数据失败')
    teacherList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const loadDeptOptions = async () => {
  try {
    const res = await deptApi.getAll()
    if (res.code === '200' && Array.isArray(res.data)) {
      deptOptions.value = res.data
        .map(d => d.deptName ?? d.dept_name ?? d.dept)
        .filter(Boolean)
    }
  } catch (err) {
    console.error('加载全部部门失败:', err)
  }
}

onMounted(() => {
  getTeacherData()
  loadDeptOptions()
  // 调试信息
  console.log('当前用户信息:', userInfo.value)
  console.log('操作权限列表:', actionCodes.value)
  console.log('canAddTeacher:', canAddTeacher.value)
  console.log('canEditAllTeacher:', canEditAllTeacher.value)
  console.log('canDeleteTeacher:', canDeleteTeacher.value)
})

const dialogVisible = ref(false)
const isEdit = ref(false)

const form = reactive({
  teacherId: '',
  name: '',
  professionalTitle: '',
  position: '',
  birth: '',
  political: '',
  phone: '',
  officePhone: '',
  password: '',
  originalPassword: '',
  dept: [],
  staffNo: '',
  education: '',
  degree: '',
  isFullTime: true,   // 默认：专任
  isRetired: false    // 默认：未退休
})

const resetForm = () => {
  form.teacherId = ''
  form.name = ''
  form.professionalTitle = ''
  form.position = ''
  form.birth = ''
  form.political = ''
  form.phone = ''
  form.officePhone = ''
  form.password = ''
  form.originalPassword = ''
  form.dept = []
  form.staffNo = ''
  form.education = ''
  form.degree = ''
  form.isFullTime = true
  form.isRetired = false
}

const openDialog = (row) => {
  dialogVisible.value = true
  if (row) {
    isEdit.value = true
    Object.assign(form, {
      teacherId: row.teacherId || '',
      name: row.name || '',
      professionalTitle: row.professionalTitle ?? row.professional_title ?? '',
      position: row.position || '',
      birth: row.birth || '',
      political: row.political || '',
      phone: row.phone || '',
      officePhone: row.officePhone || '',
      password: '',
      dept: parseDept(row.dept ?? row.deptName ?? row.dept_name ?? ''),
      staffNo: row.staffNo ?? row.staff_no ?? '',
      education: row.education ?? row.Education ?? '',
      degree: row.degree ?? row.Degree ?? '',
      isFullTime: row.isFullTime,
      isRetired: row.isRetired
    })
  } else {
    isEdit.value = false
    resetForm()
  }
}

const saveTeacher = async () => {
  if (!form.teacherId || !form.name) {
    ElMessage.warning('教师编号和姓名不能为空')
    return
  }

  try {
    const payload = {
      teacherId: form.teacherId,
      name: form.name,
      professionalTitle: form.professionalTitle,
      position: form.position,
      birth: form.birth,
      political: form.political,
      phone: form.phone,
      officePhone: form.officePhone,
      dept: Array.isArray(form.dept) ? form.dept.join(',') : form.dept,
      isFullTime: form.isFullTime ? 'YES' : 'NO',
      isRetired: form.isRetired ? 'YES' : 'NO',
      staffNo: form.staffNo,
      education: form.education,
      degree: form.degree,
      password: form.password
    }

    if (isEdit.value) {
      const res = await teacherApi.update(payload)
      if (res.code === '200') {
        ElMessage.success('编辑成功')
        // 如果当前登录用户是自己，更新 localStorage 中 userInfo
        if (String(payload.teacherId) === String(loginTeacherId.value)) {
          const storageInfoStr = localStorage.getItem('userInfo')
          if (storageInfoStr) {
            const storageInfo = JSON.parse(storageInfoStr)
            storageInfo.name = payload.name
            storageInfo.userName = payload.name
            storageInfo.phone = payload.phone || storageInfo.phone
            storageInfo.dept = payload.dept || storageInfo.dept
            storageInfo.staffNo = payload.staffNo || storageInfo.staffNo
            storageInfo.professionalTitle = payload.professionalTitle || storageInfo.professionalTitle
            storageInfo.position = payload.position || storageInfo.position
            storageInfo.birth = payload.birth || storageInfo.birth
            storageInfo.political = payload.political || storageInfo.political
            storageInfo.officePhone = payload.officePhone || storageInfo.officePhone
            storageInfo.education = payload.education || storageInfo.education
            storageInfo.degree = payload.degree || storageInfo.degree
            storageInfo.isFullTime = payload.isFullTime || storageInfo.isFullTime
            storageInfo.isRetired = payload.isRetired || storageInfo.isRetired
            localStorage.setItem('userInfo', JSON.stringify(storageInfo))
          }
        }
        getTeacherData()
      } else {
        ElMessage.error('编辑失败')
      }
    } else {
      if (!form.password) {
        ElMessage.warning('新增教师需要设置密码')
        return
      }
      const res = await teacherApi.create(payload)
      if (res.code === '200') {
        ElMessage.success('新增成功')
        getTeacherData()
      } else {
        ElMessage.error('新增失败')
      }
    }
    dialogVisible.value = false
  } catch (err) {
    console.error('保存教师失败:', err)
    ElMessage.error('保存教师失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定删除教师【${row.name}】吗？`,
    '删除提示',
    { type: 'warning' }
  )
  .then(async () => {
    try {
      const res = await teacherApi.delete(row.teacherId)
      if (res.code === '200') {
        ElMessage.success('删除成功')
        getTeacherData()
      } else {
        ElMessage.error('删除失败')
      }
    } catch (err) {
      console.error('删除教师失败:', err)
      ElMessage.error('删除教师失败')
    }
  })
  .catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 执行搜索
const handleSearch = () => {
  if (searchKey.value.length > 20) {
    ElMessageBox.alert('搜索内容不能超过20个字符', '提示', {
      type: 'warning'
    })
    return
  }

  currentPage.value = 1
  getTeacherData()
}

// 重置搜索
const resetSearch = () => {
  searchKey.value = ''
  currentPage.value = 1
  getTeacherData()
}

// 分页事件
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  getTeacherData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  getTeacherData()
}
</script>