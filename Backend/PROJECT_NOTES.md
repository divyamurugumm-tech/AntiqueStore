# Project Notes: Antique Collectibles Store System

## 1. What Is Done

### Architecture & Layering
- **Project Structure**: Organized in `backend/` following standard 5-layer separation:
  - `controller`: `ItemController`, `EraController`, `ValuationController`, `SaleController`, `GlobalExceptionHandler`.
  - `service`: `ItemService`, `EraService`, `ValuationService`, `SaleService`.
  - `repository`: `ItemRepository`, `EraRepository`, `ValuationRepository`, `SaleRepository` using plain `JdbcTemplate`.
  - `model` & DTOs: `Item`, `Era`, `Valuation`, `Sale`, `ItemStatus`, `ItemRequest`, `EraRequest`, `ValuationRequest`, `SaleRequest`, `SalesReportResponse`, `SaleDetailDto`, `ErrorResponse`.
  - `ds`: Custom `BinarySearchTree` and `TreeNode`.

### Database Integration (Plain JDBC)
- Plain `JdbcTemplate` implementation without JPA/Hibernate or ORM tools.
- Strict schema adherence matching `database/schema.sql` (`era`, `item`, `valuation`, `sale`).
- Explicit transaction management (`@Transactional`) on `SaleService.recordSale` to maintain database consistency across sale creation and item status update.
- MySQL datasource configuration in `application.properties` with password placeholder.

### Custom Data Structure (`ds/BinarySearchTree`)
- Custom Binary Search Tree implemented completely from scratch (no `java.util.TreeMap` or `java.util.TreeSet`).
- Nodes hold `Item` objects keyed by `item_id`.
- Implemented operations:
  - `insert(Item item)`: Recursive insertion; updates node value if key already exists.
  - `search(Integer itemId)`: Searches item by ID.
  - `delete(Integer itemId)`: Full BST deletion covering 0 children (leaf), 1 child, and 2 children (replacing with in-order successor).
  - `inorder()`: In-order traversal (left, root, right) returning items sorted by `item_id`.
  - `rangeSearch(BigDecimal minPrice, BigDecimal maxPrice)`: Traverses the BST and filters items within the price range `[minPrice, maxPrice]`.
  - Concurrency: Guaranteed thread safety via `ReentrantReadWriteLock` for simultaneous HTTP queries and updates.

### Startup & Operational Synchronization
- **Startup**: `@EventListener(ApplicationReadyEvent.class)` in `ItemService` loads all items where `status = 'AVAILABLE'` from MySQL into the BST.
- **Write Consistency**:
  - `POST /api/items`: Inserts into MySQL first, then inserts into BST if `AVAILABLE`.
  - `PUT /api/items/{id}`: Updates MySQL first, then updates BST (adds/updates if `AVAILABLE`, deletes if changed to `SOLD`).
  - `DELETE /api/items/{id}`: Deletes from MySQL first, then removes from BST.
  - `POST /api/sales`: Inserts into `sale` table, updates `item.status` to `SOLD` in MySQL, and deletes from BST.

### CORS & Error Handling
- Enabled CORS globally for `http://localhost:5500` and `http://localhost:5173`.
- `GlobalExceptionHandler` intercepting:
  - Validation failures (`MethodArgumentNotValidException`) -> 400 with detailed field map.
  - Not found exceptions (`ResourceNotFoundException`) -> 404.
  - Invalid requests (`BadRequestException`) -> 400.
  - Business conflicts (`ConflictException`) -> 409 (e.g. attempting to sell an already-sold item).
  - Database constraint violations (`DataIntegrityViolationException`) -> 409.
  - Unexpected errors (`Exception`) -> 500 without leaking sensitive database credentials.

### Build & Automated Tests
- Configured Maven for Java 17 release target (`<release>17</release>`), successfully built with Maven 3.10.1 on JDK 26.
- Comprehensive unit test suite in `BinarySearchTreeTest.java` (10 test cases covering insert, duplicate update, leaf deletion, 1-child deletion, 2-child deletion, root deletion, in-order traversal, price range search, and clear).
- Build and tests executed with 100% success (`BUILD SUCCESS`). Runnable JAR created at `target/antique-collectibles-backend-1.0.0.jar`.

---

## 2. What Is Pending

1. **User Database Password**:
   - The user must enter their MySQL 9.7 root password into `backend/src/main/resources/application.properties` (`spring.datasource.password=...`).
2. **Live Execution & End-to-End Curl Verification**:
   - Once the database password is provided by the user in `application.properties`, start the application (`mvn spring-boot:run` or `java -jar target/antique-collectibles-backend-1.0.0.jar`) and run curl tests on every endpoint against the live MySQL `antique_db`.
