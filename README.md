# Roadwise — Traffic Monitoring and Violation Management System
*A Java Desktop Application for Traffic Monitoring, Violation Management, and Signal Optimization*  
*Technology Stack: Java JDK 25 · Java Swing & AWT · JDBC · MySQL Server 8.4/8.0 LTS*

---

## 1. Project Overview

**Roadwise** (**Smart Traffic Monitoring and Violation Management System**) is a professional desktop operations console designed for city traffic departments, transport authorities, and violation management personnel. It provides real-time monitoring of registered vehicles, automated violation tracking, traffic signal timing optimization, accident incident records, and driver risk scoring.

The application connects directly to a local or remote **MySQL Server** using JDBC and PreparedStatements to ensure data persistence, strict data integrity, foreign-key relationships (with cascade operations), and SQL-injection defense.

### Key Capabilities
- **Genuine Database Persistence:** Every metric, dashboard statistic, table row, and calculation is queried live from MySQL. No fake or mock data is injected.
- **Clean Empty States:** When the database starts empty, statistic cards show clean zero totals and helpful guidance on registering records.
- **Adaptive Traffic Signal Calculation:** Proportional allocation algorithm calculating fair green-light durations within a 120-second cycle (minimum 10s, maximum 60s per approach) with deterministic rebalancing.
- **Explainable Driver Risk Scoring:** Bounded 0–100 demerit points system evaluating driver safety ratings (`LOW`, `MEDIUM`, `HIGH`) based on recorded offenses and unpaid fine surcharges.
- **Refined Dark Operations Console:** Premium forest-green operations theme (`#202520` background, `#191E1A` sidebar, `#A2BA91` sage accent, `#F2F3EB` light text) built entirely with standard Java Swing and AWT components—no external UI libraries required.
- **Robust Exception Handling:** Thorough application of Core Java OOP exception handling (`try`, `catch`, `finally`, `throw`, `throws`, `IllegalArgumentException`, `SQLException`, `IOException`) across models, database operations, file I/O, and user input validation.

---

## 2. Visual Theme & Color Palette

The interface adheres to a modern, calm, and distraction-free dark operations console theme:

| Component Role | Hex Code | RGB | Description |
|---|---|---|---|
| **Main Background** | `#202520` | `rgb(32, 37, 32)` | Deep charcoal canvas |
| **Sidebar Background** | `#191E1A` | `rgb(25, 30, 26)` | Compact slate navigation rail |
| **Card Background** | `#2B332C` | `rgb(43, 51, 44)` | Dark olive container cards |
| **Elevated Panels** | `#323B33` | `rgb(50, 59, 51)` | Section headers and dialogs |
| **Primary Accent** | `#A2BA91` | `rgb(162, 186, 145)` | Sage green active tabs and primary buttons |
| **Main Typography** | `#F2F3EB` | `rgb(242, 243, 235)` | Warm ivory readable text |
| **Secondary Typography** | `#AAB5A8` | `rgb(170, 181, 168)` | Muted grey-green field labels & hints |
| **Subtle Borders** | `#414B40` | `rgb(65, 75, 64)` | Restrained dividers and outlines |
| **Warning / Pending** | `#D8AC60` | `rgb(216, 172, 96)` | Amber status badge |
| **Error / High-Risk** | `#D7736C` | `rgb(215, 115, 108)` | Red status badge and error indicators |
| **Input Background** | `#252C26` | `rgb(37, 44, 38)` | Recessed dark form inputs |

---

## 3. Application Layout & Navigation

### Left Navigation Rail (6 Modules)
The left sidebar contains 6 essential operational modules with a slim sage-green active indicator:
1. **Dashboard:** 4 compact statistic cards (Registered Vehicles, Recorded Violations, Recorded Accidents, Unpaid Fines) + balanced two-column view with latest signal timing and recent offenses table.
2. **Vehicles & Drivers:** Unified vehicle registration and driver licensing with live multi-column search, edit, and cascade deletion.
3. **Violations & Fines:** Violation recording with standard fines, filtering by category/payment status, and instant paid/unpaid toggling.
4. **Signal Calculator:** Proportional 4-way intersection green-time allocator with bounds enforcement, live visual distribution bars, step-by-step mathematical explanation, and history logging.
5. **Accident Records:** Incident logging with location, timestamp, collision notes, and color-coded severity badges (`Minor`, `Moderate`, `Severe`, `Fatal`).
6. **Driver Risk:** Demerit points assessment (0–100) with risk tier badges (`LOW`, `MEDIUM`, `HIGH`) and itemized breakdown of contributing offenses.

### Global Header Bar & Database Configuration
The top bar features:
- Current active page title and descriptive subtitle.
- **Live Connection Status Pill:** Dynamic dot indicator reflecting actual MySQL connection state:
  - 🟢 **Connected:** Database is online and verified.
  - 🟡 **Configuration Required:** Password or credentials missing.
  - 🔴 **Disconnected:** Service unreachable or network fault.
- **Configure Database Button:** Opens a modal dialog allowing credentials to be updated, connection to be tested, and database tables to be initialized without restarting the application.
- **Refresh Button:** Instantly refreshes all metrics, tables, and caches from MySQL.

---

## 4. Project Structure

```
SmartTrafficManagement/
├── src/
│   ├── Main.java                   ← Application bootstrap and Swing EDT launcher
│   ├── TrafficManagementFrame.java ← Main window, dark theme layout, 6 modules, forms & tables
│   ├── Vehicle.java                ← Entity model: vehicle registration & driver details
│   ├── Violation.java              ← Entity model: offenses, fine rates, & payment status
│   ├── Accident.java               ← Entity model: accident records & severity ratings
│   ├── TrafficObservation.java     ← Entity model: saved traffic signal timing record
│   ├── TrafficAlgorithm.java       ← Proportional allocation calculation engine
│   └── DatabaseManager.java        ← Central JDBC service: connection pool, CRUD, & security
│
├── database/
│   └── schema.sql                  ← Relational MySQL database schema (tables, foreign keys, indexes)
│
├── lib/
│   └── mysql-connector-j-8.4.0.jar ← Official MySQL JDBC Connector driver
│
├── test/
│   ├── SelfCheck.java              ← 59-point automated verification test suite
│   ├── GuiHeadlessCheck.java       ← Swing GUI lifecycle & EDT verification
│   └── DatabaseOperationsTest.java ← MySQL connection diagnostic and credential test
│
├── db.properties.example           ← Safe configuration template
├── db.properties                   ← Local credentials file (excluded from Git)
├── .gitignore                      ← Excludes compiled binaries and credentials
└── README.md                       ← Technical documentation
```

---

## 5. MySQL Database & Credential Configuration

### Authentication Diagnostic
If the application reports:
```
java.sql.SQLException: Access denied for user 'root'@'localhost' (using password: NO)
```
This indicates MySQL Server is running and enforcing password authentication, but no password was provided.

### Configuration Methods

#### Method 1: Environment Variables (Recommended)
Set the environment variable in your terminal before launching:

**PowerShell (Windows):**
```powershell
$env:TRAFFIC_DB_URL="jdbc:mysql://localhost:3306/traffic_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:TRAFFIC_DB_USER="root"
$env:TRAFFIC_DB_PASSWORD="your_actual_mysql_password"
```

**Command Prompt (cmd):**
```cmd
set TRAFFIC_DB_URL=jdbc:mysql://localhost:3306/traffic_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
set TRAFFIC_DB_USER=root
set TRAFFIC_DB_PASSWORD=your_actual_mysql_password
```

#### Method 2: Local `db.properties` File
Save your credentials in `db.properties` in the project root (ignored by `.gitignore`):

```ini
TRAFFIC_DB_URL=jdbc:mysql://localhost:3306/traffic_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
TRAFFIC_DB_USER=root
TRAFFIC_DB_PASSWORD=your_actual_mysql_password
```

#### Method 3: In-App "Configure Database" Dialog
1. Launch the application.
2. Click the **"Configure Database"** button in the top right header.
3. Enter your MySQL password and click **"Test Connection"**.
4. Click **"Save & Apply"** to persist credentials to `db.properties` and connect immediately.
5. If tables are not yet created, click **"Initialize Database Tables"**.

### Initializing the Database via SQL CLI
Alternatively, run the SQL schema script directly from MySQL CLI:
```powershell
mysql -u root -p traffic_db < database/schema.sql
```

---

## 6. Object-Oriented Exception Handling Architecture

The codebase rigorously demonstrates Core Java OOP exception handling across all functional tiers:

### 1. Constructs Employed
- **`try-catch` blocks:** Encapsulate operations prone to runtime or checked failures (JDBC execution, number parsing, dialog displays).
- **`finally` / `try-with-resources`:** Guarantee prompt closing of database connections, statements, and streams even during abrupt exceptions.
- **`throw`:** Explicitly raises standard Java exceptions when domain invariants are violated.
- **`throws`:** Declares recoverable or structural checked exceptions in method signatures (`throws SQLException`, `throws IOException`).

### 2. Specific Exception Types Handled
- **`IllegalArgumentException` (Unchecked Domain Validation):**
  - Raised by `Vehicle.validate(reg, driver, lic)` when registration or driver details are null, empty, or improperly formatted.
  - Raised by `Violation.validate(vehicleId, type, dateTime, fine)` if fine amounts are negative or vehicle foreign keys are missing.
  - Raised by `Accident.validate(location, dateTime)` if incident locations or timestamps are missing.
  - Raised by `TrafficAlgorithm.calculate(n, s, e, w)` if any directional vehicle count is negative.
  - Raised by `DatabaseManager.addVehicle(null)` or `deleteVehicle(-1)` when invalid parameters are supplied.
- **`NumberFormatException` (Unchecked Input Parsing):**
  - Handled in Swing input fields (directional vehicle counts, manual fine adjustments) with informative user warning messages rather than silent failures.
- **`SQLException` (Checked Persistence Errors):**
  - Handled across all CRUD methods in `DatabaseManager`.
  - Transaction rollbacks executed on multi-step operations (such as cascading vehicle deletion).
  - SQL duplicate key violations (e.g. unique vehicle registration) caught and translated into user-friendly notices.
- **`IOException` (Checked Configuration & File I/O):**
  - Declared and handled during `db.properties` loading and credential serialization.

### 3. Graceful UI Recovery
All user actions in `TrafficManagementFrame` catch exceptions and present structured, non-technical feedback to the operator via styled `JOptionPane.showMessageDialog` alerts:
- Domain validation failures display an **"Input Validation Error"** alert.
- Database access issues display a **"Database Error"** alert with actionable troubleshooting tips.

---

## 7. Compilation and Execution

Navigate to the project directory:
```powershell
cd c:\Users\user\Documents\traffic\SmartTrafficManagement
```

### 1. Compile All Classes
```powershell
javac -encoding UTF-8 -cp "lib/mysql-connector-j-8.4.0.jar;src" -d out src/*.java test/*.java
```

*(If using JDK 25 with explicit binary path):*
```powershell
& "C:\Program Files\Java\jdk-25.0.5\bin\javac.exe" -encoding UTF-8 -cp "lib/mysql-connector-j-8.4.0.jar;src" -d out src/*.java test/*.java
```

### 2. Run Comprehensive Automated Verification Tests (59 Checks)
```powershell
java -cp "out;lib/mysql-connector-j-8.4.0.jar" SelfCheck
```

*(If using JDK 25 with explicit binary path):*
```powershell
& "C:\Program Files\Java\jdk-25.0.5\bin\java.exe" -cp "out;lib/mysql-connector-j-8.4.0.jar" SelfCheck
```

### 3. Run Swing GUI Lifecycle Verification
```powershell
java -Djava.awt.headless=false -cp "out;lib/mysql-connector-j-8.4.0.jar" GuiHeadlessCheck
```

### 4. Run Multi-Resolution GUI Layout & Component Inspection
```powershell
java -Djava.awt.headless=false -cp "out;lib/mysql-connector-j-8.4.0.jar" GuiViewInspectionTest
```

### 5. Run MySQL Connection & Credential Diagnostics
```powershell
java -cp "out;lib/mysql-connector-j-8.4.0.jar" DatabaseOperationsTest
```

### 6. Launch the Application
```powershell
java -cp "out;lib/mysql-connector-j-8.4.0.jar" Main
```

*(Or passing password inline on PowerShell):*
```powershell
$env:TRAFFIC_DB_PASSWORD="your_password"; java -cp "out;lib/mysql-connector-j-8.4.0.jar" Main
```

---

## 8. Verification Suite Summary

| Test File | Verification Scope | Status |
|---|---|---|
| **`SelfCheck.java`** | 59 automated checks covering: Vehicle, Violation, Accident, and TrafficObservation models; deterministic 120s traffic allocation invariant; safety bounds clamping `[10s, 60s]`; zero & equal traffic distributions; driver risk scoring & demerit points; credential resolution hierarchy; and structured exception validation (`IllegalArgumentException`, `SQLException`, `IOException`). | **PASSED (59/59)** |
| **`GuiHeadlessCheck.java`** | Swing Event Dispatch Thread (EDT) execution, Look and Feel initialization, frame creation, theme color consistency, and clean window disposal. | **PASSED** |
| **`GuiViewInspectionTest.java`** | Multi-resolution layout verification (1024x680, 1180x720, 1366x768) across all 6 views verifying that all 20+ buttons, inputs, dropdowns, and tables maintain valid bounds and visibility without label truncation or clipping. | **PASSED** |
| **`DatabaseOperationsTest.java`** | Missing password detection, incorrect credential diagnostic reporting, and live MySQL communication checks. | **PASSED** |

---

## 9. Troubleshooting Guide

| Issue | Cause | Solution |
|---|---|---|
| `Access denied for user 'root'@'localhost'` | MySQL root password missing or incorrect. | Supply the correct password via `TRAFFIC_DB_PASSWORD`, `db.properties`, or the in-app **Configure Database** dialog. |
| `Communications link failure` | MySQL Server service is stopped or unreachable on port 3306. | Start MySQL in Windows Services (`services.msc`) or run `net start MySQL80` / `net start MySQL84`. |
| `Table 'traffic_db.vehicles' doesn't exist` | Database exists but tables have not been created. | Click **"Initialize Database Tables"** inside the in-app **Configure Database** dialog or run `database/schema.sql`. |
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | MySQL Connector/J driver missing from classpath. | Ensure `-cp "out;lib/mysql-connector-j-8.4.0.jar"` is included in the execution command. |
| Table headers appear white / mismatched | Swing Look and Feel defaulting to standard Synth. | Application sets cross-platform Look and Feel with `DarkTableCellRenderer` on all column data types and scroll pane viewports. |
