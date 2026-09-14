# Workload（工作量管理）接口文档

> 注意：本文档包含早期 CRUD 草稿，部分接口并未存在于当前 `WorkloadController`。当前实际路径、方法和参数请以 `restful-api-reference.md` 和 `WorkloadController` 为准。

## 1. 基础信息

| 项目 | 说明 |
|------|------|
| **接口前缀** | `/workload` |
| **请求方式** | RESTful |
| **响应格式** | JSON |
| **字符编码** | UTF-8 |

---

## 2. 统一响应结构

所有接口均返回以下 JSON 结构：

```json
{
  "code": "200",
  "msg": "成功",
  "data": {}
}
```

### 响应字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `code` | String | 状态码：`"200"` 表示成功，`"500"` 表示系统异常 |
| `msg` | String | 提示信息 |
| `data` | Object/Array/null | 响应数据，查询接口返回对象或数组，增删改接口通常为 null |

### 常见状态码

| 状态码 | 说明 |
|--------|------|
| `200` | 操作成功 |
| `500` | 系统异常/操作失败 |

---

## 3. 数据模型 — Workload

### 3.1 完整结构（查询接口返回）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `wlId` | Integer | 是 | 工作量ID（主键） |
| `courseNo` | String | 是 | 课程编号 |
| `courseName` | String | 是 | 课程名称 |
| `hourType` | String | 是 | 课时类型（如：理论、实验、实习等） |
| `notificationNo` | String | 否 | 通知单号 |
| `transferHours` | Integer | 否 | 调课时数 |
| `actualHours` | Integer | 是 | 实际课时 |
| `totalHours` | Integer | 是 | 总课时 |
| `totalCredit` | Double | 是 | 总学分 |
| `actualHourRatio` | Double | 是 | 实际课时系数 |
| `studentCount` | Integer | 是 | 学生人数 |
| `className` | String | 是 | 班级名称 |
| `teacher` | Object | 是 | **嵌套对象**：关联的教师信息 |
| `semester` | Object | 是 | **嵌套对象**：关联的学期信息 |

### 3.2 嵌套对象 — Teacher（教师信息）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `teacherId` | Integer | 教师ID |
| `name` | String | 教师姓名 |
| `professionalTitle` | String | 职称 |
| `position` | String | 职务 |

### 3.3 嵌套对象 — Semester（学期信息）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `semesterId` | Integer | 学期ID |
| `semesterName` | String | 学期名称（如：2024-2025-1） |
| `year` | Integer | 年份 |

---

## 4. 接口列表

### 4.1 查询所有工作量

**接口描述**：获取系统中所有工作量记录（已自动关联教师和学期信息）

| 项目 | 说明 |
|------|------|
| **请求方法** | `GET` |
| **请求路径** | `/workload` |
| **请求参数** | 无 |
| **响应数据** | `Workload[]` 数组 |

**请求示例：**
```http
GET /workload HTTP/1.1
Host: localhost:8080
```

**成功响应示例：**
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "wlId": 1,
      "courseNo": "CS101",
      "courseName": "数据结构",
      "hourType": "理论",
      "notificationNo": "TZ2024001",
      "transferHours": 0,
      "actualHours": 64,
      "totalHours": 64,
      "totalCredit": 4.0,
      "actualHourRatio": 1.0,
      "studentCount": 45,
      "className": "计科2101",
      "teacher": {
        "teacherId": 1,
        "name": "张三",
        "professionalTitle": "教授",
        "position": "系主任"
      },
      "semester": {
        "semesterId": 1,
        "semesterName": "2024-2025-1",
        "year": 2024
      }
    }
  ]
}
```

---

### 4.2 根据ID查询单个工作量

**接口描述**：根据工作量ID查询单条记录的详细信息

| 项目 | 说明 |
|------|------|
| **请求方法** | `GET` |
| **请求路径** | `/workload/{id}` |
| **路径参数** | `id` — Integer，工作量ID |
| **响应数据** | 单个 `Workload` 对象 |

**请求示例：**
```http
GET /workload/1 HTTP/1.1
Host: localhost:8080
```

**成功响应示例：**
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "wlId": 1,
    "courseNo": "CS101",
    "courseName": "数据结构",
    "hourType": "理论",
    "notificationNo": "TZ2024001",
    "transferHours": 0,
    "actualHours": 64,
    "totalHours": 64,
    "totalCredit": 4.0,
    "actualHourRatio": 1.0,
    "studentCount": 45,
    "className": "计科2101",
    "teacher": {
      "teacherId": 1,
      "name": "张三",
      "professionalTitle": "教授",
      "position": "系主任"
    },
    "semester": {
      "semesterId": 1,
      "semesterName": "2024-2025-1",
      "year": 2024
    }
  }
}
```

---

### 4.3 根据教师ID查询工作量

**接口描述**：查询指定教师的所有工作量记录

| 项目 | 说明 |
|------|------|
| **请求方法** | `GET` |
| **请求路径** | `/workload/teacher/{teacherId}` |
| **路径参数** | `teacherId` — Integer，教师ID |
| **响应数据** | `Workload[]` 数组 |

**请求示例：**
```http
GET /workload/teacher/1 HTTP/1.1
Host: localhost:8080
```

**成功响应示例：**
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "wlId": 1,
      "courseNo": "CS101",
      "courseName": "数据结构",
      "hourType": "理论",
      "notificationNo": "TZ2024001",
      "transferHours": 0,
      "actualHours": 64,
      "totalHours": 64,
      "totalCredit": 4.0,
      "actualHourRatio": 1.0,
      "studentCount": 45,
      "className": "计科2101",
      "teacher": {
        "teacherId": 1,
        "name": "张三",
        "professionalTitle": "教授",
        "position": "系主任"
      },
      "semester": {
        "semesterId": 1,
        "semesterName": "2024-2025-1",
        "year": 2024
      }
    }
  ]
}
```

---

### 4.4 根据学期ID查询工作量

**接口描述**：查询指定学期的所有工作量记录

| 项目 | 说明 |
|------|------|
| **请求方法** | `GET` |
| **请求路径** | `/workload/semester/{semesterId}` |
| **路径参数** | `semesterId` — Integer，学期ID |
| **响应数据** | `Workload[]` 数组 |

**请求示例：**
```http
GET /workload/semester/1 HTTP/1.1
Host: localhost:8080
```

**成功响应示例：**
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "wlId": 1,
      "courseNo": "CS101",
      "courseName": "数据结构",
      "hourType": "理论",
      "notificationNo": "TZ2024001",
      "transferHours": 0,
      "actualHours": 64,
      "totalHours": 64,
      "totalCredit": 4.0,
      "actualHourRatio": 1.0,
      "studentCount": 45,
      "className": "计科2101",
      "teacher": {
        "teacherId": 1,
        "name": "张三",
        "professionalTitle": "教授",
        "position": "系主任"
      },
      "semester": {
        "semesterId": 1,
        "semesterName": "2024-2025-1",
        "year": 2024
      }
    }
  ]
}
```

---

### 4.5 查询所有工作量详情（多表JOIN）

**接口描述**：获取所有工作量记录（与 `/workload` 返回结构相同，已做 LEFT JOIN 关联）

| 项目 | 说明 |
|------|------|
| **请求方法** | `GET` |
| **请求路径** | `/workload/details` |
| **请求参数** | 无 |
| **响应数据** | `Workload[]` 数组 |

**请求示例：**
```http
GET /workload/details HTTP/1.1
Host: localhost:8080
```

> ⚠️ **注意**：该接口与 `GET /workload` 返回结构完全相同，前端可任选其一使用。

---

### 4.6 根据教师ID查询工作量详情

**接口描述**：查询指定教师的工作量详情（与 `/workload/teacher/{teacherId}` 返回结构相同）

| 项目 | 说明 |
|------|------|
| **请求方法** | `GET` |
| **请求路径** | `/workload/details/teacher/{teacherId}` |
| **路径参数** | `teacherId` — Integer，教师ID |
| **响应数据** | `Workload[]` 数组 |

**请求示例：**
```http
GET /workload/details/teacher/1 HTTP/1.1
Host: localhost:8080
```

> ⚠️ **注意**：该接口与 `GET /workload/teacher/{teacherId}` 返回结构完全相同，前端可任选其一使用。

---

### 4.7 新增工作量

**接口描述**：创建一条新的工作量记录

| 项目 | 说明 |
|------|------|
| **请求方法** | `POST` |
| **请求路径** | `/workload` |
| **Content-Type** | `application/json` |
| **请求体** | `Workload` 对象（见下方示例） |
| **响应数据** | 成功时 `data: null`，失败时返回错误信息 |

**请求体示例：**
```json
{
  "wlId": null,
  "courseNo": "CS102",
  "courseName": "操作系统",
  "hourType": "理论",
  "notificationNo": "TZ2024002",
  "transferHours": 2,
  "actualHours": 62,
  "totalHours": 64,
  "totalCredit": 4.0,
  "actualHourRatio": 0.96875,
  "studentCount": 40,
  "className": "计科2102",
  "teacher": {
    "teacherId": 1
  },
  "semester": {
    "semesterId": 1
  }
}
```

**请求示例：**
```http
POST /workload HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "wlId": null,
  "courseNo": "CS102",
  "courseName": "操作系统",
  "hourType": "理论",
  "notificationNo": "TZ2024002",
  "transferHours": 2,
  "actualHours": 62,
  "totalHours": 64,
  "totalCredit": 4.0,
  "actualHourRatio": 0.96875,
  "studentCount": 40,
  "className": "计科2102",
  "teacher": {
    "teacherId": 1
  },
  "semester": {
    "semesterId": 1
  }
}
```

**成功响应示例：**
```json
{
  "code": "200",
  "msg": "成功",
  "data": null
}
```

**失败响应示例：**
```json
{
  "code": "500",
  "msg": "系统异常",
  "data": null
}
```

> 📌 **注意事项**：
> - 新增时 `wlId` 建议传 `null`，由数据库自增生成。
> - `teacher` 对象只需传 `teacherId`，其他字段可选。
> - `semester` 对象只需传 `semesterId`，其他字段可选。
> - 所有业务字段（`courseNo`、`courseName` 等）均为必填项。

---

### 4.8 更新工作量

**接口描述**：更新已有的工作量记录（全量更新）

| 项目 | 说明 |
|------|------|
| **请求方法** | `PUT` |
| **请求路径** | `/workload` |
| **Content-Type** | `application/json` |
| **请求体** | `Workload` 对象（必须包含 `wlId`） |
| **响应数据** | 成功时 `data: null`，失败时返回错误信息 |

**请求体示例：**
```json
{
  "wlId": 1,
  "courseNo": "CS101",
  "courseName": "数据结构（修订）",
  "hourType": "理论",
  "notificationNo": "TZ2024001",
  "transferHours": 4,
  "actualHours": 60,
  "totalHours": 64,
  "totalCredit": 4.0,
  "actualHourRatio": 0.9375,
  "studentCount": 48,
  "className": "计科2101",
  "teacher": {
    "teacherId": 1
  },
  "semester": {
    "semesterId": 1
  }
}
```

**请求示例：**
```http
PUT /workload HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "wlId": 1,
  "courseNo": "CS101",
  "courseName": "数据结构（修订）",
  "hourType": "理论",
  "notificationNo": "TZ2024001",
  "transferHours": 4,
  "actualHours": 60,
  "totalHours": 64,
  "totalCredit": 4.0,
  "actualHourRatio": 0.9375,
  "studentCount": 48,
  "className": "计科2101",
  "teacher": {
    "teacherId": 1
  },
  "semester": {
    "semesterId": 1
  }
}
```

**成功响应示例：**
```json
{
  "code": "200",
  "msg": "成功",
  "data": null
}
```

**失败响应示例：**
```json
{
  "code": "500",
  "msg": "系统异常",
  "data": null
}
```

> ⚠️ **重要提示**：
> - 更新操作为**全量更新**，前端需要把**所有字段**都传过来（即使未修改的字段也要带上原值）。
> - `wlId` 必须指定，用于定位要更新的记录。
> - `teacher.teacherId` 和 `semester.semesterId` 必须传递有效值。

---

### 4.9 删除工作量

**接口描述**：根据工作量ID删除指定的工作量记录

| 项目 | 说明 |
|------|------|
| **请求方法** | `DELETE` |
| **请求路径** | `/workload/{id}` |
| **路径参数** | `id` — Integer，工作量ID |
| **响应数据** | 成功时 `data: null`，失败时返回错误信息 |

**请求示例：**
```http
DELETE /workload/1 HTTP/1.1
Host: localhost:8080
```

**成功响应示例：**
```json
{
  "code": "200",
  "msg": "成功",
  "data": null
}
```

**失败响应示例：**
```json
{
  "code": "500",
  "msg": "系统异常",
  "data": null
}
```

---

## 5. 接口汇总速查表

| 序号 | 方法 | URL | 用途 | 返回数据类型 |
|------|------|-----|------|--------------|
| 1 | GET | `/workload` | 查询全部工作量 | `Workload[]` |
| 2 | GET | `/workload/{id}` | 按ID查询单个工作量 | `Workload` |
| 3 | GET | `/workload/teacher/{teacherId}` | 按教师查询工作量 | `Workload[]` |
| 4 | GET | `/workload/semester/{semesterId}` | 按学期查询工作量 | `Workload[]` |
| 5 | GET | `/workload/details` | 查询全部（JOIN详情） | `Workload[]` |
| 6 | GET | `/workload/details/teacher/{teacherId}` | 按教师查询（JOIN详情） | `Workload[]` |
| 7 | POST | `/workload` | 新增工作量 | `null` |
| 8 | PUT | `/workload` | 更新工作量 | `null` |
| 9 | DELETE | `/workload/{id}` | 删除工作量 | `null` |

---

## 6. 前端开发注意事项

### 6.1 接口选择建议

- **查询接口**：接口 1 和 5、接口 3 和 6 返回结构完全相同，建议优先使用简洁路径的接口（1、2、3、4）。
- **新增/更新**：`teacher` 和 `semester` 对象内只需传 ID 字段即可。

### 6.2 新增操作流程

1. 先调用 `GET /teacher` 和 `GET /semester` 获取可用的教师和学期列表，供用户选择。
2. 用户填写表单后，构造 `Workload` 对象（`wlId` 传 `null`）。
3. 调用 `POST /workload` 提交数据。
4. 根据响应 `code` 判断操作是否成功。

### 6.3 更新操作流程

1. 先调用 `GET /workload/{id}` 获取当前记录的完整数据。
2. 将数据填充到编辑表单中，用户修改后，确保所有字段都有值（包括未修改的字段）。
3. 调用 `PUT /workload` 提交更新后的完整对象。
4. 根据响应 `code` 判断操作是否成功。

### 6.4 错误处理

- 所有接口的失败响应 `code` 均为 `"500"`，前端应根据 `code` 进行统一错误提示。
- 建议在 Axios 或其他 HTTP 客户端中配置统一的响应拦截器，处理 `code !== "200"` 的情况。

### 6.5 数据校验建议

前端在提交前应进行以下校验：

- **新增/更新时**：
  - `courseNo`、`courseName`、`hourType`、`actualHours`、`totalHours`、`totalCredit`、`studentCount`、`className` 不能为空。
  - `teacher.teacherId` 和 `semester.semesterId` 必须为有效整数。
  - `actualHours`、`totalHours`、`studentCount` 应为非负整数。
  - `totalCredit`、`actualHourRatio` 应为非负浮点数。

---

## 7. 附录：完整请求/响应示例

### 7.1 Vue + Axios 调用示例

```javascript
import axios from 'axios'

const baseURL = 'http://localhost:8080'

// 查询所有工作量
export const getAllWorkloads = () => {
  return axios.get(`${baseURL}/workload`)
}

// 根据ID查询
export const getWorkloadById = (id) => {
  return axios.get(`${baseURL}/workload/${id}`)
}

// 根据教师ID查询
export const getWorkloadByTeacher = (teacherId) => {
  return axios.get(`${baseURL}/workload/teacher/${teacherId}`)
}

// 新增工作量
export const createWorkload = (workload) => {
  return axios.post(`${baseURL}/workload`, workload)
}

// 更新工作量
export const updateWorkload = (workload) => {
  return axios.put(`${baseURL}/workload`, workload)
}

// 删除工作量
export const deleteWorkload = (id) => {
  return axios.delete(`${baseURL}/workload/${id}`)
}
```

### 7.2 React + Fetch 调用示例

```javascript
const baseURL = 'http://localhost:8080'

// 查询所有工作量
export const getAllWorkloads = async () => {
  const response = await fetch(`${baseURL}/workload`)
  return response.json()
}

// 新增工作量
export const createWorkload = async (workload) => {
  const response = await fetch(`${baseURL}/workload`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(workload),
  })
  return response.json()
}

// 更新工作量
export const updateWorkload = async (workload) => {
  const response = await fetch(`${baseURL}/workload`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(workload),
  })
  return response.json()
}

// 删除工作量
export const deleteWorkload = async (id) => {
  const response = await fetch(`${baseURL}/workload/${id}`, {
    method: 'DELETE',
  })
  return response.json()
}
```

---

**文档版本**：v1.0  
**最后更新**：2026-06-27  
**维护人员**：后端开发团队
