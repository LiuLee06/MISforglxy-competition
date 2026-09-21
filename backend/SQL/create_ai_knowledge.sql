-- AI 学院文件知识库
-- 原始文件二进制继续使用现有 FILE_STORE 表，Markdown 和目录信息保存在本表。
CREATE TABLE IF NOT EXISTS FILE_STORE (
    file_name VARCHAR(255) PRIMARY KEY,
    content LONGBLOB NOT NULL,
    upload_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ai_knowledge_document (
    document_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    original_file_name VARCHAR(255) NOT NULL,
    storage_file_name VARCHAR(255) NOT NULL,
    source_format VARCHAR(16) NOT NULL,
    file_size BIGINT NOT NULL DEFAULT 0,
    title VARCHAR(255),
    summary VARCHAR(2000),
    keywords VARCHAR(1000),
    document_type VARCHAR(64),
    markdown_content LONGTEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'PROCESSING',
    error_message VARCHAR(1000),
    uploaded_by INT,
    processing_attempts INT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_ai_knowledge_filename (original_file_name),
    INDEX idx_ai_knowledge_status (status),
    INDEX idx_ai_knowledge_updated (updated_at)
);
