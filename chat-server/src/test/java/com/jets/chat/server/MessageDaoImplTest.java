package com.jets.chat.server;

import com.jets.chat.server.dao.MessageDao;
import com.jets.chat.server.dao.impl.MessageDaoImpl;
import com.jets.chat.server.dto.CreateMessageDto;
import com.jets.chat.server.dto.EditMessageDto;
import com.jets.chat.server.dto.MessageResponseDto;
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
        config.setJdbcUrl("jdbc:h2:mem:chat_integration_test;MODE=MySQL;DB_CLOSE_DELAY=-1");
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
                    + "FOREIGN KEY (sender_id) REFERENCES users(user_id))");

            stmt.execute("INSERT INTO users (display_name) VALUES ('TestUser')");
            stmt.execute("INSERT INTO chats (chat_id) VALUES (1)");
        }

        messageDao = new MessageDaoImpl(dataSource);
    }

    @AfterAll
    void tearDown() {
        dataSource.close();
    }

    @Test
    @Order(1)
    @DisplayName("CRUD: Save new message")
    void testSave() {
        CreateMessageDto dto = new CreateMessageDto();
        dto.setChatId(1);
        dto.setSenderId(1);
        dto.setMessageType("TEXT");
        dto.setContent("Hello, this is a test message");
        dto.setBold(true);

        MessageResponseDto response = messageDao.save(dto);

        assertNotNull(response);
        assertTrue(response.getMessageId() > 0);
        assertEquals("TestUser", response.getSenderName());
        assertEquals("Hello, this is a test message", response.getContent());

        createdMessageId = response.getMessageId();
    }

    @Test
    @Order(2)
    @DisplayName("CRUD: Find message by ID")
    void testFindById() {
        Optional<MessageResponseDto> found = messageDao.findById(createdMessageId);

        assertTrue(found.isPresent());
        assertEquals(createdMessageId, found.get().getMessageId());
    }

    @Test
    @Order(3)
    @DisplayName("CRUD: Update existing message")
    void testUpdate() {
        EditMessageDto editDto = new EditMessageDto();
        editDto.setMessageId(createdMessageId);
        editDto.setContent("Updated content");
        editDto.setItalic(true);
        editDto.setFontSize(18);

        MessageResponseDto updated = messageDao.update(editDto);

        assertNotNull(updated);
        assertEquals("Updated content", updated.getContent());
        assertTrue(updated.isItalic());
        assertEquals(18, updated.getFontSize());
    }

    @Test
    @Order(4)
    @DisplayName("CRUD: Find all messages")
    void testFindAll() {
        List<MessageResponseDto> messages = messageDao.findAll();
        assertFalse(messages.isEmpty());
        assertTrue(messages.stream().anyMatch(m -> m.getMessageId() == createdMessageId));
    }

    @Test
    @Order(5)
    @DisplayName("CRUD: Delete message by ID")
    void testDelete() {
        MessageResponseDto deleted = messageDao.deleteById(createdMessageId);
        assertNotNull(deleted);

        Optional<MessageResponseDto> found = messageDao.findById(createdMessageId);
        assertFalse(found.isPresent());
    }

    @Test
    @Order(6)
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
                    CreateMessageDto dto = new CreateMessageDto();
                    dto.setChatId(1);
                    dto.setSenderId(1);
                    dto.setMessageType("TEXT");
                    dto.setContent("Concurrent Msg");

                    MessageResponseDto res = messageDao.save(dto);
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

        assertTrue(finished, "Concurrect test timed out");
        assertEquals(threadCount, successfulSaves.get(), "Some messages failed to save under load");
        assertEquals(threadCount, generatedIds.size(),
                "Duplicate IDs detected - thread safety issue!");

        executor.shutdown();
    }

    @Test
    @Order(7)
    @DisplayName("Concurrency: Mixed Read/Write operations")
    void testMixedReadWrite() throws InterruptedException {
        int tasks = 40;
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch latch = new CountDownLatch(tasks);

        for (int i = 0; i < tasks; i++) {
            if (i % 2 == 0) {
                executor.submit(() -> {
                    CreateMessageDto d = new CreateMessageDto();
                    d.setChatId(1);
                    d.setSenderId(1);
                    d.setMessageType("TEXT");
                    d.setContent("Spam");
                    messageDao.save(d);
                    latch.countDown();
                });
            } else {
                executor.submit(() -> {
                    messageDao.findAll();
                    latch.countDown();
                });
            }
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();
    }
}