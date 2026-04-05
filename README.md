# Chatflow - Real-time Chat Application

Modern Spring Boot chat app with WebSocket messaging, JWT auth, contacts/contacts sync, group chats, and responsive UI.

## 🌟 Features
- Real-time 1:1 & group chat (WebSockets)
- JWT authentication + OTP verification (Redis)
- Contact requests/sync (Google People API)
- Public static pages (home, about, features, help)
- Glassmorphism UI (Tailwind, dark/light theme)
- Responsive (desktop/mobile)
- MySQL persistence + Redis caching

## 🛠 Tech Stack
- **Backend**: Spring Boot 3+, JPA/MySQL, WebSocket/STOMP, Redis, JWT
- **Frontend**: HTML/JS/Tailwind CSS, Material Symbols
- **Build**: Maven

## 🚀 Quick Start
```bash
mvn clean install
mvn spring-boot:run
```
Open http://localhost:8080/home.html

**Env Vars**:
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (MySQL)
- `JWT_SECRET` (base64-encoded 32-byte secret; generate with `openssl rand -base64 32` or equivalent)
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_USERNAME`, `REDIS_PASSWORD` (optional; defaults to `localhost:6379`)
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`
- `MAIL_SMTP_AUTH`, `MAIL_SMTP_STARTTLS_ENABLE`, `MAIL_SMTP_STARTTLS_REQUIRED`
- `MAIL_FROM`, `MAIL_FROM_NAME`
- Redis/Google creds optional

## Authentication Flow
1. Register/Login with email/password
2. OTP verification (email/Redis TTL)
3. JWT token stored in localStorage

## Key APIs
- `/api/auth/**`: Auth steps
- `/api/chats/**`: List/create/send (private/group)
- `/api/contacts/**`: Manage requests/sync
- `/ws`: WebSocket

## Redis Usage
- OTP storage/rate-limiting (`OtpService`)
- Google contacts cache 1hr (`GooglePeopleService`)

## Project Structure
```
src/main/java/com/chatflow/
├── ChatflowApplication.java
├── config/ (SecurityConfig, RedisConfig, WebSocketConfig)
├── controller/ (AuthController, ChatController, ContactController...)
├── dto/, model/, repository/, service/
└── websocket/ChatWebSocketHandler.java

src/main/resources/
├── application.yml
└── static/ (home.html, chat.html, chat.js...)
```

## Testing
- 403 fix: Public pages permitAll in SecurityConfig
- Group create: Chats tab → "Create New Group" → Launch
- Run `mvn test`

## Future
- File upload/encryption
- Push notifications
- Typing indicators (partial)

MIT License. Enjoy!
