# KKC Backend

Java 21 Spring Boot backend for donation payment order creation, Razorpay payment verification, Razorpay webhook endpoint, Chatbot FAQ APIs, and PostgreSQL persistence.

## Local database

```sql
CREATE DATABASE kkc_db;
```

## Environment variables

```env
DB_URL=jdbc:postgresql://localhost:5432/kkc_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
RAZORPAY_KEY_ID=rzp_test_xxxxxxxxxxxxxx
RAZORPAY_KEY_SECRET=xxxxxxxxxxxxxxxx
RAZORPAY_WEBHOOK_SECRET=xxxxxxxxxxxxxxxx
FRONTEND_ORIGIN=http://localhost:5173
```

## Run

```bash
mvn clean spring-boot:run
```

## APIs

```text
POST /api/payments/create-order
POST /api/payments/verify
POST /api/payments/webhook/razorpay
GET  /api/chatbot/faqs
POST /api/chatbot/message
```

Do not expose `RAZORPAY_KEY_SECRET`, `RAZORPAY_WEBHOOK_SECRET`, or database credentials in React.
