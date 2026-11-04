# Database Schema Reference (`resort_pos`)

All tables use `InnoDB` and `utf8mb4_unicode_ci`. Monetary columns use `DECIMAL(10, 2)`.

## Tables

1. **`users`**: `user_id` (PK), `username` (UNIQUE), `password_hash`, `full_name`, `role` (`ADMIN`, `CASHIER`, `CUSTOMER`), `status` (`ACTIVE`, `INACTIVE`), `created_at`
2. **`customers`**: `customer_id` (PK), `user_id` (FK -> `users.user_id`), `full_name`, `contact_number`, `email`, `address`, `created_at`
3. **`cottages`**: `cottage_id` (PK), `cottage_name` (UNIQUE), `cottage_type`, `capacity`, `rental_price` (`DECIMAL(10,2)`), `status` (`AVAILABLE`, `UNAVAILABLE`, `MAINTENANCE`)
4. **`rooms`**: `room_id` (PK), `room_name` (UNIQUE), `room_type`, `max_capacity`, `price_per_night` (`DECIMAL(10,2)`), `status` (`AVAILABLE`, `UNAVAILABLE`, `MAINTENANCE`)
5. **`entrance_fees`**: `fee_id` (PK), `fee_type` (`ADULT`, `CHILD` UNIQUE), `description`, `price` (`DECIMAL(10,2)`)
6. **`reservations`**: `reservation_id` (PK), `customer_id` (FK), `reservation_status` (`PENDING`, `CONFIRMED`, `CHECKED_IN`, `COMPLETED`, `CANCELLED`), `payment_status` (`UNPAID`, `PAID`, `CANCELLED`), `total_amount`, `amount_paid`, `created_by` (FK), `notes`, `created_at`
7. **`reservation_items`**: `item_id` (PK), `reservation_id` (FK), `item_type` (`COTTAGE`, `ROOM`), `cottage_id` (FK), `room_id` (FK), `start_datetime`, `end_datetime`, `guests`, `unit_price`, `subtotal`
8. **`transactions`**: `transaction_no` (UNIQUE, `RS-yyyyMMdd-0001`), `customer_id` (FK), `reservation_id` (FK), `cashier_id` (FK), `subtotal`, `discount`, `total_amount`, `amount_received`, `change_amount`, `payment_method`, `reference_no`, `transaction_date`
9. **`transaction_items`**: `transaction_item_id` (PK), `transaction_id` (FK), `item_type` (`ENTRANCE_ADULT`, `ENTRANCE_CHILD`, `COTTAGE`, `ROOM`), `item_ref_id`, `description`, `quantity`, `unit_price`, `subtotal`
10. **`audit_logs`**: `log_id` (PK), `user_id`, `action`, `details`, `created_at`
