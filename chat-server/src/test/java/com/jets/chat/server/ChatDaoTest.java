package com.jets.chat.server;

import com.jets.chat.server.dao.ChatDao;
import com.jets.chat.server.dao.impl.ChatDaoImpl;
import com.jets.chat.server.entity.Chat;
import com.jets.chat.server.entity.ChatGroup;
import com.jets.chat.server.entity.ChatParticipant;
import com.jets.chat.common.enums.ChatType;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChatDaoTest {

    private HikariDataSource dataSource;
    private ChatDao chatDao;
    private long createdPrivateChatId;
    private long createdGroupChatId;
    private long createdChatId;

    @BeforeAll
    void setup() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:chat_test1;MODE=MySQL;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(50);
        dataSource = new HikariDataSource(config);

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {

            // Create users table
            stmt.execute("CREATE TABLE users (" + "user_id BIGINT PRIMARY KEY AUTO_INCREMENT, "
                    + "display_name VARCHAR(100), " + "email VARCHAR(100), "
                    + "password VARCHAR(255))");

            // Create chats table
            stmt.execute("CREATE TABLE chats (" + "chat_id BIGINT PRIMARY KEY AUTO_INCREMENT, "
                    + "chat_type VARCHAR(10) NOT NULL, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // Create chat_participants table
            stmt.execute("CREATE TABLE chat_participants (" + "chat_id BIGINT, "
                    + "user_id BIGINT, " + "joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "PRIMARY KEY (chat_id, user_id), "
                    + "FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE, "
                    + "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE)");

            // Create chat_groups table
            stmt.execute("CREATE TABLE chat_groups (" + "chat_id BIGINT PRIMARY KEY, "
                    + "group_name VARCHAR(100) NOT NULL, " + "owner_id BIGINT NOT NULL, "
                    + "FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE, "
                    + "FOREIGN KEY (owner_id) REFERENCES users(user_id) ON DELETE CASCADE)");

            // Insert test users
            for (int i = 1; i <= 20; i++) {
                stmt.execute(String.format("INSERT INTO users (display_name, email, password) "
                        + "VALUES ('User%d', 'user%d@test.com', 'pass%d')", i, i, i));
            }

            // Insert a test chat for basic operations
            stmt.execute("INSERT INTO chats (chat_type) VALUES ('GROUP')");
            stmt.execute(
                    "INSERT INTO chat_groups (chat_id, group_name, owner_id) VALUES (1, 'Existing Group', 1)");
            stmt.execute("INSERT INTO chat_participants (chat_id, user_id) VALUES (1, 1)");
            stmt.execute("INSERT INTO chat_participants (chat_id, user_id) VALUES (1, 2)");

            createdChatId = 1;
        }

        chatDao = new ChatDaoImpl(dataSource);
    }

    @AfterAll
    void tearDown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Happy Path: Insert chat and retrieve by ID")
    void testInsertChatAndFindById() {
        // Insert a new chat
        long chatId = chatDao.insertChat(ChatType.PRIVATE);
        assertTrue(chatId > 0);

        // Find the chat by ID
        Optional<Chat> foundChat = chatDao.findById(chatId);

        assertTrue(foundChat.isPresent());
        assertEquals(chatId, foundChat.get().getChatId());
        assertEquals(ChatType.PRIVATE, foundChat.get().getChatType());
        assertNotNull(foundChat.get().getCreatedAt());

        createdPrivateChatId = chatId;
    }

    @Test
    @Order(2)
    @DisplayName("Happy Path: Insert group and get group info")
    void testInsertGroupAndGetGroupInfo() {
        // First insert a chat for the group
        long chatId = chatDao.insertChat(ChatType.GROUP);
        assertTrue(chatId > 0);

        // Insert group info
        chatDao.insertGroup(chatId, "Test Group", 1L);

        // Get group info
        Optional<ChatGroup> groupInfo = chatDao.getGroupInfo(chatId);

        assertTrue(groupInfo.isPresent());
        assertEquals(chatId, groupInfo.get().getChatId());
        assertEquals("Test Group", groupInfo.get().getGroupName());
        assertEquals(1L, groupInfo.get().getOwnerId());

        createdGroupChatId = chatId;
    }

    @Test
    @Order(3)
    @DisplayName("Happy Path: Add participant to chat")
    void testAddParticipant() {
        boolean success = chatDao.addParticipant(createdPrivateChatId, 3L);
        assertTrue(success);

        // Verify participant was added
        List<ChatParticipant> participants = chatDao.getParticipants(createdPrivateChatId);
        assertFalse(participants.isEmpty());
        assertTrue(participants.stream().anyMatch(p -> p.getUserId() == 3L));
    }

    @Test
    @Order(4)
    @DisplayName("Happy Path: Remove participant from chat")
    void testRemoveParticipant() {
        // First add a participant
        chatDao.addParticipant(createdPrivateChatId, 4L);

        // Then remove them
        boolean success = chatDao.removeParticipant(createdPrivateChatId, 4L);
        assertTrue(success);

        // Verify participant was removed
        List<ChatParticipant> participants = chatDao.getParticipants(createdPrivateChatId);
        assertFalse(participants.stream().anyMatch(p -> p.getUserId() == 4L));
    }

    @Test
    @Order(5)
    @DisplayName("Happy Path: Get participants for chat")
    void testGetParticipants() {
        // Add multiple participants
        chatDao.addParticipant(createdPrivateChatId, 5L);
        chatDao.addParticipant(createdPrivateChatId, 6L);

        List<ChatParticipant> participants = chatDao.getParticipants(createdPrivateChatId);

        assertFalse(participants.isEmpty());
        assertEquals(3, participants.size()); // Should have 3 participants now
        assertTrue(participants.stream().allMatch(p -> p.getChatId() == createdPrivateChatId));
        assertNotNull(participants.get(0).getJoinedAt());
    }

    @Test
    @Order(6)
    @DisplayName("Happy Path: Find chats by user")
    void testFindChatsByUser() {
        // Create another chat and add user 7 to it
        long anotherChatId = chatDao.insertChat(ChatType.PRIVATE);
        chatDao.addParticipant(anotherChatId, 7L);
        chatDao.addParticipant(anotherChatId, 8L);

        List<Chat> userChats = chatDao.findChatsByUser(7L);

        assertFalse(userChats.isEmpty());
        assertTrue(userChats.stream().anyMatch(c -> c.getChatId() == anotherChatId));
        assertEquals(ChatType.PRIVATE, userChats.get(0).getChatType());
    }

    @Test
    @Order(7)
    @DisplayName("Happy Path: Rename group")
    void testRenameGroup() {
        String newName = "Renamed Group " + System.currentTimeMillis();
        boolean success = chatDao.renameGroup(createdGroupChatId, newName);
        assertTrue(success);

        // Verify rename
        Optional<ChatGroup> groupInfo = chatDao.getGroupInfo(createdGroupChatId);
        assertTrue(groupInfo.isPresent());
        assertEquals(newName, groupInfo.get().getGroupName());
    }

    @Test
    @Order(8)
    @DisplayName("Happy Path: Change group owner")
    void testChangeGroupOwner() {
        boolean success = chatDao.changeGroupOwner(createdGroupChatId, 2L);
        assertTrue(success);

        // Verify owner change
        Optional<ChatGroup> groupInfo = chatDao.getGroupInfo(createdGroupChatId);
        assertTrue(groupInfo.isPresent());
        assertEquals(2L, groupInfo.get().getOwnerId());
    }

    @Test
    @Order(9)
    @DisplayName("Sad Path: Find non-existent chat")
    void testFindByIdNotFound() {
        Optional<Chat> chat = chatDao.findById(999999L);
        assertFalse(chat.isPresent());
    }

    @Test
    @Order(10)
    @DisplayName("Sad Path: Get group info for non-group chat")
    void testGetGroupInfoForNonGroupChat() {
        Optional<ChatGroup> groupInfo = chatDao.getGroupInfo(createdPrivateChatId);
        assertFalse(groupInfo.isPresent());
    }

    @Test
    @Order(11)
    @DisplayName("Sad Path: Add participant to non-existent chat")
    void testAddParticipantToNonExistentChat() {
        // This should throw RuntimeException as per your implementation
        assertThrows(RuntimeException.class, () -> {
            chatDao.addParticipant(999999L, 1L);
        });
    }

    @Test
    @Order(12)
    @DisplayName("Concurrency: Multiple chat insertions")
    void testConcurrentChatInsertions() throws InterruptedException {
        int threadCount = 30;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        Set<Long> generatedChatIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
        AtomicInteger successfulInserts = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    ChatType type = (ThreadLocalRandom.current().nextBoolean())
                            ? ChatType.PRIVATE
                            : ChatType.GROUP;
                    long chatId = chatDao.insertChat(type);
                    generatedChatIds.add(chatId);
                    successfulInserts.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean finished = endLatch.await(10, TimeUnit.SECONDS);

        assertTrue(finished, "Concurrent test timed out");
        assertEquals(threadCount, successfulInserts.get());
        assertEquals(threadCount, generatedChatIds.size());

        executor.shutdown();
    }

    @Test
    @Order(13)
    @DisplayName("Concurrency: Mixed operations on same chat")
    void testConcurrentMixedOperations() throws InterruptedException {
        int tasks = 40;
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch latch = new CountDownLatch(tasks);

        for (int i = 0; i < tasks; i++) {
            final int taskType = i % 4;
            final long userId = (i % 5) + 10; // Use users 10-14

            executor.submit(() -> {
                try {
                    switch (taskType) {
                        case 0 :
                            // Add participant
                            chatDao.addParticipant(createdChatId, userId);
                            break;
                        case 1 :
                            // Remove participant (may fail if not exists)
                            try {
                                chatDao.removeParticipant(createdChatId, userId);
                            } catch (RuntimeException e) {
                                // Expected if participant doesn't exist
                            }
                            break;
                        case 2 :
                            // Get participants
                            chatDao.getParticipants(createdChatId);
                            break;
                        case 3 :
                            // Get group info
                            chatDao.getGroupInfo(createdChatId);
                            break;
                    }
                } catch (Exception e) {
                    // Some operations may fail due to constraints
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        // Verify chat still exists and has participants
        Optional<Chat> chat = chatDao.findById(createdChatId);
        assertTrue(chat.isPresent());

        executor.shutdown();
    }

    @Test
    @Order(14)
    @DisplayName("Concurrency: Race condition test - multiple group updates")
    void testConcurrentGroupUpdates() throws InterruptedException {
        int updateThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(updateThreads);

        // Create a new group for this test
        long testGroupChatId = chatDao.insertChat(ChatType.GROUP);
        chatDao.insertGroup(testGroupChatId, "Concurrent Test Group", 1L);

        for (int i = 0; i < updateThreads; i++) {
            final int threadNum = i;
            executor.submit(() -> {
                try {
                    // Half rename, half change owner
                    if (threadNum % 2 == 0) {
                        chatDao.renameGroup(testGroupChatId, "Updated " + threadNum);
                    } else {
                        chatDao.changeGroupOwner(testGroupChatId, (threadNum % 5) + 1L);
                    }
                } catch (Exception e) {
                    // Some updates may fail due to concurrent modifications
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        // Verify group still exists
        Optional<ChatGroup> groupInfo = chatDao.getGroupInfo(testGroupChatId);
        assertTrue(groupInfo.isPresent());

        executor.shutdown();
    }

    @Test
    @Order(15)
    @DisplayName("Data Integrity: Verify foreign key constraints")
    void testForeignKeyConstraints() {
        // Try to add participant with non-existent user ID
        assertThrows(RuntimeException.class, () -> {
            chatDao.addParticipant(createdPrivateChatId, 99999L);
        });

        // Try to create group with non-existent owner
        long newChatId = chatDao.insertChat(ChatType.GROUP);
        assertThrows(RuntimeException.class, () -> {
            chatDao.insertGroup(newChatId, "Invalid Group", 99999L);
        });
    }

    @Test
    @Order(16)
    @DisplayName("Data Integrity: Verify unique constraints")
    void testUniqueConstraints() {
        // Add same participant twice should throw exception
        chatDao.addParticipant(createdPrivateChatId, 15L);
        assertThrows(RuntimeException.class, () -> {
            chatDao.addParticipant(createdPrivateChatId, 15L);
        });
    }

    @Test
    @Order(17)
    @DisplayName("Performance: Bulk participant operations")
    void testBulkParticipantOperations() {
        long startTime = System.currentTimeMillis();
        int participantCount = 20;

        // Create a new chat for bulk test
        long bulkChatId = chatDao.insertChat(ChatType.GROUP);
        chatDao.insertGroup(bulkChatId, "Bulk Test Group", 1L);

        // Add many participants
        for (int i = 1; i <= participantCount; i++) {
            chatDao.addParticipant(bulkChatId, (long) i);
        }

        // Verify all participants were added
        List<ChatParticipant> participants = chatDao.getParticipants(bulkChatId);
        assertEquals(participantCount, participants.size());

        long endTime = System.currentTimeMillis();
        System.out.printf("Bulk operations completed in %d ms%n", (endTime - startTime));
    }
}