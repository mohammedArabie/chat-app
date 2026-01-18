package com.jets.chat.server.dao;

import com.jets.chat.common.enums.Gender;
import com.jets.chat.common.enums.UserStatus;
import com.jets.chat.server.dao.impl.UserDaoImpl;
import com.jets.chat.server.entity.User;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDaoImplTest {

    private HikariDataSource dataSource;
    private UserDaoImpl userDao;

    private static final String CREATE_USERS_TABLE = "CREATE TABLE users ("
            + "user_id BIGINT PRIMARY KEY AUTO_INCREMENT, "
            + "phone_number VARCHAR(20) UNIQUE NOT NULL, " + "display_name VARCHAR(100) NOT NULL, "
            + "email VARCHAR(255) UNIQUE, " + "password_hash VARCHAR(255) NOT NULL, "
            + "gender VARCHAR(10) NOT NULL, " + "country VARCHAR(100), " + "date_of_birth DATE, "
            + "bio TEXT, " + "picture_path VARCHAR(500), "
            + "chatbot_enabled BOOLEAN DEFAULT FALSE, "
            + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

    private static final String CREATE_USER_STATUS_TABLE = "CREATE TABLE user_status ("
            + "user_id BIGINT PRIMARY KEY, " + "status VARCHAR(20) NOT NULL, "
            + "last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
            + "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE)";

    private static final String CREATE_USER_SESSIONS_TABLE = "CREATE TABLE user_sessions ("
            + "session_id VARCHAR(255) PRIMARY KEY, " + "user_id BIGINT NOT NULL, "
            + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
            + "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE)";

    @BeforeAll
    void initDatabase() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:user_test;MODE=MySQL;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(10);
        config.setAutoCommit(true);

        dataSource = new HikariDataSource(config);

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_USERS_TABLE);
            stmt.execute(CREATE_USER_STATUS_TABLE);
            stmt.execute(CREATE_USER_SESSIONS_TABLE);
        }

        userDao = new UserDaoImpl(dataSource);
    }

    @BeforeEach
    void cleanDatabase() throws Exception {
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM user_sessions");
            stmt.execute("DELETE FROM user_status");
            stmt.execute("DELETE FROM users");
        }
    }

    @AfterAll
    void tearDown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private User createValidUser(String phoneNumber, String email) {
        User user = new User();
        user.setPhoneNumber(phoneNumber);
        user.setDisplayName("John Doe");
        user.setEmail(email);
        user.setPasswordHash("$2a$10$hashedPasswordExample123456");
        user.setGender(Gender.MALE);
        user.setCountry("USA");
        user.setDateOfBirth(Date.valueOf(LocalDate.of(1990, 5, 15)));
        user.setBio("This is a test bio");
        user.setPicturePath("/images/profile/user123.jpg");
        user.setChatbotEnabled(true);
        return user;
    }

    private int countRowsInTable(String tableName) throws SQLException {
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private boolean userExistsInDb(long userId) throws SQLException {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn
                        .prepareStatement("SELECT 1 FROM users WHERE user_id = ?")) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private String getUserStatusFromDb(long userId) throws SQLException {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn
                        .prepareStatement("SELECT status FROM user_status WHERE user_id = ?")) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("status");
                }
                return null;
            }
        }
    }

    private Timestamp getLastSeenFromDb(long userId) throws SQLException {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn
                        .prepareStatement("SELECT last_seen FROM user_status WHERE user_id = ?")) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp("last_seen");
                }
                return null;
            }
        }
    }

    @Test
    @Order(1)
    @DisplayName("testSave_HappyPath: Save user and verify DB state")
    void testSave_HappyPath() throws SQLException {
        User user = createValidUser("+1234567890", "john.doe@example.com");

        User savedUser = userDao.save(user);

        assertNotNull(savedUser, "Saved user should not be null");
        assertTrue(savedUser.getUserId() > 0, "User ID should be generated and > 0");

        assertTrue(userExistsInDb(savedUser.getUserId()), "User should exist in database");

        String status = getUserStatusFromDb(savedUser.getUserId());
        assertNotNull(status, "User status should exist");
        assertEquals("OFFLINE", status, "Initial status should be OFFLINE");

        Optional<User> retrieved = userDao.findById(savedUser.getUserId());
        assertTrue(retrieved.isPresent());
        User retrievedUser = retrieved.get();

        assertEquals("+1234567890", retrievedUser.getPhoneNumber());
        assertEquals("john.doe@example.com", retrievedUser.getEmail());
        assertEquals(Gender.MALE, retrievedUser.getGender());
        assertEquals("USA", retrievedUser.getCountry());
        assertEquals(Date.valueOf(LocalDate.of(1990, 5, 15)), retrievedUser.getDateOfBirth());
        assertTrue(retrievedUser.isChatbotEnabled());
    }

    @Test
    @Order(2)
    @DisplayName("testSave_DuplicatePhoneNumber: Attempt to save user with duplicate phone")
    void testSave_DuplicatePhoneNumber() {
        User user1 = createValidUser("+1234567890", "user1@example.com");
        User user2 = createValidUser("+1234567890", "user2@example.com"); // Same phone

        User savedUser1 = userDao.save(user1);
        assertNotNull(savedUser1, "First user should be saved successfully");

        User savedUser2 = userDao.save(user2);

        assertNull(savedUser2, "Second user with duplicate phone should return null");
    }

    @Test
    @Order(3)
    @DisplayName("testSave_DuplicateEmail: Attempt to save user with duplicate email")
    void testSave_DuplicateEmail() {
        User user1 = createValidUser("+1234567890", "duplicate@example.com");
        User user2 = createValidUser("+0987654321", "duplicate@example.com"); // Same email

        User savedUser1 = userDao.save(user1);
        assertNotNull(savedUser1, "First user should be saved successfully");

        User savedUser2 = userDao.save(user2);

        assertNull(savedUser2, "Second user with duplicate email should return null");
    }

    @Test
    @Order(4)
    @DisplayName("testSave_TransactionRollback: Verify rollback on user_status insert failure")
    void testSave_TransactionRollback() throws SQLException {
        User user = createValidUser("+1234567890", "rollback@example.com");

        int userCountBefore = countRowsInTable("users");

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE user_status");
        }

        User savedUser = userDao.save(user);

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_USER_STATUS_TABLE);
        }

        assertNull(savedUser, "Save should return null when user_status insert fails");

        int userCountAfter = countRowsInTable("users");
        assertEquals(userCountBefore, userCountAfter,
                "User count should remain same - transaction should be rolled back");
    }

    @Test
    @Order(5)
    @DisplayName("testFindById_Found: Retrieve user and verify all fields")
    void testFindById_Found() {
        User user = createValidUser("+1234567890", "find@example.com");
        user.setBio("Detailed bio for mapping test");
        user.setPicturePath("/path/to/picture.jpg");
        User savedUser = userDao.save(user);
        assertNotNull(savedUser);

        Optional<User> result = userDao.findById(savedUser.getUserId());

        assertTrue(result.isPresent(), "User should be found");
        User foundUser = result.get();

        assertEquals(savedUser.getUserId(), foundUser.getUserId(), "User ID mismatch");
        assertEquals("+1234567890", foundUser.getPhoneNumber(), "Phone number mismatch");
        assertEquals("John Doe", foundUser.getDisplayName(), "Display name mismatch");
        assertEquals("find@example.com", foundUser.getEmail(), "Email mismatch");
        assertEquals("$2a$10$hashedPasswordExample123456", foundUser.getPasswordHash(),
                "Password hash mismatch");
        assertEquals(Gender.MALE, foundUser.getGender(), "Gender enum mismatch");
        assertEquals("USA", foundUser.getCountry(), "Country mismatch");
        assertEquals(Date.valueOf(LocalDate.of(1990, 5, 15)), foundUser.getDateOfBirth(),
                "Date of birth mismatch");
        assertEquals("Detailed bio for mapping test", foundUser.getBio(), "Bio mismatch");
        assertEquals("/path/to/picture.jpg", foundUser.getPicturePath(), "Picture path mismatch");
        assertTrue(foundUser.isChatbotEnabled(), "Chatbot enabled mismatch");
        assertNotNull(foundUser.getCreatedAt(), "Created at should not be null");
    }

    @Test
    @Order(6)
    @DisplayName("testFindById_NotFound: Search for non-existent user")
    void testFindById_NotFound() {
        Optional<User> result = userDao.findById(999999L);

        assertFalse(result.isPresent(), "Optional should be empty for non-existent ID");
    }

    @Test
    @Order(7)
    @DisplayName("testFindByPhoneNumber_Found: Retrieve user by phone number")
    void testFindByPhoneNumber_Found() {
        User user = createValidUser("+1122334455", "phone@example.com");
        User savedUser = userDao.save(user);
        assertNotNull(savedUser);

        Optional<User> result = userDao.findByPhoneNumber("+1122334455");

        assertTrue(result.isPresent(), "User should be found by phone number");
        User foundUser = result.get();
        assertEquals(savedUser.getUserId(), foundUser.getUserId());
        assertEquals("+1122334455", foundUser.getPhoneNumber());
        assertEquals("phone@example.com", foundUser.getEmail());
    }

    @Test
    @Order(8)
    @DisplayName("testFindByPhoneNumber_NotFound: Search with non-existent phone")
    void testFindByPhoneNumber_NotFound() {
        Optional<User> result = userDao.findByPhoneNumber("+9999999999");

        assertFalse(result.isPresent(), "Optional should be empty for non-existent phone");
    }

    @Test
    @Order(9)
    @DisplayName("testUpdate_UserProfile: Update multiple fields and verify")
    void testUpdate_UserProfile() throws SQLException {
        User user = createValidUser("+1234567890", "update@example.com");
        User savedUser = userDao.save(user);
        assertNotNull(savedUser);

        User persistedUser = userDao.findById(savedUser.getUserId()).orElseThrow();
        Timestamp originalCreatedAt = persistedUser.getCreatedAt();
        String originalPassword = persistedUser.getPasswordHash();

        persistedUser.setDisplayName("Jane Smith");
        persistedUser.setGender(Gender.FEMALE);
        persistedUser.setBio("Updated bio text");
        persistedUser.setCountry("Canada");
        persistedUser.setDateOfBirth(Date.valueOf(LocalDate.of(1995, 8, 20)));
        persistedUser.setPicturePath("/new/path/image.png");
        persistedUser.setChatbotEnabled(false);

        boolean updateResult = userDao.update(persistedUser);
        assertTrue(updateResult, "Update should return true");

        Optional<User> retrieved = userDao.findById(savedUser.getUserId());
        assertTrue(retrieved.isPresent());
        User updatedUser = retrieved.get();

        assertEquals("Jane Smith", updatedUser.getDisplayName());
        assertEquals(Gender.FEMALE, updatedUser.getGender());
        assertEquals(originalCreatedAt, updatedUser.getCreatedAt(),
                "Created at timestamp should not change");
        assertEquals(originalPassword, updatedUser.getPasswordHash(),
                "Password should not change during profile update");
    }

    @Test
    @Order(10)
    @DisplayName("testUpdate_Password: Update password and verify")
    void testUpdate_Password() {
        User user = createValidUser("+1234567890", "password@example.com");
        User savedUser = userDao.save(user);
        assertNotNull(savedUser);

        String newPasswordHash = "$2a$10$newHashedPasswordExample789";

        boolean updateResult = userDao.updatePassword(savedUser.getUserId(), newPasswordHash);

        assertTrue(updateResult, "Password update should return true");

        Optional<User> retrieved = userDao.findById(savedUser.getUserId());
        assertTrue(retrieved.isPresent());
        assertEquals(newPasswordHash, retrieved.get().getPasswordHash());
    }

    @Test
    @Order(11)
    @DisplayName("testUpdate_Status_And_Timestamp: Update status and verify last_seen")
    void testUpdate_Status_And_Timestamp() throws SQLException {
        User user = createValidUser("+1234567890", "status@example.com");
        User savedUser = userDao.save(user);
        assertNotNull(savedUser);

        String initialStatus = getUserStatusFromDb(savedUser.getUserId());
        assertEquals("OFFLINE", initialStatus);

        Timestamp initialLastSeen = getLastSeenFromDb(savedUser.getUserId());
        assertNotNull(initialLastSeen);

        boolean updateResult = userDao.updateStatus(savedUser.getUserId(), UserStatus.AVAILABLE);

        assertTrue(updateResult, "Status update should return true");

        String updatedStatus = getUserStatusFromDb(savedUser.getUserId());
        assertEquals("AVAILABLE", updatedStatus, "Status should be updated to ONLINE");

        Timestamp updatedLastSeen = getLastSeenFromDb(savedUser.getUserId());
        assertNotNull(updatedLastSeen);

        assertThat(updatedLastSeen).isAfterOrEqualTo(initialLastSeen);
    }

    @Test
    @Order(12)
    @DisplayName("testUpdate_NonExistentUser: Update non-existent user returns false")
    void testUpdate_NonExistentUser() {
        User user = createValidUser("+1234567890", "fake@example.com");
        user.setUserId(999999L);

        boolean updateResult = userDao.update(user);

        assertFalse(updateResult, "Update should return false for non-existent user");
    }

    @Test
    @Order(13)
    @DisplayName("testGetStatus: Retrieve user status")
    void testGetStatus() {
        User user = createValidUser("+1234567890", "getstatus@example.com");
        User savedUser = userDao.save(user);
        assertNotNull(savedUser);

        UserStatus status = userDao.getStatus(savedUser.getUserId());

        assertEquals(UserStatus.OFFLINE, status, "Initial status should be OFFLINE");

        userDao.updateStatus(savedUser.getUserId(), UserStatus.AVAILABLE);
        UserStatus updatedStatus = userDao.getStatus(savedUser.getUserId());
        assertEquals(UserStatus.AVAILABLE, updatedStatus);
    }

    @Test
    @Order(14)
    @DisplayName("testGetStatus_NonExistentUser: Get status for non-existent user")
    void testGetStatus_NonExistentUser() {
        UserStatus status = userDao.getStatus(999999L);

        assertEquals(UserStatus.OFFLINE, status,
                "Should return OFFLINE for non-existent user (default behavior)");
    }

    @Test
    @Order(15)
    @DisplayName("testCreateSession: Create session and verify in DB")
    void testCreateSession() throws SQLException {
        User user = createValidUser("+1234567890", "session@example.com");
        User savedUser = userDao.save(user);
        assertNotNull(savedUser);

        String sessionId = UUID.randomUUID().toString();

        boolean createResult = userDao.createSession(sessionId, savedUser.getUserId());

        assertTrue(createResult, "Session creation should return true");

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT user_id FROM user_sessions WHERE session_id = ?")) {
            stmt.setString(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "Session should exist in database");
                assertEquals(savedUser.getUserId(), rs.getLong("user_id"));
            }
        }
    }

    @Test
    @Order(16)
    @DisplayName("testDeleteSession: Delete session and verify removal")
    void testDeleteSession() throws SQLException {
        User user = createValidUser("+1234567890", "deletesession@example.com");
        User savedUser = userDao.save(user);
        assertNotNull(savedUser);

        String sessionId = UUID.randomUUID().toString();
        userDao.createSession(sessionId, savedUser.getUserId());

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn
                        .prepareStatement("SELECT 1 FROM user_sessions WHERE session_id = ?")) {
            stmt.setString(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "Session should exist before deletion");
            }
        }

        boolean deleteResult = userDao.deleteSession(sessionId);

        assertTrue(deleteResult, "Session deletion should return true");

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn
                        .prepareStatement("SELECT 1 FROM user_sessions WHERE session_id = ?")) {
            stmt.setString(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                assertFalse(rs.next(), "Session should not exist after deletion");
            }
        }
    }

    @Test
    @Order(17)
    @DisplayName("testDeleteSession_NonExistent: Delete non-existent session")
    void testDeleteSession_NonExistent() {
        boolean deleteResult = userDao.deleteSession("non-existent-session-id");

        assertFalse(deleteResult, "Deleting non-existent session should return false");
    }

    @Test
    @Order(18)
    @DisplayName("Edge Case: User with FEMALE gender enum")
    void testSave_FemaleGender() {
        User user = createValidUser("+1234567890", "female@example.com");
        user.setGender(Gender.FEMALE);

        User savedUser = userDao.save(user);

        assertNotNull(savedUser);
        Optional<User> retrieved = userDao.findById(savedUser.getUserId());
        assertTrue(retrieved.isPresent());
        assertEquals(Gender.FEMALE, retrieved.get().getGender());
    }

    @Test
    @Order(19)
    @DisplayName("Edge Case: User with null optional fields")
    void testSave_NullOptionalFields() {
        User user = new User();
        user.setPhoneNumber("+1234567890");
        user.setDisplayName("Minimal User");
        user.setPasswordHash("hashedPassword");
        user.setGender(Gender.MALE);
        user.setEmail(null);
        user.setCountry(null);
        user.setDateOfBirth(null);
        user.setBio(null);
        user.setPicturePath(null);
        user.setChatbotEnabled(false);

        User savedUser = userDao.save(user);

        assertNotNull(savedUser);
        Optional<User> retrieved = userDao.findById(savedUser.getUserId());
        assertTrue(retrieved.isPresent());
        User retrievedUser = retrieved.get();

        assertNull(retrievedUser.getEmail());
        assertNull(retrievedUser.getCountry());
        assertNull(retrievedUser.getDateOfBirth());
        assertNull(retrievedUser.getBio());
        assertNull(retrievedUser.getPicturePath());
    }

    @Test
    @Order(20)
    @DisplayName("Edge Case: Multiple sessions for same user")
    void testMultipleSessions_SameUser() {
        User user = createValidUser("+1234567890", "multisession@example.com");
        User savedUser = userDao.save(user);
        assertNotNull(savedUser);

        String session1 = UUID.randomUUID().toString();
        String session2 = UUID.randomUUID().toString();
        String session3 = UUID.randomUUID().toString();

        assertTrue(userDao.createSession(session1, savedUser.getUserId()));
        assertTrue(userDao.createSession(session2, savedUser.getUserId()));
        assertTrue(userDao.createSession(session3, savedUser.getUserId()));

        assertTrue(userDao.deleteSession(session1));
        assertTrue(userDao.deleteSession(session2));
        assertTrue(userDao.deleteSession(session3));
    }

    @Test
    @Order(21)
    @DisplayName("Edge Case: Update all status types")
    void testUpdateStatus_AllTypes() {
        User user = createValidUser("+1234567890", "allstatus@example.com");
        User savedUser = userDao.save(user);
        assertNotNull(savedUser);

        for (UserStatus status : UserStatus.values()) {
            assertTrue(userDao.updateStatus(savedUser.getUserId(), status));
            assertEquals(status, userDao.getStatus(savedUser.getUserId()));
        }
    }

    @Test
    @Order(22)
    @DisplayName("Data Integrity: Cascading delete on user removal")
    void testCascadingDelete() throws SQLException {
        User user = createValidUser("+1234567890", "cascade@example.com");
        User savedUser = userDao.save(user);
        assertNotNull(savedUser);

        String sessionId = UUID.randomUUID().toString();
        userDao.createSession(sessionId, savedUser.getUserId());

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn
                        .prepareStatement("DELETE FROM users WHERE user_id = ?")) {
            stmt.setLong(1, savedUser.getUserId());
            stmt.executeUpdate();
        }

        assertThat(getUserStatusFromDb(savedUser.getUserId())).isNull();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn
                        .prepareStatement("SELECT 1 FROM user_sessions WHERE session_id = ?")) {
            stmt.setString(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                assertFalse(rs.next(), "Session should be deleted via cascade");
            }
        }
    }
}