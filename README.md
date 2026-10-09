# Payment Search API

A Java 17 / Spring Boot REST API that searches payment records in PostgreSQL. It has no authentication or authorization configured.

## Requirements

- Java 17 or newer
- Maven 3.9+
- PostgreSQL database

## Database configuration

The application is configured for your Neon host by default. Set `DB_PASSWORD` before starting it; the database password is deliberately not stored in the project. `DB_URL` and `DB_USERNAME` can also override the URL and username.

The Neon URL is the PostgreSQL JDBC form of your connection string (the JDBC driver requires the `jdbc:` prefix):

```text
jdbc:postgresql://ep-soft-shadow-b57phmcb-pooler.c-7.us-east-2.aws.neon.tech/neondb?sslmode=require&channelBinding=require
```

Set the password in your shell using the value from the Neon dashboard (do not include it in source files or commit it):

```bash
export DB_PASSWORD='<your-neon-database-password>'
```

Do not commit database credentials. The schema in `src/main/resources/schema.sql` creates the tables and indexes used by this example when the application starts. If your Neon database already has tables, review the schema and column names before running the application.

## Run

```bash
mvn spring-boot:run
```

The service starts on port `8080` by default (`PORT` can override it).

## Search endpoint

`GET /api/payments/find`

Required query parameters:

| Parameter | Description |
| --- | --- |
| `customerId` | Customer ID |
| `accountingMonth` | Accounting month in `yyyy-MM`, `MM/yyyy`, or `MM-yyyy` format |
| `pageNo` | Zero-based page number |
| `pageSize` | Page size from 1 to 200 |

Optional parameters: `search`, `amount`, `depositDate` (`yyyy-MM-dd`), `paymentType`, `bankAccountName` (bank account ID), `sortBy`, and `sortDir` (`asc` or `desc`).

Supported `sortBy` values: `paymentEntryId`, `depositBatchNumber`, `employerName`, `employerCode`, `originalAmount`, `amount`, `paymentType`, `paymentReferenceNo`, `paymentStatus`, `depositedDate`, and `memberName`. The default sort is deposited date descending, with payment entry ID descending as a stable tie-breaker.

Example:

```bash
curl 'http://localhost:8080/api/payments/find?customerId=42&accountingMonth=2026-09&pageNo=0&pageSize=20&search=Acme&sortBy=depositedDate&sortDir=desc'
```

The response includes `status`, `message`, and `data`; `data.reports` contains the records along with `pageNo`, `pageSize`, `totalElements`, and `totalPages`. Searches are scoped to tenant ID `1`, following the supplied controller example, and only include finalized/adjusted approved payments or reversed payments.

## Load demo payments

To insert three repeatable demo records into the connected database, stop the regular app and run:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo-seed
```

The demo profile seeds customer `990001` for accounting month `2026-10`, then leaves the API running. Demo rows use reserved-looking IDs starting with `990000001`; inserts do nothing if those IDs already exist. The seed SQL is in `src/main/resources/demo-data.sql`.

Example requests after seeding:

```text
http://localhost:8080/api/payments/find?customerId=990001&accountingMonth=2026-10&pageNo=0&pageSize=20
http://localhost:8080/api/payments/find?customerId=990001&accountingMonth=2026-10&pageNo=0&pageSize=20&search=Acme
http://localhost:8080/api/payments/find?customerId=990001&accountingMonth=2026-10&pageNo=0&pageSize=20&amount=1250.50
http://localhost:8080/api/payments/find?customerId=990001&accountingMonth=2026-10&pageNo=0&pageSize=20&depositDate=2026-10-03
```

## Build and test

```bash
mvn test
```
