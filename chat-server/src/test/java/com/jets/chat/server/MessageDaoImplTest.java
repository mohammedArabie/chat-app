package com.jets.chat.server;

import com.jets.chat.server.dao.MessageDao;
import com.jets.chat.server.dao.impl.MessageDaoImpl;
import com.jets.chat.server.entity.Message;
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
public class MessageDaoImplTest {

    private HikariDataSource dataSource;
    private MessageDao messageDao;
    private long createdMessageId;

    @BeforeAll
    void setup() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:chat_integration_test1;MODE=MySQL;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(20);
        dataSource = new HikariDataSource(config);

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {

            stmt.execute(
                    "CREATE TABLE users (user_id BIGINT PRIMARY KEY AUTO_INCREMENT, display_name VARCHAR(100))");
            stmt.execute("CREATE TABLE chats (chat_id BIGINT PRIMARY KEY AUTO_INCREMENT)");

            stmt.execute("CREATE TABLE messages ("
                    + "message_id BIGINT PRIMARY KEY AUTO_INCREMENT, " + "chat_id BIGINT, "
                    + "sender_id BIGINT, " + "message_type VARCHAR(10), " + "content TEXT, "
                    + "sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "font_style VARCHAR(50), font_color VARCHAR(20), font_size INT, "
                    + "is_bold TINYINT(1), is_italic TINYINT(1), is_underline TINYINT(1), "
                    + "background_color VARCHAR(20), "
                    + "FOREIGN KEY (sender_id) REFERENCES users(user_id), "
                    + "FOREIGN KEY (chat_id) REFERENCES chats(chat_id))");

            stmt.execute("INSERT INTO users (display_name) VALUES ('TestUser')");
            stmt.execute("INSERT INTO chats (chat_id) VALUES (1)");
        }

        messageDao = new MessageDaoImpl(dataSource);
    }

    @AfterAll
    void tearDown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Happy Path: Save new message entity")
    void testSave() {
        Message message = new Message();
        message.setChatId(1);
        message.setSenderId(1);
        message.setMessageType("TEXT");
        message.setContent("Hello, this is a test message");
        message.setBold(true);

        Message savedMessage = messageDao.save(message);

        assertNotNull(savedMessage);
        assertTrue(savedMessage.getMessageId() > 0);
        assertNotNull(savedMessage.getSentAt());
        assertEquals("Hello, this is a test message", savedMessage.getContent());

        createdMessageId = savedMessage.getMessageId();
    }

    @Test
    @Order(2)
    @DisplayName("Happy Path: Find message by ID")
    void testFindById() {
        Optional<Message> found = messageDao.findById(createdMessageId);

        assertTrue(found.isPresent());
        assertEquals(createdMessageId, found.get().getMessageId());
        assertEquals(1, found.get().getChatId());
    }

    @Test
    @Order(3)
    @DisplayName("Happy Path: Update existing message")
    void testUpdate() {
        Message message = messageDao.findById(createdMessageId).get();

        message.setContent("Updated content");
        message.setItalic(true);
        message.setFontSize(18);

        Message updated = messageDao.update(message);

        assertNotNull(updated);
        assertEquals("Updated content", updated.getContent());
        assertTrue(updated.isItalic());
        assertEquals(18, updated.getFontSize());
    }

    @Test
    @Order(4)
    @DisplayName("Happy Path: Find messages by Chat ID")
    void testFindByChatId() {
        List<Message> messages = messageDao.findByChatId(1);
        assertFalse(messages.isEmpty());
        assertTrue(messages.stream().anyMatch(m -> m.getMessageId() == createdMessageId));
    }

    @Test
    @Order(5)
    @DisplayName("Happy Path: Delete message by ID")
    void testDelete() {
        Optional<Message> deleted = messageDao.deleteById(createdMessageId);
        assertTrue(deleted.isPresent());
        assertEquals(createdMessageId, deleted.get().getMessageId());

        Optional<Message> found = messageDao.findById(createdMessageId);
        assertFalse(found.isPresent());
    }

    @Test
    @Order(6)
    @DisplayName("Sad Path: Find non-existent message")
    void testFindByIdNotFound() {
        Optional<Message> found = messageDao.findById(9999);
        assertFalse(found.isPresent());
    }

    @Test
    @Order(7)
    @DisplayName("Concurrency: 50 simultaneous inserts")
    void testConcurrentInserts() throws InterruptedException {
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        Set<Long> generatedIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
        AtomicInteger successfulSaves = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    Message m = new Message();
                    m.setChatId(1);
                    m.setSenderId(1);
                    m.setMessageType("TEXT");
                    m.setContent("Concurrent Msg");

                    Message res = messageDao.save(m);
                    if (res != null) {
                        generatedIds.add(res.getMessageId());
                        successfulSaves.incrementAndGet();
                    }
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
        assertEquals(threadCount, successfulSaves.get());
        assertEquals(threadCount, generatedIds.size());

        executor.shutdown();
    }

    @Test
    @Order(8)
    @DisplayName("Concurrency: Mixed Read/Write operations")
    void testMixedReadWrite() throws InterruptedException {
        int tasks = 40;
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch latch = new CountDownLatch(tasks);

        for (int i = 0; i < tasks; i++) {
            if (i % 2 == 0) {
                executor.submit(() -> {
                    Message m = new Message();
                    m.setChatId(1);
                    m.setSenderId(1);
                    m.setMessageType("TEXT");
                    m.setContent("Spam");
                    messageDao.save(m);
                    latch.countDown();
                });
            } else {
                executor.submit(() -> {
                    messageDao.findByChatId(1);
                    latch.countDown();
                });
            }
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();
    }
}