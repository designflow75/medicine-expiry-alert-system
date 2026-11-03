-- =====================================================================
-- Medicine Expiry Alert System - Supabase / PostgreSQL Database Schema
-- College OOP Practical Project (KTU S3 B.Tech AI/ML)
-- =====================================================================

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    student_id VARCHAR(50),
    college_name VARCHAR(150),
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);

-- 2. Medicines Table
CREATE TABLE IF NOT EXISTS medicines (
    medicine_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    medicine_name VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    batch_number VARCHAR(50) NOT NULL,
    quantity INT NOT NULL CHECK (quantity >= 0),
    purchase_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    storage_location VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_med_expiry_date ON medicines (expiry_date);
CREATE INDEX IF NOT EXISTS idx_med_category ON medicines (category);
CREATE INDEX IF NOT EXISTS idx_med_batch ON medicines (batch_number);

-- 3. Seed Demo Account
-- Demo credentials: demo@medicine.com / Demo@123
-- SHA-256 hash of "Demo@123": 3cf8d4512e96d11f67f22dd3bc461c28c89ffb5749f99ad5f2f534438fa74136
INSERT INTO users (user_id, full_name, student_id, college_name, email, password)
VALUES (1, 'Demo Student', 'KTU-AIML-042', 'KTU College of Engineering', 'demo@medicine.com', '3cf8d4512e96d11f67f22dd3bc461c28c89ffb5749f99ad5f2f534438fa74136')
ON CONFLICT (email) DO UPDATE SET full_name = EXCLUDED.full_name, password = EXCLUDED.password;

SELECT setval(pg_get_serial_sequence('users', 'user_id'), COALESCE((SELECT MAX(user_id) FROM users), 1));

-- 4. Sample Seed Medicines for Demo Account
INSERT INTO medicines (user_id, medicine_name, category, batch_number, quantity, purchase_date, expiry_date, storage_location, notes)
VALUES
(1, 'Amoxicillin 500mg', 'Antibiotics', 'AMX-2024-09', 12, CURRENT_DATE - INTERVAL '180 days', CURRENT_DATE - INTERVAL '15 days', 'Medicine Cabinet A', 'Prescription antibiotic. Expired past shelf-life.'),
(1, 'Cough Syrup Dextromethorphan', 'Syrups', 'DXM-2024-11', 1, CURRENT_DATE - INTERVAL '90 days', CURRENT_DATE - INTERVAL '3 days', 'First Aid Box', 'Bottle opened 3 months ago. Do not use.'),
(1, 'Hydrocortisone Cream 1%', 'Ointments', 'HYD-TODAY-01', 2, CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE, 'Top Drawer', 'Expires today! Use or discard immediately.'),
(1, 'Insulin Glargine 100 IU/ml', 'Injections', 'INS-7889-C', 3, CURRENT_DATE - INTERVAL '45 days', CURRENT_DATE + INTERVAL '3 days', 'Refrigerator (2-8 C)', 'Keep refrigerated. Expiring in 3 days.'),
(1, 'Salbutamol Inhaler 100mcg', 'Respiratory', 'SAL-2025-04', 1, CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE + INTERVAL '6 days', 'Emergency Bag', 'Rescue inhaler. Reorder soon.'),
(1, 'Cetirizine 10mg', 'Antihistamines', 'CET-8812-E', 20, CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE + INTERVAL '14 days', 'Medicine Cabinet A', 'Allergy relief tablets.'),
(1, 'Ibuprofen 400mg', 'Pain Relief', 'IBU-9921-X', 15, CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE + INTERVAL '25 days', 'Medicine Cabinet B', 'Anti-inflammatory tablets.'),
(1, 'Paracetamol 650mg', 'Analgesics', 'PCM-650-SAFE', 30, CURRENT_DATE - INTERVAL '15 days', CURRENT_DATE + INTERVAL '180 days', 'Medicine Cabinet A', 'Common fever and pain relief.'),
(1, 'Vitamin C 500mg Chewable', 'Supplements', 'VTC-500-SAFE', 50, CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE + INTERVAL '365 days', 'Kitchen Shelf', 'Immunity booster vitamins.'),
(1, 'Metformin 500mg', 'Diabetes', 'MET-500-SAFE', 60, CURRENT_DATE - INTERVAL '20 days', CURRENT_DATE + INTERVAL '240 days', 'Bedroom Drawer', 'Daily blood sugar control medication.');

SELECT setval(pg_get_serial_sequence('medicines', 'medicine_id'), COALESCE((SELECT MAX(medicine_id) FROM medicines), 1));
