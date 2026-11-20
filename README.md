# 🏺 Antique Collectibles Store System

A full-stack antique inventory and appraisal management system featuring a high-performance Java 17 Spring Boot REST API with in-memory custom Binary Search Tree (BST), MySQL persistence, and a clean, responsive Vanilla HTML5/CSS3/JavaScript frontend.

---

## 🏛️ System Architecture

```
AntiqueStore/
├── Backend/                 # Spring Boot 3 REST API (Java 17, JdbcTemplate, Custom BST)
├── Database/                # MySQL 9.7 Schema & Sample Seed Data
├── frontend/                # Plain HTML5, CSS3, Vanilla JS Frontend UI
│   ├── index.html           # Single-page UI with semantic markup & dialogs
│   ├── styles.css           # Antique gallery aesthetic & responsive layout
│   ├── app.js               # API client, state management, and DOM bindings
│   └── serve.py             # Zero-dependency Python server for port 5500
├── README.md                # System documentation & run instructions
└── PROJECT_NOTES.md         # Project status, architectural decisions & notes
```

### Key Components

1. **Frontend (`frontend/`)**:
   - Zero-dependency plain HTML5, CSS3, and modern Vanilla JavaScript (no frameworks or build tools).
   - Designed with an antique collectibles gallery theme (warm parchment, espresso tones, and antique gold accents).
   - Responsive layout for desktop, tablet, and mobile displays.
   - Live synchronization with Spring Boot backend via REST API at `http://localhost:8080/api`.
   - Served on `http://localhost:5500` to satisfy backend CORS configuration.

2. **Backend (`Backend/`)**:
   - Spring Boot 3 on Java 17 target using plain Spring `JdbcTemplate` (no JPA/Hibernate).
   - In-memory custom `BinarySearchTree` (`ds/BinarySearchTree`) providing logarithmic search by item ID, in-order sorted traversal, and price range queries.
   - Concurrent read/write safety via `ReentrantReadWriteLock`.
   - Global CORS configured for `http://localhost:5500` and `http://localhost:5173`.

3. **Database (`Database/`)**:
   - MySQL database `antique_db` with relational schema: `era`, `item`, `valuation`, and `sale`.

---

## 🚀 Quick Start & Run Steps

### 1. Database Setup
Ensure MySQL is running on `localhost:3306`, then import the schema:
```bash
mysql -u root -p < Database/schema.sql
```

### 2. Run the Backend API (Port 8080)
1. Verify database credentials in `Backend/src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/antique_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
   spring.datasource.username=root
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   ```
2. Build and launch the Spring Boot service:
   ```bash
   cd Backend
   mvn spring-boot:run
   ```
   *The backend will start and listen on `http://localhost:8080`.*

### 3. Run the Frontend (Port 5500)
The backend enforces CORS strictly for origin `http://localhost:5500`. Serve the frontend on port 5500 using either option below:

**Option A: Using the included Python runner (Recommended)**
```bash
python frontend/serve.py
```

**Option B: Using Python's built-in `http.server` module**
```bash
python -m http.server 5500 --directory frontend
```

**Option C: Using Node.js `npx http-server`**
```bash
npx http-server frontend -p 5500 -c-1
```

Once running, open your web browser to:
👉 **[http://localhost:5500](http://localhost:5500)**

---

## 📱 Frontend Features & Sections

### 1. 📦 Inventory Items
- **Inventory Table**: View all cataloged items with ID, Name, Category, Historical Era, Condition, Price, and Status badges (`AVAILABLE` vs `SOLD`).
- **BST In-Order Sorted View**: Query the custom in-memory Binary Search Tree via `GET /api/items/sorted` to traverse items ordered by `itemId`.
- **Search by ID (BST Lookup)**: Direct item search using `GET /api/items/{id}` against the in-memory tree.
- **Price Range Filter (BST Range Search)**: Filter items by minimum and maximum price using `GET /api/items/range?min=&max=`.
- **Add Antique Item**: Modal form to add a new artifact (`POST /api/items`), automatically populating registered eras and synchronizing both MySQL and the BST.
- **Edit Item**: Pre-filled update modal (`PUT /api/items/{id}`).
- **Delete Item**: Confirmation dialog to safely remove items (`DELETE /api/items/{id}`) from both MySQL and the BST.

### 2. 🏛️ Historical Eras
- **Eras Directory**: View historical periods with Start Year, End Year, and era duration (`GET /api/eras`).
- **Add Historical Era**: Form with period validation (`POST /api/eras`). Adding an era dynamically refreshes all item dropdowns across the application.

### 3. 📜 Valuations & Appraisals
- **Appraisal History**: Inspect historical appraisal certificates for any item (`GET /api/items/{id}/valuations`).
- **Record New Appraisal**: Record certified valuations with Appraised Value, Appraisal Date, and Appraiser Name (`POST /api/valuations`).

### 4. 💰 Sales & Revenue Report
- **Financial KPIs**: Live summary metrics showing Total Revenue (₹), Total Antiques Sold, and Average Realized Price.
- **Sell Available Item**: Sell action modal (`POST /api/sales`) with buyer details and date. Selling an item marks its status as `SOLD` in MySQL, automatically removes it from the in-memory BST, and updates the sales ledger.
- **Completed Transactions Ledger**: Detailed table of realized sales with buyer name, sale price, and date (`GET /api/sales`).

---

## 📡 Backend REST API Reference

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/items` | List all items from MySQL database |
| `GET` | `/api/items/{id}` | Search item by ID via in-memory BST |
| `GET` | `/api/items/sorted` | In-order traversal of BST (sorted by `itemId`) |
| `GET` | `/api/items/range?min=&max=` | BST price range query |
| `POST` | `/api/items` | Create new item (MySQL first, then BST if `AVAILABLE`) |
| `PUT` | `/api/items/{id}` | Update item (MySQL first, then updates BST) |
| `DELETE` | `/api/items/{id}` | Delete item (MySQL first, then removes from BST) |
| `GET` | `/api/eras` | List all historical eras |
| `POST` | `/api/eras` | Add a new historical era |
| `POST` | `/api/valuations` | Record an appraisal certificate |
| `GET` | `/api/items/{id}/valuations` | List appraisal history for an item |
| `POST` | `/api/sales` | Record sale, update status to `SOLD`, remove from BST |
| `GET` | `/api/sales` | Sales report with total revenue and transaction history |

---

## 🧪 Verification & Testing

1. **Static Assets Check**:
   ```bash
   curl.exe -I http://localhost:5500/
   curl.exe -I http://localhost:5500/styles.css
   curl.exe -I http://localhost:5500/app.js
   ```
2. **CORS Verification**:
   ```bash
   curl.exe -i -H "Origin: http://localhost:5500" http://localhost:8080/api/items
   ```
3. **Automated Unit Tests (Backend BST)**:
   ```bash
   cd Backend
   mvn test
   ```
