# Insurance Client & Contract Management API

A RESTful API for managing insurance clients (Person/Company) and their contracts, built with Spring Boot.

## Architecture & Design

This application follows a **layered architecture** with clear separation of concerns:
- **Controller Layer**: REST endpoints with Bean Validation
- **Service Layer**: Business logic and transaction management
- **Repository Layer**: Data persistence using Spring Data JPA with custom queries
- **Entity Layer**: Domain models using Single Table Inheritance for Client hierarchy (Person/Company)

**Key design decisions:**
- **Single Table Inheritance**: Efficient storage for Person and Company sharing common fields, using `@DiscriminatorColumn` for type distinction
- **DTO Pattern**: Separates API contracts from domain models, controlling field exposure (updateDate hidden from API)
- **Cascade Operations**: Automatic contract end date updates when clients are deleted
- **Database Indexing**: Indexes on clientId and updateDate for performant queries
- **Native SQL for aggregation**: Sum endpoint uses database-level aggregation for optimal performance
- **H2 file-based persistence**: Data survives application restarts, stored in `./data/insurance.mv.db`

The architecture ensures maintainability, testability, and performance while respecting SOLID principles.

## Prerequisites

- Java 21 or higher
- Maven 3.6+

## How to Run

### 1. Clone the repository
```bash
git clone https://github.com/YOUR-USERNAME/insurance-api.git
cd insurance-api
```

### 2. Build the application
```bash
mvn clean install
```

### 3. Run the application
```bash
mvn spring-boot:run
```

The API will start on **http://localhost:8080**

### 4. Access the H2 Database Console (Optional)

- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:file:./data/insurance`
- **Username**: `sa`
- **Password**: *(leave empty)*

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/clients` | Create client (Person/Company) |
| GET | `/api/clients/{id}` | Get client details |
| PUT | `/api/clients/{id}` | Update client (except birthdate/companyIdentifier) |
| DELETE | `/api/clients/{id}` | Delete client (updates contract end dates) |
| POST | `/api/contracts` | Create contract |
| PATCH | `/api/contracts/{id}/cost` | Update contract cost (auto-updates updateDate) |
| GET | `/api/contracts/client/{clientId}` | Get active contracts (optional: ?updatedAfter=YYYY-MM-DD) |
| GET | `/api/contracts/client/{clientId}/sum` | Get sum of active contracts (performant) |

## Key Features Implemented

✅ **Two client types**: Person (with birthdate) and Company (with identifier)  
✅ **Full CRUD operations** on clients and contracts  
✅ **Immutable fields**: Birthdate and company identifier cannot be updated  
✅ **Cascade delete**: Contract end dates automatically set to current date when client deleted  
✅ **Smart defaults**: Start date defaults to current date if not provided  
✅ **Hidden internal fields**: updateDate maintained internally but NOT exposed in API  
✅ **Active contract filtering**: Returns only contracts where current date < end date  
✅ **Update date filtering**: Optional filter by updateDate on active contracts  
✅ **Performant aggregation**: Native SQL query for sum calculation using database aggregation  
✅ **Full validation**: Email, phone (must start with +), dates (ISO 8601), positive numbers  
✅ **RESTful design**: Proper HTTP methods (GET, POST, PUT, PATCH, DELETE) and status codes  
✅ **Data persistence**: H2 file-based database survives application crashes and restarts  
✅ **Comprehensive tests**: 46 unit and integration tests with 100% success rate  
✅ **Error handling**: Consistent error response format with proper HTTP status codes

## Request/Response Examples

### Create Person Client

**Request:**
```json
POST /api/clients
Content-Type: application/json

{
  "type": "PERSON",
  "name": "Alice Johnson",
  "email": "alice@example.com",
  "phone": "+41789123456",
  "birthdate": "1985-03-20"
}
```

**Response (201 Created):**
```json
{
  "type": "PERSON",
  "id": 1,
  "name": "Alice Johnson",
  "email": "alice@example.com",
  "phone": "+41789123456",
  "birthdate": "1985-03-20"
}
```

### Create Company Client

**Request:**
```json
POST /api/clients
Content-Type: application/json

{
  "type": "COMPANY",
  "name": "SwissBank AG",
  "email": "info@swissbank.ch",
  "phone": "+41227654322",
  "companyIdentifier": "CHE-987.654.321"
}
```

### Create Contract

**Request:**
```json
POST /api/contracts
Content-Type: application/json

{
  "clientId": 1,
  "costAmount": 2500.00,
  "startDate": "2025-01-01",
  "endDate": "2026-01-01"
}
```

**Note:**
- If `startDate` is omitted, it defaults to current date
- If `endDate` is omitted, it remains null (open-ended contract)
- `updateDate` is set internally and NOT returned in response

### Update Contract Cost

**Request:**
```json
PATCH /api/contracts/1/cost
Content-Type: application/json

{
  "costAmount": 3000.00
}
```

**Effect:** Cost updated AND updateDate automatically set to current timestamp (internal only).

### Get Active Contracts with Filter
```bash
GET /api/contracts/client/1?updatedAfter=2025-10-01
```

Returns only active contracts updated after October 1, 2025.

### Get Sum of Active Contracts
```bash
GET /api/contracts/client/1/sum
```

**Response:**
```json
{
  "totalCost": 5500.00
}
```

**Performance:** Uses native SQL with database aggregation for optimal speed.

## Technologies Used

- **Java 25**
- **Spring Boot 3.5.6**
- **Spring Data JPA** (data persistence)
- **H2 Database** (file-based persistence)
- **Bean Validation** (Hibernate Validator)
- **Lombok** (reduce boilerplate)
- **JUnit 5 & Mockito** (testing)
- **Maven** (build tool)

## Project Structure
```
insurance-api/
├── src/
│   ├── main/
│   │   ├── java/com/insurance/api/
│   │   │   ├── controller/       # REST endpoints (ClientController, ContractController)
│   │   │   ├── service/          # Business logic (ClientService, ContractService)
│   │   │   ├── repository/       # Data access with custom queries
│   │   │   ├── entity/           # JPA entities (Client, Person, Company, Contract)
│   │   │   ├── dto/              # Data transfer objects (API contracts)
│   │   │   └── exception/        # Exception handling (GlobalExceptionHandler)
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/insurance/api/
│           ├── controller/       # Controller integration tests
│           ├── service/          # Service unit tests
│           └── repository/       # Repository integration tests
├── data/                         # H2 database files (auto-created)
│   └── insurance.mv.db
├── pom.xml
├── .gitignore
└── README.md
```

## Validation Rules

| Field | Rule | Example |
|-------|------|---------|
| Email | Valid email format | `john@example.com` |
| Phone | Must start with `+` and contain 10-15 digits | `+41791234567` |
| Birthdate | Must be in the past (ISO 8601) | `1990-05-15` |
| Cost Amount | Must be positive | `1500.50` |
| Company Identifier | Pattern: xxx-xxx (letters, numbers, hyphens, dots) | `CHE-123.456.789` |
| All dates | ISO 8601 format (yyyy-MM-dd) | `2025-10-19` |

## Error Handling

All errors return a consistent JSON format:
```json
{
  "timestamp": "2025-10-19T16:30:00",
  "status": 400,
  "error": "Validation Failed",
  "messages": [
    "email: Email must be valid",
    "phone: Phone must start with + and contain 10-15 digits"
  ],
  "path": "/api/clients"
}
```

**HTTP Status Codes:**
- `200 OK`: Successful GET/PUT/PATCH
- `201 Created`: Successful POST
- `204 No Content`: Successful DELETE
- `400 Bad Request`: Validation failed
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Unexpected error

## Database Schema

### CLIENTS Table (Single Table Inheritance)
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| client_type | VARCHAR | NOT NULL (PERSON/COMPANY) |
| name | VARCHAR | NOT NULL |
| email | VARCHAR | NOT NULL |
| phone | VARCHAR | NOT NULL |
| birthdate | DATE | NULLABLE (only for PERSON), NOT UPDATABLE |
| company_identifier | VARCHAR | NULLABLE (only for COMPANY), UNIQUE, NOT UPDATABLE |

### CONTRACTS Table
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| client_id | BIGINT | FOREIGN KEY → CLIENTS(id), NOT NULL |
| cost_amount | DECIMAL(19,2) | NOT NULL, POSITIVE |
| start_date | DATE | NOT NULL |
| end_date | DATE | NULLABLE |
| update_date | TIMESTAMP | NOT NULL, INTERNAL ONLY |

**Indexes:**
- `idx_client_id` on client_id (for faster contract lookups)
- `idx_update_date` on update_date (for filtered queries)

## Business Rules

1. **Client Types**: System supports two client types with different fields:
    - Person: requires birthdate
    - Company: requires company identifier

2. **Immutable Fields**:
    - Person's birthdate cannot be changed after creation
    - Company's identifier cannot be changed after creation

3. **Contract Lifecycle**:
    - Start date defaults to current date if not provided
    - End date can be null for open-ended contracts
    - updateDate automatically maintained on cost updates (not exposed in API)

4. **Cascade Delete**:
    - When a client is deleted, all their active contracts have end_date set to current date
    - Expired contracts remain unchanged

5. **Active Contracts**:
    - A contract is active if: end_date is NULL OR current_date < end_date

## Development

### Run in development mode
```bash
mvn spring-boot:run
```

### Run tests
```bash
mvn test
```

### Build JAR file
```bash
mvn clean package
java -jar target/insurance-api-0.0.1-SNAPSHOT.jar
```

### Clean database (start fresh)
```bash
rm -rf data/
mvn spring-boot:run
```

## Author

Created as part of a technical exercise to demonstrate:
- RESTful API design following best practices
- Clean architecture with layered approach
- Spring Boot framework expertise
- Data persistence and optimization
- Comprehensive testing strategy
- Professional code quality and documentation

---

**GitHub Repository:** https://github.com/yasserchby/insurance-api

For questions or issues, please open an issue on GitHub.