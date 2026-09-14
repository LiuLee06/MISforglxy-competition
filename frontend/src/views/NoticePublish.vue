<template>
  <div class="page-container">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑公告' : '发布公告' }}</h2>
    </div>

    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>公告信息</span>
          <el-button type="primary" @click="draftDialogVisible = true">草稿箱</el-button>
        </div>
      </template>

      <el-form :model="form" label-width="100px" style="max-width: 900px; margin: 0 auto;">
        <el-row :gutter="20">
          <el-col :span="10">
            <el-form-item label="通知标题" required>
              <el-input v-model="form.title" placeholder="请输入通知标题" />
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="通知类型" required>
              <el-select v-model="form.noticeType" placeholder="请选择通知类型">
                <el-option v-for="item in noticeTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="发布部门" required>
              <el-select v-model="form.publishDept" placeholder="自动识别" disabled>
                <el-option v-for="item in deptOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="通知正文" required>
          <el-input v-model="form.content" type="textarea" :rows="12" placeholder="请输入通知正文内容" />
        </el-form-item>

        <el-form-item label="附件上传">
          <el-upload
            action="/mis-api/file/upload"
            :headers="uploadHeaders"
            :on-success="handleFileUploadSuccess"
            :on-error="handleFileUploadError"
            :on-remove="handleFileRemove"
            :show-file-list="false"
            multiple
            :before-upload="beforeUpload"
          >
            <el-button type="primary" size="small">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持上传Word、PDF、Excel、PPT、图片、压缩包、视频等格式，单个文件不超过500MB</div>
            </template>
          </el-upload>
          <div v-if="fileList.length > 0" style="margin-top: 10px;">
            <div v-for="(file, index) in fileList" :key="index" class="file-item">
              <span>{{ file.name }}</span>
              <span style="color: #999; margin-left: 10px; font-size: 12px;">{{ formatFileSize(file.size) }}</span>
              <el-button type="text" size="small" @click="handleViewAttachment(file)">查看</el-button>
              <el-button type="text" size="small" @click="handleFileRemove(file, index)">删除</el-button>
            </div>
          </div>
        </el-form-item>

        <!-- 接收对象 - 树形选择弹窗 -->
        <el-form-item label="接收对象" required>
          <el-input
            v-model="selectedReceiversText"
            placeholder="请选择接收对象"
            readonly
            @click="receiverDialogVisible = true"
            style="cursor: pointer;"
          >
            <template #append>
              <el-button @click="receiverDialogVisible = true">选择</el-button>
            </template>
          </el-input>
        </el-form-item>

        

        <el-form-item style="text-align: center; margin-top: 30px;">
          <el-button type="primary" @click="saveDraft">保存草稿</el-button>
          <el-button type="primary" @click="previewNotice">预览公告</el-button>
          <el-button type="success" @click="submitForm">{{ isEdit ? '保存修改' : '发布公告' }}</el-button>
          <el-button @click="resetForm">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>


    <el-dialog title="选择接收对象" v-model="receiverDialogVisible" width="500px">
      <el-tree
        ref="treeRef"
        :data="teacherTreeData"
        show-checkbox
        node-key="id"
        :default-checked-keys="selectedReceiverIds"
        :props="{ label: 'label', children: 'children' }"
      />
      <template #footer>
        <el-button @click="receiverDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReceivers">确定</el-button>
      </template>
    </el-dialog>

    
    <el-dialog title="草稿箱" v-model="draftDialogVisible" width="650px">
      <el-empty v-if="draftList.length === 0" description="暂无保存的草稿" />
      <div v-else class="draft-list">
        <div 
          v-for="(draft, index) in draftList" 
          :key="draft.draftId" 
          class="draft-item"
        >
          <div class="draft-info">
            <div class="draft-title">{{ draft.title || '无标题草稿' }}</div>
            <div class="draft-meta">
              <span>保存时间：{{ draft.saveTime }}</span>
              <span class="draft-type">{{ draft.noticeType || '未设置类型' }}</span>
            </div>
            <div class="draft-content">{{ draft.content?.slice(0, 60) || '暂无正文' }}...</div>
          </div>
          <div class="draft-actions">
            <el-button type="primary" size="small" @click="editDraft(index)">编辑并发布</el-button>
            <el-button type="danger" size="small" @click="deleteDraft(index)">删除</el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 通知预览弹窗 -->
    <el-dialog v-model="previewDialogVisible" width="700px" :show-close="false">
      <div class="notice-preview">
        <div class="notice-header">
          <h2 class="notice-title">{{ form.title || '通知标题' }}</h2>
          <div class="notice-meta">
            <span class="meta-item">
              <span class="meta-label">发布部门：</span>
              <span class="meta-value">{{ form.publishDept || '未设置' }}</span>
            </span>
            <span class="meta-item">
              <span class="meta-label">通知类型：</span>
              <span class="meta-value">{{ form.noticeType || '未设置' }}</span>
            </span>
            <span class="meta-item">
              <span class="meta-label">接收对象：</span>
              <span class="meta-value">{{ selectedReceiversText || '未选择' }}</span>
            </span>
            <span class="meta-item">
              <span class="meta-label">发布时间：</span>
              <span class="meta-value">{{ new Date().toLocaleString() }}</span>
            </span>
          </div>
        </div>
        <div class="notice-divider"></div>
        <div class="notice-content">
          <p style="white-space: pre-wrap; line-height: 1.8; margin: 0;">
            {{ form.content || '通知正文内容' }}
          </p>
        </div>
        <div v-if="fileList.length > 0" class="notice-attachments">
          <div class="attachments-title">附件列表</div>
          <div v-for="(file, index) in fileList" :key="index" class="attachment-item">
            <span class="attachment-icon">📎</span>
            <span class="attachment-name">{{ file.name }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="previewDialogVisible = false">关闭预览</el-button>
      </template>
    </el-dialog>

    <!-- 附件预览组件 -->
    <AttachmentPreview v-model="attachmentPreviewVisible" :file="attachmentPreviewFile" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { noticeApi, deptApi, teacherApi, noticeReceiveApi } from '../api/index.js'
import AttachmentPreview from '../components/AttachmentPreview.vue'

// 操作权限码
const actionCodes = JSON.parse(localStorage.getItem('userInfo') || '{}').actionCodes || []
const hasPermission = (code) => actionCodes.includes(code)

const route = useRoute()
const router = useRouter()

const isEdit = ref(false)
const currentNoticeId = ref(null)
const treeRef = ref(null)

const form = ref({
  title: '',
  noticeType: '',
  publishDept: '',
  content: ''
})

const fileList = ref([])

// el-upload 直传不走 axios 拦截器,需手动附加 JWT(否则被 AuthInterceptor 拦截返回 401)
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`
}))

// 接收对象相关
const receiverDialogVisible = ref(false)
const selectedReceiverIds = ref([]) // 选中的教师ID列表
const teacherTreeData = ref([]) // 树形结构数据：部门 -> 教师
const allTeachers = ref([]) // 所有教师数据

const draftDialogVisible = ref(false)
const draftList = ref([])

const previewDialogVisible = ref(false)

// 附件预览状态
const attachmentPreviewVisible = ref(false)
const attachmentPreviewFile = ref(null)

const noticeTypeOptions = [
  { label: '教学通知', value: '教学通知' },
  { label: '行政通知', value: '行政通知' },
  { label: '学生工作通知', value: '学生工作通知' },
  { label: '会议通知', value: '会议通知' },
  { label: '考试通知', value: '考试通知' },
  { label: '科研通知', value: '科研通知' },
  { label: '工作量通知', value: '工作量通知' }
]

const deptOptions = [
  { label: '学院领导', value: '学院领导' },
  { label: '院办', value: '院办' },
  { label: '教学办', value: '教学办' },
  { label: '学工办', value: '学工办' },
  { label: '科研办', value: '科研' },
  { label: '各系部', value: '系' },
  { label: '系统', value: '系统' }
]

const getUserDraftsKey = () => {
  const userId = parseInt(localStorage.getItem('userId')) || 0
  return `noticeDrafts_${userId}`
}

// 计算已选接收对象的显示文本
const selectedReceiversText = computed(() => {
  if (selectedReceiverIds.value.length === 0) return ''
  const selectedNames = allTeachers.value
    .filter(t => selectedReceiverIds.value.includes(t.teacherId))
    .map(t => t.name)
  if (selectedNames.length <= 3) {
    return selectedNames.join('、')
  } else {
    return selectedNames.slice(0, 3).join('、') + ` 等${selectedNames.length}人`
  }
})

onMounted(async () => {
  await loadTeachers()
  await loadCurrentUserDept()
  
  const draftsKey = getUserDraftsKey()
  const localDrafts = localStorage.getItem(draftsKey)
  if (localDrafts) {
    draftList.value = JSON.parse(localDrafts)
  }
  
  const oldKeyDrafts = localStorage.getItem('noticeDrafts')
  if (oldKeyDrafts) {
    try {
      const allDrafts = JSON.parse(oldKeyDrafts)
      const userId = parseInt(localStorage.getItem('userId')) || 0
      const myDrafts = allDrafts.filter(d => d.userId === userId || !d.userId)
      if (myDrafts.length > 0) {
        const existingKeyDrafts = draftList.value
        const merged = [...myDrafts, ...existingKeyDrafts]
        draftList.value = merged
        localStorage.setItem(draftsKey, JSON.stringify(merged))
      }
      localStorage.removeItem('noticeDrafts')
    } catch (e) {
      console.error('迁移旧草稿数据失败:', e)
    }
  }

  if (route.query.noticeId) {
    isEdit.value = true
    currentNoticeId.value = parseInt(route.query.noticeId)
    loadNoticeDetail(currentNoticeId.value)
  }
})

// 加载当前登录用户的部门信息，自动填充发布部门
const loadCurrentUserDept = async () => {
  try {
    // 获取用户类型，判断是否为管理员
    const userInfoStr = localStorage.getItem('userInfo')
    let userType = ''
    if (userInfoStr) {
      try {
        userType = JSON.parse(userInfoStr).userType || ''
      } catch (e) {
        console.error('解析用户信息失败:', e)
      }
    }
    

    if (userType === 'admin') {
      form.value.publishDept = '系统'
      return
    }
    
  
    const userId = parseInt(localStorage.getItem('userId'))
  
    if (!userId || isNaN(userId)) {
      // userId 无效时直接返回，不默认使用 1 避免数据串扰
      form.value.publishDept = '院办'
      return
    }
    const res = await teacherApi.getById(userId)
    if (res.code === '200' && res.data && res.data.dept) {
      let dept = res.data.dept
      if (dept.includes('院办')) {
        form.value.publishDept = '院办'
      } else if (dept.includes('教学办')) {
        form.value.publishDept = '教学办'
      } else if (dept.includes('学工办')) {
        form.value.publishDept = '学工办'
      } else if (dept.includes('系') || dept.includes('部')) {
        form.value.publishDept = '各系部'
      } else {
        form.value.publishDept = dept
      }
    }
  } catch (err) {
    console.error('获取用户部门信息失败:', err)
  }
}

// 加载教师并构建树形结构
const loadTeachers = async () => {
  try {
    const res = await teacherApi.getAll()
    if (res.code === '200' && res.data) {
      allTeachers.value = res.data
      
      // 按部门分组构建树形结构
      const deptMap = {}
      res.data.forEach(teacher => {
        const dept = teacher.dept || '未分配'
        if (!deptMap[dept]) {
          deptMap[dept] = []
        }
        deptMap[dept].push({
          id: teacher.teacherId,
          label: teacher.name,
          teacherId: teacher.teacherId
        })
      })
      
      // 构建树形数据
      teacherTreeData.value = Object.keys(deptMap).map((dept, index) => ({
        id: 'dept_' + index,
        label: dept,
        children: deptMap[dept]
      }))
    }
  } catch (err) {
    console.error('加载教师数据失败:', err)
    // 加载失败时使用默认数据
    teacherTreeData.value = [
      {
        id: 'dept_1',
        label: '计算机系',
        children: [
          { id: 1, label: '张老师' },
          { id: 2, label: '李老师' }
        ]
      },
      {
        id: 'dept_2',
        label: '数学系',
        children: [
          { id: 3, label: '王老师' }
        ]
      }
    ]
  }
}

const loadNoticeDetail = async (id) => {
  try {
    const res = await noticeApi.getById(id)
    if (res.code === '200' && res.data) {
      form.value = { ...res.data }

      fileList.value = []
      if (res.data.attachments) {
        try {
          const atts = typeof res.data.attachments === 'string'
            ? JSON.parse(res.data.attachments)
            : res.data.attachments
          if (Array.isArray(atts)) {
            fileList.value = atts.map(f => ({
              name: f.name || '',
              url: f.url || '',
              size: f.size || 0,
              type: f.type || ''
            }))
          }
        } catch (e) {
          console.warn('解析附件失败:', e)
        }
      }

      try {
        const readRes = await noticeReceiveApi.getReadList(id)
        if (readRes.code === '200' && Array.isArray(readRes.data)) {
          selectedReceiverIds.value = readRes.data.map(t => t.teacherId)
        }
      } catch (e) {
        console.warn('加载接收人列表失败:', e)
      }
    }
  } catch (err) {
    console.error('加载通知详情失败:', err)
    ElMessage.error('加载通知详情失败')
  }
}

// 确认选择接收对象
const confirmReceivers = () => {
  if (treeRef.value) {
    // 获取所有选中的叶子节点（教师）ID
    const checkedNodes = treeRef.value.getCheckedNodes(false, true)
    selectedReceiverIds.value = checkedNodes
      .filter(node => node.teacherId !== undefined)
      .map(node => node.teacherId)
  }
  receiverDialogVisible.value = false
}

// 允许的文件扩展名
const allowedExtensions = [
  // 文档
  '.doc', '.docx', '.pdf', '.txt',
  // 表格
  '.xls', '.xlsx',
  // 演示文稿
  '.ppt', '.pptx',
  // 图片
  '.jpg', '.jpeg', '.png', '.gif', '.bmp',
  // 压缩包
  '.zip', '.rar', '.7z',
  // 视频
  '.mp4', '.avi', '.mov', '.wmv', '.flv', '.mkv', '.webm', '.rmvb', '.3gp'
]

const beforeUpload = (file) => {
  // 文件大小限制：500MB
  const isLt500M = file.size / 1024 / 1024 < 500
  if (!isLt500M) {
    ElMessage.error('上传文件大小不能超过 500MB')
    return false
  }

  // 文件类型限制
  const fileName = file.name.toLowerCase()
  const extension = fileName.substring(fileName.lastIndexOf('.'))
  if (!allowedExtensions.includes(extension)) {
    ElMessage.error(`不支持的文件类型：${extension}，支持的格式：${allowedExtensions.join('、')}`)
    return false
  }

  return true
}

const handleFileUploadSuccess = (response, file) => {
  if (response.code === '200' && response.data) {
    fileList.value.push({
      name: response.data.filename,
      url: response.data.url,
      size: response.data.size,
      type: response.data.type,
      uid: file.uid
    })
    ElMessage.success(`文件 ${response.data.filename} 上传成功`)
  } else {
    ElMessage.error('文件上传失败')
  }
}

const handleFileUploadError = (error) => {
  console.error('文件上传失败:', error)
  ElMessage.error('文件上传失败')
}

const handleFileRemove = (file, index) => {
  // el-upload 的 on-remove 回调签名是 (file, uploadFiles)，第二参数是数组而非 index，
  // 因此优先用 uid 匹配；uid 不存在时（如回填的已上传附件）用 url 匹配
  if (file.uid) {
    fileList.value = fileList.value.filter(f => f.uid !== file.uid)
  } else if (file.url) {
    fileList.value = fileList.value.filter(f => f.url !== file.url)
  } else if (typeof index === 'number') {
    fileList.value.splice(index, 1)
  }
}

const handleViewAttachment = (file) => {
  if (!file || !file.url) {
    ElMessage.info('附件文件暂未上传，无法预览')
    return
  }
  attachmentPreviewFile.value = { ...file }
  attachmentPreviewVisible.value = true
}

const formatFileSize = (bytes) => {
  if (!bytes) return '未知'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / 1024 / 1024).toFixed(2) + ' MB'
}

const saveDraft = () => {
  if (!form.value.title && !form.value.content) {
    ElMessage.warning('请至少填写标题或正文后再保存草稿')
    return
  }

  const userId = parseInt(localStorage.getItem('userId')) || 0

  const draft = {
    draftId: form.value.draftId || Date.now(),
    userId: userId,
    ...form.value,
    receiverIds: selectedReceiverIds.value,
    attachments: fileList.value.map(file => ({
      name: file.name,
      url: file.url || '',
      size: file.size || 0,
      type: file.type || '',
      uid: file.uid || undefined
    })),
    saveTime: new Date().toLocaleString()
  }

  // 按 draftId 原地更新，避免重复生成新草稿；新草稿则插入顶部
  const editIdx = draftList.value.findIndex(d => d.draftId === draft.draftId)
  if (editIdx !== -1) {
    draftList.value.splice(editIdx, 1, draft)
  } else {
    draftList.value.unshift(draft)
  }
  // 记录当前表单对应的草稿ID：再次保存时更新同一条，发布成功后可精确移除
  form.value.draftId = draft.draftId
  localStorage.setItem(getUserDraftsKey(), JSON.stringify(draftList.value))

  ElMessage.success(editIdx !== -1 ? '草稿已更新' : '草稿保存成功，可在草稿箱中查看')
}

const editDraft = (index) => {
  const draft = draftList.value[index]
  form.value = { ...draft }
  selectedReceiverIds.value = draft.receiverIds || []

  // 还原附件列表
  fileList.value = []
  if (draft.attachments && Array.isArray(draft.attachments)) {
    fileList.value = draft.attachments.map(f => ({
      name: f.name || '',
      url: f.url || '',
      size: typeof f.size === 'number' ? f.size : 0,
      type: f.type || '',
      uid: f.uid || undefined
    }))
  }

  draftDialogVisible.value = false
  ElMessage.success('草稿已加载，可编辑后发布')
}

const deleteDraft = (index) => {
  ElMessageBox.confirm('确定要删除这条草稿吗？', '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    draftList.value.splice(index, 1)
    localStorage.setItem(getUserDraftsKey(), JSON.stringify(draftList.value))
    ElMessage.success('草稿已删除')
  }).catch(() => {})
}

const previewNotice = () => {
  if (!form.value.title || !form.value.content) {
    ElMessage.warning('请填写通知标题和正文')
    return
  }
  previewDialogVisible.value = true
}

const submitForm = async () => {
  if (!form.value.title || !form.value.content || !form.value.noticeType || !form.value.publishDept) {
    ElMessage.warning('请填写所有必填信息')
    return
  }
  
  if (selectedReceiverIds.value.length === 0) {
    ElMessage.warning('请选择接收对象')
    return
  }

  ElMessageBox.confirm(
    isEdit.value ? '确定要保存修改吗？' : '确定要发布这条通知吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      let attachments = '[]'
        if (fileList.value.length > 0) {
          const filesInfo = fileList.value.map(file => ({
            name: file.name,
            url: file.url,
            size: file.size,
            type: file.type
          }))
          attachments = JSON.stringify(filesInfo)
        }

      const publisherId = parseInt(localStorage.getItem('userId'))
      const userType = localStorage.getItem('userType') || 'teacher'

      if (!publisherId || isNaN(publisherId)) {
        ElMessage.error('登录信息已失效，请重新登录')
        router.push('/login')
        return
      }

      let publisherName = localStorage.getItem('userName') || ''
      if (!publisherName) {
        const userInfoStr = localStorage.getItem('userInfo')
        if (userInfoStr) {
          try {
            const userInfo = JSON.parse(userInfoStr)
            publisherName = userInfo.name || userInfo.username || ''
          } catch (e) {}
        }
      }

      const publishData = {
        title: form.value.title,
        content: form.value.content,
        noticeType: form.value.noticeType,
        publishDept: form.value.publishDept,
        attachments: attachments,
        publisherId: publisherId,
        publisherType: userType,
        publisherName: publisherName,
        userType: userType,
        receiverIds: selectedReceiverIds.value
      }

      let res
      if (isEdit.value && currentNoticeId.value) {
        res = await noticeApi.editPublish({
          ...publishData,
          oldNoticeId: currentNoticeId.value
        })
      } else {
        res = await noticeApi.publish(publishData)
      }

      if (res.code === '200') {
        ElMessage.success(isEdit.value ? '修改保存成功！' : '公告发布成功！')

        // 发布成功后，若该公告源自草稿，按 draftId 从草稿箱精确移除
        if (form.value.draftId) {
          const draftIdx = draftList.value.findIndex(d => d.draftId === form.value.draftId)
          if (draftIdx !== -1) {
            draftList.value.splice(draftIdx, 1)
            localStorage.setItem(getUserDraftsKey(), JSON.stringify(draftList.value))
          }
        }

        router.push('/notice-maintain')
      } else {
        ElMessage.error('操作失败')
      }
    } catch (err) {
      console.error('提交失败:', err)
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

const resetForm = () => {
  if (isEdit.value) {
    loadNoticeDetail(currentNoticeId.value)
  } else {
    form.value = {
      title: '',
      noticeType: '',
      publishDept: '',
      content: '',
      sendType: 'immediate',
      sendDate: '',
      sendTime: ''
    }
    selectedReceiverIds.value = []
  }
  fileList.value = []
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}
.draft-list {
  max-height: 400px;
  overflow-y: auto;
}
.draft-item {
  display: flex;
  justify-content: space-between;
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 10px;
}
.draft-info {
  flex: 1;
}
.draft-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 8px;
}
.draft-meta {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
  display: flex;
  gap: 20px;
}
.draft-type {
  color: #409eff;
}
.draft-content {
  font-size: 14px;
  color: #606266;
}
.draft-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  justify-content: center;
}

/* 通知预览样式 */
.notice-preview {
  padding: 10px 0;
}
.notice-header {
  text-align: center;
  margin-bottom: 20px;
}
.notice-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 15px 0;
}
.notice-meta {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 20px;
  font-size: 13px;
  color: #909399;
}
.meta-item {
  display: inline-flex;
}
.meta-label {
  color: #909399;
}
.meta-value {
  color: #606266;
  font-weight: 500;
}
.notice-divider {
  height: 1px;
  background: linear-gradient(to right, transparent, #dcdfe6, transparent);
  margin: 20px 0;
}
.notice-content {
  background-color: #fafafa;
  padding: 25px 30px;
  border-radius: 6px;
  font-size: 15px;
  color: #303133;
  min-height: 150px;
}
.notice-attachments {
  margin-top: 20px;
  padding: 15px 20px;
  background-color: #f5f7fa;
  border-radius: 6px;
}
.attachments-title {
  font-weight: 600;
  color: #606266;
  margin-bottom: 10px;
  font-size: 14px;
}
.attachment-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
  color: #409eff;
}
.attachment-icon {
  margin-right: 8px;
}
</style>
