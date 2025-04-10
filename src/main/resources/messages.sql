CREATE TABLE IF NOT EXISTS messages (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          conversation_id VARCHAR(255) NOT NULL,
                          sender_id BIGINT NOT NULL,
                          receiver_id BIGINT NOT NULL,
                          message_content TEXT NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (id, conversation_id),
                          -- FOREIGN KEY (conversation_id) REFERENCES conversation(conversation_id),
                          -- ON DELETE CASCADE,此处外键和级联删除与分区有冲突，在触发器里实现级联删除
                          INDEX idx_conversation_id (conversation_id))
    PARTITION BY KEY(conversation_id) PARTITIONS 16;