-- Promise Resort POS System - MySQL Schema (InnoDB)
-- Migrated from Java NetBeans (Swing/JDBC) to Web (React + PHP 8 PDO + MySQL)

CREATE DATABASE IF NOT EXISTS resort_pos
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE resort_pos;

-- 1. Users (Admins, Cashiers, optional Customer accounts)
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    role ENUM('ADMIN', 'CASHIER', 'CUSTOMER') NOT NULL DEFAULT 'CASHIER',
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Customers
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    full_name VARCHAR(120) NOT NULL,
    contact_number VARCHAR(20) NULL,
    email VARCHAR(120) NULL,
    address VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_customers_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_customers_name (full_name)
) ENGINE=InnoDB;

-- 3. Cottages
CREATE TABLE IF NOT EXISTS cottages (
    cottage_id INT AUTO_INCREMENT PRIMARY KEY,
    cottage_name VARCHAR(100) NOT NULL UNIQUE,
    cottage_type VARCHAR(80) NOT NULL,
    capacity INT NOT NULL,
    rental_price DECIMAL(10, 2) NOT NULL,
    status ENUM('AVAILABLE', 'UNAVAILABLE', 'MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE'
) ENGINE=InnoDB;

-- 4. Rooms
CREATE TABLE IF NOT EXISTS rooms (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    room_name VARCHAR(100) NOT NULL UNIQUE,
    room_type VARCHAR(80) NOT NULL,
    max_capacity INT NOT NULL,
    price_per_night DECIMAL(10, 2) NOT NULL,
    status ENUM('AVAILABLE', 'UNAVAILABLE', 'MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE'
) ENGINE=InnoDB;

-- 5. Reservations
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    reservation_status ENUM('PENDING', 'CONFIRMED', 'CHECKED_IN', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    payment_status ENUM('UNPAID', 'PAID', 'CANCELLED') NOT NULL DEFAULT 'PAID',
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    amount_paid DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    created_by INT NULL,
    notes VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_res_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT fk_res_creator FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_res_status (reservation_status)
) ENGINE=InnoDB;

-- 7. Reservation Items (Cottage / Room schedule blocks)
CREATE TABLE IF NOT EXISTS reservation_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT NOT NULL,
    item_type ENUM('COTTAGE', 'ROOM') NOT NULL,
    cottage_id INT NULL,
    room_id INT NULL,
    start_datetime DATETIME NOT NULL,
    end_datetime DATETIME NOT NULL,
    guests INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    subtotal DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_ri_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE CASCADE,
    CONSTRAINT fk_ri_cottage FOREIGN KEY (cottage_id) REFERENCES cottages(cottage_id),
    CONSTRAINT fk_ri_room FOREIGN KEY (room_id) REFERENCES rooms(room_id),
    INDEX idx_ri_cottage_dates (cottage_id, start_datetime, end_datetime),
    INDEX idx_ri_room_dates (room_id, start_datetime, end_datetime)
) ENGINE=InnoDB;

-- 8. Transactions (POS Sales & Payments)
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_no VARCHAR(30) NOT NULL UNIQUE,
    customer_id INT NOT NULL,
    reservation_id INT NULL,
    cashier_id INT NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    discount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    amount_received DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    change_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    payment_method VARCHAR(30) NOT NULL DEFAULT 'CASH',
    reference_no VARCHAR(80) NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tx_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT fk_tx_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE SET NULL,
    CONSTRAINT fk_tx_cashier FOREIGN KEY (cashier_id) REFERENCES users(user_id),
    INDEX idx_tx_date (transaction_date)
) ENGINE=InnoDB;

-- 9. Transaction Items (Line items for Receipt & Reports)
CREATE TABLE IF NOT EXISTS transaction_items (
    transaction_item_id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id INT NOT NULL,
    item_type ENUM('COTTAGE', 'ROOM') NOT NULL,
    item_ref_id INT NULL,
    description VARCHAR(200) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    subtotal DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_ti_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    INDEX idx_ti_type (item_type)
) ENGINE=InnoDB;

-- 10. Audit Logs (Web migration security enhancement)
CREATE TABLE IF NOT EXISTS audit_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    action VARCHAR(80) NOT NULL,
    details VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_user (user_id)
) ENGINE=InnoDB;
