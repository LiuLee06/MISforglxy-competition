CREATE TABLE IF NOT EXISTS original_exam_teacher (
    id INT AUTO_INCREMENT PRIMARY KEY,
    exam_id INT NOT NULL,
    teacher_id INT NOT NULL,
    invigilate_role VARCHAR(20) NOT NULL COMMENT '监考角色：主监考/副监考',
    note VARCHAR(255) DEFAULT NULL COMMENT '备注',
    status VARCHAR(20) DEFAULT '待执行' COMMENT '任务状态：待执行/已完成',
    reason VARCHAR(500) DEFAULT NULL COMMENT '意见反馈',
    FOREIGN KEY (exam_id) REFERENCES original_exam(exam_id),
    FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id),
    UNIQUE KEY uk_exam_teacher (exam_id, teacher_id, invigilate_role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='草稿监考任务表';