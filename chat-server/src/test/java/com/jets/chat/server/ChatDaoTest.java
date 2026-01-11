package com.jets.chat.server;

import com.jets.chat.server.config.DataSourceConfig;
import com.jets.chat.server.dao.ChatDao;
import com.jets.chat.server.dao.impl.ChatDaoImpl;
import com.jets.chat.server.entity.Chat;
import com.jets.chat.server.entity.ChatGroup;
import com.jets.chat.server.entity.ChatParticipant;
import com.jets.chat.server.entity.ChatType;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChatDaoTest {

    private ChatDao chatDao;
    private DataSource dataSource;
    private static long privateChatId;
    private static long groupChatId;
    private static long testUserId1, testUserId2, testUserId3;

    // ===================== SETUP =====================
    @BeforeEach
    void setUp() throws SQLException {
        dataSource = DataSourceConfig.getDataSource();
        chatDao = new ChatDaoImpl(dataSource);

        // Clean up before each test
        cleanTestData();

        // Insert fresh test users for each test
        testUserId1 = insertTestUser("111", "User One", "MALE");
        testUserId2 = insertTestUser("222", "User Two", "FEMALE");
        testUserId3 = insertTestUser("333", "User Three", "MALE");
    }

    @AfterEach
    void tearDown() throws SQLException {
        cleanTestData();
    }

    // ===================== TESTS =====================
    @Test
    @Order(1)
    void testCreatePrivateChat() {
        Chat chat = chatDao.createPrivateChat(testUserId1, testUserId2);
        assertNotNull(chat);
        assertEquals(ChatType.PRIVATE, chat.getChatType());
        privateChatId = chat.getChatId();
    }

    @Test
    @Order(2)
    void testFindChatById() {
        testCreatePrivateChat(); // Ensure chat exists
        Optional<Chat> chat = chatDao.findById(privateChatId);
        assertTrue(chat.isPresent());
        assertEquals(ChatType.PRIVATE, chat.get().getChatType());
    }

    @Test
    @Order(3)
    void testFindChatsByUser() {
        testCreatePrivateChat(); // Ensure chat exists
        List<Chat> chats = chatDao.findChatsByUser(testUserId1);
        assertFalse(chats.isEmpty());
        assertEquals(1, chats.size());
    }

    @Test
    @Order(4)
    void testCreateGroupChat() {
        Chat chat = chatDao.createGroupChat("Study Group", testUserId1,
                List.of(testUserId2, testUserId3));
        assertNotNull(chat);
        assertEquals(ChatType.GROUP, chat.getChatType());
        groupChatId = chat.getChatId();
    }

    @Test
    @Order(5)
    void testGetGroupInfo() {
        testCreateGroupChat(); // Ensure group exists
        Optional<ChatGroup> group = chatDao.getGroupInfo(groupChatId);
        assertTrue(group.isPresent());
        assertEquals("Study Group", group.get().getGroupName());
        assertEquals(testUserId1, group.get().getOwnerId());
    }

    @Test
    @Order(6)
    void testGetParticipants() {
        testCreateGroupChat(); // Ensure group exists
        List<ChatParticipant> participants = chatDao.getParticipants(groupChatId);
        assertEquals(3, participants.size());
    }

    @Test
    @Order(7)
    void testRenameGroup() {
        testCreateGroupChat(); // Ensure group exists
        chatDao.renameGroup(groupChatId, "New Group Name");
        Optional<ChatGroup> group = chatDao.getGroupInfo(groupChatId);
        assertTrue(group.isPresent());
        assertEquals("New Group Name", group.get().getGroupName());
    }

    @Test
    @Order(8)
    void testChangeGroupOwner() {
        testCreateGroupChat(); // Ensure group exists
        chatDao.changeGroupOwner(groupChatId, testUserId2);
        Optional<ChatGroup> group = chatDao.getGroupInfo(groupChatId);
        assertTrue(group.isPresent());
        assertEquals(testUserId2, group.get().getOwnerId());
    }

    @Test
    @Order(9)
    void testRemoveParticipant() {
        testCreateGroupChat(); // Ensure group exists
        chatDao.removeParticipant(groupChatId, testUserId3);
        List<ChatParticipant> participants = chatDao.getParticipants(groupChatId);
        assertEquals(2, participants.size());
    }

    @Test
    @Order(10)
    void testAddParticipantToExistingGroup() {
        testCreateGroupChat(); // Ensure group exists
        chatDao.removeParticipant(groupChatId, testUserId3); // Remove first
        chatDao.addParticipant(groupChatId, testUserId3); // Add back
        List<ChatParticipant> participants = chatDao.getParticipants(groupChatId);
        assertEquals(3, participants.size());
    }

    // ===================== HELPER METHODS =====================
    private long insertTestUser(String phoneNumber, String displayName, String gender)
            throws SQLException {
        String sql = """
                INSERT INTO users (phone_number, display_name, password_hash, gender, created_at)
                VALUES (?, ?, ?, ?, NOW())
                """;

        String getLastIdSql = "SELECT LAST_INSERT_ID()";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             PreparedStatement idPs = conn.prepareStatement(getLastIdSql)) {

            ps.setString(1, phoneNumber);
            ps.setString(2, displayName);
            ps.setString(3, "hashed_password_123");
            ps.setString(4, gender);
            ps.executeUpdate();

            try (var rs = idPs.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new RuntimeException("Failed to insert test user");
    }

    private void cleanTestData() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            // Delete in correct order to respect foreign keys
            deleteFromTable(conn, "chat_participants");
            deleteFromTable(conn, "chat_groups");
            deleteFromTable(conn, "chats");

            // Find and delete test users by phone number
            deleteTestUsers(conn);
        }
    }

    private void deleteFromTable(Connection conn, String tableName) throws SQLException {
        String sql = "DELETE FROM " + tableName
                + " WHERE created_at > DATE_SUB(NOW(), INTERVAL 1 HOUR)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            // Table might not exist, ignore
        }
    }

    private void deleteTestUsers(Connection conn) throws SQLException {
        String sql = "DELETE FROM users WHERE phone_number IN (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "111");
            ps.setString(2, "222");
            ps.setString(3, "333");
            ps.executeUpdate();
        }
    }
}