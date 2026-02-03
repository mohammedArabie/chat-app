package com.jets.chat.server.dao;

import com.jets.chat.server.dao.impl.AnnouncementDaoImpl;
import com.jets.chat.server.entity.Announcement;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AnnouncementDaoTest {

    private HikariDataSource dataSource;
    private AnnouncementDaoImpl dao;
    private Long sharedTestId;
    private List<Long> createdIds = new ArrayList<>();

    @BeforeAll
    void init() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:announcement_test1;MODE=MySQL;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(20);
        dataSource = new HikariDataSource(config);

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE announcements ("
                    + "announcement_id BIGINT PRIMARY KEY AUTO_INCREMENT, "
                    + "content TEXT NOT NULL, " + "sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "font_style VARCHAR(50), " + "font_color VARCHAR(20), "
                    + "is_bold TINYINT(1) DEFAULT 0, " + "is_italic TINYINT(1) DEFAULT 0)");
        }

        dao = new AnnouncementDaoImpl(dataSource);
    }

    @AfterAll
    void cleanup() {
        for (Long id : createdIds) {
            try {
                dao.deleteById(id);
            } catch (Exception ignored) {
            }
        }
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("CRUD: Save announcement and verify ID generation")
    void testSave() {
        Announcement a = new Announcement();
        a.setContent("Unit Test Announcement");
        a.setFontStyle("Courier");
        a.setFontColor("#00FF00");
        a.setBold(false);
        a.setItalic(true);

        Announcement saved = dao.save(a);

        assertNotNull(saved.getAnnouncementId());
        assertNotNull(saved.getSentAt());
        assertTrue(saved.getSentAt().before(new Timestamp(System.currentTimeMillis() + 1000)));
        sharedTestId = saved.getAnnouncementId();
        createdIds.add(sharedTestId);
    }

    @Test
    @Order(2)
    @DisplayName("CRUD: Save announcement with custom timestamp")
    void testSaveWithCustomTimestamp() {
        Timestamp customTime = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        Announcement a = new Announcement();
        a.setContent("Announcement with custom time");
        a.setSentAt(customTime);
        a.setFontStyle("Arial");
        a.setFontColor("#FF0000");
        a.setBold(true);
        a.setItalic(false);

        Announcement saved = dao.save(a);
        createdIds.add(saved.getAnnouncementId());

        assertNotNull(saved.getAnnouncementId());
        assertEquals(customTime, saved.getSentAt());
    }

    @Test
    @Order(3)
    @DisplayName("CRUD: Find by ID and check data integrity")
    void testFindById() {
        Optional<Announcement> found = dao.findById(sharedTestId);

        assertTrue(found.isPresent());
        Announcement announcement = found.get();
        assertEquals("Unit Test Announcement", announcement.getContent());
        assertEquals("Courier", announcement.getFontStyle());
        assertEquals("#00FF00", announcement.getFontColor());
        assertFalse(announcement.isBold());
        assertTrue(announcement.isItalic());
        assertNotNull(announcement.getSentAt());
    }

    @Test
    @Order(4)
    @DisplayName("CRUD: Find by non-existent ID returns empty Optional")
    void testFindByNonExistentId() {
        Optional<Announcement> found = dao.findById(999999L);
        assertFalse(found.isPresent());
    }

    @Test
    @Order(5)
    @DisplayName("CRUD: Find all announcements")
    void testFindAll() {
        Announcement a1 = new Announcement();
        a1.setContent("Test Announcement 1");
        a1.setFontStyle("Arial");
        a1.setFontColor("#000000");
        Announcement saved1 = dao.save(a1);
        createdIds.add(saved1.getAnnouncementId());

        Announcement a2 = new Announcement();
        a2.setContent("Test Announcement 2");
        a2.setFontStyle("Verdana");
        a2.setFontColor("#0000FF");
        a2.setBold(true);
        Announcement saved2 = dao.save(a2);
        createdIds.add(saved2.getAnnouncementId());

        List<Announcement> allAnnouncements = dao.findAll();

        assertNotNull(allAnnouncements);
        assertTrue(allAnnouncements.size() >= 2);

        for (int i = 0; i < allAnnouncements.size() - 1; i++) {
            assertTrue(allAnnouncements.get(i).getSentAt()
                    .compareTo(allAnnouncements.get(i + 1).getSentAt()) >= 0);
        }
    }

    @Test
    @Order(6)
    @DisplayName("CRUD: Find recent announcements with limit")
    void testFindRecent() {
        for (int i = 1; i <= 10; i++) {
            Announcement a = new Announcement();
            a.setContent("Recent Test " + i);
            a.setFontStyle("Style" + i);
            Announcement saved = dao.save(a);
            createdIds.add(saved.getAnnouncementId());
        }

        List<Announcement> recent5 = dao.findRecent(5);
        assertEquals(5, recent5.size());

        List<Announcement> recent0 = dao.findRecent(0);
        assertEquals(5, recent0.size());

        List<Announcement> recentNeg = dao.findRecent(-1);
        assertEquals(5, recentNeg.size());

        List<Announcement> recentLarge = dao.findRecent(100);
        assertTrue(recentLarge.size() >= 10);

        for (int i = 0; i < recent5.size() - 1; i++) {
            assertTrue(recent5.get(i).getSentAt().compareTo(recent5.get(i + 1).getSentAt()) >= 0);
        }
    }

    @Test
    @Order(7)
    @DisplayName("CRUD: Update and verify changes")
    void testUpdate() {
        Announcement a = dao.findById(sharedTestId).get();
        String originalContent = a.getContent();
        Timestamp originalSentAt = a.getSentAt();

        a.setContent("Updated via Test");
        a.setFontStyle("UpdatedFont");
        a.setFontColor("#123456");
        a.setBold(true);
        a.setItalic(false);

        boolean updated = dao.update(a);
        assertTrue(updated);

        Announcement verified = dao.findById(sharedTestId).get();
        assertEquals("Updated via Test", verified.getContent());
        assertEquals("UpdatedFont", verified.getFontStyle());
        assertEquals("#123456", verified.getFontColor());
        assertTrue(verified.isBold());
        assertFalse(verified.isItalic());
        assertEquals(originalSentAt, verified.getSentAt());
        assertNotEquals(originalContent, verified.getContent());
    }

    @Test
    @Order(8)
    @DisplayName("CRUD: Update non-existent announcement returns false")
    void testUpdateNonExistent() {
        Announcement a = new Announcement();
        a.setAnnouncementId(999999L);
        a.setContent("Non-existent");

        boolean updated = dao.update(a);
        assertFalse(updated);
    }

    @Test
    @Order(9)
    @DisplayName("CRUD: Delete and verify removal")
    void testDelete() {
        Announcement toDelete = new Announcement();
        toDelete.setContent("To be deleted");
        toDelete.setFontStyle("DeleteMe");
        Announcement saved = dao.save(toDelete);
        Long deleteId = saved.getAnnouncementId();

        assertTrue(dao.findById(deleteId).isPresent());

        boolean deleted = dao.deleteById(deleteId);
        assertTrue(deleted);

        Optional<Announcement> found = dao.findById(deleteId);
        assertFalse(found.isPresent());

        boolean deletedAgain = dao.deleteById(deleteId);
        assertFalse(deletedAgain);
    }

    @Test
    @Order(10)
    @DisplayName("CRUD: Delete non-existent announcement returns false")
    void testDeleteNonExistent() {
        boolean deleted = dao.deleteById(999999L);
        assertFalse(deleted);
    }

    @Test
    @Order(11)
    @DisplayName("Concurrent: Multiple threads saving announcements")
    void testConcurrentSaves() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        List<Long> concurrentIds = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            final int threadNum = i;
            executor.submit(() -> {
                try {
                    latch.await();
                    Announcement a = new Announcement();
                    a.setContent("Concurrent Announcement " + threadNum);
                    a.setFontStyle("ConcurrentStyle");
                    Announcement saved = dao.save(a);
                    concurrentIds.add(saved.getAnnouncementId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertEquals(threadCount, successCount.get());

        for (Long id : concurrentIds) {
            assertTrue(dao.findById(id).isPresent());
        }

        for (Long id : concurrentIds) {
            dao.deleteById(id);
        }
    }

    @Test
    @Order(12)
    @DisplayName("Data Mapping: Verify all fields are properly mapped from ResultSet")
    void testDataMapping() {
        Announcement a = new Announcement();
        a.setContent("Mapping Test");
        a.setFontStyle("TestFont");
        a.setFontColor("#654321");
        a.setBold(true);
        a.setItalic(true);
        Announcement saved = dao.save(a);
        createdIds.add(saved.getAnnouncementId());

        Optional<Announcement> retrieved = dao.findById(saved.getAnnouncementId());
        assertTrue(retrieved.isPresent());

        Announcement retrievedAnn = retrieved.get();
        assertEquals(saved.getAnnouncementId(), retrievedAnn.getAnnouncementId());
        assertEquals(saved.getContent(), retrievedAnn.getContent());
        assertEquals(saved.getFontStyle(), retrievedAnn.getFontStyle());
        assertEquals(saved.getFontColor(), retrievedAnn.getFontColor());
        assertEquals(saved.isBold(), retrievedAnn.isBold());
        assertEquals(saved.isItalic(), retrievedAnn.isItalic());
        assertThat(retrievedAnn.getSentAt().toInstant()).isCloseTo(saved.getSentAt().toInstant(),
                within(1, ChronoUnit.SECONDS));
    }

    @Test
    @Order(13)
    @DisplayName("Boundary: Announcement with maximum length content")
    void testMaxLengthContent() {
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longContent.append("This is a very long announcement content. ");
        }

        Announcement a = new Announcement();
        a.setContent(longContent.toString());
        a.setFontStyle("LongFont");

        Announcement saved = dao.save(a);
        createdIds.add(saved.getAnnouncementId());

        Optional<Announcement> retrieved = dao.findById(saved.getAnnouncementId());
        assertTrue(retrieved.isPresent());
        assertEquals(longContent.toString(), retrieved.get().getContent());
    }

    @Test
    @Order(14)
    @DisplayName("Null Handling: Announcement with null optional fields")
    void testNullOptionalFields() {
        Announcement a = new Announcement();
        a.setContent("Null Fields Test");

        Announcement saved = dao.save(a);
        createdIds.add(saved.getAnnouncementId());

        Optional<Announcement> retrieved = dao.findById(saved.getAnnouncementId());
        assertTrue(retrieved.isPresent());
        assertEquals("Null Fields Test", retrieved.get().getContent());
    }

    @Test
    @Order(15)
    @DisplayName("Error Handling: Save with null content should throw exception")
    void testSaveWithNullContent() {
        Announcement a = new Announcement();
        a.setContent(null);
        a.setFontStyle("Test");

        assertThrows(RuntimeException.class, () -> dao.save(a));
    }
}