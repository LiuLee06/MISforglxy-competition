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
