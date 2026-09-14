<template>
  <div class="page-container">
    <div class="page-header">
      <h2>教学计划安排</h2>
    </div>

    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="hover">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="教师姓名">
          <el-input v-model="searchForm.teacherName" placeholder="请输入教师姓名" clearable />
        </el-form-item>
        <el-form-item label="课程名称">
          <el-input v-model="searchForm.courseName" placeholder="请输入课程名称" clearable />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="searchForm.major" placeholder="请输入专业" clearable />
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="searchForm.semesterId" placeholder="全部学期" clearable style="width: 180px">
            <el-option
              v-for="semester in semesterList"
              :key="semester.semesterId"
              :label="semester.semesterName"
              :value="semester.semesterId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="确认状态">
          <el-select v-model="searchForm.confirmStatus" placeholder="全部" clearable style="width: 140px">
            <el-option label="全部" value="" />
            <el-option label="待确认" value="0" />
            <el-option label="已确认" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span>教学计划列表</span>
          <div>
            <el-button v-if="hasPermission('plan:delete')" type="danger" @click="deleteSemesterData">按学期批量删除</el-button>
            <el-button v-if="hasPermission('plan:import')" type="success" @click="openImportDialog">导入 CSV</el-button>
            <el-button type="warning" @click="handleExport">导出 CSV</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" border stripe style="width: 100%" v-loading="loading" max-height="600">
        <el-table-column prop="noticeId" label="通知单编号" min-width="180" show-overflow-tooltip fixed />
        <el-table-column prop="courseCode" label="课程编号" width="100" />
        <el-table-column prop="courseName" label="课程名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="授课教师" width="100" />
        <el-table-column prop="teacherId" label="教工号" width="110" />
        <el-table-column prop="teacherTitle" label="教师职称" width="100" />
        <el-table-column prop="className" label="班级名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="courseSeq" label="课序号" width="90" />
        <el-table-column prop="courseCategory" label="课程类别" width="100" />
        <el-table-column prop="courseNature" label="课程性质" width="100" />
        <el-table-column prop="courseAttribute" label="课程属性" width="100" />
        <el-table-column prop="isDegreeCourse" label="是否学位课" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isDegreeCourse === 1 ? 'success' : 'info'">
              {{ row.isDegreeCourse === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="credit" label="学分" width="70" />
        <el-table-column prop="offeringDepartment" label="开课单位" min-width="140" show-overflow-tooltip />
        <el-table-column prop="campus" label="校区" width="100" />
        <el-table-column prop="groupName" label="分组名" width="100" />
        <el-table-column prop="planHourUnit" label="计划学时单位" width="110" />
        <el-table-column prop="totalPlanHours" label="计划学时" width="100" />
        <el-table-column prop="lectureHours" label="讲课学时" width="90" />
        <el-table-column prop="labHours" label="上机学时" width="90" />
        <el-table-column prop="experimentHours" label="实验学时" width="90" />
        <el-table-column prop="otherHours" label="其他学时" width="90" />
        <el-table-column prop="arrangedHours" label="安排学时" width="100" />
        <el-table-column prop="scheduledHours" label="已排学时" width="90" />
        <el-table-column prop="weeklyHours" label="周学时" width="80" />
        <el-table-column prop="lectureWeeks" label="讲课周次" min-width="120" show-overflow-tooltip />
        <el-table-column prop="labWeeks" label="上机周次" min-width="120" show-overflow-tooltip />
        <el-table-column prop="experimentWeeks" label="实验周次" min-width="120" show-overflow-tooltip />
        <el-table-column prop="practiceWeeks" label="实践周次" min-width="120" show-overflow-tooltip />
        <el-table-column prop="practiceWeeksCount" label="实践周" width="90" />
        <el-table-column prop="otherWeeks" label="其他周次" min-width="120" show-overflow-tooltip />
        <el-table-column prop="selectedStudentCount" label="选课人数" width="90" />
        <el-table-column prop="scheduledStudentCount" label="排课人数" width="90" />
        <el-table-column prop="actualClassSize" label="班级实际人数" width="110" />
        <el-table-column prop="functionalArea" label="功能区" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="100" fixed="right">
          <template #default="{ row }">
            <el-tag :type="Number(row.confirmStatus) === 1 ? 'success' : 'warning'">
              {{ Number(row.confirmStatus) === 1 ? '已确认' : '待确认' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="importDialogVisible"
      title="导入教学计划"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form :model="importForm" label-width="90px">
        <el-form-item label="学期" required>
          <el-select v-model="importForm.semesterId" placeholder="请选择学期" style="width: 100%">
            <el-option
              v-for="semester in semesterList"
              :key="semester.semesterId"
              :label="semester.semesterName"
              :value="semester.semesterId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="专业" required>
          <el-input v-model="importForm.major" placeholder="请输入专业简称，例如：计科" />
        </el-form-item>
        <el-form-item label="CSV 文件" required>
          <el-upload
            ref="uploadRef"
            accept=".csv"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="upload-tip">请使用 CSV UTF-8 格式，缺少的字段将按空值导入。</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <el-alert
        v-if="importRows.length"
        :title="`已读取 ${importRows.length} 条记录，导入后均为待确认状态`"
        type="success"
        :closable="false"
      />

      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button v-if="hasPermission('plan:import')" type="primary" :loading="importing" @click="confirmImport">确认导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { semesterApi, teachingScheduleApi } from '../api/index'

// 操作权限码（登录时由后端返回）
const actionCodes = JSON.parse(localStorage.getItem('userInfo') || '{}').actionCodes || []
const hasPermission = (code) => actionCodes.includes(code)

const searchForm = reactive({
  teacherName: '',
  courseName: '',
  major: '',
  semesterId: '',
  confirmStatus: ''
})

const tableData = ref([])
const semesterList = ref([])
const loading = ref(false)
const importDialogVisible = ref(false)
const importing = ref(false)
const importRows = ref([])
const uploadRef = ref()

const importForm = reactive({
  semesterId: '',
  major: ''
})

const csvFields = [
  { label: '通知单编号', prop: 'noticeId' },
  { label: '课程编号', prop: 'courseCode' },
  { label: '课程名称', prop: 'courseName' },
  { label: '授课教师', prop: 'teacherName' },
  { label: '班级名称', prop: 'className' },
  { label: '课序号', prop: 'courseSeq' },
  { label: '选课人数', prop: 'selectedStudentCount' },
  { label: '学分', prop: 'credit' },
  { label: '已排学时', prop: 'scheduledHours' },
  { label: '开课单位', prop: 'offeringDepartment' },
  { label: '教师职称', prop: 'teacherTitle' },
  { label: '课程类别', prop: 'courseCategory' },
  { label: '课程性质', prop: 'courseNature' },
  { label: '课程属性', prop: 'courseAttribute' },
  { label: '校区', prop: 'campus' },
  { label: '教工号', prop: 'teacherId' },
  { label: '计划学时单位', prop: 'planHourUnit' },
  { label: '分组名', prop: 'groupName' },
  { label: '排课人数', prop: 'scheduledStudentCount' },
  { label: '班级实际人数', prop: 'actualClassSize' },
  { label: '是否学位课', prop: 'isDegreeCourse' },
  { label: '讲课周次', prop: 'lectureWeeks' },
  { label: '上机周次', prop: 'labWeeks' },
  { label: '计划学时', prop: 'totalPlanHours' },
  { label: '实验周次', prop: 'experimentWeeks' },
  { label: '实践周次', prop: 'practiceWeeks' },
  { label: '其他周次', prop: 'otherWeeks' },
  { label: '讲课学时', prop: 'lectureHours' },
  { label: '上机学时', prop: 'labHours' },
  { label: '实验学时', prop: 'experimentHours' },
  { label: '实践周', prop: 'practiceWeeksCount' },
  { label: '安排学时', prop: 'arrangedHours' },
  { label: '其他学时', prop: 'otherHours' },
  { label: '周学时', prop: 'weeklyHours' },
  { label: '功能区', prop: 'functionalArea' }
]

const fetchSemesters = async () => {
  try {
    const res = await semesterApi.getAll()
    if (res.code === '200' && res.data) {
      semesterList.value = res.data
    }
  } catch (error) {
    console.error('获取学期列表失败:', error)
    ElMessage.error('获取学期列表失败')
  }
}

const handleSearch = async () => {
  loading.value = true

  try {
    const params = {}
    if (searchForm.teacherName) params.teacherName = searchForm.teacherName
    if (searchForm.courseName) params.courseName = searchForm.courseName
    if (searchForm.major) params.major = searchForm.major
    if (searchForm.semesterId) params.semesterId = searchForm.semesterId
    if (searchForm.confirmStatus) params.confirmStatus = searchForm.confirmStatus

    const res = await teachingScheduleApi.getByConditions(params)
    if (res.code === '200' && res.data) {
      tableData.value = res.data
    } else {
      tableData.value = []
    }
  } catch (error) {
    console.error('获取教学计划列表失败:', error)
    ElMessage.error('获取教学计划列表失败')
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchForm.teacherName = ''
  searchForm.courseName = ''
  searchForm.major = ''
  searchForm.semesterId = ''
  searchForm.confirmStatus = ''
  handleSearch()
}

const openImportDialog = () => {
  importForm.semesterId = searchForm.semesterId || ''
  importForm.major = searchForm.major || ''
  importRows.value = []
  uploadRef.value?.clearFiles()
  importDialogVisible.value = true
}

const deleteSemesterData = async () => {
  if (!searchForm.semesterId) {
    ElMessage.warning('请选择要删除的学期')
    return
  }
  const semester = semesterList.value.find(item => String(item.semesterId) === String(searchForm.semesterId))
  const semesterName = semester?.semesterName || `学期 ${searchForm.semesterId}`
  try {
    await ElMessageBox.confirm(
      `将删除 TEACHING_SCHEDULE 表中“${semesterName}”的全部记录，已确认和未确认记录都会删除，且不可恢复。是否继续？`,
      '确认批量删除',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
    const res = await teachingScheduleApi.deleteBySemester(searchForm.semesterId)
    if (res.code === '200') {
      ElMessage.success(`已删除 ${res.data || 0} 条教学计划记录`)
      await handleSearch()
    } else {
      ElMessage.error(res.msg || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('批量删除失败')
    }
  }
}

const parseCsv = (text, separator) => {
  const rows = []
  let row = []
  let value = ''
  let inQuotes = false

  for (let i = 0; i < text.length; i++) {
    const char = text[i]
    if (char === '"') {
      if (inQuotes && text[i + 1] === '"') {
        value += '"'
        i++
      } else {
        inQuotes = !inQuotes
      }
    } else if (char === separator && !inQuotes) {
      row.push(value)
      value = ''
    } else if ((char === '\r' || char === '\n') && !inQuotes) {
      if (char === '\r' && text[i + 1] === '\n') {
        i++
      }
      row.push(value)
      if (row.some(item => item.trim())) {
        rows.push(row)
      }
      row = []
      value = ''
    } else {
      value += char
    }
  }

  row.push(value)
  if (row.some(item => item.trim())) {
    rows.push(row)
  }
  return rows
}

const handleFileChange = (file) => {
  if (!file.name.toLowerCase().endsWith('.csv')) {
    ElMessage.error('请选择 CSV 文件')
    uploadRef.value?.clearFiles()
    return
  }

  const reader = new FileReader()
  reader.onload = (event) => {
    const text = String(event.target.result || '').replace(/^\uFEFF/, '')
    const firstLine = text.split(/\r?\n/, 1)[0]
    const separator = firstLine.includes('\t') ? '\t' : ','
    const csvRows = parseCsv(text, separator)
    if (csvRows.length < 2) {
      importRows.value = []
      ElMessage.error('CSV 文件中没有可导入的数据')
      return
    }

    const headers = csvRows[0].map(header => header.trim())
    const headerIndexes = {}
    headers.forEach((header, index) => {
      headerIndexes[header] = index
    })

    importRows.value = csvRows.slice(1).map(values => {
      const row = {}
      csvFields.forEach(field => {
        const index = headerIndexes[field.label]
        row[field.prop] = index === undefined ? '' : (values[index] || '').trim()
      })

      row.noticeId = row.noticeId.replace(/^="(.*)"$/, '$1')
      const degreeValue = row.isDegreeCourse
      row.isDegreeCourse = degreeValue === '' ? null : (degreeValue === '是' || degreeValue === '1' ? 1 : 0)
      return row
    })
  }
  reader.onerror = () => {
    importRows.value = []
    ElMessage.error('读取 CSV 文件失败')
  }
  reader.readAsText(file.raw, 'UTF-8')
}

const handleFileRemove = () => {
  importRows.value = []
}

const confirmImport = async () => {
  if (!importForm.semesterId) {
    ElMessage.warning('请选择学期')
    return
  }
  if (!importForm.major.trim()) {
    ElMessage.warning('请输入专业简称')
    return
  }
  if (!importRows.value.length) {
    ElMessage.warning('请选择需要导入的 CSV 文件')
    return
  }

  importing.value = true
  try {
    const data = importRows.value.map(row => ({
      ...row,
      semesterId: importForm.semesterId,
      major: importForm.major.trim(),
      confirmStatus: 0
    }))
    const res = await teachingScheduleApi.batchImport(data)
    if (res.code === '200') {
      ElMessage.success(`成功导入 ${data.length} 条教学计划`)
      importDialogVisible.value = false
      handleSearch()
    } else {
      ElMessage.error(res.msg || '导入失败')
    }
  } catch (error) {
    console.error('导入教学计划失败:', error)
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
}

const escapeCsvValue = (value) => {
  const text = value === null || value === undefined ? '' : String(value)
  return `"${text.replace(/"/g, '""')}"`
}

const handleExport = () => {
  if (!tableData.value.length) {
    ElMessage.warning('当前没有可导出的教学计划')
    return
  }

  const headers = csvFields.map(field => escapeCsvValue(field.label)).join(',')
  const rows = tableData.value.map(row => {
    return csvFields.map(field => {
      let value = row[field.prop]
      if (field.prop === 'isDegreeCourse') {
        value = value === 1 ? '是' : (value === 0 ? '否' : '')
      } else if (field.prop === 'noticeId' && value) {
        value = `="${value}"`
      }
      return escapeCsvValue(value)
    }).join(',')
  })

  const csvContent = [headers, ...rows].join('\r\n')
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `教学计划_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

onMounted(() => {
  fetchSemesters()
  handleSearch()
})
</script>

<style scoped>
.teaching-plan {
  padding: 0;
}

.search-card {
  margin-bottom: 16px;
}

.table-card {
  overflow: hidden;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.upload-tip {
  color: #909399;
}
</style>
