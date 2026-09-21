<template>
  <div class="page-container">
    <div class="page-header">
      <h2>成果信息人工验证</h2>
    </div>

    <el-card class="search-card" shadow="hover">
      <el-form :inline="true" :model="query" class="search-form" @submit.prevent>
        <el-form-item label="成果类别">
          <el-select v-model="query.category" placeholder="全部类别" clearable style="width: 180px">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="提交时间">
          <el-date-picker
            v-model="submitRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="query.person" placeholder="请输入姓名" clearable style="width: 160px" @keyup.enter="load" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="load">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          <el-button type="success" :icon="RefreshRight" :loading="loading" @click="load">刷新</el-button>
        </el-form-item>
        <el-form-item>
          <span class="total-tip">共 {{ total }} 条待验证记录</span>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="hover" style="margin-top: 16px;">
      <div class="batch-bar">
        <el-button type="success" plain :disabled="!selection.length" @click="onBatchPass">
          <el-icon><CircleCheck /></el-icon>&nbsp;批量通过{{ selection.length ? ` (${selection.length})` : ' (0)' }}
        </el-button>
        <el-button type="danger" plain :disabled="!selection.length" @click="onBatchDelete">
          <el-icon><Delete /></el-icon>&nbsp;批量删除
        </el-button>
      </div>

      <el-table
        ref="tableRef"
        :data="list"
        v-loading="loading"
        stripe
        border
        @selection-change="onSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="category" label="成果类别" width="130" show-overflow-tooltip />
        <el-table-column prop="classifyLevel" label="分类等级" width="100" />
        <el-table-column prop="name" label="成果名称" min-width="220" show-overflow-tooltip />
        <el-table-column prop="persons" label="姓名" min-width="150" show-overflow-tooltip />
        <el-table-column prop="level" label="成果级别" width="100" />
        <el-table-column prop="grade" label="成果等级" width="100" />
        <el-table-column prop="submitTime" label="提交时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="info" plain @click="preview(row)">查看图片</el-button>
            <el-button size="small" type="success" plain @click="passOne(row)">通过</el-button>
            <el-button size="small" type="primary" plain @click="openEdit(row)">编辑</el-button>
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
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, RefreshRight, CircleCheck, Delete } from '@element-plus/icons-vue'
import { achievementApi } from '../../api/index'

const loading = ref(false)
const tableRef = ref()
const list = ref([])
const total = ref(0)
const selection = ref([])
const categories = ref([])
const categoryLevels = ref({})
const levels = ref([])
const grades = ref([])

const query = reactive({ category: '', person: '', page: 1, pageSize: 10 })
const submitRange = ref(null)

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

const load = async () => {
  loading.value = true
  try {
    const res = await achievementApi.getList({
      status: 0,
      category: query.category || undefined,
      person: query.person || undefined,
      submitStart: submitRange.value?.[0],
      submitEnd: submitRange.value?.[1],
      page: query.page,
      size: query.pageSize
    })
    if (res.code === '200' && res.data) {
      list.value = res.data.list || []
      total.value = Number(res.data.total || 0)
      selection.value = []
      // 数据到位后重算表格布局，避免首次进入只渲染出部分行
      await nextTick()
      tableRef.value?.doLayout()
    }
  } catch (error) {
    console.error('加载待验证成果失败:', error)
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.category = ''
  query.person = ''
  submitRange.value = null
  query.page = 1
  load()
}

const onSelectionChange = (rows) => { selection.value = rows }

const passOne = async (row) => {
  try {
    const res = await achievementApi.verify(row.id, 1)
    if (res.code === '200') {
      ElMessage.success('该成果已通过验证')
      load()
    } else {
      ElMessage.error(res.msg || '审核失败')
    }
  } catch (error) {
    console.error('成果审核失败:', error)
  }
}

const onBatchPass = async () => {
  const ids = selection.value.map(r => r.id)
  try {
    await ElMessageBox.confirm(`确定要批量通过这 ${ids.length} 条记录吗？`, '批量审核确认', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
  } catch {
    return
  }
  try {
    const res = await achievementApi.batchPass(ids)
    if (res.code === '200') {
      ElMessage.success(`批量通过 ${res.data?.updated || 0} 条数据，已正式入库`)
      load()
    } else {
      ElMessage.error(res.msg || '批量通过失败')
    }
  } catch (error) {
    console.error('批量通过失败:', error)
  }
}

const onBatchDelete = async () => {
  const ids = selection.value.map(r => r.id)
  try {
    await ElMessageBox.confirm(`确定要批量删除这 ${ids.length} 条记录吗？删除后不可恢复。`, '批量删除确认', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
  } catch {
    return
  }
  try {
    const res = await achievementApi.batchDelete(ids)
    if (res.code === '200') {
      ElMessage.success(`批量删除 ${res.data?.deleted || 0} 条数据`)
      load()
    } else {
      ElMessage.error(res.msg || '批量删除失败')
    }
  } catch (error) {
    console.error('批量删除失败:', error)
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
      load()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (error) {
    console.error('保存成果修改失败:', error)
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await loadOptions()
  await load()
})
</script>

<style scoped>
.batch-bar { margin-bottom: 12px; }
.total-tip { color: #909399; font-size: 14px; }
.pager { margin-top: 16px; display: flex; justify-content: flex-end; }
.preview-wrap { text-align: center; }
.pdf-frame { width: 100%; height: 60vh; border: none; }
</style>
