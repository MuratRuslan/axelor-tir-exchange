# TIR Exchange (Axelor Open Platform port)

This is the [Axelor Open Platform](https://github.com/axelor/axelor-open-platform)
(AOP) 7.4 port of the original Spring Boot `tir-exchange` service. It keeps the
same TIR-EPD message exchange behaviour and the same REST surface, but rebuilt on
AOP: entities come from generated **domain XML**, dependency injection is **Guice**
instead of Spring, and the REST API is a **JAX-RS** resource auto-discovered by AOP.

The original Spring Boot project is untouched in the parent directory; this is a
parallel implementation under `axelor-tirexchange/`.

## Project layout

```
axelor-tirexchange/
├── settings.gradle                 # AOP plugin + module include
├── build.gradle                    # application (set of modules), Java 17 toolchain
├── src/main/resources/
│   └── axelor-config.properties    # DB / app config (H2 in-memory by default)
└── modules/tir-exchange/           # the feature module
    ├── build.gradle
    └── src/main/
        ├── java/kg/itg/tirexchange/
        │   ├── TirExchangeModule.java          # Guice bindings (was Spring scanning)
        │   ├── db/repo/TirMessageRepository.java
        │   ├── service/…                       # service + EPD processors + XML mapper
        │   ├── dto/…                           # Jackson XML DTOs (ported 1:1)
        │   ├── mapper/…                        # MapStruct mappers
        │   └── web/TirExchangeResource.java    # JAX-RS endpoints
        └── resources/
            ├── domains/TirMessage.xml          # generates the JPA entity + abstract repo
            └── views/                          # grid/form + menu for the Axelor UI
```

## What changed vs. Spring Boot

| Concern            | Spring Boot                              | Axelor Open Platform                                            |
|--------------------|------------------------------------------|----------------------------------------------------------------|
| Entity             | `@Entity TirMessage` (hand-written)      | `domains/TirMessage.xml` → generated `kg.itg.tirexchange.db.TirMessage` |
| Repository         | `JpaRepository` derived queries          | `TirMessageRepository extends AbstractTirMessageRepository` with `Query` calls |
| DI                 | Spring `@Service` / `@Autowired`         | Guice `@Inject`, bindings in `TirExchangeModule`               |
| REST               | `@RestController` under `/api`           | JAX-RS `@Path` resource under `/ws`                            |
| Error handling     | `@RestControllerAdvice` SOAP fault       | try/catch in the resource returns the same SOAP fault XML      |
| ObjectMapper (XML) | `XmlConfig` `@Bean`                       | `TirXmlMapper` (self-configuring, Guice-injectable)            |
| Pagination         | Spring `Page`/`Pageable`                  | `PageResponse<T>` (same JSON field names)                      |
| Build/run          | `bootRun`, fat jar                        | AOP Gradle plugin, deployable `.war`                          |

The EPD processing rules, validation, statuses and XML request/response shapes are
unchanged.

## REST API

AOP mounts JAX-RS under `/ws`, so the **only** path change is the `/api` → `/ws`
prefix:

| Original (Spring)                         | Axelor                                    |
|-------------------------------------------|-------------------------------------------|
| `POST /api/tir/exchange` (XML in/out)     | `POST /ws/tir/exchange` (XML in/out)      |
| `GET  /api/tir/messages`                  | `GET  /ws/tir/messages`                   |
| `GET  /api/tir/messages/{id}`             | `GET  /ws/tir/messages/{id}`              |
| `GET  /api/tir/messages/type/{type}`      | `GET  /ws/tir/messages/type/{type}`       |
| `GET  /api/tir/messages/guarantee/{gn}`   | `GET  /ws/tir/messages/guarantee/{gn}`    |

**Authentication:** AOP secures `/ws/*` with Shiro, so requests must be
authenticated. From Postman, log in first (session cookie) or send HTTP Basic
credentials of a valid AOP user (default dev admin is `admin` / `admin`).

Example:

```bash
curl -u admin:admin -X POST http://localhost:8080/ws/tir/exchange \
  -H 'Content-Type: application/xml' \
  --data '<EPD015><GuaranteeNumber>KG12345678</GuaranteeNumber><HolderNumber>TIRH-1</HolderNumber></EPD015>'
```

## Database

AOP requires **PostgreSQL**. Unlike the original Spring Boot service (which used
in-memory H2), an embedded database is not viable here: AOP runs its metadata
install as 8 parallel write transactions, and embedded HSQLDB/H2 deadlock under
that concurrency. PostgreSQL handles it natively.

A `docker-compose.yml` is provided that spins up PostgreSQL pre-configured with
the right database, user and password (matching `axelor-config.properties`:
`tirexchange` / `axelor` / `axelor` on `localhost:5432`).

```bash
docker compose up -d        # starts postgres on localhost:5432
# docker compose down       # stop;  add -v to also delete the data volume
```

Adjust `db.default.url` / `user` / `password` in
`src/main/resources/axelor-config.properties` (and the matching values in
`docker-compose.yml`) if you need different credentials.

## Build & run

Requires JDK 17 (AOP 7.4 supports JDK 11+). The Axelor plugin and framework
artifacts are pulled from `https://repository.axelor.com`, so the first build
needs network access to that repository.

```bash
cd axelor-tirexchange
./gradlew build          # compiles, generates entities/views, runs tests, builds the WAR
./gradlew run            # runs the app with the embedded server (Tomcat)
```

The first start runs the AOP metadata install (a minute or two), then the web UI
(with a **TIR Exchange → Messages** menu over the saved records) is available at
`http://localhost:8080/axelor-tirexchange` — log in with `admin` / `admin`.
Subsequent starts are fast because the metadata persists in PostgreSQL.

## API docs (Swagger UI / OpenAPI)

A self-contained Swagger UI is bundled under
`modules/tir-exchange/src/main/webapp/swagger-ui/`:

- `index.html` — loads `swagger-ui-dist` from a CDN
- `openapi.json` — a hand-authored OpenAPI 3.0 spec with the request/response
  **models and example payloads**

It is hand-authored on purpose: AOP 7.4's built-in OpenAPI scanner only emits
operation summaries/paths, not response/request schemas or examples (its own
resources don't use them either), so the generated `/ws/openapi.json` would show
empty bodies. The bundled spec gives a complete, browsable API.

- Swagger UI: `http://localhost:8080/axelor-tirexchange/swagger-ui/`

Because AOP secures `/ws/*` and serves the page behind login, **log in to the app
first** (`admin`/`admin`), then open the Swagger UI, click **Authorize** and enter
`admin`/`admin` (HTTP Basic). That makes **Try it out** work — including the POST
endpoint — without a session cookie or CSRF token (stateless basic auth is
enabled via `auth.local.basic-auth = direct`).
