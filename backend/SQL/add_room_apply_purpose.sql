-- 为已有 ROOM_APPLY 表增加申请用途字段。
-- 可重复执行：字段已存在时不会重复添加。
SET @purpose_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'ROOM_APPLY'
      AND column_name = 'purpose'
);
SET @purpose_sql = IF(
    @purpose_column_exists = 0,
    'ALTER TABLE ROOM_APPLY ADD COLUMN purpose VARCHAR(500) NULL AFTER applicant_name',
    'SELECT 1'
);
PREPARE purpose_stmt FROM @purpose_sql;
EXECUTE purpose_stmt;
DEALLOCATE PREPARE purpose_stmt;