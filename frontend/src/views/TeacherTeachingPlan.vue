<template>
  <div class="page-container">
    <div class="page-header">
      <h2>我的教学计划</h2>
    </div>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <span>教学计划列表</span>
      </template>

      <el-table :data="tableData" border stripe style="width: 100%" v-loading="loading" max-height="600">
        <el-table-column prop="noticeId" label="通知单编号" min-width="180" show-overflow-tooltip fixed />
        <el-table-column prop="courseCode" label="课程编号" width="100" />
        <el-table-column prop="courseName" label="课程名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="授课教师" width="100" />
        <el-table-column prop="teacherId" label="教工号" width="110" />
        <el-table-column prop="teacherTitle" label="教师职称" width="100" />
        <el-table-column prop="className" label="班级名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="courseSeq" label="课序号" width="90" />
        <el-table-column prop="courseCategory" label="课程类别" width="100" />
        <el-table-column prop="courseNature" label="课程性质" width="100" />
        <el-table-column prop="courseAttribute" label="课程属性" width="100" />
        <el-table-column prop="isDegreeCourse" label="是否学位课" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isDegreeCourse === 1 ? 'success' : 'info'">
              {{ row.isDegreeCourse === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="credit" label="学分" width="70" />
        <el-table-column prop="offeringDepartment" label="开课单位" min-width="140" show-overflow-tooltip />
        <el-table-column prop="campus" label="校区" width="100" />
        <el-table-column prop="groupName" label="分组名" width="100" />
        <el-table-column prop="planHourUnit" label="计划学时单位" width="110" />
        <el-table-column prop="totalPlanHours" label="计划学时" width="100" />
        <el-table-column prop="lectureHours" label="讲课学时" width="90" />
        <el-table-column prop="labHours" label="上机学时" width="90" />
        <el-table-column prop="experimentHours" label="实验学时" width="90" />
        <el-table-column prop="otherHours" label="其他学时" width="90" />
        <el-table-column prop="arrangedHours" label="安排学时" width="100" />
        <el-table-column prop="scheduledHours" label="已排学时" width="90" />
        <el-table-column prop="weeklyHours" label="周学时" width="80" />
        <el-table-column prop="lectureWeeks" label="讲课周次" min-width="120" show-overflow-tooltip />
        <el-table-column prop="labWeeks" label="上机周次" min-width="120" show-overflow-tooltip />
        <el-table-column prop="experimentWeeks" label="实验周次" min-width="120" show-overflow-tooltip />
        <el-table-column prop="practiceWeeks" label="实践周次" min-width="120" show-overflow-tooltip />
        <el-table-column prop="practiceWeeksCount" label="实践周" width="90" />
        <el-table-column prop="otherWeeks" label="其他周次" min-width="120" show-overflow-tooltip />
        <el-table-column prop="selectedStudentCount" label="选课人数" width="90" />
        <el-table-column prop="scheduledStudentCount" label="排课人数" width="90" />
        <el-table-column prop="actualClassSize" label="班级实际人数" width="110" />
        <el-table-column prop="functionalArea" label="功能区" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="100" fixed="right">
          <template #default="{ row }">
            <el-tag :type="Number(row.confirmStatus) === 1 ? 'success' : 'warning'">
              {{ Number(row.confirmStatus) === 1 ? '已确认' : '待确认' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              type="success"
              size="small"
              :disabled="Number(row.confirmStatus) === 1"
              @click="handleConfirm(row)"
            >
              {{ Number(row.confirmStatus) === 1 ? '已确认' : '确认' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑教学计划"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="通知单编号">
          <el-input :model-value="editForm.noticeId" disabled />
        </el-form-item>
        <el-form-item label="教师姓名">
          <el-input v-model="editForm.teacherName" />
        </el-form-item>
        <el-form-item label="讲课周次">
          <el-input v-model="editForm.lectureWeeks" placeholder="例如：1-16周" />
        </el-form-item>
        <el-form-item label="实验周次">
          <el-input v-model="editForm.experimentWeeks" placeholder="例如：5-14周" />
        </el-form-item>
        <el-form-item label="实践周次">
          <el-input v-model="editForm.practiceWeeks" placeholder="例如：17-18周" />
        </el-form-item>
        <el-form-item label="上机周次">
          <el-input v-model="editForm.labWeeks" placeholder="例如：3-15周" />
        </el-form-item>
        <el-form-item label="周学时">
          <el-input v-model="editForm.weeklyHours" placeholder="例如：4" />
        </el-form-item>
        <el-form-item label="功能区">
          <el-input v-model="editForm.functionalArea" placeholder="例如：教学楼A301" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { teachingScheduleApi } from '../api/index'

// 操作权限码
const actionCodes = JSON.parse(localStorage.getItem('userInfo') || '{}').actionCodes || []
const hasPermission = (code) => actionCodes.includes(code)

const tableData = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)

const editForm = reactive({
  noticeId: '',
  teacherName: '',
  lectureWeeks: '',
  experimentWeeks: '',
  practiceWeeks: '',
  labWeeks: '',
  weeklyHours: '',
  functionalArea: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    // 按当前登录教师姓名查询自己的教学计划
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const res = await teachingScheduleApi.getByConditions({ teacherName: userInfo.name || '' })
    if (res.code === '200' && res.data) {
      tableData.value = res.data
    } else {
      tableData.value = []
    }
  } catch (error) {
    console.error('获取教学计划失败:', error)
    ElMessage.error('获取教学计划失败')
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const handleEdit = (row) => {
  editForm.noticeId = row.noticeId
  editForm.teacherName = row.teacherName || ''
  editForm.lectureWeeks = row.lectureWeeks || ''
  editForm.experimentWeeks = row.experimentWeeks || ''
  editForm.practiceWeeks = row.practiceWeeks || ''
  editForm.labWeeks = row.labWeeks || ''
  editForm.weeklyHours = row.weeklyHours || ''
  editForm.functionalArea = row.functionalArea || ''
  dialogVisible.value = true
}

const handleSave = async () => {
  saving.value = true
  try {
    const res = await teachingScheduleApi.updateEditable(editForm)
    if (res.code === '200') {
      ElMessage.success('更新成功')
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
    console.error('更新失败:', error)
    ElMessage.error('系统异常，请稍后重试')
  } finally {
    saving.value = false
  }
}

const handleConfirm = async (row) => {
  try {
    const res = await teachingScheduleApi.confirm(row.noticeId)
    if (res.code === '200') {
      row.confirmStatus = 1
      ElMessage.success('确认成功')
      await fetchData()
    } else {
      ElMessage.error(res.message || '确认失败')
    }
  } catch (error) {
    console.error('确认失败:', error)
    ElMessage.error('系统异常，请稍后重试')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.teacher-teaching-plan {
  padding: 0;
}
</style>
