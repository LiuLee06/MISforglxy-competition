# 学期、教学计划与教学工作量 RESTful 接口现状

本文档以当前 `Controller` 和前端 `src/api/index.js` 为准，记录 RESTful 改造后的实际接口。旧版接口说明中的路径不再作为当前实现依据。

统一响应结构仍为：

```json
{
  "code": "200",
  "msg": "操作成功",
  "data": null
}
```

成功和失败同时使用 HTTP 状态码与响应体中的 `code` 表示：成功返回 `200`，请求参数或当前业务条件不满足返回 `400`，资源不存在返回 `404`，服务器异常返回 `500`。前端仍可继续读取 `Result.code`，同时网络工具也能根据 HTTP 状态码识别失败请求。

## 1. 学期资源 `/semester`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/semester` | 查询学期列表，可通过 `semesterName` 模糊查询 |
| GET | `/semester/{id}` | 查询单个学期 |
| GET | `/semester/current` | 查询当前学期 |
| POST | `/semester` | 新增学期，请求体为 `Semester` |
| PUT | `/semester/{id}` | 按路径 ID 全量更新学期，请求体中的 ID 以路径为准 |
| DELETE | `/semester/{id}` | 删除单个学期 |

## 2. 教学计划资源 `/teaching-schedule`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/teaching-schedule` | 查询教学计划，可使用 `teacherName`、`courseName`、`major`、`semesterId` 和 `confirmStatus` 筛选 |
| GET | `/teaching-schedule/{noticeId}` | 查询单条教学计划 |
| GET | `/teaching-schedule/editable?teacherName=xxx` | 查询指定教师可编辑字段的表示 |
| POST | `/teaching-schedule` | 新增教学计划 |
| POST | `/teaching-schedule/imports` | 批量导入或更新教学计划集合 |
| PUT | `/teaching-schedule/{noticeId}` | 按资源 ID 全量更新教学计划 |
| PUT | `/teaching-schedule/{noticeId}/editable-fields` | 更新指定教学计划的教师可编辑字段 |
| PUT | `/teaching-schedule/{noticeId}/confirmation` | 修改教学计划的确认状态 |
| DELETE | `/teaching-schedule/{noticeId}` | 删除单条教学计划 |
| DELETE | `/teaching-schedule?semesterId=xxx` | 删除指定学期的教学计划集合 |

其中 `PUT` 用于完整资源更新，也用于更新明确的资源子项；路径中不再使用 `list`、`update`、`confirm` 和 `batch` 等动作词。

## 3. 工作量资源 `/workload`

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/workload` | 按教师、课程、学期和确认状态查询工作量 |
| GET | `/workload/related` | 查询当前教师相关的工作量，仍通过 `currentTeacherName` 传递当前教师姓名 |
| GET | `/workload/department` | 查询教师所在部门的工作量 |
| GET | `/workload/completion` | 查询工作量确认完成度统计 |
| GET | `/workload/department-completion` | 查询部门工作量确认完成度统计 |
| GET | `/workload/{id}` | 查询单条工作量 |
| POST | `/workload/imports` | 批量导入工作量集合 |
| PUT | `/workload/{id}/actual-hours` | 修改指定工作量的实际学时 |
| PUT | `/workload/{id}/confirmation` | 修改指定工作量的确认状态 |
| DELETE | `/workload?semesterId=xxx` | 删除指定学期的工作量集合 |

工作量的批量导入被表示为 `imports` 子资源；实际学时和确认状态属于工作量资源的可修改子资源，因此使用 `PUT`，不再使用 `/batch` 和 `/confirm` 动作路径。

## 4. 当前仍需注意的实现边界

1. 工作量“当前教师”接口仍由前端传入 `currentTeacherName`，这是现有登录身份获取方式的限制，不是路径风格问题。若后续接入可靠的登录认证，应改为由后端从登录用户中获取身份。
2. 统计接口返回的是工作量完成度统计对象，不是单条工作量资源，因此保留 `completion` 和 `department-completion` 作为统计子资源。
3. 批量导入、按学期批量删除属于集合级操作，分别使用导入子资源和带筛选条件的集合 DELETE 表示。
