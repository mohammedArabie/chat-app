# 💬 ChatApp - Secure Real-Time Messaging Platform  

A secure, and scalable Java-based chat application with an elegant JavaFX interface and an admin dashboard. Built with modern Java technologies, enterprise design patterns, and an emphasis on security and usability.

---

## 🚀 Features

### **Core Features** 
- **Secure Registration & Authentication** – Phone number as unique ID with strong password validation.
- **1-on-1 & Group Chat** – Real-time messaging.
- **Contact Management** – Add contacts by phone number.
- **Status Management** – Online/Offline/Away/Busy indicators with icons.
- **File Transfer** – Send/receive text, images, audio, and video files.
- **Offline Messaging** – Messages queued and delivered when the recipient comes online.
- **Persistent Sessions** – Chat history and contacts saved to database.
- **Secure Sign-Out & Exit** – Password cleared on sign-out; credentials preserved on exit.
- **Admin Dashboard** – Real-time statistics, user management, and announcements.
- **Contact Blocking** – Block users while maintaining selective chat ability.
- **"Appear Offline" Mode** – Read messages without triggering "seen" receipts.
- **Contact Groups** – Organize contacts into categories (Friends, Family, Work).

---

## 🏗️ Architecture

### **Project Structure (Maven Multi-Module)**

```text
chat-app/
├── chat-common/     # Shared DTOs, enums, RMI interfaces
├── chat-server/     # Admin dashboard, RMI services, DAO layer
├── chat-client/     # End-user JavaFX application
└── pom.xml          # Parent POM
```

### **Design Patterns Applied**
- **Singleton** – ServerManager, ClientManager
- **MVC** – JavaFX Controllers, FXML Views, Entity Models
- **Observer** – ClientCallback for real-time status updates
- **DAO** – Database abstraction for User, Message, Contact

### **SOLID Principles**
- **Single Responsibility** – Each service/controller handles one domain.
- **Open/Closed** – Admin dashboard extensible via new controllers.
- **Liskov Substitution** – Services implement interfaces for easy replacement.
- **Interface Segregation** – Fine-grained RMI interfaces.
- **Dependency Inversion** – High-level modules depend on abstractions.

---

## ⚙️ Technology Stack

| Layer | Technology |
|-------|------------|
| **Frontend** | JavaFX 17+, CSS3, FXML |
| **Backend** | Java 17, RMI |
| **Database** | MySQL 8.0, HikariCP connection pool |
| **Build** | Maven |
| **Security** | SHA-256 password hashing |
| **Testing** | JUnit 5 |

---

## 🔐 Security Implementation

### **Password Hashing**
Passwords are securely hashed using **SHA-256** before storage.

### **Security Features**
- 🔒 **SHA-256 password hashing** – One-way cryptographic hashing
- 🔒 **Input validation** – Prevention against XSS/SQL injection attacks
- 🔒 **Admin password rotation** – Forced password change on first login

---

## 🚀 Getting Started

### **Prerequisites**
- Java 17+ JDK
- MySQL 8.0+
- Maven 3.8+

### **1. Database Setup**

```sql
CREATE DATABASE chat_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

```bash
mysql -u root -p chat_db < chat-server/src/main/resources/schema.sql
```

Copy the database configuration template:

```bash
cp chat-server/src/main/resources/db.properties.template chat-server/src/main/resources/db.properties
```

Edit `db.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/chat_db?useSSL=false&serverTimezone=UTC
db.username=root
db.password=your_password
```


### **2. Build the Project**

```bash
mvn clean install -DskipTests
```


### **3. Run the Server (Admin Dashboard)**

```bash
cd chat-server
java -jar target/chat-server-1.0.jar
```

Default admin login: `admin` / `admin123` (password change required on first login).


### **4. Run the Client Application**

```bash
cd chat-client
java -jar target/chat-client-1.0.jar
```

Register with a valid phone number (E.164 format, e.g., `+201012345678`).


---

## 🧪 Testing

Run unit tests:

```bash
mvn test
```


---

## 📁 Project Structure Highlights

```text
chat-server/src/main/java/com/jets/chat/server/
├── dao/          # Data Access Objects (UserDao, MessageDao, etc.)
├── rmi/          # RMI service implementations
├── service/      # Business logic layer
├── admin/        # Admin dashboard controllers & views
└── util/         # Utilities (PasswordUtil, DtoMapper, etc.)
```


---

## 👥 Team

- Mohammed Arabie
- Mohammed Ashour
- Omar Mohammad
- Rawan Almashad


---

## 💡 Demo Tips

1. **Start the server first** → Admin dashboard opens.

2. **Register test users** via the client before demoing chat.

3. **Show admin editing**: double-click a user field → edit → press Enter → changes persist.

4. **Demonstrate offline messaging**:
   - User A sends a message to offline User B.
   - User B signs in → receives queued messages immediately.

5. **Highlight security**: show SHA-256 password hashes in database (64-character hex strings).
