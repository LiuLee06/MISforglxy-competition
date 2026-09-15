CREATE TABLE IF NOT EXISTS ai_conversation (
    conversation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    user_type VARCHAR(32) NOT NULL,
    title VARCHAR(255),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_ai_conversation_user (user_id, user_type)
);
CREATE TABLE IF NOT EXISTS ai_message (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(32) NOT NULL,
    content LONGTEXT,
    tool_name VARCHAR(128),
    tool_call_id VARCHAR(255),
    tool_arguments LONGTEXT,
    tool_result LONGTEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ai_message_conversation (conversation_id)
);
CREATE TABLE IF NOT EXISTS ai_pending_action (
    action_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    user_id INT NOT NULL,
    user_type VARCHAR(32) NOT NULL,
    tool_name VARCHAR(128) NOT NULL,
    arguments_json LONGTEXT NOT NULL,
    action_summary VARCHAR(1000),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    expires_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    confirmed_at DATETIME,
    executed_at DATETIME,
    INDEX idx_ai_pending_user_status (user_id, user_type, status)
);
CREATE TABLE IF NOT EXISTS ai_action_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT,
    user_id INT,
    user_type VARCHAR(32),
    tool_name VARCHAR(128),
    risk_level VARCHAR(32),
    arguments_json LONGTEXT,
    result_json LONGTEXT,
    status VARCHAR(32),
    duration_ms BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

