# Semester（学期管理）接口文档

> 注意：本文档中的更新示例仍是早期版本。当前实际接口已改为 `PUT /semester/{id}`，以 URL 路径中的 ID 定位资源；完整现状请以 `restful-api-reference.md` 和 `SemesterController` 为准。

## 1. 基础信息

| 项目 | 说明 |
|------|------|
| **接口前缀** | `/semester` |
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

## 3. 数据模型 — Semester

### 3.1 完整结构

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `semesterId` | Integer | 是（更新时） | 学期ID（主键，新增时可为 null，由数据库自增） |
| `semesterName` | String | 是 | 学期名称（如：2024-2025-1、2024-2025-2） |
| `year` | Integer | 是 | 年份（如：2024、2025） |
| `isCurrent` | Integer | 是 | 是否当前学期：`1` = 是，`0` = 否 |

### 3.2 字段说明

- **semesterName**：学期名称通常采用"学年-学期"的格式，例如：
  - `2024-2025-1` 表示 2024-2025 学年第一学期
  - `2024-2025-2` 表示 2024-2025 学年第二学期
  
- **year**：学年起始年份，例如 2024-2025 学年的 year 值为 `2024`

- **isCurrent**：标识当前正在使用的学期，系统中通常只有一个学期的 `isCurrent` 为 `1`

---

## 4. 接口列表

### 4.1 查询所有学期 / 按名称模糊查询

**接口描述**：获取系统中所有学期记录，或通过学期名称进行模糊查询

| 项目 | 说明 |
|------|------|
| **请求方法** | `GET` |
| **请求路径** | `/semester` |
| **请求参数** | `semesterName` — String，可选，学期名称（支持模糊匹配） |
| **响应数据** | `Semester[]` 数组 |

**请求示例 1：查询所有学期**
```http
GET /semester HTTP/1.1
Host: localhost:8080
```

**成功响应示例：**
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "semesterId": 1,
      "semesterName": "2023-2024-1",
      "year": 2023,
      "isCurrent": 0
    },
    {
      "semesterId": 2,
      "semesterName": "2023-2024-2",
      "year": 2023,
      "isCurrent": 0
    },
    {
      "semesterId": 3,
      "semesterName": "2024-2025-1",
      "year": 2024,
      "isCurrent": 1
    }
  ]
}
```

**请求示例 2：按名称模糊查询**
```http
GET /semester?semesterName=2024 HTTP/1.1
Host: localhost:8080
```

**成功响应示例：**
```json
{
  "code": "200",
  "msg": "成功",
  "data": [
    {
      "semesterId": 3,
      "semesterName": "2024-2025-1",
      "year": 2024,
      "isCurrent": 1
    },
    {
      "semesterId": 4,
      "semesterName": "2024-2025-2",
      "year": 2024,
      "isCurrent": 0
    }
  ]
}
```

**空结果响应示例**（未找到匹配的学期）：
```json
{
  "code": "200",
  "msg": "成功",
  "data": []
}
```

**使用场景**：
- 不传参数：初始化下拉选择框，展示所有学期
- 传入 `semesterName`：学期搜索功能，用户输入部分学期名称即可查找
- 快速定位特定学年的学期（如输入 "2024" 查找所有 2024 年相关的学期）
- 自动补全功能，根据用户输入实时显示匹配的学期

> 💡 **提示**：
> - 该接口使用前后模糊匹配，输入 "2024" 可以匹配到 "2023-2024-1"、"2024-2025-1"、"2024-2025-2" 等。
> - 这是一个智能接口：不传参数返回全部，传参数则进行搜索过滤。

---

### 4.2 根据ID查询单个学期

**接口描述**：根据学期ID查询单个学期的详细信息

| 项目 | 说明 |
|------|------|
| **请求方法** | `GET` |
| **请求路径** | `/semester/{id}` |
| **路径参数** | `id` — Integer，学期ID |
| **响应数据** | 单个 `Semester` 对象 |

**请求示例：**
```http
GET /semester/3 HTTP/1.1
Host: localhost:8080
```

**成功响应示例：**
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "semesterId": 3,
    "semesterName": "2024-2025-1",
    "year": 2024,
    "isCurrent": 1
  }
}
```

**失败响应示例**（学期不存在）：
```json
{
  "code": "200",
  "msg": "成功",
  "data": null
}
```

**使用场景**：
- 编辑学期前获取当前数据
- 查看某个学期的详细信息
- 根据学期ID加载相关数据

---

### 4.3 查询当前学期

**接口描述**：获取当前正在使用的学期（`isCurrent = 1` 的学期）

| 项目 | 说明 |
|------|------|
| **请求方法** | `GET` |
| **请求路径** | `/semester/current` |
| **请求参数** | 无 |
| **响应数据** | 单个 `Semester` 对象 |

**请求示例：**
```http
GET /semester/current HTTP/1.1
Host: localhost:8080
```

**成功响应示例：**
```json
{
  "code": "200",
  "msg": "成功",
  "data": {
    "semesterId": 3,
    "semesterName": "2024-2025-1",
    "year": 2024,
    "isCurrent": 1
  }
}
```

**失败响应示例**（未设置当前学期）：
```json
{
  "code": "200",
  "msg": "成功",
  "data": null
}
```

**使用场景**：
- 系统首页显示当前学期
- 新增工作量、教学计划等业务数据时，默认使用当前学期
- 查询当前学期的教学任务、考试安排等

> ⚠️ **注意**：如果系统中没有设置当前学期（所有记录的 `isCurrent` 都为 `0`），则返回 `data: null`。

---

### 4.4 新增学期

**接口描述**：创建一个新的学期记录

| 项目 | 说明 |
|------|------|
| **请求方法** | `POST` |
| **请求路径** | `/semester` |
| **Content-Type** | `application/json` |
| **请求体** | `Semester` 对象（不含 `semesterId` 或传 `null`） |
| **响应数据** | 成功时 `data: null`，失败时返回错误信息 |

**请求体示例：**
```json
{
  "semesterId": null,
  "semesterName": "2024-2025-2",
  "year": 2024,
  "isCurrent": 0
}
```

**请求示例：**
```http
POST /semester HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "semesterId": null,
  "semesterName": "2024-2025-2",
  "year": 2024,
  "isCurrent": 0
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
> - 新增时 `semesterId` 建议传 `null`，由数据库自增生成。
> - `semesterName`、`year`、`isCurrent` 均为必填项。
> - 如果要将新创建的学期设为当前学期，将 `isCurrent` 设为 `1`。
> - 建议在业务层保证同一时间只有一个学期的 `isCurrent` 为 `1`（可能需要先将其他学期的 `isCurrent` 更新为 `0`）。

---

### 4.5 更新学期

**接口描述**：更新已有的学期记录

| 项目 | 说明 |
|------|------|
| **请求方法** | `PUT` |
| **请求路径** | `/semester` |
| **Content-Type** | `application/json` |
| **请求体** | `Semester` 对象（必须包含 `semesterId`） |
| **响应数据** | 成功时 `data: null`，失败时返回错误信息 |

**请求体示例：**
```json
{
  "semesterId": 3,
  "semesterName": "2024-2025-1",
  "year": 2024,
  "isCurrent": 1
}
```

**请求示例：**
```http
PUT /semester HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "semesterId": 3,
  "semesterName": "2024-2025-1",
  "year": 2024,
  "isCurrent": 1
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
> - `semesterId` 必须指定，用于定位要更新的记录。
> - 切换当前学期时，通常需要两步操作：
>   1. 将原当前学期的 `isCurrent` 更新为 `0`
>   2. 将新学期的 `isCurrent` 更新为 `1`

---

### 4.6 删除学期

**接口描述**：根据学期ID删除指定的学期记录

| 项目 | 说明 |
|------|------|
| **请求方法** | `DELETE` |
| **请求路径** | `/semester/{id}` |
| **路径参数** | `id` — Integer，学期ID |
| **响应数据** | 成功时 `data: null`，失败时返回错误信息 |

**请求示例：**
```http
DELETE /semester/1 HTTP/1.1
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

> ⚠️ **注意事项**：
> - 删除前需检查该学期是否被其他业务数据引用（如工作量、教学计划、考试安排等）。
> - 如果被引用，建议禁止删除或先清理关联数据。
> - 不建议删除当前学期（`isCurrent = 1` 的记录）。

---

## 5. 接口汇总速查表

| 序号 | 方法 | URL | 用途 | 返回数据类型 |
|------|------|-----|------|--------------|
| 1 | GET | `/semester` | 查询所有学期 | `Semester[]` |
| 2 | GET | `/semester?semesterName=xxx` | 按名称模糊查询 | `Semester[]` |
| 3 | GET | `/semester/{id}` | 按ID查询单个学期 | `Semester` |
| 4 | GET | `/semester/current` | 查询当前学期 | `Semester` |
| 5 | POST | `/semester` | 新增学期 | `null` |
| 6 | PUT | `/semester` | 更新学期 | `null` |
| 7 | DELETE | `/semester/{id}` | 删除学期 | `null` |

---

## 6. 前端开发注意事项

### 6.1 常用业务流程

#### 6.1.1 初始化学期下拉框

```javascript
// 1. 调用接口获取所有学期
const response = await axios.get('/semester')

// 2. 渲染下拉选项
const options = response.data.data.map(semester => ({
  label: semester.semesterName,
  value: semester.semesterId
}))
```

#### 6.1.2 获取并使用当前学期

```javascript
// 1. 获取当前学期
const currentResponse = await axios.get('/semester/current')
const currentSemester = currentResponse.data.data

// 2. 在新增工作量时使用当前学期
if (currentSemester) {
  const workload = {
    // ... 其他字段
    semester: {
      semesterId: currentSemester.semesterId
    }
  }
  await axios.post('/workload', workload)
}
```

#### 6.1.3 切换当前学期

```javascript
// 场景：将学期ID为5的学期设为当前学期

// 步骤1：获取所有学期
const allResponse = await axios.get('/semester')
const semesters = allResponse.data.data

// 步骤2：将原当前学期设为非当前
const oldCurrent = semesters.find(s => s.isCurrent === 1)
if (oldCurrent) {
  oldCurrent.isCurrent = 0
  await axios.put('/semester', oldCurrent)
}

// 步骤3：将新学期设为当前
const newCurrent = semesters.find(s => s.semesterId === 5)
newCurrent.isCurrent = 1
await axios.put('/semester', newCurrent)
```

> 💡 **优化建议**：上述切换逻辑最好在后端实现，前端只需传递新学期ID，后端自动处理事务。

### 6.2 数据校验建议

前端在提交前应进行以下校验：

- **新增/更新时**：
  - `semesterName` 不能为空，且应符合命名规范（如：YYYY-YYYY-X 格式）
  - `year` 不能为空，应为合理的年份范围（如：2020-2030）
  - `isCurrent` 只能为 `0` 或 `1`

- **正则表达式校验学期名称**：
  ```javascript
  const semesterNamePattern = /^\d{4}-\d{4}-[12]$/
  if (!semesterNamePattern.test(semesterName)) {
    // 提示格式错误
  }
  ```

### 6.3 错误处理

- 所有接口的失败响应 `code` 均为 `"500"`，前端应根据 `code` 进行统一错误提示。
- 建议在 Axios 或其他 HTTP 客户端中配置统一的响应拦截器，处理 `code !== "200"` 的情况。

### 6.4 缓存策略

- 学期数据变化频率较低，可以考虑在前端进行缓存（如 localStorage、Vuex、Pinia 等）。
- 在以下情况需要刷新缓存：
  - 新增学期后
  - 更新学期后
  - 删除学期后
  - 切换当前学期后

---

## 7. 附录：完整请求/响应示例

### 7.1 Vue + Axios 调用示例

```javascript
import axios from 'axios'

const baseURL = 'http://localhost:8080'

// 查询所有学期
export const getAllSemesters = () => {
  return axios.get(`${baseURL}/semester`)
}

// 按ID查询单个学期
export const getSemesterById = (id) => {
  return axios.get(`${baseURL}/semester/${id}`)
}

// 按名称模糊查询
export const searchSemesterByName = (semesterName) => {
  return axios.get(`${baseURL}/semester`, {
    params: { semesterName }
  })
}

// 查询当前学期
export const getCurrentSemester = () => {
  return axios.get(`${baseURL}/semester/current`)
}

// 新增学期
export const createSemester = (semester) => {
  return axios.post(`${baseURL}/semester`, semester)
}

// 更新学期
export const updateSemester = (semester) => {
  return axios.put(`${baseURL}/semester`, semester)
}

// 删除学期
export const deleteSemester = (id) => {
  return axios.delete(`${baseURL}/semester/${id}`)
}

// 切换当前学期（封装好的业务方法）
export const switchCurrentSemester = async (newSemesterId) => {
  // 1. 获取所有学期
  const allResponse = await getAllSemesters()
  const semesters = allResponse.data.data
  
  // 2. 将原当前学期设为非当前
  const oldCurrent = semesters.find(s => s.isCurrent === 1)
  if (oldCurrent) {
    oldCurrent.isCurrent = 0
    await updateSemester(oldCurrent)
  }
  
  // 3. 将新学期设为当前
  const newCurrent = semesters.find(s => s.semesterId === newSemesterId)
  if (newCurrent) {
    newCurrent.isCurrent = 1
    await updateSemester(newCurrent)
  }
}
```

### 7.2 React + Fetch 调用示例

```javascript
const baseURL = 'http://localhost:8080'

// 查询所有学期
export const getAllSemesters = async () => {
  const response = await fetch(`${baseURL}/semester`)
  return response.json()
}

// 按ID查询单个学期
export const getSemesterById = async (id) => {
  const response = await fetch(`${baseURL}/semester/${id}`)
  return response.json()
}

// 按名称模糊查询
export const searchSemesterByName = async (semesterName) => {
  const response = await fetch(`${baseURL}/semester?semesterName=${encodeURIComponent(semesterName)}`)
  return response.json()
}

// 查询当前学期
export const getCurrentSemester = async () => {
  const response = await fetch(`${baseURL}/semester/current`)
  return response.json()
}

// 新增学期
export const createSemester = async (semester) => {
  const response = await fetch(`${baseURL}/semester`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(semester),
  })
  return response.json()
}

// 更新学期
export const updateSemester = async (semester) => {
  const response = await fetch(`${baseURL}/semester`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(semester),
  })
  return response.json()
}

// 删除学期
export const deleteSemester = async (id) => {
  const response = await fetch(`${baseURL}/semester/${id}`, {
    method: 'DELETE',
  })
  return response.json()
}
```

### 7.3 Element UI 下拉框示例（Vue）

```vue
<template>
  <el-select v-model="selectedSemesterId" placeholder="请选择学期">
    <el-option
      v-for="item in semesterList"
      :key="item.semesterId"
      :label="item.semesterName"
      :value="item.semesterId"
    />
  </el-select>
</template>

<script>
import { getAllSemesters, getCurrentSemester } from '@/api/semester'

export default {
  data() {
    return {
      semesterList: [],
      selectedSemesterId: null
    }
  },
  created() {
    this.loadSemesters()
  },
  methods: {
    async loadSemesters() {
      // 加载所有学期
      const response = await getAllSemesters()
      this.semesterList = response.data.data
      
      // 默认选中当前学期
      const currentResponse = await getCurrentSemester()
      if (currentResponse.data.data) {
        this.selectedSemesterId = currentResponse.data.data.semesterId
      }
    }
  }
}
</script>
```

### 7.4 Ant Design 下拉框示例（React）

```jsx
import React, { useState, useEffect } from 'react'
import { Select } from 'antd'
import { getAllSemesters, getCurrentSemester } from '@/api/semester'

const { Option } = Select

const SemesterSelect = ({ onChange }) => {
  const [semesterList, setSemesterList] = useState([])
  const [selectedSemesterId, setSelectedSemesterId] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadSemesters()
  }, [])

  const loadSemesters = async () => {
    try {
      // 加载所有学期
      const response = await getAllSemesters()
      setSemesterList(response.data.data)
      
      // 默认选中当前学期
      const currentResponse = await getCurrentSemester()
      if (currentResponse.data.data) {
        setSelectedSemesterId(currentResponse.data.data.semesterId)
      }
    } finally {
      setLoading(false)
    }
  }

  const handleChange = (value) => {
    setSelectedSemesterId(value)
    onChange && onChange(value)
  }

  return (
    <Select
      value={selectedSemesterId}
      onChange={handleChange}
      placeholder="请选择学期"
      loading={loading}
      style={{ width: 200 }}
    >
      {semesterList.map(semester => (
        <Option key={semester.semesterId} value={semester.semesterId}>
          {semester.semesterName}
        </Option>
      ))}
    </Select>
  )
}

export default SemesterSelect
```

### 7.5 学期搜索功能示例（Vue + Element UI）

```vue
<template>
  <div>
    <!-- 搜索框 -->
    <el-input
      v-model="searchKeyword"
      placeholder="请输入学期名称进行搜索"
      clearable
      @input="handleSearch"
      style="width: 300px; margin-bottom: 20px"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
    </el-input>

    <!-- 搜索结果列表 -->
    <el-table :data="searchResults" border stripe>
      <el-table-column prop="semesterName" label="学期名称" width="200" />
      <el-table-column prop="year" label="年份" width="100" />
      <el-table-column label="是否当前学期" width="120">
        <template #default="{ row }">
          <el-tag :type="row.isCurrent === 1 ? 'success' : 'info'">
            {{ row.isCurrent === 1 ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="handleSelect(row)">
            选择
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { searchSemesterByName } from '@/api/semester'

export default {
  data() {
    return {
      searchKeyword: '',
      searchResults: [],
      searchTimer: null
    }
  },
  methods: {
    // 防抖搜索
    handleSearch() {
      if (this.searchTimer) {
        clearTimeout(this.searchTimer)
      }
      
      if (!this.searchKeyword.trim()) {
        this.searchResults = []
        return
      }
      
      this.searchTimer = setTimeout(async () => {
        try {
          const response = await searchSemesterByName(this.searchKeyword)
          this.searchResults = response.data.data
        } catch (error) {
          this.$message.error('搜索失败')
        }
      }, 300)
    },
    
    // 选择学期
    handleSelect(semester) {
      this.$emit('select', semester)
      this.$message.success(`已选择学期：${semester.semesterName}`)
    }
  }
}
</script>
```

---

## 8. 常见问题 FAQ

### Q1: 如何保证只有一个当前学期？

**答**：有两种方案：

1. **后端控制**（推荐）：在更新学期的 `isCurrent` 字段时，后端先将所有学期的 `isCurrent` 设为 `0`，再将目标学期设为 `1`，使用事务保证原子性。

2. **前端控制**：前端在切换当前学期时，先调用更新接口将原当前学期设为 `0`，再调用更新接口将新学期设为 `1`。但这种方式存在并发问题，不推荐。

### Q2: 学期名称有什么命名规范？

**答**：建议使用 `YYYY-YYYY-X` 格式，其中：
- 第一个 `YYYY` 为学年起始年份
- 第二个 `YYYY` 为学年结束年份
- `X` 为学期编号（1 表示第一学期，2 表示第二学期）

例如：`2024-2025-1`、`2024-2025-2`

### Q3: 删除学期时需要注意什么？

**答**：
- 检查该学期是否被工作量、教学计划、考试安排等业务数据引用
- 如果被引用，应禁止删除或提示用户先清理关联数据
- 不建议删除当前学期（`isCurrent = 1` 的记录）
- 建议在删除前弹出确认对话框

### Q4: 为什么要提供 `/semester/current` 接口？

**答**：
- 频繁查询当前学期的场景很多（如新增工作量时默认使用当前学期）
- 避免前端每次都遍历所有学期来查找当前学期
- 提高查询效率，简化前端逻辑

### Q5: 模糊查询接口如何使用？

**答**：
- 接口地址：`GET /semester?semesterName=xxx`
- 支持前后模糊匹配，输入部分内容即可匹配
- 例如：
  - 输入 `"2024"` 可以匹配到 `"2023-2024-1"`、`"2024-2025-1"`、`"2024-2025-2"`
  - 输入 `"-1"` 可以匹配到所有第一学期
  - 输入 `"2024-2025"` 可以匹配到该学年的两个学期
- 返回结果为数组，可能包含 0 个或多个匹配的学期
- 建议在前端实现防抖搜索，避免频繁请求
- 不传参数时返回所有学期

---

**文档版本**：v1.0  
**最后更新**：2026-06-27  
**维护人员**：后端开发团队
