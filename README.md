# 🗳️ VoteSecure — Online Voting System

A production-ready, secure online voting platform built with **Spring Boot 3**, **Spring Security**, **JWT authentication**, and an **H2/MySQL** database backend.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green?style=flat-square)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-blue?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-purple?style=flat-square)

---

## ✨ Features

### 🔐 Security
- **JWT Bearer Token** authentication (HS256)
- **BCrypt** password hashing (strength 12)
- **One-vote-per-user** enforced at DB level (unique constraint)
- **Vote receipt hashing** (SHA-256) for anonymous verification
- Role-based access control (`VOTER` / `ADMIN`)
- Global exception handler with clean error responses

### 🗳️ Voting
- Browse active elections and candidates
- Cast a secure, anonymised vote
- Receive a **SHA-256 receipt hash** after voting
- Verify your vote was recorded with the receipt hash
- Real-time live vote counts and results with percentages

### 🛡️ Admin Panel
- Full election lifecycle: `DRAFT → ACTIVE → CLOSED`
- **Auto-scheduler** activates/closes elections by date
- Add/remove candidates
- Manage users (enable, disable, delete)
- View live results with bar charts

---

## 🏗️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.2 |
| Security | Spring Security 6, JJWT 0.12 |
| Database | H2 (dev), MySQL 8 (prod) |
| ORM | Spring Data JPA / Hibernate |
| Validation | Jakarta Bean Validation |
| Frontend | Vanilla HTML/CSS/JS (no frameworks) |
| Build | Maven |

---

## 📁 Project Structure

```
online-voting-system/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/voting/
    │   │   ├── VotingApplication.java        # Entry point + admin seed
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java        # JWT filter chain, CORS
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   └── SchedulingConfig.java
    │   │   ├── controller/
    │   │   │   ├── AuthController.java        # /api/auth/*
    │   │   │   ├── ElectionController.java    # /api/elections/*
    │   │   │   ├── VoteController.java        # /api/votes/*
    │   │   │   └── AdminController.java       # /api/admin/*
    │   │   ├── model/
    │   │   │   ├── User.java
    │   │   │   ├── Election.java
    │   │   │   ├── Candidate.java
    │   │   │   ├── Vote.java                  # unique(voter+election)
    │   │   │   ├── Role.java
    │   │   │   └── ElectionStatus.java
    │   │   ├── repository/                    # JPA repositories
    │   │   ├── service/
    │   │   │   ├── UserService.java
    │   │   │   ├── ElectionService.java       # + @Scheduled auto-status
    │   │   │   ├── VoteService.java           # cast + receipt + results
    │   │   │   └── JwtService.java
    │   │   ├── security/
    │   │   │   ├── JwtAuthFilter.java
    │   │   │   └── UserDetailsServiceImpl.java
    │   │   └── dto/                           # Request/Response DTOs
    │   └── resources/
    │       ├── application.properties
    │       └── static/
    │           ├── index.html                 # Voter UI
    │           └── admin.html                 # Admin dashboard
    └── test/
        └── java/com/voting/
            └── VotingApplicationTests.java
```

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+

### Run (H2 in-memory — zero config)

```bash
git clone https://github.com/YOUR_USERNAME/online-voting-system.git
cd online-voting-system
mvn spring-boot:run
```

Open **http://localhost:8080**

### Default Admin Credentials
```
Username : admin
Password : Admin@123
```
> ⚠️ **Change these** in `application.properties` before deploying!

---

## 🗄️ Switch to MySQL (Production)

1. Create the database:
```sql
CREATE DATABASE votingdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'votinguser'@'localhost' IDENTIFIED BY 'strongpassword';
GRANT ALL PRIVILEGES ON votingdb.* TO 'votinguser'@'localhost';
```

2. Update `application.properties`:
```properties
# Comment out H2 lines, uncomment MySQL lines:
spring.datasource.url=jdbc:mysql://localhost:3306/votingdb?useSSL=true&serverTimezone=UTC
spring.datasource.username=votinguser
spring.datasource.password=strongpassword
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Disable H2 console
spring.h2.console.enabled=false
```

---

## 🔌 API Reference

### Auth
| Method | Endpoint | Body | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | `{username, email, password, fullName}` | Public |
| POST | `/api/auth/login` | `{username, password}` | Public |

### Elections (Voter)
| Method | Endpoint | Auth |
|---|---|---|
| GET | `/api/elections` | Optional |
| GET | `/api/elections/{id}` | Optional |
| GET | `/api/elections/{id}/results` | Optional |
| GET | `/api/elections/{id}/has-voted` | Required |

### Votes
| Method | Endpoint | Auth |
|---|---|---|
| POST | `/api/votes` | Required (Voter) |
| GET | `/api/votes/verify/{hash}` | Public |

### Admin (requires `ROLE_ADMIN`)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/elections` | All elections |
| POST | `/api/admin/elections` | Create election |
| PUT | `/api/admin/elections/{id}` | Update election |
| DELETE | `/api/admin/elections/{id}` | Delete election |
| PATCH | `/api/admin/elections/{id}/activate` | Activate |
| PATCH | `/api/admin/elections/{id}/close` | Close |
| POST | `/api/admin/elections/{id}/candidates` | Add candidate |
| DELETE | `/api/admin/candidates/{id}` | Remove candidate |
| GET | `/api/admin/users` | All users |
| PATCH | `/api/admin/users/{id}/toggle` | Enable/disable |
| DELETE | `/api/admin/users/{id}` | Delete user |

---

## 🔒 Security Architecture

```
Request → JwtAuthFilter (validates Bearer token)
        → SecurityConfig (checks roles for endpoint)
        → Controller → Service → Repository
        
Vote Integrity:
  - DB unique constraint: (voter_id, election_id) — prevents double voting
  - SHA-256 receipt: generated from userId + electionId + candidateId + timestamp
  - Votes are immutable — no update/delete on Vote entity
```

---

## 🧪 Run Tests

```bash
mvn test
```

---

## 📸 Screenshots

| Page | URL |
|---|---|
| Voter UI | http://localhost:8080 |
| Admin Panel | http://localhost:8080/admin.html |
| H2 Console | http://localhost:8080/h2-console |

---

## 📄 License

MIT License — free to use, modify, and distribute.

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit changes: `git commit -m 'Add my feature'`
4. Push: `git push origin feature/my-feature`
5. Open a Pull Request
