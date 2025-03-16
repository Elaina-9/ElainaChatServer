CREATE TABLE IF NOT EXISTS friends (
                           id BIGINT NOT NULL AUTO_INCREMENT,
                           user_id BIGINT NOT NULL,
                           friend_id BIGINT NOT NULL,
                           status TINYINT NOT NULL DEFAULT 0 COMMENT '0:pending, 1:accepted, 2:blocked',
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (id),
                           INDEX idx_user_id (user_id),
                           INDEX idx_friend_id (friend_id),
                           INDEX idx_status (status)
);