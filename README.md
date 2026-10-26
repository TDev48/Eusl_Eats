# EUSL Eats

Campus food ordering for **Eastern University, Sri Lanka**: students browse approved food stores, build a
cart and place orders; store owners manage their menu and orders; an administrator approves new stores.

| Folder | What it is |
|---|---|
| `SmartCafeteria/` | The website (HTML, CSS, JavaScript) |
| `eusleats/eusl-eats/` | The back end (Java 25, Spring Boot, JPA) |
| `database/` | Database notes (tables are created automatically) |
| `deploy/` | One-command setup with Docker, and a guide to put it online |

## Features
- **Students:** browse stores and menus, search and filter food, cart, place and cancel orders, track order progress, notifications.
- **Stores:** manage menu (photos, sizes, availability), receive orders, update status, notifications, SMS alert for new orders.
- **Admin:** approve, reject or revoke stores.
- Server-side login tokens and permission checks (a student only sees their own orders; a store only its own).

## See the design (no install)
The website folder is published automatically with GitHub Pages: open the **Pages link** shown in the
repository's *Settings -> Pages*. It is a **design preview only**: there is no back end there, so login and
orders do not work. To use the real thing, run it on your own computer (below).

## Run it on your own computer

### Option A - Docker (easiest, works on Windows, Mac and Linux)
1. Install **Docker Desktop** and **Git**.
2. Open a terminal and run:
   ```
   git clone https://github.com/YOUR-NAME/eusl-eats.git
   cd eusl-eats/deploy
   copy .env.example .env          (Mac/Linux: cp .env.example .env)
   ```
3. Open `.env` in a text editor and change the passwords.
   If XAMPP (or anything else) already uses port 80 on your computer, also set `HTTP_PORT=8081` and `HTTPS_PORT=8443`.
4. Start it (the first start takes a few minutes):
   ```
   docker compose up --build
   ```
5. Open **http://localhost** (or **http://localhost:8081**).
   Log in as admin at `/admin-login.html` with the `ADMIN_EMAIL` and `ADMIN_PASSWORD` from your `.env`.
6. Stop with `Ctrl + C`. Your data is kept for next time.

### Option B - without Docker
You need **JDK 25**, **XAMPP** (Apache + MySQL/MariaDB) and **VS Code** with the *Live Server* extension.
1. Start MySQL in XAMPP and create an empty database named `smart_cafeteria` in phpMyAdmin.
2. Start the back end (the first start creates the tables):
   ```
   cd eusleats/eusl-eats
   set DDL_AUTO=update                         (PowerShell: $env:DDL_AUTO="update")
   mvnw.cmd spring-boot:run                    (Mac/Linux: DDL_AUTO=update ./mvnw spring-boot:run)
   ```
3. Open `SmartCafeteria/index.html` with Live Server (port 5500). The site finds the back end on `localhost:8080` by itself.
4. Admin login: `admin@eusl.lk` / `Admin@12345` (default for local use only - change it before sharing a live site).

### Try it out
1. Register a **store** (`store-register.html`) and a **student** (`register.html`).
2. Log in as **admin** and approve the store.
3. Log in as the store, add food items.
4. Log in as the student, add items to the cart and place an order; the store sees it and can change its status.

## Put it online
See [`deploy/DEPLOY.md`](deploy/DEPLOY.md) (free options, HTTPS, backups).

## Settings (environment variables)
`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `APP_SECRET`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `DDL_AUTO`,
`UPLOAD_DIR`, `SMS_PROVIDER` (`log`, `notifylk` or `twilio`) and the matching SMS keys.
The defaults in `application.properties` are for local testing only.

## Notes
- No database data is stored in this repository (it would contain emails, phone numbers and password hashes).
- Text messages are only written to the log unless an SMS provider is configured.
