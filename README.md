# Retail Discount Rule Engine

<img width="1440" height="2136" alt="image" src="https://github.com/user-attachments/assets/4a7792af-528b-495d-bddb-4c88930d5d1f" />

---
## About Project

A high-performance, functional rule-based engine built in Scala that evaluates retail transactions against business discount logic, computes final prices, and persists results to an Oracle Database. The engine is designed for high-throughput processing using parallel execution that detects available CPU cores and chunked streaming based on data size, enabling efficient handling of thousands to millions of records.

---

## Table of Contents

  * Overview
  * Discount Rules
  * Project Structure
  * Tech Stack
  * Getting Started
  * How It Works
  * Functional Programming Principles
  * Pipeline Test Execution

-----

## Overview

The **Retail Discount Rule Engine** automates the pricing lifecycle for retail transactions. It reads raw CSV data, transforms it into structured models, applies a series of competitive discount rules, and saves the final results.

### Key Features

  * **Parallel Execution:** Uses `ForkJoinPool` and Scala Parallel Collections to maximize CPU utilization.
  * **Fault Tolerance:** Employs `Try` and `Either` to ensure a single malformed row doesn't crash the entire pipeline.
  * **Memory Efficiency:** Processes data in dynamic chunks, keeping RAM usage stable even with 10M+ rows.
  * **Top-2 Average Logic:** If an order qualifies for multiple discounts, the engine automatically averages the **top two** highest values.
  * **Scalability** If you want to add rules any time just put your new functions' of rules in the list, and run your pipeline.

-----

## Discount Rules

The engine evaluates each order against these specific business qualifiers:

| \# | Rule Name | Qualifying Condition | Calculation |
| :--- | :--- | :--- | :--- |
| 1 | **Expiration** | Product expires in \< 30 days | `(30 - daysRemaining)%` |
| 2 | **Special Date** | Transaction on March 23rd | `50%` flat |
| 3 | **Product Category** | Cheese or Wine products | Cheese: `10%`, Wine: `5%` |
| 4 | **Bulk Quantity** | Quantity \> 5 units | 6-9: `5%`, 10-14: `7%`, 15+: `10%` |
| 5 | **Payment Method** | Paid via Visa | `5%` flat |
| 6 | **App Adoption** | Purchased via Mobile App | `ceil(qty / 5) * 5%` |

-----

## Project Structure

```text
Functional-Programming-With-Scala/
├── src/main/scala/
│   ├── Main.scala                  # Entry point & Parallel Pipeline logic
│   ├── MainRules.scala             # Rule definitions & Parsing (ParsedRow)
│   ├── DB_Connection.scala         # JDBC Oracle integration & Batch Inserts
│   ├── HelperFunctions.scala       # File I/O and String manipulation
│   ├── Log.scala                   # Custom File-based Logger
│   └── Config.scala                # Configuration constants (DB URL, Paths, and Columns' Number)
├── src/resources/
│   ├── rule_engine.log             # Application runtime logs
│   ├── application.config          # Application Hidden configuration like (DB : username , password)
└── build.sbt                       # SBT dependencies
```

-----

## Tech Stack

  * **Language:** Scala 2.13.x
  * **Runtime:** JVM (Java 8+)
  * **Database:** Oracle Database (OJDBC8)
  * **Concurrency:** ForkJoinPool & Scala Parallel Collections
  * **IO:** Scala `Source` & `java.io`

-----

## Getting Started

### 1\. Prerequisites

  * JDK 8 or 11
  * SBT (Scala Build Tool)
  * Oracle Database instance

### 2\. Database Setup

Create the target table in your Oracle schema:

```sql
CREATE TABLE transactions_order (
    product_name VARCHAR2(255),
    discount NUMBER(5,2),
    final_price NUMBER(10,2)
);
```

### 3\. Configuration

Update `Config.scala` & `application.config` with your environment details:

```scala
val dbUrl = "jdbc:oracle:thin:@localhost:1521:xe"
val dbUser = "SYSTEM"
val dbPassword = "your_password"
val dataPath = "path/to/your/input.csv"
```

### 4\. Running the Engine

```bash
sbt run
```

-----

## How It Works

### The Pipeline Flow

1.  **Lazy Loading:** `readFile` opens the CSV via `Using` to ensure the file handle is closed safely.
2.  **Chunking:** The data is split into chunks based on available CPU cores.
3.  **Parallel Mapping:**  `safeProcess` converts rows to `ParsedRow`.
      * Rules are applied to generate a `List[Double]` of discounts.
      * The `calculateDiscount` function filters and averages the top 2.
4.  **Error Collection:** A `foldLeft` pass separates successful results from error strings.
5.  **Batch Insertion:** Validated results are grouped (e.g., 10,000 or 200,000) and pushed to Oracle using `addBatch()` for maximum performance.

-----

## Functional Programming Principles

This project emphasizes clean, maintainable Scala code:

  * **Immutability:** No `var` keywords are used; all data structures are immutable `Vectors` and `Lists`.
  * **Pure Logic:** The `MainRules` object contains pure functions that are easy to unit test.
  * **Declarative Patterns:** Replaced loops with high-order functions like `map`, `flatMap`, and `foldLeft`.
  * **Type Safety:** Used `Either[String, T]` to handle domain errors and `Try[T]` for infrastructure side effects.

-----

## Pipeline Test Execution

### Log schema
- In Successful case , and Handling File, and Database Error
<img width="1258" height="360" alt="image" src="https://github.com/user-attachments/assets/dc3d8e5a-835e-4371-85e9-f08ce1c46c98" />

- Processing & Insertion Time For 1000 rows
  <img width="1017" height="169" alt="image" src="https://github.com/user-attachments/assets/8a7d7e66-50c6-45cd-b29d-22a07bcfe726" />

- Processing & Insertion Time For 10M rows
<img width="801" height="197" alt="image" src="https://github.com/user-attachments/assets/3bcb11d5-083c-4e87-a67b-41cf2b2f3714" />




