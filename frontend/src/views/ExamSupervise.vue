<template>
  <div class="page-container">
    <div class="page-header">
      <h2>监考安排</h2>
    </div>

    <el-card class="search-card" shadow="hover">
      <el-form :inline="true" :model="searchForm" class="search-form" @submit.prevent="handleSearch">
        <el-form-item label="课程名称">
          <el-input v-model="searchForm.courseName" placeholder="模糊搜索课程名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="监考人">
          <el-input v-model="searchForm.teacherName" placeholder="请输入监考人姓名" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="考场地点">
          <el-input v-model="searchForm.room" placeholder="模糊搜索考场" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="searchForm.startTime" type="datetime" placeholder="选择开始时间" value-format="YYYY-MM-DD HH:mm:ss" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="searchForm.endTime" type="datetime" placeholder="选择结束时间" value-format="YYYY-MM-DD HH:mm:ss" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="searchForm.semesterId" placeholder="全部学期" clearable style="width: 180px">
            <el-option label="全部学期" :value="null" />
            <el-option
              v-for="semester in semesterList"
              :key="semester.semesterId"
              :label="semester.semesterName"
              :value="semester.semesterId"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearchReset">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="action-bar" v-if="isAdmin" style="margin-top: 20px;">
      <div class="action-left">
        <el-button type="primary" @click="openImportDialog">
          <el-icon><Upload /></el-icon>
          批量导入Excel
        </el-button>
        <el-button type="danger" :disabled="!selectedRows.length" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>
      <div class="action-right">
        <el-button type="warning" @click="exportTableData">
          <el-icon><Download /></el-icon>
          导出Excel
        </el-button>
      </div>
    </div>

    <div class="table-section">
      <el-table
        :data="tableData"
        border
        stripe
        style="width: 100%"
        :empty-text="'暂无监考数据'"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="课程名称" prop="courseName" min-width="150" align="center" />
        <el-table-column label="考场" prop="room" width="140" align="center" />
        <el-table-column label="主监考" prop="mainTeacherName" width="120" align="center" />
        <el-table-column label="副监考1" prop="subTeacherName" width="120" align="center" />
        <el-table-column label="副监考2" prop="subTeacher2Name" width="120" align="center" />
        <el-table-column label="副监考3" prop="subTeacher3Name" width="120" align="center" />
        <el-table-column label="开课校区" prop="teachingCampus" width="120" align="center" />
        <el-table-column label="考场所在校区" prop="examCampus" width="130" align="center" />
        <el-table-column label="开课院系" prop="teachingDept" width="120" align="center" />
        <el-table-column label="授课教师" prop="teachingTeacher" width="120" align="center" />
        <el-table-column label="监考学院" prop="invigilationCollege" width="120" align="center" />
        <el-table-column label="教学班名称" prop="className" width="140" align="center" />
        <el-table-column label="人数" prop="studentCount" width="80" align="center" />
        <el-table-column label="开始时间" prop="startTime" min-width="160" align="center" />
        <el-table-column label="结束时间" prop="endTime" min-width="160" align="center" />
      </el-table>
    </div>

    <div class="pagination-section">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="totalCount"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="handleSearch"
        @size-change="handleSearchReset"
      />
    </div>

    <el-dialog v-model="importDialogVisible" title="导入监考安排" width="950" :close-on-click-modal="false">
      <el-form :model="importForm" label-width="90px" v-if="!importRows.length">
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
        <el-form-item label="Excel 文件" required>
          <el-upload
            ref="uploadRef"
            accept=".xlsx,.xls"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="upload-tip">请选择 Excel 文件（xlsx/xls 格式），学期信息将以上方选择的学期为准。</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <el-alert
        v-if="importRows.length"
        :title="`已读取 ${importRows.length} 条记录，请确认数据无误后导入`"
        type="success"
        :closable="false"
        style="margin-bottom: 16px"
      />

      <el-table v-if="importRows.length" :data="previewExamData" border stripe max-height="400">
        <el-table-column label="课程名称" prop="courseName" align="center" min-width="120" />
        <el-table-column label="学期" align="center" width="120">
          <template #default>{{ currentSemesterName }}</template>
        </el-table-column>
        <el-table-column label="主监考" prop="mainTeacher" align="center" width="100" />
        <el-table-column label="副监考1" prop="subTeacher1" align="center" width="100" />
        <el-table-column label="副监考2" prop="subTeacher2" align="center" width="100" />
        <el-table-column label="副监考3" prop="subTeacher3" align="center" width="100" />
        <el-table-column label="开课校区" prop="teachingCampus" align="center" width="100" />
        <el-table-column label="考场所在校区" prop="examCampus" align="center" width="110" />
        <el-table-column label="开课院系" prop="teachingDept" align="center" width="110" />
        <el-table-column label="授课教师" prop="teachingTeacher" align="center" width="100" />
        <el-table-column label="监考学院" prop="invigilationCollege" align="center" width="110" />
        <el-table-column label="教学班名称" prop="className" align="center" width="120" />
        <el-table-column label="人数" prop="studentCount" align="center" width="70" />
        <el-table-column label="开始时间" prop="startTime" align="center" min-width="150" />
        <el-table-column label="结束时间" prop="endTime" align="center" min-width="150" />
        <el-table-column label="考场" prop="room" align="center" width="130" />
        <el-table-column label="状态" prop="status" align="center" width="90" />
      </el-table>
      <template #footer>
        <el-button @click="cancelImport">取消</el-button>
        <el-button v-if="importRows.length" type="primary" :loading="importLoading" @click="confirmImport">确认导入入库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Download, Refresh, Delete } from '@element-plus/icons-vue'
import { finalExamApi, finalExamTeacherApi, teacherApi, semesterApi } from '../api/index'
import * as XLSX from 'xlsx'

// 操作权限码
const actionCodes = JSON.parse(localStorage.getItem('userInfo') || '{}').actionCodes || []
const hasPermission = (code) => {
  if (isAdmin.value) return true
  return actionCodes.includes(code)
}

const teacherDept = ref('')

const isAdmin = computed(() => {
  const userRole = localStorage.getItem('userRole')
  if (userRole && userRole === 'admin') return true
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr)
      if (userInfo.userType === 'admin') return true
      if (userInfo.dept === '教学办公室') return true
    } catch (e) {}
  }
  if (teacherDept.value === '教学办公室') return true
  return false
})

const currentUsername = ref('')
const currentPhone = computed(() => {
  const username = localStorage.getItem('username')
  if (username) return username
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr)
      return userInfo.username || ''
    } catch (e) {
      return ''
    }
  }
  return ''
})

const loadTeacherInfo = async () => {
  if (!currentPhone.value) return
  try {
    const res = await teacherApi.getByPhone(currentPhone.value)
    if (res.code === '200' && res.data) {
      currentUsername.value = res.data.name || ''
      teacherDept.value = res.data.dept || ''
    }
  } catch (e) {
    console.error('获取教师信息失败:', e)
  }
}

const searchForm = reactive({
  courseName: '',
  teacherName: '',
  room: '',
  startTime: '',
  endTime: '',
  semesterId: ''
})

const pageNum = ref(1)
const pageSize = ref(10)
const tableData = ref([])
const totalCount = ref(0)
const selectedRows = ref([])

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const handleBatchDelete = async () => {
  if (!selectedRows.value.length) return
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 条监考数据吗？删除后不可恢复。`,
      '批量删除确认',
      { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
    )
  } catch (e) {
    return
  }
  try {
    const ids = [...new Set(selectedRows.value.map(r => r.examId))]
    const res = await finalExamApi.batchDelete(ids)
    if (res && res.code === '200') {
      ElMessage.success(`成功删除 ${res.data || ids.length} 条监考数据`)
      selectedRows.value = []
      handleSearch()
    } else {
      ElMessage.error(res?.msg || '删除失败')
    }
  } catch (err) {
    console.error('批量删除失败:', err)
    ElMessage.error('删除失败: ' + (err.message || err))
  }
}

const semesterList = ref([])
const currentSemesterName = computed(() => {
  const s = semesterList.value.find(s => s.semesterId === importForm.semesterId)
  return s ? s.semesterName : ''
})

const uploadRef = ref(null)
const importDialogVisible = ref(false)
const previewExamData = ref([])
const importLoading = ref(false)
const importRows = ref([])

const importForm = reactive({
  semesterId: ''
})

const handleSearchReset = () => {
  pageNum.value = 1
  handleSearch()
}

const handleSearch = async () => {
  const params = {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    courseName: searchForm.courseName,
    teacherName: searchForm.teacherName,
    room: searchForm.room,
    startTime: searchForm.startTime,
    endTime: searchForm.endTime,
    semesterId: searchForm.semesterId
  }

  try {
    const res = await finalExamApi.pageQuery(params)
    if (res.code === '200' && res.data) {
      tableData.value = (res.data.records || res.data.list || []).map(row => ({
        examId: row.examId || '',
        courseName: row.courseName || '',
        semesterId: row.semester?.semesterId || '',
        startTime: row.startTime || '',
        endTime: row.endTime || '',
        room: row.room || '',
        status: row.status || '',
        mainTeacherName: row.mainTeacherName || '',
        subTeacherName: row.subTeacherName || '',
        subTeacher2Name: row.subTeacher2Name || '',
        subTeacher3Name: row.subTeacher3Name || '',
        teachingCampus: row.teachingCampus || '',
        examCampus: row.examCampus || '',
        teachingDept: row.teachingDept || '',
        teachingTeacher: row.teachingTeacher || '',
        invigilationCollege: row.invigilationCollege || '',
        className: row.className || '',
        studentCount: row.studentCount || 0
      }))
      totalCount.value = Number(res.data.total || 0)
    } else {
      tableData.value = []
      totalCount.value = 0
    }
  } catch (err) {
    console.error('加载监考数据失败:', err)
    ElMessage.error('加载监考数据失败')
    tableData.value = []
    totalCount.value = 0
  }
}

const handleReset = () => {
  const currentSemester = semesterList.value.find(s => s.isCurrent === 1)
  Object.assign(searchForm, {
    courseName: '',
    teacherName: !isAdmin.value && currentUsername.value ? currentUsername.value : '',
    room: '',
    startTime: '',
    endTime: '',
    semesterId: currentSemester ? currentSemester.semesterId : ''
  })
  pageNum.value = 1
  handleSearch()
}

const formatExcelDate = (val) => {
  if (!val) return ''
  if (val instanceof Date) {
    return val.toISOString().split('T')[0]
  }
  if (typeof val === 'number') {
    const d = new Date((val - 25569) * 86400 * 1000)
    return d.toISOString().split('T')[0]
  }
  return String(val).replace(/\//g, '-')
}

const openImportDialog = () => {
  importForm.semesterId = searchForm.semesterId || ''
  importRows.value = []
  previewExamData.value = []
  uploadRef.value?.clearFiles()
  importDialogVisible.value = true
}

const cancelImport = () => {
  importDialogVisible.value = false
  importRows.value = []
  previewExamData.value = []
  uploadRef.value?.clearFiles()
}

const handleFileChange = (fileObj) => {
  if (!importForm.semesterId) {
    ElMessage.warning('请先选择学期')
    uploadRef.value?.clearFiles()
    return
  }
  const raw = fileObj.raw
  const suffix = raw.name.split('.').pop().toLowerCase()
  if (!['xlsx', 'xls'].includes(suffix)) {
    ElMessage.error('仅支持 xlsx / xls 格式文件')
    uploadRef.value?.clearFiles()
    return
  }
  
  const reader = new FileReader()
  reader.readAsArrayBuffer(raw)
  reader.onload = e => {
    try {
      const data = new Uint8Array(e.target.result)
      const workbook = XLSX.read(data, { type: 'array' })
      const sheetName = workbook.SheetNames[0]
      const worksheet = workbook.Sheets[sheetName]
      const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })
      
      if (jsonData.length <= 1) return ElMessage.warning('表格无有效数据')
      
      const headers = jsonData[0].map(h => String(h).trim())
      const rows = jsonData.slice(1).filter(row => row.length > 0 && row.some(cell => cell !== null && cell !== undefined && String(cell).trim() !== ''))
      
      const headerMap = {
        "考试日期": "examDate",
        "考试时间": "examTime",
        "课程名称": "courseName",
        "考试地点": "room",
        "主监考": "mainTeacher",
        "副监考": "subTeacher1",
        "副监考1": "subTeacher1",
        "副监考2": "subTeacher2",
        "副监考3": "subTeacher3",
        "开课校区": "teachingCampus",
        "考场所在校区": "examCampus",
        "开课院系": "teachingDept",
        "授课教师": "teachingTeacher",
        "监考学院": "invigilationCollege",
        "教学班名称": "className",
        "人数": "studentCount",
        "试卷份数": "studentCount"
      }
      
      const temp = []
      
      rows.forEach((row) => {
        const item = {}
        headers.forEach((h, i) => {
          if (headerMap[h]) item[headerMap[h]] = row[i] ?? ''
        })
        
        item.examDate = formatExcelDate(item.examDate)
        if (item.examDate && item.examTime) {
          const timeStr = String(item.examTime)
          const tArr = timeStr.includes('~') ? timeStr.split('~').map(x => x.trim()) : timeStr.split('-').map(x => x.trim())
          item.startTime = `${item.examDate} ${tArr[0]}:00`
          item.endTime = `${item.examDate} ${tArr[1] || tArr[0]}:00`
        }
        
        item.semesterId = importForm.semesterId
        item.semester = { semesterId: importForm.semesterId }
        item.status = "待安排"
        
        if (item.courseName && item.startTime && item.endTime && item.room) temp.push(item)
      })
      
      if (temp.length === 0) return ElMessage.warning('未解析到合法数据，请检查课程名称、日期、时间、考场列')
      previewExamData.value = temp
      importRows.value = temp
    } catch (err) {
      console.error('解析Excel失败:', err)
      ElMessage.error('解析Excel失败，请确保文件格式正确')
    }
  }
}

const handleFileRemove = () => {
  importRows.value = []
  previewExamData.value = []
}

const confirmImport = async () => {
  importLoading.value = true
  try {
    let teacherMap = new Map()
    try {
      const teacherRes = await teacherApi.getAll()
      if (teacherRes.code === '200' && teacherRes.data) {
        teacherRes.data.forEach(t => {
          teacherMap.set(t.name, t.teacherId)
        })
      }
    } catch (e) {
      ElMessage.warning('获取教师列表失败')
    }
    
    const res = await finalExamApi.batchImport(previewExamData.value)
    if (res.code === '200' && res.data) {
      const examList = res.data
      const teacherList = []
      
      examList.forEach((exam, index) => {
        const originalData = previewExamData.value[index]
        const mainTeacherId = teacherMap.get(originalData.mainTeacher)
        if (originalData.mainTeacher && mainTeacherId) {
          teacherList.push({
            exam: { examId: exam.examId },
            teacher: { teacherId: mainTeacherId },
            invigilateRole: '主监考'
          })
        }
        const sub1Id = teacherMap.get(originalData.subTeacher1)
        if (originalData.subTeacher1 && sub1Id) {
          teacherList.push({
            exam: { examId: exam.examId },
            teacher: { teacherId: sub1Id },
            invigilateRole: '副监考1'
          })
        }
        const sub2Id = teacherMap.get(originalData.subTeacher2)
        if (originalData.subTeacher2 && sub2Id) {
          teacherList.push({
            exam: { examId: exam.examId },
            teacher: { teacherId: sub2Id },
            invigilateRole: '副监考2'
          })
        }
        const sub3Id = teacherMap.get(originalData.subTeacher3)
        if (originalData.subTeacher3 && sub3Id) {
          teacherList.push({
            exam: { examId: exam.examId },
            teacher: { teacherId: sub3Id },
            invigilateRole: '副监考3'
          })
        }
      })
      
      if (teacherList.length > 0) {
        await finalExamTeacherApi.batchImport(teacherList)
      }
      
      ElMessage.success(`成功导入 ${examList.length} 条监考数据和 ${teacherList.length} 条教师数据`)
      importDialogVisible.value = false
      previewExamData.value = []
      handleSearch()
    } else {
      ElMessage.error('导入失败')
    }
  } catch (err) {
    console.error('导入失败:', err)
    ElMessage.error('导入失败')
  } finally {
    importLoading.value = false
  }
}

const handleResetData = async () => {
  try {
    const res = await finalExamTeacherApi.deleteAll()
    if (res && res.code === '200') {
      const count = res.data || 0
      ElMessage.success(`成功删除 ${count} 条监考数据`)
      tableData.value = []
      totalCount.value = 0
      handleSearch()
    } else {
      ElMessage.error(res?.msg || '删除失败')
    }
  } catch (err) {
    console.error('删除失败:', err.response?.data || err.message || err)
    ElMessage.error('删除失败: ' + (err.message || err))
  }
}

const exportTableData = async () => {
  // 调用分页接口传大 pageSize 获取当前查询条件下的全量数据
  // 否则只导出当前页 tableData，会漏掉其他页数据
  let exportData
  try {
    const params = {
      pageNum: 1,
      pageSize: 99999,
      courseName: searchForm.courseName,
      teacherName: searchForm.teacherName,
      room: searchForm.room,
      startTime: searchForm.startTime,
      endTime: searchForm.endTime,
      semesterId: searchForm.semesterId
    }
    const res = await finalExamApi.pageQuery(params)
    if (res.code !== '200' || !res.data) {
      return ElMessage.error('导出失败')
    }
    exportData = res.data.records || res.data.list || []
  } catch (err) {
    console.error('导出失败:', err)
    return ElMessage.error('导出失败')
  }
  if (!Array.isArray(exportData) || exportData.length === 0) {
    return ElMessage.warning('当前无数据可导出')
  }

  const headers = ["课程名称", "考场", "主监考", "副监考1", "副监考2", "副监考3", "开课校区", "考场所在校区", "开课院系", "授课教师", "监考学院", "教学班名称", "人数", "开始时间", "结束时间"]
  const rows = exportData.map(row => [
    row.courseName || '',
    row.room || '',
    row.mainTeacherName || '',
    row.subTeacherName || '',
    row.subTeacher2Name || '',
    row.subTeacher3Name || '',
    row.teachingCampus || '',
    row.examCampus || '',
    row.teachingDept || '',
    row.teachingTeacher || '',
    row.invigilationCollege || '',
    row.className || '',
    row.studentCount || 0,
    row.startTime || '',
    row.endTime || ''
  ])
  
  // 标准 CSV：逗号分隔，字段含逗号/引号/换行时用双引号包裹，内部引号转义为两个双引号
  const escapeCsv = (val) => {
    const s = String(val == null ? '' : val)
    if (/[",\r\n]/.test(s)) {
      return '"' + s.replace(/"/g, '""') + '"'
    }
    return s
  }
  const csvContent = [headers.map(escapeCsv).join(','), ...rows.map(r => r.map(escapeCsv).join(','))].join('\r\n')
  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
  
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  const dateStr = new Date().toISOString().slice(0, 10).replace(/-/g, '')
  link.setAttribute('href', url)
  link.setAttribute('download', `监考任务_${dateStr}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  
  ElMessage.success("监考表格导出成功，文件已下载")
}

const fetchSemesters = async () => {
  try {
    const res = await semesterApi.getAll()
    if (res.code === '200' && res.data) {
      semesterList.value = res.data
      const current = res.data.find(s => s.isCurrent == 1)
      if (current) {
        searchForm.semesterId = current.semesterId
      } else if (res.data.length > 0) {
        searchForm.semesterId = res.data[res.data.length - 1].semesterId
      }
    }
  } catch (error) {
    console.error('获取学期列表失败:', error)
  }
}

onMounted(async () => {
  await loadTeacherInfo()
  await fetchSemesters()
  if (!isAdmin.value && currentUsername.value) {
    searchForm.teacherName = currentUsername.value
  }
  handleSearch()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
  background: #ffffff;
  min-height: 100vh;
}

.page-title {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.search-section {
  background: #fff;
  padding: 16px 24px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 4px 16px;
}

.action-left {
  display: flex;
  gap: 12px;
}

.table-section {
  background: #fff;
  padding: 0 24px 16px;
  border-radius: 4px;
}

.pagination-section {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 16px 24px;
  background: #fff;
  border-radius: 4px;
  margin-top: 16px;
  clear: both;
  position: relative;
  z-index: 10;
}

.upload-tip {
  color: #909399;
}
</style>
