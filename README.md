# Smart Campus API

## Overview

This project is a JAX-RS REST API for a university Smart Campus scenario. It manages rooms, sensors installed in those rooms, and historical sensor readings. The API is designed around RESTful resource modelling, nested resources, consistent JSON responses, and custom exception mapping.

The implementation follows the coursework constraints:

- JAX-RS with Jersey
- In-memory storage only using Java collections
- No database
- No Spring Boot or alternative frameworks

## API Design Summary

The API is versioned under `/api/v1` and uses a resource hierarchy that matches the domain:

- `rooms` represent physical spaces on campus
- `sensors` represent devices linked to rooms
- `readings` are nested under sensors because readings only make sense in the context of a specific sensor

The design also includes:

- A discovery endpoint at the API root
- Query-based filtering for sensor type searches
- Business-rule enforcement when deleting rooms
- Exception mappers for predictable JSON error responses
- Request and response logging using JAX-RS filters

## Technology Stack

- Java 8
- Maven
- JAX-RS (`javax.ws.rs`)
- Jersey 2.32
- Jackson JSON provider
- WAR packaging for servlet container deployment

## Project Structure

```text
smart-campus-api/
├── pom.xml
├── src/main/java/com/smartcampus/
│   ├── SmartCampusApplication.java
│   ├── data/DataStore.java
│   ├── exception/
│   ├── filter/LoggingFilter.java
│   ├── model/
│   └── resource/
└── src/main/webapp/META-INF/context.xml
```

## Core Models

### Room

- `id`
- `name`
- `capacity`
- `sensorIds`

### Sensor

- `id`
- `type`
- `status`
- `currentValue`
- `roomId`

### SensorReading

- `id`
- `timestamp`
- `value`

## Seed Data

The in-memory `DataStore` starts with example records so the API can be tested immediately:

- Rooms: `R-1`, `R-2`
- Sensors: `S-1`, `S-2`, `S-3`
- `S-3` starts in `MAINTENANCE` status to demonstrate the `403 Forbidden` reading constraint

## Base URL

When deployed to a servlet container using the included context path, the API base URL is:

```text
http://localhost:8080/smart-campus-api/api/v1
```

## Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/v1` | Discovery endpoint with metadata and links |
| GET | `/api/v1/rooms` | List all rooms |
| POST | `/api/v1/rooms` | Create a room |
| GET | `/api/v1/rooms/{roomId}` | Get one room |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room if it has no sensors |
| GET | `/api/v1/sensors` | List all sensors |
| GET | `/api/v1/sensors?type=CO2` | Filter sensors by type |
| POST | `/api/v1/sensors` | Create a sensor linked to an existing room |
| GET | `/api/v1/sensors/{sensorId}/readings` | Get reading history for a sensor |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a reading and update sensor `currentValue` |

## Error Handling

The API returns structured JSON errors through custom exception mappers.

| Status | Scenario |
| --- | --- |
| `404 Not Found` | Room or sensor does not exist |
| `409 Conflict` | Attempt to delete a room that still has sensors |
| `422 Unprocessable Entity` | Sensor creation references a room that does not exist |
| `403 Forbidden` | Attempt to add a reading to a sensor in `MAINTENANCE` |
| `500 Internal Server Error` | Any unhandled runtime error |

Example error payload:

```json
{
  "errorMessage": "Cannot create sensor: room 'R-99' does not exist.",
  "errorCode": 422,
  "documentation": "https://smartcampus.edu/api/docs/errors#linked-resource-not-found"
}
```

## Build And Run

### Prerequisites

- JDK 8 or later
- Maven installed and available on the command line
- Apache Tomcat 9

Tomcat 11 is not supported for this project. The API uses the `javax.ws.rs` / Jersey 2.x stack, while Tomcat 11 is aligned with the newer `jakarta.*` namespace.

### Build And Run In NetBeans

1. Install or extract Tomcat 9 to a writable location, for example `C:\Users\keith\Tools\apache-tomcat-9.0.117`.
2. In NetBeans, open `Services` -> `Servers` -> `Add Server`.
3. Select `Apache Tomcat or TomEE`.
4. Set the server location to your Tomcat 9 directory.
5. Open project `Properties` -> `Run`.
6. Set `Server` to the Tomcat 9 instance.
7. Run the project.

NetBeans will build the WAR, deploy it in place, and start the application.

Use this URL to verify the deployment:

```text
http://localhost:8080/smart-campus-api/api/v1/
```

### Build And Run From The Command Line

From the project root:

```bash
mvn clean package
```

This produces a WAR file in `target/`, for example:

```text
target/smart-campus-api-1.0-SNAPSHOT.war
```

Deploy the generated WAR to Tomcat 9 and start the server.

Example Tomcat 9 startup command on Windows:

```powershell
& "C:\Users\keith\Tools\apache-tomcat-9.0.117\bin\catalina.bat" run
```

If you copy the WAR into Tomcat's `webapps/`, verify the API at:

```text
http://localhost:8080/smart-campus-api/api/v1/
```

## Sample cURL Commands

### 1. Discover the API

```bash
curl -i http://localhost:8080/smart-campus-api/api/v1
```

### 2. List all rooms

```bash
curl -i http://localhost:8080/smart-campus-api/api/v1/rooms
```

### 3. Create a new room

```bash
curl -i -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Seminar Room B\",\"capacity\":30}"
```

### 4. List all sensors

```bash
curl -i http://localhost:8080/smart-campus-api/api/v1/sensors
```

### 5. Filter sensors by type

```bash
curl -i "http://localhost:8080/smart-campus-api/api/v1/sensors?type=CO2"
```

### 6. Create a sensor for an existing room

```bash
curl -i -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":22.4,\"roomId\":\"R-1\"}"
```

### 7. Add a reading to a sensor

```bash
curl -i -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/S-1/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\":23.7}"
```

### 8. Get reading history for a sensor

```bash
curl -i http://localhost:8080/smart-campus-api/api/v1/sensors/S-1/readings
```

### 9. Trigger a `409 Conflict` by deleting a room that still has sensors

```bash
curl -i -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/R-1
```

### 10. Trigger a `422 Unprocessable Entity` by linking a sensor to a missing room

```bash
curl -i -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"type\":\"CO2\",\"roomId\":\"R-99\"}"
```

## Coursework Report Answers

### 1. Server Architecture and setup

#### 1.1 Project and application setup

**Question**

In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

**Answer**

By default, JAX-RS resource classes follow a per-request lifecycle, meaning a new instance is created for each incoming HTTP request. This aligns with the stateless nature of REST, where resource objects do not retain client-specific state between requests. Consequently, instance fields in classes such as SensorRoomResource or SensorResource cannot be used to persist data, as they are reinitialised on each request.

To maintain application state, data is instead stored in shared in memory structures (e.g., static collections in the DataStore class), allowing it to persist across requests. However because these collections are shared mutable state accessed by multiple threads, they are prone to race conditions and inconsistent updates. Therefore proper synchronization or the use of thread safe collections would be required in a production environment.

#### 1.2 The “Discovery” endpoint

**Question**

Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

**Answer**

Hypermedia is considered a hallmark of advanced RESTful design because it allows the server to guide the client dynamically by embedding links and navigation options within responses. This reflects the HATEOAS principle, where clients do not need to hard-code endpoint paths, but instead discover available resources and actions at runtime from the API’s representations. In this coursework, the discovery endpoint that exposes links to collections such as rooms and sensors directly demonstrates this approach.

This benefits client developers by making the API more self-descriptive and reducing the reliance on external documentation tools such as Swagger in which clients must know and hard code paths in advance. If those paths change, such clients are prone to breaking. In contrast, hypermedia driven clients can continue functioning by following updated links in responses.

### 2. Room Management

#### 2.1 Room Resource Implementation

**Question**

When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.

**Answer**

Returning only room IDs reduces payload size, improving bandwidth efficiency and response times, but shifts complexity to the client, which must make additional requests to retrieve full details, increasing latency and HTTP overhead. Returning full room objects however results in larger responses but provides all necessary data in a single call, simplifying client-side processing and reducing the need for multiple round trips. Ultimately, the preferred approach depends on the use case and business context, Id only responses suit scenarios prioritising scalability and minimal data transfer, while full representations are more appropriate when clients require immediate access to detailed information.

#### 2.2 Room Deletion & Safety Logic

**Question**

Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

**Answer**

Yes, DELETE is idempotent in the sense that repeating the same delete request should not create additional side effects after the first successful deletion. In this implementation, if a room exists and has no assigned sensors, the first DELETE removes it and returns a 204 No Content. If the same DELETE is sent again, the room is no longer present, so the API returns a 404 Not Found error. The important point is that the second request does not delete anything new or change server state again. If the room still contains sensors,any amount of requests are blocked every time with 409 Conflict until those sensors are removed. Repeating that same blocked request also does not create a new side effect, so the behaviour remains idempotent.

### 3. Sensor Resource & Integrity

#### 3.1 Sensor Resource & Integrity

**Question**

We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

**Answer**

@Consumes(MediaType.APPLICATION_JSON) tells JAX-RS that the POST method only accepts JSON. If the client sends a different content type such as text/plain or application/xml, the request does not match a suitable message body reader for that method. In practice,

JAX-RS will reject the request, typically with `415 Unsupported Media Type`. This is useful because it enforces a clear API contract. The server does not try to guess how to parse unsupported data formats, and the client gets immediate feedback that the payload format is invalid for that endpoint. It also simplifies the logic within the POST endpoint by avoiding the need to handle multiple media types, keeping the implementation focused and free from unnecessary complexity.

#### 3.2 Filtered Retrieval & Search

**Question**

You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

**Answer**

Using @QueryParam("type") for /api/v1/sensors?type=CO2 is preferable than embedding the filter in the path (e.g., /api/v1/sensors/type/CO2) because filtering does not represent a distinct resource with its own identity, but simply a filtered view (subset) of the existing sensors collection. A path segment typically denotes a specific resource or hierarchical entity (e.g., /sensors/{id}), whereas filtering does not create a new entity it only modifies which results are returned. Query parameters are therefore more semantically appropriate for search and filtering operations.

Additionally, query parameters are more flexible and scalable, as they allow multiple criteria to be combined naturally (e.g., /api/v1/sensors?type=CO2&status=ACTIVE). In contrast, encoding filters in the path leads to rigid and less maintainable URL structures as filtering requirements grow. Overall, the query parameter approach better supports extensibility, readability, and adherence to RESTful design principles.

### 4. Deep Nesting with Sub - Resources

#### 4.1 The Sub-Resource Locator Pattern

**Question**

Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

**Answer**

The Sub-Resource Locator pattern improves modularity by delegating nested paths to dedicated classes, allowing each resource to focus on a single responsibility. For example, SensorResource handles /sensors while SensorReadingResource handles /sensors/{id}/readings. As the API grows, additional nested resources such as /alerts or /history can be added without overloading a single controller. In contrast, placing all nested logic in one class leads to large, tightly coupled code that is harder to maintain, test, and extend. By separating concerns into distinct classes, the API remains scalable, easier to reason about, and better structured around resource relationships.

### 5. Advanced Error Handling, Exception Mapping & Logging

#### 5.2 Dependency Validation (422 Unprocessable Entity)

**Question**

Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

**Answer**

payload contains an invalid reference. In the SensorResource , POST /api/v1/sensors is a valid endpoint and the request body may be valid JSON, but if the supplied roomId does not exist then the server cannot process the entity correctly. Using 404 would suggest that the requested endpoint or requested direct resource could not be found. In contrast, 422 communicates that the request syntax is acceptable, but the content fails semantic validation because it points to a linked resource that does not exist.

#### 5.4 The Global Safety Net (500)

**Question**

From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

**Answer**

Cybersecurity risks of exposing Java stack traces
Exposing stack traces to external clients leaks internal implementation details that attackers can use for reconnaissance. A stack trace can reveal package names, class names, method names, library versions, file paths, and the internal structure of the application. That information makes it easier to identify frameworks in use, guess vulnerable components, and target specific code paths. It can also expose logic assumptions and validation gaps, which may help an attacker

plan malicious requests more effectively. For that reason, the API should log the full error internally for developers, but only return a generic `500 Internal Server Error` message to clients. The request syntax is acceptable, but the content fails semantic validation because it points to a linked resource that does not exist.

#### 5.5 API Request & Response Logging Filters

**Question**

Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

**Answer**

Logging is a cross-cutting concern, so JAX-RS filters are the appropriate place to implement it. Request and response filters execute automatically for every request, ensuring logging is applied consistently across the entire API. This centralisation avoids code duplication and eliminates the risk of developers forgetting to add logging in individual resource methods.

While Logger.info() statements can still be useful within specific resource methods to trace complex or business-critical logic, relying on them alone leads to inconsistent and scattered logging. Filters keep resource classes focused on core functionality, enforce a standard logging approach, and improve maintainability by separating cross cutting-concerns from business logic.

## Notes

- Data is stored only in memory using `HashMap` and `ArrayList`.
- Restarting the server resets runtime-created data.
- Logging is handled through `java.util.logging` in a request/response filter.
