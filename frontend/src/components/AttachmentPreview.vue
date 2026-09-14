<template>
  <el-dialog
    v-model="visible"
    :title="`预览：${file?.name || ''}`"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="preview-container">
      <div v-if="loading" class="preview-loading">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <div v-else-if="error" class="preview-error">
        <el-icon :size="48" color="#f56c6c"><Warning /></el-icon>
        <p>{{ error }}</p>
      </div>

      <!-- 图片预览 -->
      <div v-else-if="fileType === 'image'" class="preview-image">
        <el-image
          :src="previewUrl"
          :preview-src-list="[previewUrl]"
          fit="contain"
          style="max-width: 100%; max-height: 500px;"
          preview-teleported
        />
      </div>

      <!-- 视频预览 -->
      <div v-else-if="fileType === 'video'" class="preview-video">
        <video
          :src="previewUrl"
          controls
          :poster="previewUrl"
          style="max-width: 100%; max-height: 500px; border-radius: 4px;"
        >
          您的浏览器不支持视频播放
        </video>
      </div>

      <!-- PDF 预览 -->
      <div v-else-if="fileType === 'pdf'" class="preview-pdf">
        <iframe
          :src="previewUrl"
          frameborder="0"
          style="width: 100%; height: 550px; border: none; border-radius: 4px;"
        ></iframe>
      </div>

      <!-- TXT 预览 -->
      <div v-else-if="fileType === 'txt'" class="preview-txt">
        <pre class="txt-content">{{ txtContent }}</pre>
      </div>

      <!-- Word (docx) 预览 -->
      <div v-else-if="fileType === 'word'" class="preview-word">
        <div v-if="wordHtml" class="word-content" v-html="wordHtml"></div>
        <div v-else class="preview-loading">
          <el-icon class="is-loading" :size="32"><Loading /></el-icon>
          <p>正在解析 Word 文档...</p>
        </div>
      </div>

      <!-- Excel (xlsx) 预览 -->
      <div v-else-if="fileType === 'excel'" class="preview-excel">
        <div v-if="excelSheets.length > 0" class="excel-content">
          <el-tabs v-model="activeSheet" type="card" size="small">
            <el-tab-pane
              v-for="(sheet, idx) in excelSheets"
              :key="idx"
              :label="sheet.name"
              :name="String(idx)"
            >
              <el-table :data="sheet.data" border stripe size="small" style="width: 100%;">
                <el-table-column
                  v-for="(col, cIdx) in sheet.columns"
                  :key="cIdx"
                  :prop="col"
                  :label="col"
                  :min-width="120"
                />
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </div>
        <div v-else class="preview-loading">
          <el-icon class="is-loading" :size="32"><Loading /></el-icon>
          <p>正在解析 Excel 文件...</p>
        </div>
      </div>

      <!-- 不支持预览的文件 -->
      <div v-else class="preview-unsupported">
        <el-icon :size="64" color="#c0c4cc"><Document /></el-icon>
        <h4>{{ file?.name }}</h4>
        <p class="file-info">
          大小：{{ formatSize(file?.size) }} &nbsp;|&nbsp; 类型：{{ file?.type || getExtension(file?.name) }}
        </p>
        <p class="unsupported-msg">此文件类型暂不支持在线预览，请点击下载后查看</p>
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button type="primary" @click="handleDownload">下载文件</el-button>
    </template>
  </el-dialog>
</template>

<script setup>import { ref, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Loading, Warning, Document } from '@element-plus/icons-vue';
import mammoth from 'mammoth';
import * as XLSX from 'xlsx';
const props = defineProps({
 modelValue: { type: Boolean, default: false },
 file: { type: Object, default: null }
});
const emit = defineEmits(['update:modelValue']);
const visible = computed({
 get: () => props.modelValue,
 set: (val) => emit('update:modelValue', val)
});
const loading = ref(false);
const error = ref('');
const txtContent = ref('');
const wordHtml = ref('');
const excelSheets = ref([]);
const activeSheet = ref('0');
const fileType = ref('');
const resolveFileUrl = (url) => {
 if (!url)
 return '';
 // 统一使用相对路径：开发环境通过 Vite proxy 代理，生产环境通过 Nginx 代理
 // 不硬编码后端地址，确保跨平台（Windows/Linux）都能正常访问
 if (url.startsWith('/file/download/')) {
   return '/mis-api' + url;
 }
 else if (url.startsWith('/upload/')) {
   return '/mis-api/file/download/' + url.split('/').pop();
 }
 else if (!url.startsWith('http')) {
   return url;
 }
 return url;
};
const previewUrl = computed(() => {
 if (!props.file?.url)
 return '';
 // 预览统一走 /file/preview/ 接口(响应头 Content-Disposition: inline)。
 // /file/download/ 是 attachment 强制下载,iframe 加载 PDF 会被浏览器直接触发下载而非渲染
 const resolvedUrl = resolveFileUrl(props.file.url);
 return resolvedUrl
 .replace('/file/download/', '/file/preview/')
 .replace('/upload/', '/file/preview/');
});
const getExtension = (filename) => {
 if (!filename)
 return '';
 const idx = filename.lastIndexOf('.');
 return idx >= 0 ? filename.slice(idx + 1).toLowerCase() : '';
};
const formatSize = (size) => {
 if (!size)
 return '未知';
 if (size < 1024)
 return size + ' B';
 if (size < 1024 * 1024)
 return (size / 1024).toFixed(2) + ' KB';
 return (size / 1024 * 1024).toFixed(2) + ' MB';
};
const determineFileType = (file) => {
 if (!file)
 return '';
 const ext = getExtension(file.name);
 const type = (file.type || '').toLowerCase();
 if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'].includes(ext) || type.startsWith('image/')) {
 return 'image';
 }
 if (['mp4', 'webm', 'avi', 'mov', 'wmv', 'flv', 'mkv', '3gp'].includes(ext) || type.startsWith('video/')) {
 return 'video';
 }
 if (ext === 'pdf' || type === 'application/pdf') {
 return 'pdf';
 }
 if (['txt'].includes(ext) || type.startsWith('text/')) {
 return 'txt';
 }
 if (ext === 'docx') {
 return 'word';
 }
 if (ext === 'xlsx') {
 return 'excel';
 }
 return 'other';
};
const loadFile = async () => {
 if (!props.file)
 return;
 loading.value = true;
 error.value = '';
 txtContent.value = '';
 wordHtml.value = '';
 excelSheets.value = [];
 fileType.value = determineFileType(props.file);
 try {
 if (!previewUrl.value) {
 throw new Error('文件URL无效');
 }
 if (fileType.value === 'txt') {
 const res = await fetch(previewUrl.value);
 if (!res.ok)
 throw new Error('加载文件失败');
 txtContent.value = await res.text();
 }
 else if (fileType.value === 'word') {
 const res = await fetch(previewUrl.value);
 if (!res.ok)
 throw new Error('加载文件失败');
 const arrayBuffer = await res.arrayBuffer();
 const result = await mammoth.convertToHtml({ arrayBuffer });
 wordHtml.value = result.value;
 }
 else if (fileType.value === 'excel') {
 const res = await fetch(previewUrl.value);
 if (!res.ok)
 throw new Error('加载文件失败');
 const arrayBuffer = await res.arrayBuffer();
 const workbook = XLSX.read(arrayBuffer, { type: 'array' });
 const sheets = [];
 for (const name of workbook.SheetNames) {
 const sheet = workbook.Sheets[name];
 const jsonData = XLSX.utils.sheet_to_json(sheet, { header: 1 });
 if (jsonData.length > 0) {
 const headers = jsonData[0].map((_, i) => `列${i + 1}`);
 const rows = jsonData.slice(1).map(row => {
 const obj = {};
 headers.forEach((h, i) => { obj[h] = row[i] !== undefined ? row[i] : ''; });
 return obj;
 });
 sheets.push({ name, columns: headers, data: rows });
 }
 }
 excelSheets.value = sheets;
 }
 }
 catch (err) {
 console.error('加载文件失败:', err);
 error.value = err.message || '加载文件失败，请尝试下载后查看';
 }
 finally {
 loading.value = false;
 }
};
const handleDownload = () => {
 if (!props.file?.url) {
 ElMessage.info('文件URL无效，无法下载');
 return;
 }
 const downloadUrl = resolveFileUrl(props.file.url);
 if (!downloadUrl) {
 ElMessage.info('文件URL无效，无法下载');
 return;
 }
 window.open(downloadUrl, '_blank');
};
const handleClose = () => {
 txtContent.value = '';
 wordHtml.value = '';
 excelSheets.value = [];
 error.value = '';
 fileType.value = '';
};
watch(() => props.modelValue, (val) => {
 if (val && props.file) {
 loadFile();
 }
});
</script>

<style scoped>
.preview-container {
  min-height: 200px;
}

.preview-loading,
.preview-error,
.preview-unsupported {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  min-height: 200px;
}

.preview-loading p,
.preview-error p {
  margin-top: 12px;
  color: #909399;
}

.preview-error p {
  color: #f56c6c;
}

.preview-image {
  text-align: center;
}

.preview-video {
  text-align: center;
}

.preview-pdf {
  text-align: center;
}

.preview-txt {
  max-height: 550px;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 16px;
}

.txt-content {
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #303133;
  margin: 0;
}

.preview-word {
  max-height: 550px;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 20px;
}

.word-content :deep(h1),
.word-content :deep(h2),
.word-content :deep(h3) {
  color: #303133;
}

.word-content :deep(p) {
  line-height: 1.8;
  margin: 8px 0;
}

.word-content :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 12px 0;
}

.word-content :deep(table td),
.word-content :deep(table th) {
  border: 1px solid #dcdfe6;
  padding: 8px 12px;
}

.preview-excel {
  max-height: 550px;
  overflow-y: auto;
}

.excel-content :deep(.el-tabs) {
  margin-bottom: 12px;
}

.file-info {
  color: #666;
  font-size: 13px;
  margin: 12px 0;
}

.unsupported-msg {
  color: #909399;
  font-size: 14px;
}
</style>
