CREATE TABLE IF NOT EXISTS conversation (
                   conversation_id VARCHAR(255) PRIMARY KEY,  -- 会话唯一标识
                   conversation_type TINYINT NOT NULL DEFAULT 0 COMMENT '0:private, 1:group',  -- 会话类型
                   title VARCHAR(255),  -- 群聊的标题
                   avatar_url VARCHAR(255),  -- 群聊的头像
                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                   last_message_time TIMESTAMP,  -- 最后一条消息的时间
                   last_message TEXT,   -- 最后一条消息的内容（用于预览）
                   status TINYINT NOT NULL DEFAULT 0 COMMENT '0:pending, 1:active'
);
