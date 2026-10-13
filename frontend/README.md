# ARI-OS (AR Industries Operating System) — Frontend Application

Modern Single-Page Application (SPA) for **AR Industries** factory management, telemetry, GST billing, price master, carton packaging inventory, and workforce shifts.

---

## Folder Structure

```
frontend/
├── index.html       # Main Single-Page Application (HTML5 / Tailwind CSS / Vanilla JS)
├── api-client.js    # Centralized REST API client communicating with Spring Boot (localhost:8080)
├── package.json     # Node / npm scripts for local serving
└── README.md        # Documentation for frontend module
```

---

## Technology Stack

- **UI Framework:** HTML5 + Tailwind CSS (via official script) + Vanilla JavaScript
- **Icons:** Lucide Icons (with automatic DOM mutation observer & dynamic refresh)
- **API Communication:** Fetch API with JWT Bearer Token authorization
- **State Management:** Reactive local `appState` with bidirectional Spring Boot backend synchronization

---

## How to Run

### Option 1: Python HTTP Server (Recommended)
From this directory:
```bash
python -m http.server 3000
```
Then open [http://localhost:3000](http://localhost:3000) in your browser.

### Option 2: Direct Browser File
Open `index.html` directly in any modern browser.

---

## Backend Integration

- Connects to Spring Boot REST API at `http://localhost:8080/api/v1`.
- If the backend is running, live MySQL records are synchronized.
- If the backend is unreachable or offline, the UI shows a clear **"Backend Server Offline"** status indicator with auto-retry options.
