📘 Event-Ledger-Api

A Spring Boot REST API for recording and retrieving financial events such as CREDIT and DEBIT transactions.

🚀 Features
Create financial events (CREDIT / DEBIT)
Retrieve events by account
Event ordering by timestamp
In-memory H2 database for development/testing
RESTful API with JSON responses
Integration tests using MockMvc

🧰 Tech Stack
Java 17+ / 21
Spring Boot
Spring Web
Spring Data JPA
H2 Database
Maven
JUnit 5 + MockMvc

📦 Prerequisites
Make sure you have installed:
Java JDK 17 or higher
java -version

Maven 3.6+
mvn -version

Git (optional)
Git Downloads

📥 Setup Instructions
1. Clone the Repository
git clone https://github.com/muthukumaran-k/Event-Ledger-Api.git
cd Event-Ledger-Api

3. Import into IDE (Optional)
You can open the project in:
Eclipse IDE
IntelliJ IDEA

Import as:
Maven Project

3. Install Dependencies
Maven automatically handles dependencies.
Run:
mvn clean install

This will:
Download dependencies
Compile the project
Run unit tests

▶️ How to Start the Application
Option 1: Using Maven
mvn spring-boot:run
Option 2: Using JAR File

Build first:
mvn clean package

Run the JAR:
java -jar target/event-ledger-api-0.0.1-SNAPSHOT.jar

🌐 Application URL

Once started:
http://localhost:8080
📡 Sample API Endpoints
➕ Create Event
POST /events
Content-Type: application/json

Example body:
{
  "eventId": "evt-101",
  "accountId": "acct-1",
  "type": "CREDIT",
  "amount": 100.00,
  "currency": "USD",
  "eventTimestamp": "2026-05-20T10:00:00Z"
}

📄 Get Events by Account
GET /events?account=acct-1

🧪 How to Run Tests
Run all tests
mvn test

Run with full logs
mvn -X test

Test Coverage (Optional Improvement)

To generate coverage report:
mvn jacoco:report
🧪 Test Stack
JUnit 5
Spring Boot Test
MockMvc

H2 in-memory database
🗄️ Database
This project uses H2 in-memory database.
H2 Console (if enabled):
http://localhost:8080/h2-console

Default config:
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

⚙️ Build Commands Summary
Action	Command
Build project	mvn clean install
Run app	mvn spring-boot:run
Run tests	mvn test
Package jar	mvn clean package

🧱 Project Structure
src/
 ├── main/
 │   ├── java/com/example/eventledger/
 │   │   ├── controller
 │   │   ├── service
 │   │   ├── repository
 │   │   ├── entity
 │   │   └── EventLedgerApiApplication.java
 │   └── resources/
 │       └── application.properties
 └── test/
     └── java/
         └── com/example/eventledger/
