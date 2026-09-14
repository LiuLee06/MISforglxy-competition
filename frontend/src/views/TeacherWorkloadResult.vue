<template>
  <div class="page-container">
    <div class="page-header">
      <h2>我的工作量</h2>
      <p class="page-tip">提示：工作量结果由教学办统一计算后发布，您仅可查看，如有疑问请联系教学办。</p>
    </div>

    <el-card class="search-card" shadow="hover">
      <template #header>
        <div class="header">
          <span>{{ teacherName }} 的工作量</span>
          <div class="header-actions">
            <el-select v-model="semesterId" placeholder="全部学期" clearable style="width: 210px">
              <el-option
                v-for="semester in semesterList"
                :key="semester.semesterId"
                :label="semester.semesterName"
                :value="semester.semesterId"
              />
            </el-select>
            <el-button type="primary" @click="fetchList">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="year" label="学年" width="90" />
        <el-table-column prop="semesterName" label="学期" min-width="170" show-overflow-tooltip />
        <el-table-column prop="studentLimit" label="人数限值 c" width="110" />
        <el-table-column prop="totalHours" label="合计执行学时" width="120" />
        <el-table-column prop="workloadHours" label="工作量合计 G" width="130">
          <template #default="{ row }">
            <span class="workload-hours">{{ row.workloadHours }}</span>
          </template>
        </el-table-column>
        <el-table-column label="计算时间" min-width="160">
          <template #default="{ row }">{{ formatTime(row.calcTime) }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && tableData.length === 0" description="暂无您的工作量计算结果" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { semesterApi, workloadResultApi } from '../api/index'

// 当前登录教师（与"我的教学课时"页面保持一致的取值方式）
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const teacherName = String(userInfo.name ?? '').trim()

// ==================== 学期数据 ====================
const semesterList = ref([])
const semesterId = ref('')

// ==================== 列表数据 ====================
const tableData = ref([])
const loading = ref(false)

// 加载学期列表，默认选中当前学期
const loadSemesters = async () => {
  try {
    const res = await semesterApi.getAll()
    if (res.code === '200') {
      semesterList.value = res.data || []
      const currentSemester = semesterList.value.find(item => Number(item.isCurrent) === 1)
      if (currentSemester) semesterId.value = currentSemester.semesterId
    }
  } catch (err) {
    console.error('加载学期列表失败:', err)
  }
}

// 查询自己的计算结果（SQL 层按当前教师姓名过滤）
const fetchList = async () => {
  loading.value = true
  try {
    const params = { currentTeacherName: teacherName }
    if (semesterId.value) params.semesterId = semesterId.value
    const res = await workloadResultApi.getMyList(params)
    if (res.code === '200') {
      tableData.value = res.data || []
    } else {
      ElMessage.error(res.msg || '查询工作量结果失败')
    }
  } catch (err) {
    console.error('查询工作量结果失败:', err)
  } finally {
    loading.value = false
  }
}

// 重置筛选
const resetFilter = () => {
  semesterId.value = ''
  fetchList()
}

// 格式化计算时间：2026-09-13T20:30:00 → 2026-09-13 20:30:00
const formatTime = (value) => {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 19)
}

onMounted(() => {
  loadSemesters()
  fetchList()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0 0 4px;
}

.page-tip {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.workload-hours {
  font-weight: 600;
  color: #409eff;
}
</style>
