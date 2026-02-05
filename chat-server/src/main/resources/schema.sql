-- 1. Setup Database
DROP DATABASE IF EXISTS chat_app;
CREATE DATABASE chat_app;
USE chat_app;

-- 2. Users Table
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    gender ENUM('MALE', 'FEMALE') NOT NULL, 
    country VARCHAR(50),
    date_of_birth DATE,
    bio VARCHAR(255),
    picture_path VARCHAR(255), 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    chatbot_enabled BOOLEAN DEFAULT FALSE
);

-- 3. User Status (Presence)
CREATE TABLE user_status (
    user_id BIGINT PRIMARY KEY,
    status ENUM('AVAILABLE', 'BUSY', 'AWAY', 'OFFLINE') DEFAULT 'OFFLINE',
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 4. User Sessions (For RMI tracking & Stats)
CREATE TABLE user_sessions (
    session_id VARCHAR(64) PRIMARY KEY, -- UUID from Java
    user_id BIGINT NOT NULL,
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 5. Contacts
CREATE TABLE contacts (
    owner_id BIGINT,
    contact_id BIGINT,
    status ENUM('PENDING', 'ACCEPTED', 'BLOCKED') DEFAULT 'PENDING',
    category VARCHAR(50) DEFAULT 'Friends', -- Simple categorization
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (owner_id, contact_id),
    FOREIGN KEY (owner_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (contact_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 6. Server Announcements
CREATE TABLE announcements (
    announcement_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    font_style VARCHAR(50),
    font_color VARCHAR(20),
    is_bold BOOLEAN DEFAULT FALSE,
    is_italic BOOLEAN DEFAULT FALSE
);

-- 7. Chats (Unified Container for 1-to-1 and Group)
CREATE TABLE chats (
    chat_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_type ENUM('PRIVATE', 'GROUP') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. Group Metadata (Only for Group chats)
CREATE TABLE chat_groups (
    chat_id BIGINT PRIMARY KEY,
    group_name VARCHAR(100) NOT NULL,
    owner_id BIGINT NOT NULL,
    FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
    FOREIGN KEY (owner_id) REFERENCES users(user_id)
);

CREATE TABLE chat_participants (
    chat_id BIGINT,
    user_id BIGINT,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (chat_id, user_id),
    FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 9. Messages
CREATE TABLE messages (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    message_type ENUM('TEXT', 'FILE') DEFAULT 'TEXT',
    content TEXT, 
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- Formatting
    font_style VARCHAR(50) DEFAULT 'Arial',
    font_color VARCHAR(20) DEFAULT '#000000',
    font_size INT DEFAULT 12,
    is_bold BOOLEAN DEFAULT FALSE,
    is_italic BOOLEAN DEFAULT FALSE,
    is_underline BOOLEAN DEFAULT FALSE,
    background_color VARCHAR(20) DEFAULT '#FFFFFF',
    FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(user_id)
);

-- 10. Message Status (Tracking delivery per user)
-- Renamed receiver_id -> user_id as suggested
CREATE TABLE message_status (
    message_id BIGINT,
    user_id BIGINT, -- The user whose status this is
    is_delivered BOOLEAN DEFAULT FALSE,
    is_seen BOOLEAN DEFAULT FALSE,
    seen_at TIMESTAMP NULL,
    PRIMARY KEY (message_id, user_id),
    FOREIGN KEY (message_id) REFERENCES messages(message_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 11. Files
CREATE TABLE files (
    file_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(50),
    FOREIGN KEY (message_id) REFERENCES messages(message_id) ON DELETE CASCADE
);

-- 12. Indexes (Optimized)
CREATE INDEX idx_phone ON users(phone_number);
CREATE INDEX idx_msg_chat ON messages(chat_id);
CREATE INDEX idx_contacts_owner ON contacts(owner_id);
CREATE INDEX idx_message_sender ON messages(sender_id);
CREATE INDEX idx_chat_participants_user ON chat_participants(user_id);
CREATE INDEX idx_message_status_user ON message_status(user_id);
CREATE INDEX idx_sessions_user ON user_sessions(user_id); -- Added this one

--Adding Admin
-- Step 1: creating table
CREATE TABLE IF NOT EXISTS admins (
                                      admin_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,  -- SHA-256 hash (64 hex characters)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL DEFAULT NULL
    ) ;

-- Step 2: Add must_change_password column (if not already added)
ALTER TABLE admins
    ADD COLUMN must_change_password BOOLEAN DEFAULT TRUE AFTER password_hash;

-- Step 3: Ensure superadmin exists
INSERT INTO admins (username, password_hash, must_change_password, created_at)
VALUES (
           'superadmin',
           '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', -- SHA-256 hash of "admin123"
           FALSE,  -- Super admin doesn't have change password on first login
           NOW()
       );