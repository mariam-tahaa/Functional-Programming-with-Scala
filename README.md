# Retail Discount Rule Engine

A high-performance, functional rule-based engine built in **Scala** that evaluates retail transactions against business discount logic, computes final prices, and persists results to an **Oracle Database**. The engine is designed for high-throughput processing using parallel execution and chunked streaming to handle millions of records efficiently.

## Table of Contents

  * Overview
  * Discount Rules]
  * Project Structure
  * Tech Stack
  * Getting Started
  * How It Works
  * Functional Programming Principles

-----

## Overview

The **Retail Discount Rule Engine** automates the pricing lifecycle for retail transactions. It reads raw CSV data, transforms it into structured models, applies a series of competitive discount rules, and saves the final results.

### Key Features

  * **Parallel Execution:** Uses `ForkJoinPool` and Scala Parallel Collections to maximize CPU utilization.
  * **Fault Tolerance:** Employs `Try` and `Either` to ensure a single malformed row doesn't crash the entire pipeline.
  * **Memory Efficiency:** Processes data in dynamic chunks, keeping RAM usage stable even with 10M+ rows.
  * **Top-2 Average Logic:** If an order qualifies for multiple discounts, the engine automatically averages the **top two** highest values.

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
│   ├── Main.scala              # Entry point & Parallel Pipeline logic
│   ├── MainRules.scala         # Rule definitions & Parsing (ParsedRow)
│   ├── DB_Connection.scala     # JDBC Oracle integration & Batch Inserts
│   ├── HelperFunctions.scala   # File I/O and String manipulation
│   ├── Log.scala               # Custom File-based Logger
│   └── Config.scala            # Configuration constants (DB URL, Paths)
├── resources/rule_engine.log   # Application runtime logs
└── build.sbt                   # SBT dependencies
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

Update `Config.scala` with your environment details:

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
