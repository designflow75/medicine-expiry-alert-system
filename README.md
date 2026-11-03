# 💊 Medicine Expiry Alert System
> **Java Swing + MySQL (JDBC) | College OOP Project**  
> *Suitable for KTU S3 B.Tech AI/ML Object-Oriented Programming (OOP) Practical Exam & Viva Demonstration.*

---

## 📋 1. Project Overview

The **Medicine Expiry Alert System** is a standalone desktop application built entirely with **Core Java, Java Swing (GUI), and MySQL Database Connectivity (JDBC)**. Designed specifically for households and healthcare clinics, it monitors medicine cabinets, calculates dynamic expiry countdowns using the Java Date-Time API, and actively warns against consuming degraded or expired medications.

The project strictly follows the **Data Access Object (DAO) pattern** and cleanly demonstrates fundamental **Object-Oriented Programming (OOP)** concepts such as **Encapsulation, Inheritance, Abstraction, Polymorphism, and Exception Handling**.

---

## 🛠️ 2. Technologies Used

* **Language**: Java 17+ / Java 21 LTS
* **User Interface**: Java Swing (pure desktop GUI, zero web frameworks)
* **Database**: MySQL 8.0+ / 26.10 (`medicine_expiry_db`)
* **Connectivity**: JDBC (`com.mysql.cj.jdbc.Driver`) via `PreparedStatement`
* **Security**: SHA-256 cryptographic password hashing (`java.security.MessageDigest`)
* **Build / Execution**: Batch scripts (`.bat`) & Command Prompt (`cmd`)

---

## 🏛️ 3. OOP Fundamentals Demonstrated

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Presentation (Java Swing)                       │
│  BaseFrame (Abstract) ──► LoginFrame, RegisterFrame, DashboardFrame    │
│  Custom Renderers     ──► StatusBadgeRenderer                          │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Calls
┌───────────────────────────────────▼────────────────────────────────────┐
│                       Service & Utilities Layer                        │
│  ExpiryCalculator (LocalDate math), PasswordUtil (SHA-256 Hashing)     │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Uses DAO Interfaces
┌───────────────────────────────────▼────────────────────────────────────┐
│                    Data Access Object (DAO) Pattern                    │
│  UserDAO (Interface)       ──► UserDAOImpl (JDBC PreparedStatement)    │
│  MedicineDAO (Interface)   ──► MedicineDAOImpl (JDBC PreparedStatement)│
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ JDBC Connection
┌───────────────────────────────────▼────────────────────────────────────┐
│                         MySQL Database Server                          │
│                     Database: medicine_expiry_db                       │
│                     Tables: users, medicines                           │
└────────────────────────────────────────────────────────────────────────┘
```

1. **Classes and Objects**: Clean domain models (`User`, `Medicine`, `ExpiryStatus`) representing business entities.
2. **Encapsulation**: Private fields across all classes, accessed via public getters/setters, paired with internal domain calculation methods (`getDaysLeft()`, `getStatus()`, `isExpired()`).
3. **Inheritance**: `BaseFrame` (abstract class) centralizes window styling, buttons, colors, borders, and dialogs. Extended by `LoginFrame`, `RegisterFrame`, and `DashboardFrame`.
4. **Abstraction**: Clean interfaces (`UserDAO`, `MedicineDAO`) decouple the business and UI layers from the underlying database SQL execution.
5. **Polymorphism & Method Overriding**: Concrete implementations (`UserDAOImpl`, `MedicineDAOImpl`) override interface contracts (`@Override`). Custom `StatusBadgeRenderer` overrides `DefaultTableCellRenderer` to paint rounded colored status pills.
6. **Exception Handling**: Safe database transactions using `try-with-resources`, ensuring connections, statements, and result sets are automatically closed.

---

## 🚀 4. How to Compile and Run in CMD / PowerShell

### Prerequisites:
1. **Java Development Kit (JDK 17+)** installed. Verify by running:
   ```cmd
   javac -version
   java -version
   ```
2. **MySQL Server** installed and running on port `3306`.

---

### Option A: Using One-Click Batch Scripts (Easiest)

Open Command Prompt (`cmd.exe`) in this folder (`c:\Users\user\Desktop\medical expiry`):

1. **To Compile:**
   ```cmd
   compile.bat
   ```
2. **To Run:**
   ```cmd
   run.bat
   ```
3. **To Compile & Run in One Command:**
   ```cmd
   build_and_run.bat
   ```

---

### Option B: Manual Command-Line Compilation & Execution

```cmd
:: 1. Compile all Java sources
javac -encoding UTF-8 -cp "lib/*" -d "bin" src\com\mediguard\*.java src\com\mediguard\model\*.java src\com\mediguard\database\*.java src\com\mediguard\dao\*.java src\com\mediguard\util\*.java src\com\mediguard\ui\*.java

:: 2. Launch Desktop GUI Application
java -cp "bin;lib/*;config" com.mediguard.Main
```

---

## 🗄️ 5. Database Configuration (Supabase & MySQL)

The application supports both **Supabase (Cloud PostgreSQL)** and **Local MySQL** with automatic schema initialization and demo data seeding.

Configuration is managed in [`config/db.properties`](config/db.properties):

### Option 1: Supabase (Cloud PostgreSQL - Recommended)
```properties
db.type=supabase
db.host=aws-0-ap-southeast-1.pooler.supabase.com
db.port=5432
db.name=postgres
db.user=postgres.ahfygicztfdszpxtohsg
db.password=YOUR_SUPABASE_PASSWORD
db.ssl=true
```

> **How to get your credentials in Supabase:**
> 1. Open your Supabase Project dashboard.
> 2. Go to **Project Settings** -> **Database**.
> 3. Under **Connection Pooling**, copy the Host, Port (5432), and User (`postgres.<project_ref>`).
> 4. Fill in your project database password in `db.password`.

---

### Option 2: Local MySQL (Alternative / Offline)
To use local MySQL instead, set `db.type=mysql`:
```properties
db.type=mysql
db.mysql.host=localhost
db.mysql.port=3306
db.mysql.name=medicine_expiry_db
db.mysql.user=root
db.mysql.password=CampusOne@2026!
```

### Automatic Schema Creation:
Whether connecting to Supabase or MySQL, `DatabaseConnection.java` will **automatically** create the `users` and `medicines` tables, foreign keys, and indexes if they do not exist. It also automatically seeds the demo account with sample medicines.

---

## 🔑 6. Login & Demo Account Credentials

A pre-configured demo account is included out of the box:

* **Email:** `demo@medicine.com`
* **Password:** `Demo@123`

### Features:
1. **Demo Credentials Displayed in Text**: The demo credentials (`demo@medicine.com` / `Demo@123`) are clearly displayed in a dedicated card on the login screen. Clicking the card autofills the fields directly into the login form.
2. **Pre-Seeded Medicines**: The demo account includes 10 pre-loaded household medicines spanning all 5 expiry tiers (Safe, Expiring Soon, Critical, Due Today, and Expired) for immediate viva presentation.
3. **Student Registration**: Click **"Create New Account"** to register a new student with Full Name, Student ID, College Name, Email, and Password. All information is genuinely saved to MySQL with SHA-256 password hashing.
4. **Data Privacy**: Each user can only view, edit, and delete their own private medicines (`WHERE user_id = ?`).

---

## 📊 7. Key Features & Screens

### 1. Main Dashboard
* Displays personalized greeting with student name and college.
* 4 Real-time KPI Cards using `GridBagLayout`:
  * **Total Medicines**
  * **Safe Medicines** (> 30 days left)
  * **Expiring Soon** (8–30 days left)
  * **Expired Medicines** (Do not consume!)
* Dynamic **Safety Warning Banner** that highlights expired or critical medications.

### 2. Add Medicine
* Complete medicine registration form:
  * Medicine Name, Category, Batch Number, Quantity.
  * Purchase Date and Expiry Date (with quick-select buttons: `+1 Mo`, `+3 Mo`, `+6 Mo`, `+1 Yr`).
  * Storage Location and Usage Notes.
* Validation: Expiry date cannot be earlier than purchase date; quantity must be >= 1.

### 3. My Medicines (Inventory)
* `JTable` displaying: `ID | Medicine Name | Category | Batch No | Quantity | Purchase Date | Expiry Date | Expiry Status | Storage Location`.
* Colored rounded pill badges rendered for each status tier.
* Action buttons:
  * ➕ **Add Medicine**
  * ✏️ **Edit Medicine** (opens edit dialog, updates MySQL)
  * 🗑️ **Delete Medicine** (confirms with `JOptionPane`, deletes from MySQL)
  * 👁️ **View Details** (inspect full card including notes)
  * 🔄 **Refresh**

### 4. Dedicated Expiry Alerts
* Categorized tabs:
  1. 🔴 **Already Expired** (past expiry date)
  2. ⚠️ **Expires Today** (due today)
  3. 🟠 **Critical** (1–7 days remaining)
  4. 🟡 **Expiring Soon** (8–30 days remaining)
* Clear warnings discouraging consumption of expired drugs.

### 5. Medicine Search & Filtering
* Real-time search across Medicine Name, Category, and Batch Number.
* Category filter dropdown.
* Status filter dropdown (All, Safe, Expiring Soon, Critical, Expires Today, Expired).
* Queries MySQL directly with date arithmetic.

### 6. Reports & Statistics
* Summary breakdown cards for all 5 tiers.
* Filter report by expiry status.
* All metrics calculated using genuine MySQL queries.

---

## 📁 8. Project Directory Structure

```text
medical expiry/
├── bin/                              # Compiled .class bytecode
├── config/
│   └── db.properties                 # MySQL configuration & credentials
├── lib/
│   └── mysql-connector-j-8.3.0.jar   # MySQL JDBC Driver
├── src/com/mediguard/
│   ├── Main.java                     # Application entry point
│   ├── model/                        # OOP Domain Entities (Encapsulation)
│   │   ├── User.java                 # Student / User account model
│   │   ├── Medicine.java             # Medicine inventory model
│   │   └── ExpiryStatus.java         # 5-Tier status enum & color definitions
│   ├── database/                     # JDBC Database Connection
│   │   └── DatabaseConnection.java   # Singleton connection manager & auto-initializer
│   ├── dao/                          # Data Access Object Pattern (Abstraction)
│   │   ├── UserDAO.java              # User DAO interface
│   │   ├── UserDAOImpl.java          # MySQL JDBC user implementation
│   │   ├── MedicineDAO.java          # Medicine DAO interface
│   │   └── MedicineDAOImpl.java      # MySQL JDBC medicine implementation
│   ├── util/                         # Business Calculation & Security
│   │   ├── ExpiryCalculator.java     # LocalDate expiry calculation logic
│   │   └── PasswordUtil.java         # SHA-256 password hashing
│   └── ui/                           # Java Swing Desktop Presentation Layer
│       ├── BaseFrame.java            # Abstract base frame (Inheritance)
│       ├── LoginFrame.java           # Login screen with Demo sign-in
│       ├── RegisterFrame.java        # Student account registration screen
│       ├── DashboardFrame.java       # Main dashboard with sidebar & CardLayout
│       ├── EditMedicineDialog.java   # Edit medicine modal dialog
│       ├── ViewMedicineDialog.java   # View medicine details dialog
│       └── StatusBadgeRenderer.java  # Custom JTable pill badge renderer
├── schema-mysql.sql                  # MySQL database schema & seed SQL script
├── compile.bat                       # Command Prompt script to compile
├── run.bat                           # Command Prompt script to run GUI
├── build_and_run.bat                 # Command Prompt script to compile & run
└── README.md                         # Project documentation
```
