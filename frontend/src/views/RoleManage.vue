<template>
  <div class="page-container">
    <div class="page-header">
      <h2>角色管理</h2>
    </div>

    <el-card class="search-card" shadow="hover">
      <template #header>
        <span style="font-weight: bold;">角色查询</span>
      </template>
      <el-form :inline="true" class="search-form">
        <el-form-item label="角色名称">
          <el-input v-model="searchForm.roleName" placeholder="输入角色名称搜索" clearable style="width: 200px;" @input="onRoleSearchInput" />
        </el-form-item>
        <el-form-item label="搜索模式">
          <el-select v-model="searchForm.searchMode" placeholder="选择模式" style="width: 120px;">
            <el-option label="精确查询" value="exact" />
            <el-option label="模糊查询" value="fuzzy" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchRoles">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="success" @click="openAddDialog">新增角色</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="hover" style="margin-top: 20px;">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="roleId" label="角色ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleDesc" label="角色描述" min-width="180" show-overflow-tooltip />
        <el-table-column label="菜单权限" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleMenuCount(row.roleId) > 0 ? 'success' : 'info'">
              {{ getRoleMenuCount(row.roleId) }} 项
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="功能权限" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleActionCount(row.roleId) > 0 ? 'warning' : 'info'">
              {{ getRoleActionCount(row.roleId) }} 项
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="deleteRole(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination style="margin-top: 20px; text-align: right;" :current-page="pagination.pageNum" :page-size="pagination.pageSize" :total="pagination.total" layout="total, prev, pager, next" @current-change="handlePageChange" @size-change="handleSizeChange" />
      <el-empty v-if="!loading && tableData.length === 0" description="暂无角色数据" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="800px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="角色名称" required>
          <el-input v-model="formData.roleName" placeholder="请输入角色名称" @input="onRoleFormInput" />
        </el-form-item>
        <el-form-item label="角色描述">
          <el-input v-model="formData.roleDesc" type="textarea" rows="2" placeholder="请输入角色描述（选填）" />
        </el-form-item>
        <el-divider content-position="left">页面访问权限（菜单）</el-divider>
        <el-form-item label="菜单权限">
          <el-checkbox-group v-model="formData.menuIds">
            <el-checkbox
              v-for="menu in allMenus"
              :key="menu.menuId"
              :label="menu.menuId"
              style="margin-right: 20px; margin-bottom: 5px;"
            >
              {{ menu.menuName }}
            </el-checkbox>
          </el-checkbox-group>
          <div v-if="allMenus.length === 0" style="color: #999;">暂无可用菜单</div>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">勾选后该角色用户可访问对应页面</div>
        </el-form-item>
        <el-divider content-position="left">操作权限（按钮）</el-divider>
        <el-form-item label="功能权限">
          <el-checkbox-group v-model="formData.actionIds">
            <div v-for="menu in menuGroupedActions" :key="menu.menuId" style="margin-bottom: 10px;">
              <div style="font-weight: bold; margin-bottom: 5px; color: #606266;">{{ menu.menuName }}</div>
              <el-checkbox
                v-for="action in menu.actions"
                :key="action.actionId"
                :label="action.actionId"
                style="margin-right: 15px; margin-bottom: 5px;"
              >
                {{ action.actionName }}
              </el-checkbox>
            </div>
          </el-checkbox-group>
          <div v-if="menuGroupedActions.length === 0" style="color: #999;">暂无可用权限</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRole">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { roleApi, menuActionApi, roleMenuActionApi, menuApi, roleMenuApi } from '../api/index.js'

const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)

const searchForm = reactive({
  roleName: '',
  searchMode: 'fuzzy'
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])

// 所有菜单
const allMenus = ref([])
// 所有操作权限
const allMenuActions = ref([])
// 角色已分配的菜单映射 { roleId: [menuId, ...] }
const roleMenuMap = ref({})
// 角色已分配的权限映射 { roleId: [actionId, ...] }
const roleActionMap = ref({})

const formData = reactive({
  roleId: null,
  roleName: '',
  roleDesc: '',
  menuIds: [],
  actionIds: []
})

// 按菜单分组的操作权限
const menuGroupedActions = computed(() => {
  const groups = {}
  allMenuActions.value.forEach(action => {
    const menuId = action.menuId
    if (!groups[menuId]) {
      groups[menuId] = { menuId, menuName: getMenuName(menuId), actions: [] }
    }
    groups[menuId].actions.push(action)
  })
  return Object.values(groups)
})

// 获取菜单名称（从 allMenus 动态查找，不再硬编码）
const getMenuName = (menuId) => {
  const menu = allMenus.value.find(m => m.menuId === menuId)
  return menu?.menuName || `菜单${menuId}`
}

// 获取角色的菜单数量
const getRoleMenuCount = (roleId) => {
  return roleMenuMap.value[roleId]?.length || 0
}

// 获取角色的权限数量
const getRoleActionCount = (roleId) => {
  return roleActionMap.value[roleId]?.length || 0
}

// 获取角色的权限名称列表
const getRoleActionNames = (roleId) => {
  const actionIds = roleActionMap.value[roleId] || []
  return actionIds.map(id => {
    const action = allMenuActions.value.find(a => a.actionId === id)
    return action?.actionName || `权限${id}`
  })
}

const fetchRoles = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      roleName: searchForm.roleName,
      searchMode: searchForm.searchMode
    }
    const res = await roleApi.getPage(params)
    if (res.code === '200' && res.data) {
      tableData.value = res.data.records || res.data.list || []
      pagination.total = parseInt(res.data.total) || 0
      
      // 加载每个角色的菜单和权限映射
      for (const role of tableData.value) {
        // 加载菜单权限
        if (!roleMenuMap.value[role.roleId]) {
          try {
            const menuRes = await roleMenuApi.getMenuIdsByRoleId(role.roleId)
            if (menuRes.code === '200' && menuRes.data) {
              roleMenuMap.value[role.roleId] = menuRes.data
            } else {
              roleMenuMap.value[role.roleId] = []
            }
          } catch (err) {
            roleMenuMap.value[role.roleId] = []
          }
        }
        // 加载操作权限
        if (!roleActionMap.value[role.roleId]) {
          try {
            const permRes = await roleMenuActionApi.getByRoleId(role.roleId)
            if (permRes.code === '200' && permRes.data) {
              roleActionMap.value[role.roleId] = permRes.data.map(item => item.actionId)
            } else {
              roleActionMap.value[role.roleId] = []
            }
          } catch (err) {
            roleActionMap.value[role.roleId] = []
          }
        }
      }
    } else {
      tableData.value = []
      pagination.total = 0
    }
  } catch (error) {
    ElMessage.error('加载角色数据失败')
    console.error('加载角色数据失败:', error)
    tableData.value = []
  } finally {
    loading.value = false
  }
}

// 加载所有菜单和操作权限
const loadMenuActions = async () => {
  try {
    // 加载所有菜单
    const menuRes = await menuApi.getAll()
    if (menuRes.code === '200' && menuRes.data) {
      // 只显示子菜单（有 menu_url 的）用于分配，父菜单不参与分配
      allMenus.value = menuRes.data.filter(m => m.menuUrl)
    }
    // 加载所有操作权限
    const actionRes = await menuActionApi.getAll()
    if (actionRes.code === '200' && actionRes.data) {
      allMenuActions.value = actionRes.data
    }
  } catch (error) {
    console.error('加载权限数据失败:', error)
  }
}

const resetSearch = () => {
  searchForm.roleName = ''
  searchForm.searchMode = 'fuzzy'
  pagination.pageNum = 1
  fetchRoles()
}

const onRoleSearchInput = (val) => {
  if (val && val.length > 10) {
    ElMessage.warning('角色名称不能超过10个字符')
  }
}

const onRoleFormInput = (val) => {
  if (val && val.length > 10) {
    ElMessage.warning('角色名称不能超过10个字符')
  }
}

const handlePageChange = (page) => {
  pagination.pageNum = page
  fetchRoles()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchRoles()
}

const openAddDialog = () => {
  isEdit.value = false
  formData.roleId = null
  formData.roleName = ''
  formData.roleDesc = ''
  formData.menuIds = []
  formData.actionIds = []
  dialogVisible.value = true
}

const openEditDialog = async (row) => {
  isEdit.value = true
  formData.roleId = row.roleId
  formData.roleName = row.roleName
  formData.roleDesc = row.roleDesc || ''
  // 加载角色已分配的菜单权限
  try {
    const menuRes = await roleMenuApi.getMenuIdsByRoleId(row.roleId)
    if (menuRes.code === '200' && menuRes.data) {
      formData.menuIds = menuRes.data
    } else {
      formData.menuIds = []
    }
  } catch (err) {
    console.error('加载角色菜单失败:', err)
    formData.menuIds = []
  }
  // 加载角色已分配的操作权限
  try {
    const permRes = await roleMenuActionApi.getByRoleId(row.roleId)
    if (permRes.code === '200' && permRes.data) {
      formData.actionIds = permRes.data.map(item => item.actionId)
    } else {
      formData.actionIds = []
    }
  } catch (err) {
    console.error('加载角色权限失败:', err)
    formData.actionIds = []
  }
  dialogVisible.value = true
}

const saveRole = async () => {
  if (!formData.roleName.trim()) {
    ElMessage.warning('请输入角色名称')
    return
  }

  // 校验角色名称字数（不得超过10个字符）
  if (formData.roleName.length > 10) {
    ElMessage.warning('角色名称不能超过10个字符')
    return
  }

  // 检查角色名称是否已存在
  const checkRes = await roleApi.getAll()
  if (checkRes.code === '200' && checkRes.data) {
    const exists = isEdit.value
      ? checkRes.data.some(r => r.roleName === formData.roleName && r.roleId !== formData.roleId)
      : checkRes.data.some(r => r.roleName === formData.roleName)
    if (exists) {
      ElMessage.warning('该角色名称已存在，请勿重复添加')
      return
    }
  }

  try {
    let res
    if (isEdit.value) {
      res = await roleApi.update(formData)
    } else {
      res = await roleApi.create(formData)
    }
    
    if (res.code === '200') {
      // 获取角色ID（编辑时使用原有ID，新增时需要获取）
      let roleId = formData.roleId
      if (!isEdit.value) {
        // 新增后重新获取角色列表来找到新创建的角色ID
        const listRes = await roleApi.getAll()
        if (listRes.code === '200' && listRes.data) {
          const newRole = listRes.data.find(r => r.roleName === formData.roleName)
          if (newRole) {
            roleId = newRole.roleId
          }
        }
      }
      
      // 保存菜单权限（页面访问权限）
      if (roleId) {
        // 先删除旧菜单权限
        await roleMenuApi.deleteByRoleId(roleId)
        // 批量添加新菜单权限
        if (formData.menuIds.length > 0) {
          const menuBatchList = formData.menuIds.map(menuId => ({
            roleId: roleId,
            menuId: menuId
          }))
          await roleMenuApi.batchInsert(menuBatchList)
        }
        // 更新本地缓存
        roleMenuMap.value[roleId] = [...formData.menuIds]
      }
      
      // 保存操作权限（按钮权限）
      if (roleId) {
        // 先删除旧操作权限
        await roleMenuActionApi.deleteByRoleId(roleId)
        // 批量添加新操作权限
        if (formData.actionIds.length > 0) {
          const actionBatchList = formData.actionIds.map(actionId => ({
            roleId: roleId,
            actionId: actionId
          }))
          await roleMenuActionApi.batchInsert(actionBatchList)
        }
        // 更新本地缓存
        roleActionMap.value[roleId] = [...formData.actionIds]
      }
      
      ElMessage.success(isEdit.value ? '角色更新成功' : '角色创建成功')
      dialogVisible.value = false
      fetchRoles()
    } else {
      ElMessage.error(isEdit.value ? '角色更新失败' : '角色创建失败')
    }
  } catch (error) {
    ElMessage.error(isEdit.value ? '角色更新失败' : '角色创建失败')
    console.error('保存角色失败:', error)
  }
}

const deleteRole = (row) => {
  ElMessageBox.confirm('确定删除该角色吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      const res = await roleApi.delete(row.roleId)
      if (res.code === '200') {
        ElMessage.success('角色删除成功')
        fetchRoles()
      } else {
        ElMessage.error('角色删除失败')
      }
    } catch (error) {
      ElMessage.error('角色删除失败')
      console.error('删除角色失败:', error)
    }
  })
}

onMounted(async () => {
  await loadMenuActions()
  fetchRoles()
})
</script>

<style scoped>
.page-container { padding: 20px; background-color: #ffffff; min-height: calc(100vh - 60px); }
.page-header { margin-bottom: 20px; }
.page-header h2 { color: #303133; font-weight: 600; }
.search-card { background-color: #ffffff; }
.search-form { display: flex; flex-wrap: wrap; align-items: center; gap: 10px; }
.search-form .el-form-item { margin-bottom: 10px; }
.table-card { background-color: #ffffff; }
:deep(.el-table .cell) { padding: 8px 0; }
:deep(.el-card__header) { border-bottom: 1px solid #ebeef5; padding: 15px 20px; font-weight: 500; }
</style>
