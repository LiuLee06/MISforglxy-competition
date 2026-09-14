<template>
  <div class="page-container">
    <div class="page-header">
      <h2>教师教学课时</h2>
    </div>

    <el-card class="search-card" shadow="hover">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="教师编号">
          <el-input v-model="searchForm.teacherNo" placeholder="请输入教师编号" clearable />
        </el-form-item>
        <el-form-item label="教师">
          <el-input v-model="searchForm.teacherName" placeholder="请输入教师姓名" clearable />
        </el-form-item>
        <el-form-item label="课程名称">
          <el-input v-model="searchForm.courseName" placeholder="请输入课程名称" clearable />
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="searchForm.semesterId" placeholder="全部学期" clearable style="width: 210px" @change="fetchData">
            <el-option
              v-for="semester in semesterList"
              :key="semester.semesterId"
              :label="semester.semesterName"
              :value="semester.semesterId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="确认状态">
          <el-select v-model="searchForm.confirmStatus" placeholder="全部" clearable style="width: 130px">
            <el-option label="待确认" :value="0" />
            <el-option label="已确认" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="completion-card" shadow="hover" style="margin-top: 20px;">
      <template #header>
        <div class="completion-header">
          <span>课时确认情况</span>
          <el-button type="primary" link @click="completionDetailVisible = true">查看教师确认明细</el-button>
        </div>
      </template>
      <div class="completion-cards">
        <div class="completion-item"><span>总教师数</span><strong>{{ completion.totalTeachers }}</strong></div>
        <div class="completion-item success"><span>已确认教师</span><strong>{{ completion.completedTeachers }}</strong></div>
        <div class="completion-item warning"><span>未确认教师</span><strong>{{ completion.uncompletedTeachers }}</strong></div>
        <div class="completion-item"><span>总记录数</span><strong>{{ completion.totalRecords }}</strong></div>
        <div class="completion-item success"><span>已确认记录</span><strong>{{ completion.confirmedRecords }}</strong></div>
        <div class="completion-item warning"><span>待确认记录</span><strong>{{ completion.pendingRecords }}</strong></div>
        <div class="completion-item rate"><span>整体确认率</span><strong>{{ completion.completionRate }}%</strong></div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="hover" style="margin-top: 20px;">
      <template #header>
        <div class="table-header">
          <span>教学课时管理</span>
          <div>
            <el-button type="danger" @click="deleteSemesterData">按学期批量删除</el-button>
            <el-button type="success" @click="openImportDialog">导入 CSV</el-button>
            <el-button type="warning" @click="exportCsv">导出 CSV</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading" max-height="620">
        <el-table-column prop="teacherNo" label="教师编号" width="110" fixed />
        <el-table-column prop="teacherName" label="教师" width="100" fixed />
        <el-table-column prop="courseNo" label="课程编号" width="110" />
        <el-table-column prop="courseName" label="课程名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="hourType" label="学时类型" width="110" />
        <el-table-column prop="notificationNo" label="通知单编号" min-width="150" show-overflow-tooltip />
        <el-table-column prop="transferHours" label="转入学时" width="100" />
        <el-table-column prop="actualHours" label="实际学时" width="100">
          <template #default="{ row }">
            {{ row.actualHours == null ? '' : Math.round(row.actualHours) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalHours" label="总学时" width="90" />
        <el-table-column prop="totalCredit" label="总学分" width="90" />
        <el-table-column label="实际学时占比" width="130">
          <template #default="{ row }">
            {{ formatRatio(row.actualHourRatio) }}
          </template>
        </el-table-column>
        <el-table-column prop="studentCount" label="人数" width="80" />
        <el-table-column prop="className" label="上课班级" min-width="160" show-overflow-tooltip />
        <el-table-column prop="semesterName" label="学期" min-width="190" show-overflow-tooltip />
        <el-table-column label="状态" width="100" fixed="right">
          <template #default="{ row }">
            <el-tag :type="Number(row.confirmStatus) === 1 ? 'success' : 'warning'">
              {{ Number(row.confirmStatus) === 1 ? '已确认' : '待确认' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="importVisible" title="导入教学课时" width="520px" :close-on-click-modal="false">
      <el-form label-width="90px">
        <el-form-item label="学期" required>
          <el-select v-model="importSemesterId" placeholder="请选择学期" style="width: 100%">
            <el-option
              v-for="semester in semesterList"
              :key="semester.semesterId"
              :label="semester.semesterName"
              :value="semester.semesterId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="CSV 文件" required>
          <el-upload
            ref="uploadRef"
            accept=".csv"
            :auto-upload="false"
            :limit="1"
            :on-change="readCsv"
            :on-remove="() => importRows = []"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="upload-tip">请选择 CSV UTF-8 文件，表头需与课时表一致。</div>
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
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="confirmImport">确认导入</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="completionDetailVisible" title="教师课时确认明细" width="850px">
      <el-table :data="completion.teacherDetails" border stripe max-height="520">
        <el-table-column prop="teacherNo" label="教师编号" width="120" />
        <el-table-column prop="teacherName" label="教师姓名" width="120" />
        <el-table-column prop="totalRecords" label="总记录数" width="110" />
        <el-table-column prop="confirmedRecords" label="已确认" width="100" />
        <el-table-column prop="pendingRecords" label="待确认" width="100" />
        <el-table-column label="确认率" width="110"><template #default="{ row }">{{ row.completionRate }}%</template></el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }"><el-tag :type="row.completed ? 'success' : 'warning'">{{ row.completed ? '已确认' : '未确认' }}</el-tag></template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!completion.teacherDetails.length" description="暂无完成情况数据" />
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { semesterApi, workloadApi } from '../api/index'

// 操作权限码
const actionCodes = JSON.parse(localStorage.getItem('userInfo') || '{}').actionCodes || []
const hasPermission = (code) => actionCodes.includes(code)

const searchForm = reactive({
  teacherNo: '',
  teacherName: '',
  courseName: '',
  semesterId: '',
  confirmStatus: ''
})
const tableData = ref([])
const semesterList = ref([])
const loading = ref(false)
const importVisible = ref(false)
const importing = ref(false)
const importSemesterId = ref('')
const importRows = ref([])
const uploadRef = ref()
const completionDetailVisible = ref(false)
const completion = reactive({
  totalTeachers: 0, completedTeachers: 0, uncompletedTeachers: 0,
  totalRecords: 0, confirmedRecords: 0, pendingRecords: 0,
  completionRate: 0, teacherDetails: []
})

const fields = [
  { label: '教师编号', prop: 'teacherNo' },
  { label: '教师', prop: 'teacherName' },
  { label: '课程编号', prop: 'courseNo' },
  { label: '课程名称', prop: 'courseName' },
  { label: '学时类型', prop: 'hourType' },
  { label: '通知单编号', prop: 'notificationNo' },
  { label: '转入学时', prop: 'transferHours', number: true },
  { label: '实际学时', prop: 'actualHours', number: true },
  { label: '总学时', prop: 'totalHours', number: true },
  { label: '总学分', prop: 'totalCredit', number: true },
  { label: '实际学时占比', prop: 'actualHourRatio', ratio: true },
  { label: '人数', prop: 'studentCount', integer: true },
  { label: '上课班级', prop: 'className' }
]

const fetchSemesters = async () => {
  try {
    const res = await semesterApi.getAll()
    semesterList.value = res.code === '200' && res.data ? res.data : []
  } catch (error) {
    ElMessage.error('获取学期列表失败')
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {}
    //entries() 方法返回一个数组，数组的每个元素都是一个包含键值对的数组，键值对的顺序与对象中键值对的顺序相同.最终是一个二维数组
    Object.entries(searchForm).forEach(([key, value]) => {
      if (value !== '' && value !== null) params[key] = value
    })
    const res = await workloadApi.getList(params)
    tableData.value = res.code === '200' && res.data ? res.data : []
    await fetchCompletion()
  } catch (error) {
    tableData.value = []
    ElMessage.error('获取教学课时失败')
  } finally {
    loading.value = false
  }
}

const fetchCompletion = async () => {
  try {
    const params = searchForm.semesterId ? { semesterId: searchForm.semesterId } : {}
    const res = await workloadApi.getCompletion(params)
    if (res.code === '200' && res.data) {
      //这里completion 是 const 声明的，重新赋值直接报错，所以用 Object.assign() 方法合并对象；改用let声明,响应式对象失效
      Object.assign(completion, res.data)
    } else {
      Object.assign(completion, {
        totalTeachers: 0, completedTeachers: 0, uncompletedTeachers: 0,
        totalRecords: 0, confirmedRecords: 0, pendingRecords: 0,
        completionRate: 0, teacherDetails: []
      })
    }
  } catch (error) {
    ElMessage.error('获取课时确认情况失败')
  }
}

const resetSearch = () => {
  Object.assign(searchForm, {
    teacherNo: '', teacherName: '', courseName: '', semesterId: '', confirmStatus: ''
  })
  fetchData()
}

const openImportDialog = () => {
  importSemesterId.value = searchForm.semesterId || ''
  importRows.value = []
  uploadRef.value?.clearFiles()
  importVisible.value = true
}

const deleteSemesterData = async () => {
  if (!searchForm.semesterId) {
    ElMessage.warning('请先选择要删除的学期')
    return
  }
  const semester = semesterList.value.find(item => String(item.semesterId) === String(searchForm.semesterId))
  const semesterName = semester?.semesterName || `学期 ${searchForm.semesterId}`
  try {
    await ElMessageBox.confirm(
      `将删除 WORKLOAD 表中“${semesterName}”的全部记录，已确认和未确认记录都会删除，且不可恢复。是否继续？`,
      '确认批量删除',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
    const res = await workloadApi.deleteBySemester(searchForm.semesterId)
    if (res.code === '200') {
      ElMessage.success(`已删除 ${res.data || 0} 条课时记录`)
      await fetchData()
    } else {
      ElMessage.error(res.msg || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('批量删除失败')
    }
  }
}

// parseCsv：把 CSV 文本按状态机方式逐字符解析成二维数组
// 入参：text = 文件全文；separator = 分隔符（',' 或 '\t'）
// 输出形式：rows = [ ['教师编号','教师',...], ['T0001','张三',...], ... ]
//          外层每个元素是一行，内层是该行各列的字符串值；全空行会被跳过
// 核心能力：用 quoted 标记是否处于引号内，引号内的逗号/换行不算分隔符，
//          两个连续引号 "" 转义为一个字面引号，保证 "软件2101,软件2102" 这类带逗号的字段不被拆列
const parseCsv = (text, separator) => {
  const rows = []        // 最终结果：所有行
  let row = []           // 当前正在拼的行
  let value = ''         // 当前正在拼的单元格
  let quoted = false     // 状态标记：是否处于双引号内部
  for (let i = 0; i < text.length; i++) {
    const char = text[i]
    if (char === '"') {
      if (quoted && text[i + 1] === '"') {
        // 引号内连续两个 " → 转义为一个字面引号，并跳过下一个字符
        value += '"'
        i++
      } else {
        // 引号进/出的开关：进入引号区或离开引号区
        quoted = !quoted
      }
    } else if (char === separator && !quoted) {
      // 引号外的分隔符 → 当前单元格结束，存入本行
      row.push(value)
      value = ''
    } else if ((char === '\r' || char === '\n') && !quoted) {
      // 引号外的换行 → 本行结束（\r\n 算一个换行，多跳一个字符）
      if (char === '\r' && text[i + 1] === '\n') i++
      row.push(value)
      // 整行都是空白则丢弃，不进入结果
      if (row.some(item => item.trim())) rows.push(row)
      row = []
      value = ''
    } else {
      // 普通字符（含引号内的逗号、换行）→ 直接累加进当前单元格
      value += char
    }
  }
  // 文本结尾可能没有换行符，把最后一个单元格和最后一行补进结果
  row.push(value)
  if (row.some(item => item.trim())) rows.push(row)
  return rows
}

const normalizeText = value => String(value || '').trim().replace(/^="(.*)"$/, '$1')
const toNullableNumber = value => {
  const text = normalizeText(value).replace(/%$/, '')
  if (text === '') return null
  const number = Number(text)
  return Number.isFinite(number) ? number : null
}

// 选文件 → 后缀校验（el-upload 的 on-change 触发本函数）
const readCsv = file => {
  if (!file.name.toLowerCase().endsWith('.csv')) {
    ElMessage.error('请选择 CSV 文件')
    uploadRef.value?.clearFiles()
    return
  }
  const reader = new FileReader()
  reader.onload = event => {
    //event.target.result 是文件内容的文本表示
    const text = String(event.target.result || '').replace(/^\uFEFF/, '')
    const separator = text.split(/\r?\n/, 1)[0].includes('\t') ? '\t' : ','
    const rows = parseCsv(text, separator)
    // 空文件拦截：不足 2 行说明只有表头甚至全空
    if (rows.length < 2) {
      importRows.value = []
      ElMessage.error('文件中没有可导入的数据')
      return
    }
    // 扫描定位表头行（容忍标题行）：占比列可缺省，其余 12 列必须齐全，全部出现的行才是表头
    const requiredFields = fields.filter(field => field.prop !== 'actualHourRatio')
    const headerRowIndex = rows.findIndex(row => {
      const headers = row.map(normalizeText)
      return requiredFields.every(field => headers.includes(field.label))
    })
    if (headerRowIndex === -1) {
      importRows.value = []
      ElMessage.error('没有找到课时表头，请检查文件内容')
      return
    }

    // 建立 列名→下标 映射（容忍列顺序变化）：如 { '教师编号': 0, '教师': 1, ... }
    const indexes = {}
    rows[headerRowIndex].forEach((header, index) => {
      indexes[normalizeText(header)] = index
    })
    //检查是否缺少必填表头,如果有,则提示用户
    const missing = requiredFields.filter(field => indexes[field.label] === undefined)
    if (missing.length) {
      importRows.value = []
      ElMessage.error(`缺少表头：${missing.map(field => field.label).join('、')}`)
      return
    }
    // 逐行逐列转成对象：数字列转数字、文字列清理、空串转 null
    importRows.value = rows
      .slice(headerRowIndex + 1)
      .map(values => {
        const item = {}
        fields.forEach(field => {
          const raw = indexes[field.label] === undefined ? '' : values[indexes[field.label]]
          if (field.number || field.integer || field.ratio) {
            item[field.prop] = toNullableNumber(raw)
          } else {
            const value = normalizeText(raw)
            item[field.prop] = value === '' ? null : value
          }
        })
        return item
      })
      //丢弃全空行：必填字段全为 null 的行不导入，结果存入 importRows
      .filter(item => requiredFields.some(field => item[field.prop] !== null))
  }
  reader.onerror = () => ElMessage.error('读取 CSV 文件失败')
  reader.readAsText(file.raw, 'UTF-8')
}

const confirmImport = async () => {
  if (!importSemesterId.value) return ElMessage.warning('请选择学期')
  if (!importRows.value.length) return ElMessage.warning('请选择需要导入的 CSV 文件')
  importing.value = true
  try {
    const data = importRows.value.map(row => ({
      ...row,
      semesterId: importSemesterId.value,
      actualHourRatio: null,
      confirmStatus: 0
    }))
    const res = await workloadApi.batchImport(data)
    if (res.code === '200') {
      ElMessage.success(`成功导入 ${data.length} 条课时记录`)
      importVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.msg || '导入失败')
    }
  } catch (error) {
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
}

const escapeCsv = value => `"${String(value ?? '').replace(/"/g, '""')}"`
const exportCsv = () => {
  if (!tableData.value.length) return ElMessage.warning('当前没有可导出的数据')
  const headers = fields.map(field => escapeCsv(field.label)).join(',')
  const rows = tableData.value.map(row => fields.map(field => {
    let value = row[field.prop]
    if (field.prop === 'teacherNo' || field.prop === 'notificationNo') {
      value = value ? `="${value}"` : ''
    } else if (field.ratio && value !== null && value !== undefined) {
      value = `${value}%`
    }
    return escapeCsv(value)
  }).join(','))
  const blob = new Blob(['\uFEFF' + [headers, ...rows].join('\r\n')], {
    type: 'text/csv;charset=utf-8;'
  })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `教学课时_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

const formatRatio = value => value === null || value === undefined ? '' : `${Number(value).toFixed(2)}%`

onMounted(async () => {
  await fetchSemesters()
  await fetchData()
})
</script>

<style scoped>
.search-card {
  margin-bottom: 16px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.completion-card {
  margin: 16px 0;
}

.completion-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.completion-cards {
  display: grid;
  grid-template-columns: repeat(7, minmax(110px, 1fr));
  gap: 12px;
}

.completion-item {
  padding: 14px 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #f8fafc;
}

.completion-item span,
.completion-item strong {
  display: block;
}

.completion-item span {
  color: #909399;
  font-size: 13px;
}

.completion-item strong {
  margin-top: 8px;
  color: #303133;
  font-size: 24px;
}

.completion-item.success strong {
  color: #67c23a;
}

.completion-item.warning strong {
  color: #e6a23c;
}

.completion-item.rate strong {
  color: #409eff;
}

@media (max-width: 1100px) {
  .completion-cards {
    grid-template-columns: repeat(4, minmax(110px, 1fr));
  }
}

.upload-tip {
  color: #909399;
}
</style>
