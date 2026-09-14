<template>
  <div class="notice-container">
    <el-tabs v-model="activeTab" type="card">
      <el-tab-pane label="通知查看" name="list">
        <NoticeList />
      </el-tab-pane>
      <el-tab-pane label="发布公告" name="publish">
        <NoticePublish />
      </el-tab-pane>
      <el-tab-pane label="公告维护" name="maintain">
        <NoticeMaintain />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NoticeList from './NoticeList.vue'
import NoticePublish from './NoticePublish.vue'
import NoticeMaintain from './NoticeMaintain.vue'

const route = useRoute()
const router = useRouter()

const activeTab = ref('list')

watch(route.query.tab, (newVal) => {
  if (newVal && ['list', 'publish', 'maintain'].includes(newVal)) {
    activeTab.value = newVal
  }
}, { immediate: true })

watch(activeTab, (newVal) => {
  router.push({ query: { tab: newVal } })
})
</script>

<style scoped>
.notice-container {
  padding: 20px;
}
</style>
