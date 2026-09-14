<template>
  <div class="page-container">
    <div class="page-header">
      <h2>我的发布</h2>
    </div>

    <el-card class="search-card" shadow="hover">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="通知编号">
          <el-input v-model="searchForm.noticeId" placeholder="请输入" style="width: 160px;" />
        </el-form-item>
        <el-form-item label="通知标题">
          <el-input v-model="searchForm.title" placeholder="请输入" style="width: 180px;" />
        </el-form-item>
        <el-form-item label="通知类型">
          <el-select v-model="searchForm.noticeType" placeholder="请选择" style="width: 140px;">
            <el-option label="全部" value="" />
            <el-option label="教学通知" value="教学通知" />
            <el-option label="行政通知" value="行政通知" />
            <el-option label="学生工作通知" value="学生工作通知" />
            <el-option label="会议通知" value="会议通知" />
            <el-option label="考试通知" value="考试通知" />
            <el-option label="科研通知" value="科研通知" />
            <el-option label="工作量通知" value="工作量通知" />
          </el-select>
        </el-form-item>
        <el-form-item label="发布时间">
          <el-date-picker
            v-model="searchForm.publishTime"
            type="date"
            placeholder="请选择日期"
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          
          <el-dropdown @command="handleSettingCommand">
            <el-button>设置</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="batch">
                  {{ batchMode ? '退出批量操作' : '开启批量操作' }}
                </el-dropdown-item>
                <el-dropdown-item command="exportAll">导出全部Excel</el-dropdown-item>
                <el-dropdown-item command="exportPage">导出当前页Excel</el-dropdown-item>
                <el-dropdown-item command="exportSelected" :disabled="selectedIds.length === 0">导出选中Excel</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="hover" style="margin-top: 20px;">
      <div v-if="batchMode" class="batch-toolbar">
        <span>已选择 {{ selectedIds.length }} 项</span>
        <el-button type="danger" size="small" @click="batchDelete" :disabled="selectedIds.length === 0">
          批量删除
        </el-button>
        <el-button size="small" @click="cancelBatch">取消选择</el-button>
      </div>

      <el-table
        :data="tableData"
        border
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="通知标题" min-width="260" />
        <el-table-column label="通知编号" width="140">
          <template #default="scope">
            {{ scope.row.displayId || scope.row.noticeId }}
          </template>
        </el-table-column>
        <el-table-column label="发布部门" width="150">
          <template #default="scope">
            {{ scope.row.publishDept }}{{ scope.row.publisherName || '' }}
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="150" />
        <el-table-column prop="noticeType" label="发布类型" width="120" />
        <el-table-column prop="content" label="内容详情" min-width="150">
          <template #default="scope">
            {{ scope.row.content?.slice(0, 8) + '...' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="viewNotice(scope.row)">查看</el-button>
            <el-button type="success" link size="small" @click="editNotice(scope.row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="deleteNotice(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="text-align: right; margin-top: 20px;">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="prev, pager, next, sizes, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>

    <el-dialog title="公告详情" v-model="viewDialogVisible" width="700px" @close="closeDialog">
      <div>
        <h3 style="font-size: 20px; margin-bottom: 15px;">{{ currentNotice.title }}</h3>
        <p style="color: #666; margin: 10px 0;">
          公告编号：{{ currentNotice.displayId || currentNotice.noticeId }}&nbsp;&nbsp;
          发布部门：{{ currentNotice.publishDept }}
        </p>
        <p style="color: #666; margin: 10px 0;">
          发布时间：{{ currentNotice.publishTime }}&nbsp;&nbsp;
          公告类型：{{ currentNotice.noticeType }}
        </p>
        <hr style="margin: 15px 0;">
        <div style="line-height: 1.8; font-size: 14px; margin-bottom: 20px;">
          {{ currentNotice.content }}
        </div>

        <!-- 已读/未读统计 -->
        <div style="background-color: #f5f7fa; padding: 15px; border-radius: 4px; margin-bottom: 20px;">
          <div style="font-weight: 600; margin-bottom: 10px;">阅读统计：</div>
          <div style="display: flex; gap: 20px;">
            <span>总人数：{{ readStats.total }}人</span>
            <span style="color: #67c23a;">已读：{{ readStats.readCount }}人</span>
            <span style="color: #f56c6c;">未读：{{ readStats.unreadCount }}人</span>
          </div>
          <div style="margin-top: 10px;">
            <el-button type="primary" link size="small" @click="loadReadList(currentNotice.noticeId)">查看详细列表</el-button>
            <el-button type="warning" link size="small" @click="handleRemind" :disabled="readStats.unreadCount === 0">
              <el-icon><Bell /></el-icon> 提醒未读教师
            </el-button>
          </div>
        </div>

        <!-- 已读/未读详细列表 -->
        <div v-if="showReadList" style="margin-bottom: 20px;">
          <div style="font-weight: 600; margin-bottom: 10px;">阅读详情：</div>
          <el-table :data="readList" border size="small" style="width: 100%;">
            <el-table-column prop="teacherName" label="教师姓名" width="150" />
            <el-table-column prop="teacherDept" label="所属部门" width="150" />
            <el-table-column prop="isReceived" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="String(scope.row.isReceived) === '1' ? 'success' : 'warning'">
                  {{ String(scope.row.isReceived) === '1' ? '已读' : '未读' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="confirmTime" label="确认时间" width="150">
              <template #default="scope">
                {{ scope.row.confirmTime || '-' }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div v-if="currentNotice.attachments && currentNotice.attachments.length > 0">
          <div style="font-weight: 600; margin-bottom: 10px;">附件列表：</div>
          <div 
            v-for="(file, index) in currentNotice.attachments" 
            :key="index" 
            class="attachment-item"
          >
            <div class="attachment-info">
              <el-icon><Document /></el-icon>
              <span style="margin-left: 8px;">{{ file.name }}</span>
              <span style="color: #999; margin-left: 10px; font-size: 12px;">{{ file.size }}</span>
            </div>
            <div class="attachment-actions">
              <el-button type="primary" link size="small" @click="handleViewAttachment(file)">查看</el-button>
              <el-button type="primary" link size="small" @click="handleDownloadAttachment(file)">下载</el-button>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="closeDialog">关闭</el-button>
      </template>
    </el-dialog>

    <AttachmentPreview v-model="previewVisible" :file="previewFile" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Bell } from '@element-plus/icons-vue'
import { noticeApi, noticeReceiveApi } from '../api/index.js'
import AttachmentPreview from '../components/AttachmentPreview.vue'

// 操作权限码
const actionCodes = JSON.parse(localStorage.getItem('userInfo') || '{}').actionCodes || []
const hasPermission = (code) => actionCodes.includes(code)

const router = useRouter()

const searchForm = ref({
  noticeId: '',
  title: '',
  noticeType: '',
  publishTime: ''
})

const tableData = ref([])
const allData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const viewDialogVisible = ref(false)
const currentNotice = ref({})
const readStats = ref({ total: 0, readCount: 0, unreadCount: 0 })
const readList = ref([])
const showReadList = ref(false)
let refreshTimer = null

// 附件预览状态（与 NoticeList 统一）
const previewVisible = ref(false)
const previewFile = ref(null)

const batchMode = ref(false)
const selectedIds = ref([])
const selectedRows = ref([])

onMounted(() => {
  handleSearch()
})

onUnmounted(() => {
  // 组件卸载时清除定时器，防止内存泄漏
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})

const handleSearch = async () => {
  try {
    const userId = parseInt(localStorage.getItem('userId'))
    const userType = localStorage.getItem('userType') || 'teacher'
    
    // userId 无效时不发起请求，避免查询到错误数据
    if (!userId || isNaN(userId)) {
      ElMessage.error('登录信息已失效，请重新登录')
      router.push('/login')
      return
    }
    
    const userRole = userType === 'admin' ? 'admin' : null
    const res = userType === 'admin'
      ? await noticeApi.getAll()
      : await noticeApi.getByPublisherId(userId, userType)
    if (res.code === '200' && res.data) {
      let result = res.data

      // 解析附件JSON字符串
      result = result.map(item => {
        if (item.attachments && typeof item.attachments === 'string') {
          try {
            item.attachments = JSON.parse(item.attachments)
          } catch (e) {
            item.attachments = []
          }
        }
        return item
      })

      // 先按发布时间正序排序（最早的在前），用于分配编号
      const sortedForId = [...result].sort((a, b) => new Date(a.publishTime) - new Date(b.publishTime))
      
      // 根据用户角色添加显示编号
      // 普通老师：从1开始编号（最早发布的为1）；超级管理员：显示noticeId
      const idMap = {}
      if (userRole !== 'admin') {
        sortedForId.forEach((item, index) => {
          idMap[item.noticeId] = index + 1
        })
      }
      
      result = result.map(item => ({
        ...item,
        displayId: idMap[item.noticeId]
      }))

      // 按发布时间倒序排序（最新的在前），用于表格显示
      result.sort((a, b) => new Date(b.publishTime) - new Date(a.publishTime))

      if (searchForm.value.noticeId && searchForm.value.noticeId.trim()) {
        const id = parseInt(searchForm.value.noticeId.trim())
        if (!isNaN(id)) {
          // 根据角色匹配不同的ID
          if (userRole === 'admin') {
            result = result.filter(item => String(item.noticeId) === String(id))
          } else {
            result = result.filter(item => String(item.displayId) === String(id))
          }
        }
      }
      if (searchForm.value.title) {
        result = result.filter(item => item.title?.includes(searchForm.value.title))
      }
      if (searchForm.value.noticeType) {
        result = result.filter(item => item.noticeType === searchForm.value.noticeType)
      }
      if (searchForm.value.publishTime) {
        const date = searchForm.value.publishTime
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const dateStr = `${year}-${month}-${day}`
        result = result.filter(item => item.publishTime?.startsWith(dateStr))
      }

      total.value = result.length
      allData.value = result
      tableData.value = result.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value)
    } else {
      tableData.value = []
      allData.value = []
      total.value = 0
    }
  } catch (err) {
    console.error('加载通知失败:', err)
    ElMessage.error('加载通知失败')
    tableData.value = []
    allData.value = []
    total.value = 0
  }
}

const handleReset = () => {
  searchForm.value = {
    noticeId: '',
    title: '',
    noticeType: '',
    publishTime: ''
  }
  currentPage.value = 1
  handleSearch()
}

const handleSettingCommand = (command) => {
  if (command === 'batch') {
    batchMode.value = !batchMode.value
    if (!batchMode.value) {
      selectedIds.value = []
      selectedRows.value = []
    }
  } else if (command === 'exportAll') {
    exportExcel('all')
  } else if (command === 'exportPage') {
    exportExcel('page')
  } else if (command === 'exportSelected') {
    exportExcel('selected')
  }
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
  selectedIds.value = selection.map(item => item.noticeId)
}

const cancelBatch = () => {
  selectedIds.value = []
  selectedRows.value = []
}

const batchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的通知')
    return
  }

  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedIds.value.length} 条通知吗？删除后不可恢复`,
    '批量删除确认',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const userId = parseInt(localStorage.getItem('userId'))
      const userType = localStorage.getItem('userType') || 'teacher'
      const count = selectedIds.value.length
      for (const id of selectedIds.value) {
        await noticeApi.delete(id, userId, userType)
      }
      handleSearch()
      selectedIds.value = []
      selectedRows.value = []
      ElMessage.success(`成功删除 ${count} 条通知`)
    } catch (err) {
      console.error('批量删除失败:', err)
      ElMessage.error('批量删除失败')
    }
  }).catch(() => {})
}

const exportExcel = (scope = 'page') => {
  let exportData = []
  let suffix = ''
  if (scope === 'all') {
    exportData = allData.value
    suffix = '全部'
  } else if (scope === 'selected') {
    exportData = selectedRows.value
    suffix = '选中'
  } else {
    exportData = tableData.value
    suffix = '当前页'
  }

  if (exportData.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }

  const headers = ['通知标题', '通知编号', '发布部门', '发布时间', '发布类型', '通知内容']
    const rows = exportData.map(item => [
      item.title,
      item.displayId || item.noticeId,
      item.publishDept,
      item.publishTime,
      item.noticeType,
      item.content?.replace(/,/g, '，') || ''
    ])

  const csvContent = '\uFEFF' + [headers, ...rows].map(row => row.join(',')).join('\n')
  
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `通知公告列表_${suffix}_${new Date().toLocaleDateString()}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)

  ElMessage.success(`导出成功（${suffix}，共${exportData.length}条）`)
}

const viewNotice = (row) => {
  currentNotice.value = { ...row }
  viewDialogVisible.value = true
  showReadList.value = true
  // 加载已读/未读统计和详细列表
  loadReadStats(row.noticeId)
  loadReadList(row.noticeId)
  
  // 开启定时刷新，每隔5秒刷新一次阅读状态
  if (refreshTimer) clearInterval(refreshTimer)
  refreshTimer = setInterval(() => {
    loadReadStats(row.noticeId)
    loadReadList(row.noticeId)
  }, 5000)
}

const editNotice = (row) => {
  // 跳转到编辑页面（修改逻辑改为删除旧的+新增新的）
  router.push(`/notice-publish?noticeId=${row.noticeId}`)
}

const loadReadStats = async (noticeId) => {
  try {
    const res = await noticeReceiveApi.getReadStats(noticeId)
    if (res.code === '200' && res.data) {
      readStats.value = res.data
      console.log('加载已读统计成功:', res.data)
    }
  } catch (err) {
    console.error('加载已读统计失败:', err)
  }
}

const loadReadList = async (noticeId) => {
  try {
    const res = await noticeReceiveApi.getReadList(noticeId)
    if (res.code === '200' && res.data) {
      readList.value = res.data
      showReadList.value = true
      console.log('加载已读列表成功:', res.data)
      console.log('isReceived类型检查:', res.data.map(item => ({name: item.teacherName, isReceived: item.isReceived, type: typeof item.isReceived})))
    }
  } catch (err) {
    console.error('加载已读列表失败:', err)
  }
}

const closeDialog = () => {
  viewDialogVisible.value = false
  // 清除定时刷新
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

const handleRemind = async () => {
  if (readStats.value.unreadCount === 0) {
    ElMessage.info('没有未读教师，无需提醒')
    return
  }
  try {
    const res = await noticeReceiveApi.remind(currentNotice.value.noticeId)
    if (res.code === '200') {
      ElMessage.success(`已提醒 ${readStats.value.unreadCount} 位未读教师`)
      loadReadStats(currentNotice.value.noticeId)
    } else {
      ElMessage.error('提醒失败')
    }
  } catch (err) {
    console.error('提醒失败:', err)
    ElMessage.error('提醒失败')
  }
}

const deleteNotice = (row) => {
  ElMessageBox.confirm(
    `确定要删除通知「${row.title}」吗？删除后不可恢复`,
    '删除确认',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const userId = parseInt(localStorage.getItem('userId'))
      const userType = localStorage.getItem('userType') || 'teacher'
      const res = await noticeApi.delete(row.noticeId, userId, userType)
      if (res.code === '200') {
        ElMessage.success('删除成功')
        handleSearch()
      } else {
        ElMessage.error('删除失败')
      }
    } catch (err) {
      console.error('删除失败:', err)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const resolveFileUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('/file/download/')) {
    return '/mis-api' + url
  } else if (url.startsWith('/upload/')) {
    return '/mis-api/file/download/' + url.split('/')[2]
  } else if (!url.startsWith('http')) {
    return '/mis-api' + url
  }
  return url
}

const handleViewAttachment = (file) => {
  const url = file.url ? resolveFileUrl(file.url) : ''
  previewFile.value = { ...file, url }
  previewVisible.value = true
}

const handleDownloadAttachment = (file) => {
  if (!file.url) {
    ElMessage.info('附件文件暂未上传，无法下载')
    return
  }
  
  let downloadUrl = file.url
  if (downloadUrl.startsWith('/file/download/')) {
    downloadUrl = '/mis-api' + downloadUrl
  } else if (downloadUrl.startsWith('/upload/')) {
    downloadUrl = '/mis-api/file/download/' + downloadUrl.split('/')[2]
  } else if (!downloadUrl.startsWith('http')) {
    downloadUrl = '/mis-api' + downloadUrl
  }
  
  window.open(downloadUrl, '_blank')
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.attachment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 8px;
}
.attachment-info {
  display: flex;
  align-items: center;
  color: #303133;
}
.attachment-actions {
  display: flex;
  gap: 10px;
}
.batch-toolbar {
  padding: 10px 15px;
  background-color: #ecf5ff;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 15px;
}
</style>