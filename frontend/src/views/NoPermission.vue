<template>
  <div class="no-permission-container">
    <el-card class="no-permission-card">
      <div class="icon-wrapper">
        <el-icon class="no-permission-icon">
          <Lock />
        </el-icon>
      </div>
      <h2 class="no-permission-title">无权限访问</h2>
      <p class="no-permission-desc">您当前的角色没有访问此页面的权限</p>
      <p class="no-permission-tip">请联系管理员分配相应权限</p>
      <el-button type="primary" @click="goBack">返回首页</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Lock } from '@element-plus/icons-vue'

const router = useRouter()

const goBack = () => {
  router.push('/profile')
}
</script>

<style scoped>
.no-permission-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.no-permission-card {
  width: 450px;
  text-align: center;
  padding: 40px;
}

.icon-wrapper {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background-color: #fef0f0;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.no-permission-icon {
  font-size: 40px;
  color: #f56c6c;
}

.no-permission-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.no-permission-desc {
  font-size: 16px;
  color: #606266;
  margin-bottom: 8px;
}

.no-permission-tip {
  font-size: 14px;
  color: #909399;
  margin-bottom: 30px;
}
</style>