<template>
  <div class="page-container">
    <div class="page-header">
      <h2>我的教学课时</h2>
      <p class="page-tip">提示：您仅可修改"实际学时"字段，其余信息由管理员统一维护。</p>
    </div>

    <el-card class="search-card" shadow="hover">
      <template #header>
        <div class="header">
          <span>{{ userInfo.name || '' }} 的课时</span>
          <div class="header-actions">
            <el-input v-model="searchForm.courseName" placeholder="课程名称" clearable style="width: 180px" />
            <el-input v-model="searchForm.teacherName" placeholder="教师姓名" clearable style="width: 150px" />
            <el-select v-model="semesterId" placeholder="全部学期" clearable style="width: 210px" @change="handleSemesterChange">
              <el-option
                v-for="semester in semesterList"
                :key="semester.semesterId"
                :label="semester.semesterName"
                :value="semester.semesterId"
              />
            </el-select>
            <el-button type="primary" @click="fetchData">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
            <el-button v-if="isDepartmentHead" type="primary" plain @click="openDepartmentDialog">
              本系确认情况
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" :span-method="spanMethod" border stripe v-loading="loading" max-height="680">
        <el-table-column prop="semesterName" label="学期" min-width="170" show-overflow-tooltip />
        <el-table-column prop="notificationNo" label="通知单编号" min-width="150" show-overflow-tooltip />
        <el-table-column prop="courseNo" label="课程编号" width="110" />
        <el-table-column prop="courseName" label="课程名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="teacherNo" label="教师编号" width="110" />
        <el-table-column prop="teacherName" label="教师" width="100" />
        <el-table-column prop="hourType" label="学时类型" width="110" />
        <el-table-column prop="transferHours" label="转入学时" width="100" />
        <el-table-column prop="actualHours" label="实际学时" width="175">
          <template #default="{ row }">
            <!-- 已确认行：纯文本锁定，不可编辑 -->
            <span v-if="Number(row.confirmStatus) === 1">{{ row.actualHours == null ? '' : Math.round(row.actualHours) }}</span>
            <!-- 待确认行：点击变输入框；编辑期间自由调整不发请求；✓/回车保存，✕/Esc/失焦取消 -->
            <div v-else-if="editingCellId === row.wlId" class="cell-editing">
              <el-input-number
                :model-value="editingValue"
                :min="0"
                :precision="0"
                :step="1"
                controls-position="right"
                size="small"
                class="cell-editing-input"
                @update:model-value="editingValue = $event"
                @blur="cancelEdit"
                @keyup.enter="commitEdit(row)"
                @keyup.esc="cancelEdit"
              />
              <el-button
                link
                type="primary"
                size="small"
                title="保存"
                @mousedown.prevent
                @click="commitEdit(row)"
              >
                <el-icon><Check /></el-icon>
              </el-button>
              <el-button
                link
                type="danger"
                size="small"
                title="取消"
                @mousedown.prevent
                @click="cancelEdit"
              >
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
            <span v-else class="editable-cell" title="点击修改实际学时" @click="startEdit(row)">{{ row.actualHours == null ? '--' : Math.round(row.actualHours) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalHours" label="总学时" width="90" />
        <el-table-column prop="totalCredit" label="总学分" width="90" />
        <el-table-column label="实际学时占比" width="130">
          <template #default="{ row }">{{ formatRatio(row.actualHourRatio) }}</template>
        </el-table-column>
        <el-table-column prop="studentCount" label="人数" width="80" />
        <el-table-column prop="className" label="上课班级" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="Number(row.confirmStatus) === 1 ? 'success' : 'warning'">
              {{ Number(row.confirmStatus) === 1 ? '已确认' : '待确认' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              type="success"
              size="small"
              :disabled="Number(row.confirmStatus) === 1"
              @click="confirmRow(row)"
            >
              {{ Number(row.confirmStatus) === 1 ? '已确认' : '确认' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && tableData.length === 0" description="暂无相关教学课时记录" />
    </el-card>

    <el-dialog v-model="departmentVisible" title="本系课时确认" width="90%" top="5vh">
      <div v-loading="departmentLoading">
        <div class="completion-cards">
          <div class="completion-item"><span>本系教师数</span><strong>{{ departmentCompletion.totalTeachers }}</strong></div>
          <div class="completion-item success"><span>已确认教师</span><strong>{{ departmentCompletion.completedTeachers }}</strong></div>
          <div class="completion-item warning"><span>未确认教师</span><strong>{{ departmentCompletion.uncompletedTeachers }}</strong></div>
          <div class="completion-item"><span>总记录数</span><strong>{{ departmentCompletion.totalRecords }}</strong></div>
          <div class="completion-item success"><span>已确认记录</span><strong>{{ departmentCompletion.confirmedRecords }}</strong></div>
          <div class="completion-item warning"><span>待确认记录</span><strong>{{ departmentCompletion.pendingRecords }}</strong></div>
          <div class="completion-item rate"><span>本系确认率</span><strong>{{ departmentCompletion.completionRate }}%</strong></div>
        </div>
        <el-table :data="departmentCompletion.teacherDetails" border stripe max-height="500" style="margin-top: 16px">
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
        <el-empty v-if="!departmentCompletion.teacherDetails.length" description="暂无本系课时记录" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close } from '@element-plus/icons-vue'
import { semesterApi, workloadApi } from '../api/index'

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const teacherName = String(userInfo.name ?? '').trim()
const teacherRoleIds = Array.isArray(userInfo.roleId) ? userInfo.roleId : [userInfo.roleId]
const isDepartmentHead = teacherRoleIds.some(roleId => Number(roleId) === 9)

const tableData = ref([])
const semesterList = ref([])
const semesterId = ref('')
const loading = ref(false)
const departmentLoading = ref(false)
const departmentVisible = ref(false)
const searchForm = reactive({ courseName: '', teacherName: '' })
// 行内编辑状态：editingCellId 记录当前正在编辑的行（同一时刻最多一个单元格处于编辑态）
const editingCellId = ref(null)
const editingValue = ref(null)
const savingCellId = ref(null)
const departmentCompletion = reactive({
  totalTeachers: 0, completedTeachers: 0, uncompletedTeachers: 0,
  totalRecords: 0, confirmedRecords: 0, pendingRecords: 0,
  completionRate: 0, teacherDetails: []
})

const fetchSemesters = async () => {
  try {
    const res = await semesterApi.getAll()
    semesterList.value = res.code === '200' && Array.isArray(res.data) ? res.data : []
    const currentSemester = semesterList.value.find(item => Number(item.isCurrent) === 1)
    //if:防止currentSemester为空(undefined)时，导致查询失败
    if (currentSemester) semesterId.value = currentSemester.semesterId
  } catch (error) {
    ElMessage.error('获取学期列表失败')
  }
}

const fetchData = async () => {
  if (!teacherName) {
    ElMessage.error('无法获取当前登录教师姓名')
    return
  }
  loading.value = true
  try {
    const params = { currentTeacherName: teacherName }
    if (semesterId.value) params.semesterId = semesterId.value
    if (searchForm.courseName) params.courseName = searchForm.courseName
    if (searchForm.teacherName) params.teacherName = searchForm.teacherName
    const res = await workloadApi.getMyRelated(params)
    tableData.value = res.code === '200' && Array.isArray(res.data) ? res.data : []
  } catch (error) {
    tableData.value = []
    ElMessage.error('获取我的教学课时失败')
  } finally {
    loading.value = false
  }
}

//合并相同学期、相同通知编号、相同课程编号的行
const getGroupKey = row => `${row.semesterId ?? ''}|${row.notificationNo ?? ''}|${row.courseNo ?? ''}`
const mergedColumns = new Set(['semesterName', 'notificationNo', 'courseNo', 'courseName'])

const spanMethod = ({ row, rowIndex, column }) => {
  if (!mergedColumns.has(column.property)) return [1, 1]
  const key = getGroupKey(row)
  if (rowIndex > 0 && getGroupKey(tableData.value[rowIndex - 1]) === key) return [0, 0]
  let rowspan = 1
  while (rowIndex + rowspan < tableData.value.length
    && getGroupKey(tableData.value[rowIndex + rowspan]) === key) {
    rowspan++
  }
  return [rowspan, 1]
}

const resetSearch = () => {
  Object.assign(searchForm, { courseName: '', teacherName: '' })
  fetchData()
}

const handleSemesterChange = () => {
  fetchData()
  if (departmentVisible.value) fetchDepartmentData()
}

const fetchDepartmentData = async () => {
  if (!isDepartmentHead || !userInfo.userId) return
  departmentLoading.value = true
  try {
    const res = await workloadApi.getDepartmentCompletion({
      teacherId: userInfo.userId,
      semesterId: semesterId.value || undefined
    })
    if (res.code !== '200') throw new Error(res.msg || '接口返回失败')
// res.data格式：
//   res.data = {
//   totalTeachers: 12,
//   completedTeachers: 5,
//   uncompletedTeachers: 7,
//   totalRecords: 30,
//   confirmedRecords: 11,
//   pendingRecords: 19,
//   completionRate: 36.67,
//   teacherDetails: [ {teacherNo: 'T001', teacherName: '张三', ...}, ... ]
// }
    Object.assign(departmentCompletion, res.data || {})
  } catch (error) {
    Object.assign(departmentCompletion, {
      totalTeachers: 0, completedTeachers: 0, uncompletedTeachers: 0,
      totalRecords: 0, confirmedRecords: 0, pendingRecords: 0,
      completionRate: 0, teacherDetails: []
    })
    ElMessage.error(`获取本系课时失败：${error.message || '请检查后端是否已重启'}`)
  } finally {
    departmentLoading.value = false
  }
}

const openDepartmentDialog = () => {
  departmentVisible.value = true
  fetchDepartmentData()
}

// 进入行内编辑态：记录目标行，初始值取当前实际学时（空则 null）
const startEdit = row => {
  if (savingCellId.value !== null) return
  editingCellId.value = row.wlId
  editingValue.value = row.actualHours === null || row.actualHours === undefined
    ? null : Number(row.actualHours)
}

// 取消编辑：恢复纯文本显示，不发请求
const cancelEdit = () => {
  editingCellId.value = null
  editingValue.value = null
}

// 提交保存：回车/✓ 按钮触发；值未变直接退出；成功后刷新表格（占比由后端全表重算）
const commitEdit = async row => {
  // 守卫：enter 与 blur 可能先后各触发一次，第一次已同步退出编辑态后第二次直接跳过
  if (editingCellId.value !== row.wlId) return
  const value = editingValue.value
  const original = row.actualHours === null || row.actualHours === undefined
    ? null : Number(row.actualHours)
  editingCellId.value = null
  editingValue.value = null
  if (value === original) return
  savingCellId.value = row.wlId
  try {
    const res = await workloadApi.updateActualHours(row.wlId, {
      currentTeacherName: teacherName,
      actualHours: value
    })
    if (res.code === '200') {
      ElMessage.success('实际学时修改成功，占比已重新计算')
      await fetchData()
    } else {
      ElMessage.error(res.msg || '修改失败，记录可能已确认')
    }
  } catch (error) {
    ElMessage.error('修改失败')
  } finally {
    savingCellId.value = null
  }
}

const confirmRow = async row => {
  try {
    await ElMessageBox.confirm('确认后不能再修改实际学时，是否继续？', '确认课时', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    const res = await workloadApi.confirm(row.wlId, { currentTeacherName: teacherName })
    if (res.code === '200') {
      ElMessage.success('确认成功')
      await fetchData()
    } else {
      ElMessage.error(res.msg || '确认失败，记录可能已确认')
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error('确认失败')
  }
}

const formatRatio = value => value === null || value === undefined ? '' : `${Number(value).toFixed(2)}%`

onMounted(async () => {
  await fetchSemesters()
  await fetchData()
})
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
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

.completion-item.success strong { color: #67c23a; }
.completion-item.warning strong { color: #e6a23c; }
.completion-item.rate strong { color: #409eff; }

/* 标题下方的操作范围提示 */
.page-tip {
  margin: 4px 0 0;
  font-size: 13px;
  color: #909399;
}

/* 行内编辑：待确认行实际学时单元格的可点击样式（高亮可修改字段） */
.editable-cell {
  cursor: pointer;
  display: inline-block;
  min-width: 36px;
  padding: 2px 8px;
  border: 1px dashed #409eff;
  border-radius: 4px;
  color: #409eff;
  font-weight: 600;
  background-color: #ecf5ff;
  transition: background-color 0.15s;
}

.editable-cell:hover {
  background-color: #d9ecff;
}

/* 行内编辑态：输入框 + ✓/✕ 按钮横向排列 */
.cell-editing {
  display: flex;
  align-items: center;
  gap: 2px;
}

.cell-editing-input {
  flex: 1;
  min-width: 0;
}

@media (max-width: 1100px) {
  .completion-cards { grid-template-columns: repeat(4, minmax(110px, 1fr)); }
}
</style>
