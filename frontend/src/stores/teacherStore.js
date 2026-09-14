import { ref, computed } from 'vue'
import { deptApi, teacherApi } from '../api/index.js'

const teacherList = ref([])
const deptList = ref([])

const totalNum = computed(() => teacherList.value.length)

const professorNum = computed(() => teacherList.value.filter(t => {
  const title = t.title || t.professionalTitle || t.professional_title || ''
  return title.includes('教授') && !title.includes('副')
}).length)

const associateNum = computed(() => teacherList.value.filter(t => {
  const title = t.title || t.professionalTitle || t.professional_title || ''
  return title.includes('副教授')
}).length)

const lecturerNum = computed(() => teacherList.value.filter(t => {
  const title = t.title || t.professionalTitle || t.professional_title || ''
  return title.includes('讲师')
}).length)

const ageDistribution = computed(() => {
  const groups = { '≤30岁': 0, '31-40岁': 0, '41-50岁': 0, '>50岁': 0 }
  const now = new Date()
  teacherList.value.forEach(item => {
    if (!item.birth) return
    const birthDate = new Date(item.birth)
    let age = now.getFullYear() - birthDate.getFullYear()
    const monthDiff = now.getMonth() - birthDate.getMonth()
    const dayDiff = now.getDate() - birthDate.getDate()
    if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) age--
    if (age <= 30) groups['≤30岁']++
    else if (age <= 40) groups['31-40岁']++
    else if (age <= 50) groups['41-50岁']++
    else groups['>50岁']++
  })
  return Object.entries(groups).map(([name, value]) => ({ name, value }))
})

const degreeDistribution = computed(() => {
  const counts = { 博士: 0, 硕士: 0, 本科及以下: 0 }
  teacherList.value.forEach(item => {
    const degree = item.degree || item.Education || ''
    if (degree.includes('博士')) counts['博士']++
    else if (degree.includes('硕士')) counts['硕士']++
    else counts['本科及以下']++
  })
  return Object.entries(counts).map(([name, value]) => ({ name, value }))
})

const parseDeptValue = (value) => {
  if (Array.isArray(value)) return value.map(v => String(v).trim()).filter(Boolean)
  if (value == null) return []
  return String(value)
    .split(/[,、;；|\n\r]+/)
    .map(item => item.trim())
    .filter(Boolean)
}

const fullTimeTeacherList = computed(() => teacherList.value.filter(t => t.isFullTime && !t.isRetired))

const deptSummary = computed(() => {
  const total = fullTimeTeacherList.value.length
  return deptList.value.map(dept => {
    const deptKey = String(dept.deptId || dept.dept_id || '').trim()
    const deptName = String(dept.deptName || dept.dept_name || '').trim()
    const teacherCount = fullTimeTeacherList.value.filter(t => {
      const teacherDeptKeys = parseDeptValue(t.deptId || t.dept_id || '')
      const teacherDeptNames = parseDeptValue(t.dept || t.deptName || t.dept_name || '')
      return teacherDeptKeys.includes(deptKey) || teacherDeptNames.includes(deptName)
    }).length
    const ratio = total > 0 ? ((teacherCount / total) * 100).toFixed(1) + '%' : '0%'
    return {
      dept_id: dept.deptId || dept.dept_id,
      dept_name: dept.deptName || dept.dept_name,
      dept_desc: dept.deptDesc || dept.dept_desc,
      teacherCount,
      ratio
    }
  })
})

const loadTeachers = async () => {
  try {
    const res = await teacherApi.getAll()
    if (res.code === '200' && res.data) {
      teacherList.value = res.data.map(t => ({
        ...t,
        deptId: t.deptId,
        deptName: t.dept,
        dept_id: t.deptId || t.dept,
        dept_name: t.dept,
        title: t.title || t.professionalTitle || t.professional_title || '',
        professionalTitle: t.professionalTitle || t.professional_title || t.title || '',
        degree: t.degree || t.Degree || t.education || t.Education || '',
        staffNo: t.staffNo ?? t.staff_no ?? '',
        education: t.education ?? t.Education ?? '',
        isRetired: ['YES', '1', 1, true].includes(t.isRetired ?? t.is_retired ?? t.retired ?? ''),
        isFullTime: ['YES', '1', 1, true].includes(t.isFullTime ?? t.fullTime ?? t.is_full_time ?? '')
      }))
    } else {
      teacherList.value = []
    }
  } catch (error) {
    console.error('加载教师数据失败:', error)
    teacherList.value = []
  }
}

const loadDepts = async () => {
  try {
    const res = await deptApi.getAll()
    if (res.code === '200' && res.data) {
      deptList.value = res.data.map(d => ({
        ...d,
        dept_id: d.deptId,
        dept_name: d.deptName,
        dept_desc: d.deptDesc
      }))
    } else {
      deptList.value = []
    }
  } catch (error) {
    console.error('加载部门数据失败:', error)
    deptList.value = []
  }
}

const addDept = (dept) => {
  deptList.value.push(dept)
}

const updateDept = (deptId, dept) => {
  const index = deptList.value.findIndex(d => (d.deptId || d.dept_id) === deptId)
  if (index !== -1) {
    deptList.value[index] = { ...deptList.value[index], ...dept }
  }
}

const removeDept = (deptId) => {
  const index = deptList.value.findIndex(d => (d.deptId || d.dept_id) === deptId)
  if (index !== -1) {
    deptList.value.splice(index, 1)
  }
}

const createDept = async (data) => {
  try {
    const res = await deptApi.create(data)
    if (res.code === '200') {
      await loadDepts()
    }
    return res
  } catch (error) {
    throw error
  }
}

const updateDeptToBackend = async (data) => {
  try {
    const payload = {
      deptId: data.deptId || data.dept_id,
      deptName: data.deptName || data.dept_name,
      deptDesc: data.deptDesc || data.dept_desc
    }
    const res = await deptApi.update(payload)
    if (res.code === '200') {
      await loadDepts()
    }
    return res
  } catch (error) {
    throw error
  }
}

const deleteDeptFromBackend = async (deptId) => {
  try {
    const res = await deptApi.delete(deptId)
    if (res.code === '200') {
      await loadDepts()
    }
    return res
  } catch (error) {
    throw error
  }
}

export {
  teacherList,
  deptList,
  totalNum,
  professorNum,
  associateNum,
  lecturerNum,
  ageDistribution,
  degreeDistribution,
  deptSummary,
  addDept,
  updateDept,
  removeDept,
  loadTeachers,
  loadDepts,
  createDept,
  updateDeptToBackend,
  deleteDeptFromBackend
}
