<template>
  <div class="page-container">
    <div class="page-header">
      <h2>通知公告</h2>
    </div>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="search-card" shadow="hover">
          <el-form :model="searchForm" inline class="search-form">
            <el-form-item label="查询时间">
              <el-date-picker
                v-model="searchForm.queryDate"
                type="date"
                placeholder="请选择日期"
                style="width: 180px;"
              />
            </el-form-item>
            <el-form-item label="关键词搜索">
              <el-input v-model="searchForm.keyword" placeholder="请输入关键词" style="width: 200px;" />
            </el-form-item>
            <el-form-item label="状态筛选">
              <el-select v-model="searchForm.status" placeholder="请选择" style="width: 120px;">
                <el-option label="全部" value="" />
                <el-option label="未读" value="unread" />
                <el-option label="已读" value="read" />
                <el-option label="待办" value="todo" />
              </el-select>
            </el-form-item>
            <el-form-item label="发布部门">
              <el-select v-model="searchForm.publishDept" placeholder="请选择" style="width: 140px;">
                <el-option label="全部" value="" />
                <el-option v-for="opt in deptOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="doSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="table-card" shadow="hover" style="margin-top: 20px;">
          <el-table :data="tableData" border stripe style="width: 100%">
            <el-table-column prop="title" label="公告标题" min-width="280">
              <template #default="scope">
                <span>
                  <el-tag v-if="scope.row.remindCount && scope.row.remindCount > 0" type="danger" size="small" style="margin-right: 5px;">提醒</el-tag>
                  {{ scope.row.title }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="noticeType" label="类型" width="120" />
            <el-table-column label="发布部门" width="150">
              <template #default="scope">
                {{ scope.row.publishDept }}{{ scope.row.publisherName || '' }}
              </template>
            </el-table-column>
            <el-table-column prop="publishTime" label="发布时间" width="150" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewNotice(scope.row)">查看</el-button>
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
      </el-col>
    </el-row>

    <!-- 浮动提醒通知 -->
    <div v-if="remindList.length > 0" class="floating-reminder" @click="showRemindDialog = true">
      <el-icon style="color: #f56c6c; font-size: 20px;"><Bell /></el-icon>
      <span>{{ teacherName }}，您有 {{ remindList.length }} 条提醒</span>
      <span class="remind-count">{{ remindList.length }}</span>
    </div>

    <!-- 提醒详情弹窗 -->
    <el-dialog
      title="提醒消息"
      v-model="showRemindDialog"
      width="500px"
    >
      <div v-if="remindList.length === 0" style="text-align: center; color: #999; padding: 40px 0;">
        暂无提醒消息
      </div>
      <div v-else>
        <div 
          v-for="(item, index) in remindList" 
          :key="item.noticeId" 
          class="remind-item"
          @click.stop="viewNotice(item)"
        >
          <div class="remind-item-header">
            <span class="remind-badge">提醒</span>
            <span>{{ item.title }}</span>
          </div>
          <div class="remind-item-body">
            <span style="color: #999;">发布部门：{{ item.publishDept }}{{ item.publisherName }}</span>
            <span style="color: #999;">发布时间：{{ item.publishTime }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showRemindDialog = false">关闭</el-button>
          <el-button type="success" @click="markAllRemindAsRead">全部确认已读</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      title="通知详情"
      v-model="dialogVisible"
      width="600px"
    >
      <div>
        <h3 style="font-size: 20px; margin-bottom: 15px;">{{ currentNotice.title }}</h3>
        <p style="color: #666; margin: 10px 0;">
          发布人：{{ currentNotice.publishDept }}{{ currentNotice.publisherName || '' }}&nbsp;&nbsp;
          发布时间：{{ currentNotice.publishTime }}&nbsp;&nbsp;
          通知类型：{{ currentNotice.noticeType }}
        </p>
        <hr style="margin: 15px 0;">
        <div style="line-height: 1.8; font-size: 14px; margin-bottom: 20px;">
          {{ currentNotice.content }}
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
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button v-if="!isAdmin && currentNotice.status === 'unread'" type="success" @click="markAsRead">
            标记已读
          </el-button>
          <el-button v-if="!isAdmin && currentNotice.status !== 'todo'" type="primary" @click="addToTodo">
            加入待办
          </el-button>
          <el-button v-if="!isAdmin && currentNotice.status === 'todo'" type="warning" @click="cancelTodo">
            取消待办
          </el-button>
        </span>
      </template>
    </el-dialog>

    <AttachmentPreview v-model="previewVisible" :file="previewFile" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, inject } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Bell } from '@element-plus/icons-vue'
import { noticeApi, noticeReceiveApi } from '../api/index.js'
import AttachmentPreview from '../components/AttachmentPreview.vue'

const router = useRouter()
const route = useRoute()
// 由 App.vue 注入：状态变更后立即刷新顶部铃铛角标，无需等待轮询
const refreshBellCount = inject('refreshBellCount', () => {})

// 附件预览状态
const previewVisible = ref(false)
const previewFile = ref(null)

// 操作权限码
const actionCodes = JSON.parse(localStorage.getItem('userInfo') || '{}').actionCodes || []
const hasPermission = (code) => actionCodes.includes(code)

// 获取当前教师ID，无效时返回 null 并跳转登录页
const getCurrentTeacherId = () => {
  const id = parseInt(localStorage.getItem('userId'))
  if (!id || isNaN(id)) {
    ElMessage.error('登录信息已失效，请重新登录')
    router.push('/login')
    return null
  }
  return id
}

const searchForm = ref({
  queryDate: '',
  keyword: '',
  status: '',
  publishDept: ''
})

const isAdmin = computed(() => localStorage.getItem('userType') === 'admin')

const deptOptions = [
  { label: '学院领导', value: '学院领导' },
  { label: '院办', value: '院办' },
  { label: '教学办', value: '教学办' },
  { label: '学工办', value: '学工办' },
  { label: '科研办', value: '科研' },
  { label: '各系部', value: '系' },
  { label: '系统', value: '系统' }
]

const tableData = ref([])
const allData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const currentNotice = ref({})
const showRemindDialog = ref(false)
const teacherName = ref('')

// 应用铃铛下拉跳转携带的状态筛选参数（?status=unread/todo）
const applyRouteFilter = () => {
  const status = route.query.status
  searchForm.value.status = ['unread', 'read', 'todo'].includes(status) ? status : ''
}

onMounted(() => {
  applyRouteFilter()
  handleSearch()
  loadTeacherInfo()
})

// 已在列表页时，再次从铃铛下拉点击不同筛选，query 变化需同步刷新
watch(() => route.query.status, () => {
  applyRouteFilter()
  currentPage.value = 1
  handleSearch()
})

const loadTeacherInfo = () => {
  teacherName.value = localStorage.getItem('userName') || '老师'
}

const handleReset = () => {
  searchForm.value = {
    queryDate: '',
    keyword: '',
    status: '',
    publishDept: ''
  }
  currentPage.value = 1
  handleSearch()
}

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
    // admin 看全部通知，teacher 只看自己的
    const res = userType === 'admin'
      ? await noticeApi.getAll()
      : await noticeApi.getByTeacherId(userId)
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

      // 前端筛选
      let filtered = [...result]
      if (searchForm.value.keyword) {
        const keyword = searchForm.value.keyword.trim().toLowerCase()
        filtered = filtered.filter(item => {
          const title = String(item.title || '').toLowerCase()
          return title.includes(keyword)
        })
      }
      if (searchForm.value.status) {
        filtered = filtered.filter(item => item.status === searchForm.value.status)
      }
      if (searchForm.value.publishDept) {
        const dept = searchForm.value.publishDept
        filtered = filtered.filter(item => item.publishDept && item.publishDept.includes(dept))
      }
      if (searchForm.value.queryDate) {
        const date = searchForm.value.queryDate
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const dateStr = `${year}-${month}-${day}`
        filtered = filtered.filter(item => item.publishTime?.startsWith(dateStr))
      }
      
      // 保存全部筛选后的数据用于统计
      allData.value = filtered
      
      total.value = filtered.length
      tableData.value = filtered.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value)
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

// 条件查询（区别于翻页）：先回到第一页再搜索，避免原页码超出新结果范围出现空页
const doSearch = () => {
  currentPage.value = 1
  handleSearch()
}

const viewNotice = async (row) => {
  currentNotice.value = { ...row }
  dialogVisible.value = true
  // 非管理员查看未读通知时，自动标记为已读（静默处理，不弹提示、不关闭弹窗）
  if (isAdmin.value) return
  if (row.status !== 'unread') return
  const teacherId = getCurrentTeacherId()
  if (teacherId === null) return
  try {
    const res = await noticeReceiveApi.markAsRead(row.noticeId, teacherId)
    if (res.code === '200') {
      // 同步更新弹窗当前通知和列表中对应项的状态
      currentNotice.value.status = 'read'
      const item = allData.value.find(i => i.noticeId === row.noticeId)
      if (item) item.status = 'read'
      // 角标立即减一，无需等待 10 秒轮询
      refreshBellCount()
    }
  } catch (err) {
    console.error('自动标记已读失败:', err)
  }
}

const markAsRead = async () => {
  try {
    const teacherId = getCurrentTeacherId()
    if (teacherId === null) return
    const res = await noticeReceiveApi.markAsRead(currentNotice.value.noticeId, teacherId)
    if (res.code === '200') {
      ElMessage.success('已标记为已读')
      // 更新本地数据状态
      const item = allData.value.find(i => i.noticeId === currentNotice.value.noticeId)
      if (item) item.status = 'read'
      handleSearch()
      refreshBellCount()
    } else {
      ElMessage.error('操作失败')
    }
  } catch (err) {
    console.error('标记已读失败:', err)
    ElMessage.error('操作失败')
  }
  dialogVisible.value = false
}

const addToTodo = async () => {
  try {
    const teacherId = getCurrentTeacherId()
    if (teacherId === null) return
    const res = await noticeReceiveApi.markAsTodo(currentNotice.value.noticeId, teacherId)
    if (res.code === '200') {
      ElMessage.success('已加入待办事项')
      // 更新本地数据状态
      const item = allData.value.find(i => i.noticeId === currentNotice.value.noticeId)
      if (item) item.status = 'todo'
      handleSearch()
      refreshBellCount()
    } else {
      ElMessage.error('操作失败')
    }
  } catch (err) {
    console.error('加入待办失败:', err)
    ElMessage.error('操作失败')
  }
  dialogVisible.value = false
}

const cancelTodo = async () => {
  try {
    const teacherId = getCurrentTeacherId()
    if (teacherId === null) return
    const res = await noticeReceiveApi.unmarkTodo(currentNotice.value.noticeId, teacherId)
    if (res.code === '200') {
      ElMessage.success('已取消待办')
      // 取消待办后 is_todo=0 且 is_received 仍为 0，真实状态回到"未读"
      const item = allData.value.find(i => i.noticeId === currentNotice.value.noticeId)
      if (item) item.status = 'unread'
      handleSearch()
      refreshBellCount()
    } else {
      ElMessage.error('操作失败')
    }
  } catch (err) {
    console.error('取消待办失败:', err)
    ElMessage.error('操作失败')
  }
  dialogVisible.value = false
}

const markAllRemindAsRead = async () => {
  try {
    const teacherId = getCurrentTeacherId()
    if (teacherId === null) return
    // 批量标记所有提醒的通知为已读
    for (const item of remindList.value) {
      await noticeReceiveApi.markAsRead(item.noticeId, teacherId)
    }
    ElMessage.success('已全部确认已读')
    showRemindDialog.value = false
    handleSearch()
    refreshBellCount()
  } catch (err) {
    console.error('批量标记已读失败:', err)
    ElMessage.error('操作失败')
  }
}

const getStatusType = (status) => {
  const map = {
    unread: 'warning',
    read: 'success',
    todo: 'info',
    published: 'primary'
  }
  return map[status] || 'warning'
}

const getStatusText = (status) => {
  const map = {
    unread: '未读',
    read: '已读',
    todo: '待办',
    published: '已发布'
  }
  return map[status] || '未读'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.slice(5, 10)
}

const remindList = computed(() => {
  return allData.value.filter(item => item.remindCount && item.remindCount > 0 && item.status !== 'read')
})

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
/* 浮动提醒通知 */
.floating-reminder {
  position: fixed;
  bottom: 30px;
  right: 30px;
  background-color: #fff;
  border: 2px solid #f56c6c;
  border-radius: 50px;
  padding: 12px 24px;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 20px rgba(245, 108, 108, 0.3);
  cursor: pointer;
  z-index: 1000;
  animation: bounce 2s infinite;
}
@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}
.floating-reminder:hover {
  background-color: #fff5f5;
}
.remind-count {
  background-color: #f56c6c;
  color: #fff;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
}
/* 提醒详情弹窗样式 */
.remind-item {
  padding: 15px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background-color 0.2s;
}
.remind-item:hover {
  background-color: #f5f7fa;
}
.remind-item-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 8px;
}
.remind-badge {
  background-color: #fef0f0;
  color: #f56c6c;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}
.remind-item-body {
  display: flex;
  gap: 20px;
  font-size: 13px;
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
</style>