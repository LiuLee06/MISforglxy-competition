# MISforglxy 学院管理系统

学院管理系统比赛版仓库，包含前端和后端两个部分。

## 目录结构

- `frontend/`：Vue 前端项目
- `backend/`：Spring Boot 后端项目

当前仓库对应学院项目的稳定版本 `v1.0.0`。后续比赛功能将在此基础上继续优化。

## 说明

项目中的配置文件、数据库脚本和示例数据在公开前需要根据实际部署环境进行检查，并完成敏感信息脱敏。

## 本地配置与 AI 联调

复制 `backend/src/main/resources/application-local.example.yaml` 为本地配置文件，填写数据库连接信息；真实配置不要提交到 Git。也可以使用环境变量 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`AI_PROVIDER`、`DEEPSEEK_API_KEY`、`DEEPSEEK_MODEL` 和 `DEEPSEEK_TIMEOUT_SECONDS`。

默认 `AI_PROVIDER=mock`。切换 DeepSeek 联调时设置 `AI_PROVIDER=deepseek`，并提供 `DEEPSEEK_API_KEY`。当前 Agent 版本强制关闭 Thinking Tool Calling。仓库历史中曾出现过的凭据不能依靠代码隐藏，部署前必须人工轮换数据库密码、JWT Secret 和 API Key。

## Clone 后快速运行

1. 准备云端 MySQL，并在云数据库中执行 `backend/SQL/` 下的基础表、业务扩展和 AI 相关脚本；至少需要执行 `acad_inte_sys.sql`、`create_ai_agent.sql`、`create_ai_knowledge.sql`、`add_room_apply_purpose.sql`，以及项目实际用到的业务扩展脚本。知识库脚本使用现有 `FILE_STORE` 表保存原始文件，确保该表已经在基础数据库中创建。确保云数据库已开放访问并加入当前客户端 IP 白名单。
2. 复制 `backend/src/main/resources/application-local.example.yaml` 为 `backend/application-local.yaml`，只修改这个本地文件：
   - `spring.datasource.url`：云数据库地址、端口和库名
   - `spring.datasource.username/password`：数据库账号密码
   - `ai.provider`：使用 DeepSeek 填 `deepseek`
   - `ai.deepseek.api-key/model`：DeepSeek Key 和模型名称
3. 在 `backend/` 执行 `start_backend.bat` 启动后端。
4. 在 `frontend/` 执行 `npm install`、`npm run dev`，访问 `http://localhost:5174`。

不要修改或提交主配置 `application.yaml`、示例配置和 `backend/application-local.yaml` 中的真实密钥，也不要把本地配置上传到 GitHub。

## AI 文件知识库

管理员登录后可从左侧“AI 知识库”上传 `.doc` 或 `.docx` 文件。系统会在后台提取文字和原生表格，调用当前配置的 AI 模型检查 Markdown 并生成目录简介。状态为“已完成”的文件才会参与 AI 问答；教师在“AI 助手”中提问时，Agent 会先检索文件目录，再读取相关正文片段并在回答中标注文件名和章节。

知识库相关可选配置如下，默认值适合普通课程设计部署：

- `AI_KNOWLEDGE_MAX_FILE_SIZE_MB`：单个文件大小，默认 20MB；
- `AI_KNOWLEDGE_CHUNK_CHARS`：Markdown 校验分段长度，默认 2000；
- `AI_KNOWLEDGE_SUMMARY_CHARS`：生成目录简介时最多送入模型的字符数，默认 8000；
- `DEEPSEEK_MAX_OUTPUT_TOKENS`：DeepSeek 单次输出上限，默认 1536。
