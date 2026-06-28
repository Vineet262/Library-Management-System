# 📚 Library Management System

A **console-based** Library Management System built with **Java 17+**, demonstrating
OOP principles, file I/O persistence, ArrayList collections, exception handling,
input validation, and a fully interactive menu UI.

---

## 🗂️ Project Structure

```
week3-library-system/
│── src/
│ ├── main/
│ │ ├── java/
│ │ │ ├── library/
│ │ │ │ ├── Main.java
│ │ │ │ ├── Book.java
│ │ │ │ ├── Member.java
│ │ │ │ ├── Library.java
│ │ │ │ └── FileHandler.java
│ ├── resources/
│── data/
│ ├── books.txt
│ └── members.txt
│── README.md
│── .gitignore
└── pom.xml
```

---

## ✨ Features

| Category          | Feature                                                      |
|-------------------|--------------------------------------------------------------|
| 📖 Books          | Add, remove, view all / available / borrowed (sorted A–Z)    |
| 👤 Members        | Register, remove, view details, borrow limit (5 books max)   |
| 🔄 Transactions   | Borrow with 2-week due date, return with overdue fine calc   |
| 🔍 Search         | By title, author, genre; by member name/email; ISBN/ID lookup |
| 🔖 Reservations   | Reserve a borrowed book; reservation enforced on borrow       |
| ⚠️ Overdue        | Overdue report sorted by most overdue first; fine = $0.50/day |
| 📊 Statistics     | Totals, available/borrowed, outstanding & collected fines     |
| 📤 CSV Export     | Export books & members to CSV files                           |
| 💾 Persistence    | Auto-save on every mutation; load on startup                  |
| ✅ Validation     | All inputs validated with re-prompt; duplicate ISBN/ID checks  |

---

## 🛠️ Prerequisites

| Requirement  | Version |
|--------------|---------|
| Java JDK     | 17+     |
| Apache Maven | 3.8+ *(optional)* |

---

## 🚀 Build & Run

### Option 1 — One-click Batch Script (Windows, no Maven needed)

```bat
run.bat
```

### Option 2 — Manual `javac` (any OS)

```bash
# Compile
javac -d out src/main/java/library/*.java

# Run
java -cp out library.Main
```

### Option 3 — Maven (fat JAR)

```bash
mvn clean package -q
java -jar target/library-system.jar
```

---

## 📋 Menu Overview

```
  +--------------------------------------------------+
  |                                                  |
  |       LIBRARY MANAGEMENT SYSTEM                  |
  |         Console Edition  v1.0                    |
  |                                                  |
  |   Manage books, members & borrowing operations   |
  |                                                  |
  +--------------------------------------------------+

  +------------------------------------------+
  |      LIBRARY MANAGEMENT SYSTEM           |
  +------------------------------------------+
  |  1.  Book Management                     |
  |  2.  Member Management                   |
  |  3.  Borrow / Return                     |
  |  4.  Search                              |
  |  5.  Library Statistics                  |
  |  6.  Export to CSV                       |
  |  7.  Reservations                        |
  |  8.  Overdue Report                      |
  |  9.  Exit                                |
  +------------------------------------------+
```

---

## 💾 Data File Format

### `data/books.txt` (pipe-delimited)
```
isbn|title|author|year|genre|available|borrowedBy|dueDate|reservedBy
9780134685991|Effective Java|Joshua Bloch|2018|Programming|true|NULL|NULL|NULL
9781617294945|Spring in Action|Craig Walls|2020|Frameworks|false|MEM001|2026-07-03|NULL
```

### `data/members.txt` (pipe-delimited)
```
id|name|email|phone|borrowedISBNs|reservedISBNs|totalFinesPaid
MEM001|Alice Smith|alice@example.com|555-1001|9781617294945|NONE|0.0
MEM002|Bob Johnson|bob@example.com|555-1002|NONE|NONE|2.50
```

---

## 📊 Sample Output

```
  ================================================================================
   ALL BOOKS (10 total)
  ================================================================================
    1. ISBN: 9780132350884   | Clean Code               | Robert C. Martin  | 2008 | [BORROWED] by: MEM002 | Due: 2026-06-25
    2. ISBN: 9780201633610   | Design Patterns          | Gang of Four      | 1994 | [AVAILABLE]
    3. ISBN: 9780134685991   | Effective Java           | Joshua Bloch      | 2018 | [AVAILABLE]
  ...

  ================================================================================
   LIBRARY STATISTICS
  ================================================================================
  Total Books:                   10
  Available Books:               8
  Borrowed Books:                2
  Overdue Books:                 0
  Outstanding Fines:             $0.00
  Registered Members:            5
  Total Fines Collected:         $7.50
```

---

## 💡 Design Decisions

- **No external libraries** — pure Java standard library only
- **OOP encapsulation** — all fields private, accessed via getters/setters
- **Streams API** — used for all filtering, searching, and sorting
- **Human-readable persistence** — pipe-delimited text (not binary) so data can be inspected/edited manually
- **Reservation system** — a reserved book can only be borrowed by the reserving member
- **Borrow limit** — 5 books per member (`Member.MAX_BORROW_LIMIT`)
- **Loan period** — 2 weeks (`Library.LOAN_PERIOD_WEEKS`)
- **Fine rate** — $0.50 per day overdue (`Book.FINE_PER_DAY`)
- **Duplicate protection** — ISBN and Member ID uniqueness enforced on add/register
- **Safe removal** — books cannot be removed while borrowed; members cannot be removed while holding books

---

## 📌 Author
Week 3 Java OOP Project — Library Management System
