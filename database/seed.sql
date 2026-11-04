USE resort_pos;

-- Default users:
-- admin / admin123 (SHA-256 hash compatible with legacy Java app; auto-upgraded to bcrypt on PHP login)
-- cashier1 / cashier123
INSERT INTO users (username, password_hash, full_name, role, status) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Resort Administrator', 'ADMIN', 'ACTIVE'),
('cashier1', 'da23890e111536e631be13a06bd45d82c592efc22b0711906e068971596069a4', 'Maria Santos', 'CASHIER', 'ACTIVE')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

-- Sample Cottages
INSERT INTO cottages (cottage_name, cottage_type, capacity, rental_price, status) VALUES
('Cottage 1', 'Small Kubo', 6, 500.00, 'AVAILABLE'),
('Cottage 2', 'Medium Kubo', 12, 850.00, 'AVAILABLE'),
('Pavilion A', 'Family Pavilion', 25, 1800.00, 'AVAILABLE')
ON DUPLICATE KEY UPDATE rental_price = VALUES(rental_price);

-- Sample Rooms
INSERT INTO rooms (room_name, room_type, max_capacity, price_per_night, status) VALUES
('Room 101', 'Standard AC Room', 2, 1500.00, 'AVAILABLE'),
('Room 102', 'Deluxe Family Room', 4, 2500.00, 'AVAILABLE'),
('Villa 1', 'Beachfront Villa', 8, 4500.00, 'AVAILABLE')
ON DUPLICATE KEY UPDATE price_per_night = VALUES(price_per_night);

-- Sample Walk-in / Registered Customers
INSERT INTO customers (user_id, full_name, contact_number, email, address) VALUES
(NULL, 'Juan Dela Cruz', '09171234567', 'juan@example.com', 'Batangas City'),
(NULL, 'Ana Reyes', '09187654321', 'ana.reyes@example.com', 'Lipa City');
