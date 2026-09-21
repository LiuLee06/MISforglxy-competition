<template>
  <div class="page-container knowledge-page">
    <div class="page-header knowledge-header">
      <div>
        <h2>AI 知识库</h2>
        <p>上传学院 Word 文件，处理完成后教师可以在 AI 助手中查询。</p>
      </div>
      <div class="header-actions">
        <el-button @click="loadDocuments" :loading="loading">刷新</el-button>
        <el-button type="primary" @click="chooseFile" :loading="uploading">上传 Word 文件</el-button>
        <input ref="fileInput" type="file" accept=".doc,.docx" hidden @change="handleFileChange" />
      </div>
    </div>

    <el-alert class="knowledge-tip" type="info" :closable="false">
      支持 .doc 和 .docx，单个文件不超过 20MB。系统会先提取文字和原生表格，再调用 AI 校验 Markdown、生成文件简介；处理完成前不会参与问答。
    </el-alert>

    <el-card class="knowledge-card" shadow="hover">
      <el-table :data="documents" v-loading="loading" border stripe empty-text="暂无知识库文件">
        <el-table-column prop="originalFileName" label="文件名" min-width="240" show-overflow-tooltip />
        <el-table-column prop="title" label="目录标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="summary" label="文件简介" min-width="310" show-overflow-tooltip />
        <el-table-column prop="documentType" label="类型" width="110" />
        <el-table-column label="状态" width="115">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
            <div v-if="scope.row.status === 'FAILED' && scope.row.errorMessage" class="error-tip" :title="scope.row.errorMessage">{{ scope.row.errorMessage }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="175" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="scope">
            <el-button v-if="scope.row.status === 'FAILED'" type="primary" link @click="retry(scope.row)">重试</el-button>
            <el-button type="danger" link @click="remove(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { knowledgeApi } from '../api/index.js'

const documents = ref([])
const loading = ref(false)
const uploading = ref(false)
const fileInput = ref(null)
let pollTimer = null

const loadDocuments = async () => {
  loading.value = true
  try {
    const res = await knowledgeApi.list()
    if (res.code === '200') documents.value = res.data || []
    else ElMessage.error(res.msg || '加载知识库失败')
  } catch (error) {
    ElMessage.error('加载知识库失败')
  } finally {
    loading.value = false
  }
}

const chooseFile = () => fileInput.value?.click()

const handleFileChange = async (event) => {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  const lower = file.name.toLowerCase()
  if (!lower.endsWith('.doc') && !lower.endsWith('.docx')) {
    ElMessage.warning('请选择 .doc 或 .docx 文件')
    return
  }
  if (file.size > 20 * 1024 * 1024) {
    ElMessage.warning('文件不能超过 20MB')
    return
  }
  uploading.value = true
  try {
    const res = await knowledgeApi.upload(file)
    if (res.code === '200') {
      ElMessage.success('文件已上传，正在后台处理')
      await loadDocuments()
      startPolling()
    } else ElMessage.error(res.msg || '上传失败')
  } catch (error) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

const retry = async (row) => {
  try {
    const res = await knowledgeApi.retry(row.documentId)
    if (res.code === '200') {
      ElMessage.success('已重新开始处理')
      await loadDocuments()
      startPolling()
    } else ElMessage.error(res.msg || '重试失败')
  } catch (error) {
    ElMessage.error('重试失败')
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除“${row.originalFileName}”吗？删除后 AI 将不再使用该文件。`, '删除知识库文件', {
      type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消'
    })
    const res = await knowledgeApi.remove(row.documentId)
    if (res.code === '200') {
      ElMessage.success('文件已删除')
      await loadDocuments()
    } else ElMessage.error(res.msg || '删除失败')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error('删除失败')
  }
}

const statusLabel = (status) => ({ PROCESSING: '处理中', READY: '已完成', FAILED: '处理失败' }[status] || status || '未知')
const statusType = (status) => ({ PROCESSING: 'warning', READY: 'success', FAILED: 'danger' }[status] || 'info')

const startPolling = () => {
  if (pollTimer) return
  pollTimer = window.setInterval(async () => {
    await loadDocuments()
    if (!documents.value.some(item => item.status === 'PROCESSING')) {
      window.clearInterval(pollTimer)
      pollTimer = null
    }
  }, 4000)
}

onMounted(async () => {
  await loadDocuments()
  if (documents.value.some(item => item.status === 'PROCESSING')) startPolling()
})

onUnmounted(() => {
  if (pollTimer) window.clearInterval(pollTimer)
})
</script>

<style scoped>
.knowledge-header{display:flex;align-items:center;justify-content:space-between;gap:20px}
.knowledge-header h2{margin:0 0 8px;color:#26364a}
.knowledge-header p{margin:0;color:#7c8b9b}
.header-actions{display:flex;gap:10px;flex-shrink:0}
.knowledge-tip{margin:18px 0}
.knowledge-card{margin-top:18px}
.error-tip{margin-top:5px;color:#f56c6c;font-size:12px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
@media(max-width:768px){.knowledge-header{align-items:flex-start;flex-direction:column}.header-actions{width:100%}.header-actions .el-button{flex:1}}
</style>
