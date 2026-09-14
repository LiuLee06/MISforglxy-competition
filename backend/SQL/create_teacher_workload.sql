-- ================================================================
-- 教师工作量计算模块 - 建表与菜单补丁
-- 用途：按 G = a × b × 执行学时 计算并落库每学期每位教师的工作量
--   a 课程系数：课程编号 GL 开头 = 1.0，其他 = 1.15
--   b 人数系数：人数<=c 时 b=1；c<人数<=2c 时 b=(人数-c)/c*0.5+1；
--              人数>2c 时 b=(人数-2c)/c*0.4+1.5
--   c 人数限值：默认 32，管理端计算时可修改
-- 执行说明：
--   1. 本脚本只需执行一次
--   2. 菜单授权（给教学办等角色）请在前端"角色管理"页面操作
-- ================================================================

USE acad_inte_sys;

-- ================================================================
-- 1. 教师工作量结果表（教师 × 学期 粒度，一人一学期一条）
-- ================================================================
CREATE TABLE IF NOT EXISTS TEACHER_WORKLOAD (
    id INT PRIMARY KEY AUTO_INCREMENT,
    teacher_no VARCHAR(50) NOT NULL COMMENT '教师工号',
    teacher_name VARCHAR(50) NULL COMMENT '教师姓名',
    semester_id INT NOT NULL COMMENT '学期',
    student_limit INT NULL COMMENT '计算时使用的人数限值 c（存档便于追溯）',
    total_hours DECIMAL(10, 2) NULL COMMENT '合计执行学时（实际学时求和）',
    workload_hours DECIMAL(10, 2) NULL COMMENT '工作量合计 G',
    calc_time DATETIME NULL COMMENT '最近一次计算时间',
    UNIQUE KEY uk_teacher_semester (teacher_no, semester_id),
    FOREIGN KEY (semester_id) REFERENCES SEMESTER(semester_id)
);

-- ================================================================
-- 2. 新增菜单（挂在"教学管理" parent_id=4 下）
--    注意：menu_id=25 已被线上库"进度管理(/exam-progress)"占用，
--    故本模块使用 26、27
--    角色授权请在前端"角色管理"页面操作，本脚本不写 ROLE_MENU
-- ================================================================
INSERT IGNORE INTO MENU (menu_id, menu_name, parent_id, menu_url) VALUES
(26, '我的工作量', 4, '/teacher-workload-result'),
(27, '工作量计算', 4, '/workload-result');

-- ================================================================
-- 3. 验证
-- ================================================================
SELECT '=== TEACHER_WORKLOAD 表结构 ===' AS title;
SHOW COLUMNS FROM TEACHER_WORKLOAD;

SELECT '=== 新增菜单 ===' AS title;
SELECT menu_id, menu_name, parent_id, menu_url FROM MENU WHERE menu_id IN (26, 27);
