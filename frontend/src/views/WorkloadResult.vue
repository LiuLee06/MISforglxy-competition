<template>
  <div class="page-container">
    <div class="page-header">
      <h2>工作量计算</h2>
      <p class="page-tip">提示：计算以学期为单位覆盖保存；工作量 G = Σ 课程系数 a × 人数系数 b × 实际学时。</p>
    </div>

    <el-card class="search-card" shadow="hover">
      <template #header>
        <div class="header">
          <span>工作量计算</span>
          <div class="header-actions">
            <el-select v-model="calcSemesterId" placeholder="请选择学期" style="width: 210px">
              <el-option
                v-for="semester in semesterList"
                :key="semester.semesterId"
                :label="semester.semesterName"
                :value="semester.semesterId"
              />
            </el-select>
            <span class="limit-label">人数限值 c</span>
            <el-input-number v-model="studentLimit" :min="1" :step="1" :precision="0" style="width: 120px" />
            <el-button type="primary" :loading="calcLoading" @click="handleCalculate">计算</el-button>
          </div>
        </div>
      </template>
      <div class="formula-tip">
        <p>课程系数 a：课程编号 GL 开头 = 1.0，其他 = 1.15</p>
        <p>人数系数 b：人数 ≤ c 时 b = 1；c &lt; 人数 ≤ 2c 时 b = (人数-c)/c×0.5+1；人数 &gt; 2c 时 b = (人数-2c)/c×0.4+1.5</p>
      </div>
    </el-card>

    <el-card class="search-card" shadow="hover">
      <template #header>
        <div class="header">
          <span>计算结果</span>
          <div class="header-actions">
            <el-select v-model="yearId" placeholder="全部学年" clearable style="width: 140px" @change="handleYearChange">
              <el-option v-for="y in yearOptions" :key="y" :label="y" :value="y" />
            </el-select>
            <el-select v-model="semesterId" placeholder="全部学期" clearable style="width: 210px" @change="handleSemesterChange">
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
        <el-table-column label="学年" width="90">
          <template #default="{ row }">{{ row.year || '—' }}</template>
        </el-table-column>
        <el-table-column label="学期" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.semesterName || '学年合计' }}</template>
        </el-table-column>
        <el-table-column prop="teacherNo" label="教师编号" width="110" />
        <el-table-column prop="teacherName" label="教师姓名" width="110" />
        <el-table-column label="人数限值 c" width="110">
          <template #default="{ row }">{{ row.studentLimit ?? '—' }}</template>
        </el-table-column>
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
      <el-empty v-if="!loading && tableData.length === 0" description="暂无工作量计算结果" />
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { semesterApi, workloadResultApi } from '../api/index'

// ==================== 学期数据 ====================
const semesterList = ref([])
const calcSemesterId = ref('') // 计算用的学期
const semesterId = ref('')     // 结果筛选用的学期
const yearId = ref('')         // 结果筛选用的学年（SEMESTER.year，同一学年两个学期共用同一值）
const studentLimit = ref(32)   // 人数限值 c，默认 32

// 学年下拉选项：从学期列表（数据库 SEMESTER 表）中提取去重后的 year，从大到小
const yearOptions = computed(() => {
  return [...new Set(semesterList.value.map(s => Number(s.year)).filter(y => !Number.isNaN(y)))]
    .sort((a, b) => b - a)
})

// 两个筛选互斥：选了学年就清空学期，选了学期就清空学年，保证同一时刻只按一个条件查询
const handleYearChange = (val) => {
  if (val) semesterId.value = ''
}

const handleSemesterChange = (val) => {
  if (val) yearId.value = ''
}

// ==================== 列表数据 ====================
const tableData = ref([])
const loading = ref(false)
const calcLoading = ref(false)

// 加载学期列表，默认选中当前学期
const loadSemesters = async () => {
  try {
    const res = await semesterApi.getAll()
    if (res.code === '200') {
      semesterList.value = res.data || []
      const currentSemester = semesterList.value.find(item => Number(item.isCurrent) === 1)
      if (currentSemester) {
        calcSemesterId.value = currentSemester.semesterId
        semesterId.value = currentSemester.semesterId
      }
    }
  } catch (err) {
    console.error('加载学期列表失败:', err)
  }
}

// 查询计算结果列表
const fetchList = async () => {
  loading.value = true
  try {
    const params = {}
    if (semesterId.value) params.semesterId = semesterId.value
    if (yearId.value) params.year = yearId.value
    const res = await workloadResultApi.getList(params)
    if (res.code === '200') {
      tableData.value = res.data || []
    } else {
      ElMessage.error(res.msg || '查询计算结果失败')
    }
  } catch (err) {
    console.error('查询计算结果失败:', err)
  } finally {
    loading.value = false
  }
}

// 重置筛选
const resetFilter = () => {
  yearId.value = ''
  semesterId.value = ''
  fetchList()
}

// 执行计算（覆盖该学期已有结果）
const handleCalculate = async () => {
  if (!calcSemesterId.value) {
    ElMessage.warning('请先选择要计算的学期')
    return
  }
  try {
    await ElMessageBox.confirm(
      '计算将覆盖该学期已有的工作量结果，是否继续？',
      '确认计算',
      { confirmButtonText: '计算', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return // 用户取消
  }

  calcLoading.value = true
  try {
    const res = await workloadResultApi.calculate({
      semesterId: calcSemesterId.value,
      studentLimit: studentLimit.value
    })
    if (res.code === '200') {
      ElMessage.success(`计算完成，共生成 ${res.data} 位教师的工作量结果`)
      semesterId.value = calcSemesterId.value
      fetchList()
    } else {
      ElMessage.error(res.msg || '计算失败')
    }
  } catch (err) {
    // 人数缺失等校验失败：后端返回 400，msg 中列出具体记录
    const msg = err.response?.data?.msg || '计算失败'
    ElMessageBox.alert(msg, '计算失败', { confirmButtonText: '知道了', type: 'error' })
  } finally {
    calcLoading.value = false
  }
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

.limit-label {
  font-size: 14px;
  color: #606266;
}

.formula-tip {
  font-size: 13px;
  color: #909399;
  line-height: 1.8;
}

.formula-tip p {
  margin: 0;
}

.workload-hours {
  font-weight: 600;
  color: #409eff;
}
</style>
