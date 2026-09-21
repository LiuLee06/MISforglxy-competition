# 学院行政 AI Agent 1.0

## 架构

前端使用现有 Axios 和 JWT 访问 /agent；后端由 AgentController 接收请求，AgentOrchestrator 负责对话循环，AgentToolRegistry 按当前 CurrentUserUtil 过滤工具，工具只调用现有 Service，不直接访问 Mapper。

Vue + JWT -> AgentController -> AgentOrchestrator -> LlmClient (mock/deepseek) -> AgentToolRegistry -> existing Service -> MySQL

大模型负责理解、规划和表达；Java 负责权限、参数、业务规则和真正执行。

## 文件知识库

管理员从“AI 知识库”页面上传 `.doc` 或 `.docx` 后，后端将原文件保存到 `FILE_STORE`，由 Word 解析工具提取文字、标题和原生表格生成 Markdown 初稿；后台再调用当前配置的 LLM 校验 Markdown，并根据最终 Markdown 生成标题、简介、关键词和文档类型，写入 `ai_knowledge_document`。处理状态为 `READY` 后才会参与问答，失败文件可以在管理页面重试。

AI 问答不会把所有正文直接塞进每次对话。Agent 先调用 `search_knowledge_catalog` 搜索文件目录，再调用 `read_knowledge_document` 读取目标文件的相关 Markdown 片段。回答必须以正文片段为依据，并标注文件名和章节；目录简介只用于选文件。第一版采用数据库目录和关键词检索，不引入向量数据库。

## Tool 清单

| Tool | 风险 | 权限 |
| --- | --- | --- |
| get_current_user | READ | 已登录 |
| search_teachers | READ | 已登录 |
| query_my_workload | READ | 已登录，仅当前教师 |
| query_workload_completion | READ | 管理员 |
| find_available_meeting_rooms | READ | 已登录 |
| query_my_room_applications | READ | 已登录，仅本人；管理员可查全部 |
| query_my_exam_assignments | READ | 已登录，仅当前教师 |
| query_notice_read_stats | READ | 管理员或通知发布者 |
| create_room_application | WRITE_CONFIRM | 已登录，申请人由服务器确定 |
| publish_notice | WRITE_CONFIRM | 管理员 |
| remind_notice | WRITE_CONFIRM | 管理员或通知发布者 |

知识库工具：

| search_knowledge_catalog | READ | 已登录教师或管理员 |
| read_knowledge_document | READ | 已登录教师或管理员 |

没有提供删除教师、删除通知、修改角色、任意 SQL 或任意 HTTP 工具。

## 配置

backend/src/main/resources/application.yaml 只读取环境变量：

- DB_URL、DB_USERNAME、DB_PASSWORD
- AI_PROVIDER=mock|deepseek
- DEEPSEEK_API_KEY
- DEEPSEEK_MODEL（默认 deepseek-v4-flash）
- AI_MAX_HISTORY_MESSAGES（默认 20）
- AI_MAX_TOOL_ITERATIONS（默认 6）
- AI_MAX_TOOL_CALLS（默认 8）

DeepSeek 只由后端调用，默认关闭 thinking。仓库不保存 API Key、数据库密码或 JWT。

## API

- POST /agent/chat：创建或继续会话
- GET /agent/conversations：当前用户会话列表
- GET /agent/conversations/{conversationId}/messages：当前用户历史消息
- POST /agent/actions/{actionId}/confirm：重新校验并幂等执行待确认写操作
- POST /agent/actions/{actionId}/cancel：取消待确认写操作

写操作进入 PENDING 后默认 10 分钟过期，确认时通过数据库条件更新抢占执行权，重复确认不会重复调用业务 Service。

## 数据表

执行 backend/SQL/create_ai_agent.sql 创建：

- ai_conversation
- ai_message
- ai_pending_action
- ai_action_log

执行 `backend/SQL/create_ai_knowledge.sql` 后还会创建 `FILE_STORE`（如尚不存在）和 `ai_knowledge_document`。

所有新增表均使用 ai_ 前缀，不修改原业务表。

## 测试和运行

1. 配置数据库环境变量。
2. 执行 SQL 脚本。
3. 使用 AI_PROVIDER=mock 启动，可不依赖模型服务验证对话、Tool Calling 和确认闭环。
4. 使用 AI_PROVIDER=deepseek 并设置 DEEPSEEK_API_KEY 接入真实模型。
5. 后端执行 backend/mvnw.cmd test，前端执行 frontend/npm run build。

手工演示建议：工作量查询、会议室查询、连续对话后提交预约、通知阅读统计。知识库演示时管理员先进入 `/ai-knowledge` 上传 Word 文件，等待状态变为“已完成”，再从 `/ai-assistant` 提问。
