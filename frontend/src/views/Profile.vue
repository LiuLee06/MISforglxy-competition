<template>
  <div class="profile-page">
    <el-tabs v-model="activeTab" type="card">
      <!-- 个人信息 -->
      <el-tab-pane label="个人信息" name="info">
        <div class="info-card">
          <div class="avatar-section">
            <el-avatar :size="120" class="profile-avatar">
              <User />
            </el-avatar>
            <div class="user-name">{{ userInfo.name }}</div>
          </div>
          
          <el-form :model="userInfo" label-width="120px" class="info-form">
            <el-form-item label="用户名（手机号）">
              <el-input v-model="userInfo.phone" :disabled="userType !== 'teacher'" placeholder="教师可修改手机号" />
            </el-form-item>
            <el-form-item label="工号" v-if="userType === 'teacher'">
              <el-input v-model="userInfo.staffNo" />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="userInfo.name" :disabled="userType === 'admin'" />
            </el-form-item>
            <el-form-item label="部门">
              <el-input v-model="userInfo.dept" disabled />
            </el-form-item>
            <el-form-item label="职称" v-if="userType === 'teacher'">
              <el-input v-model="userInfo.professionalTitle" />
            </el-form-item>
            <el-form-item label="职务" v-if="userType === 'teacher'">
              <el-input v-model="userInfo.position" />
            </el-form-item>
            <el-form-item label="出生日期" v-if="userType === 'teacher'">
              <el-input v-model="userInfo.birth" placeholder="格式如 1990-01" />
            </el-form-item>
            <el-form-item label="政治面貌" v-if="userType === 'teacher'">
              <el-input v-model="userInfo.political" />
            </el-form-item>
            <el-form-item label="办公电话" v-if="userType === 'teacher'">
              <el-input v-model="userInfo.officePhone" />
            </el-form-item>
            <el-form-item label="学历" v-if="userType === 'teacher'">
              <el-input v-model="userInfo.education" />
            </el-form-item>
            <el-form-item label="学位" v-if="userType === 'teacher'">
              <el-input v-model="userInfo.degree" />
            </el-form-item>
            <el-form-item label="是否专任" v-if="userType === 'teacher'">
              <el-switch
                v-model="userInfo.isFullTime"
                active-text="专任"
                inactive-text="非专任"
              />
            </el-form-item>
            <el-form-item label="是否退休" v-if="userType === 'teacher'">
              <el-switch
                v-model="userInfo.isRetired"
                active-text="已退休"
                inactive-text="未退休"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveUserInfo">保存修改</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <!-- 修改密码 -->
      <el-tab-pane label="修改密码" name="password">
        <div class="password-card">
          <el-form :model="passwordForm" label-width="120px" :rules="passwordRules" ref="passwordFormRef">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input type="password" v-model="passwordForm.oldPassword" placeholder="请输入旧密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input type="password" v-model="passwordForm.newPassword" placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input type="password" v-model="passwordForm.confirmPassword" placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <!-- 重置教师密码（仅管理员可见） -->
      <el-tab-pane v-if="userType === 'admin'" label="重置教师密码" name="reset">
        <div class="password-card">
          <el-alert
            title="说明：重置后教师密码将变为默认密码 123456，请通知教师及时登录并修改密码。"
            type="warning"
            :closable="false"
            show-icon
            style="margin-bottom: 20px;"
          />
          <el-form label-width="120px">
            <el-form-item label="选择教师">
              <el-select
                v-model="resetTeacherId"
                filterable
                clearable
                placeholder="请输入姓名/手机号搜索教师"
                style="width: 100%;"
                @change="onResetTeacherChange"
              >
                <el-option
                  v-for="t in teacherOptions"
                  :key="t.teacherId"
                  :label="`${t.name}（${t.phone || '无手机号'}）`"
                  :value="t.teacherId"
                />
              </el-select>
            </el-form-item>
            <el-form-item v-if="resetTeacherInfo" label="教师信息">
              <div class="reset-teacher-info">
                <span>姓名：{{ resetTeacherInfo.name }}</span>
                <span>部门：{{ resetTeacherInfo.dept || '-' }}</span>
                <span>手机号：{{ resetTeacherInfo.phone || '-' }}</span>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button
                type="danger"
                @click="handleResetPassword"
                :disabled="!resetTeacherId"
                :loading="resetLoading"
              >
                重置密码
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { User } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api/index'

const activeTab = ref('info')
const passwordFormRef = ref(null)

// 重置教师密码相关状态
const teacherOptions = ref([])
const resetTeacherId = ref(null)
const resetTeacherInfo = ref(null)
const resetLoading = ref(false)

// 从localStorage获取用户信息
const getUserInfoFromStorage = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  return userInfoStr ? JSON.parse(userInfoStr) : {}
}

// 当前用户类型（admin / teacher）
const userType = ref('')

// 用户信息
const userInfo = reactive({
  phone: '',
  staffNo: '',
  name: '',
  dept: '',

  userId: '',
  professionalTitle: '',
  position: '',
  birth: '',
  political: '',
  officePhone: '',
  education: '',
  degree: '',
  isFullTime: false,
  isRetired: false
})

// 修改密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码校验规则
const passwordRules = reactive({
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        // Windows 文件夹命名不允许的特殊字符: \ / : * ? " < > |
        if (/[\\\/:*?"<>|]/.test(value || '')) {
          callback(new Error('密码不能包含特殊字符: \\ / : * ? " < > |'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

// 加载用户信息
const loadUserInfo = async () => {
  const storageInfo = getUserInfoFromStorage()
  userType.value = storageInfo.userType || ''
  userInfo.phone = storageInfo.username || ''
  userInfo.userId = storageInfo.userId || ''
  
  // 判断用户类型
  if (storageInfo.userType === 'admin') {
    // 管理员用户：用户名和姓名一样，部门固定为"超级管理员"
    userInfo.name = storageInfo.username || ''
    userInfo.dept = '超级管理员'
    userInfo.staffNo = ''
  } else if (storageInfo.userType === 'teacher') {
    // 教师用户：从后端获取完整信息
    userInfo.name = storageInfo.name || storageInfo.userName || storageInfo.user_name || ''
    userInfo.dept = storageInfo.dept || storageInfo.deptName || storageInfo.department || ''
    userInfo.phone = storageInfo.username || storageInfo.phone || storageInfo.userName || ''
    userInfo.staffNo = storageInfo.staffNo || storageInfo.teacherId || ''
    userInfo.professionalTitle = storageInfo.professionalTitle || storageInfo.title || ''
    userInfo.position = storageInfo.position || ''
    userInfo.birth = storageInfo.birth || ''
    userInfo.political = storageInfo.political || ''
    userInfo.officePhone = storageInfo.officePhone || storageInfo.office_phone || ''
    userInfo.education = storageInfo.education || storageInfo.Education || ''
    userInfo.degree = storageInfo.degree || storageInfo.Degree || ''
    userInfo.isFullTime = storageInfo.isFullTime === 'YES' || storageInfo.isFullTime === true || storageInfo.isFullTime === 1 || storageInfo.is_full_time === 'YES' || storageInfo.is_full_time === 1 || storageInfo.is_full_time === true
    userInfo.isRetired = storageInfo.isRetired === 'YES' || storageInfo.isRetired === true || storageInfo.isRetired === 1 || storageInfo.is_retired === 'YES' || storageInfo.is_retired === 1 || storageInfo.is_retired === true
    
    try {
      const res = await api.get(`/teacher/${userInfo.userId}`)
      if (res.code === '200' && res.data) {
        const data = res.data
        userInfo.staffNo = data.staffNo || data.staff_no || userInfo.staffNo
        userInfo.name = data.name || userInfo.name
        userInfo.dept = data.dept || data.deptName || data.dept_name || userInfo.dept
        userInfo.phone = data.phone || userInfo.phone
        userInfo.professionalTitle = data.professionalTitle || data.professional_title || data.title || userInfo.professionalTitle
        userInfo.position = data.position || userInfo.position
        userInfo.birth = data.birth || userInfo.birth
        userInfo.political = data.political || userInfo.political
        userInfo.officePhone = data.officePhone || data.office_phone || userInfo.officePhone
        userInfo.education = data.education || data.Education || userInfo.education
        userInfo.degree = data.degree || data.Degree || userInfo.degree
        userInfo.isFullTime = data.isFullTime === 'YES' || data.isFullTime === true || data.isFullTime === 1 || data.is_full_time === 'YES' || data.is_full_time === 1 || data.is_full_time === true || userInfo.isFullTime
        userInfo.isRetired = data.isRetired === 'YES' || data.isRetired === true || data.isRetired === 1 || data.is_retired === 'YES' || data.is_retired === 1 || data.is_retired === true || userInfo.isRetired
      }
    } catch (err) {
      console.error('加载教师信息失败:', err)
    }
  }
}

// 保存用户信息
const saveUserInfo = async () => {
  const storageInfo = getUserInfoFromStorage()

  // 校验姓名:至少两个字
  if (userInfo.name && userInfo.name.trim().length < 2) {
    ElMessage.warning('姓名不能少于两个字')
    return
  }
  // 校验手机号:必须为11位纯数字
  if (userInfo.phone && !/^\d{11}$/.test(userInfo.phone)) {
    ElMessage.warning('手机号必须为11位数字')
    return
  }

  if (storageInfo.userType === 'admin') {
    // 管理员用户：用户名和姓名一样，更新localStorage
    storageInfo.name = userInfo.name
    localStorage.setItem('userInfo', JSON.stringify(storageInfo))
    ElMessage.success('个人信息保存成功！')
    return
  }
  
  if (storageInfo.userType === 'teacher') {
    try {
      const res = await api.put('/teacher', {
        teacherId: userInfo.userId,
        name: userInfo.name,
        phone: userInfo.phone,
        dept: userInfo.dept,
        staffNo: userInfo.staffNo,
        professionalTitle: userInfo.professionalTitle,
        position: userInfo.position,
        birth: userInfo.birth,
        political: userInfo.political,
        officePhone: userInfo.officePhone,
        education: userInfo.education,
        degree: userInfo.degree,
        isFullTime: userInfo.isFullTime ? 'YES' : 'NO',
        isRetired: userInfo.isRetired ? 'YES' : 'NO'
      })
      if (res.code === '200') {
        ElMessage.success('个人信息保存成功！')
        // 同步更新localStorage：姓名 + 手机号 +其他教师信息字段
        storageInfo.name = userInfo.name
        storageInfo.userName = userInfo.name
        storageInfo.dept = userInfo.dept
        storageInfo.staffNo = userInfo.staffNo
        storageInfo.professionalTitle = userInfo.professionalTitle
        storageInfo.position = userInfo.position
        storageInfo.birth = userInfo.birth
        storageInfo.political = userInfo.political
        storageInfo.officePhone = userInfo.officePhone
        storageInfo.education = userInfo.education
        storageInfo.degree = userInfo.degree
        storageInfo.isFullTime = userInfo.isFullTime ? 'YES' : 'NO'
        storageInfo.isRetired = userInfo.isRetired ? 'YES' : 'NO'
        if (userInfo.phone && userInfo.phone !== storageInfo.username) {
          storageInfo.username = userInfo.phone
          ElMessage.info('手机号已修改，下次登录请使用新号码')
        }
        localStorage.setItem('userInfo', JSON.stringify(storageInfo))
      } else {
        ElMessage.error('保存失败')
      }
    } catch (err) {
      console.error('保存用户信息失败:', err)
      ElMessage.error('保存失败')
    }
  }
}

// 修改密码
const changePassword = () => {
  if (passwordFormRef.value) {
    passwordFormRef.value.validate(async (valid) => {
      if (valid) {
        try {
          const storageInfo = getUserInfoFromStorage()
          let res
          
          if (storageInfo.userType === 'admin') {
            // 管理员修改密码
            res = await api.post('/admin/change-password', {
              adminId: userInfo.userId,
              oldPassword: passwordForm.oldPassword,
              newPassword: passwordForm.newPassword
            })
          } else {
            // 教师修改密码
            res = await api.post('/teacher/change-password', {
              teacherId: userInfo.userId,
              oldPassword: passwordForm.oldPassword,
              newPassword: passwordForm.newPassword
            })
          }
          
          if (res.code === '200') {
            ElMessage.success('密码修改成功！')
            passwordForm.oldPassword = ''
            passwordForm.newPassword = ''
            passwordForm.confirmPassword = ''
          } else {
            ElMessage.error(res.msg || '密码修改失败')
          }
        } catch (err) {
          console.error('修改密码失败:', err)
          ElMessage.error('修改密码失败')
        }
      }
    })
  }
}

// 加载教师列表（供管理员重置密码时选择）
const loadTeacherOptions = async () => {
  if (userType.value !== 'admin') return
  try {
    const res = await api.get('/teacher')
    if (res.code === '200' && res.data) {
      teacherOptions.value = res.data
    }
  } catch (err) {
    console.error('加载教师列表失败:', err)
  }
}

// 选中教师后展示其信息
const onResetTeacherChange = (teacherId) => {
  if (!teacherId) {
    resetTeacherInfo.value = null
    return
  }
  resetTeacherInfo.value = teacherOptions.value.find(t => t.teacherId === teacherId) || null
}

// 执行重置密码
const handleResetPassword = async () => {
  if (!resetTeacherId.value) return
  const teacher = teacherOptions.value.find(t => t.teacherId === resetTeacherId.value)
  const teacherName = teacher ? teacher.name : ''
  try {
    await ElMessageBox.confirm(
      `确定要重置「${teacherName}」的密码吗？重置后密码将变为 123456。`,
      '确认重置密码',
      { type: 'warning', confirmButtonText: '确定重置', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  resetLoading.value = true
  try {
    const res = await api.post('/teacher/reset-password', { teacherId: resetTeacherId.value })
    if (res.code === '200') {
      ElMessage.success('密码已重置为 123456，请通知该教师及时修改密码')
    } else {
      ElMessage.error(res.msg || '重置密码失败')
    }
  } catch (err) {
    console.error('重置密码失败:', err)
    ElMessage.error('重置密码失败')
  } finally {
    resetLoading.value = false
  }
}

onMounted(() => {
  loadUserInfo()
  loadTeacherOptions()
})
</script>

<style scoped>
.profile-page {
  padding: 20px;
}

.info-card, .password-card {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}

.profile-avatar {
  margin-bottom: 15px;
}

.user-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.info-form {
  max-width: 500px;
}

.password-card {
  max-width: 500px;
}

.el-form-item {
  margin-bottom: 20px;
}

.reset-teacher-info {
  display: flex;
  gap: 24px;
  color: #606266;
  font-size: 14px;
}
</style>