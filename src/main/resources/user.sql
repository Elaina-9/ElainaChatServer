CREATE TABLE IF NOT EXISTS users (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       username VARCHAR(50) NOT NULL,
                       password VARCHAR(100) NOT NULL,
                       avatar_url VARCHAR(255),
                       email VARCHAR(100),
                       token VARCHAR(255),
                       phone VARCHAR(20),
                       status TINYINT NOT NULL DEFAULT 1 COMMENT '1:active, 0:inactive, 2:blocked',
                       last_login_time TIMESTAMP,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (id),
                       INDEX idx_status (status)
);