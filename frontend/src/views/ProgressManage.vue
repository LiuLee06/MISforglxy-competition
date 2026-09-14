<template>
  <div class="page-container">
    <div class="page-header">
      <h2>进度管理</h2>
    </div>

    <el-card class="search-card" shadow="hover">
      <el-form :inline="true" class="search-form" @submit.prevent="loadData">
        <el-form-item label="学期">
          <el-select v-model="searchForm.semesterId" placeholder="全部学期" clearable style="width: 180px" @change="loadData">
            <el-option label="全部学期" :value="null" />
            <el-option
              v-for="semester in semesterList"
              :key="semester.semesterId"
              :label="semester.semesterName"
              :value="semester.semesterId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="系部" v-if="isAdmin || isTeachingOffice">
          <el-select v-model="searchForm.dept" placeholder="全部系部" clearable style="width: 180px" @change="loadData">
            <el-option label="全部系部" :value="null" />
            <el-option
              v-for="dept in deptList"
              :key="dept"
              :label="dept"
              :value="dept"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">刷新</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="confirm-stats-section">
      <div class="stats-card">
        <div class="stats-title">确认进度总览<span class="stats-semester">{{ currentSemesterName }}</span></div>
        <div class="stats-list">
          <div class="stat-item">
            <div class="stat-label">总任务数</div>
            <div class="stat-value">{{ confirmStats.total || 0 }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">未确认</div>
            <div class="stat-value text-gray">{{ confirmStats.unconfirmed || 0 }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">确认无异议</div>
            <div class="stat-value text-green">{{ confirmStats.noObjection || 0 }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">已提交反馈</div>
            <div class="stat-value text-orange">{{ confirmStats.hasFeedback || 0 }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">完成率</div>
            <div class="stat-value text-blue">{{ completionRate }}%</div>
          </div>
        </div>
      </div>
    </div>

    <div class="table-section">
      <div class="table-header">
        <span class="table-title">教师完成情况明细</span>
        <el-input
          v-model="filterText"
          placeholder="搜索教师姓名"
          clearable
          style="width: 200px"
        />
      </div>
      <el-table
        :data="filteredTeacherStats"
        border
        stripe
        style="width: 100%"
        :empty-text="'暂无数据'"
      >
        <el-table-column label="序号" type="index" width="70" align="center" />
        <el-table-column label="教师姓名" prop="teacherName" width="140" align="center" />
        <el-table-column label="所在系部" prop="dept" width="160" align="center" />
        <el-table-column label="总任务数" prop="total" width="110" align="center" />
        <el-table-column label="未确认" prop="unconfirmed" width="110" align="center">
          <template #default="{ row }">
            <span :class="{ 'text-red': row.unconfirmed > 0 }">{{ row.unconfirmed }}</span>
          </template>
        </el-table-column>
        <el-table-column label="确认无异议" prop="noObjection" width="120" align="center">
          <template #default="{ row }">
            <span class="text-green">{{ row.noObjection }}</span>
          </template>
        </el-table-column>
        <el-table-column label="已提交反馈" prop="hasFeedback" width="120" align="center">
          <template #default="{ row }">
            <span class="text-orange">{{ row.hasFeedback }}</span>
          </template>
        </el-table-column>
        <el-table-column label="完成率" width="120" align="center">
          <template #default="{ row }">
            <el-progress
              :percentage="getCompletionRate(row)"
              :color="getProgressColor(getCompletionRate(row))"
              :stroke-width="14"
              :text-inside="true"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row)" size="small">
              {{ getStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="summary-section" v-if="teacherStats.length">
      <div class="summary-card">
        <div class="summary-title">未完成教师列表</div>
        <div class="summary-content">
          <el-tag
            v-for="teacher in unconfirmedTeachers"
            :key="teacher.teacherId"
            type="danger"
            size="large"
            class="teacher-tag"
          >
            {{ teacher.teacherName }}（{{ teacher.unconfirmed }}项未确认）
          </el-tag>
          <span v-if="!unconfirmedTeachers.length" class="all-done">全部教师已完成确认</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { originalExamApi, semesterApi, teacherApi } from '../api/index'

const teacherDept = ref('')
const teacherPosition = ref('')

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

const isTeachingOffice = computed(() => {
  if (teacherDept.value === '教学办公室') return true
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr)
      return userInfo.dept === '教学办公室'
    } catch (e) {}
  }
  return false
})

const isDeptHead = computed(() => {
  return teacherPosition.value && teacherPosition.value.includes('系主任')
})

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
      teacherDept.value = res.data.dept || ''
      teacherPosition.value = res.data.position || ''
    }
  } catch (e) {
    console.error('获取教师信息失败:', e)
  }
}

const semesterList = ref([])
const deptList = ref([])
const teacherStats = ref([])
const confirmStats = ref({})
const filterText = ref('')

const searchForm = reactive({
  semesterId: null,
  dept: null
})

const currentSemesterName = computed(() => {
  if (!searchForm.semesterId) return '（全部学期）'
  const s = semesterList.value.find(s => s.semesterId === searchForm.semesterId)
  return s ? `（${s.semesterName}）` : ''
})

const completionRate = computed(() => {
  const total = Number(confirmStats.value.total || 0)
  if (total === 0) return 0
  const done = Number(confirmStats.value.noObjection || 0) + Number(confirmStats.value.hasFeedback || 0)
  return Math.round(done * 100 / total)
})

const filteredTeacherStats = computed(() => {
  if (!filterText.value) return teacherStats.value
  return teacherStats.value.filter(t => 
    t.teacherName && t.teacherName.includes(filterText.value)
  )
})

const unconfirmedTeachers = computed(() => {
  return teacherStats.value.filter(t => Number(t.unconfirmed) > 0)
})

const getCompletionRate = (row) => {
  const total = Number(row.total || 0)
  if (total === 0) return 100
  const done = Number(row.noObjection || 0) + Number(row.hasFeedback || 0)
  return Math.round(done * 100 / total)
}

const getProgressColor = (rate) => {
  if (rate === 100) return '#67c23a'
  if (rate >= 50) return '#e6a23c'
  return '#f56c6c'
}

const getStatusTag = (row) => {
  const rate = getCompletionRate(row)
  if (rate === 100) return 'success'
  if (rate >= 50) return 'warning'
  return 'danger'
}

const getStatusText = (row) => {
  const rate = getCompletionRate(row)
  if (rate === 100) return '已完成'
  if (rate >= 50) return '进行中'
  return '待处理'
}

const fetchSemesters = async () => {
  try {
    const res = await semesterApi.getAll()
    if (res.code === '200' && res.data) {
      semesterList.value = res.data
      const current = res.data.find(s => s.isCurrent == 1)
      if (current) {
        searchForm.semesterId = current.semesterId
      }
    }
  } catch (e) {
    console.error('获取学期列表失败:', e)
  }
}

const loadConfirmStats = async () => {
  try {
    const params = { semesterId: searchForm.semesterId || null }
    if (isDeptHead.value) {
      params.dept = teacherDept.value
    } else if (searchForm.dept) {
      params.dept = searchForm.dept
    }
    const res = await originalExamApi.getConfirmStatsFlat(params)
    if (res.code === '200' && res.data) {
      confirmStats.value = res.data
    }
  } catch (err) {
    console.error('获取确认进度失败:', err)
  }
}

const loadDeptList = async () => {
  if (!isAdmin.value && !isTeachingOffice.value) return
  try {
    const res = await originalExamApi.getDistinctDept({ semesterId: searchForm.semesterId || null })
    if (res.code === '200' && res.data) {
      deptList.value = res.data
    }
  } catch (err) {
    console.error('获取系部列表失败:', err)
  }
}

const loadTeacherStats = async () => {
  try {
    const params = { semesterId: searchForm.semesterId || null }
    if (isDeptHead.value) {
      params.dept = teacherDept.value
    } else if (searchForm.dept) {
      params.dept = searchForm.dept
    }
    const res = await originalExamApi.getStatsByTeacher(params)
    if (res.code === '200' && res.data) {
      teacherStats.value = res.data.map(row => ({
        teacherId: row.teacherId,
        teacherName: row.teacherName || '',
        dept: row.dept || '',
        total: Number(row.total || 0),
        unconfirmed: Number(row.unconfirmed || 0),
        noObjection: Number(row.noObjection || 0),
        hasFeedback: Number(row.hasFeedback || 0)
      }))
    } else {
      teacherStats.value = []
    }
  } catch (err) {
    console.error('获取教师统计失败:', err)
    ElMessage.error('获取教师统计失败')
    teacherStats.value = []
  }
}

const loadData = async () => {
  await Promise.all([
    loadConfirmStats(),
    loadTeacherStats(),
    loadDeptList()
  ])
}

onMounted(async () => {
  await loadTeacherInfo()
  await fetchSemesters()
  await loadData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
  background: #ffffff;
  min-height: 100vh;
}

.page-title {
  margin: 0 0 16px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.search-section {
  background: #fff;
  padding: 20px 24px;
  border-radius: 4px;
  margin-bottom: 16px;
  border: 1px solid #ebeef5;
}

.confirm-stats-section {
  margin-bottom: 16px;
}

.stats-card {
  background: #fff;
  border-radius: 4px;
  padding: 20px 24px;
  border: 1px solid #ebeef5;
}

.stats-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 18px;
}

.stats-list {
  display: flex;
  gap: 80px;
}

.stats-semester {
  font-size: 14px;
  font-weight: 500;
  color: #409eff;
  margin-left: 12px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.text-gray { color: #909399; }
.text-green { color: #67c23a; }
.text-orange { color: #e6a23c; }
.text-red { color: #f56c6c; }
.text-blue { color: #409eff; }

.table-section {
  background: #fff;
  border-radius: 4px;
  padding: 20px 24px;
  border: 1px solid #ebeef5;
  margin-bottom: 16px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.table-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.summary-section {
  margin-bottom: 16px;
}

.summary-card {
  background: #fff;
  border-radius: 4px;
  padding: 20px 24px;
  border: 1px solid #ebeef5;
}

.summary-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.summary-content {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.teacher-tag {
  margin: 0;
}

.all-done {
  color: #67c23a;
  font-size: 14px;
}
</style>
