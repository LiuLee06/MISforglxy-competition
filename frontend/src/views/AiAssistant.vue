<template>
  <div class="ai-page">
    <section class="ai-shell">
      <header class="ai-header">
        <div><h1>学院行政 AI 助手</h1><p>查询学院业务，并在确认后执行预约和通知操作</p></div>
        <el-button plain @click="startConversation">新建会话</el-button>
      </header>
      <div class="ai-body">
        <aside class="conversation-panel">
          <div class="history-heading"><div class="panel-title">历史会话</div><span v-if="conversations.length" class="history-count">{{ conversations.length }}</span></div>
          <div v-for="item in conversations" :key="item.conversationId" class="conversation-item-wrap">
            <button :class="['conversation-item', { active: item.conversationId === conversationId }]"
              @click="loadConversation(item.conversationId)"><span>{{ item.title || '学院行政助手' }}</span></button>
            <el-button class="delete-conversation" link type="danger" title="删除会话" aria-label="删除会话"
              @click.stop="deleteConversation(item)"><el-icon><Delete /></el-icon></el-button>
          </div>
          <div v-if="!conversations.length" class="empty-history">暂无历史会话</div>
        </aside>
        <main class="chat-panel">
          <div ref="messageList" class="message-list">
            <div v-if="!messages.length" class="welcome-card">
              <div class="welcome-icon">✦</div><h2>你好，我是学院行政 AI 助手</h2><p>你可以试试：</p>
              <button v-for="question in suggestions" :key="question" @click="useSuggestion(question)">{{ question }}</button>
            </div>
            <article v-for="(item, index) in messages" :key="item.localId || item.messageId || index"
              :class="['message-row', item.role === 'user' ? 'user-row' : 'assistant-row']">
              <div class="avatar">{{ item.role === 'user' ? '我' : 'AI' }}</div>
              <div class="message-content">
                <div v-if="item.role === 'assistant'" class="message-bubble markdown-body" v-html="renderMarkdown(item.content || '已完成处理。')"></div>
                <div v-else class="message-bubble">{{ item.content }}</div>
                <div v-if="item.toolName" class="stored-tool">{{ item.toolName }}</div>
                <div v-if="item.toolTrace?.length" class="tool-trace">
                  <span v-for="step in item.toolTrace" :key="step.tool + step.status" :class="['trace-step', step.status]">
                    {{ traceIcon(step.status) }} {{ step.displayName || step.tool }}
                  </span>
                </div>
              </div>
            </article>
            <article v-if="loading" class="message-row assistant-row"><div class="avatar">AI</div><div class="message-bubble typing">正在查询学院业务…</div></article>
          </div>
          <div class="composer">
            <div class="composer-card">
              <el-input class="composer-input" v-model="input" type="textarea" :autosize="{ minRows: 1, maxRows: 5 }" maxlength="4000"
                resize="none" placeholder="输入消息…" @keydown.enter.exact.prevent="sendMessage" />
              <div class="composer-footer"><div class="composer-actions"><span class="character-count">{{ input.length }}/4000</span><el-button class="send-button" type="primary" :loading="loading" @click="sendMessage"><el-icon><Promotion /></el-icon>发送</el-button></div></div>
            </div>
          </div>
        </main>
      </div>
    </section>
    <el-dialog v-model="confirmVisible" title="AI 准备执行以下操作" width="460px" :close-on-click-modal="false">
      <div v-if="pendingAction" class="confirm-card"><h3>{{ pendingAction.title }}</h3><p>{{ pendingAction.summary }}</p><p class="expire-tip">确认后系统会再次校验权限和业务状态，操作有效期 10 分钟。</p></div>
      <template #footer><el-button @click="cancelPending">取消</el-button><el-button type="primary" :loading="confirming" @click="confirmPending">确认执行</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { nextTick, onMounted, ref } from 'vue'
import { Delete, Promotion } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DOMPurify from 'dompurify'
import { marked } from 'marked'
import { agentApi } from '../api/index.js'
const conversations = ref([]), messages = ref([]), input = ref(''), conversationId = ref(null)
const loading = ref(false), confirming = ref(false), confirmVisible = ref(false), pendingAction = ref(null), messageList = ref(null)
const suggestions = ['我这学期的工作量是多少？', '帮我查一下明天下午可用的会议室。', '我这周有没有监考？', '还有哪些教师没有确认工作量？', '帮我看看我发布的通知还有多少人没读。']
marked.setOptions({ breaks: true, gfm: true })
const fieldLabels = '教师姓名|姓名|教师工号|教师编号|工号|所属部门|部门|职称|性别|联系电话|电话|邮箱|办公地点|研究方向'
const keyValuePattern = new RegExp(`^\\s*(${fieldLabels})\\s*(?:[:：]\\s*|\\s+)(.+?)\\s*$`)
const parseKeyValueLine = (line) => line.trim().match(keyValuePattern)
const toMarkdownTable = (rows) => ['| 字段 | 内容 |', '| --- | --- |', ...rows.map(row => `| ${row[1]} | ${row[2].replaceAll('|', '\\\\|')} |`)].join('\\n')
const normalizeStructuredTables = (content) => {
  const lines = String(content || '').replaceAll('\\r\\n', '\\n').split('\\n')
  const normalized = []
  for (let index = 0; index < lines.length;) {
    if (/^\\s*字段\\s+内容\\s*$/.test(lines[index])) {
      const rows = []
      let next = index + 1
      while (next < lines.length) {
        const row = parseKeyValueLine(lines[next])
        if (!row) break
        rows.push(row)
        next += 1
      }
      if (rows.length) {
        normalized.push(toMarkdownTable(rows))
        index = next
        continue
      }
    }
    normalized.push(lines[index])
    index += 1
  }
  return normalized.join('\\n')
}
const toMarkdownTableFixed = (rows) => ['| 字段 | 内容 |', '| --- | --- |', ...rows.map(row => '| ' + row[1] + ' | ' + row[2].replaceAll('|', '\\|') + ' |')].join('\n')
const normalizeStructuredTablesFixed = (content) => {
  const lines = String(content || '').replaceAll('\r\n', '\n').split('\n')
  const normalized = []
  for (let index = 0; index < lines.length;) {
    if (/^\s*\*{0,2}字段\*{0,2}\s+\*{0,2}内容\*{0,2}\s*$/.test(lines[index])) {
      const rows = []
      let next = index + 1
      while (next < lines.length) {
        const row = parseKeyValueLine(lines[next])
        if (!row) break
        rows.push(row)
        next += 1
      }
      if (rows.length) {
        normalized.push(toMarkdownTableFixed(rows))
        index = next
        continue
      }
    }
    normalized.push(lines[index])
    index += 1
  }
  return normalized.join('\n')
}
const renderMarkdown = (content) => DOMPurify.sanitize(marked.parse(normalizeStructuredTablesFixed(content)))
const loadConversations = async () => { try { const res = await agentApi.getConversations(); if (res.code === '200') conversations.value = res.data || [] } catch (e) { console.error(e) } }
const loadConversation = async (id) => { conversationId.value = id; try { const res = await agentApi.getMessages(id); if (res.code === '200') messages.value = (res.data || []).filter(x => x.role !== 'system' && x.role !== 'tool' && (x.role === 'user' || x.content)).map(x => ({ ...x, localId: x.messageId })) } catch (e) { ElMessage.error('加载会话失败') } }
const startConversation = () => { conversationId.value = null; messages.value = []; pendingAction.value = null; confirmVisible.value = false }
const deleteConversation = async (item) => {
  try {
    await ElMessageBox.confirm(`确定删除会话“${item.title || '学院行政助手'}”吗？删除后对话记录不可恢复。`, '删除历史会话', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
    const res = await agentApi.deleteConversation(item.conversationId)
    if (res.code !== '200') { ElMessage.error(res.msg || '删除会话失败'); return }
    conversations.value = conversations.value.filter(x => x.conversationId !== item.conversationId)
    if (conversationId.value === item.conversationId) startConversation()
    ElMessage.success('历史会话已删除')
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') { console.error(e); ElMessage.error('删除会话失败') }
  }
}
const useSuggestion = (question) => { input.value = question; sendMessage() }
const sendMessage = async () => {
  const text = input.value.trim(); if (!text || loading.value) return
  messages.value.push({ role: 'user', content: text, localId: 'user-' + Date.now() }); input.value = ''; loading.value = true; scrollToBottom()
  try {
    const res = await agentApi.chat({ conversationId: conversationId.value, message: text })
    if (res.code !== '200' || !res.data) { ElMessage.error(res.msg || 'AI 请求失败'); return }
    const data = res.data; conversationId.value = data.conversationId
    if (data.message) messages.value.push({ role: 'assistant', content: data.message, toolTrace: data.toolTrace || [], localId: 'assistant-' + Date.now() })
    if (data.status === 'need_confirmation' && data.pendingAction) { pendingAction.value = data.pendingAction; confirmVisible.value = true }
    await loadConversations(); scrollToBottom()
  } catch (e) { ElMessage.error('AI 服务暂时不可用，请稍后重试') } finally { loading.value = false }
}
const confirmPending = async () => {
  if (!pendingAction.value) return; confirming.value = true
  try {
    const res = await agentApi.confirmAction(pendingAction.value.actionId)
    const result = res.data
    if (res.code === '200' && result?.success === true && result.status === 'EXECUTED') {
      ElMessage.success(result.message || '操作已完成。')
      confirmVisible.value = false; pendingAction.value = null
      if (conversationId.value) await loadConversation(conversationId.value)
    } else {
      ElMessage.error(result?.message || res.msg || '操作未完成')
      if (result?.status === 'EXPIRED' || result?.status === 'REJECTED' || result?.status === 'FAILED') {
        confirmVisible.value = false; pendingAction.value = null
        if (conversationId.value) await loadConversation(conversationId.value)
      }
    }
  } catch (e) { ElMessage.error('确认操作失败') } finally { confirming.value = false }
}
const cancelPending = async () => { if (!pendingAction.value) return; try { const res = await agentApi.cancelAction(pendingAction.value.actionId); if (res.code === '200' && res.data?.status === 'CANCELLED') { ElMessage.info('操作已取消'); if (conversationId.value) await loadConversation(conversationId.value) } else ElMessage.error(res.msg || '取消失败') } finally { confirmVisible.value = false; pendingAction.value = null } }
const traceIcon = (status) => ({ success: '✓', failed: '✕', pending_confirmation: '○' }[status] || '·')
const scrollToBottom = () => nextTick(() => { if (messageList.value) messageList.value.scrollTop = messageList.value.scrollHeight })
onMounted(loadConversations)
</script>
<style scoped>
.ai-page{min-height:calc(100vh - 130px);padding:8px;color:#26364a}.ai-shell{max-width:1320px;margin:auto;background:#fff;border-radius:18px;box-shadow:0 10px 35px #3d608726;overflow:hidden}.ai-header{padding:26px 30px;display:flex;justify-content:space-between;align-items:center;background:linear-gradient(120deg,#eef7ff,#f8fbff);border-bottom:1px solid #e6eef7}.ai-header h1{margin:0 0 8px;font-size:25px}.ai-header p{margin:0;color:#6e7f92}.ai-body{display:flex;min-height:650px}.conversation-panel{width:240px;border-right:1px solid #edf1f5;padding:18px 12px;background:#fbfdff}.history-heading{display:flex;align-items:center;justify-content:space-between}.panel-title{font-weight:600;padding:0 10px 12px;color:#53677d}.history-count{margin:0 8px 12px 0;padding:2px 7px;border-radius:10px;background:#e8f2fb;color:#6b89a5;font-size:12px}.conversation-item-wrap{display:flex;align-items:center;gap:2px;margin-bottom:3px}.conversation-item{flex:1;min-width:0;border:0;background:transparent;text-align:left;padding:11px 10px;border-radius:9px;cursor:pointer;color:#526477;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.conversation-item span{display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.conversation-item:hover,.conversation-item.active{background:#e9f3ff;color:#2272c8}.delete-conversation{flex:0 0 30px;opacity:0;color:#9baec0}.conversation-item-wrap:hover .delete-conversation,.conversation-item-wrap:focus-within .delete-conversation{opacity:1}.delete-conversation:hover{color:#f56c6c}.empty-history{color:#a8b4c1;font-size:13px;padding:10px}.chat-panel{flex:1;display:flex;flex-direction:column;min-width:0}.message-list{flex:1;overflow-y:auto;padding:28px clamp(18px,5vw,70px);max-height:610px}.welcome-card{text-align:center;max-width:560px;margin:85px auto;color:#60758b}.welcome-icon{margin:auto;width:48px;height:48px;border-radius:50%;line-height:48px;color:#fff;background:linear-gradient(135deg,#4f9ce8,#8e7cf2);font-size:26px}.welcome-card h2{color:#34495e;font-size:22px}.welcome-card button{margin:5px;border:1px solid #d5e6f8;border-radius:16px;padding:8px 13px;color:#3775ae;background:#f7fbff;cursor:pointer}.message-row{display:flex;gap:10px;margin-bottom:22px;align-items:flex-start}.user-row{flex-direction:row-reverse}.avatar{flex:0 0 34px;height:34px;border-radius:50%;background:#4c9be8;color:#fff;text-align:center;line-height:34px;font-size:13px}.user-row .avatar{background:#6fc49a}.message-content{max-width:min(78%,760px)}.message-bubble{padding:12px 15px;border-radius:14px;background:#f3f7fb;line-height:1.7;word-break:break-word}.user-row .message-bubble{background:#e5f3e9;white-space:pre-wrap}.markdown-body :first-child{margin-top:0}.markdown-body :last-child{margin-bottom:0}.markdown-body p{margin:0 0 10px}.markdown-body ul,.markdown-body ol{padding-left:22px;margin:8px 0}.markdown-body li{margin:4px 0}.markdown-body h1,.markdown-body h2,.markdown-body h3{margin:12px 0 8px;color:#2f4359;line-height:1.35}.markdown-body h1{font-size:1.35em}.markdown-body h2{font-size:1.2em}.markdown-body h3{font-size:1.08em}.markdown-body strong{color:#233b55}.markdown-body code{padding:2px 5px;border-radius:4px;background:#e8eef5;font-size:.9em}.markdown-body pre{padding:12px;overflow-x:auto;border-radius:8px;background:#203247;color:#edf5ff}.markdown-body pre code{padding:0;background:transparent;color:inherit}.markdown-body blockquote{margin:10px 0;padding:3px 12px;border-left:3px solid #8bb9e7;color:#6b7e91;background:#edf4fb}.stored-tool{margin-top:6px;color:#6990b3;font-size:12px}.composer{padding:14px 28px 20px;border-top:1px solid #edf1f5;background:linear-gradient(180deg,#fff,#f7fafc)}.composer-card{padding:13px 16px 11px;border:1px solid #dbe6f0;border-radius:16px;background:#fff;box-shadow:0 5px 18px #47749c14;transition:border-color .2s,box-shadow .2s}.composer-card:focus-within{border-color:#78b2e8;box-shadow:0 6px 22px #47749c26}.composer-topline{display:flex;align-items:center;gap:7px;color:#58728b;font-size:12px;margin:0 2px 7px}.online-dot{width:7px;height:7px;border-radius:50%;background:#67c49a;box-shadow:0 0 0 3px #e3f5ec}.composer-tip{margin-left:auto;color:#9aabb9}.composer-input{display:block}.composer-input :deep(.el-textarea__inner){padding:5px 2px 4px;border:0;box-shadow:none;color:#26364a;font-size:15px;line-height:1.65}.composer-input :deep(.el-textarea__inner:focus){box-shadow:none}.composer-input :deep(.el-textarea__inner::placeholder){color:#a6b5c3}.composer-footer{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-top:7px}.composer-placeholder{color:#a6b5c3;font-size:12px}.composer-actions{display:flex;align-items:center;gap:12px}.character-count{color:#a2b0bd;font-size:12px}.send-button{height:36px;padding:0 17px;border-radius:10px;font-weight:600;box-shadow:0 5px 12px #409eff3b}.send-button .el-icon{margin-right:5px}.typing{color:#7e91a5}.confirm-card{padding:18px 20px;border:1px solid #d7e8fa;border-radius:12px;background:#f7fbff}.confirm-card h3{margin-top:0;color:#285f91}.confirm-card p{line-height:1.7}.expire-tip{color:#8a98a5;font-size:13px}@media(max-width:768px){.conversation-panel{display:none}.ai-header{padding:20px}.ai-header h1{font-size:20px}.ai-header p{font-size:13px}.message-list{padding:20px 14px}.composer{padding:12px 14px 16px}.composer-tip,.composer-placeholder{display:none}.composer-card{padding:12px}.composer-footer{justify-content:flex-end}.message-content{max-width:82%}}
.markdown-body ul,.markdown-body ol{padding-left:0;list-style:none}
.markdown-body li{position:relative;padding-left:22px}
.markdown-body ul>li::before{content:'•';position:absolute;left:2px;top:0;color:#6d86a0;font-size:1.2em;line-height:1.4}
.markdown-body table{width:100%;border-collapse:collapse;margin:12px 0;font-size:14px}
.markdown-body th,.markdown-body td{border:1px solid #dbe5ef;padding:8px 10px;text-align:left}
.markdown-body th{background:#f5f8fb;color:#34495e;font-weight:600}
.tool-trace{display:flex;flex-wrap:wrap;gap:6px 12px;margin-top:8px;color:#7890a5;font-size:12px}
.trace-step.success{color:#4d9b77}.trace-step.failed{color:#d66b6b}.trace-step.pending_confirmation{color:#c08b45}
.markdown-body ol{counter-reset:markdown-item}
.markdown-body ol>li{counter-increment:markdown-item}
.markdown-body ol>li::before{content:counter(markdown-item) '.';position:absolute;left:0;top:0;color:#6d86a0;font-weight:600}
.markdown-body li ul,.markdown-body li ol{margin:4px 0}
.markdown-body table{width:100%;margin:14px 0;border:1px solid #d8e4ee;border-radius:10px;border-spacing:0;border-collapse:separate;overflow:hidden;background:#fff;font-size:.95em}
.markdown-body th,.markdown-body td{padding:10px 12px;text-align:left;border-right:1px solid #e4edf4;border-bottom:1px solid #e4edf4;line-height:1.45;white-space:nowrap}
.markdown-body th{background:#eaf3fb;color:#2d587d;font-weight:600}
.markdown-body tr:nth-child(even) td{background:#f8fbfd}
.markdown-body th:last-child,.markdown-body td:last-child{border-right:0}
.markdown-body tr:last-child td{border-bottom:0}
.ai-page{min-height:calc(100vh - 104px);padding:0;background:#f7f9fc;color:#27384b}
.ai-shell{max-width:none;min-height:calc(100vh - 104px);margin:0;border:1px solid #e3eaf1;border-radius:0;box-shadow:none;background:#fff}
.ai-header{min-height:72px;padding:16px 28px;background:#fff;border-bottom:1px solid #e5ebf1}
.ai-header h1{font-size:20px;letter-spacing:.01em;color:#1e3349;margin-bottom:5px}
.ai-header p{font-size:13px;color:#8595a5}
.ai-header .el-button{border-color:#d6e0ea;color:#4c647b;border-radius:8px;background:#fff}
.ai-header .el-button:hover{border-color:#82b5e5;color:#2876bd;background:#f5faff}
.ai-body{min-height:calc(100vh - 176px);height:calc(100vh - 176px);background:#fff}
.conversation-panel{width:260px;padding:22px 14px;background:#f8fafc;border-right:1px solid #e5ebf1}
.history-heading{padding:0 4px}
.panel-title{padding:0 8px 14px;color:#40566d;font-size:14px}
.history-count{margin-right:8px;background:#eef3f8;color:#8093a5}
.conversation-item-wrap{margin:2px 0}
.conversation-item{padding:10px 11px;border-radius:7px;color:#627589;font-size:13px}
.conversation-item:hover,.conversation-item.active{background:#e9f2fb;color:#216da9}
.delete-conversation{opacity:0}
.chat-panel{background:#fff}
.message-list{max-height:none;min-height:0;padding:36px clamp(24px,7vw,110px);background:#fff}
.message-row{gap:12px;margin-bottom:30px}
.avatar{flex-basis:32px;width:32px;height:32px;line-height:32px;background:linear-gradient(135deg,#4d9ee9,#7b7fea);font-size:12px}
.user-row .avatar{background:#6fc49a}
.message-content{max-width:min(82%,850px)}
.assistant-row .message-bubble{padding:0 2px;background:transparent;border-radius:0}
.user-row .message-bubble{padding:10px 15px;border:1px solid #e3eee7;border-radius:15px;background:#f0f7f2}
.stored-tool{margin-top:8px;color:#8498a9}
.markdown-body{font-size:15px;line-height:1.8;color:#2f4358}
.markdown-body table{margin:16px 0;border-color:#dce6ef;box-shadow:0 1px 2px #304b6110}
.markdown-body th{background:#f1f6fa;color:#385d7b}
.composer{padding:16px 28px 20px;border-top:1px solid #e5ebf1;background:#fff}
.composer-card{padding:0;border:0;border-radius:0;background:transparent;box-shadow:none}
.composer-card:focus-within{border-color:transparent;box-shadow:none}
.composer-topline{padding:0 2px;margin-bottom:8px;color:#73879a}
.composer-tip{color:#a1afbc}
.composer-input :deep(.el-textarea__inner){padding:12px 14px;border:1px solid #d7e2ec;border-radius:11px;background:#fbfcfe;font-size:14px;line-height:1.65;transition:border-color .2s,box-shadow .2s}
.composer-input :deep(.el-textarea__inner:focus){border-color:#79afe0;box-shadow:0 0 0 3px #409eff18;background:#fff}
.composer-footer{margin-top:9px}
.composer-placeholder{color:#9aa9b7}
.send-button{height:34px;border-radius:8px;box-shadow:none}
.welcome-card{max-width:680px;margin:clamp(70px,13vh,150px) auto;color:#718398}
.welcome-card h2{font-size:24px;color:#243b53;margin:16px 0 8px}
.welcome-card p{font-size:14px}
.welcome-card button{margin:5px;padding:9px 14px;border-radius:18px;background:#fff;border-color:#d6e5f3;color:#4f83b1;transition:all .2s}
.welcome-card button:hover{border-color:#79afe0;background:#f5faff;transform:translateY(-1px)}
@media(max-width:768px){.ai-page,.ai-shell{min-height:calc(100vh - 80px)}.ai-header{padding:16px 18px}.ai-body{height:calc(100vh - 152px);min-height:0}.conversation-panel{display:none}.message-list{padding:26px 18px}.composer{padding:12px 14px 16px}.message-content{max-width:84%}}
.ai-shell{border:0}
.composer{padding:14px 24px 18px}
.composer-card{padding:10px 12px 8px;border:1px solid #d7e2ec;border-radius:12px;background:#fff;box-shadow:0 2px 8px #304b610d}
.composer-card:focus-within{border-color:#79afe0;box-shadow:0 0 0 3px #409eff14}
.composer-input :deep(.el-textarea__inner){padding:4px 2px 8px;border:0;border-radius:0;background:transparent;box-shadow:none;font-size:15px}
.composer-input :deep(.el-textarea__inner:focus){border:0;background:transparent;box-shadow:none}
.composer-footer{justify-content:flex-end;margin-top:3px}
.composer-actions{margin-left:auto}
.character-count{font-size:11px;color:#a4b0bb}
.send-button{height:32px;padding:0 14px;border-radius:7px;font-size:13px}
@media(max-width:768px){.composer{padding:10px 14px 14px}.composer-card{padding:9px 11px 7px}}
.message-list{padding-left:24px;padding-right:24px}
.message-row{width:100%}
.assistant-row{justify-content:flex-start}
.user-row{justify-content:flex-start}
@media(max-width:768px){.message-list{padding-left:16px;padding-right:16px}}
</style>
<style>
.ai-page .markdown-body :first-child{margin-top:0}
.ai-page .markdown-body :last-child{margin-bottom:0}
.ai-page .markdown-body p{margin:0 0 10px}
.ai-page .markdown-body ul,.ai-page .markdown-body ol{padding-left:0;margin:8px 0;list-style:none}
.ai-page .markdown-body li{position:relative;padding-left:22px;margin:4px 0}
.ai-page .markdown-body ul>li::before{content:'•';position:absolute;left:2px;top:0;color:#6d86a0;font-size:1.2em;line-height:1.4}
.ai-page .markdown-body ol{counter-reset:markdown-item}
.ai-page .markdown-body ol>li{counter-increment:markdown-item}
.ai-page .markdown-body ol>li::before{content:counter(markdown-item) '.';position:absolute;left:0;top:0;color:#6d86a0;font-weight:600}
.ai-page .markdown-body li ul,.ai-page .markdown-body li ol{margin:4px 0}
.ai-page .markdown-body h1,.ai-page .markdown-body h2,.ai-page .markdown-body h3{margin:12px 0 8px;color:#2f4359;line-height:1.35}
.ai-page .markdown-body h1{font-size:1.35em}.ai-page .markdown-body h2{font-size:1.2em}.ai-page .markdown-body h3{font-size:1.08em}
.ai-page .markdown-body strong{color:#233b55}
.ai-page .markdown-body code{padding:2px 5px;border-radius:4px;background:#e8eef5;font-size:.9em}
.ai-page .markdown-body pre{padding:12px;overflow-x:auto;border-radius:8px;background:#203247;color:#edf5ff}
.ai-page .markdown-body pre code{padding:0;background:transparent;color:inherit}
.ai-page .markdown-body blockquote{margin:10px 0;padding:3px 12px;border-left:3px solid #8bb9e7;color:#6b7e91;background:#edf4fb}
.ai-page .markdown-body table{width:100%;margin:16px 0;border:1px solid #d8e4ee;border-radius:10px;border-spacing:0;border-collapse:separate;overflow:hidden;background:#fff;box-shadow:0 1px 2px #304b6110;font-size:.95em}
.ai-page .markdown-body th,.ai-page .markdown-body td{padding:10px 12px;text-align:left;border-right:1px solid #e4edf4;border-bottom:1px solid #e4edf4;line-height:1.45;white-space:nowrap}
.ai-page .markdown-body th{background:#f1f6fa;color:#385d7b;font-weight:600}
.ai-page .markdown-body tr:nth-child(even) td{background:#f8fbfd}
.ai-page .markdown-body th:last-child,.ai-page .markdown-body td:last-child{border-right:0}
.ai-page .markdown-body tr:last-child td{border-bottom:0}
</style>
