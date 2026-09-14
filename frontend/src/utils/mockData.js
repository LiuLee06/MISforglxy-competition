import { ref } from 'vue' // 顶部加上这行导入

export const mockNotices = ref([
  {
    noticeId: 'we2e323',
    title: '关于研究生管理系统例行维护的通知',
    publishDept: '教学办',
    publishTime: '2025-03-15 10:00',
    noticeType: '教学通知',
    content: '管理学校通知时代的系统将于本周六进行例行维护，维护期间系统将暂停服务，请各位老师提前做好准备。',
    receivers: ['全体教师'],
    sendType: 'immediate',
    status: 'read'
  },
  {
    noticeId: 'gsdfvs',
    title: '选课业务办理通知',
    publishDept: '院办',
    publishTime: '2025-03-18 14:30',
    noticeType: '教学通知',
    content: '本学期选课截止时间为3月20日，请各位老师及时通知学生完成选课。',
    receivers: ['全体教师'],
    sendType: 'immediate',
    status: 'unread'
  },
  {
    noticeId: 'gdgdg',
    title: '关于举办筑基讲坛的通知',
    publishDept: '教学办',
    publishTime: '2025-04-16 09:00',
    noticeType: '教学通知',
    content: '第四期筑基讲坛将于4月20日下午2点在学术报告厅举行，请各位老师准时参加。',
    receivers: ['全体教师'],
    sendType: 'immediate',
    status: 'read'
  },
  {
    noticeId: 'wrwrwr',
    title: '关于开展2025年研究生学术论坛论文的通知',
    publishDept: '院办',
    publishTime: '2025-04-28 16:00',
    noticeType: '行政通知',
    content: '2025年研究生学术论坛论文征集截止时间为5月15日，请各位老师积极组织学生投稿。',
    receivers: ['全体教师'],
    sendType: 'immediate',
    status: 'unread'
  },
  {
    noticeId: 'fsfsfsfs',
    title: '关于提交研究生学位论文盲审稿的紧急通知',
    publishDept: '教学办',
    publishTime: '2025-05-23 08:30',
    noticeType: '行政通知',
    content: '请各位导师于5月30日前提交研究生学位论文盲审稿，逾期将影响答辩安排。',
    receivers: ['全体教师'],
    sendType: 'immediate',
    status: 'read'
  }
])