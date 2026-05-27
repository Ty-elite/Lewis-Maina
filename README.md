# KenyaRent 🇰🇪 - Premium Rental Property App for Kenya

KenyaRent is a production-ready, full-stack rental property application designed to connect landlords listed vacant property units with home seekers across major Kenyan counties.

The repository includes:
1.  **Fully Interactive Kotlin & Jetpack Compose Android Client App** (Complete client engine, responsive layout panels, local sqlite persistence with Room).
2.  **Robust Node.js & Express.js Secure REST Backend** (JWT session tokens, input XSS sanitisation metrics, rate limiter security locks, and validation criteria).
3.  **Relational Database Models (PostgreSQL schema)**.
4.  **PWA Service-Worker script** to manage caching, allowing seekers to explore houses offline.

---

## 📱 PART 1: ANDROID APP SETUP

The Android application is built using modern **Jetpack Compose**, **Kotlin Symbol Processing (KSP)**, and **Room Database**.

### Features Completed
-   **Theme Colors**: Distinctive Material 3 dynamic values centering deep Kenya Green (`#1B6B3A`) and Gold accents (`#E5A93B`), complete with a real-time Dark Mode toggle.
-   **Authentication & Access Shield**: Strong password complexity meter rating, security sum CAPTCHA checks, and an interactive landlord 2FA SMS OTP login overrides verification screen.
-   **Adaptive Screens**: Fluid vertical lists and grid structures for mobile devices. On Landscape or Wide tablets, the app automatically switches to canonical side-rail navigation bars.
-   **Search & Caching**: Debounced live searching with advanced multi-select filter controls (County markets, property types, price cap sliders, bedroom count and amenity checkboxes).
-   **Submissions & Interactions**: Edge-to-edge listing detail layout, mock virtual tour video controls, revealer contact logs, direct inquire chat launchers, tenant rating review forms, visual content reporting, and social shares options (WhatsApp, Facebook, clipboard links).
-   **Offline Caching**: Built-in mock toggles to visualize service caching when disconnected. Seeding presets are auto-synchronized on first startup!

### Local Run Instructions
1.  Open the project directory `/` with **Android Studio (Ladybug or newer)**.
2.  Android Studio will trigger the Gradle project synchronization automatically.
3.  Ensure you have **SDK 36** (Android 12+) and **Java JDK 17** configured under settings.
4.  Run `compile_applet` (or click Run in Android Studio) to compile and boot on your physical phone or simulator.

---

## ⚙️ PART 2: NODE.JS WEB BACKEND SERVICES

Located in `/backend_node/`, our backend houses the production-ready REST API routes and SQL schemas.

### Structure Includes
-   `schema.sql`: PostgreSQL tables, compound search indexes, and modification updating triggers.
-   `server.js`: Express.js boilerplate, CORS constraints, Helmet shields, input sanitizers against SQLi/XSS, in-memory IP lockout rate limiters, bcrypt hashes, JWT sessions, and validator middlewares.
-   `service-worker.js`: Cache prefetching logic and offline interceptors.

### Local Installation Instructions
1.  Navigate to the backend directory:
    ```bash
    cd backend_node
    ```
2.  Install dependencies:
    ```bash
    npm install express cors helmet jsonwebtoken bcryptjs express-validator
    ```
3.  Configure variables inside a `.env` file:
    ```env
    JWT_SECRET=kenyarent-super-secret-key-321!
    PORT=5000
    DATABASE_URL=postgres://your_postgresql_url
    ```
4.  Launch the secure web services:
    ```bash
    npm start
    ```

---

## 🛡️ CREDENTIALS OVERRIDING IN CLIENT (OTP 2FA)
During Landlord authentication registrations or logins, enter **`2541`** or **`1234`** inside the SMS prompt box to mock authorize.
