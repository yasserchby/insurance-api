# Insurance Client & Contract Management API

A RESTful API for managing insurance clients (Person/Company) and their contracts, built with Spring Boot.

## Architecture & Design

This application follows a **layered architecture** with clear separation of concerns:
- **Controller Layer**: REST endpoints with validation
- **Service Layer**: Business logic and transaction management
- **Repository Layer**: Data persistence using Spring Data JPA
- **Entity Layer**: Domain models with inheritance (Client → Person/Company)

Key design decisions:
- **Single Table Inheritance** for Client types (Person/Company) using `@DiscriminatorColumn`
- **Cascade operations** to handle contract lifecycle when clients are deleted
- **Database indexing** on clientId and updateDate for performant queries
- **DTO pattern** to control API exposure (hide internal fields like updateDate)
- **Bean Validation** for data integrity (email, phone, dates, numbers)
- **H2 database** with file persistence for easy local development

The performant sum endpoint uses a native query with aggregation at the database level for optimal performance.

## Prerequisites

- Java 21 or higher
- Maven 3.6+

## How to Run

1. **Clone the repository**
```bash
git clone <repository-url>
cd insurance-api
```

2. **Build the application**
```bash
mvn clean install
```

3. **Run the application**
```bash
mvn spring-boot:run
```

The API will start on `http://localhost:8080`

## Running Tests
```bash
mvn test
```

The project includes 46 comprehensive tests covering:
- Unit tests for services
- Integration tests for controllers
- Repository tests for custom queries

## API Endpoints

### Client Management

#### Create Client (Person)
```bash
POST /api/clients
Content-Type: application/json

{
  "type": "PERSON",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+41791234567",
  "birthdate": "1990-05-15"
}
```

#### Create Client (Company)
```bash
POST /api/clients
Content-Type: application/json

{
  "type": "COMPANY",
  "name": "TechCorp SA",
  "email": "contact@techcorp.com",
  "phone": "+41227654321",
  "companyIdentifier": "CHE-123.456.789"
}
```

#### Get Client
```bash
GET /api/clients/{id}
```

#### Update Client
```bash
PUT /api/clients/{id}
Content-Type: application/json

{
  "name": "John Smith",
  "email": "john.smith@example.com",
  "phone": "+41791112233"
}
```

Note: Birthdate and company identifier cannot be updated.

#### Delete Client
```bash
DELETE /api/clients/{id}
```

When a client is deleted, the end date of all their active contracts is automatically set to the current date.

### Contract Management

#### Create Contract
```bash
POST /api/contracts
Content-Type: application/json

{
  "clientId": 1,
  "costAmount": 1500.50,
  "startDate": "2025-01-01",
  "endDate": "2026-01-01"
}
```

If `startDate` is not provided, it defaults to the current date.  
If `endDate` is not provided, it remains null (open-ended contract).

#### Update Contract Cost
```bash
PATCH /api/contracts/{id}/cost
Content-Type: application/json

{
  "costAmount": 1800.00
}
```

The `updateDate` field is automatically updated internally when the cost is modified.

#### Get Active Contracts for Client
```bash
GET /api/contracts/client/{clientId}
```

Returns only active contracts (current date < end date or end date is null).

Optional filter by update date:
```bash
GET /api/contracts/client/{clientId}?updatedAfter=2025-01-01
```

#### Get Sum of Active Contracts (Performant)
```bash
GET /api/contracts/client/{clientId}/sum
```

This endpoint uses a native SQL query with database-level aggregation for optimal performance.

## Database

The application uses H2 database with file persistence. Data is stored in `./data/insurance.mv.db`

### H2 Console Access
- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:file:./data/insurance`
- **Username**: `sa`
- **Password**: *(leave empty)*

## Validation Rules

- **Email**: Must be a valid email format
- **Phone**: Must start with + and contain 10-15 digits
- **Dates**: ISO 8601 format (yyyy-MM-dd)
- **Cost Amount**: Must be positive
- **Birthdate**: Must be in the past
- **Company Identifier**: Must match pattern xxx-xxx (letters/numbers and hyphens)

## Example Usage
```bash
# Create a person client
curl -X POST http://localhost:8080/api/clients \
  -H "Content-Type: application/json" \
  -d '{
    "type": "PERSON",
    "name": "Alice Johnson",
    "email": "alice@example.com",
    "phone": "+41789123456",
    "birthdate": "1985-03-20"
  }'

# Create a contract for the client
curl -X POST http://localhost:8080/api/contracts \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": 1,
    "costAmount": 2500.00,
    "startDate": "2025-01-01",
    "endDate": "2026-01-01"
  }'

# Get all active contracts
curl http://localhost:8080/api/contracts/client/1

# Get sum of active contracts
curl http://localhost:8080/api/contracts/client/1/sum
```

## Technologies Used

- **Spring Boot 3.5.6**
- **Spring Data JPA**
- **H2 Database**
- **Bean Validation (Hibernate Validator)**
- **Lombok**
- **JUnit 5 & Mockito** (for testing)
- **Maven**

## Project Structure
```
src/
├── main/
│   ├── java/com/insurance/api/
│   │   ├── controller/      # REST endpoints
│   │   ├── service/         # Business logic
│   │   ├── repository/      # Data access
│   │   ├── entity/          # Domain models
│   │   ├── dto/             # Data transfer objects
│   │   └── exception/       # Exception handling
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/insurance/api/
        ├── controller/      # Controller tests
        ├── service/         # Service tests
```

## License

This project is part of a technical exercise.