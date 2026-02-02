package com.jets.chat.server.dao;

import com.jets.chat.server.dao.impl.AdminDaoImpl;
import com.jets.chat.server.entity.Admin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminDaoImplTest {

    private HikariDataSource dataSource;
    private AdminDaoImpl dao;

    private final String TEST_USERNAME = "testadmin";
    private final String TEST_PASSWORD_HASH = "hashedpassword123";
    private Long createdAdminId;

    @BeforeAll
    void init() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:admin_test;MODE=MySQL;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(5);

        dataSource = new HikariDataSource(config);

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE admins (" + "admin_id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "username VARCHAR(50) UNIQUE NOT NULL, "
                    + "password_hash VARCHAR(255) NOT NULL, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " + "last_login TIMESTAMP, "
                    + "must_change_password BOOLEAN DEFAULT FALSE" + ")");
        }

        dao = new AdminDaoImpl(dataSource);
    }

    @AfterAll
    void cleanup() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Create admin should succeed with valid data")
    void testCreateAdmin() {
        boolean result = dao.createAdmin(TEST_USERNAME, TEST_PASSWORD_HASH);

        assertTrue(result, "Admin creation should succeed");

        Optional<Admin> admin = dao.findByUsername(TEST_USERNAME);
        assertTrue(admin.isPresent(), "Admin should exist after creation");

        Admin created = admin.get();
        assertEquals(TEST_USERNAME, created.getUsername());
        assertEquals(TEST_PASSWORD_HASH, created.getPasswordHash());
        assertNotNull(created.getCreatedAt());
        assertNull(created.getLastLogin());

        // Since we can see the Admin entity has isMustChangePassword() method,
        // we can directly assert it
        assertTrue(created.isMustChangePassword(),
                "New admin should have must_change_password = TRUE");

        createdAdminId = created.getAdminId();
        assertNotNull(createdAdminId);
    }

    @Test
    @Order(2)
    @DisplayName("Find by username returns correct admin")
    void testFindByUsername() {
        // Skip if admin wasn't created
        if (createdAdminId == null) {
            System.out.println("Skipping test - admin not created");
            return;
        }

        Optional<Admin> admin = dao.findByUsername(TEST_USERNAME);

        assertTrue(admin.isPresent());
        Admin found = admin.get();

        assertEquals(TEST_USERNAME, found.getUsername());
        assertEquals(TEST_PASSWORD_HASH, found.getPasswordHash());
        assertEquals(createdAdminId, found.getAdminId());
        assertTrue(found.isMustChangePassword());
    }

    @Test
    @Order(3)
    @DisplayName("Find by ID returns correct admin")
    void testFindById() {
        // Skip if admin wasn't created
        if (createdAdminId == null) {
            System.out.println("Skipping test - admin not created");
            return;
        }

        Optional<Admin> admin = dao.findById(createdAdminId);

        assertTrue(admin.isPresent());
        Admin found = admin.get();

        assertEquals(TEST_USERNAME, found.getUsername());
        assertEquals(TEST_PASSWORD_HASH, found.getPasswordHash());
        assertEquals(createdAdminId, found.getAdminId());
        assertTrue(found.isMustChangePassword());
    }

    @Test
    @Order(4)
    @DisplayName("Find by username for non-existent admin returns empty")
    void testFindByUsernameNonExistent() {
        Optional<Admin> admin = dao.findByUsername("nonexistent");
        assertTrue(admin.isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("Find by ID for non-existent admin returns empty")
    void testFindByIdNonExistent() {
        Optional<Admin> admin = dao.findById(99999L);
        assertTrue(admin.isEmpty());
    }

    @Test
    @Order(6)
    @DisplayName("Update must_change_password should succeed")
    void testUpdateMustChangePassword() {
        // Skip if admin wasn't created
        if (createdAdminId == null) {
            System.out.println("Skipping test - admin not created");
            return;
        }

        boolean result = dao.updateMustChangePassword(createdAdminId, false);
        assertTrue(result, "Update must_change_password should succeed");

        Optional<Admin> admin = dao.findById(createdAdminId);
        assertTrue(admin.isPresent());
        assertFalse(admin.get().isMustChangePassword());

        result = dao.updateMustChangePassword(createdAdminId, true);
        assertTrue(result);

        admin = dao.findById(createdAdminId);
        assertTrue(admin.isPresent());
        assertTrue(admin.get().isMustChangePassword());
    }

    @Test
    @Order(7)
    @DisplayName("Update last login should succeed")
    void testUpdateLastLogin() {
        // Skip if admin wasn't created
        if (createdAdminId == null) {
            System.out.println("Skipping test - admin not created");
            return;
        }

        Timestamp beforeUpdate = new Timestamp(System.currentTimeMillis());

        boolean result = dao.updateLastLogin(createdAdminId);

        assertTrue(result, "Update last login should succeed");

        Optional<Admin> admin = dao.findById(createdAdminId);
        assertTrue(admin.isPresent());

        Timestamp lastLogin = admin.get().getLastLogin();
        assertNotNull(lastLogin, "Last login should not be null after update");

        assertTrue(lastLogin.after(beforeUpdate) || lastLogin.equals(beforeUpdate));
    }

    @Test
    @Order(8)
    @DisplayName("Update password should succeed")
    void testUpdatePassword() {
        // Skip if admin wasn't created
        if (createdAdminId == null) {
            System.out.println("Skipping test - admin not created");
            return;
        }

        String newPasswordHash = "newhashedpassword456";

        boolean result = dao.updatePassword(createdAdminId, newPasswordHash);

        assertTrue(result, "Password update should succeed");

        Optional<Admin> admin = dao.findById(createdAdminId);
        assertTrue(admin.isPresent());
        assertEquals(newPasswordHash, admin.get().getPasswordHash());
    }

    @Test
    @Order(9)
    @DisplayName("Update password for non-existent admin should fail")
    void testUpdatePasswordNonExistent() {
        boolean result = dao.updatePassword(99999L, "somehash");
        assertFalse(result, "Updating non-existent admin should fail");
    }

    @Test
    @Order(10)
    @DisplayName("Update last login for non-existent admin should fail")
    void testUpdateLastLoginNonExistent() {
        boolean result = dao.updateLastLogin(99999L);
        assertFalse(result, "Updating non-existent admin should fail");
    }

    @Test
    @Order(11)
    @DisplayName("Update must_change_password for non-existent admin should fail")
    void testUpdateMustChangePasswordNonExistent() {
        boolean result = dao.updateMustChangePassword(99999L, true);
        assertFalse(result, "Updating non-existent admin should fail");
    }

    @Test
    @Order(12)
    @DisplayName("Create admin with duplicate username should fail")
    void testCreateAdminDuplicateUsername() {
        // Skip if admin wasn't created
        if (createdAdminId == null) {
            System.out.println("Skipping test - admin not created");
            return;
        }

        boolean result = dao.createAdmin(TEST_USERNAME, "anotherhash");
        assertFalse(result, "Creating duplicate admin should fail");
    }

    @Test
    @Order(13)
    @DisplayName("Generate random password returns valid password")
    void testGenerateRandomPassword() {
        int length = 12;

        String password = dao.generateRandomPassword(length);

        assertNotNull(password);
        assertEquals(length, password.length());

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*].*");

        assertTrue(hasUpper || hasLower || hasDigit || hasSpecial,
                "Password should have some complexity");
    }

    @Test
    @Order(14)
    @DisplayName("Admin creation timestamp is set")
    void testAdminCreationTimestamp() throws InterruptedException {
        String uniqueUser = "timestamp_test_" + System.currentTimeMillis();
        Timestamp beforeCreation = new Timestamp(System.currentTimeMillis());

        Thread.sleep(10);

        dao.createAdmin(uniqueUser, "hash123");

        Thread.sleep(10);

        Timestamp afterCreation = new Timestamp(System.currentTimeMillis());

        Optional<Admin> admin = dao.findByUsername(uniqueUser);
        assertTrue(admin.isPresent());

        Timestamp createdAt = admin.get().getCreatedAt();
        assertNotNull(createdAt);

        assertTrue(createdAt.after(beforeCreation) || createdAt.equals(beforeCreation));
        assertTrue(createdAt.before(afterCreation) || createdAt.equals(afterCreation));
    }

    @Test
    @Order(15)
    @DisplayName("Multiple admin creation works")
    void testMultipleAdmins() {
        String[] usernames = {"admin1", "admin2", "admin3"};

        for (int i = 0; i < usernames.length; i++) {
            String username = usernames[i];
            String passwordHash = "hash_" + i;

            boolean created = dao.createAdmin(username, passwordHash);
            assertTrue(created, "Should create admin: " + username);

            Optional<Admin> found = dao.findByUsername(username);
            assertTrue(found.isPresent(), "Should find created admin: " + username);
            assertEquals(passwordHash, found.get().getPasswordHash());
            assertTrue(found.get().isMustChangePassword(),
                    "New admin should have must_change_password = TRUE");
        }
    }

    @AfterEach
    void cleanupTestData() {
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM admins WHERE username <> 'testadmin'");
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
}