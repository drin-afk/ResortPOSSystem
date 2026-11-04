# Promise Resort POS & Management System (Web Migration)

Migrated from **Java NetBeans (Java Swing + JDBC)** to **React.js + PHP 8 REST API (PDO) + MySQL (XAMPP)**.

## Architecture

```text
Browser (React.js + React Router + CSS3)
  -> Fetch API (JSON + Session Cookie + X-CSRF-Token)
  -> PHP 8 REST API (backend/index.php on Apache / XAMPP)
  -> PDO Prepared Statements & MySQL InnoDB Transactions (SELECT ... FOR UPDATE)
  -> MySQL Database (resort_pos)
```

## Project Structure

```text
ResortPOSSystem/
├── ResortPOSSystem/              # Original Java NetBeans source project (preserved)
├── frontend/                     # React.js (Vite) single-page application
│   ├── src/
│   │   ├── components/ReceiptModal.jsx
│   │   ├── pages/
│   │   │   ├── LoginPage.jsx
│   │   │   ├── Dashboards.jsx
│   │   │   ├── POSPage.jsx
│   │   │   ├── SharedModules.jsx
│   │   │   └── AdminModules.jsx
│   │   ├── services/api.js
│   │   ├── styles/app.css
│   │   ├── App.jsx
│   │   └── main.jsx
│   ├── package.json
│   └── vite.config.js
├── backend/                      # PHP 8 REST API (deployed to C:\xampp\htdocs\resort-pos\backend)
│   ├── config/database.php
│   ├── controllers/
│   ├── middleware/AuthMiddleware.php
│   ├── services/AuditService.php
│   ├── utils/
│   ├── .htaccess
│   └── index.php
├── database/
│   ├── schema.sql                # InnoDB schema with FKs, DECIMAL(10,2), and date-range indexes
│   └── seed.sql                  # Default Admin/Cashier accounts, Entrance Fees, Cottages, Rooms
└── docs/
    ├── migration/as-is-analysis-and-mapping.md
    ├── database/schema-reference.md
    └── testing/test-results.md
```

## Quick Start (Windows + XAMPP)

1. **Start Apache & MySQL** in **XAMPP Control Panel**.
2. **Import Database** (already imported into local XAMPP `resort_pos`):
   ```powershell
   Get-Content database\schema.sql | C:\xampp\mysql\bin\mysql.exe -u root
   Get-Content database\seed.sql   | C:\xampp\mysql\bin\mysql.exe -u root
   ```
3. **Deploy Backend to XAMPP `htdocs`** (already copied to `C:\xampp\htdocs\resort-pos\backend`):
   ```powershell
   Copy-Item -Path backend -Destination C:\xampp\htdocs\resort-pos\backend -Recurse -Force
   ```
4. **Run the React Frontend**:
   ```powershell
   cd frontend
   npm install
   npm run dev
   ```
   Open `http://localhost:5173`. Vite proxies `/api/*` requests to `http://localhost/resort-pos/backend/index.php`.

## Default Accounts

| Role | Username | Password |
| :--- | :--- | :--- |
| **ADMIN** | `admin` | `admin123` |
| **CASHIER** | `cashier1` | `cashier123` |
