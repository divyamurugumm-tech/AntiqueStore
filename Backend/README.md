# Antique Collectibles Store System - Backend API

A production-ready Spring Boot 3 REST API for an **Antique Collectibles Store System** built with Java 17 release target, plain Spring `JdbcTemplate` (no JPA/Hibernate), custom in-memory Binary Search Tree (`BinarySearchTree`), and MySQL 9.7.

---

## 🏛️ Architecture & Highlights

- **Pure JDBC**: Built using Spring's `JdbcTemplate` for high performance, explicit SQL queries, and zero ORM overhead.
- **Custom Binary Search Tree (`ds/BinarySearchTree`)**:
  - Implemented completely from scratch without using `TreeMap` or `TreeSet`.
  - Nodes store `Item` objects keyed by `item_id`.
  - Operations implemented: `insert`, `search`, `delete` (handling 0, 1, and 2 children with in-order successor), `inorder`, `rangeSearch(minPrice, maxPrice)`.
  - Thread-safe using `ReentrantReadWriteLock` for concurrent HTTP request safety.
- **In-Memory & Database Synchronization**:
  - At startup (`ApplicationReadyEvent`), all `AVAILABLE` items from MySQL are loaded into the BST.
  - Every create, update, delete, and sale operation writes to MySQL first, and then synchronizes with the in-memory BST.
  - Recording a sale updates item status to `SOLD` in MySQL and removes the item from the BST.
- **CORS Configured**:
  - Cross-Origin Resource Sharing enabled for `http://localhost:5500` and `http://localhost:5173`.
- **Validation & Error Handling**:
  - Request validation with `@Valid`, `@NotBlank`, `@NotNull`, `@DecimalMin`, etc.
  - Global exception handling returning clean JSON error responses with proper HTTP status codes (200, 201, 400, 404, 409, 500).

---

## 📋 Prerequisites & Environment

- **OS**: Windows 11
- **Java**: JDK 17+ (compiled with Java release 17, tested on JDK 26)
- **Build Tool**: Apache Maven 3.10.1+
- **Database**: MySQL 9.7 on `localhost:3306` with database `antique_db` initialized from `database/schema.sql`.

---

## ⚙️ Configuration

Open `backend/src/main/resources/application.properties` and fill in your MySQL password:

```properties
spring.application.name=antique-collectibles-backend
server.port=8080

# MySQL DataSource Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/antique_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD_HERE
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Connection Pool Settings
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2

# Logging Configuration
logging.level.com.antiquestore=INFO
```

---

## 🚀 Build and Run

### 1. Build the Application
From the `backend` folder:
```bash
mvn clean package
```

### 2. Run the Application
You can run using either the Spring Boot Maven plugin or the generated JAR:

**Option A: Spring Boot Maven Plugin**
```bash
mvn spring-boot:run
```

**Option B: Standalone JAR**
```bash
java -jar target/antique-collectibles-backend-1.0.0.jar
```

The server will start on port `8080` (e.g. `http://localhost:8080`).

---

## 📡 API Endpoints & `curl` Examples

### 1. Items (`/api/items`)

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/items` | List all items from MySQL |
| `GET` | `/api/items/{id}` | Search item via in-memory BST |
| `GET` | `/api/items/sorted` | In-order traversal of BST (sorted by `item_id`) |
| `GET` | `/api/items/range?min=&max=` | BST range search by price |
| `POST` | `/api/items` | Create new item (MySQL first, then BST if AVAILABLE) |
| `PUT` | `/api/items/{id}` | Update item (MySQL first, then updates BST) |
| `DELETE` | `/api/items/{id}` | Delete item (MySQL first, then removes from BST) |

#### Examples:

**List all items (DB):**
```bash
curl -X GET http://localhost:8080/api/items
```

**Search item by ID (via BST):**
```bash
curl -X GET http://localhost:8080/api/items/1
```

**In-order BST traversal:**
```bash
curl -X GET http://localhost:8080/api/items/sorted
```

**BST Price range search:**
```bash
curl -X GET "http://localhost:8080/api/items/range?min=40000&max=200000"
```

**Create item:**
```bash
curl -X POST http://localhost:8080/api/items \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Baroque Gilded Mirror",
    "category": "Decor",
    "eraId": 2,
    "condition": "Excellent",
    "price": 125000.00,
    "status": "AVAILABLE"
  }'
```

**Update item:**
```bash
curl -X PUT http://localhost:8080/api/items/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Brass Astrolabe (Restored)",
    "category": "Instruments",
    "eraId": 2,
    "condition": "Restored",
    "price": 195000.00,
    "status": "AVAILABLE"
  }'
```

**Delete item:**
```bash
curl -X DELETE http://localhost:8080/api/items/12
```

---

### 2. Eras (`/api/eras`)

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/eras` | List all historical eras |
| `POST` | `/api/eras` | Create a new historical era |

#### Examples:

**List eras:**
```bash
curl -X GET http://localhost:8080/api/eras
```

**Create era:**
```bash
curl -X POST http://localhost:8080/api/eras \
  -H "Content-Type: application/json" \
  -d '{
    "eraName": "Edwardian",
    "startYear": 1901,
    "endYear": 1914
  }'
```

---

### 3. Valuations (`/api/valuations` & `/api/items/{id}/valuations`)

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/valuations` | Record an appraisal/valuation for an item |
| `GET` | `/api/items/{id}/valuations` | List all historical valuations for an item |

#### Examples:

**Record valuation:**
```bash
curl -X POST http://localhost:8080/api/valuations \
  -H "Content-Type: application/json" \
  -d '{
    "itemId": 1,
    "appraisedValue": 190000.00,
    "valDate": "2026-11-20",
    "appraiser": "H. Vance"
  }'
```

**Get valuations for an item:**
```bash
curl -X GET http://localhost:8080/api/items/1/valuations
```

---

### 4. Sales (`/api/sales`)

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/sales` | Record sale, update item status to SOLD in DB, and remove from BST |
| `GET` | `/api/sales` | Sales report with item name and total revenue |

#### Examples:

**Record a sale:**
```bash
curl -X POST http://localhost:8080/api/sales \
  -H "Content-Type: application/json" \
  -d '{
    "itemId": 2,
    "buyerName": "Eleanor Vance",
    "salePrice": 240000.00,
    "saleDate": "2026-11-20"
  }'
```

**Get sales report:**
```bash
curl -X GET http://localhost:8080/api/sales
```

Sample Sales Report Response:
```json
{
  "sales": [
    {
      "saleId": 4,
      "itemId": 2,
      "itemName": "Mahogany Writing Desk",
      "buyerName": "Eleanor Vance",
      "salePrice": 240000.00,
      "saleDate": "2026-11-20"
    },
    {
      "saleId": 3,
      "itemId": 11,
      "itemName": "Art Nouveau Vase",
      "buyerName": "Rahul Verma",
      "salePrice": 35500.00,
      "saleDate": "2025-10-11"
    }
  ],
  "totalRevenue": 343500.00,
  "totalCount": 4
}
```

---

## 🧪 Testing

Run automated JUnit tests for the custom BST:
```bash
mvn test
```
All 10 unit test cases for BST insertion, deletion (0, 1, 2 children), searching, duplicate handling, in-order sorting, and price range queries will execute and report results.
