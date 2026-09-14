# Semester 模块修改说明

## 修改时间
2026-06-27

## 修改内容

### 1. 接口变更

**原接口：**
```
GET /semester/{id}  - 根据ID查询单个学期
```

**新接口：**
```
GET /semester/search?semesterName=xxx  - 按学期名称模糊查询
```

### 2. 代码修改清单

#### 2.1 Controller 层
**文件：** `SemesterController.java`

**修改前：**
```java
@GetMapping("/{id}")
public Result findById(@PathVariable Integer id) {
    return Result.success(semesterService.findById(id));
}
```

**修改后：**
```java
@GetMapping("/search")
public Result findByName(@RequestParam String semesterName) {
    return Result.success(semesterService.findByName(semesterName));
}
```

---

#### 2.2 Service 接口层
**文件：** `SemesterService.java`

**修改前：**
```java
Semester findById(Integer semesterId);
```

**修改后：**
```java
List<Semester> findByName(String semesterName);
```

---

#### 2.3 Service 实现层
**文件：** `SemesterServiceImpl.java`

**修改前：**
```java
@Override
public Semester findById(Integer semesterId) {
    return semesterMapper.selectById(semesterId);
}
```

**修改后：**
```java
@Override
public List<Semester> findByName(String semesterName) {
    return semesterMapper.selectByName(semesterName);
}
```

---

#### 2.4 Mapper 接口层
**文件：** `SemesterMapper.java`

**修改前：**
```java
Semester selectById(Integer semesterId);
```

**修改后：**
```java
List<Semester> selectByName(String semesterName);
```

---

#### 2.5 Mapper XML
**文件：** `SemesterMapper.xml`

**修改前：**
```xml
<select id="selectById" parameterType="Integer" resultType="com.sdjzuxg.collegemanagesystem.entity.Semester">
    SELECT * FROM SEMESTER WHERE semester_id = #{semesterId}
</select>
```

**修改后：**
```xml
<select id="selectByName" parameterType="String" resultType="com.sdjzuxg.collegemanagesystem.entity.Semester">
    SELECT * FROM SEMESTER WHERE semester_name LIKE CONCAT('%', #{semesterName}, '%')
</select>
```

---

## 3. 功能说明

### 3.1 模糊查询特性

- **前后模糊匹配**：使用 `LIKE CONCAT('%', #{semesterName}, '%')` 实现
- **返回结果**：数组类型（可能包含 0、1 或多个匹配的学期）
- **参数名**：`semesterName`（String 类型）

### 3.2 使用示例

#### 示例 1：查询包含 "2024" 的所有学期
```http
GET /semester/search?semesterName=2024
```

**可能返回：**
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

#### 示例 2：查询所有第一学期
```http
GET /semester/search?semesterName=-1
```

#### 示例 3：精确查询某个学期
```http
GET /semester/search?semesterName=2024-2025-1
```

---

## 4. 前端适配建议

### 4.1 API 调用方式变更

**修改前：**
```javascript
// 根据ID查询
const response = await axios.get(`/semester/${id}`)
const semester = response.data.data  // 单个对象
```

**修改后：**
```javascript
// 按名称模糊查询
const response = await axios.get('/semester/search', {
  params: { semesterName: keyword }
})
const semesters = response.data.data  // 数组
```

### 4.2 搜索功能实现建议

1. **防抖处理**：避免用户输入时频繁请求
   ```javascript
   let searchTimer = null
   
   const handleSearch = (keyword) => {
     if (searchTimer) clearTimeout(searchTimer)
     
     searchTimer = setTimeout(async () => {
       const response = await searchSemesterByName(keyword)
       // 处理结果
     }, 300)
   }
   ```

2. **空值处理**：当搜索框为空时，清空搜索结果
   ```javascript
   if (!keyword.trim()) {
     searchResults.value = []
     return
   }
   ```

3. **结果展示**：由于返回的是数组，需要使用列表或表格展示

---

## 5. 影响范围

### 5.1 不受影响的接口
- ✅ `GET /semester` - 查询所有学期
- ✅ `GET /semester/current` - 查询当前学期
- ✅ `POST /semester` - 新增学期
- ✅ `PUT /semester/{id}` - 按路径 ID 更新学期
- ✅ `DELETE /semester/{id}` - 删除学期

### 5.2 需要调整的前端代码
- ❌ 原来调用 `GET /semester/{id}` 的代码需要改为 `GET /semester/search?semesterName=xxx`
- ⚠️ 注意返回值从**单个对象**变为**数组**，需要相应调整数据处理逻辑

---

## 6. 测试建议

### 6.1 功能测试

1. **模糊匹配测试**
   - 输入完整学期名称，验证能否精确匹配
   - 输入部分学期名称，验证能否模糊匹配
   - 输入不存在的学期名称，验证返回空数组

2. **边界测试**
   - 输入空字符串
   - 输入特殊字符
   - 输入超长字符串

3. **性能测试**
   - 大量数据下的搜索响应时间
   - 频繁搜索时的服务器负载

### 6.2 回归测试

确保其他接口功能正常：
- 查询所有学期
- 查询当前学期
- 新增、更新、删除学期

---

## 7. 注意事项

1. **返回值类型变化**：从单个 `Semester` 对象变为 `Semester[]` 数组
2. **参数传递方式**：从路径参数（Path Variable）变为查询参数（Query Parameter）
3. **SQL 注入防护**：MyBatis 的 `#{}` 占位符已自动处理，无需额外担心
4. **大小写敏感**：MySQL 默认情况下 LIKE 查询不区分大小写

---

## 8. 后续优化建议

1. **添加分页支持**：如果学期数据量很大，可以考虑添加分页参数
2. **多字段搜索**：可以扩展为同时搜索学期名称和年份
3. **缓存优化**：对于频繁搜索的关键词，可以考虑添加缓存
4. **搜索历史**：记录用户的搜索历史，提供快捷搜索

---

**修改人员**：AI Assistant  
**审核人员**：待审核  
**部署时间**：待定
