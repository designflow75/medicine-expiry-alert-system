-- ========================================================
-- Medicine Expiry Alert System - MySQL Database Schema
-- Database: medicine_expiry_db
-- ========================================================

CREATE DATABASE IF NOT EXISTS medicine_expiry_db
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE medicine_expiry_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    student_id VARCHAR(50),
    college_name VARCHAR(150),
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Medicines Table
CREATE TABLE IF NOT EXISTS medicines (
    medicine_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    medicine_name VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    batch_number VARCHAR(50) NOT NULL,
    quantity INT NOT NULL CHECK (quantity >= 0),
    purchase_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    storage_location VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_expiry_date (expiry_date),
    INDEX idx_category (category),
    INDEX idx_batch (batch_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed Demo Account
-- Demo Credentials: demo@medicine.com / Demo@123
INSERT INTO users (user_id, full_name, student_id, college_name, email, password)
VALUES (
    1,
    'Demo Student',
    'KTU-AIML-042',
    'KTU Institute of Technology',
    'demo@medicine.com',
    'ff96673205dc722320598ebf8f88325b2ac56922d5a2164b5765868274bc0d73'
)
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- Seed Initial Medicines for Demo Account across all 5 Expiry Tiers:
-- 1) Expired (< 0 days)
-- 2) Expires Today (0 days)
-- 3) Critical (1-7 days)
-- 4) Expiring Soon (8-30 days)
-- 5) Safe (> 30 days)
INSERT INTO medicines (user_id, medicine_name, category, batch_number, quantity, purchase_date, expiry_date, storage_location, notes)
SELECT 1, 'Amoxicillin 500mg', 'Antibiotics', 'AMX-2024-09', 12, DATE_SUB(CURRENT_DATE, INTERVAL 180 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 15 DAY), 'Medicine Cabinet A', 'Prescription antibiotic. Expired past shelf-life.'
WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE user_id = 1 AND batch_number = 'AMX-2024-09');

INSERT INTO medicines (user_id, medicine_name, category, batch_number, quantity, purchase_date, expiry_date, storage_location, notes)
SELECT 1, 'Cough Syrup Dextromethorphan', 'Syrups', 'DXM-2024-11', 1, DATE_SUB(CURRENT_DATE, INTERVAL 90 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), 'First Aid Box', 'Bottle opened 3 months ago. Do not use.'
WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE user_id = 1 AND batch_number = 'DXM-2024-11');

INSERT INTO medicines (user_id, medicine_name, category, batch_number, quantity, purchase_date, expiry_date, storage_location, notes)
SELECT 1, 'Hydrocortisone Cream 1%', 'Ointments', 'HYD-TODAY-01', 2, DATE_SUB(CURRENT_DATE, INTERVAL 60 DAY), CURRENT_DATE, 'Top Drawer', 'Expires today! Use or discard immediately.'
WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE user_id = 1 AND batch_number = 'HYD-TODAY-01');

INSERT INTO medicines (user_id, medicine_name, category, batch_number, quantity, purchase_date, expiry_date, storage_location, notes)
SELECT 1, 'Insulin Glargine 100 IU/ml', 'Injections', 'INS-7889-C', 3, DATE_SUB(CURRENT_DATE, INTERVAL 45 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY), 'Refrigerator (2-8 C)', 'Keep refrigerated. Expiring in 3 days.'
WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE user_id = 1 AND batch_number = 'INS-7889-C');

INSERT INTO medicines (user_id, medicine_name, category, batch_number, quantity, purchase_date, expiry_date, storage_location, notes)
SELECT 1, 'Salbutamol Inhaler 100mcg', 'Respiratory', 'SAL-2025-04', 1, DATE_SUB(CURRENT_DATE, INTERVAL 60 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), 'Emergency Bag', 'Rescue inhaler. Reorder soon.'
WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE user_id = 1 AND batch_number = 'SAL-2025-04');

INSERT INTO medicines (user_id, medicine_name, category, batch_number, quantity, purchase_date, expiry_date, storage_location, notes)
SELECT 1, 'Cetirizine 10mg', 'Antihistamines', 'CET-8812-E', 20, DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY), 'Medicine Cabinet A', 'Allergy relief tablets.'
WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE user_id = 1 AND batch_number = 'CET-8812-E');

INSERT INTO medicines (user_id, medicine_name, category, batch_number, quantity, purchase_date, expiry_date, storage_location, notes)
SELECT 1, 'Ibuprofen 400mg', 'Pain Relief', 'IBU-9921-X', 15, DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 25 DAY), 'Medicine Cabinet B', 'Anti-inflammatory tablets.'
WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE user_id = 1 AND batch_number = 'IBU-9921-X');

INSERT INTO medicines (user_id, medicine_name, category, batch_number, quantity, purchase_date, expiry_date, storage_location, notes)
SELECT 1, 'Paracetamol 650mg', 'Analgesics', 'PCM-650-SAFE', 30, DATE_SUB(CURRENT_DATE, INTERVAL 15 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 180 DAY), 'Medicine Cabinet A', 'Common fever and pain relief.'
WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE user_id = 1 AND batch_number = 'PCM-650-SAFE');

INSERT INTO medicines (user_id, medicine_name, category, batch_number, quantity, purchase_date, expiry_date, storage_location, notes)
SELECT 1, 'Vitamin C 500mg Chewable', 'Supplements', 'VTC-500-SAFE', 50, DATE_SUB(CURRENT_DATE, INTERVAL 10 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 365 DAY), 'Kitchen Shelf', 'Immunity booster vitamins.'
WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE user_id = 1 AND batch_number = 'VTC-500-SAFE');

INSERT INTO medicines (user_id, medicine_name, category, batch_number, quantity, purchase_date, expiry_date, storage_location, notes)
SELECT 1, 'Metformin 500mg', 'Diabetes', 'MET-500-SAFE', 60, DATE_SUB(CURRENT_DATE, INTERVAL 20 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 240 DAY), 'Bedroom Drawer', 'Daily blood sugar control medication.'
WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE user_id = 1 AND batch_number = 'MET-500-SAFE');
