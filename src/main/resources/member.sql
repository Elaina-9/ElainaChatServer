CREATE TABLE IF NOT EXISTS member (
                  conversation_id VARCHAR(255),
                  user_id BIGINT NOT NULL,
                  nickname VARCHAR(64),  -- 在此会话中的昵称
                  role TINYINT NOT NULL DEFAULT 0 COMMENT '0:member, 1:admin, 2:owner',  -- 会话角色
                  joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  last_read_id BIGINT,  -- 最后阅读的消息ID
                  is_muted BOOLEAN DEFAULT FALSE,  -- 是否静音
                  PRIMARY KEY (conversation_id, user_id),
                  FOREIGN KEY (conversation_id) REFERENCES conversation(conversation_id)
                  ON DELETE CASCADE
);