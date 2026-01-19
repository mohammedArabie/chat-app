package com.jets.chat.server.dao;

import com.jets.chat.common.enums.ContactStatus;
import com.jets.chat.server.dao.impl.ContactsDaoImpl;
import com.jets.chat.server.entity.Contact;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ContactsDaoTest {

    private HikariDataSource dataSource;
    private ContactsDaoImpl dao;

    private long sharedOwnerId = 1001L;
    private long sharedContactId = 2001L;

    @BeforeAll
    void init() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:contacts_test;MODE=MySQL;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(20);

        dataSource = new HikariDataSource(config);

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE contacts (" + "owner_id BIGINT NOT NULL, "
                    + "contact_id BIGINT NOT NULL, " + "status VARCHAR(20) NOT NULL, "
                    + "category VARCHAR(50), " + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "PRIMARY KEY (owner_id, contact_id)" + ")");
        }

        dao = new ContactsDaoImpl(dataSource);
    }

    @AfterAll
    void cleanup() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("CRUD: Save contact and verify it exists")
    void testSave() {
        Contact contact = new Contact();
        contact.setOwnerId(sharedOwnerId);
        contact.setContactId(sharedContactId);
        contact.setStatus(ContactStatus.PENDING);
        contact.setCategory("Friends");

        Contact saved = dao.save(contact);

        assertNotNull(saved);
        assertEquals(sharedOwnerId, saved.getOwnerId());
        assertEquals(sharedContactId, saved.getContactId());
        assertEquals(ContactStatus.PENDING, saved.getStatus());
        assertEquals("Friends", saved.getCategory());
        assertNotNull(saved.getCreatedAt());

        assertTrue(dao.exists(sharedOwnerId, sharedContactId));
    }

    @Test
    @Order(2)
    @DisplayName("CRUD: Find by ids returns correct contact")
    void testFindByIds() {
        Optional<Contact> found = dao.findByIds(sharedOwnerId, sharedContactId);

        assertTrue(found.isPresent());
        Contact c = found.get();

        assertEquals(sharedOwnerId, c.getOwnerId());
        assertEquals(sharedContactId, c.getContactId());
        assertEquals(ContactStatus.PENDING, c.getStatus());
        assertEquals("Friends", c.getCategory());
        assertNotNull(c.getCreatedAt());
    }

    @Test
    @Order(3)
    @DisplayName("CRUD: Find by ids for non-existent contact returns empty")
    void testFindByIdsNonExistent() {
        Optional<Contact> found = dao.findByIds(9999L, 8888L);
        assertTrue(found.isEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("CRUD: Find all contacts by owner id")
    void testFindAllContactsByOwnerId() {
        for (int i = 1; i <= 3; i++) {
            Contact c = new Contact();
            c.setOwnerId(sharedOwnerId);
            c.setContactId(3000L + i);
            c.setStatus(ContactStatus.ACCEPTED);
            c.setCategory("Work");
            dao.save(c);
        }

        List<Contact> contacts = dao.findAllContactsByOwnerId(sharedOwnerId);

        assertNotNull(contacts);
        assertTrue(contacts.size() >= 4);
    }

    @Test
    @Order(5)
    @DisplayName("CRUD: Find all contacts by owner id and status")
    void testFindAllContactsByOwnerIdAndStatus() {
        List<Contact> pendingContacts = dao.findAllContactsByOwnerIdAndStatus(sharedOwnerId,
                ContactStatus.PENDING);

        assertNotNull(pendingContacts);
        assertTrue(pendingContacts.size() >= 1);

        for (Contact c : pendingContacts) {
            assertEquals(ContactStatus.PENDING, c.getStatus());
        }
    }

    @Test
    @Order(6)
    @DisplayName("CRUD: Find all contacts by owner id and category")
    void testFindAllContactsByOwnerIdAndCategory() {
        List<Contact> workContacts = dao.findAllContactsByOwnerIdAndCategory(sharedOwnerId, "Work");

        assertNotNull(workContacts);
        assertTrue(workContacts.size() >= 3);

        for (Contact c : workContacts) {
            assertEquals("Work", c.getCategory());
        }
    }

    @Test
    @Order(7)
    @DisplayName("CRUD: Update full contact (status + category)")
    void testUpdate() {
        Contact contact = dao.findByIds(sharedOwnerId, sharedContactId).orElseThrow();

        contact.setStatus(ContactStatus.ACCEPTED);
        contact.setCategory("BestFriends");

        boolean updated = dao.update(contact);
        assertTrue(updated);

        Contact verified = dao.findByIds(sharedOwnerId, sharedContactId).orElseThrow();
        assertEquals(ContactStatus.ACCEPTED, verified.getStatus());
        assertEquals("BestFriends", verified.getCategory());
    }

    @Test
    @Order(8)
    @DisplayName("CRUD: Update status only")
    void testUpdateStatus() {
        boolean updated = dao.updateStatus(sharedOwnerId, sharedContactId, ContactStatus.BLOCKED);
        assertTrue(updated);

        Contact verified = dao.findByIds(sharedOwnerId, sharedContactId).orElseThrow();
        assertEquals(ContactStatus.BLOCKED, verified.getStatus());
    }

    @Test
    @Order(9)
    @DisplayName("CRUD: Update category only")
    void testUpdateCategory() {
        boolean updated = dao.updateCategory(sharedOwnerId, sharedContactId, "BlockedList");
        assertTrue(updated);

        Contact verified = dao.findByIds(sharedOwnerId, sharedContactId).orElseThrow();
        assertEquals("BlockedList", verified.getCategory());
    }

    @Test
    @Order(10)
    @DisplayName("CRUD: Update non-existent contact returns false")
    void testUpdateNonExistent() {
        boolean updated = dao.updateStatus(1111L, 2222L, ContactStatus.ACCEPTED);
        assertFalse(updated);
    }

    @Test
    @Order(11)
    @DisplayName("CRUD: Delete contact and verify removal")
    void testDelete() {
        Contact c = new Contact();
        c.setOwnerId(5000L);
        c.setContactId(6000L);
        c.setStatus(ContactStatus.ACCEPTED);
        c.setCategory("Temp");

        dao.save(c);

        assertTrue(dao.exists(5000L, 6000L));

        boolean deleted = dao.deleteByIds(5000L, 6000L);
        assertTrue(deleted);

        assertFalse(dao.exists(5000L, 6000L));
        assertTrue(dao.findByIds(5000L, 6000L).isEmpty());

        boolean deletedAgain = dao.deleteByIds(5000L, 6000L);
        assertFalse(deletedAgain);
    }

    @Test
    @Order(12)
    @DisplayName("Boundary: Save contact with custom created_at timestamp")
    void testSaveWithCustomCreatedAt() {
        Contact c = new Contact();
        c.setOwnerId(7000L);
        c.setContactId(8000L);
        c.setStatus(ContactStatus.PENDING);
        c.setCategory("Family");

        Timestamp customTime = Timestamp.valueOf(LocalDateTime.now().minusDays(2));
        c.setCreatedAt(customTime);

        Contact saved = dao.save(c);

        assertNotNull(saved.getCreatedAt());
        assertEquals(customTime, saved.getCreatedAt());
    }

    @Test
    @Order(13)
    @DisplayName("Error Handling: Save duplicate (same owner_id + contact_id) should throw exception")
    void testSaveDuplicateShouldFail() {
        Contact c1 = new Contact();
        c1.setOwnerId(9000L);
        c1.setContactId(9001L);
        c1.setStatus(ContactStatus.PENDING);
        dao.save(c1);

        Contact c2 = new Contact();
        c2.setOwnerId(9000L);
        c2.setContactId(9001L);
        c2.setStatus(ContactStatus.ACCEPTED);

        assertThrows(RuntimeException.class, () -> dao.save(c2));
    }

    @Test
    @Order(14)
    @DisplayName("Concurrent: Multiple threads saving contacts")
    void testConcurrentSaves() throws InterruptedException {
        int threadCount = 10;
        long ownerId = 99900L;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        List<Long> contactIds = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    latch.await();
                    Contact c = new Contact();
                    c.setOwnerId(ownerId);
                    c.setContactId(100000L + idx);
                    c.setStatus(ContactStatus.ACCEPTED);
                    c.setCategory("Concurrent");

                    dao.save(c);
                    contactIds.add(c.getContactId());
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

        for (Long cid : contactIds) {
            assertTrue(dao.exists(ownerId, cid));
            assertTrue(dao.findByIds(ownerId, cid).isPresent());
        }

        for (Long cid : contactIds) {
            dao.deleteByIds(ownerId, cid);
        }
    }

    @Test
    @Order(15)
    @DisplayName("Ordering: findAllContactsByOwnerId should return results ordered by created_at DESC")
    void testOrderingByCreatedAtDesc() throws InterruptedException {
        long ownerId = 7777L;

        Contact c1 = new Contact();
        c1.setOwnerId(ownerId);
        c1.setContactId(1L);
        c1.setStatus(ContactStatus.ACCEPTED);
        c1.setCategory("OrderTest");
        dao.save(c1);

        Thread.sleep(10);

        Contact c2 = new Contact();
        c2.setOwnerId(ownerId);
        c2.setContactId(2L);
        c2.setStatus(ContactStatus.ACCEPTED);
        c2.setCategory("OrderTest");
        dao.save(c2);

        List<Contact> contacts = dao.findAllContactsByOwnerId(ownerId);

        assertTrue(contacts.size() >= 2);

        assertEquals(2L, contacts.get(0).getContactId());
        assertEquals(1L, contacts.get(1).getContactId());

        dao.deleteByIds(ownerId, 1L);
        dao.deleteByIds(ownerId, 2L);
    }

    @Test
    @Order(16)
    @DisplayName("Null Handling: Save contact with null category should succeed")
    void testSaveWithNullCategory() {
        long ownerId = 8888L;
        long contactId = 9999L;

        Contact c = new Contact();
        c.setOwnerId(ownerId);
        c.setContactId(contactId);
        c.setStatus(ContactStatus.PENDING);
        c.setCategory(null);

        Contact saved = dao.save(c);

        assertNotNull(saved);
        assertTrue(dao.exists(ownerId, contactId));

        Contact fetched = dao.findByIds(ownerId, contactId).orElseThrow();
        assertNull(fetched.getCategory());

        dao.deleteByIds(ownerId, contactId);
    }

    @Test
    @Order(17)
    @DisplayName("Error Handling: Save contact with null status should throw IllegalArgumentException")
    void testSaveWithNullStatusShouldFail() {
        Contact c = new Contact();
        c.setOwnerId(12345L);
        c.setContactId(54321L);
        c.setStatus(null);
        c.setCategory("Test");

        assertThrows(IllegalArgumentException.class, () -> dao.save(c));
    }

    @Test
    @Order(18)
    @DisplayName("Validation: Save contact with ownerId <= 0 should throw IllegalArgumentException")
    void testSaveWithInvalidOwnerIdShouldFail() {
        Contact c = new Contact();
        c.setOwnerId(0);
        c.setContactId(11111L);
        c.setStatus(ContactStatus.PENDING);
        c.setCategory("Test");

        assertThrows(IllegalArgumentException.class, () -> dao.save(c));
    }

    @Test
    @Order(19)
    @DisplayName("Validation: Save contact with contactId <= 0 should throw IllegalArgumentException")
    void testSaveWithInvalidContactIdShouldFail() {
        Contact c = new Contact();
        c.setOwnerId(22222L);
        c.setContactId(0);
        c.setStatus(ContactStatus.PENDING);
        c.setCategory("Test");

        assertThrows(IllegalArgumentException.class, () -> dao.save(c));
    }
}
