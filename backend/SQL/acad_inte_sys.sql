-- ============================================
-- 创建数据库并切换
-- ============================================
CREATE DATABASE IF NOT EXISTS acad_inte_sys
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE acad_inte_sys;
CREATE TABLE ROLE (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL,
    role_desc VARCHAR(200)
);

-- 管理员表
CREATE TABLE ADMIN (
    admin_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    phone VARCHAR(20) UNIQUE,
    email VARCHAR(100) UNIQUE,
    role_id INT NOT NULL,
    FOREIGN KEY (role_id) REFERENCES ROLE(role_id)
);

-- 菜单表
CREATE TABLE MENU (
    menu_id INT PRIMARY KEY AUTO_INCREMENT,
    menu_name VARCHAR(50) NOT NULL,
    parent_id INT,
    menu_url VARCHAR(100),
    FOREIGN KEY (parent_id) REFERENCES MENU(menu_id)
);

-- 角色菜单关联表
CREATE TABLE ROLE_MENU (
    id INT PRIMARY KEY AUTO_INCREMENT,
    role_id INT NOT NULL,
    menu_id INT NOT NULL,
    FOREIGN KEY (role_id) REFERENCES ROLE(role_id),
    FOREIGN KEY (menu_id) REFERENCES MENU(menu_id),
    UNIQUE KEY (role_id, menu_id)
);

-- 部门表
CREATE TABLE DEPT (
    dept_id INT PRIMARY KEY AUTO_INCREMENT,
    dept_name VARCHAR(50) NOT NULL,
    dept_desc VARCHAR(200)
);

-- 教师表
CREATE TABLE TEACHER (
    teacher_id INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    dept VARCHAR(50),
    name VARCHAR(50) NOT NULL,
    professional_title VARCHAR(50),
    position VARCHAR(50),
    birth VARCHAR(20),
    political VARCHAR(20),
    phone VARCHAR(20),
    officePhone VARCHAR(20),
    password VARCHAR(100) NOT NULL,
    is_retired ENUM('YES', 'NO') NOT NULL,
    is_full_time ENUM('YES', 'NO')
);

-- 教师角色关联表
CREATE TABLE TEACHER_ROLE (
    id INT PRIMARY KEY AUTO_INCREMENT,
    teacher_id INT NOT NULL,
    role_id INT NOT NULL,
    FOREIGN KEY (teacher_id) REFERENCES TEACHER(teacher_id),
    FOREIGN KEY (role_id) REFERENCES ROLE(role_id),
    UNIQUE KEY (teacher_id, role_id)
);

-- 学期表
CREATE TABLE SEMESTER (
    semester_id INT PRIMARY KEY AUTO_INCREMENT,
    semester_name VARCHAR(50) NOT NULL,
    year INT NOT NULL,
    is_current TINYINT(1) NOT NULL DEFAULT 0
);

-- 教学计划表
CREATE TABLE TEACHING_PLAN (
    tp_id INT NOT NULL PRIMARY KEY,
    semester_id INT NOT NULL,
    teacher_id INT NOT NULL,
    notification_no VARCHAR(50) NOT NULL UNIQUE,
    course_no VARCHAR(30) NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    class_name VARCHAR(50) NOT NULL,
    course_seq INT NULL,
    student_count INT NULL,
    credit DECIMAL(3, 1) NOT NULL,
    scheduled_hours INT NULL,
    teaching_department VARCHAR(60) NULL,
    course_category VARCHAR(50) NULL,
    course_nature VARCHAR(50) NULL,
    course_attribute VARCHAR(30) NULL,
    campus VARCHAR(30) NULL,
    plan_hour_unit VARCHAR(20) NULL,
    group_name VARCHAR(50) NULL,
    scheduling_count INT NULL,
    actual_class_count INT NULL,
    is_degree_course TINYINT(1) NOT NULL,
    lecture_weeks VARCHAR(100) NULL,
    computer_weeks VARCHAR(100) NULL,
    total_plan_hours INT NOT NULL,
    lab_weeks VARCHAR(100) NULL,
    practice_weeks VARCHAR(100) NULL,
    other_weeks VARCHAR(100) NULL,
    lecture_hours INT NULL,
    computer_hours INT NULL,
    lab_hours INT NULL,
    practice_hours INT NULL,
    arranged_hours INT NULL,
    other_hours INT NULL,
    weekly_hours INT NULL,
    functional_area VARCHAR(100) NULL,
    FOREIGN KEY (semester_id) REFERENCES SEMESTER(semester_id),
    FOREIGN KEY (teacher_id) REFERENCES TEACHER(teacher_id)
) ;
-- 工作量表
CREATE TABLE WORKLOAD (
    wl_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    semester_id INT NULL,
    teacher_no VARCHAR(50) NULL,
    teacher_name VARCHAR(50) NULL,
    course_no VARCHAR(50) NULL,
    course_name VARCHAR(100) NULL,
    hour_type VARCHAR(50) NULL,
    notification_no VARCHAR(50) NULL,
    transfer_hours DECIMAL(8, 2) NULL,
    actual_hours DECIMAL(8, 2) NULL,
    total_hours DECIMAL(8, 2) NULL,
    total_credit DECIMAL(8, 2) NULL,
    actual_hour_ratio DECIMAL(8, 2) NULL,
    student_count INT NULL,
    class_name VARCHAR(100) NULL,
    confirm_status TINYINT NULL,
    FOREIGN KEY (semester_id) REFERENCES SEMESTER(semester_id)
);

-- 最初考试安排表
CREATE TABLE ORIGINAL_EXAM (
    exam_id INT PRIMARY KEY AUTO_INCREMENT,
    course_name VARCHAR(50) NOT NULL,
    semester_id INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    room VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reason VARCHAR(200),
    FOREIGN KEY (semester_id) REFERENCES SEMESTER(semester_id),
    CHECK (end_time > start_time)
);

-- 期末考试表
CREATE TABLE FINAL_EXAM (
    exam_id INT PRIMARY KEY AUTO_INCREMENT,
    course_name VARCHAR(50) NOT NULL,
    semester_id INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    room VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    FOREIGN KEY (semester_id) REFERENCES SEMESTER(semester_id),
    CHECK (end_time > start_time)
);

-- 最初考试安排监考关联表
CREATE TABLE ORIGINAL_EXAM_TEACHER (
    id INT PRIMARY KEY AUTO_INCREMENT,
    exam_id INT NOT NULL,
    teacher_id INT NOT NULL,
    invigilate_role VARCHAR(50),
    FOREIGN KEY (exam_id) REFERENCES ORIGINAL_EXAM(exam_id),
    FOREIGN KEY (teacher_id) REFERENCES TEACHER(teacher_id),
    UNIQUE KEY (exam_id, teacher_id)
);

-- 期末考试监考关联表
CREATE TABLE FINAL_EXAM_TEACHER (
    id INT PRIMARY KEY AUTO_INCREMENT,
    exam_id INT NOT NULL,
    teacher_id INT NOT NULL,
    invigilate_role VARCHAR(50),
    FOREIGN KEY (exam_id) REFERENCES FINAL_EXAM(exam_id),
    FOREIGN KEY (teacher_id) REFERENCES TEACHER(teacher_id),
    UNIQUE KEY (exam_id, teacher_id)
);

-- 会议室表
CREATE TABLE MEETING_ROOM (
    room_id INT PRIMARY KEY AUTO_INCREMENT,
    room_name VARCHAR(50) NOT NULL,
    capacity INT NOT NULL CHECK(capacity>0),
    room_status INT DEFAULT 1
);

-- 会议室申请表
CREATE TABLE ROOM_APPLY (
    apply_id INT PRIMARY KEY AUTO_INCREMENT,
    room_id INT NOT NULL,
    teacher_id INT NOT NULL,
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    apply_status INT DEFAULT 0,
    FOREIGN KEY (room_id) REFERENCES MEETING_ROOM(room_id),
    FOREIGN KEY (teacher_id) REFERENCES TEACHER(teacher_id),
    CHECK (end_time > start_time)
);

-- 通知公告表
CREATE TABLE NOTICE (
    notice_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    publish_time DATETIME NOT NULL,
    publisher_id INT NOT NULL,
    publisher_type VARCHAR(10) DEFAULT 'teacher' COMMENT '发布人类型: admin/teacher',
    notice_type VARCHAR(50) COMMENT '通知类型',
    publish_dept VARCHAR(50) COMMENT '发布部门',
    attachments TEXT COMMENT '附件JSON',
    receive_roles VARCHAR(255) COMMENT '接收角色',
    receive_depts VARCHAR(255) COMMENT '接收部门',
    status VARCHAR(20) DEFAULT 'published' COMMENT '状态：published/unread/read',
    publisher_name VARCHAR(100) DEFAULT NULL COMMENT '发布人姓名（发布时存入，避免 admin_id 与 teacher_id 冲突）'
);

-- 通知接收关联表
CREATE TABLE NOTICE_RECEIVE (
    id INT PRIMARY KEY AUTO_INCREMENT,
    notice_id INT NOT NULL,
    teacher_id INT NOT NULL,
    is_received TINYINT(1) NOT NULL DEFAULT 0,
    is_todo TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否待办：0否 1是',
    confirm_time DATETIME,
    status VARCHAR(20) DEFAULT 'unread' COMMENT '状态: unread/read/todo',
    remind_count INT DEFAULT 0 COMMENT '提醒次数',
    FOREIGN KEY (notice_id) REFERENCES NOTICE(notice_id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES TEACHER(teacher_id),
    UNIQUE KEY (notice_id, teacher_id)
);

-- 教师部门关联表
CREATE TABLE TEACHER_DEPT (
    id INT PRIMARY KEY AUTO_INCREMENT,
    teacher_id INT NOT NULL,
    dept_id INT NOT NULL,
    FOREIGN KEY (teacher_id) REFERENCES TEACHER(teacher_id),
    FOREIGN KEY (dept_id) REFERENCES DEPT(dept_id),
    UNIQUE KEY (teacher_id, dept_id)
);

-- 插入会议室
INSERT INTO MEETING_ROOM (room_name, capacity, room_status) VALUES
                                                                ('第一会议室', 30, 1),
                                                                ('第二会议室', 50, 1),
                                                                ('第三会议室', 80, 0),
                                                                ('第四会议室', 20, 1),
                                                                ('第五会议室', 40, 1);

-- 插入教师（用于申请）
INSERT INTO TEACHER (name, password, is_retired, is_full_time) VALUES
                                                                   ('张老师', '123456', 'NO', 'YES'),
                                                                   ('李老师', '123456', 'NO', 'YES'),
                                                                   ('王老师', '123456', 'NO', 'YES');

-- 插入测试申请
INSERT INTO ROOM_APPLY (room_id, teacher_id, start_time, end_time, apply_status) VALUES
                                                                                     (1, 1, '2026-06-24 09:00:00', '2026-06-24 11:00:00', 1),
                                                                                     (1, 2, '2026-06-24 14:00:00', '2026-06-24 16:00:00', 0),
                                                                                (2, 1, '2026-06-25 10:00:00', '2026-06-25 12:00:00', 0);
SELECT * FROM MEETING_ROOM;
