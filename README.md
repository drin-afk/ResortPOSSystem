# Promise Resort POS & Management System (Web Migration)

Migrated from **Java NetBeans (Java Swing + JDBC)** to **React.js + PHP 8 REST API (PDO) + MySQL (XAMPP)**.

---

## Step-by-Step Setup Guide for Groupmates (VS Code)

### 1. Prerequisites (Install Once)
Make sure your laptop has the following installed:
1. **[XAMPP](https://www.apachefriends.org/download.html)** (for Apache + PHP 8 + MySQL) — installed at `C:\xampp`
2. **[Node.js (LTS)](https://nodejs.org/)** (includes `npm` to run the React frontend)
3. **[Git](https://git-scm.com/downloads)** and **[Visual Studio Code](https://code.visualstudio.com/)**

---

### 2. Clone the Project in VS Code
1. Open **VS Code**.
2. Open the integrated terminal (**`Ctrl + ~`** or **Terminal -> New Terminal**).
3. Run the following commands to clone the project and open it:
   ```powershell
   git clone https://github.com/drin-afk/ResortPOSSystem.git
   cd ResortPOSSystem
   code .
   ```

---

### 3. Start XAMPP (Apache & MySQL)
1. Open **XAMPP Control Panel**.
2. Click **Start** next to **Apache**.
3. Click **Start** next to **MySQL**.
   *(Both module names should turn green).*

---

### 4. Import the MySQL Database (`resort_pos`)
You can import the database using **Option A (VS Code Terminal)** or **Option B (phpMyAdmin)**.

#### Option A — Via VS Code Terminal (Fastest)
In the VS Code terminal (inside the `ResortPOSSystem` root folder), run:
```powershell
Get-Content database\schema.sql | C:\xampp\mysql\bin\mysql.exe -u root
Get-Content database\seed.sql   | C:\xampp\mysql\bin\mysql.exe -u root
```

#### Option B — Via phpMyAdmin (Browser)
1. Open `http://localhost/phpmyadmin` in your browser.
2. Click the **Import** tab at the top.
3. Choose [`database/schema.sql`](database/schema.sql) and click **Import** at the bottom.
4. Click **Import** again, choose [`database/seed.sql`](database/seed.sql), and click **Import**.

---

### 5. Copy the PHP Backend to XAMPP `htdocs`
In your VS Code terminal (inside the `ResortPOSSystem` root folder), run:
```powershell
New-Item -ItemType Directory -Force -Path "C:\xampp\htdocs\resort-pos"
Copy-Item -Path "backend" -Destination "C:\xampp\htdocs\resort-pos\backend" -Recurse -Force
```
> **Note:** Whenever you pull or edit files inside `backend/`, re-run the `Copy-Item` command above so XAMPP Apache serves the latest PHP files.

---

### 6. Install & Run the React Frontend
In your VS Code terminal, run:
```powershell
cd frontend
npm install
npm run dev
```
Then open the URL shown in the terminal in your browser:
- **Website URL:** `http://localhost:5173/`

---

## Default Login Accounts

| Role | Username | Password |
| :--- | :--- | :--- |
| **ADMIN** | `admin` | `admin123` |
| **CASHIER** | `cashier1` | `cashier123` |

---

## Troubleshooting (Common Laptop Issues)

- **PowerShell says `npm : File ...\npm.ps1 cannot be loaded because running scripts is disabled`:**
  Run this command once in your VS Code PowerShell terminal:
  ```powershell
  Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned
  ```
- **Port `5173` or `esbuild` error on `npm run dev`:**
  If `esbuild` binary was skipped during install, run:
  ```powershell
  node node_modules\esbuild\install.js
  npm run dev
  ```
- **`Failed to connect to database` or `500` error on Login:**
  1. Check that **Apache** and **MySQL** are both running in XAMPP Control Panel.
  2. Verify `http://localhost/resort-pos/backend/index.php?route=auth/me` opens in your browser and returns JSON.
  3. If your MySQL `root` user has a password or uses port `3307`, update `DB_PORT` / `DB_PASS` in `C:\xampp\htdocs\resort-pos\backend\config\database.php`.

---

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
│   ├── .env.example
│   ├── .htaccess
│   └── index.php
├── database/
│   ├── schema.sql                # InnoDB schema with FKs, DECIMAL(10,2), and date-range indexes
│   └── seed.sql                  # Default Admin/Cashier accounts, Cottages, Rooms
└── docs/
    ├── migration/as-is-analysis-and-mapping.md
    ├── database/schema-reference.md
    └── testing/test-results.md
```
