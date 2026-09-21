<template>
  <div class="page-container">
    <div class="page-header">
      <h2>成果收集</h2>
    </div>

    <el-card shadow="hover">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" label-position="right">
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="成果类别" prop="category">
              <el-select v-model="form.category" placeholder="请选择成果类别" style="width: 100%" @change="onCategoryChange">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成果分类等级" prop="classifyLevel">
              <el-select
                v-model="form.classifyLevel"
                :placeholder="form.category ? '请选择成果分类等级' : '请先选择成果类别'"
                :disabled="!form.category"
                style="width: 100%"
              >
                <el-option v-for="l in classifyLevels" :key="l" :label="l" :value="l" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="上传图片">
              <el-upload
                ref="uploadRef"
                class="upload-box"
                drag
                accept=".jpg,.jpeg,.png,.pdf"
                :auto-upload="true"
                :show-file-list="false"
                :http-request="doUpload"
              >
                <el-icon :size="44" color="#c0c4cc"><UploadFilled /></el-icon>
                <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
              </el-upload>
              <div class="upload-tip">支持 JPG/PNG/PDF 格式，文件大小不超过 10MB</div>
              <div v-if="form.fileUrl" class="file-card">
                <el-image
                  v-if="isImage"
                  :src="form.fileUrl"
                  :preview-src-list="[form.fileUrl]"
                  preview-teleported
                  fit="contain"
                  class="file-thumb"
                />
                <div v-else class="file-doc">
                  <el-icon :size="30" color="#409eff"><Document /></el-icon>
                </div>
                <div class="file-meta">
                  <span class="file-name" :title="form.fileName">{{ form.fileName }}</span>
                  <el-button link type="danger" @click="removeFile">
                    <el-icon><Close /></el-icon>
                  </el-button>
                </div>
                <div class="ocr-row">
                  <el-button type="primary" plain :loading="ocrLoading" @click="startOcr">
                    {{ ocrLoading ? 'OCR识别中...' : '开始OCR识别' }}
                  </el-button>
                  <span v-if="!ocrConfigured" class="ocr-tip">（OCR 未配置，请联系管理员配置 DeepSeek）</span>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="成果名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入成果名称" maxlength="200" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="persons">
              <el-input v-model="form.persons" placeholder="请输入姓名，多人用、分隔" maxlength="500" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="成果级别" prop="level">
              <el-select v-model="form.level" placeholder="请选择成果级别" style="width: 100%">
                <el-option v-for="l in levels" :key="l" :label="l" :value="l" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成果等级" prop="grade">
              <el-select v-model="form.grade" placeholder="请选择成果等级" style="width: 100%" allow-create filterable>
                <el-option v-for="g in grades" :key="g" :label="g" :value="g" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="获得时间" prop="achieveDate">
              <el-date-picker
                v-model="form.achieveDate"
                type="date"
                placeholder="请选择获得时间"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发证单位" prop="issuer">
              <el-input v-model="form.issuer" placeholder="请输入发证单位" maxlength="200" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="24">
            <el-form-item label="比赛名称">
              <el-input v-model="form.contestName" placeholder="请输入比赛名称（可选）" maxlength="200" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">提交成果信息</el-button>
          <el-button @click="resetAll">重置表单</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Document, Close } from '@element-plus/icons-vue'
import { achievementApi } from '../../api/index'

const formRef = ref()
const uploadRef = ref()
const categories = ref([])
const categoryLevels = ref({})
const levels = ref([])
const grades = ref([])
const ocrLoading = ref(false)
const submitting = ref(false)
const ocrConfigured = ref(true)

const emptyForm = () => ({
  category: '', classifyLevel: '', name: '', persons: '',
  level: '', grade: '', achieveDate: '', issuer: '', contestName: '',
  fileName: '', fileUrl: '', ocrFilled: 0
})
const form = reactive(emptyForm())

const rules = {
  category: [{ required: true, message: '请选择成果类别', trigger: 'change' }],
  classifyLevel: [{ required: true, message: '请选择成果分类等级', trigger: 'change' }],
  name: [{ required: true, message: '请输入成果名称', trigger: 'blur' }],
  persons: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  level: [{ required: true, message: '请选择成果级别', trigger: 'change' }],
  grade: [{ required: true, message: '请选择成果等级', trigger: 'change' }],
  achieveDate: [{ required: true, message: '请选择获得时间', trigger: 'change' }],
  issuer: [{ required: true, message: '请输入发证单位', trigger: 'blur' }]
}

const classifyLevels = computed(() => categoryLevels.value[form.category] || [])
const isImage = computed(() => /\.(png|jpe?g)$/i.test(form.fileUrl || ''))

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

const loadOcrStatus = async () => {
  try {
    const res = await achievementApi.getOcrStatus()
    ocrConfigured.value = !!(res.code === '200' && res.data && res.data.ready)
  } catch (error) {
    ocrConfigured.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadOptions(), loadOcrStatus()])
})

const onCategoryChange = () => {
  form.classifyLevel = ''
}

let uploading = false

const doUpload = async ({ file }) => {
  if (form.fileUrl || uploading) {
    ElMessage.warning('已上传文件，请先移除后再上传')
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 10MB')
    return
  }
  uploading = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await achievementApi.upload(fd)
    if (res.code === '200' && res.data) {
      form.fileName = res.data.fileName
      form.fileUrl = res.data.fileUrl
      ElMessage.success('文件上传成功')
    } else {
      ElMessage.error(res.msg || '文件上传失败')
    }
  } catch (error) {
    console.error('成果附件上传失败:', error)
  } finally {
    uploading = false
  }
}

const removeFile = () => {
  uploadRef.value?.clearFiles()
  form.fileName = ''
  form.fileUrl = ''
}

const startOcr = async () => {
  if (!form.fileUrl) return
  ocrLoading.value = true
  try {
    const res = await achievementApi.ocrRecognize(form.fileUrl)
    const data = res.data
    if (res.code !== '200') {
      ElMessage.error(res.msg || 'OCR 识别失败')
      return
    }
    if (data && data._raw) {
      ElMessage.warning('OCR 返回内容无法解析为字段，请手动核对填写')
      return
    }
    const map = {
      name: 'name', persons: 'persons', level: 'level', grade: 'grade',
      achieve_date: 'achieveDate', issuer: 'issuer', contest_name: 'contestName'
    }
    let filled = 0
    for (const [k, v] of Object.entries(map)) {
      if (data[k]) {
        form[v] = data[k]
        filled++
      }
    }
    if (filled > 0) {
      form.ocrFilled = 1
      // 自动填充后清除已触发的校验错误提示（此前点击提交触发的红字应随之消失）
      formRef.value?.clearValidate()
    }
    ElMessage.success('OCR识别完成，已自动填充表单，请核对信息')
  } catch (error) {
    console.error('OCR 识别失败:', error)
  } finally {
    ocrLoading.value = false
  }
}

const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const res = await achievementApi.create({ ...form })
    if (res.code === '200') {
      ElMessage.success('成果信息提交成功')
      resetAll()
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch (error) {
    console.error('提交成果信息失败:', error)
  } finally {
    submitting.value = false
  }
}

const resetAll = () => {
  formRef.value?.resetFields()
  uploadRef.value?.clearFiles()
  Object.assign(form, emptyForm())
}
</script>

<style scoped>
.upload-box { width: 340px; }
.upload-tip { font-size: 12px; color: #909399; margin-top: 4px; }
.file-card {
  width: 340px;
  margin-top: 10px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fafbfc;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.file-thumb { width: 100%; height: 200px; border-radius: 4px; flex: none; object-fit: contain; background: #fff; }
.file-doc {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 4px;
}
.file-meta { display: flex; align-items: center; gap: 10px; }
.file-name {
  flex: 1;
  font-size: 13px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.ocr-row { margin-top: 4px; display: flex; align-items: center; gap: 10px; }
.ocr-tip { font-size: 12px; color: #e6a23c; }
</style>
