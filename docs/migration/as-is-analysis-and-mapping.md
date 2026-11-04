# As-Is Java NetBeans System Analysis & Web Migration Mapping

## 1. As-Is Java NetBeans System Inventory

- **Build System & UI Toolkit**: Apache Ant (`build.xml`, `nbproject/project.properties`) + Java Swing (`javax.swing.JFrame`) — no Maven or JavaFX/FXML files were used in the original project.
- **Database Connection**: `src/config/DatabaseConnection.java` connects to MySQL `localhost:3306/resort_pos` (`serverTimezone=Asia/Manila`).
- **Security Findings in As-Is Code**:
  - `util/Password.java` used unsalted `SHA-256`. **Web Fix**: Upgraded to PHP `password_hash()` / `password_verify()` with automatic transparent upgrade on login when a legacy 64-char SHA-256 hash is verified.
  - `util/SessionManager.java` stored user state in a static JVM field. **Web Fix**: Replaced with server-side PHP `$_SESSION` (`HttpOnly`, `SameSite=Lax`, `session_regenerate_id(true)`) + `X-CSRF-Token` validation on state-changing requests.

## 2. Screen & Feature Mapping

| Java Swing Class (`src/...`) | Role | Business Rules & Validations Preserved | Web React Component | PHP API Endpoint |
| :--- | :--- | :--- | :--- | :--- |
| `auth/LoginFrame.java` | Public | Authenticates `ACTIVE` `ADMIN` or `CASHIER`; blocks `CUSTOMER` or `INACTIVE` accounts. | `pages/LoginPage.jsx` | `POST /api/auth/login` |
| `admin/AdminDashboardFrame.java` | Admin | 9 quick-access cards with live search filter. | `pages/Dashboards.jsx` (`AdminDashboard`) | `GET /api/auth/me` |
| `cashier/CashierDashboardFrame.java` | Cashier/Admin | 6 numbered workflow cards (`01`–`06`). | `pages/Dashboards.jsx` (`CashierDashboard`) | `GET /api/auth/me` |
| `cashier/POSFrame.java` | Cashier/Admin | Entrance fees (`ADULT`, `CHILD`), optional Cottage (checks `adults + children <= capacity` & date overlap), optional Room (nights = `max(1, days)`, `room_guests <= max_capacity` & date overlap), payment methods (`Cash`, `GCash`, `Maya`, `MariBank`, `BDO`, `Credit/Debit Card`), requires `reference_no` for non-cash, locks rows with `FOR UPDATE`, generates `RS-yyyyMMdd-0001`. | `pages/POSPage.jsx` | `POST /api/pos/calculate`, `POST /api/pos/checkout` |
| `cashier/ReceiptFrame.java` | Cashier/Admin | 40-column formatted official receipt with line items, totals, change, and print trigger. | `components/ReceiptModal.jsx` | `GET /api/transactions/{txNo}` |
| `cashier/AvailabilityFrame.java` | Cashier/Admin | Checks all cottages and rooms against `start` and `end` (`FREE`, `BOOKED`, `NOT BOOKABLE`). | `pages/SharedModules.jsx` (`AvailabilityPage`) | `GET /api/availability` |
| `cashier/BookingListFrame.java` | Cashier/Admin | Read-only list of `ROOM` or `COTTAGE` bookings filtered by status. | `pages/SharedModules.jsx` (`BookingListPage`) | `GET /api/reservations/bookings/{type}` |
| `cashier/CustomerRegistrationFrame.java` | Cashier/Admin | Validates name (`^[A-Za-zÑñ .'-]+$`), PH phone (`^09\d{9}$`), email. | `pages/SharedModules.jsx` (`CustomerRegistrationPage`) | `GET /api/customers`, `POST /api/customers` |
| `cashier/CashierTransactionHistoryFram.java` | Cashier/Admin | Date-range filter (`from`, `to`), total sales sum, click row to view receipt. | `pages/SharedModules.jsx` (`TransactionHistoryPage`) | `GET /api/transactions` |
| `admin/ManageCashiersFrame.java` | Admin | Add cashier (`username` 4–20 chars, `password >= 6`), toggle `ACTIVE`/`INACTIVE`, reset password. | `pages/AdminModules.jsx` (`ManageCashiersPage`) | `GET/POST /api/cashiers`, `PATCH /api/cashiers/{id}/status`, `PATCH /api/cashiers/{id}/password` |
| `admin/ManageCustomersFrame.java` | Admin | Edit customer details and view account status. | `pages/AdminModules.jsx` (`ManageCustomersPage`) | `PUT /api/customers/{id}` |
| `admin/ManageCottagesFrame.java` | Admin | CRUD cottages, unique name check, blocks delete if `reservation_items` exist. | `pages/AdminModules.jsx` (`ManageCottagesPage`) | `GET/POST/PUT/DELETE /api/cottages` |
| `admin/ManageRoomsFrame.java` | Admin | CRUD rooms, unique name check, blocks delete if `reservation_items` exist. | `pages/AdminModules.jsx` (`ManageRoomsPage`) | `GET/POST/PUT/DELETE /api/rooms` |
| `admin/ManageEntranceFeesFrame.java` | Admin | Update description & price (`> 0`) for `ADULT` and `CHILD`. | `pages/AdminModules.jsx` (`ManageEntranceFeesPage`) | `GET/PUT /api/entrance-fees` |
| `admin/ManageReservationsFrame.java` | Admin | Transition status (`CHECKED_IN`, `COMPLETED`, `CANCELLED`); blocks modifying `CANCELLED`/`COMPLETED`. | `pages/AdminModules.jsx` (`ManageReservationsPage`) | `GET /api/reservations`, `PATCH /api/reservations/{id}/status` |
| `admin/SalesReportFrame.java` | Admin | Daily, Weekly (Mon–Sun), and Monthly sales breakdown by Entrance, Cottage, Room, and Total + Print. | `pages/AdminModules.jsx` (`SalesReportPage`) | `GET /api/reports/sales` |
