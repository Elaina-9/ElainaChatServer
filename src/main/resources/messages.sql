CREATE TABLE IF NOT EXISTS messages (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          conversation_id VARCHAR(255) NOT NULL,
                          sender_id BIGINT NOT NULL,
                          receiver_id BIGINT NOT NULL,
                          message_content TEXT NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (id, conversation_id),
                          INDEX idx_conversation_id (conversation_id))
    PARTITION BY KEY(conversation_id) PARTITIONS 16;