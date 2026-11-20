-- Antique Collectibles Store System: database script (MySQL 8+/9)
-- Run:  mysql -u root -p < schema.sql

DROP DATABASE IF EXISTS antique_db;
CREATE DATABASE antique_db CHARACTER SET utf8mb4;
USE antique_db;

CREATE TABLE era (
  era_id     INT PRIMARY KEY AUTO_INCREMENT,
  era_name   VARCHAR(50) NOT NULL UNIQUE,
  start_year INT NOT NULL,
  end_year   INT NOT NULL,
  CHECK (start_year <= end_year)
);

CREATE TABLE item (
  item_id        INT PRIMARY KEY AUTO_INCREMENT,
  name           VARCHAR(100) NOT NULL,
  category       VARCHAR(50)  NOT NULL,
  era_id         INT NOT NULL,
  item_condition VARCHAR(30)  NOT NULL,
  price          DECIMAL(12,2) NOT NULL CHECK (price >= 0),
  status         ENUM('AVAILABLE','SOLD') NOT NULL DEFAULT 'AVAILABLE',
  FOREIGN KEY (era_id) REFERENCES era(era_id)
);

CREATE TABLE valuation (
  val_id          INT PRIMARY KEY AUTO_INCREMENT,
  item_id         INT NOT NULL,
  appraised_value DECIMAL(12,2) NOT NULL CHECK (appraised_value >= 0),
  val_date        DATE NOT NULL,
  appraiser       VARCHAR(80),
  FOREIGN KEY (item_id) REFERENCES item(item_id) ON DELETE CASCADE
);

CREATE TABLE sale (
  sale_id    INT PRIMARY KEY AUTO_INCREMENT,
  item_id    INT NOT NULL UNIQUE,
  buyer_name VARCHAR(100) NOT NULL,
  sale_price DECIMAL(12,2) NOT NULL CHECK (sale_price >= 0),
  sale_date  DATE NOT NULL,
  FOREIGN KEY (item_id) REFERENCES item(item_id)
);

CREATE INDEX idx_item_era    ON item(era_id);
CREATE INDEX idx_item_status ON item(status);
CREATE INDEX idx_val_item    ON valuation(item_id);

-- ---------- Sample data ----------
INSERT INTO era (era_name, start_year, end_year) VALUES
('Medieval',        1000, 1499),
('Renaissance',     1500, 1699),
('Georgian',        1700, 1836),
('Victorian',       1837, 1901),
('Art Nouveau',     1890, 1910),
('Art Deco',        1920, 1939);

INSERT INTO item (name, category, era_id, item_condition, price, status) VALUES
('Brass Astrolabe',            'Instruments', 2, 'Good',      185000.00, 'AVAILABLE'),
('Mahogany Writing Desk',      'Furniture',   3, 'Excellent', 245000.00, 'AVAILABLE'),
('Victorian Pocket Watch',     'Timepieces',  4, 'Good',       42000.00, 'SOLD'),
('Silver Tea Service',         'Silverware',  4, 'Excellent',  96000.00, 'AVAILABLE'),
('Tiffany-style Table Lamp',   'Lighting',    5, 'Fair',       58000.00, 'AVAILABLE'),
('Art Deco Cocktail Shaker',   'Barware',     6, 'Excellent',  27500.00, 'SOLD'),
('Iron Knight Gauntlet',       'Armour',      1, 'Fair',      310000.00, 'AVAILABLE'),
('Georgian Silver Candelabra', 'Silverware',  3, 'Good',       74000.00, 'AVAILABLE'),
('Renaissance Oil Portrait',   'Paintings',   2, 'Restored',  420000.00, 'AVAILABLE'),
('Victorian Porcelain Doll',   'Toys',        4, 'Good',       18500.00, 'AVAILABLE'),
('Art Nouveau Vase',           'Ceramics',    5, 'Excellent',  36000.00, 'SOLD'),
('Deco Radio Cabinet',         'Electronics', 6, 'Fair',       31000.00, 'AVAILABLE');

INSERT INTO valuation (item_id, appraised_value, val_date, appraiser) VALUES
(1, 170000.00, '2024-03-10', 'R. Menon'),
(1, 185000.00, '2025-09-02', 'R. Menon'),
(2, 230000.00, '2024-06-18', 'S. Iyer'),
(2, 245000.00, '2025-10-05', 'S. Iyer'),
(3,  38000.00, '2024-01-22', 'A. Kumar'),
(3,  42000.00, '2025-02-14', 'A. Kumar'),
(4,  90000.00, '2024-11-30', 'S. Iyer'),
(5,  52000.00, '2025-04-09', 'R. Menon'),
(6,  25000.00, '2024-08-12', 'A. Kumar'),
(7, 290000.00, '2024-12-01', 'R. Menon'),
(7, 310000.00, '2025-11-01', 'R. Menon'),
(8,  70000.00, '2025-05-20', 'S. Iyer'),
(9, 400000.00, '2024-07-07', 'M. Rao'),
(9, 420000.00, '2025-08-25', 'M. Rao'),
(10, 17000.00, '2025-01-15', 'A. Kumar'),
(11, 34000.00, '2024-10-03', 'S. Iyer'),
(12, 29000.00, '2025-06-30', 'A. Kumar');

INSERT INTO sale (item_id, buyer_name, sale_price, sale_date) VALUES
(3,  'Arjun Nair',   41000.00, '2025-03-05'),
(6,  'Meera Krishnan', 27000.00, '2025-07-19'),
(11, 'Rahul Verma',  35500.00, '2025-10-11');

-- ---------- Quick checks ----------
-- SELECT COUNT(*) FROM item;                       -- 12
-- SELECT status, COUNT(*) FROM item GROUP BY status; -- 9 AVAILABLE, 3 SOLD
-- SELECT i.name, e.era_name FROM item i JOIN era e USING (era_id);
