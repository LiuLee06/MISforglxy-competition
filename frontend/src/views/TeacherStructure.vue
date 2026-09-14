<template>
  <div class="page-container">
    <div class="page-header">
      <h2>专任教师结构管理</h2>
    </div>
    <el-card class="search-card" shadow="hover">
    <el-row :gutter="20" style="margin: 0;">
      <el-col :span="8">
        <el-select
          v-model="selectedDeptId"
          filterable
          clearable
          placeholder="请选择部门查看分布"
          @change="updateChart"
          style="width: 100%;"
        >
          <el-option label="全部部门" value="" />
          <el-option
            v-for="dept in deptList"
            :key="dept.dept_id"
            :label="dept.dept_name"
            :value="dept.dept_id"
          />
        </el-select>
      </el-col>
      <el-col :span="16" style="display: flex; align-items: center;">
        <div>当前查看：<strong>{{ selectedDeptName }}</strong></div>
      </el-col>
    </el-row>
    </el-card>
    <!-- 统计卡片区域 -->
    <el-row :gutter="20" style="margin: 20px 0;">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">专任教师总数</div>
          <div class="stat-num">{{ selectedTotalNum }} 人</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">教授</div>
          <div class="stat-num">{{ selectedProfessorNum }} 人</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">副教授</div>
          <div class="stat-num">{{ selectedAssociateNum }} 人</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">讲师</div>
          <div class="stat-num">{{ selectedLecturerNum }} 人</div>
        </el-card>
      </el-col>
    </el-row>
    <!-- ECharts 图表 -->
    <el-row :gutter="20" style="margin: 20px 0;" v-loading="loading">
      <el-col :span="12">
        <el-card title="职称分布占比">
          <div id="titleChart" style="width: 100%; height: 400px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card title="各部门教师人数">
         <div id="deptChart" style="width: 100%; height: 400px;"></div> </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin: 20px 0;">
      <el-col :span="12">
        <el-card title="教师年龄分布">
          <div id="ageChart" style="width: 100%; height: 320px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card title="博士学历分布">
          <div id="degreeChart" style="width: 100%; height: 320px;"></div>
        </el-card>
      </el-col>
    </el-row>
    <!-- 部门列表 + 增删改查 -->
    <div style="margin-top: 20px;">
      <h3>部门信息列表</h3>
      <el-table :data="displaySummary" border style="width: 100%; margin-top: 10px;">
        <el-table-column prop="dept_id" label="部门编号" width="120" />
        <el-table-column prop="dept_name" label="部门名称" width="180" />
        <el-table-column prop="teacherCount" label="教师人数" width="120" />
        <el-table-column prop="ratio" label="人数占比" width="120" />
        <el-table-column prop="dept_desc" label="部门简介" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button v-if="hasPermission('dept:edit')" size="small" @click="openDeptDialog(row)">编辑</el-button>
            <el-button v-if="hasPermission('dept:delete')" size="small" type="danger" @click="delDept(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!-- 部门新增/编辑弹窗 -->
<el-dialog v-model="deptDialogVisible" title="部门信息编辑" width="500px">
  <el-form :model="deptForm" label-width="100px">
    <el-form-item label="部门编号">
        <el-input
          v-model="deptForm.dept_id"
          :disabled="true"
          placeholder="自动生成，无需填写"
        ></el-input>
    </el-form-item>
    <el-form-item label="部门名称">
      <el-input v-model="deptForm.dept_name" placeholder="请输入部门名称"></el-input>
    </el-form-item>
    <el-form-item label="部门简介">
      <el-input v-model="deptForm.dept_desc" type="textarea" rows="3"></el-input>
    </el-form-item>
    <el-form-item label="教师人数">
      <el-input-number v-model="deptForm.teacherCount" :min="0" />
    </el-form-item>
  </el-form>
  <template #footer>
    <el-button @click="deptDialogVisible = false">取消</el-button>
    <el-button v-if="hasPermission('dept:edit')" type="primary" @click="saveDept">保存</el-button>
  </template>
</el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { teacherApi, deptApi } from '../api/index'

// ========== 权限相关 ==========
const actionCodes = JSON.parse(localStorage.getItem('userInfo') || '{}').actionCodes || []
const hasPermission = (code) => actionCodes.includes(code)

// ========== 组件内部状态 ==========
const teacherList = ref([])
const deptList = ref([])

// ========== 工具函数==========
const normalizeValue = (value) => {
  if (value == null) return ''
  return String(value).trim()
}

const parseDeptValue = (value) => {
  if (Array.isArray(value)) return value.map(v => normalizeValue(v)).filter(Boolean)
  if (value == null) return []
  return String(value)
    .split(/[,、;；|\n\r]+/)
    .map(item => normalizeValue(item))
    .filter(Boolean)
}

// ========== 加载数据函数 ==========
const loadTeachers = async () => {
  try {
    const res = await teacherApi.getAll()
    if (res.code === '200' && res.data) {
      teacherList.value = res.data.map(t => ({
        ...t,
        deptId: t.deptId,
        deptName: t.dept,
        dept_id: t.deptId || t.dept,
        dept_name: t.dept,
        title: t.title || t.professionalTitle || t.professional_title || '',
        professionalTitle: t.professionalTitle || t.professional_title || t.title || '',
        degree: t.degree || t.Degree || t.education || t.Education || '',
        staffNo: t.staffNo ?? t.staff_no ?? '',
        education: t.education ?? t.Education ?? '',
        isRetired: ['YES', '1', 1, true].includes(t.isRetired ?? t.is_retired ?? t.retired ?? ''),
        isFullTime: ['YES', '1', 1, true].includes(t.isFullTime ?? t.fullTime ?? t.is_full_time ?? '')
      }))
    } else {
      teacherList.value = []
    }
  } catch (error) {
    console.error('加载教师数据失败:', error)
    teacherList.value = []
  }
}

const loadDepts = async () => {
  try {
    const res = await deptApi.getAll()
    if (res.code === '200' && res.data) {
      deptList.value = res.data.map(d => ({
        ...d,
        dept_id: d.deptId,
        dept_name: d.deptName,
        dept_desc: d.deptDesc
      }))
    } else {
      deptList.value = []
    }
  } catch (error) {
    console.error('加载部门数据失败:', error)
    deptList.value = []
  }
}

// ========== 部门后端操作方法） ==========
const createDept = async (data) => {
  const res = await deptApi.create(data)
  if (res.code === '200') {
    await loadDepts()
  }
  return res
}

const updateDeptToBackend = async (data) => {
  const payload = {
    deptId: data.deptId || data.dept_id,
    deptName: data.deptName || data.dept_name,
    deptDesc: data.deptDesc || data.dept_desc
  }
  const res = await deptApi.update(payload)
  if (res.code === '200') {
    await loadDepts()
  }
  return res
}

const deleteDeptFromBackend = async (deptId) => {
  const res = await deptApi.delete(deptId)
  if (res.code === '200') {
    await loadDepts()
  }
  return res
}

// ========== 页面原有本地状态 ==========
const deptDialogVisible = ref(false)
const isEditDept = ref(false)
const deptForm = ref({
  dept_id: '',
  dept_name: '',
  dept_desc: '',
  teacherCount: 0
})
const selectedDeptId = ref('')
const loading = ref(false)

const selectedDeptName = computed(() => {
  if (!selectedDeptId.value) return '全部部门'
  const dept = deptList.value.find(item => item.dept_id === selectedDeptId.value)
  return dept ? dept.dept_name : '未知部门'
})

const filteredTeacherList = computed(() => {
  if (!selectedDeptId.value) return teacherList.value
  const selectedKey = normalizeValue(selectedDeptId.value)
  const selectedDept = deptList.value.find(item => normalizeValue(item.dept_id) === selectedKey)
  const selectedDeptName = selectedDept ? normalizeValue(selectedDept.dept_name) : selectedKey
  return teacherList.value.filter(item => {
    const teacherDeptIds = parseDeptValue(item.dept_id)
    const teacherDeptNames = parseDeptValue(item.dept || item.deptName || item.dept_name)
    return teacherDeptIds.includes(selectedKey) || teacherDeptNames.includes(selectedDeptName) || teacherDeptIds.includes(selectedDeptName)
  })
})

const filteredFullTimeTeacherList = computed(() => filteredTeacherList.value.filter(item => item.isFullTime && !item.isRetired))
const selectedTotalNum = computed(() => filteredFullTimeTeacherList.value.length)

const getTitle = (item) => (item.professionalTitle || item.professional_title || item.title || '').trim()
const selectedProfessorNum = computed(() => filteredFullTimeTeacherList.value.filter(item => getTitle(item).includes('教授') && !getTitle(item).includes('副')).length)
const selectedAssociateNum = computed(() => filteredFullTimeTeacherList.value.filter(item => getTitle(item).includes('副教授')).length)
const selectedLecturerNum = computed(() => filteredFullTimeTeacherList.value.filter(item => getTitle(item).includes('讲师')).length)

const selectedAgeDistribution = computed(() => {
  const groups = { '≤30岁': 0, '31-40岁': 0, '41-50岁': 0, '>50岁': 0 }
  const getAge = (birth) => {
    if (!birth) return 0
    const birthDate = new Date(birth)
    const now = new Date()
    let age = now.getFullYear() - birthDate.getFullYear()
    const monthDiff = now.getMonth() - birthDate.getMonth()
    const dayDiff = now.getDate() - birthDate.getDate()
    if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) age--
    return age
  }
  filteredFullTimeTeacherList.value.forEach(item => {
    const age = getAge(item.birth)
    if (age <= 30) groups['≤30岁']++
    else if (age <= 40) groups['31-40岁']++
    else if (age <= 50) groups['41-50岁']++
    else groups['>50岁']++
  })
  return Object.entries(groups).map(([name, value]) => ({ name, value }))
})

const selectedDegreeDistribution = computed(() => {
  const counts = { 博士: 0, 硕士: 0, 本科及以下: 0 }
  filteredFullTimeTeacherList.value.forEach(item => {
    if (item.degree === '博士') counts['博士']++
    else if (item.degree === '硕士') counts['硕士']++
    else counts['本科及以下']++
  })
  return Object.entries(counts).map(([name, value]) => ({ name, value }))
})

// 部门汇总表格
const deptSummary = computed(() => {
  const fullTimeTeacherList = teacherList.value.filter(t => t.isFullTime && !t.isRetired)
  const total = fullTimeTeacherList.length
  return deptList.value.map(dept => {
    const deptKey = String(dept.dept_id || '').trim()
    const deptName = String(dept.dept_name || '').trim()
    const teacherCount = fullTimeTeacherList.filter(t => {
      const teacherDeptKeys = parseDeptValue(t.dept_id || '')
      const teacherDeptNames = parseDeptValue(t.dept || t.deptName || t.dept_name || '')
      return teacherDeptKeys.includes(deptKey) || teacherDeptNames.includes(deptName)
    }).length
    const ratio = total > 0 ? ((teacherCount / total) * 100).toFixed(1) + '%' : '0%'
    return {
      dept_id: dept.dept_id,
      dept_name: dept.dept_name,
      dept_desc: dept.dept_desc,
      teacherCount,
      ratio
    }
  })
})

const displaySummary = computed(() => {
  const summary = deptSummary.value.map(item => ({ ...item }))
  if (!selectedDeptId.value) return summary
  return summary.filter(item => item.dept_id === selectedDeptId.value)
})

// ========== Echarts实例 ==========
let titleChart = null
let deptChart = null
let ageChart = null
let degreeChart = null

const initChart = () => {
  titleChart = echarts.init(document.getElementById('titleChart'))
  deptChart = echarts.init(document.getElementById('deptChart'))
  ageChart = echarts.init(document.getElementById('ageChart'))
  degreeChart = echarts.init(document.getElementById('degreeChart'))
  updateChart()
}

const updateChart = () => {
  const pieData = [
    { name: '教授', value: selectedProfessorNum.value },
    { name: '副教授', value: selectedAssociateNum.value },
    { name: '讲师', value: selectedLecturerNum.value }
  ]
  titleChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: params => `${params.marker} ${params.name}: ${params.value} 人 (${params.percent}%)`
    },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      type: 'pie',
      radius: '70%',
      data: pieData,
      label: {
        formatter: '{b}: {c}\n({d}%)'
      }
    }]
  })

  const xData = deptList.value.map(d => d.dept_name)
  const barData = deptList.value.map(dept => {
    const countFromDept = typeof dept.teacherCount === 'number' && dept.teacherCount >= 0 ? dept.teacherCount : null
    return countFromDept !== null
      ? countFromDept
      : filteredFullTimeTeacherList.value.filter(t => {
          const teacherDeptIds = parseDeptValue(t.dept_id)
          const teacherDeptNames = parseDeptValue(t.dept || t.deptName || t.dept_name)
          const deptKey = normalizeValue(dept.dept_id)
          const deptName = normalizeValue(dept.dept_name)
          return teacherDeptIds.includes(deptKey) || teacherDeptNames.includes(deptName)
        }).length
  })
  deptChart.setOption({
    grid: {
      left: '0%',
      right: '0%',
      bottom: '10%',
      containLabel: true
    },
    tooltip: {
      trigger: 'axis',
      formatter: params => {
        const item = params[0]
        const total = barData.reduce((sum, curr) => sum + curr, 0)
        const percent = total > 0 ? ((item.value / total) * 100).toFixed(1) : '0.0'
        return `${item.marker} ${item.name}: ${item.value} 人 (${percent}%)`
      }
    },
    xAxis: {
      type: 'category',
      data: xData,
      axisLabel: {
        rotate: 45,
        interval: 0,
        align: 'right',
        verticalAlign: 'middle',
        margin: 12,
        lineHeight: 20,
        textStyle: {
          fontSize: 12
        }
      },
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f0f0f0' } }
    },
    series: [{
      name: '教师人数',
      type: 'bar',
      data: barData,
      barWidth: '24%',
      itemStyle: {
        color: '#409EFF'
      }
    }]
  })

  ageChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: params => {
        const item = params[0]
        const total = selectedAgeDistribution.value.reduce((sum, curr) => sum + curr.value, 0)
        const percent = total > 0 ? ((item.value / total) * 100).toFixed(1) : '0.0'
        return `${item.marker} ${item.name}: ${item.value} 人 (${percent}%)`
      }
    },
    xAxis: { type: 'category', data: selectedAgeDistribution.value.map(item => item.name) },
    yAxis: { type: 'value' },
    series: [{
      name: '教师人数',
      type: 'bar',
      data: selectedAgeDistribution.value.map(item => item.value),
      itemStyle: { color: '#67c23a' },
      barWidth: '40%'
    }]
  })

  degreeChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: params => `${params.marker} ${params.name}: ${params.value} 人 (${params.percent}%)`
    },
    legend: { orient: 'vertical', left: 'right' },
    series: [{
      type: 'pie',
      radius: '60%',
      data: selectedDegreeDistribution.value,
      label: {
        formatter: '{b}: {c} ({d}%)'
      }
    }]
  })
}

// 部门弹窗
const openDeptDialog = (row) => {
  deptDialogVisible.value = true
  if (row) {
    isEditDept.value = true
    deptForm.value = { ...row, teacherCount: (typeof row.teacherCount === 'number' ? row.teacherCount : 0) }
  } else {
    isEditDept.value = false
    deptForm.value = { dept_id: '', dept_name: '', dept_desc: '', teacherCount: 0 }
  }
}

const saveDept = async () => {
  if (!deptForm.value.dept_name) {
    ElMessage.warning('部门名称不能为空，编号由后端自动生成')
    return
  }
  if (isEditDept.value) {
    await updateDeptToBackend({ ...deptForm.value, teacherCount: typeof deptForm.value.teacherCount === 'number' ? deptForm.value.teacherCount : 0 })
    ElMessage.success('编辑成功')
  } else {
    const existName = deptList.value.some(d => d.dept_name === deptForm.value.dept_name)
    if (existName) {
      ElMessage.error('该部门名称已存在')
      return
    }
    const payload = { ...deptForm.value, teacherCount: typeof deptForm.value.teacherCount === 'number' ? deptForm.value.teacherCount : 0 }
    delete payload.dept_id
    await createDept(payload)
    ElMessage.success('新增成功')
  }
  deptDialogVisible.value = false
  updateChart()
}

const delDept = (row) => {
  ElMessageBox.confirm('确定删除该部门？', '提示', { type: 'warning' })
    .then(async () => {
      loading.value = true
      try {
        await deleteDeptFromBackend(row.dept_id)
        ElMessage.success('删除成功')
      } catch (error) {
        ElMessage.error(error?.message || '删除失败，请检查后端服务是否已启动')
      } finally {
        loading.value = false
      }
    })
    .catch(() => {
      ElMessage.info('已取消')
    })
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([loadDepts(), loadTeachers()])
  } finally {
    loading.value = false
  }
  nextTick(() => {
    initChart()
  })
})

watch([
  () => teacherList.value.length,
  () => deptList.value.length,
  () => selectedDeptId.value
], () => {
  if (titleChart && deptChart && ageChart && degreeChart) {
    updateChart()
  }
}, { immediate: true })
</script>

<style scoped>
.stat-title {
  font-size: 14px;
  color: #666;
}
.stat-num {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
  margin-top: 10px;
}
</style>
