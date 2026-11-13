# Madrid Help

Web app that helps people experiencing homelessness in Madrid find food, shelter, showers and clothing that are **open right now**. No sign-up required.

> **About this project:** it is 100% AI-generated (design decisions, code and tests were written by Claude). The author drove the process as Product Owner: defined the problem, prioritised the backlog, made the design decisions, and reviewed and ran every piece. It is part of a portfolio that separates projects by how much of the work was done by hand and how much with AI.

## Status

Work in progress, developed in Scrum sprints.

| Sprint | Goal | State |
|---|---|---|
| 1 | Backend API: list places, filter by type, "open now", "next to open" | Done |
| 2 | Overnight opening hours + React frontend with a map | In progress (overnight hours done) |
| 3 | Optional sign-up for organisations to publish and update their own data | Planned |

All data currently in the project is **fictional** (every name starts with `TEST`) and must not be used by real people.

## Why

People in a street situation need fast, reliable information, but it is spread across many websites, often outdated, and usually requires a stable connection or an account. Design principles:

- **No registration** for the person looking for help.
- **Few taps:** open the app and see what is closest and open.
- **Light and readable:** low data and battery use, large buttons, high contrast (WCAG AA is the target).
- **Privacy:** the user's location is never stored or sent to the server.
- **Reliable data:** every place shows when it was last updated.

## Tech stack

- Java 21, Spring Boot 3.5 (Web, Data JPA, Validation)
- H2 in-memory database for development (PostgreSQL or MySQL planned)
- JUnit 5 and MockMvc for tests
- React frontend (planned)

## Run it

Requirements: JDK 21 and Maven.

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080` with the `dev` profile, which loads six fictional places. To inspect the database, open `http://localhost:8080/h2-console` with JDBC URL `jdbc:h2:mem:madridhelp`, user `sa` and an empty password.

Run the tests:

```bash
mvn test
```

## API

| Endpoint | Description |
|---|---|
| `GET /api/places` | All places |
| `GET /api/places?type=SOUP_KITCHEN` | Filter by type: `SOUP_KITCHEN`, `SHELTER`, `SHOWER`, `CLOTHING` |
| `GET /api/places?openNow=true` | Only places open right now (Madrid time) |
| `GET /api/places/next-opening` | The place that opens soonest, and when (optional `type`) |

`next-opening` is a separate endpoint on purpose: `openNow=true` must never return a closed place.

## Design decisions

- **Opening hours are rows, not columns:** one `Schedule` row per day and time range. Two shifts in one day are two rows.
- **Overnight and 24-hour slots:** if `closesAt` is not after `opensAt`, the slot crosses midnight and belongs to the day it starts. `opensAt` equal to `closesAt` means exactly 24 hours (store `00:00 -> 00:00` for a place open all day). Do not use `00:00 -> 23:59`: closing is exclusive, so it leaves a one-minute gap.- **Opening is inclusive, closing is exclusive:** a 12:00-15:00 slot is open at 12:00 and closed at 15:00.
- **Injected `Clock` in Madrid time:** servers usually run in UTC, and tests can freeze the time.
- **DTOs are records, entities are classes:** the API does not expose the database model.
- **Fictional seed data only under the `dev` profile.**

## Project structure

```
src/main/java/com/madridhelp
  controller/   HTTP endpoints
  service/      business logic and opening rules
  repository/   data access
  model/        JPA entities
  dto/          API responses
  config/       clock and development data
```

## Process

Backlog, MoSCoW prioritisation, acceptance criteria and sprint retrospectives are part of the project. See the case study (coming soon).
