-- ============================================================
-- 成果管理模块数据库（本地库 achievement_system）
-- ------------------------------------------------------------
-- 成果管理功能使用独立的本地数据库 achievement_system，
-- 与主系统业务库（acad_inte_sys）相互隔离，互不影响。
--
-- 执行方式（在本地 MySQL 上执行一次即可）：
--   mysql -uroot -p < achievement_system.sql
--
-- 连接账号默认 root/123456，可在 application.yaml 的
-- achievement.datasource 配置中修改，或用环境变量覆盖：
--   ACHIEVEMENT_DB_URL / ACHIEVEMENT_DB_USERNAME / ACHIEVEMENT_DB_PASSWORD
-- ============================================================

CREATE DATABASE IF NOT EXISTS achievement_system
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE achievement_system;

DROP TABLE IF EXISTS achievement;

CREATE TABLE achievement (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    category        VARCHAR(64)  NOT NULL                COMMENT '成果类别',
    classify_level  VARCHAR(32)  DEFAULT NULL            COMMENT '成果分类等级',
    name            VARCHAR(255) NOT NULL                COMMENT '成果名称',
    persons         VARCHAR(500) NOT NULL                COMMENT '姓名（多人用、分隔）',
    level           VARCHAR(32)  NOT NULL                COMMENT '成果级别：国家级/省级/市级/校级',
    grade           VARCHAR(64)  NOT NULL                COMMENT '成果等级：一等奖/二等奖等',
    achieve_date    DATE         NOT NULL                COMMENT '获得时间',
    issuer          VARCHAR(255) NOT NULL                COMMENT '发证单位',
    contest_name    VARCHAR(255) DEFAULT NULL            COMMENT '比赛名称（可选）',
    file_name       VARCHAR(255) DEFAULT NULL            COMMENT '附件原始文件名',
    file_url        VARCHAR(500) DEFAULT NULL            COMMENT '附件访问地址',
    ocr_filled      TINYINT      NOT NULL DEFAULT 0      COMMENT '是否经OCR识别填充：0否 1是',
    status          TINYINT      NOT NULL DEFAULT 0      COMMENT '状态：0待验证 1通过 2驳回',
    verify_time     DATETIME     DEFAULT NULL            COMMENT '验证时间',
    verify_remark   VARCHAR(500) DEFAULT NULL            COMMENT '验证备注',
    submit_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_status (status),
    KEY idx_category (category),
    KEY idx_achieve_date (achieve_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '成果信息表（成果管理模块新增表，独立本地库）';

-- ------------------------------------------------------------
-- 种子数据（示例，可自行删改）
-- 前 4 条为「已通过」，供成果信息展示页列表与图表展示；
-- 后 2 条为「待验证」，供成果信息人工验证页审核体验。
-- ------------------------------------------------------------
INSERT INTO achievement
    (category, classify_level, name, persons, level, grade, achieve_date, issuer, contest_name, ocr_filled, status, verify_time, submit_time)
VALUES
    ('教学成果获奖', '二类', '山东省省级教学成果奖',
     '桑培东、王德东、李祥军、刘兴民、张琳、李媛媛、亓霞',
     '市级', '二等奖', '2022-11-01', '山东省省级教学成果奖评审委员会', NULL,
     0, 1, '2026-09-18 10:00:00', '2026-09-18 08:30:00'),
    ('教学成果获奖', '三类A', '山东省第九届教学成果奖(高等教育类)',
     '苟志远、张贵华、周景阳、赵辉、洪文霞、申建红、赵金先、刘丁、张卓如、李伟丽',
     '省级', '二等奖', '2022-11-01', '山东省省级教学成果奖评审委员会', NULL,
     0, 1, '2026-09-18 09:40:00', '2026-09-18 08:35:00'),
    ('指导学生竞赛获奖', '五类', '蓝桥杯大赛',
     '张仲妹', '省级', '五类', '2025-05-26', '工业和信息化部人才交流中心', '蓝桥杯大赛',
     0, 1, '2026-09-18 09:00:00', '2026-09-18 08:40:00'),
    ('指导学生竞赛获奖', '五类', '第十五届蓝桥杯全国软件和信息技术专业人才大赛山东赛区Python程序设计大学B组三等奖',
     '张仲妹', '省级', '五类', '2024-04-29', '工业和信息化部人才交流中心', '蓝桥杯大赛',
     0, 1, '2026-09-18 09:10:00', '2026-09-18 08:45:00'),
    ('教材建设成果', '省级规划教材', '《数据结构》教材编写',
     '王伟', '省级', '省级规划教材', '2023-09-01', '教育部', NULL,
     0, 0, NULL, '2026-09-18 08:50:00'),
    ('教学研究论文', '四类', '基于深度学习的遥感影像分类方法研究',
     '李明、张华', '国家级', '四类', '2024-06-15', '中国计算机学会', NULL,
     0, 0, NULL, '2026-09-18 08:55:00');
