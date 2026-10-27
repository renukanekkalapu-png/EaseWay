# EaseWay — Personal Accessibility Assistant
### Aligned with RV College of Engineering (RVCE), Mysuru Road Campus, Bengaluru

**EaseWay** is a simple, menu-driven, console-based Java application that recommends accessible routes and campus facilities tailored to an individual’s personal accessibility preferences.

The application has been restructured into a **beginner-friendly, compact architecture with minimal modularization (only 4 core source files + 1 test file in a single package)**, making it exceptionally easy to read, modify, and explain during a lab viva or practical demonstration.

---

## 1. Project Directory Structure

```text
EaseWay/
├── src/
│   └── com/
│       └── easeway/
│           ├── Main.java                        # Interactive console UI & program entry point
│           ├── UserProfile.java                 # Profile identity, preferences, & file persistence
│           ├── CampusModel.java                 # Domain models (Status, Category, Location, Route, PathResult)
│           ├── CampusMap.java                   # RVCE Mysuru Road campus graph & authentic routes
│           └── RouteEngine.java                 # Cycle-safe DFS routing, cost scorer, & facility recommender
├── test/
│   └── com/
│       └── easeway/
│           └── EaseWayTest.java                 # Standalone automated test suite (37 assertions)
├── bin/                                         # Compiled bytecode (.class files)
├── rvce_easeway_profile.properties              # Local persistent user profile & preferences
├── run.bat                                      # 1-click compile and launch for Windows
├── run.sh                                       # 1-click compile and launch for Linux/macOS
└── README.md                                    # Complete system documentation
```

---

## 2. RV College of Engineering (RVCE) Campus Alignment

The application features 12 authentic locations and realistic pathways from the **RVCE Mysuru Road campus in Bengaluru**:

| Location | Category | Ramp | Lift | Campus Landmarks & Trade-Offs |
| :--- | :--- | :---: | :---: | :--- |
| **Main Gate (Mysuru Road)** | ENTRANCE | Yes | No | Opposite RVCE Metro Station; level turnstiles & tactile tiles |
| **Administrative Block** | ADMINISTRATIVE | Yes | Yes | Principal's Office, Exam Section, Registrar; 1:12 slope ramp & lift |
| **Dr. A.P.J. Abdul Kalam Central Library** | LIBRARY | Yes | Yes | Central digital library; dual elevators, wide automated doors |
| **CSE & ISE Department Block** | ACADEMIC | Yes | Yes | Modern elevators on all 4 floors; skybridge to Central Library |
| **ECE & Telecom Block** | ACADEMIC | Yes | Yes | North ramped entry, elevator to upper lecture halls |
| **Mechanical & Civil Block** | ACADEMIC | Yes | Yes | Heavy labs, ramped south portico, elevator in Civil wing |
| **Sir M. Visvesvaraya Auditorium** | AUDITORIUM | Yes | Yes | 8th Mile cultural fest hall; wheelchair ramps & designated seating |
| **Campus Food Court (Munch Box)** | DINING | Yes | No | Student dining pavilion; step-free ground entrance |
| **Health Centre & Pharmacy** | HEALTHCARE | Yes | No | 24/7 clinic & pharmacy; ambulance ramp, accessible restroom |
| **Chamundi & Cauvery Hostels** | RESIDENTIAL | Yes | Yes | Student residences; Cauvery lift, ramped entrance porch |
| **Sports Complex & Cricket Ground** | RECREATION | Yes | Yes | Cricket oval, gymnasium, synthetic courts; accessible track |
| **Heritage Mechanical Workshop** | ACADEMIC | No | No | **1963 historic brick foundry: ONLY steep stone steps or unpaved lane** |

### Authentic Accessibility Scenarios Modeled:
1. **Short Stepped Shortcut vs. Longer Step-Free Route**:
   - `Main Gate -> Admin Block`: 90m shortcut with 18 stone steps vs 150m asphalt boulevard with ramps.
2. **Elevated Skybridge with Lifts**:
   - `Central Library <-> CSE & ISE Block`: Level enclosed skywalk with elevator connections.
3. **Electric Buggy Shuttle (Transfer Tolerance)**:
   - `Chamundi Hostels <-> Sports Complex`: RVCE campus e-cart (5-min transfer wait) vs 390m direct cricket oval sidewalk.
4. **Uncertain / Under-Maintenance Pathway**:
   - `Main Gate -> Sir M. Visvesvaraya Auditorium`: Metro corridor pathway under construction (accessibility unverified).
5. **Scenario with NO Fully Verified Accessible Route**:
   - `Health Centre -> Heritage Mechanical Workshop`: Historic 1963 machine building reachable only via 28 steep stone steps or an unverified service delivery lane. EaseWay clearly flags warnings and explains why no certified route exists.
6. **Preference Inversion Demonstration**:
   - When a user selects `Minimize Walking = Yes` and `Avoid Stairs = No`, the ranking dynamically flips to prefer the 90m stepped shortcut!

---

## 3. Improved User Profile & Local Persistence

The user profile combines personal identity information with accessibility preferences:

### Profile Attributes
- **Personal Details**:
  - Full Name (e.g. `"Kavya Ramesh"`)
  - User Role / Category (`Student`, `Faculty`, `Mobility-Impaired Student`, `Wheelchair User`, `Visitor / Guest`)
  - USN / College ID (e.g. `"1RV22CS099"`)
  - Emergency Assistance Contact Number (e.g. `"+91 98765 43210"`)
- **Accessibility Preferences**:
  - Avoid Stairs: `Yes` / `No`
  - Prefer Lifts: `Yes` / `No`
  - Minimize Walking Distance: `Yes` / `No`
  - Allow Extra Transfer Time (for campus shuttles / e-carts): `Yes` / `No`
  - Maximum Preferred Walking Distance (meters): `50 - 5000 m`
  - Extra Transfer Time Tolerance (minutes): `0 - 60 min`

### Persistence & Error Resilience
- Saved locally to `rvce_easeway_profile.properties`.
- **Fault-Tolerant Parsing**: If any individual property in the file is corrupted (e.g. `pref.avoidStairs=MAYBE` or `pref.maxWalkingDistanceMeters=INVALID`), EaseWay automatically falls back to safe defaults for that specific attribute without crashing or losing the remaining valid settings.
- **Reset to Defaults**: Menu Option 7 allows one-click restoration of all identity details and accessibility preferences after explicit confirmation.

---

## 4. Preference-Aware Route Scoring Algorithm

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

## 5. How to Compile and Run

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

1. **Compile all files:**
   ```bash
   javac -d bin src/com/easeway/*.java test/com/easeway/*.java
   ```

2. **Run the interactive EaseWay application:**
   ```bash
   java -cp bin com.easeway.Main
   ```

3. **Run the automated test suite (37 tests):**
   ```bash
   java -cp bin com.easeway.EaseWayTest
   ```

---

## 6. Sample Console Interaction

```text
========================================================================
                                EASEWAY                                 
       Personal Accessibility Assistant — RVCE Mysuru Road Campus       
                 RV College of Engineering, Bengaluru                   
========================================================================
Active User: Kavya Ramesh | Role: Wheelchair User | USN: 1RV22CS099
Preferences: Avoid Stairs: Yes | Prefer Lifts: Yes | Min Walk: No | Max Dist: 500m
------------------------------------------------------------------------

========== RVCE EASEWAY MAIN MENU ==========
1. View Profile & Accessibility Preferences
2. Edit User Profile Details (Name, USN, Role, Contact)
3. Update Accessibility Preferences
4. Find an Accessible Route (RVCE Campus)
5. Recommend Campus Facilities
6. View RVCE Campus Locations & Map
7. Reset Profile to Defaults
8. Exit
============================================
Choose an option (1-8): 4

Enter Starting Location: Main Gate (Mysuru Road)
Enter Destination: Dr. A.P.J. Abdul Kalam Central Library

========================================================================
                        TOP RECOMMENDED ROUTE                           
========================================================================
Starting location : Main Gate (Mysuru Road)
Destination       : Dr. A.P.J. Abdul Kalam Central Library

Recommended Pathway:
  Main Gate (Mysuru Road) -> Administrative Block -> Dr. A.P.J. Abdul Kalam Central Library

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

---

## 7. Verification Test Suite Output

Running `java -cp bin com.easeway.EaseWayTest`:

```text
========================================================================
          EASEWAY (RVCE CAMPUS) — AUTOMATED TEST SUITE                  
========================================================================
[PASS] Default Profile Name
[PASS] Default Profile Role
[PASS] Default Profile USN
[PASS] Default Profile Avoid Stairs
[PASS] Default Profile Prefer Lifts
[PASS] Default Profile Max Distance
[PASS] Save Profile Details to disk
[PASS] Loaded Full Name matches
[PASS] Loaded Role matches
[PASS] Loaded USN matches
[PASS] Loaded Emergency Contact matches
[PASS] Loaded Max Distance matches
[PASS] Routes exist between Main Gate and Central Library
[PASS] Top route avoids stairs when profile avoids stairs
[PASS] Top route provides lift when profile prefers lifts
[PASS] Stair-averse user chooses 150m ramped boulevard without stairs
[PASS] Distance-minimizer flips ranking to 90m shortcut with stone steps
[PASS] User who disallows transfers receives direct walk without vehicle transfers
[PASS] Electric buggy shuttle route evaluated when transfers are allowed
[PASS] Top route is fully verified via Admin Block when accessible alternative exists
[PASS] Unverified route carries uncertainty advisory warnings
[PASS] Routes to Heritage Workshop exist
[PASS] Correctly identifies that NO certified stair-free route exists to Heritage Workshop
[PASS] Top route to Heritage Workshop carries warning advisories
[PASS] Corrupted avoidStairs safely falls back to default
[PASS] Corrupted maxDistance safely falls back to default
[PASS] Valid name and lift preference in corrupt file preserved
[PASS] Reset restores default name
[PASS] Reset restores default avoidStairs
[PASS] Reset restores default maxDistance
[PASS] Recommendations generated for all other RVCE locations
[PASS] Stair-free facilities filter contains facilities
[PASS] Category filter for DINING finds Campus Food Court (Munch Box)
[PASS] Facilities with incomplete survey data identifies Heritage Workshop
[PASS] Same start and destination returns empty list
[PASS] Null start returns empty list without exception
[PASS] Non-existent location lookup returns null safely
========================================================================
Test Results: 37 Passed | 0 Failed
========================================================================
```
