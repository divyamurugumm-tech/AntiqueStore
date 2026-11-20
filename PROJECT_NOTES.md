# Project Notes: Antique Collectibles Store System

## 1. What Is Done

### Architecture & Layering
- **Full-Stack System Structure**:
  - `Backend/`: Spring Boot 3 REST API on Java 17 release target with standard 5-layer separation (`controller`, `service`, `repository`, `model`/DTOs, `ds`).
  - `Database/`: MySQL 9.7 schema (`schema.sql`) defining tables `era`, `item`, `valuation`, and `sale`.
  - `frontend/`: Zero-dependency Vanilla HTML5, CSS3, and modern JavaScript application running independently on port 5500.

### Frontend Implementation (`frontend/`)
- **Technology Stack**:
  - Plain HTML5 semantic markup, responsive CSS3 using modern custom properties (CSS variables), and modern modular ES6+ JavaScript.
  - Zero external UI libraries, no npm build steps, and no bundling tools required.
- **Antique Store Aesthetic**:
  - Curated gallery theme with rich parchment (`#fbf9f4`), warm walnut (`#241e17`), and antique brass/gold accents (`#b8860b`).
  - Styled components: responsive data tables, KPI metric cards, action toolbars, modal dialogs, status badges (`AVAILABLE` vs `SOLD`), and an animated toast notification system.
- **Features Implemented**:
  1. **Inventory Management**:
     - Live data table querying all cataloged items (`GET /api/items`).
     - In-order BST sorted view (`GET /api/items/sorted`).
     - BST item search by ID (`GET /api/items/{id}`).
     - BST price range filtering (`GET /api/items/range?min=&max=`).
     - Modal for adding antique items (`POST /api/items`) with dynamic era selection.
     - Modal for editing antique items (`PUT /api/items/{id}`).
     - Confirmation dialog for deleting items (`DELETE /api/items/{id}`).
  2. **Historical Eras**:
     - Directory table displaying registered eras, start/end years, and durations (`GET /api/eras`).
     - Creation form (`POST /api/eras`) with client validation (`startYear <= endYear`) and automatic synchronization with item form dropdowns.
  3. **Valuations & Appraisals**:
     - Historical appraisal certificate viewer for any chosen item (`GET /api/items/{id}/valuations`).
     - Record appraisal form (`POST /api/valuations`) with validation for non-negative valuation and certified appraiser.
  4. **Sales & Revenue**:
     - KPI metric cards for Total Revenue, Total Antiques Sold, and Average Realized Price.
     - Sell item modal (`POST /api/sales`) with pre-filled prices, marking item as `SOLD` in MySQL and evicting it from the in-memory BST.
     - Completed transactions ledger (`GET /api/sales`).
  5. **API Error Handling**:
     - Client-side error mapping parsing Spring Boot's `ErrorResponse`.
     - Automatically highlights offending input fields and renders specific validation error messages beneath inputs when 400 Validation errors occur.
     - Handles 404 (Item not found) and 409 (Conflict / already sold) with actionable toast notifications.

### Serving & CORS
- Frontend is served on `http://localhost:5500` via `frontend/serve.py` (lightweight Python server with no-cache headers).
- CORS headers on the Spring Boot backend (`http://localhost:8080`) allow `http://localhost:5500` with full support for `GET`, `POST`, `PUT`, `DELETE`, and `OPTIONS` requests.

### Verification & Testing
- **Read Endpoints Tested**:
  - `GET /api/items` -> 12 items retrieved successfully.
  - `GET /api/items/sorted` -> 9 available items returned in-order by `itemId`.
  - `GET /api/items/1` -> "Brass Astrolabe" retrieved from BST.
  - `GET /api/items/range?min=50000&max=100000` -> 3 items filtered by BST price range.
  - `GET /api/eras` -> 6 eras retrieved.
  - `GET /api/items/1/valuations` -> 2 appraisal records retrieved.
  - `GET /api/sales` -> Revenue ₹103,500.00 across 3 sales verified.
- **Mutation & Consistency Tested**:
  - Item creation (`POST /api/items`) -> Verified persisted in MySQL and immediately searchable in BST.
  - Item update (`PUT /api/items/{id}`) -> Verified changes reflected in BST and MySQL.
  - Valuation creation (`POST /api/valuations`) -> Verified attached to target item.
  - Item deletion (`DELETE /api/items/{id}`) -> Verified removed from both MySQL and BST (subsequent lookup returns 404).
- **Backend & Database Integrity**:
  - Zero modifications were made to the `Backend/` or `Database/` directories.

---

## 2. Operational Guide

### Starting the System

1. **Backend**:
   ```bash
   cd Backend
   mvn spring-boot:run
   ```
   *(Running on `http://localhost:8080`)*

2. **Frontend**:
   ```bash
   python frontend/serve.py
   ```
   *(Running on `http://localhost:5500`)*

3. **Browser Access**:
   Navigate to `http://localhost:5500` in Google Chrome, Edge, or Firefox.
