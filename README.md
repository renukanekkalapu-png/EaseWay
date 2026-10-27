# EaseWay — Personal Accessibility Assistant
### Aligned with RV College of Engineering (RVCE), Mysuru Road Campus, Bengaluru

**EaseWay** is a menu-driven, console-based Java application that recommends accessible routes and campus facilities tailored to an individual's personal accessibility preferences.

The application supports **multi-user accounts** — each user registers once, logs in on every visit, and maintains a fully independent profile with their own accessibility preferences and settings. User data is persisted to disk in separate files so one user's profile never affects another.

The architecture is **beginner-friendly and compact (5 core source files + 1 test file in a single package)**, making it easy to read, modify, and demonstrate during a lab viva or practical.

---

## 1. Project Directory Structure

```text
EaseWay/
├── src/
│   └── com/
│       └── easeway/
│           ├── Main.java                        # Console UI, auth flow (login/register/logout), & entry point
│           ├── UserProfile.java                 # Per-user profile, credentials, preferences & file persistence
│           ├── CampusModel.java                 # Domain models (Status, Category, Location, Route, PathResult)
│           ├── CampusMap.java                   # RVCE campus graph with 20 locations & authentic routes
│           └── RouteEngine.java                 # Cycle-safe DFS routing, cost scorer, & facility recommender
├── test/
│   └── com/
│       └── easeway/
│           └── EaseWayTest.java                 # Standalone automated test suite
├── bin/                                         # Compiled bytecode (.class files)
├── users/                                       # Per-user profile files (auto-created on first registration)
│   └── <username>.properties                    # One file per registered user
├── run.bat                                      # 1-click compile and launch for Windows
├── run.sh                                       # 1-click compile and launch for Linux/macOS
└── README.md                                    # Complete system documentation
```

---

## 2. Multi-User Account System

EaseWay supports multiple independent user accounts. Each user's accessibility preferences and profile details are stored in a separate file under the `users/` directory and never interfere with other users.

### Authentication Flow

On startup, users are presented with an authentication menu:

```
========== EASEWAY — USER AUTHENTICATION ==========
  1. Login with existing account
  2. Register a new account
  3. Continue as Guest (no profile saved)
  4. Exit EaseWay
===================================================
```

| Option | Behaviour |
| :--- | :--- |
| **Login** | Lists all registered accounts; verifies username & password before granting access |
| **Register** | Validates username uniqueness (alphanumeric + underscore), enforces a 4-character minimum password with confirmation, and collects profile details |
| **Guest Mode** | Creates a temporary in-memory profile; no data is saved to disk |
| **Logout** | Main Menu Option 8 — returns to the auth menu so another user can log in |

### Per-User Storage

Each registered user's profile is saved as:
```
users/<username>.properties
```

The file stores credentials (`auth.username`, `auth.password`) alongside personal details and all accessibility preferences. Profiles are completely isolated — changing one user's settings has zero effect on any other user.

> **Note:** Passwords are stored in plain text, which is appropriate for a console demonstration application.

---

## 3. RV College of Engineering (RVCE) Campus Locations

The application features **20 authentic RVCE campus locations** connected by a realistic route graph:

| # | Location | Category | Lift | Ramp | Access Notes |
| :---: | :--- | :--- | :---: | :---: | :--- |
| 1 | **Cauvery Block** | RESIDENTIAL | Yes | Yes | Wheelchair ramp at main entrance; lift to upper floors |
| 2 | **Computer Science and Engineering** | ACADEMIC | Yes | Yes | Ground-floor ramp; elevators in lobby; skywalk to Library |
| 3 | **RVCE Mingos Cafe Main Canteen** | DINING | No | Yes | Step-free ground-level dining; wide entry, low counters |
| 4 | **RVCE Vidyanikethan Main Gate** | ENTRANCE | No | Yes | Level plaza; wide automated gate; tactile paving to blocks |
| 5 | **EEE Department** | ACADEMIC | Yes | Yes | North-side ramp; elevator access to all lab floors |
| 6 | **Department of AIML Departmental Library** | LIBRARY | No | Yes | Ground-floor accessible; wide aisles throughout |
| 7 | **IEM Auditorium** | AUDITORIUM | Yes | Yes | Ramp at entrance; dedicated accessible seating in front rows |
| 8 | **Electronics and Communication Department** | ACADEMIC | Yes | Yes | North ramped entry; lift to 1st, 2nd & 3rd-floor labs |
| 9 | **RV University** | ADMINISTRATIVE | Yes | Yes | Front ramp (1:12 slope); hydraulic lift to all floors |
| 10 | **RVCE Parking Slot** | ENTRANCE | No | Yes | Flat surface; dedicated accessible bays with sidewalk ramp |
| 11 | **Tennis Court** | RECREATION | No | Yes | Paved approach; flat viewing area pathway |
| 12 | **Ganesha Temple** | RECREATION | Yes | Yes | Level pathway; ramp at temple entrance; accessible prayer area |
| 13 | **RVCE Main Ground** | RECREATION | Yes | No | Ramped pavilion; paved accessible perimeter walkway |
| 14 | **Mini Canteen** | DINING | No | Yes | Ground-level step-free access; space for mobility aids |
| 15 | **Library** | LIBRARY | Yes | Yes | Dual elevators; auto sensor doors; braille section on GF |
| 16 | **Mechanical Department Library** | LIBRARY | No | Yes | Ground-floor location; step-free access; wide reading aisles |
| 17 | **IEM Department** | ACADEMIC | Yes | Yes | Ramped south entrance; elevator within department wing |
| 18 | **Mathematics Department** | ACADEMIC | No | Yes | Ground-floor ramp; level corridor to common block |
| 19 | **Biotechnology Department** | ACADEMIC | Yes | Yes | Accessible ramp; elevator to lab floors |
| 20 | **Electronics and Instrumentation Department** | ACADEMIC | Yes | Yes | West-side ramp; lift to instrumentation lab floors |

### Accessibility Scenarios Modeled

1. **Stepped Shortcut vs. Step-Free Route**
   - `RVCE Vidyanikethan Main Gate → Library`: Long step-free boulevard with ramps vs. faster path with 12 outdoor steps through a shortcut.

2. **Covered Skywalk with Lifts**
   - `Library ↔ Computer Science and Engineering`: Enclosed level skywalk with elevator connections vs. direct courtyard path with 14 stone steps.

3. **Electric Buggy Shuttle (Transfer Tolerance)**
   - `Cauvery Block ↔ Library`: RVCE campus e-cart (5-min transfer wait) vs. longer direct 500m walkway route.

4. **Uncertain / Partially-Surveyed Pathway**
   - `RVCE Parking Slot → RV University`: Service lane with unverified accessibility status — EaseWay flags this with uncertainty warnings.

5. **Preference Inversion Demonstration**
   - When a user sets `Minimize Walking = Yes` and `Avoid Stairs = No`, route ranking dynamically flips to prefer shorter stepped paths over longer step-free alternatives.

6. **Multi-User Isolation**
   - User A (wheelchair user) prefers step-free routes with lifts. User B (staff, no restrictions) prefers shortest distance. Both profiles coexist independently — logging in as User B never changes User A's preferences.

---

## 4. User Profile & Accessibility Preferences

### Profile Attributes

- **Account Credentials**:
  - Username (login identifier — alphanumeric + underscore)
  - Password (minimum 4 characters)
- **Personal Details**:
  - Full Name (e.g. `"Kavya Ramesh"`)
  - User Role (`Student`, `Faculty`, `Mobility-Impaired Student`, `Wheelchair User`, `Visitor / Guest`)
  - USN / College ID (e.g. `"1RV22CS099"`)
  - Emergency Assistance Contact Number
- **Accessibility Preferences**:
  - Avoid Stairs: `Yes` / `No`
  - Prefer Lifts / Elevators: `Yes` / `No`
  - Minimize Walking Distance: `Yes` / `No`
  - Allow Extra Transfer Time (for campus e-cart / shuttle): `Yes` / `No`
  - Maximum Preferred Walking Distance: `50 – 5000 m`
  - Extra Transfer Time Tolerance: `0 – 60 min`

### Persistence & Error Resilience

- Each user's profile is saved to `users/<username>.properties` automatically on save/update.
- **Fault-Tolerant Parsing**: If any individual property in the file is corrupted (e.g. `pref.avoidStairs=MAYBE`), EaseWay falls back to safe defaults for that field without crashing or discarding other valid settings.
- **Reset to Defaults** (Menu Option 7): Restores all identity details and accessibility preferences to defaults. Username and password are **never** reset.
- **Password Change**: Available from Menu Option 2 → field 5. Requires the current password before accepting a new one.

---

## 5. Main Menu Options

After logging in, the main menu offers 9 options:

```
========== RVCE EASEWAY MAIN MENU ==========
1. View Profile & Accessibility Preferences
2. Edit User Profile Details (Name, USN, Role, Contact)
3. Update Accessibility Preferences
4. Find an Accessible Route (RVCE Campus)
5. Recommend Campus Facilities
6. View RVCE Campus Locations & Map
7. Reset Profile to Defaults
8. Logout (Switch User)
9. Exit
============================================
```

---

## 6. Preference-Aware Route Scoring Algorithm

`RouteEngine.java` evaluates candidate loop-free routes discovered via cycle-safe Depth-First Search (DFS) with backtracking.

Routes are scored using an objective cost function where **lower score indicates a better route match**:

$$\text{Score} = C_{\text{dist}} + C_{\text{time}} + P_{\text{stairs}} + B_{\text{lift}} + P_{\text{transfer}} + P_{\text{excess\_dist}} + P_{\text{uncertainty}}$$

| Component | Rule | Impact on Score |
| :--- | :--- | :--- |
| **Walking Distance** | Default / Normal | $+\text{distance (m)} \times 1.0$ |
| **Minimized Walking** | `minimizeWalking == true` | $+\text{distance (m)} \times 2.5$ |
| **Travel Time** | All routes | $+\text{travelTime (min)} \times 15.0$ |
| **Stairs (Strict Avoid)**| `avoidStairs == true` & stairs present | **$+2500.0$ heavy penalty** |
| **Stairs (Uncertain)** | `avoidStairs == true` & stairs unverified | **$+700.0$ uncertainty penalty** |
| **Stairs (Permitted)** | `avoidStairs == false` & stairs present | **$+40.0$ minor effort penalty** |
| **Lift Bonus** | `preferLifts == true` & lift available | **$-90.0$ preference benefit** |
| **Disallowed Transfer** | `allowExtraTransfer == false` & transfer required | **$+500.0$ penalty** |
| **Excess Transfer Time**| $\text{transferTime} > \text{allowedTolerance}$ | $+30.0$ per excess minute |
| **Excess Distance** | $\text{distance} > \text{maxWalkingDistance}$ | $+8.0$ per 10m excess |
| **Unverified Survey** | Any segment has unverified data | **$+250.0$ uncertainty penalty** |

---

## 7. How to Compile and Run

### 1-Click Launch Scripts

**On Windows:**
```cmd
run.bat
```

**On Linux / macOS:**
```bash
chmod +x run.sh
./run.sh
```

### Manual Terminal Commands

1. **Compile all source files:**
   ```bash
   javac -d bin src/com/easeway/*.java test/com/easeway/*.java
   ```

2. **Run the interactive EaseWay application:**
   ```bash
   java -cp bin com.easeway.Main
   ```

3. **Run the automated test suite:**
   ```bash
   java -cp bin com.easeway.EaseWayTest
   ```

---

## 8. Sample Console Interaction

### Registration & Login

```text
========================================================================
                                EASEWAY                                 
       Personal Accessibility Assistant — RVCE Mysuru Road Campus       
                 RV College of Engineering, Bengaluru                   
========================================================================

========== EASEWAY — USER AUTHENTICATION ==========
  1. Login with existing account
  2. Register a new account
  3. Continue as Guest (no profile saved)
  4. Exit EaseWay
===================================================
Choose an option (1-4): 2

=== REGISTER NEW ACCOUNT ===
Choose a username (letters/numbers/underscore, no spaces): kavya_r
Choose a password (min 4 characters): ****
Confirm password: ****

--- Personal Details (press Enter to use default value) ---
Full Name [RVCE Student]: Kavya Ramesh
Select Role:
  1. Student  2. Faculty  3. Mobility-Impaired Student  4. Wheelchair User  5. Visitor / Guest
Enter role (1-5): 4
USN / College ID [1RV22CS001]: 1RV22CS099
Emergency Contact [+91 98765 43210]: +91 99001 12345

[Success] Account registered and profile saved.
Welcome to EaseWay, Kavya Ramesh!
```

### Finding an Accessible Route

```text
Active User: Kavya Ramesh (@kavya_r) | Role: Wheelchair User | USN: 1RV22CS099
Preferences: Avoid Stairs: Yes | Prefer Lifts: Yes | Min Walk: No | Max Dist: 500m
------------------------------------------------------------------------

========== RVCE EASEWAY MAIN MENU ==========
...
Choose an option (1-9): 4

======================================================================
              FIND AN ACCESSIBLE ROUTE — RVCE CAMPUS
======================================================================
Available Campus Locations:
   1. Biotechnology Department              (ACADEMIC)
   2. Cauvery Block                         (RESIDENTIAL)
   3. Computer Science and Engineering      (ACADEMIC)
   ...
  20. Tennis Court                          (RECREATION)

Enter Starting Location (name or number): 4
Enter Destination (name or number): 15

Calculating accessible paths from [RVCE Vidyanikethan Main Gate] to [Library]...

========================================================================
                        TOP RECOMMENDED ROUTE                           
========================================================================
Active Preferences: Avoid Stairs=Yes | Prefer Lifts=Yes | Min Walk=No | Max Dist=500m
------------------------------------------------------------------------
Origin            : RVCE Vidyanikethan Main Gate
Destination       : Library

Recommended Pathway:
  RVCE Vidyanikethan Main Gate -> RV University -> Library

Route Summary Metrics:
  - Walking Distance        : 320 meters
  - Walking Travel Time     : 7 minutes
  - Vehicle Transfer        : None (Direct Walk)
  - Stairs Present          : No
  - Lift / Elevator Access  : Yes
  - Data Completeness       : Verified (Full Data)
  - Preference Route Score  : 335.0 points (lower score is better)

Why this route is recommended for your profile:
  * Base walking distance: 320 meters (+320 pts)
  * Estimated travel time: 7 min (+105 pts)
  * Step-free accessible route conforms perfectly to stair avoidance preference.
  * Elevator / lift available along route (-90 preference bonus)
  * Direct connection with zero vehicle/shuttle transfers.
========================================================================
```

### Logout and Switch User

```text
Choose an option (1-9): 8

========================================================================
  Logged out. Goodbye, Kavya Ramesh!
========================================================================

========== EASEWAY — USER AUTHENTICATION ==========
  1. Login with existing account
  2. Register a new account
  3. Continue as Guest (no profile saved)
  4. Exit EaseWay
===================================================
```

---

## 9. Project Contributors

| Contributor | Profile Link | Role |
| :--- | :--- | :--- |
| **Renuka Nekkalapu** | [@renukanekkalapu-png](https://github.com/renukanekkalapu-png) | Project Lead & Maintainer |
| **Paoli Patra** | [@paoli-patra26](https://github.com/paoli-patra26) | Collaborator / Contributor |
| **Tanvi J** | [@tanvij33](https://github.com/tanvij33) | Collaborator / Contributor |
| **Manasi Kittur** | [@kitturmanasi-lgtm](https://github.com/kitturmanasi-lgtm) | Collaborator / Contributor |
