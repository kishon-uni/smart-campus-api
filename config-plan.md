# Configuration Changes — smart-campus-api

## Context

NetBeans scaffolded this project with Jakarta EE 11 defaults. The coursework requires Jersey 2.32 (`javax.ws.rs.*` imports), not Jakarta EE 11 (`jakarta.ws.rs.*`). These changes bring the project config in line with Tutorial Week 09 patterns before any application code is written.

---

## Change 1 — `pom.xml`

**Problem:** NetBeans generated `jakarta.jakartaee-api` v11 as the dependency and set the Java compiler to version 17. This is incompatible with Jersey 2.32 which uses `javax.ws.rs.*` imports.

**Removed:**
- `<properties>` block containing `<jakartaee>11.0.0-M1</jakartaee>`
- `jakarta.jakartaee-api` dependency

**Added:**
- Three Jersey 2.32 dependencies (matching Tutorial Week 09 `pom.xml` exactly):
  - `jersey-hk2` — dependency injection
  - `jersey-container-servlet` — Jersey servlet container
  - `jersey-media-json-jackson` — JSON serialisation via Jackson

**Updated:**
- Compiler plugin: `17` → `1.8` (Java 8)
- War plugin: added `<failOnMissingWebXml>false</failOnMissingWebXml>`

---

## Change 2 — `src/main/webapp/WEB-INF/web.xml`

**Problem:** Only contained a session timeout. No Jersey servlet definition, so Tomcat had no way to route incoming HTTP requests to Jersey.

**Replaced** the entire file with a Jersey servlet definition (Tutorial Week 09 `web.xml` structure):

- `servlet-class`: `org.glassfish.jersey.servlet.ServletContainer`
- `jersey.config.server.provider.packages`: `com.mycompany.smart.campus.api` — Jersey scans this root package and all sub-packages to discover resources, mappers, and filters automatically
- `url-pattern`: `/api/v1/*` — establishes the versioned API base path required by the coursework specification

---

## Change 3 — Deleted `JakartaRestConfiguration.java`

**Problem:** Used `jakarta.ws.rs` imports which do not compile with Jersey 2.32. Also had `@ApplicationPath("resources")` which does not match the coursework-required `/api/v1` path.

**Action:** Deleted. The `web.xml` package scan handles routing — no Application subclass is needed (consistent with Tutorial Week 09 which has no Application class).

---

## Change 4 — Deleted forbidden and unnecessary files

| File | Reason |
|---|---|
| `src/main/resources/META-INF/persistence.xml` | JPA/database configuration — coursework explicitly forbids databases; presence risks zero mark |
| `src/main/webapp/WEB-INF/beans.xml` | CDI (Contexts and Dependency Injection) config — not used in Jersey 2.32 |
| `src/main/java/.../resources/JakartaEE11Resource.java` | NetBeans boilerplate test resource — not part of the API |
| `src/main/webapp/index.html` | Default welcome page — not needed |

---

## Files Kept

| File | Reason |
|---|---|
| `src/main/webapp/META-INF/context.xml` | Sets Tomcat context path to `/smart-campus-api` — controls the URL root |
| `src/main/webapp/WEB-INF/web.xml` | Jersey servlet definition (rewritten in Change 2) |

---

## Final Project Structure (post-config, pre-code)

```
smart-campus-api/
├── pom.xml                          ← Jersey 2.32, Java 8, WAR packaging
└── src/main/webapp/
    ├── META-INF/
    │   └── context.xml              ← Tomcat context path = /smart-campus-api
    └── WEB-INF/
        └── web.xml                  ← Jersey servlet, url-pattern /api/v1/*
```
