-- =============================================================
-- AR Industries (Vyapar AR / ARI-OS) - V1 Initial Schema
-- Flyway Migration Script
-- =============================================================

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_users_role (role),
    INDEX idx_users_active (active)
);

-- Invoice sequence for concurrency-safe numbering
CREATE TABLE IF NOT EXISTS invoice_sequence (
    id BIGINT NOT NULL PRIMARY KEY,
    seq_value BIGINT NOT NULL DEFAULT 1000
);
INSERT INTO invoice_sequence (id, seq_value) VALUES (1, 1000) ON DUPLICATE KEY UPDATE id=id;

-- Customers table
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_code VARCHAR(20) UNIQUE,
    customer_name VARCHAR(200) NOT NULL,
    company_name VARCHAR(200),
    phone VARCHAR(20),
    alternate_phone VARCHAR(20),
    email VARCHAR(150),
    gstin VARCHAR(20) UNIQUE,
    pan VARCHAR(15),
    billing_address TEXT,
    shipping_address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    state_code VARCHAR(5),
    pincode VARCHAR(10),
    credit_limit DECIMAL(15,2) DEFAULT 0.00,
    payment_terms VARCHAR(50),
    opening_balance DECIMAL(15,2) DEFAULT 0.00,
    outstanding_balance DECIMAL(15,2) DEFAULT 0.00,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_customers_name (customer_name),
    INDEX idx_customers_active (active),
    INDEX idx_customers_state (state)
);

-- Price records table
CREATE TABLE IF NOT EXISTS price_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    price_code VARCHAR(20) NOT NULL,
    category VARCHAR(30) NOT NULL,
    thickness VARCHAR(10) NOT NULL,
    product_type VARCHAR(50) NOT NULL,
    size VARCHAR(20) NOT NULL,
    packaging VARCHAR(50),
    pcs_per_box INT NOT NULL DEFAULT 20,
    wholesale_rate DECIMAL(10,2) NOT NULL,
    retail_rate DECIMAL(10,2) NOT NULL,
    wholesale_box_rate DECIMAL(10,2),
    retail_box_rate DECIMAL(10,2),
    effective_from DATE NOT NULL,
    effective_to DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    notes TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_price_variant_date (thickness, product_type, size, effective_from),
    INDEX idx_price_category (category),
    INDEX idx_price_thickness (thickness),
    INDEX idx_price_active (active)
);

-- Inventory table
CREATE TABLE IF NOT EXISTS inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_code VARCHAR(30) NOT NULL UNIQUE,
    item_name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    thickness VARCHAR(10) NOT NULL,
    product_type VARCHAR(50) NOT NULL,
    size VARCHAR(20) NOT NULL,
    packaging VARCHAR(50),
    pcs_per_box INT NOT NULL DEFAULT 20,
    current_stock INT NOT NULL DEFAULT 0,
    minimum_stock INT NOT NULL DEFAULT 0,
    unit VARCHAR(20) DEFAULT 'Pcs',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_inventory_category (category),
    INDEX idx_inventory_low_stock (current_stock, minimum_stock)
);

-- Stock movements table
CREATE TABLE IF NOT EXISTS stock_movements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inventory_id BIGINT NOT NULL,
    movement_type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    stock_before INT NOT NULL,
    stock_after INT NOT NULL,
    reference_type VARCHAR(50),
    reference_id VARCHAR(50),
    notes TEXT,
    performed_by VARCHAR(100),
    created_at DATETIME NOT NULL,
    FOREIGN KEY (inventory_id) REFERENCES inventory(id) ON DELETE RESTRICT,
    INDEX idx_stock_movement_inventory (inventory_id),
    INDEX idx_stock_movement_type (movement_type),
    INDEX idx_stock_movement_date (created_at)
);

-- Invoices table
CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(30) NOT NULL UNIQUE,
    invoice_date DATE NOT NULL,
    due_date DATE,
    customer_id BIGINT NOT NULL,
    customer_gstin VARCHAR(20),
    billing_address TEXT,
    shipping_address TEXT,
    place_of_supply VARCHAR(100),
    is_interstate BOOLEAN NOT NULL DEFAULT FALSE,
    subtotal DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    cgst DECIMAL(15,2) DEFAULT 0.00,
    sgst DECIMAL(15,2) DEFAULT 0.00,
    igst DECIMAL(15,2) DEFAULT 0.00,
    round_off DECIMAL(15,2) DEFAULT 0.00,
    grand_total DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    amount_paid DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    balance_due DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    invoice_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    payment_mode VARCHAR(20),
    notes TEXT,
    terms_and_conditions TEXT,
    created_by VARCHAR(100),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
    INDEX idx_invoices_customer (customer_id),
    INDEX idx_invoices_date (invoice_date),
    INDEX idx_invoices_status (invoice_status),
    INDEX idx_invoices_payment_status (payment_status)
);

-- Invoice items table
CREATE TABLE IF NOT EXISTS invoice_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    category VARCHAR(50),
    thickness VARCHAR(10),
    product_type VARCHAR(50),
    size VARCHAR(20),
    packaging VARCHAR(50),
    pcs_per_box INT NOT NULL DEFAULT 20,
    quantity INT NOT NULL,
    billing_unit VARCHAR(10) DEFAULT 'Boxes',
    rate_per_pc DECIMAL(10,2) NOT NULL,
    rate_per_box DECIMAL(10,2) NOT NULL,
    discount_percent DECIMAL(10,2) DEFAULT 0.00,
    taxable_amount DECIMAL(15,2) NOT NULL,
    cgst_percent DECIMAL(10,2) DEFAULT 0.00,
    sgst_percent DECIMAL(10,2) DEFAULT 0.00,
    igst_percent DECIMAL(10,2) DEFAULT 0.00,
    cgst_amount DECIMAL(15,2) DEFAULT 0.00,
    sgst_amount DECIMAL(15,2) DEFAULT 0.00,
    igst_amount DECIMAL(15,2) DEFAULT 0.00,
    total_amount DECIMAL(15,2) NOT NULL,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    INDEX idx_invoice_items_invoice (invoice_id)
);

-- Payments table
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    invoice_id BIGINT,
    payment_date DATE NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    payment_mode VARCHAR(20) NOT NULL,
    reference_number VARCHAR(100),
    notes TEXT,
    reversed BOOLEAN NOT NULL DEFAULT FALSE,
    recorded_by VARCHAR(100),
    created_at DATETIME NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE SET NULL,
    INDEX idx_payments_customer (customer_id),
    INDEX idx_payments_invoice (invoice_id),
    INDEX idx_payments_date (payment_date)
);

-- Employees table
CREATE TABLE IF NOT EXISTS employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_code VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150),
    phone VARCHAR(20) NOT NULL,
    date_of_birth DATE,
    address TEXT,
    joining_date DATE NOT NULL,
    department VARCHAR(100),
    designation VARCHAR(100),
    employment_type VARCHAR(50),
    work_location VARCHAR(100),
    shift VARCHAR(50),
    basic_salary DECIMAL(12,2) DEFAULT 0.00,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_employees_dept (department),
    INDEX idx_employees_active (active),
    INDEX idx_employees_code (employee_code)
);

-- Attendance table
CREATE TABLE IF NOT EXISTS attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    check_in DATETIME,
    check_out DATETIME,
    status VARCHAR(20) NOT NULL DEFAULT 'ABSENT',
    working_hours DOUBLE,
    notes TEXT,
    marked_by VARCHAR(100),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_attendance_emp_date (employee_id, attendance_date),
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    INDEX idx_attendance_date (attendance_date),
    INDEX idx_attendance_status (status)
);

-- Audit logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    module VARCHAR(50) NOT NULL,
    description TEXT,
    entity_type VARCHAR(50),
    entity_id VARCHAR(50),
    timestamp DATETIME NOT NULL,
    INDEX idx_audit_module (module),
    INDEX idx_audit_timestamp (timestamp),
    INDEX idx_audit_actor (actor)
);

-- =============================================================
-- Seed Data: Initial Admin User
-- Password: admin123 (BCrypt hash)
-- =============================================================
INSERT INTO users (username, password_hash, email, full_name, role, active, created_at, updated_at)
VALUES (
    'admin',
    '$2a$12$HSJlHRlqpRCE4W8mk2jvge726eCJIMWp/aoZ/H4Kr1afSBqr.1tNm',
    'admin@arindustries.in',
    'A. Rahman (Admin)',
    'SUPER_ADMIN',
    TRUE,
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE username=username;

-- =============================================================
-- Seed Data: Initial Customers
-- =============================================================
INSERT INTO customers (customer_code, customer_name, company_name, phone, gstin, state, state_code, billing_address, outstanding_balance, active, created_at, updated_at)
VALUES
('CUST-001', 'Vidarbha Electrical Traders', 'Vidarbha Electrical Traders', '9823011223', '27AABCV1234F1Z1', 'Maharashtra', '27', 'Ganjakhet Chowk, Itwari, Nagpur', 32000.00, TRUE, NOW(), NOW()),
('CUST-002', 'Mahalaxmi Electrical Hardware', 'Mahalaxmi Electrical Hardware', '9822455667', '27BAAPS5544K1ZY', 'Maharashtra', '27', 'Central Avenue, Gandhibagh, Nagpur', 14200.00, TRUE, NOW(), NOW()),
('CUST-003', 'Raipur Power Switchgears', 'Raipur Power Switchgears', '9425211998', '22AAJCP9876Q1Z0', 'Chhattisgarh', '22', 'Gudhiyari Industrial Area, Raipur', 64800.00, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE customer_code=customer_code;

-- =============================================================
-- Seed Data: Price Master (Modular Box - 0.8mm)
-- =============================================================
INSERT INTO price_records (price_code, category, thickness, product_type, size, packaging, pcs_per_box, wholesale_rate, retail_rate, wholesale_box_rate, retail_box_rate, effective_from, active, created_at, updated_at) VALUES
('PL-08-S2',  'Modular Box', '0.8 mm', 'Normal Clip', '2M',     '20 Pcs / Box', 20, 21.50,  25.00,  430.00,   500.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-08-B2',  'Modular Box', '0.8 mm', 'Box Clip',   '2M',     '20 Pcs / Box', 20, 23.50,  27.50,  470.00,   550.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-08-S3',  'Modular Box', '0.8 mm', 'Normal Clip', '3M',     '20 Pcs / Box', 20, 26.00,  30.50,  520.00,   610.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-08-B3',  'Modular Box', '0.8 mm', 'Box Clip',   '3M',     '20 Pcs / Box', 20, 28.50,  33.00,  570.00,   660.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-08-S4',  'Modular Box', '0.8 mm', 'Normal Clip', '4M',     '20 Pcs / Box', 20, 32.00,  37.50,  640.00,   750.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-08-B4',  'Modular Box', '0.8 mm', 'Box Clip',   '4M',     '20 Pcs / Box', 20, 34.50,  40.00,  690.00,   800.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-08-S6',  'Modular Box', '0.8 mm', 'Normal Clip', '6M',     '10 Pcs / Box', 10, 46.75,  54.00,  467.50,   540.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-08-B6',  'Modular Box', '0.8 mm', 'Box Clip',   '6M',     '10 Pcs / Box', 10, 49.50,  57.50,  495.00,   575.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-08-S8H', 'Modular Box', '0.8 mm', 'Normal Clip', '8M(H)',  '10 Pcs / Box', 10, 58.00,  67.00,  580.00,   670.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-08-B8H', 'Modular Box', '0.8 mm', 'Box Clip',   '8M(H)',  '10 Pcs / Box', 10, 61.00,  70.50,  610.00,   705.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-08-S8S', 'Modular Box', '0.8 mm', 'Normal Clip', '8M(S)',  '10 Pcs / Box', 10, 60.00,  69.50,  600.00,   695.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-08-B8S', 'Modular Box', '0.8 mm', 'Box Clip',   '8M(S)',  '10 Pcs / Box', 10, 63.50,  73.00,  635.00,   730.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-08-S12', 'Modular Box', '0.8 mm', 'Normal Clip', '12M',    '10 Pcs / Box', 10, 78.00,  90.00,  780.00,   900.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-08-B12', 'Modular Box', '0.8 mm', 'Box Clip',   '12M',    '10 Pcs / Box', 10, 82.00,  95.00,  820.00,   950.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-08-S16', 'Modular Box', '0.8 mm', 'Normal Clip', '16M',    '6 Pcs / Box',   6, 98.00, 114.00,  588.00,   684.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-08-B16', 'Modular Box', '0.8 mm', 'Box Clip',   '16M',    '6 Pcs / Box',   6,104.00, 120.00,  624.00,   720.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-08-S18', 'Modular Box', '0.8 mm', 'Normal Clip', '18M',    '6 Pcs / Box',   6,112.00, 130.00,  672.00,   780.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-08-B18', 'Modular Box', '0.8 mm', 'Box Clip',   '18M',    '6 Pcs / Box',   6,118.00, 137.00,  708.00,   822.00,  '2026-01-01', TRUE, NOW(), NOW()),
-- 0.7mm
('PL-07-S2',  'Modular Box', '0.7 mm', 'Normal Clip', '2M',     '20 Pcs / Box', 20, 18.50,  21.50,  370.00,   430.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-07-B2',  'Modular Box', '0.7 mm', 'Box Clip',   '2M',     '20 Pcs / Box', 20, 20.50,  24.00,  410.00,   480.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-07-S3',  'Modular Box', '0.7 mm', 'Normal Clip', '3M',     '20 Pcs / Box', 20, 22.00,  25.50,  440.00,   510.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-07-B3',  'Modular Box', '0.7 mm', 'Box Clip',   '3M',     '20 Pcs / Box', 20, 24.00,  28.00,  480.00,   560.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-07-S4',  'Modular Box', '0.7 mm', 'Normal Clip', '4M',     '20 Pcs / Box', 20, 27.50,  32.00,  550.00,   640.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-07-B4',  'Modular Box', '0.7 mm', 'Box Clip',   '4M',     '20 Pcs / Box', 20, 29.50,  34.50,  590.00,   690.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-07-S6',  'Modular Box', '0.7 mm', 'Normal Clip', '6M',     '10 Pcs / Box', 10, 39.00,  45.00,  390.00,   450.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-07-B6',  'Modular Box', '0.7 mm', 'Box Clip',   '6M',     '10 Pcs / Box', 10, 41.50,  48.00,  415.00,   480.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-07-S8H', 'Modular Box', '0.7 mm', 'Normal Clip', '8M(H)',  '10 Pcs / Box', 10, 49.00,  57.00,  490.00,   570.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-07-B8H', 'Modular Box', '0.7 mm', 'Box Clip',   '8M(H)',  '10 Pcs / Box', 10, 52.00,  60.00,  520.00,   600.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-07-S8S', 'Modular Box', '0.7 mm', 'Normal Clip', '8M(S)',  '10 Pcs / Box', 10, 51.00,  59.00,  510.00,   590.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-07-B8S', 'Modular Box', '0.7 mm', 'Box Clip',   '8M(S)',  '10 Pcs / Box', 10, 54.00,  62.50,  540.00,   625.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-07-S12', 'Modular Box', '0.7 mm', 'Normal Clip', '12M',    '10 Pcs / Box', 10, 67.00,  77.00,  670.00,   770.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-07-B12', 'Modular Box', '0.7 mm', 'Box Clip',   '12M',    '10 Pcs / Box', 10, 71.00,  82.00,  710.00,   820.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-07-S16', 'Modular Box', '0.7 mm', 'Normal Clip', '16M',    '6 Pcs / Box',   6, 84.00,  97.00,  504.00,   582.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-07-B16', 'Modular Box', '0.7 mm', 'Box Clip',   '16M',    '6 Pcs / Box',   6, 89.00, 103.00,  534.00,   618.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-07-S18', 'Modular Box', '0.7 mm', 'Normal Clip', '18M',    '6 Pcs / Box',   6, 96.00, 111.00,  576.00,   666.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-07-B18', 'Modular Box', '0.7 mm', 'Box Clip',   '18M',    '6 Pcs / Box',   6,101.00, 117.00,  606.00,   702.00,  '2026-01-01', TRUE, NOW(), NOW()),
-- 1.0mm
('PL-10-S2',  'Modular Box', '1 mm',   'Normal Clip', '2M',     '20 Pcs / Box', 20, 26.50,  31.00,  530.00,   620.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-10-B2',  'Modular Box', '1 mm',   'Box Clip',   '2M',     '20 Pcs / Box', 20, 29.00,  34.00,  580.00,   680.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-10-S3',  'Modular Box', '1 mm',   'Normal Clip', '3M',     '20 Pcs / Box', 20, 31.50,  36.50,  630.00,   730.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-10-B3',  'Modular Box', '1 mm',   'Box Clip',   '3M',     '20 Pcs / Box', 20, 34.50,  40.00,  690.00,   800.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-10-S4',  'Modular Box', '1 mm',   'Normal Clip', '4M',     '20 Pcs / Box', 20, 39.00,  45.00,  780.00,   900.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-10-B4',  'Modular Box', '1 mm',   'Box Clip',   '4M',     '20 Pcs / Box', 20, 42.00,  49.00,  840.00,   980.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-10-S6',  'Modular Box', '1 mm',   'Normal Clip', '6M',     '10 Pcs / Box', 10, 56.50,  65.00,  565.00,   650.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-10-B6',  'Modular Box', '1 mm',   'Box Clip',   '6M',     '10 Pcs / Box', 10, 60.00,  69.50,  600.00,   695.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-10-S8H', 'Modular Box', '1 mm',   'Normal Clip', '8M(H)',  '10 Pcs / Box', 10, 69.50,  80.00,  695.00,   800.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-10-B8H', 'Modular Box', '1 mm',   'Box Clip',   '8M(H)',  '10 Pcs / Box', 10, 74.00,  85.50,  740.00,   855.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-10-S8S', 'Modular Box', '1 mm',   'Normal Clip', '8M(S)',  '10 Pcs / Box', 10, 72.00,  83.00,  720.00,   830.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-10-B8S', 'Modular Box', '1 mm',   'Box Clip',   '8M(S)',  '10 Pcs / Box', 10, 77.00,  89.00,  770.00,   890.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-10-S12', 'Modular Box', '1 mm',   'Normal Clip', '12M',    '10 Pcs / Box', 10, 94.00, 108.00,  940.00,  1080.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-10-B12', 'Modular Box', '1 mm',   'Box Clip',   '12M',    '10 Pcs / Box', 10, 99.50, 115.00,  995.00,  1150.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-10-S16', 'Modular Box', '1 mm',   'Normal Clip', '16M',    '6 Pcs / Box',   6,119.00, 138.00,  714.00,   828.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-10-B16', 'Modular Box', '1 mm',   'Box Clip',   '16M',    '6 Pcs / Box',   6,126.00, 145.00,  756.00,   870.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-10-S18', 'Modular Box', '1 mm',   'Normal Clip', '18M',    '6 Pcs / Box',   6,136.00, 157.00,  816.00,   942.00,  '2026-01-01', TRUE, NOW(), NOW()),
('PL-10-B18', 'Modular Box', '1 mm',   'Box Clip',   '18M',    '6 Pcs / Box',   6,144.00, 166.00,  864.00,   996.00,  '2026-01-01', TRUE, NOW(), NOW()),
-- Fan Box
('PL-FB-16R', 'Fan Box', '1.6 mm', 'Heavy Rod Hook',     '4 Inch Deep',    '20 Pcs / Box', 20, 44.00, 52.00,  880.00, 1040.00, '2026-01-01', TRUE, NOW(), NOW()),
('PL-FB-12S', 'Fan Box', '1.2 mm', 'Standard Concealed', '4 Inch Standard','25 Pcs / Box', 25, 36.00, 42.00,  900.00, 1050.00, '2026-01-01', TRUE, NOW(), NOW()),
('PL-FB-10H', 'Fan Box', '1.0 mm', 'Hexagonal',          'Standard Hex',   '25 Pcs / Box', 25, 31.00, 36.50,  775.00,  912.50, '2026-01-01', TRUE, NOW(), NOW()),
('PL-FB-16D', 'Fan Box', '1.6 mm', 'Deep Fan Box',       'Heavy Duty',     '20 Pcs / Box', 20, 48.00, 56.00,  960.00, 1120.00, '2026-01-01', TRUE, NOW(), NOW()),
('PL-FB-08E', 'Fan Box', '0.8 mm', 'Standard Concealed', '4 Inch Standard','25 Pcs / Box', 25, 27.00, 32.00,  675.00,  800.00, '2026-01-01', TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE price_code=price_code;

-- =============================================================
-- Seed Data: Inventory
-- =============================================================
INSERT INTO inventory (item_code, item_name, category, thickness, product_type, size, packaging, pcs_per_box, current_stock, minimum_stock, unit, active, created_at, updated_at) VALUES
('ARI-08-S2',  '0.8mm MS Modular Box (Normal Clip) 2M',         'Modular Box', '0.8 mm', 'Normal Clip', '2M',    '20 Pcs / Box', 20, 520, 100, 'Pcs', TRUE, NOW(), NOW()),
('ARI-08-B2',  '0.8mm MS Modular Box (Box Clip) 2M',            'Modular Box', '0.8 mm', 'Box Clip',   '2M',    '20 Pcs / Box', 20, 340,  80, 'Pcs', TRUE, NOW(), NOW()),
('ARI-08-S6',  '0.8mm MS Modular Box (Normal Clip) 6M',         'Modular Box', '0.8 mm', 'Normal Clip', '6M',    '10 Pcs / Box', 10, 190,  50, 'Pcs', TRUE, NOW(), NOW()),
('ARI-08-B8H', '0.8mm MS Modular Box (Box Clip) 8M(H)',         'Modular Box', '0.8 mm', 'Box Clip',   '8M(H)', '10 Pcs / Box', 10,  65, 100, 'Pcs', TRUE, NOW(), NOW()),
('ARI-07-S2',  '0.7mm MS Modular Box (Normal Clip) 2M',         'Modular Box', '0.7 mm', 'Normal Clip', '2M',    '20 Pcs / Box', 20, 680, 150, 'Pcs', TRUE, NOW(), NOW()),
('ARI-10-B12', '1.0mm Heavy MS Box (Box Clip) 12M',             'Modular Box', '1 mm',   'Box Clip',   '12M',   '10 Pcs / Box', 10,  85,  40, 'Pcs', TRUE, NOW(), NOW()),
('ARI-FB-16R', '1.6mm Heavy MS Fan Box (with Rod Hook) 4 Inch', 'Fan Box',     '1.6 mm', 'Heavy Rod Hook',     '4 Inch Deep',    '20 Pcs / Box', 20, 240,  60, 'Pcs', TRUE, NOW(), NOW()),
('ARI-FB-12S', '1.2mm Standard MS Fan Box 4 Inch',              'Fan Box',     '1.2 mm', 'Standard Concealed', '4 Inch Standard','25 Pcs / Box', 25, 380,  80, 'Pcs', TRUE, NOW(), NOW()),
('ARI-FB-10H', '1.0mm Hexagonal MS Fan Box',                    'Fan Box',     '1.0 mm', 'Hexagonal',          'Standard Hex',   '25 Pcs / Box', 25, 150,  40, 'Pcs', TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE item_code=item_code;

-- =============================================================
-- Seed Data: Employees
-- =============================================================
INSERT INTO employees (employee_code, first_name, last_name, phone, joining_date, department, designation, basic_salary, active, created_at, updated_at) VALUES
('EMP-101', 'Rameshwar', 'Deshmukh', '9823400111', '2020-01-15', 'Production',  'Plant Supervisor',    28000.00, TRUE, NOW(), NOW()),
('EMP-102', 'Sunil',     'Wankhede', '9823400222', '2021-03-01', 'Press Shop',  'Press Operator',      19500.00, TRUE, NOW(), NOW()),
('EMP-103', 'Pooja',     'Verma',    '9823400333', '2022-06-10', 'Finance',     'GST Accountant',      26000.00, TRUE, NOW(), NOW()),
('EMP-104', 'Mohammad',  'Irfan',    '9823400444', '2019-08-20', 'Tooling',     'Tool Room Machinist', 22000.00, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE employee_code=employee_code;
