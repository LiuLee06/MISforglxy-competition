<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-left">
        <img src="../img/2.jpg" alt="illustration" class="left-img" />
      </div>
      <div class="login-right">
        <h2 class="welcome-title">欢迎登录</h2>
        <el-form :model="loginForm" :rules="rules" ref="formRef" label-position="top">
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" :placeholder="usernamePlaceholder" size="large">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password size="large">
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading" size="large">登录</el-button>
          </el-form-item>
        </el-form>
        <!-- 登录方式切换：仅对教师生效，admin 始终用账号登录 -->
        <div class="login-type-switch">
          <span>教师登录方式：</span>
          <el-radio-group v-model="loginType" size="small">
            <el-radio-button label="phone">手机号</el-radio-button>
            <el-radio-button label="name">姓名</el-radio-button>
            <el-radio-button label="staffNo">工号</el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </div>
    <p class="icp-beian"><a href="https://beian.miit.gov.cn/" target="_blank">鲁ICP备2026049301号-1</a></p>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import api from '../api/index'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

// 登录方式：phone（手机号，默认）/ name（姓名）/ staffNo（工号）
const loginType = ref('phone')

const usernamePlaceholder = computed(() => {
  if (loginType.value === 'name') return '请输入姓名'
  if (loginType.value === 'staffNo') return '请输入工号'
  return '请输入账号/手机号'
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const payload = {
          username: loginForm.username,
          password: loginForm.password,
          loginType: loginType.value
        }

        if (loginType.value === 'phone') {
          payload.phone = loginForm.username
        } else if (loginType.value === 'name') {
          payload.name = loginForm.username
        } else if (loginType.value === 'staffNo') {
          payload.staffNo = loginForm.username
        }

        const res = await api.post('/login', payload)

        if (res.code === '200' && res.data) {
          localStorage.setItem('isLoggedIn', 'true')
          localStorage.setItem('userInfo', JSON.stringify(res.data))
          localStorage.setItem('userId', String(res.data.userId))
          localStorage.setItem('userType', res.data.userType || 'teacher')
          localStorage.setItem('userName', res.data.name || '')
          localStorage.setItem('menus', JSON.stringify(res.data.menus || []))
          // 后端下发的 JWT，后续 axios 请求会自动附加到 Authorization 头
          if (res.data.token) {
            localStorage.setItem('token', res.data.token)
          }
          // 记录登录时间，用于 24 小时会话过期判断
          localStorage.setItem('loginTime', String(Date.now()))
          ElMessage.success('登录成功')
          router.push('/profile')
        } else {
          ElMessage.error(res.msg || '登录失败')
        }
      } catch (error) {
        console.error('登录失败:', error)
        ElMessage.error('登录失败，请检查后端服务是否已启动')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: url('../img/1.jpg') no-repeat center center fixed;
  background-size: cover;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}

.icp-beian {
  position: absolute;
  bottom: 20px;
  left: 0;
  right: 0;
  text-align: center;
  font-size: 13px;
  margin: 0;
}
.icp-beian a {
  color: rgba(255, 255, 255, 0.85);
  text-decoration: none;
  transition: color 0.2s;
}
.icp-beian a:hover {
  color: #fff;
}

.login-card {
  display: flex;
  width: 820px;
  min-height: 440px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 50, 120, 0.25);
  overflow: hidden;
}

.login-left {
  flex: 1.2;
  background: linear-gradient(135deg, #e8f1ff 0%, #c8e0ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30px;
  min-height: 440px;
}

.left-img {
  width: 100%;
  height: 100%;
  max-height: 380px;
  object-fit: contain;
}

.login-right {
  flex: 1;
  padding: 50px 45px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.welcome-title {
  font-size: 26px;
  font-weight: 600;
  color: #1a3a6e;
  margin: 0 0 30px 0;
  text-align: left;
}

.login-btn {
  width: 100%;
  background: linear-gradient(135deg, #4a90e2 0%, #357abd 100%);
  border: none;
  font-size: 16px;
  letter-spacing: 4px;
  border-radius: 6px;
  height: 44px;
}

.login-btn:hover {
  background: linear-gradient(135deg, #357abd 0%, #2a6cb5 100%);
}

:deep(.el-input__wrapper) {
  border-radius: 6px;
  padding: 4px 12px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #4a90e2 inset;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-type-switch {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}
</style>