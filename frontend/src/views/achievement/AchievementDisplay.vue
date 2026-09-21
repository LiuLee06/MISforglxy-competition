<template>
  <div class="page-container">
    <div class="page-header">
      <h2>成果信息展示</h2>
    </div>

    <el-card class="search-card" shadow="hover">
      <el-form :model="query" inline class="search-form" @submit.prevent>
        <el-form-item label="成果类别">
          <el-select v-model="query.category" placeholder="全部类别" clearable style="width: 160px">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="成果级别">
          <el-select v-model="query.level" placeholder="全部级别" clearable style="width: 160px">
            <el-option v-for="l in levels" :key="l" :label="l" :value="l" />
          </el-select>
        </el-form-item>
        <el-form-item label="获得时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item label="成果名称">
          <el-input v-model="query.name" placeholder="请输入成果名称" clearable style="width: 180px" @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="query.person" placeholder="请输入姓名" clearable style="width: 160px" @keyup.enter="search" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="search">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          <el-button type="success" :icon="Download" @click="exportData">导出数据</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover">
          <div class="stat-title">{{ card.label }}</div>
          <div class="stat-num">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="table-card" shadow="hover">
      <el-table :data="list" v-loading="loading" stripe border>
        <el-table-column prop="category" label="成果类别" width="130" show-overflow-tooltip />
        <el-table-column prop="classifyLevel" label="分类等级" width="100" />
        <el-table-column prop="name" label="成果名称" min-width="230" show-overflow-tooltip />
        <el-table-column prop="persons" label="姓名" min-width="150" show-overflow-tooltip />
        <el-table-column label="成果级别" width="100">
          <template #default="{ row }">
            <el-tag type="warning" effect="light" size="small">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="成果等级" width="100">
          <template #default="{ row }">
            <el-tag effect="plain" size="small">{{ row.grade }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="achieveDate" label="获得时间" width="120" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="info" plain @click="preview(row)">查看图片</el-button>
            <el-button size="small" type="primary" plain @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>暂无数据</template>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="load"
          @current-change="load"
        />
      </div>
    </el-card>

    <!-- 可视化图表 -->
    <el-row :gutter="16" class="chart-row" v-loading="loading">
      <el-col :span="12">
        <el-card shadow="hover">
          <div ref="categoryChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <div ref="levelChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16" class="chart-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <div ref="yearChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图片预览 -->
    <el-dialog v-model="previewVisible" title="图片预览" width="760px" align-center>
      <div class="preview-wrap">
        <el-image
          v-if="previewRow && /\.(png|jpe?g)(\?.*)?$/i.test(previewRow.fileUrl || '')"
          :src="previewRow.fileUrl"
          :preview-src-list="[previewRow.fileUrl]"
          preview-teleported
          fit="contain"
          style="width: 100%; height: 60vh"
        />
        <template v-else-if="previewRow && /\.pdf$/i.test(previewRow.fileUrl || '')">
          <iframe :src="previewRow.fileUrl" class="pdf-frame"></iframe>
        </template>
        <el-empty v-else description="该记录没有可预览的图片" />
      </div>
    </el-dialog>

    <!-- 编辑成果信息 -->
    <el-dialog v-model="editVisible" title="编辑成果信息" width="640px" destroy-on-close>
      <el-form ref="editRef" :model="editForm" :rules="editRules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="成果类别" prop="category">
              <el-select v-model="editForm.category" style="width: 100%" @change="editForm.classifyLevel = ''">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成果分类等级" prop="classifyLevel">
              <el-select v-model="editForm.classifyLevel" style="width: 100%" :disabled="!editForm.category">
                <el-option v-for="l in (categoryLevels[editForm.category] || [])" :key="l" :label="l" :value="l" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="成果名称" prop="name">
              <el-input v-model="editForm.name" maxlength="200" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="姓名" prop="persons">
              <el-input v-model="editForm.persons" maxlength="500" placeholder="多人用、分隔" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成果级别" prop="level">
              <el-select v-model="editForm.level" style="width: 100%">
                <el-option v-for="l in levels" :key="l" :label="l" :value="l" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成果等级" prop="grade">
              <el-select v-model="editForm.grade" style="width: 100%" allow-create filterable>
                <el-option v-for="g in grades" :key="g" :label="g" :value="g" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="获得时间" prop="achieveDate">
              <el-date-picker v-model="editForm.achieveDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发证单位" prop="issuer">
              <el-input v-model="editForm.issuer" maxlength="200" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="比赛名称">
              <el-input v-model="editForm.contestName" maxlength="200" placeholder="可选" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { achievementApi } from '../../api/index'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const categories = ref([])
const categoryLevels = ref({})
const levels = ref([])
const grades = ref([])

const query = reactive({ category: '', level: '', name: '', person: '', page: 1, pageSize: 10 })
const dateRange = ref(null)

const stats = reactive({ total: 0, nationalCount: 0, provinceCount: 0, schoolCount: 0 })
const categoryData = ref([])
const levelData = ref([])
const yearData = ref([])

const previewVisible = ref(false)
const previewRow = ref(null)

const editVisible = ref(false)
const saving = ref(false)
const editRef = ref()
const editForm = reactive({
  id: null, category: '', classifyLevel: '', name: '', persons: '',
  level: '', grade: '', achieveDate: '', issuer: '', contestName: ''
})
const editRules = {
  category: [{ required: true, message: '请选择成果类别', trigger: 'change' }],
  name: [{ required: true, message: '请输入成果名称', trigger: 'blur' }],
  persons: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  level: [{ required: true, message: '请选择成果级别', trigger: 'change' }],
  grade: [{ required: true, message: '请选择成果等级', trigger: 'change' }],
  achieveDate: [{ required: true, message: '请选择获得时间', trigger: 'change' }],
  issuer: [{ required: true, message: '请输入发证单位', trigger: 'blur' }]
}

const statCards = computed(() => ([
  { label: '总成果数', value: stats.total },
  { label: '国家级成果', value: stats.nationalCount },
  { label: '省级成果', value: stats.provinceCount },
  { label: '校级成果', value: stats.schoolCount }
]))

// ========== 图表 ==========
const categoryChartRef = ref()
const levelChartRef = ref()
const yearChartRef = ref()
let categoryChart = null
let levelChart = null
let yearChart = null
let resizeObserver = null

const initCharts = () => {
  categoryChart?.dispose()
  levelChart?.dispose()
  yearChart?.dispose()
  categoryChart = echarts.init(categoryChartRef.value)
  levelChart = echarts.init(levelChartRef.value)
  yearChart = echarts.init(yearChartRef.value)
  renderCharts()
}

const renderCharts = () => {
  const pieOption = (title, data) => ({
    title: { text: title, left: 'center', textStyle: { fontSize: 14, fontWeight: 500 } },
    tooltip: { trigger: 'item', formatter: params => `${params.marker} ${params.name}: ${params.value} (${params.percent}%)` },
    legend: { orient: 'vertical', left: 'left', top: 'middle' },
    series: [{
      type: 'pie',
      radius: '62%',
      center: ['55%', '55%'],
      data,
      label: { formatter: '{b}: {c} ({d}%)', fontSize: 12 }
    }]
  })
  if (categoryChartRef.value) {
    initChart(categoryChart, pieOption('成果类别分布', categoryData.value))
  }
  if (levelChartRef.value) {
    levelChart.setOption(pieOption('成果级别分布', levelData.value))
  }
  yearChart.setOption({
    title: { text: '年度成果趋势', left: 'center', textStyle: { fontSize: 14, fontWeight: 500 } },
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '6%', containLabel: true },
    xAxis: { type: 'category', data: yearData.value.map(i => i.name) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      name: '成果数量',
      type: 'line',
      smooth: true,
      data: yearData.value.map(i => i.value),
      itemStyle: { color: '#409EFF' },
      areaStyle: { color: 'rgba(64,158,255,0.18)' }
    }]
  })
}

const initChart = (chart, option) => {
  if (!chart) return
  chart.setOption(option, true)
}

const loadOptions = async () => {
  try {
    const res = await achievementApi.getOptions()
    if (res.code === '200' && res.data) {
      categories.value = Object.keys(res.data.categoryLevels || {})
      categoryLevels.value = res.data.categoryLevels || {}
      levels.value = res.data.levels || []
      grades.value = res.data.grades || []
    }
  } catch (error) {
    console.error('加载成果字典失败:', error)
  }
}

const buildParams = () => ({
  status: 1,
  category: query.category || undefined,
  level: query.level || undefined,
  name: query.name || undefined,
  person: query.person || undefined,
  startDate: dateRange.value?.[0],
  endDate: dateRange.value?.[1]
})

const load = async () => {
  loading.value = true
  try {
    const res = await achievementApi.getList({
      ...buildParams(),
      page: query.page,
      size: query.pageSize
    })
    if (res.code === '200' && res.data) {
      list.value = res.data.list || []
      total.value = Number(res.data.total || 0)
    }
  } catch (error) {
    console.error('加载成果列表失败:', error)
  } finally {
    loading.value = false
  }
}

/** 后端返回的数量都是字符串（全局 Long/Integer 序列化策略），图表统一转成数字 */
const toPoints = (rows) => (rows || []).map(i => ({ name: i.name, value: Number(i.value || 0) }))

const loadStats = async () => {
  try {
    const res = await achievementApi.getStats(buildParams())
    if (res.code === '200' && res.data) {
      stats.total = Number(res.data.total || 0)
      stats.nationalCount = Number(res.data.nationalCount || 0)
      stats.provinceCount = Number(res.data.provinceCount || 0)
      stats.schoolCount = Number(res.data.schoolCount || 0)
      // 后端统一把数字序列化为字符串（Long/Integer 防精度丢失），图表取值时转回数字
      categoryData.value = toPoints(res.data.byCategory)
      levelData.value = toPoints(res.data.byLevel)
      yearData.value = toPoints(res.data.byYear)
      renderCharts()
    }
  } catch (error) {
    console.error('加载成果统计失败:', error)
  }
}

const search = async () => {
  query.page = 1
  await Promise.all([load(), loadStats()])
}

const resetQuery = async () => {
  query.category = ''
  query.level = ''
  query.name = ''
  query.person = ''
  dateRange.value = null
  query.page = 1
  await Promise.all([load(), loadStats()])
}

const exportData = async () => {
  try {
    const res = await achievementApi.getList({ ...buildParams(), page: 1, size: 10000 })
    if (res.code !== '200' || !res.data) {
      ElMessage.error(res.msg || '导出失败')
      return
    }
    const rows = res.data.list || []
    const header = ['成果类别', '分类等级', '成果名称', '姓名', '成果级别', '成果等级', '获得时间', '发证单位', '比赛名称', '提交时间']
    const body = rows.map(r => [
      r.category, r.classifyLevel || '', r.name, r.persons, r.level, r.grade,
      r.achieveDate, r.issuer, r.contestName || '', r.submitTime
    ].map(v => `"${String(v ?? '').replace(/"/g, '""')}"`).join(','))
    const csv = '\ufeff' + header.join(',') + '\n' + body.join('\n')
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
    const a = document.createElement('a')
    a.href = URL.createObjectURL(blob)
    a.download = '成果数据.csv'
    a.click()
    URL.revokeObjectURL(a.href)
    ElMessage.success(`已导出 ${rows.length} 条数据`)
  } catch (error) {
    console.error('导出成果数据失败:', error)
  }
}

const preview = (row) => {
  previewRow.value = row
  previewVisible.value = true
}

const openEdit = (row) => {
  Object.assign(editForm, {
    id: row.id,
    category: row.category,
    classifyLevel: row.classifyLevel || '',
    name: row.name,
    persons: row.persons,
    level: row.level,
    grade: row.grade,
    achieveDate: row.achieveDate,
    issuer: row.issuer,
    contestName: row.contestName || ''
  })
  editVisible.value = true
}

const saveEdit = async () => {
  await editRef.value.validate()
  saving.value = true
  try {
    const { id, ...rest } = editForm
    const res = await achievementApi.update({ id, ...rest })
    if (res.code === '200') {
      ElMessage.success('修改已保存')
      editVisible.value = false
      await Promise.all([load(), loadStats()])
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (error) {
    console.error('保存成果修改失败:', error)
  } finally {
    saving.value = false
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除「${row.name}」这条成果吗？删除后不可恢复。`, '删除确认', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
  } catch {
    return
  }
  try {
    const res = await achievementApi.remove(row.id)
    if (res.code === '200') {
      ElMessage.success('删除成功')
      await Promise.all([load(), loadStats()])
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    console.error('删除成果失败:', error)
  }
}

onMounted(async () => {
  await loadOptions()
  await Promise.all([load(), loadStats()])
  nextTick(() => {
    initCharts()
    resizeObserver = new ResizeObserver(() => {
      categoryChart?.resize()
      levelChart?.resize()
      yearChart?.resize()
    })
    resizeObserver.observe(categoryChartRef.value)
    resizeObserver.observe(levelChartRef.value)
    resizeObserver.observe(yearChartRef.value)
  })
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  categoryChart?.dispose()
  levelChart?.dispose()
  yearChart?.dispose()
})
</script>

<style scoped>
.stat-row { margin: 16px 0; }
.stat-title { font-size: 14px; color: #666; }
.stat-num { font-size: 28px; font-weight: bold; color: #409eff; margin-top: 10px; }
.pager { margin-top: 16px; display: flex; justify-content: flex-end; }
.chart-row { margin-top: 16px; }
.chart-box { width: 100%; height: 340px; }
.preview-wrap { text-align: center; }
.pdf-frame { width: 100%; height: 60vh; border: none; }
</style>
