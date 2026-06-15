# SI-BLO — Pro Court Rentals

> A sports court rental platform built with Spring Boot. Users can browse sports, book courts, manage bookings, and admins can manage the system.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 21+ |
| **Framework** | Spring Boot 3.4.1 |
| **Database** | H2 (in-memory, no server needed) |
| **ORM** | Spring Data JPA + Hibernate |
| **Frontend** | Thymeleaf + HTML/CSS (served by Spring) |
| **Security** | Spring Security + JWT (JJWT 0.12.6) |
| **Build Tool** | Maven (via Maven Wrapper `mvnw`) |
| **CSS** | Custom (`static/css/siblo.css`) |

---

## Project Structure

```
siblo/
├── mvnw / mvnw.cmd          Maven wrapper (build tool — no Maven install needed)
├── pom.xml                   Maven project config (dependencies, build plugins)
├── db_siblo.sql              Database schema reference (PostgreSQL syntax)
├── HELP.md                  Spring Boot generated help file
├── Prompt.md                Original project prompt
│
└── src/main/
    ├── java/com/siblo/rent/
    │   ├── RentApplication.java              Entry point (@SpringBootApplication)
    │   │
    │   ├── config/
    │   │   ├── SecurityConfig.java            Spring Security config (JWT filter, route rules)
    │   │   └── DataSeeder.java               Seeds sample data on startup (users, sports, courts, slots)
    │   │
    │   ├── controller/                        REST + Page controllers
    │   │   ├── PageController.java           Thymeleaf page routes (/, /booking, /my-bookings, /login, /manage-admin)
    │   │   ├── AuthController.java           POST /api/auth/login, POST /api/auth/register
    │   │   ├── CourtController.java          GET /api/courts, /api/courts/{id}, /api/courts/{id}/availability
    │   │   ├── SportController.java          GET /api/sports, /api/sports/{id}
    │   │   ├── BookingController.java        GET/POST/PATCH /api/bookings/*
    │   │   └── AdminController.java          Admin CRUD: stats, courts, timeline
    │   │
    │   ├── dto/                               Data Transfer Objects (API request/response shapes)
    │   │   ├── SportDTO.java                 Sport response (id, name, slug, icon, locationCount, imageUrl)
    │   │   ├── CourtDTO.java                 Court response (includes sportName, venueName, venueZone)
    │   │   ├── CourtRequest.java             Court create/update request body
    │   │   ├── TimeSlotDTO.java              Time slot response (id, startTime, endTime, status)
    │   │   ├── BookingDTO.java               Booking response (includes user/court details)
    │   │   ├── BookingRequest.java           Booking create request (courtId, slotIds, date)
    │   │   └── AdminStatsDTO.java            Dashboard stats (revenue, bookings, capacity, slots)
    │   │
    │   ├── entity/                            JPA entities (database tables)
    │   │   ├── User.java                     users table — name, email, password, role, membershipTier
    │   │   ├── Sport.java                    sports table — name, slug, icon, locationCount
    │   │   ├── Venue.java                    venues table — name, address, zone
    │   │   ├── Court.java                    courts table — venue, sport, price, capacity, status, rating
    │   │   ├── TimeSlot.java                 time_slots table — court, date, start/end time, status, version (optimistic lock)
    │   │   └── Booking.java                  bookings table — user, court, date, time, totalPrice, status
    │   │
    │   ├── repository/                       Spring Data JPA repositories (database queries)
    │   │   ├── UserRepository.java           findByEmail, existsByEmail
    │   │   ├── SportRepository.java          findBySlug
    │   │   ├── VenueRepository.java          Default CRUD only
    │   │   ├── CourtRepository.java          findBySportId, searchCourts, findActiveCourts, countByStatus
    │   │   ├── TimeSlotRepository.java       findByCourtIdAndDate, countCourtsWithAvailableSlots
    │   │   └── BookingRepository.java        findByUserId, findByCourtIdAndDate, sumRevenueByDate
    │   │
    │   ├── security/                          JWT authentication layer
    │   │   ├── JwtTokenProvider.java          Generate/validate/parse JWT tokens
    │   │   ├── JwtAuthenticationFilter.java   Extract JWT from Authorization header, authenticate requests
    │   │   └── CustomUserDetailsService.java  Load user from DB by email for Spring Security
    │   │
    │   └── service/                           Business logic layer
    │       ├── SportService.java              List sports, get by ID
    │       ├── CourtService.java              Active courts, search, availability, CRUD for admin
    │       ├── BookingService.java            Create/cancel/pay bookings, user bookings, timeline
    │       └── AdminService.java              Dashboard statistics aggregation
    │
    └── resources/
        ├── application.properties            All config (DB, JPA, JWT, Thymeleaf)
        ├── static/
        │   └── css/siblo.css                 Complete custom CSS (dark theme UI)
        └── templates/
            ├── home.html                     Landing page (hero, sport cards, court grid, stats)
            ├── booking.html                  Court detail + date/time slot picker + checkout
            ├── my-bookings.html              Upcoming/past bookings list
            ├── login.html                    Login form with demo credentials
            ├── manage-admin.html             Admin dashboard (stats, court CRUD, timeline)
            └── fragments/                    Reusable Thymeleaf components
                ├── head.html                 <head> fragment (meta, title, CSS)
                ├── sidebar.html              Desktop sidebar navigation
                ├── topbar.html               Top bar with search + quick book
                └── bottomnav.html            Mobile bottom navigation bar
```

---

## Prerequisites

| Requirement | Version |
|------------|---------|
| **Java JDK** | 21 or higher |
| **JAVA_HOME** | Must point to JDK 21+ |
| **Git** | Any recent version |

Check your Java version:

```bash
java -version
# Must show version 21 or higher
```

---

## How to Clone & Run (Your Machine)

### 1. Clone the repository

```bash
git clone https://github.com/MKhansa067/Si-BLO.git
cd Si-BLO
```

### 2. Run the app

```bash
.\mvnw spring-boot:run
```

> **First run is slow** — Maven downloads all dependencies. Wait until you see:
> ```
> Started RentApplication in ... seconds
> ```

### 3. Open in browser

- **App:** http://localhost:8080
- **H2 Console (DB):** http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:siblodb`
  - User: `sa`
  - Password: *(leave blank)*

### 4. Stop the app

Press **`Ctrl + C`** in the terminal.

---

## Demo Accounts

These accounts are **auto-seeded** every time the app starts:

| Role | Email | Password |
|------|-------|----------|
| **Admin** | admin@siblo.com | admin123 |
| **Member** | john@siblo.com | john123 |

---

## How to Push Code (For Team Members)

### First-time setup

```bash
# 1. Clone the repository
git clone https://github.com/MKhansa067/Si-BLO.git
cd Si-BLO

# 2. Create your feature branch
git checkout -b your-feature-name
```

### After making changes

```bash
# 1. Check what changed
git status

# 2. Stage the files you want to commit
git add .

# 3. Commit with a meaningful message
git commit -m "brief description of your changes"

# 4. Push to GitHub (first time for your branch)
git push -u origin your-feature-name

# Subsequent pushes on the same branch
git push
```

### If you get a "fetch first" error

Someone pushed changes before you. Update your local repo:

```bash
git pull --rebase origin master
git push -u origin your-feature-name
```

### Working with the main branch

```bash
# Switch to master and get latest
git checkout master
git pull origin master

# Merge your feature into master (then push)
git merge your-feature-name
git push origin master
```

> **Note:** Push will ask for your GitHub credentials.
> - **Password field** → use a **Personal Access Token (PAT)**
> - Create one at: https://github.com/settings/tokens
> - Scope: `repo` (full control)

---

## Branch Management Workflow

This repository uses three main branches:

| Branch | Purpose |
|----------|----------|
| `master` | Active development branch |
| `main` | Stable production branch |
| `v1.0` | Version 1.0 release snapshot |

### Update your local branch before working

Always get the latest changes before starting:

```bash
git checkout master
git pull origin master
```

### Push changes to the development branch (`master`)

```bash
git checkout master

git add .
git commit -m "describe your changes"

git push origin master
```

### Update GitHub `master` with your local `master`

If your local branch contains the latest changes and you want GitHub's `master` branch to match it:

```bash
git checkout master
git push origin master
```

If GitHub contains commits that are not needed and you intentionally want to overwrite the remote branch:

```bash
git push origin master --force
```

> Warning: `--force` replaces the remote branch history and should only be used by repository maintainers.

### Merge `master` into `main` (Production Release)

After testing is complete:

```bash
git checkout main
git pull origin main

git merge master

git push origin main
```

This makes the production branch contain all approved changes from development.

### Create or Update the `v1.0` Release Branch

When preparing a version release:

```bash
git checkout v1.0
git pull origin v1.0

git merge main

git push origin v1.0
```

### View All Branches

```bash
git branch
```

Local and remote branches:

```bash
git branch -a
```

### Typical Workflow

```text
Developer Work
      │
      ▼
   master
      │
      ▼
    main
      │
      ▼
    v1.0
```

- Develop and test on `master`
- Merge stable features into `main`
- Create release snapshots in `v1.0`

---

## Key Config (`application.properties`)

| Property | Value | Notes |
|----------|-------|-------|
| `spring.datasource.url` | `jdbc:h2:mem:siblodb` | In-memory DB (data lost on restart) |
| `spring.jpa.hibernate.ddl-auto` | `create-drop` | Auto-creates tables from entities |
| `spring.h2.console.enabled` | `true` | H2 web console at `/h2-console` |
| `app.jwt.secret` | *(256-bit key)* | JWT signing key |
| `app.jwt.expiration-ms` | `86400000` | Token expires in 24 hours |

---

## API Endpoints

### Public

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/login` | Login with email + password → returns JWT |
| POST | `/api/auth/register` | Register new user |
| GET | `/api/sports` | List all sports |
| GET | `/api/sports/{id}` | Get sport by ID |
| GET | `/api/courts` | List active courts (filter by `?sport=id` or `?search=query`) |
| GET | `/api/courts/count` | Available courts count |
| GET | `/api/courts/{id}` | Get court by ID |
| GET | `/api/courts/{id}/availability` | Time slots for a court (`?date=YYYY-MM-DD`) |

### Authenticated (requires `Authorization: Bearer <token>` header)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/bookings/me` | Current user's bookings (`?upcoming=true`) |
| POST | `/api/bookings` | Create a booking |
| PATCH | `/api/bookings/{id}` | Cancel a booking |
| POST | `/api/bookings/{id}/pay` | Pay for a booking |

### Admin (requires admin JWT)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/admin/stats/dashboard` | Dashboard statistics |
| GET | `/api/admin/courts` | All courts (admin view) |
| POST | `/api/admin/courts` | Add a court |
| PUT | `/api/admin/courts/{id}` | Update a court |
| PATCH | `/api/admin/courts/{id}/availability` | Toggle court active/inactive |
| DELETE | `/api/admin/courts/{id}` | Delete a court |
| GET | `/api/admin/bookings/timeline` | Today's booking timeline |

---

## Features

- **Role-based access** — Member vs Admin
- **JWT Authentication** — login, register, token-based API security
- **Smart booking** — select date, pick consecutive time slots, checkout
- **Admin dashboard** — revenue stats, court CRUD, booking timeline
- **Responsive UI** — desktop sidebar + mobile bottom nav
- **Dark theme** — Custom CSS design system with CSS variables
- **Auto seed data** — 2 users, 5 sports, 4 venues, 8 courts, time slots for 7 days, sample bookings
- **H2 in-memory** — Zero database setup needed
