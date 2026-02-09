# Election App

## What it does

Voting application with hexagonal architecture. Manages elections, voters, and vote casting with real-time result aggregation. Built with Spring Boot 3.2.5, Java 21, and PostgreSQL 16.

Core domain models:
- `Election` - voting container with options
- `Voter` - participant with email uniqueness and block status
- `Vote` - immutable cast vote record
- `ElectionResults` - aggregated vote counts per option

Business rules enforced:
- One vote per voter per election
- Blocked voters cannot vote
- Email uniqueness across all voters
- Votes are immutable once cast

Technical stack:
- Hexagonal architecture (package-based: domain, application, adapter.in, adapter.out)
- Framework-free domain and application layers
- MapStruct for DTO mapping
- Caffeine caching for election results
- Flyway migrations for schema versioning
- Bucket4j rate limiting (100 req/min per IP)
- Prometheus metrics and health probes
- OpenAPI documentation with SpringDoc

## Prerequisites

- Java 21
- Docker + Docker Compose
- Maven 3.9+

## Build

Compile and run tests:

```bash
mvn clean verify
```

Build Docker image:

```bash
docker build -t election-app:latest .
```

## Run

Start PostgreSQL and application:

```bash
docker-compose up --build
```

Application starts on port 8080 after PostgreSQL healthcheck passes.

## Test

Run all tests (unit + integration + architecture):

```bash
mvn test
```

Integration tests use Testcontainers with PostgreSQL 16.

Architecture tests validate:
- Domain layer has no Spring dependencies
- Application layer has no Spring dependencies
- Adapters properly depend on application ports
- Hexagonal boundaries are not violated

Key test classes:
- `ElectionTest` - domain invariants
- `VotingServiceTest` - business logic with Mockito
- `VotingServiceIntegrationTest` - full use case flow with Testcontainers
- `VotingControllerIntegrationTest` - HTTP endpoint testing
- `FullElectionFlowE2ETest` - complete voting workflow
- `ArchitectureTest` - ArchUnit rules

## API

Swagger UI: http://localhost:8080/swagger-ui.html

OpenAPI spec: http://localhost:8080/v3/api-docs

### Elections

- `POST /api/elections` - create election
- `GET /api/elections` - list all elections
- `GET /api/elections/{id}` - get election by ID
- `POST /api/elections/{id}/options` - add voting option

### Voters

- `POST /api/voters` - create voter
- `GET /api/voters` - list all voters
- `GET /api/voters/{id}` - get voter by ID
- `PATCH /api/voters/{id}/block` - block voter
- `PATCH /api/voters/{id}/unblock` - unblock voter

### Voting

- `POST /api/elections/{electionId}/votes` - cast vote
- `GET /api/elections/{electionId}/results` - get results (cached)

## API Examples

All examples below have been tested against a running application instance.

### Health Check

```bash
curl -s http://localhost:8080/actuator/health
# Response: {"status":"UP","groups":["liveness","readiness"]}
```

### Voters API

#### Create voter

```bash
curl -s -X POST http://localhost:8080/api/voters \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jan Kowalski",
    "email": "jan.kowalski@example.com"
  }'
# Response: {"id":"9dfae822-b6ba-4f35-bc61-a344e3833c4a","name":"Jan Kowalski","email":"jan.kowalski@example.com","status":"ACTIVE","createdAt":"2026-02-09T18:24:34.277900239Z"}
```

#### List all voters

```bash
curl -s http://localhost:8080/api/voters
# Response: [{"id":"9dfae822-b6ba-4f35-bc61-a344e3833c4a","name":"Jan Kowalski","email":"jan.kowalski@example.com","status":"ACTIVE","createdAt":"2026-02-09T18:24:34.277900Z"},...]
```

#### Get voter by ID

```bash
curl -s http://localhost:8080/api/voters/9dfae822-b6ba-4f35-bc61-a344e3833c4a
# Response: {"id":"9dfae822-b6ba-4f35-bc61-a344e3833c4a","name":"Jan Kowalski","email":"jan.kowalski@example.com","status":"ACTIVE","createdAt":"2026-02-09T18:24:34.277900Z"}
```

#### Block voter

```bash
curl -s -X PATCH http://localhost:8080/api/voters/9dfae822-b6ba-4f35-bc61-a344e3833c4a/block
# Response: {"id":"9dfae822-b6ba-4f35-bc61-a344e3833c4a","name":"Jan Kowalski","email":"jan.kowalski@example.com","status":"BLOCKED","createdAt":"2026-02-09T18:24:34.277900Z"}
```

#### Unblock voter

```bash
curl -s -X PATCH http://localhost:8080/api/voters/9dfae822-b6ba-4f35-bc61-a344e3833c4a/unblock
# Response: {"id":"9dfae822-b6ba-4f35-bc61-a344e3833c4a","name":"Jan Kowalski","email":"jan.kowalski@example.com","status":"ACTIVE","createdAt":"2026-02-09T18:24:34.277900Z"}
```

### Elections API

#### Create election

```bash
curl -s -X POST http://localhost:8080/api/elections \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wybory Burmistrza 2025"
  }'
# Response: {"id":"550cb983-fdd3-45a5-a320-503ef8777c94","name":"Wybory Burmistrza 2025","votingOptions":[],"createdAt":"2026-02-09T18:24:57.736036459Z"}
```

#### List all elections

```bash
curl -s http://localhost:8080/api/elections
# Response: [{"id":"550cb983-fdd3-45a5-a320-503ef8777c94","name":"Wybory Burmistrza 2025","votingOptions":[],"createdAt":"2026-02-09T18:24:57.736036Z"}]
```

#### Get election by ID

```bash
curl -s http://localhost:8080/api/elections/550cb983-fdd3-45a5-a320-503ef8777c94
# Response: {"id":"550cb983-fdd3-45a5-a320-503ef8777c94","name":"Wybory Burmistrza 2025","votingOptions":[...],"createdAt":"2026-02-09T18:24:57.736036Z"}
```

#### Add voting option (candidate)

```bash
curl -s -X POST http://localhost:8080/api/elections/550cb983-fdd3-45a5-a320-503ef8777c94/options \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Kandydat A - Maria Kowalska"
  }'
# Response: {"id":"6d334579-c0fc-4c36-847d-bda8095d4f35","name":"Kandydat A - Maria Kowalska"}
```

### Voting API

#### Cast vote

```bash
curl -s -X POST http://localhost:8080/api/elections/550cb983-fdd3-45a5-a320-503ef8777c94/votes \
  -H "Content-Type: application/json" \
  -d '{
    "voterId": "9dfae822-b6ba-4f35-bc61-a344e3833c4a",
    "votingOptionId": "6d334579-c0fc-4c36-847d-bda8095d4f35"
  }'
# Response: {"id":"00da368f-3df7-4b86-9dd2-722f69c9e217","voterId":"9dfae822-b6ba-4f35-bc61-a344e3833c4a","electionId":"550cb983-fdd3-45a5-a320-503ef8777c94","votingOptionId":"6d334579-c0fc-4c36-847d-bda8095d4f35","castAt":"2026-02-09T18:25:16.721160918Z"}
```

#### Get election results

```bash
curl -s http://localhost:8080/api/elections/550cb983-fdd3-45a5-a320-503ef8777c94/results
# Response: {"electionId":"550cb983-fdd3-45a5-a320-503ef8777c94","electionName":"Wybory Burmistrza 2025","results":[{"optionId":"6d334579-c0fc-4c36-847d-bda8095d4f35","optionName":"Kandydat A - Maria Kowalska","voteCount":2,"percentage":66.66666666666667},{"optionId":"79dc83a6-7bea-4689-9484-4e35096c4845","optionName":"Kandydat B - Piotr Nowak","voteCount":1,"percentage":33.333333333333336}],"totalVotes":3}
```

### Error Scenarios

#### Attempt duplicate vote

```bash
curl -s -X POST http://localhost:8080/api/elections/550cb983-fdd3-45a5-a320-503ef8777c94/votes \
  -H "Content-Type: application/json" \
  -d '{
    "voterId": "9dfae822-b6ba-4f35-bc61-a344e3833c4a",
    "votingOptionId": "79dc83a6-7bea-4689-9484-4e35096c4845"
  }'
# Response: {"timestamp":"2026-02-09T18:25:19.793303318Z","status":409,"errorCode":"DUPLICATE_VOTE","message":"Voter already voted in this election","path":"/api/elections/550cb983-fdd3-45a5-a320-503ef8777c94/votes"}
```

#### Attempt vote by blocked voter

```bash
# First block the voter
curl -s -X PATCH http://localhost:8080/api/voters/34aace2c-c74e-4ffa-8277-312073598106/block

# Then try to vote
curl -s -X POST http://localhost:8080/api/elections/550cb983-fdd3-45a5-a320-503ef8777c94/votes \
  -H "Content-Type: application/json" \
  -d '{
    "voterId": "34aace2c-c74e-4ffa-8277-312073598106",
    "votingOptionId": "79dc83a6-7bea-4689-9484-4e35096c4845"
  }'
# Response: {"timestamp":"2026-02-09T18:25:41.551765993Z","status":409,"errorCode":"VOTER_BLOCKED","message":"Voter is blocked: 34aace2c-c74e-4ffa-8277-312073598106","path":"/api/elections/550cb983-fdd3-45a5-a320-503ef8777c94/votes"}
```

### Observability

- Health: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics
- Prometheus: http://localhost:8080/actuator/prometheus

Liveness probe: `/actuator/health/liveness`
Readiness probe: `/actuator/health/readiness`

## Stop

```bash
docker compose down
```

Remove volumes:

```bash
docker compose down -v
```
# -election-app
